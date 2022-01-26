package k7system.gpuobjects;

import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;
import k7system.VectorManager;

/** エンジンの描画単位です<br>
 * 頂点情報とマテリアルと座標がパッケージになっています．<br>
 * 座標変換を実施して画面表示までを行います．<br>
 * 全ての親から切り離された時点で資源を解放しようとします．
 * そのため，一時的に利用されなくなっても再利用を想定している場合には，ダミーの親を設定しておいてください．<br>
 * 一つでも親が設定されていれば，GPUに載り続けます． */
public class VertexPackage extends GPUResource{
    private VertexArrayObject vao;
    private BasicMaterial material;

    /** この頂点パッケージの境界ボックスです．<br>
     * 値はモデル座標系であり、[minX,minY,minZ],[maxX,maxY,maxZ]の順となります
     * コンストラクタ及び頂点パッケージに初期化をかけるタイミングで生成されます． **/
    private float[][] boundingBox=null;
    private float[] center=null;// 重心

    /** 頂点パッケージのコンストラクタです<br>
     * vaoとマテリアルはコンストラクタで指定し，そのオブジェクトを使い続けます．途中で入れ替えることはできません<br>
     * ただし，たとえばマテリアルをgetして中の値を書き換えることはできます． */
    public VertexPackage(VertexArrayObject vao, BasicMaterial material) {
        this.vao=vao;
        this.vao.addParent(this);
        this.material=material;
        this.material.addParent(this);
        this.boundingBox=this.createBoundingBox(this.vao.getVertices());
    }

    /** この頂点パッケージのマテリアルオブジェクトを取得します */
    public BasicMaterial getMaterial(){
        return this.material;
    }

    /** この頂点パッケージのVAOオブジェクトを取得します */
    public VertexArrayObject getVao(){
        return this.vao;
    }

    /** この頂点パッケージのビュー行列を取得します*/
    private float[] getViewMatrix(){
        return material.getMvMatrix();
    }

    /** この頂点パッケージのモデルビュー行列を設定します<br>
     * モデル座標にこの行列を適用した後には視点座標になることが期待されます */
    public void setMvMatrix(float[] mat){
        this.material.setMvMatrix(mat);
    }

    /** この頂点パッケージのMVP行列を取得します<br>
     * 行列は，透視変換，座標変換を実施した後のものです */
    private float[] getMvpMatrix(){
        return material.getMvpMatrix();
    }

    /** この頂点パッケージのMVP行列を設定します<br>
     * 行列は，透視変換，座標変換を実施した後のものであり，モデル座標にこの行列を適用した後にはスクリーン座標になることが期待されるものです */
    public void setMvpMatrix(float[] mat){
        this.material.setMvpMatrix(mat);
    }

    /** この頂点パッケージの回転行列を取得します<br>
     * モデル座標系のベクトルにこの行列を適用したのち，視点座標系のベクトルになることが期待されます */
    private float[] getRotationMatrix(){
        return this.material.getRotationMatrix();
    }

    /** この頂点パッケージの回転行列を設定します<br>
     * モデル座標系のベクトルにこの行列を適用したのち，視点座標系のベクトルになることが期待されます */
    public void setRotationMatrix(float[] matrix){
        this.material.setRotationMatrix(matrix);
    }

    /** この頂点パッケージのバウンディングボックスを返します */
    public float[][] getBoundingBox(){
        return this.boundingBox;
    }

    /** この頂点パッケージのバウンディングボックスを1次元配列で返します<br>
     *  */
    public float[] getBoundingBoxFlat(){
        float[] flat=new float[6];
        flat[0]=this.boundingBox[0][0];
        flat[1]=this.boundingBox[0][1];
        flat[2]=this.boundingBox[0][2];
        flat[3]=this.boundingBox[1][0];
        flat[4]=this.boundingBox[1][1];
        flat[5]=this.boundingBox[1][2];
        return flat;
    }

    /** この頂点パッケージのバウンディングボックスを生成して返します<br>
     * ここで重心の計算も実施します */
    private float[][] createBoundingBox(float[] vertices){
        float[][] bBox=new float[2][];

        float minX=Float.POSITIVE_INFINITY;
        float maxX=Float.NEGATIVE_INFINITY;
        float minY=Float.POSITIVE_INFINITY;
        float maxY=Float.NEGATIVE_INFINITY;
        float minZ=Float.POSITIVE_INFINITY;
        float maxZ=Float.NEGATIVE_INFINITY;

        this.center=new float[3];
        int size=vertices.length/3;
        for (int i=0;i<size;i++){
            float x=vertices[i*3];
            float y=vertices[i*3+1];
            float z=vertices[i*3+2];

            this.center[0]+=x;
            this.center[1]+=y;
            this.center[2]+=z;

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

        bBox[0]=new float[]{minX,minY,minZ};
        bBox[1]=new float[]{maxX,maxY,maxZ};

        this.center[0]/=size;
        this.center[1]/=size;
        this.center[2]/=size;

        return bBox;
    }

    /** この頂点パッケージが視界に含まれるかをチェックします<br>
     * バウンディングボックスの頂点が一つでも視界に入っていれば描画します． */
    public boolean isInSight(){
        boolean result=true;
        float[] mvpMatrix=this.getMvpMatrix();
        if (this.boundingBox!=null){
            //スクリーン座標系で再度バウンディングボックスを生成
            float xMax=Float.NEGATIVE_INFINITY;
            float yMax=Float.NEGATIVE_INFINITY;
            float zMax=Float.NEGATIVE_INFINITY;
            float xMin=Float.POSITIVE_INFINITY;
            float yMin=Float.POSITIVE_INFINITY;
            float zMin=Float.POSITIVE_INFINITY;
            for (int i=0;i<8;i++){
                float[] vertex={this.boundingBox[(i & 0x4)>>2][0],this.boundingBox[(i & 0x2)>>1][1],this.boundingBox[i & 0x1][2], 1};
                vertex=VectorManager.multMatrixVector(mvpMatrix, vertex); // スクリーン座標系にする
                float screenX=vertex[0]/vertex[3];
                float screenY=vertex[1]/vertex[3];
                float screenZ=vertex[2]/vertex[3];

                if (xMax<screenX){
                    xMax=screenX;
                }
                if (screenX<xMin){
                    xMin=screenX;
                }
                if (yMax<screenY){
                    yMax=screenY;
                }
                if (screenY<yMin){
                    yMin=screenY;
                }
                if (zMax<screenZ){
                    zMax=screenZ;
                }
                if (screenZ<zMin){
                    zMin=screenZ;
                }
            }

            // カメラがパッケージのバウンディングボックスの外側にあった場合
            if (1<xMin || xMax<-1 || 1<yMin || yMax<-1 || 1<zMin || zMax<-1 ){
                result=false;
            }else{
                //System.out.println("DEBUG: Draw!  minX:"+xMin+" maxX:"+xMax+" minY:"+yMin+" maxY:"+yMax);
            }
        }

        return result;
    }

    /** この頂点パッケージがVRAMに載っているかどうかを取得します<br>
     * この頂点パッケージを構成するVAOとマテリアルが両方ともVRAMに載っている場合，trueを返します． */
    @Override
    public boolean isUploaded(){
        return (this.material.isUploaded() & this.vao.isUploaded());
    }

    /** VRAMの消去が発生したため，その情報をマテリアルとVAOに通知します */
    @Override
    public void vramFlushed() {
        if (this.material.isUploaded()){
            this.material.vramFlushed();
        }
        if (this.vao.isUploaded()){
            this.vao.vramFlushed();
        }
    }

    /** この頂点パッケージを初期化します<br>
     * 基本的にエンジンが利用するメソッドであり，ユーザーが呼び出すことはありません． */
    @Override
    public int init(GL3 gl,GraphicEngine eng){
        super.init( gl, eng);
        this.material.init(gl, eng);
        this.vao.init(gl, eng);
        this.boundingBox=this.getBoundingBox();
        System.out.println("DEBUG: VertexPackage has been initialized !");
        return 0;
    }

    /** この頂点パッケージを描画します */
    public void draw(GL3 gl){

        // 視界内にあるかチェック
        if (this.isInSight()){
            // マテリアルの初期化を確認し，マテリアルをバインド
            if (!this.material.isUploaded()){
                this.material.init(gl, this.getEngine());
            }
            this.material.bind(gl);
            // VAOの初期化を確認し，VAOを描画
            if (!this.vao.isUploaded()){
                this.vao.init(gl, this.getEngine());
            }
            this.vao.draw(gl);

            // マテリアルをアンバインド
            this.material.unbind(gl);
        }
    }

    /** この頂点パッケージを削除します */
    @Override
    public void dispose(GL3 gl){
        this.vao.removeParent(this);
        this.material.removeParent(this);
        System.out.println("DEBUG: VertexPackage is disposed!");
    }

}
