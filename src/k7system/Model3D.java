package k7system;

import java.util.ArrayList;
import java.util.List;



import com.jogamp.opengl.GL3;

import k7system.collision.CollisionElement;
import k7system.collision.CollisionObject;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexPackage;


/** ゲーム用3Dモデルです<br>
 * GraphicEngineの存在が前提になります<br>
 * モデルは一つまたは複数の頂点パッケージリストで構成されます．
 * 頂点パッケージリストは，一つまたは複数の頂点パッケージで構成されます．
 * 頂点パッケージがリストになっているのは，LODに対応するためです．*/
public class Model3D extends Object3D implements CollisionObject{
    public static final int NUM_OF_MAX_LODS=8;

    private List<List<VertexPackage>> vPacksList=new ArrayList<List<VertexPackage>>(); // このモデルに含まれる頂点パッケージ
    private float[] lodDepthArray=new float[]{Float.MAX_VALUE}; // LoD切り替え深度の配列です n番目のLoDモデルが担当する範囲が設定されます
    private int currentLod=0; // 現在のLoDレベルです

    private float[] viewPos=new float[4]; // 視点座標系での座標です


    /** コンストラクタです */
    public Model3D() {
        vPacksList.add(new ArrayList<VertexPackage>()); // LODゼロの頂点パッケージリストを準備します
    }

    /** 呼び出し元のグラフィックエンジンを設定します */
    @Override
    public void setEngine(GraphicEngine ge) {
        super.setEngine(ge);
        if (ge!=null){
            ge.addModel(this);
        }
    }


    /** 全てのLoDを含む頂点パッケージを取得します */
    public List<List<VertexPackage>> getAllVertexPackages(){
        return this.vPacksList;
    }

    /** 頂点パッケージを登録します<br>
     * 特に指定がなければ0番に登録されます */
    public void setVertexPackages(List<VertexPackage> packs){
        this.vPacksList.set(0, packs);
    }

    /** 頂点パッケージ群を取得します<br>
     * LoD0番の頂点パッケージ群が返ります．*/
    public List<VertexPackage> getVertexPackages(){
        return getVertexPackages(0);
    }

    /** 指定したLoDに対応する頂点パッケージ群を取得します<br>
     * もし指定したLoDに対応する頂点パッケージ群が存在しない場合，指定されたLoDより詳細な，最も近い頂点パッケージ群を指定します．<br>
     * 例として，LoD 0,1,5に対応する頂点パッケージ群が登録されている場合に引数4を指定すると，LoD1の頂点パッケージ群が返ります．*/
    public List<VertexPackage> getVertexPackages(int lod){
        List<VertexPackage> result=null;

        // そのレベルの頂点パッケージ群を取得．無いなら1段階細かいものを取得
        while(result==null){
            if (lod<this.vPacksList.size()){
                result=this.vPacksList.get(lod);
                lod--;
            }else{
                lod=this.vPacksList.size()-1;
            }
        }
        return result;
    }

    /** 頂点パッケージを追加します<br>
     * 特に指定がなければ0番に登録されます */
    public void addVertexPackage(VertexPackage pack){
        this.vPacksList.get(0).add(pack);
    }

    /** 頂点パッケージを指定したLoDに追加します */
    public void addVertexPackage(VertexPackage pack, int lod){
        while(this.vPacksList.size()<=lod){
            this.vPacksList.add(null); // 数を合わせる
        }
        List<VertexPackage> target=this.vPacksList.get(lod);
        if (target==null){
            this.vPacksList.set(lod, new ArrayList<VertexPackage>());
            target=this.vPacksList.get(lod);
        }
        target.add(pack);
    }

    /** 現在のLoDレベルを取得するメソッドです*/
    public int getCurrentLod(){
        return this.currentLod;
    }

    /** 現在のLoDレベルを確認するメソッドです<br>
     * 引数には視点座標系でのZ座標の絶対値が入ります */
    private int checkCurrentLod(float zDepth){
        int result=0;
        for(int i=0;i<this.lodDepthArray.length;i++){
            result=i;
            if (zDepth<this.lodDepthArray[i]){
                break;
            }
        }
        this.currentLod=result;
        return result;
    }

    /** LoD切り替え深度を配列で指定します<br>
     * 深度切り替えの数値が昇順に並んでいる必要があります．n個のLoDモデルを用意した場合，要素n-1にはFloat.MAX_VALUEを設定してください．
     * 最も詳細なLoDモデルが0番となります．<br>
     * デフォルトでは要素数1の配列が指定されており，それが全ての深度値をカバーします．*/
    public void setLodChangeDepth(float[] depthArray){
        this.lodDepthArray=depthArray;
    }

    /** このモデルのバウンディング境界をモデル座標系で取得します<br>
     * 返り値は、minX,minY,minZ,maxX,maxY,maxZの順で値が収められた配列です */
    public double[] getLocalBoundByArray(){
        double minX=Double.MAX_VALUE;
        double minY=Double.MAX_VALUE;
        double minZ=Double.MAX_VALUE;
        double maxX=Double.MIN_VALUE;
        double maxY=Double.MIN_VALUE;
        double maxZ=Double.MIN_VALUE;
        for(VertexPackage pack:this.getVertexPackages()){
            float[] aabb=pack.getBoundingBoxFlat();
            if (aabb[0]<minX){
                minX=aabb[0];
            }
            if (aabb[1]<minY){
                minY=aabb[1];
            }
            if (aabb[2]<minZ){
                minZ=aabb[2];
            }
            if (aabb[3]>maxX){
                maxX=aabb[3];
            }
            if (aabb[4]>maxY){
                maxY=aabb[4];
            }
            if (aabb[5]>maxZ){
                maxZ=aabb[5];
            }
        }
        return new double[]{minX,minY,minZ,maxX,maxY,maxZ};
    }

    /** このモデルのバウンディング境界をワールド座標系で取得します <br>
     * 返り値は、minX,minY,minZ,maxX,maxY,maxZの順で値が収められた配列です */
    @Override
    public float[] getBoundByArray(){
        return null;
    }

    /** このモデルに属するコリジョンエレメントを取得します */
    @Override
    public List<CollisionElement> getCollisionElements() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /** このモデルの座標を視点座標系で取得します */
    public float[] getPositionByView(){
        return this.viewPos;
    }

    /** このモデルの視点座標系での座標を設定します<br>
     * 内部で設定されるので外から書き込む必要はありません．<br>
     * 視点座標は，主に描画順序決定のためのZソートに利用されます． */
    private void setPositionByView(float[] vPos){
        this.viewPos=vPos;
    }

    /** このモデルにVRAMフラッシュを通知します */
    @Override
    protected void vramFlushed(){
        // このモデルのアップロード情報を初期化
        for (List<VertexPackage> packs:this.vPacksList){
            for (VertexPackage vp:packs){
                vp.vramFlushed();
            }
        }
    }

    /** モデルの初期化を行います */
    @Override
    public void init(GL3 gl, GraphicEngine eng){
        this.initObject(gl, eng);
        super.init(gl, eng); // 子モデルの初期化
    }

    /** モデルの初期化を行います */
    public void initObject(GL3 gl, GraphicEngine eng){
        for(List<VertexPackage> vpList:this.vPacksList){
            for(VertexPackage vp:vpList){
                vp.init(gl, eng);
                vp.addParent(this);
            }
        }
    }

    /** 切り離される場合の処理 */
    @Override
    protected void detachInternal() {
        super.detachInternal();
        /* エンジンからRemoveしてしまうとVRAM破壊が認識できなくなる
        GraphicEngine engine=this.getGE();
        if (engine!=null){
            engine.removeModel(this);
            this.setGE(null);
        }
        */
    }

    /** モデルの描画処理を行います */
    public void drawObject(GL3 gl){
        float[] mvp;
        float[] viewMatrix;
        float[] rotation;
        float[] mvMatrix;
        GraphicEngine engine=this.getEngine();
        List<LightObject> lights=null;

        if (engine==null){
            mvp=Node3D.UNIT_MAT4;
            viewMatrix=Node3D.UNIT_MAT4;
            mvMatrix=Node3D.UNIT_MAT4;
            rotation=Node3D.UNIT_MAT3;
        }else{
            // モデルビュー行列を取得します
            viewMatrix=this.getEngine().getViewMatrix();
            mvMatrix=VectorManager.multMatrix4(viewMatrix, this.getWorldMatrix());
            this.setPositionByView(new float[]{mvMatrix[12],mvMatrix[13],mvMatrix[14]}); // 視点座標系での座標を設定

            // 回転行列を計算します
            rotation=new float[9];
            rotation[0]=mvMatrix[0];
            rotation[1]=mvMatrix[1];
            rotation[2]=mvMatrix[2];
            rotation[3]=mvMatrix[4];
            rotation[4]=mvMatrix[5];
            rotation[5]=mvMatrix[6];
            rotation[6]=mvMatrix[8];
            rotation[7]=mvMatrix[9];
            rotation[8]=mvMatrix[10];
            rotation=VectorManager.getInverse3(rotation);
            rotation=VectorManager.getTransposed(rotation);

            // 座標変換行列を計算
            mvp=VectorManager.multMatrix4(this.getEngine().getPVMatrix(),this.getWorldMatrix());
            // ライト情報を取得
            lights=this.getEngine().getLightObjects();
        }

        // 現在のLoDレベルを計算して取得します(引数は視点座標系でのZ座標の絶対値)
        int currentLod=this.checkCurrentLod(Math.abs(this.viewPos[2]));
        List<VertexPackage> vPacks=this.getVertexPackages(currentLod);

        for(VertexPackage vp:vPacks){
            vp.setMvpMatrix(mvp);
            vp.setMvMatrix(mvMatrix);
            vp.setRotationMatrix(rotation);
            // マテリアルにライトを使用するかどうかを設定
            BasicMaterial mat=vp.getMaterial();
            mat.setUseLights(this.isUseLight());
            // マテリアルにシステムが持っているライト情報を設定
            mat.refleshLights(lights,viewMatrix);

            // 半透明情報を設定
            if (this.getTransparent()!=BlendType.NOT){
                gl.glEnable(GL3.GL_BLEND);
                if (this.getTransparent()==BlendType.BLEND){
                    gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
                }else if (this.getTransparent()==BlendType.ADDING){
                    gl.glBlendFunc(GL3.GL_SRC_ALPHA,GL3.GL_ONE);
                }
            }
            // 実際の描画
            if (this.isVisible()){
                vp.draw(gl);
            }
            if (this.getTransparent()!=BlendType.NOT){
                gl.glDisable(GL3.GL_BLEND);
            }
        }
    }

    /** モデルの後始末を行います<br>
     * disposeが呼ばれた時点でグラフィックエンジンから完全に削除されます． */
    @Override
    public void dispose(GL3 gl){
        super.dispose(gl);
        for (List<VertexPackage> packList:vPacksList){
            for(VertexPackage vp:packList){
                vp.removeParent(this);
            }
        }
        // 消滅する場合はエンジンから完全に削除
        GraphicEngine engine=this.getEngine();
        if (engine!=null){
            engine.removeModel(this);
        }
    }
}

