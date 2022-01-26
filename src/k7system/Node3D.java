package k7system;

import java.util.ArrayList;

import com.jogamp.opengl.GL3;

/** 描画対象となるノードクラスです。
 *  このクラスを拡張してModel3Dクラスが実装されています */
public class Node3D {
    public static final float[] UNIT_MAT4=VectorManager.createIdentityMatrix(4); //単位行列
    public static  final float[] UNIT_MAT3=VectorManager.createIdentityMatrix(3); //単位行列

    private GraphicEngine gEngine=null;

    private float[] matrix=VectorManager.createIdentityMatrix(4); // このモデルの同次行列です．親座標に対する変換になります．

    private float[] worldMatrix=VectorManager.createIdentityMatrix(4); // このモデルのワールド座標系での同次行列です

    private String name="noName";
    private boolean destroyFlag=false;
    private Node3D parentModel=null;
    private ArrayList<Node3D> childNodes=new ArrayList<Node3D>();

    private ArrayList<Node3D> destroyedModelList=new ArrayList<Node3D>(); // そのサイクルで破棄されたモデルリストです

    private boolean isVisible=true; //基本は可視
    private float scale=1.0f; // 大きさ変更用

    /** 呼び出し元のグラフィックエンジンを取得します */
    public GraphicEngine getEngine() {
        return gEngine;
    }

    /** 呼び出し元のグラフィックエンジンを設定します<br>
     * グラフィックエンジンに登録した場合には自動で呼び出されるためこのメソッドを呼ぶ必要はありません */
    public void setEngine(GraphicEngine ge) {
        this.gEngine = ge;
        for (Node3D node:this.getChildObjects()){
            node.setEngine(ge);
        }
    }

    /** 名前を取得します */
    public String getName() {
        return name;
    }

    /** 名前を設定します */
    public void setName(String name) {
        this.name = name;
    }

    /** 可視属性を取得します */
    public boolean isVisible(){
        return isVisible;
    }

    /** 可視属性を設定します */
    public void setVisible(boolean visibleFlag){
        isVisible=visibleFlag;
    }

    /** 親オブジェクトを設定します */
    public void setParentObject(Node3D parent){
        parentModel=parent;
    }

    /** 子オブジェクトのリストを取得します */
    public ArrayList<Node3D> getChildObjects(){
        return childNodes;
    }

    /** 子オブジェクトを追加します<br>
     * 同じオブジェクトを重複して登録することはできません */
    public void attach(Node3D child){
        if (!this.childNodes.contains(child)){ // 重複チェック
            child.setParentObject(this); // 子供の親を自分に設定
            child.setEngine(this.gEngine);
            childNodes.add(child);
        }
    }

    /** 子オブジェクトを切り離します<br>
     * 切り離された子オブジェクトは消滅するわけではないため，リソースを削除するためには改めてdispose()を呼ぶ必要があります．*/
    public void detach(Node3D child){
        child.detachInternal(); // 分離処理を呼ぶ
        childNodes.remove(child);
    }

    /** 親オブジェクトから離脱します<br>
     * 切り離された子オブジェクトは消滅するわけではないため，リソースを削除するためには改めてdispose()を呼ぶ必要があります．*/
    public void detachMe(){
        if (this.parentModel!=null){
            this.parentModel.detach(this); // 親から自分を切り離す
            this.detachInternal();
        }
    }

    /** 内部的なデタッチ処理です */
    protected void detachInternal(){
        if (this.parentModel!=null){
            this.parentModel=null;
        }
        this.setEngine(null); // GEを初期化
    }

    /**このモデルの大きさを変更します<br>
     * 同次行列自体に反映されるため，このノードの子オブジェクト全てが影響を受けます */
    public void setScale(float zoom){
        this.scale=zoom;
    }

    /** 同次行列を取得します<br>
     * scaleが設定されていた場合，それを反映した同次行列となります */
    public float[] getMatrix(){
        float[] mat=this.matrix.clone();
        mat[0]*=this.scale;
        mat[1]*=this.scale;
        mat[2]*=this.scale;
        mat[4]*=this.scale;
        mat[5]*=this.scale;
        mat[6]*=this.scale;
        mat[8]*=this.scale;
        mat[9]*=this.scale;
        mat[10]*=this.scale;
        return mat;
    }

    /** 同次行列を設定します */
    public void setMatrix(float[] mat){
        this.matrix=mat;
    }

    /** このノードのワールド座標での同次行列を取得します */
    public float[] getWorldMatrix(){
        return this.worldMatrix;
    }

    /** ノードのワールド座標を取得します<br>
     * 3次元ベクトルです．*/
    public float[] getWorldPosition(){
        return new float[]{this.worldMatrix[12],this.worldMatrix[13],this.worldMatrix[14]};
    }

    /** モデルのワールド座標を取得します */
    public float[] getPositionByArray(){
        return new float[]{this.worldMatrix[12],this.worldMatrix[13],this.worldMatrix[14]};
    }

    /** ノードの座標を設定します<br>
     * 親座標に対して相対位置で指定することになります */
    public void setPosition(float x,float y,float z){
        matrix[12]=x;
        matrix[13]=y;
        matrix[14]=z;
    }

    /** モデルを移動させます<br>
     * 座標系は親座標のものが利用されます */
    public void translate(float x,float y,float z){
        matrix[12]+=x;
        matrix[13]+=y;
        matrix[14]+=z;
    }

    /** モデルの回転角を指定します<br>
     *  引数はGL.glRotateと同じです */
    public void rotate(float angle ,float x,float y,float z){
        float sinAngle=(float)Math.sin(angle/180*Math.PI);
        float cosAngle=(float)Math.cos(angle/180*Math.PI);

        matrix=new float[]{
            x*x*(1-cosAngle)+cosAngle,x*y*(1-cosAngle)+z*sinAngle,x*z*(1-cosAngle)-y*sinAngle,0,
            x*y*(1-cosAngle)-z*sinAngle,y*y*(1-cosAngle)+cosAngle,y*z*(1-cosAngle)+x*sinAngle,0,
            x*z*(1-cosAngle)+y*sinAngle,y*z*(1-cosAngle)-x*sinAngle,z*z*(1-cosAngle)+cosAngle,0,
            matrix[12],matrix[13],matrix[14],1
        };
    }

    /** モデルを回転させます<br>
     * 現在の回転角からさらに回転させます．
     * 引数はGL.glRotateと同じです */
    public void multRotate(float angle ,float x,float y,float z){
        float sinAngle=(float)Math.sin(angle/180*Math.PI);
        float cosAngle=(float)Math.cos(angle/180*Math.PI);

        float nowX=matrix[12];
        float nowY=matrix[13];
        float nowZ=matrix[14];
        float[] newMatrix=new float[]{
                x*x*(1-cosAngle)+cosAngle,x*y*(1-cosAngle)+z*sinAngle,x*z*(1-cosAngle)-y*sinAngle,0,
                x*y*(1-cosAngle)-z*sinAngle,y*y*(1-cosAngle)+cosAngle,y*z*(1-cosAngle)+x*sinAngle,0,
                x*z*(1-cosAngle)+y*sinAngle,y*z*(1-cosAngle)-x*sinAngle,z*z*(1-cosAngle)+cosAngle,0,
                0,0,0,1
            };

        matrix=VectorManager.multMatrix4(newMatrix,matrix.clone());
        matrix[12]=nowX;
        matrix[13]=nowY;
        matrix[14]=nowZ;
    }

    /** 破棄フラグを取得します */
    public boolean isDestroyFlag() {
        return destroyFlag;
    }

    /** 破棄フラグを設定します */
    public void setDestroyFlag(boolean flag) {
        destroyFlag=flag;
    }

    /** このノードにVRAMフラッシュを通知します */
    protected void vramFlushed(){
    }

    /** モデル初期化用のメソッドです．<br>
     * グラフィックエンジンから呼び出されます．<br>
     * 本当の初期化の場合以外にも，ウィンドウサイズの変更などでVRAMがクリアされる際には呼び出されます． */
    public void init(GL3 gl, GraphicEngine eng){
        for (Node3D child:this.childNodes){
            child.init(gl, eng);
        }
    }

    /** モデル描画用のメソッドです。<br>
     *  グラフィックエンジンから呼び出されます。 */
    public void draw(GL3 gli){
        GL3 gl=(GL3)gli;

        // 親階層のワールド同次行列を取得
        float[] worldMatrixOfParentNode;
        if (this.parentModel!=null){
            worldMatrixOfParentNode=this.parentModel.getWorldMatrix();
        }else{
            worldMatrixOfParentNode=VectorManager.createIdentityMatrix(4);
        }
        float[] worldMatrix=VectorManager.multMatrix4(worldMatrixOfParentNode,this.getMatrix()); // 自分のワールド同次行列を計算
        this.worldMatrix=worldMatrix; // ワールド同次行列を更新

        for(Node3D child:childNodes){// 子オブジェクトの描画を行います
            if(child.isDestroyFlag()){ // 削除フラグが立っていた場合
                child.dispose(gl);
                destroyedModelList.add(child);
            }else{
                child.draw(gl);
            }
        }

        // 破棄済みモデルをリストから削除します
        for(Node3D node:destroyedModelList){
            childNodes.remove(node);
        }
    }

    /** 現在確保している資源をすべて破棄します<br>
     * 一度このメソッドが呼ばれた場合，配下のすべての資源が破棄される可能性があります．*/
    public void dispose(GL3 gl){
        this.setDestroyFlag(true);
        return;
    }
}
