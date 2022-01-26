package k7system;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.jogamp.opengl.GL3;

import k7system.gpuobjects.BillboardMaterial;
import k7system.gpuobjects.TextureK7;
import k7system.gpuobjects.VertexArrayObject;

/** ビルボードクラスです<br>
 * ビルボードは常にカメラに正対する1枚ポリゴンのオブジェクトです<br>
 * ビルボードには専用マテリアルが適用されるうえ，常に長方形ポリゴンとして扱われるため，マテリアルや形状等を登録する必要はありません．<br>
 * ビルボードの大きさはピクセルで指定されます．これは，ビルボードが明らかに他のポリゴンオブジェクトとは異質であることを示します．
 * 座標はワールド座標の場合もスクリーン座標の場合も3次元空間で保持するため，描画の際は前後関係が考慮されます．*/
public class Billboard extends Object3D{
    public static final int WORLD_COORDINATES=0;
    public static final int SCREEN_COORDINATES=1;
    // 座標の基準点
    public static final int ORIGIN_TYPE_LEFT_BOTTOM=0; // デフォルト
    public static final int ORIGIN_TYPE_CENTER=1;
    public static final int ORIGIN_TYPE_LEFT_TOP=2;
    private int originType=ORIGIN_TYPE_LEFT_BOTTOM;

    private int width=Params.UNDEFFINED;
    private int height=Params.UNDEFFINED;
    private float scale=1.0f; // 拡大率
    private float rotation=0;
    private BillboardMaterial mat=new BillboardMaterial();
    private int system=WORLD_COORDINATES; // 座標系がワールド座標かスクリーン座標かを指定

    private boolean mirrorY=false; // Y軸反転を実施するか

    private VertexArrayObject vao;

    private static final float DEFAULT_VTXS[]=new float[]{
            0,0,0, // 左下
            1,0,0, // 右下
            1,1,0, // 右上
            0,1,0, // 左上
    };

    private float verteces[]=DEFAULT_VTXS.clone();

    /** テクスチャを指定してビルボードを生成 */
    public Billboard(TextureK7 texture) {
        this.width=texture.getImageWidth();
        this.height=texture.getImageHeight();
        this.mat.setTexture(texture);
        this.createVao();
    }

    /** 大きさを指定してビルボードを生成 */
    public Billboard(int width, int height) {
        this.width=width;
        this.height=height;

        // 真っ白で塗りつぶす
        BufferedImage image=new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g=image.getGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0,0, width, height);
        this.mat.setTexture(new TextureK7(image));
        this.mat.setName("Billboard Material");
        this.createVao();
    }

    /** イメージオブジェクトを指定してビルボードを生成 */
    public Billboard(BufferedImage image) {
        this.width=image.getWidth();
        this.height=image.getHeight();
        TextureK7 texture=new TextureK7(image);
        this.mat.setTexture(texture);
        this.createVao();
    }

    /** ビルボードの上下反転を行います<br>
     * FBO利用時など，テクスチャの向きが逆転しているため，これを利用することで反転できます */
    public void setMirrorY(boolean flag){
        this.mirrorY=flag;
    }

    /** このビルボードの座標系を取得します */
    public int getCoordinateSystem(){
        return this.system;
    }

    /** このビルボードの拡大率を取得します */
    public float getScale(){
        return this.scale;
    }

    /** このビルボードの拡大率を指定します */
    public void setScale(float scale){
        this.scale=scale;
    }

    /** 座標系を指定します<br>
     * 座標をワールド座標として保持するか，スクリーン座標として保持するかです<br>
     * Billboard.WORLDまたはBillboard.SCREENを指定してください．
     * ワールド座標だった場合，座標は浮動小数点でワールド座標を示します．
     * 一方，スクリーン座標だった場合，浮動小数点でスクリーン座標を示します．*/
    public void setCoordinateSystem(int system){
        this.system=system;
        this.mat.setCoordinatesSystem(system);
    }

    /** VAOを生成します */
    private void createVao(){
        if (this.vao!=null){
            this.vao.removeParent(this);
        }
        this.vao=new VertexArrayObject();
        this.vao.setVertices(this.verteces);
        this.vao.setIndices(new int[]{0,1,2, 2,3,0});
        this.vao.setTexCoords(new float[]{0,1, 1,1, 1,0, 0,0});
        this.vao.createNormals();
    }

    /** マテリアルを取得します */
    public BillboardMaterial getMaterial(){
        return this.mat;
    }

    /** テクスチャイメージを取得します */
    public BufferedImage getImage(){
        return this.mat.getTexture().getImage();
    }

    /** テクスチャイメージに変更があったことを通知します */
    public void refreshImage(){
        this.mat.getTexture().refreshImage();
    }

    /** 座標の基準点の決め方を取得します */
    public int getOrigineType(){
        return originType;
    }

    /** 座標の基準点の決め方を設定します<br>
     * 指定できるのは，左下，左上，中心です．
     * 無効な指定をした場合，左下が指定されます */
    public void  setOrigineType(int type){
        switch (type) {
        case ORIGIN_TYPE_LEFT_BOTTOM:
            this.verteces=DEFAULT_VTXS;
            break;

        case ORIGIN_TYPE_CENTER:
            this.verteces=DEFAULT_VTXS.clone();
            for (int i=0;i<this.verteces.length;i++){
                if (i % 3 !=2){ // x及びyの値を操作
                    this.verteces[i]=this.verteces[i]-0.5f;
                }
            }
            break;

        case ORIGIN_TYPE_LEFT_TOP:
            this.verteces=DEFAULT_VTXS.clone();
            for (int i=0;i<this.verteces.length;i++){
                if (i % 3 ==1){ // yの値を操作
                    this.verteces[i]=this.verteces[i]-1.0f;
                }
            }
            break;

        default:
            this.verteces=DEFAULT_VTXS;
            break;
        }
        this.createVao();
    }

    /** ビルボードの幅を取得します */
    public int getWitdh(){
        return this.width;
    }

    /** ビルボードの高さを取得します */
    public int getHeight() {
        return this.height;
    }

    /** 回転角を指定します */
    public float getRotation() {
        return this.rotation;
    }

    /** 回転角を取得します */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /** このビルボードが画面内に存在するかどうかです */
    public boolean isInSight(){
        return true;
    }

    /** ビルボードモデルの描画処理を行います */
    @Override
    public void drawObject(GL3 gl) {
        float[] mvp;
        //float[] viewMatrix;
        //float[] mvMatrix;
        GraphicEngine engine=this.getEngine();

        if (engine==null){
            mvp=Node3D.UNIT_MAT4;
            //viewMatrix=Node3D.UNIT_MAT4;
            //mvMatrix=Node3D.UNIT_MAT4;
        }else{
            // モデルビュー行列を取得します
            //viewMatrix=this.getEngine().getViewMatrix();
            //mvMatrix=VectorManager.multMatrix4(viewMatrix, this.getWorldMatrix());

            // 座標変換行列を計算
            mvp=VectorManager.multMatrix4(this.getEngine().getPVMatrix(),this.getWorldMatrix());
        }

        // 拡大率を仕込む
        this.mat.setScale(this.scale);

        // 座標変換用行列を設定
        this.mat.setMvpMatrix(mvp);

        // スクリーン座標を設定
        this.mat.setScreenCoord(this.getPositionByArray());

        // Y軸反転フラグを設定
        this.mat.setMirrorY(this.mirrorY);

        // 半透明情報を設定
        if (this.getTransparent()!=BlendType.NOT){
            gl.glEnable(GL3.GL_BLEND);
            if (this.getTransparent()==BlendType.BLEND){
                gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
            }else if (this.getTransparent()==BlendType.ADDING){
                gl.glBlendFunc(GL3.GL_SRC_ALPHA,GL3.GL_ONE);
            }
        }

        GraphicEngine gEngine=this.getEngine();

        // 実際の描画
        if (this.isVisible()){
            // 視界内にあるかチェック
            if (this.isInSight()){
                // マテリアルの初期化を確認し，マテリアルをバインド
                if (!this.mat.isUploaded()){
                    this.mat.init(gl, gEngine);
                }
                this.mat.bind(gl);

                // VAOの初期化を確認し，VAOを描画
                if (!this.vao.isUploaded()){
                    this.vao.init(gl, gEngine);
                }
                this.vao.draw(gl);
                // マテリアルをアンバインド
                this.mat.unbind(gl);
            }
        }
        if (this.getTransparent()!=BlendType.NOT){
            gl.glDisable(GL3.GL_BLEND);
        }
    }

    @Override
    public void dispose(GL3 gl) {
        super.dispose(gl);
        // マテリアルがNullの場合は想定しない
        this.mat.removeParent(this);
    }
}
