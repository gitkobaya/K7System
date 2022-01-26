package k7system;

import java.util.List;




import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.gpuobjects.FloatTextureK7;
import k7system.gpuobjects.FrameBufferObject;
import k7system.gpuobjects.GPUResource;
import k7system.gpuobjects.DepthMaterial;
import k7system.gpuobjects.TextureK7;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** 照明のオブジェクトです<br>
 * デフォルトでは白色の光が設定されます． */
public class LightObject extends GPUResource{
    private static DepthMaterial DEFAULT_SHADOW_MATERIAL;

    private float[] position=new float[]{0,0,0,1};
    private float[] power=new float[]{1,1,1,1};
    private float[] ambient=new float[]{0,0,0,1};

    /** このフラグがtrueになっている場合，照明が有効になります */
    private boolean lighting=false;// 照明フラグ

    /** このフラグがtrueになっている場合，影を生成する光源として扱われます */
    private boolean shadowing=false; // 影フラグ
    private float[] perspective=new float[16]; // このライトの透視変換行列(影生成用)
    private float[] pvMatrix=new float[16]; // このライトのPV行列(影生成用)
    private float[] parameters=new float[6]; // このライトを視点とした時のカメラパラメーター(left, right, bottom, top, near, far)
    private FrameBufferObject shadowFbo=null;
    private DepthMaterial shadowMaterial=null;

    private GraphicEngine gEngine=null;

    /** シャドウマテリアルを設定します．これは使いまわすのでstatic宣言しています */
    static{
        String dummy="DUMMY DATA";
        DEFAULT_SHADOW_MATERIAL=new DepthMaterial();
        DEFAULT_SHADOW_MATERIAL.addParent(dummy); // デフォルトマテリアルが抹消されないようにダミーデータを登録しておきます
    }

    /** ライトの位置を取得します */
    public float[] getLightPosition(){
        return this.position;
    }

    /** ライトの位置をワールド座標で設定します<br>
     * 最後の要素が1なら点光源，1なら平行光源になります */
    public void setLightPosition(float[] pos){
        this.position[0]=pos[0];
        this.position[1]=pos[1];
        this.position[2]=pos[2];
        if (pos.length>3){
            this.position[3]=pos[3];
        }
    }

    /** このライトオブジェクトの影生成用FBOを取得します */
    public FrameBufferObject getShadowFbo(){
        return this.shadowFbo;
    }

    /** このライトオブジェクトに影生成用FBOを設定します */
    public void setShadowFbo(FrameBufferObject fbo){
        this.shadowFbo=fbo;
    }

    /** このライトオブジェクトの視点によるシャドウバッファを描画します<br>
     * 影生成属性が付いていなければ何も処理が行われません */
    public void drawShadowBuffer(GL3 gl){
        if (this.shadowing){
            if (!this.shadowMaterial.isUploaded()){
                this.shadowMaterial.init(gl, this.getEngine());
            }
            if (!this.shadowFbo.isUploaded()){
                this.shadowFbo.init(gl, this.getEngine());
            }

            this.shadowFbo.bind(gl);
            gl.glClearColor(0, 0, 0, 0);
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
            gl.glEnable(GL.GL_CULL_FACE);              // 裏返ったポリゴンを描画しません

            List<Object3D> models=this.getEngine().getModels();
            for (Object3D model:models){
                if (model instanceof Model3D && model.isShadowable()){ // モデルが影の対象か
                    Model3D m3d=(Model3D)model;
                    List<VertexPackage> pkgs=m3d.getVertexPackages(m3d.getAllVertexPackages().size()-1);
                    for (VertexPackage p:pkgs){
                        float[] mvp=VectorManager.multMatrix4(this.createLightPVMatrix(), m3d.getWorldMatrix());
                        VertexArrayObject vao=p.getVao(); // 形状データを読み込みまして
                        this.shadowMaterial.setMvpMatrix(mvp); // マテリアルに行列を設定しまして
                        this.shadowMaterial.bind(gl);
                        vao.draw(gl);
                        this.shadowMaterial.unbind(gl);
                    }
                }
            }
            this.shadowFbo.unbind(gl);
            gl.glViewport(0, 0, this.getEngine().getScreenWidth(), this.getEngine().getScreenHeight()); // ビューポートを戻しておく
        }
    }

    /** カメラとライトの深度バッファを比較し，シャドウバッファを作成します */
    public TextureK7 createShadowBuffer(TextureK7 cameraDepth, TextureK7 lightDepth){

        return null;
    }

    /** ライトを光源とした場合のPV行列を取得し，内部変数に設定します<br>
     * 並行光源及びスポットライトの場合に利用するメソッドであり，ここで作成された行列にワールド座標を掛けることで光源視点座標系になります  */
    public float[] createLightPVMatrix(){
        GraphicEngine engine=this.getEngine();
        float[] lightVec=new float[]{position[0], position[1], position[2], 0}; // この時点ではワールド座標
        float[] lightUpper=engine.getCameraUpper(); // この時点ではワールド座標
        float[] perspective=this.createLightPerspectiveMatrix(engine, this.position, lightVec, lightUpper);
        float[] viewMatix=VectorManager.createIdentityMatrix(4);
        float[] pvMatrix=null;

        // 平行光源の場合
        if (position[3]==0){
            float[] centerWorld=engine.getFrustumCenterWorld();
            // 視点光源カメラの位置(視錐台重心)，注視点，上ベクトルをワールド座標系で指定
            float[] gaiseki=VectorManager.cross(this.position, lightUpper);
            if (gaiseki[0]<0.0001f  && gaiseki[1]<0.0001f && gaiseki[2]<0.0001f){
                lightUpper=VectorManager.sub(engine.getCameraTarget(), engine.getCameraPosition());
            }
            viewMatix=CameraObject.createViewMatrix(centerWorld, VectorManager.add(centerWorld, this.position), lightUpper);
        }else{
            // 点光源の場合 TBD
            // スポットライトの場合 TBD
        }

        pvMatrix=VectorManager.multMatrix4(perspective, viewMatix);
        this.pvMatrix=pvMatrix;
        return pvMatrix;
    }

    /** ライトからの視界行列を取得し，内部変数に設定します<br>
     * 平行光源及びスポットライトの場合に利用するメソッドであり，平行光源の場合，光源からの視錐柱はカメラの視錐台を含む大きさに設定されます．
     * また，計算を簡単にするため，光源ベクトルがカメラのY軸と一致してない限り，上ベクトルはカメラの上方向と一致させます．<br>
     * もし光源ベクトルとカメラのY軸が一致していると破綻するため，その際はカメラの奥行き方向を光源視界の上方向とします． */
    public float[] createLightPerspectiveMatrix(GraphicEngine engine, float[] pos, float[] vec, float[] upper){
        float[] cameraView=engine.getViewMatrix();

        // 加工するので(実際には必要ないが)念のためコピーしておく
        float[] lightVec=vec.clone();
        float[] lightUpper=upper.clone();

        // 光源視点パラメーターを視点座標系に変換しておく
        lightUpper=VectorManager.normalize3(VectorManager.multMatrixVec4(cameraView, new float[]{lightUpper[0], lightUpper[1],lightUpper[2],0})); // 視点座標系でのベクトルになる
        lightVec=VectorManager.normalize3(VectorManager.multMatrixVec4(cameraView, lightVec)); // 視点座標系でのベクトルになる

        // 平行光源の場合
        if (this.position[3]==0){
            float[][] cFrustum=engine.getCameraFrustum(); // カメラの視錐台の各頂点の座標を取得
            this.parameters=createLightViewParameters(cFrustum, lightVec, lightUpper); // 光源視点でのカメラパラメーターを取得
            float left=this.parameters[0];
            float right=this.parameters[1];
            float bottom=this.parameters[2];
            float top=this.parameters[3];
            float near=this.parameters[4];
            float far=this.parameters[5];

            this.perspective=CameraObject.createPerspectiveMatrix(left, right, bottom, top, near, far, GraphicEngine.ORTHO, false, 0.0f);
        }
        return this.perspective;
    }

    /** 光源用の視点パラメーターを取得します<br>
     * パラメーターは視錐台の各頂点（視点座標系），光源ベクトル （視点座標系），光源上ベクトル（視点座標系）です*/
    public float[] createLightViewParameters(float[][] cFrustum, float[] lightVec, float[] lightUpper){
        float[] lightX=VectorManager.normalize3(VectorManager.cross( lightUpper ,lightVec)); // 視点座標系での光源座標系のX軸
        lightUpper=VectorManager.cross(lightX, lightVec);

        // 重心を求める(この点を光源スクリーンの基準にする)
        float cx=0,cy=0,cz=0;
        for(float[] pos:cFrustum){
            cx+=pos[0];
            cy+=pos[1];
            cz+=pos[2];
        }
        cx/=8;
        cy/=8;
        cz/=8;
        float[] camera=new float[]{cx, cy, cz}; // 視点座標系における視錐台重心

        // さっきの各頂点を光源座標系に変換する
        float maxX=-Float.MAX_VALUE;
        float maxY=-Float.MAX_VALUE;
        float maxZ=-Float.MAX_VALUE;
        float minX=Float.MAX_VALUE;
        float minY=Float.MAX_VALUE;
        float minZ=Float.MAX_VALUE;
        for(float[] pos:cFrustum){
            float[] vtxPos=VectorManager.sub(pos, camera); // 光源視点を原点にする
            float x=VectorManager.dot(vtxPos, lightX);
            float y=VectorManager.dot(vtxPos, lightUpper);
            float z=VectorManager.dot(vtxPos, lightVec);

            if (x<minX){
                minX=x;
            }
            if (maxX<x){
                maxX=x;
            }
            if (y<minY){
                minY=y;
            }
            if (maxY<y){
                maxY=y;
            }
            if (z<minZ){
                minZ=z;
            }
            if (maxZ<z){
                maxZ=z;
            }
        }
        return new float[]{minX, maxX, minY, maxY, minZ, maxZ};
    }

    /** 光源パラメータを行列の形で取得します<br>
     * 最初の4要素が位置，以降diffuse, ambient, 予備となります．
     * 位置ベクトルの4番目の要素が1なら位置，0ならベクトルとして解釈されます．<br>
     * 光源がdisableだった場合，空行列が返ります． */
    public float[] getLightParameters(){
        float[] result=new float[16];
        if (this.lighting){
            result=new float[]{
                    this.position[0],this.position[1],this.position[2],this.position[3],
                    this.power[0],this.power[1],this.power[2],this.power[3],
                    this.ambient[0],this.ambient[1],this.ambient[2],this.ambient[3],
                    0,0,0,0
            };
        }
        return result;
    }

    /** 光源パラメータを行列の形で設定します<br>
     * 最初の4要素が位置，以降diffuse, ambient, 予備となります．
     * 位置ベクトルの4番目の要素が1なら位置，0ならベクトルとして解釈されます．<br>
     * 光源がdisableだった場合，空行列が返ります． */
    public void setLightParameters(float[] power){
        power=power.clone();
    }

    /** 光の強さを取得します */
    public float[] getPower() {
        return power;
    }

    /** 光の強さを設定します */
    public void setPower(float[] diffuse) {
        this.power[0]=diffuse[0];
        this.power[1]=diffuse[1];
        this.power[2]=diffuse[2];
        if (diffuse.length>3){
            this.power[3]=diffuse[3];
        }
    }

    /** 環境光を取得します */
    public float[] getAmbient() {
        return ambient;
    }

    /** 環境光を設定します */
    public void setAmbient(float[] ambient) {
        this.ambient[0]=ambient[0];
        this.ambient[1]=ambient[1];
        this.ambient[2]=ambient[2];
        if (ambient.length>3){
            this.ambient[3]=ambient[3];
        }
    }

    /** ライトを有効にします */
    public void enable(){
        this.lighting = true;
    }

    /** ライトを無効にします */
    public void disable(){
        this.lighting = false;
    }

    /** 照明フラグを取得します */
    public boolean isLighting() {
        return lighting;
    }

    /** 影フラグを設定します<br>
     * 影フラグが設定された照明は影を落とすようになります．<br>
     * ただし，その分負荷が大きくなります */
    public void setShadowFlag(boolean flag){
        this.setShadowFlag(flag, Params.DEFAULT_SHADOW_BUFFER_SIZE);
    }

    /** 影フラグを設定します<br>
     * 影フラグが設定された照明は影を落とすようになります．<br>
     * ただし，その分負荷が大きくなります */
    public void setShadowFlag(boolean flag, int size){
        if (!this.shadowing && flag){ // オフからオンになった場合
            this.setShadowFlag(Params.DEFAULT_SHADOW_BUFFER_SIZE);
        }else if (this.shadowing && !flag){ // オンからオフになった場合
            this.shadowFbo.removeParent(this);
            this.shadowFbo=null;
        }
        this.shadowing=flag;
    }

    /** 影フラグを指定したサイズで設定します<br>
     * 影フラグが設定された照明は影を落とすようになります．<br>
     * ただし，その分負荷が大きくなります */
    public void setShadowFlag(int size){
        // シャドウバッファのサイズ変更
        if (this.shadowFbo!=null){
            this.shadowFbo.removeParent(this);
        }
        FloatTextureK7 shadowTexture=new FloatTextureK7(size, size, 1);
        this.shadowFbo=new FrameBufferObject(shadowTexture);
        this.shadowMaterial=DEFAULT_SHADOW_MATERIAL;
    }

    /** 影フラグを取得します */
    public boolean isShadowing(){
        return this.shadowing;
    }

    /** ライトをグラフィックエンジンから削除します */
    public void remove(){
        gEngine.removeLightObject(this);
    }

    /** 描画エンジンを設定します<br>
     * エンジンによって呼び出されるためユーザーが指定する必要はありません */
    protected void setEngine(GraphicEngine ge){
        gEngine=ge;
    }

    @Override
    public GraphicEngine getEngine() {
        return this.gEngine;
    }

    @Override
    public void vramFlushed() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void dispose(GL3 gl) {
        // TODO 自動生成されたメソッド・スタブ

    }
}
