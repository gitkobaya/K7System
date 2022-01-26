package k7system;

import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.gpuobjects.DepthMaterial;
import k7system.gpuobjects.FrameBufferObject;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** カメラを表現するオブジェクトです */
public class CameraObject{
    private float[] cameraPosition={0,0,0};
    private float[] cameraTarget={0,0,-1};
    private float[] cameraUpper={0,1,0};

    private float left=-1;
    private float right=1;
    private float bottom=-1;
    private float top=1;
    private float near=1; // 近距離クリッピング距離です
    private float far=-1;  // 遠距離クリッピング距離です

    // 行列関係
    private float[] perspectiveMatrix=VectorManager.createIdentityMatrix(4); // 透視行列
    private float[] viewMatrix=VectorManager.createIdentityMatrix(4); // ビュー行列
    private float[] pvMatrix=VectorManager.createIdentityMatrix(4); // 透視ビュー行列（pvをかけたもの）

    // デプスバッファ用
    public static final DepthMaterial DEFAULT_DEPTH_MATERIAL;
    private FrameBufferObject depthFbo=null;

    /** 深度マテリアルを設定します．これは使いまわすのでstatic宣言しています */
    static{
        String dummy="DUMMY DATA";
        DEFAULT_DEPTH_MATERIAL=new DepthMaterial();
        DEFAULT_DEPTH_MATERIAL.addParent(dummy); // デフォルトマテリアルが抹消されないようにダミーデータを登録しておきます
        DEFAULT_DEPTH_MATERIAL.setName("Depth for Camera");
    }
    private DepthMaterial depthMat=DEFAULT_DEPTH_MATERIAL;

    private GraphicEngine engine;

    /** コンストラクタでエンジンを指定します */
    public CameraObject(GraphicEngine engine) {
        this.engine=engine;
        this.depthFbo=new FrameBufferObject(Params.DEFAULT_SHADOW_BUFFER_SIZE, Params.DEFAULT_SHADOW_BUFFER_SIZE);
    }

    /** 視点の位置を取得します<br>
     * 返り値の行列はクローンなので，それを直接操作しても視点位置は変更されません． */
    public float[] getCameraPosition(){
        return this.cameraPosition.clone();
    }

    /** 視点パラメータを取得します<br>
     * 配列にはleft, right, bottom, top, near, farの順番にデータが入っています */
    public float[] getCameraParameters(){
        return new float[]{this.left, this.right, this.bottom, this.top, this.near, this.far};
    }

    /** カメラの視錐台の頂点を取得します<br>
     * 順番は，near側の右上，左上，左下，右下，far側の右上，左上，左下，右下となっています<br>
     * 全て視点座標系です */
    public float[][] getCameraFrustum(){
        float curRight=right;
        float curLeft=left;
        float aspect=this.engine.getAspect();

        if (this.engine.isAutoAspect()){
            curRight=right*aspect;
            curLeft=left*aspect;
        }

        float[] nRT=new float[]{curRight, this.top, this.near};
        float[] nLT=new float[]{curLeft, this.top, this.near};
        float[] nLB=new float[]{curLeft, this.bottom, this.near};
        float[] nRB=new float[]{curRight, this.bottom, this.near};
        float zoom=1;
        if (this.engine.getProjectionMode()==GraphicEngine.PERSPECTIVE){
            zoom=this.far/this.near;
        }
        float[] fRT=new float[]{curRight*zoom, this.top*zoom, this.far};
        float[] fLT=new float[]{curLeft*zoom, this.top*zoom, this.far};
        float[] fLB=new float[]{curLeft*zoom, this.bottom*zoom, this.far};
        float[] fRB=new float[]{curRight*zoom, this.bottom*zoom, this.far};
        return new float[][]{nRT, nLT, nLB, nRB, fRT, fLT, fLB, fRB};
    }

    /** カメラの視錐台の重心座標を取得します<br>
     * 単に頂点座標の平均ですので，実際の重心よりは視点側に寄っています */
    public float[] getFrustumCenter(){
        float[][] frustum=this.getCameraFrustum();

        float cx=0,cy=0,cz=0;
        for(float[] pos:frustum){
            cx+=pos[0];
            cy+=pos[1];
            cz+=pos[2];
        }
        cx/=8;
        cy/=8;
        cz/=8;
        return new float[]{cx,cy,cz};
    }

    /** カメラの視錐台の重心座標をワールド座標系で取得します */
    public float[] getFrustumCenterWorld(){
        float distance=(this.far-this.near)/2; // 視錐台重心までの距離
        float[] cameraVec=VectorManager.normalize3(VectorManager.sub(this.getCameraTarget(), this.getCameraPosition()));
        float[] center=VectorManager.add(this.getCameraPosition(), VectorManager.multVectorValue(cameraVec, distance));

        return center;
    }

    /** 視点パラメータを設定します */
    public void setCameraParameters(float left, float right, float bottom, float top, float near, float far){
        this.left=left;
        this.right=right;
        this.bottom=bottom;
        this.top=top;
        this.near=near;
        this.far=far;
        this.refreshPerspectiveMatrix();
    }

    /** 視点の位置を設定します */
    public void setCameraPosition(float[] position){
        cameraPosition[0]=position[0];
        cameraPosition[1]=position[1];
        cameraPosition[2]=position[2];
        this.createViewMatrix();
    }

    /** 注視点を取得します<br>
     * 返り値の行列はクローンなので，それを直接操作しても注視点は変更されません． */
    public float[] getCameraTarget(){
        return this.cameraTarget.clone();
    }

    /** 注視点を設定します */
    public void setCameraTarget(float[] target){
        cameraTarget[0]=target[0];
        cameraTarget[1]=target[1];
        cameraTarget[2]=target[2];
        this.createViewMatrix();
    }

    /** 画面の上方向ベクトルを取得します<BR>
     * 返り値の行列はクローンなので，それを直接操作しても画面の上方向ベクトルは変更されません． */
    public float[] getCameraUpper(){
        return this.cameraUpper.clone();
    }

    /** 画面の上方向ベクトルを設定します */
    public void setCameraUpper(float[] upper){
        cameraUpper[0]=upper[0];
        cameraUpper[1]=upper[1];
        cameraUpper[2]=upper[2];
        this.createViewMatrix();
    }

    /** 視点の位置と注視点，画面上での上をセットで設定します */
    public void setCameraPosition(float[] position,float[] target,float[] upper){
        cameraPosition[0]=position[0];
        cameraPosition[1]=position[1];
        cameraPosition[2]=position[2];
        cameraTarget[0]=target[0];
        cameraTarget[1]=target[1];
        cameraTarget[2]=target[2];
        cameraUpper[0]=upper[0];
        cameraUpper[1]=upper[1];
        cameraUpper[2]=upper[2];
        this.createViewMatrix();
    }


    /** ビュー変換行列を取得します<br>
     * この行列をワールド座標の左側に掛けるとカメラ座標系になります */
    public float[] getViewMatrix(){
        return this.viewMatrix;
    }

    /** 透視変換，ビュー変換行列を掛けた行列を取得します<br>
     * この行列をワールド座標の左側に掛けるとスクリーン座標系になります */
    public float[] getPVMatrix(){
        return this.pvMatrix.clone();
    }

    /** 透視変換，ビュー変換行列を掛けた行列を設定します */
    protected void setPVMatrix(float[] mat){
        this.pvMatrix=mat;
    }

    /** 視点座標変換行列を生成します */
    public void createViewMatrix(){
        // ビュー行列の生成
        this.viewMatrix=createViewMatrix(this.cameraPosition, this.cameraTarget, this.cameraUpper);
    }

    /** 透視行列を取得します<br>
     * クローンで渡しているので内容を操作しても実際の表示には影響ありません */
    public float[] getPerspectiveMatrix(){
        return this.perspectiveMatrix.clone();
    }

    /** カメラパラメーターから透視変換行列を作成します */
    public float[] refreshPerspectiveMatrix(){
        this.perspectiveMatrix=createPerspectiveMatrix(left, right, bottom, top, near, far, this.engine.getProjectionMode(), this.engine.isAutoAspect(), this.engine.getAspect());
        return this.perspectiveMatrix;
    }

    /** カメラパラメーターから透視変換行列を作成します */
    public static float[] createPerspectiveMatrix(float left, float right, float bottom, float top, float near, float far, int projectionMode, boolean autoAspect, float aspect){
        float[] perspectiveMatrix=new float[16];
        float curRight=right;
        float curLeft=left;
        if (autoAspect){
            curRight=right*aspect;
            curLeft=left*aspect;
        }

        if (projectionMode==GraphicEngine.PERSPECTIVE){
            perspectiveMatrix[0]=(2*near)/(curRight-curLeft);
            perspectiveMatrix[1]=0;
            perspectiveMatrix[2]=0;
            perspectiveMatrix[3]=0;

            perspectiveMatrix[4]=0;
            perspectiveMatrix[5]=(2*near)/(top-bottom);
            perspectiveMatrix[6]=0;
            perspectiveMatrix[7]=0;

            perspectiveMatrix[8]=(curRight+curLeft)/(curRight-curLeft);
            perspectiveMatrix[9]=(top+bottom)/(top-bottom);
            perspectiveMatrix[10]=-(far+near)/(far-near);
            perspectiveMatrix[11]=-1;

            perspectiveMatrix[12]=0;
            perspectiveMatrix[13]=0;
            perspectiveMatrix[14]=-2*far*near/(far-near);
            perspectiveMatrix[15]=0;
        }else{
            perspectiveMatrix[0]=2/(curRight-curLeft);
            perspectiveMatrix[1]=0;
            perspectiveMatrix[2]=0;
            perspectiveMatrix[3]=0;

            perspectiveMatrix[4]=0;
            perspectiveMatrix[5]=2/(top-bottom);
            perspectiveMatrix[6]=0;
            perspectiveMatrix[7]=0;

            perspectiveMatrix[8]=0;
            perspectiveMatrix[9]=0;
            perspectiveMatrix[10]=-2/(far-near);
            perspectiveMatrix[11]=0;

            perspectiveMatrix[12]=-(curRight+curLeft)/(curRight-curLeft);
            perspectiveMatrix[13]=-(top+bottom)/(top-bottom);
            perspectiveMatrix[14]=-(far+near)/(far-near);
            perspectiveMatrix[15]=1;
        }
        return perspectiveMatrix;
    }

    /** カメラ情報から視点座標変換行列を生成します */
    public static float[] createViewMatrix(float cameraX, float cameraY, float cameraZ, float targetX, float targetY, float targetZ, float upperX, float upperY, float upperZ){
        float[] viewMatrix=new float[16];
        viewMatrix=createViewMatrix(new float[]{cameraX, cameraY, cameraZ},new float[]{targetX, targetY, targetZ}, new float[]{upperX, upperY, upperZ});
        return viewMatrix;
    }

    /** カメラ情報から視点座標変換行列を生成します */
    public static float[] createViewMatrix(float[] cameraPosition, float[] cameraTarget, float[] cameraUpper){
        float[] viewMatrix=new float[16];
        // ビュー行列の生成
        float[] zAxis=VectorManager.normalize3(VectorManager.sub(cameraPosition, cameraTarget));
        float[] xAxis=VectorManager.normalize3(VectorManager.cross(cameraUpper, zAxis));
        float[] yAxis=VectorManager.normalize3(VectorManager.cross(zAxis, xAxis));

        viewMatrix[0]=xAxis[0];
        viewMatrix[4]=xAxis[1];
        viewMatrix[8]=xAxis[2];
        viewMatrix[12]=-VectorManager.dot(cameraPosition, xAxis);

        viewMatrix[1]=yAxis[0];
        viewMatrix[5]=yAxis[1];
        viewMatrix[9]=yAxis[2];
        viewMatrix[13]=-VectorManager.dot(cameraPosition, yAxis);

        viewMatrix[2]=zAxis[0];
        viewMatrix[6]=zAxis[1];
        viewMatrix[10]=zAxis[2];
        viewMatrix[14]=-VectorManager.dot(cameraPosition, zAxis);

        viewMatrix[3]=0;
        viewMatrix[7]=0;
        viewMatrix[11]=0;
        viewMatrix[15]=1;
        return viewMatrix;
    }

    /** このカメラオブジェクトの深度バッファ出力用FBOを取得します */
    public FrameBufferObject getDepthFbo(){
        return this.depthFbo;
    }

    /** このライトオブジェクトの視点によるシャドウバッファを描画します<br>
     * 影生成属性が付いていなければ何も処理が行われません */
    public void drawDepthBuffer(GL3 gl){
        if (!this.depthMat.isUploaded()){
            this.depthMat.init(gl, this.engine);
        }
        if (!this.depthFbo.isUploaded()){
            this.depthFbo.init(gl, this.engine);
        }

        this.depthFbo.bind(gl);
        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_CULL_FACE);              // 裏返ったポリゴンを描画しません

        List<Object3D> models=this.engine.getModels();
        for (Object3D model:models){
            if (model instanceof Model3D && model.isShadowable()){ // モデルが影の対象か
                Model3D m3d=(Model3D)model;
                List<VertexPackage> pkgs=m3d.getVertexPackages(m3d.getAllVertexPackages().size()-1);
                for (VertexPackage p:pkgs){
                    float[] mvp=VectorManager.multMatrix4(this.pvMatrix, m3d.getWorldMatrix());
                    VertexArrayObject vao=p.getVao(); // 形状データを読み込みまして
                    this.depthMat.setMvpMatrix(mvp); // マテリアルに行列を設定しまして
                    this.depthMat.bind(gl);
                    vao.draw(gl);
                    this.depthMat.unbind(gl);
                }
            }
        }
        this.depthFbo.unbind(gl);
        gl.glViewport(0, 0, this.engine.getScreenWidth(), this.engine.getScreenHeight()); // ビューポートを戻しておく
    }


}
