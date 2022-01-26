package k7system.gpuobjects;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;




import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;

/** K7Systemで利用されるテクスチャクラスです<br>
 * 利用できるイメージはTYPE_4BYTE_ABGRまたはTYPE_3BYTE_BGRです<br>
 * Texture class of K7System<br>
 * This features of the class is different from genuine Texture class of JOGL */
public class TextureK7 extends GPUResource{
    private Logger logger=Logger.getGlobal();

    public static final int NOT_REGISTERD=-1;
    private BufferedImage image=null; // このテクスチャで利用されるイメージです
    private int texId=NOT_REGISTERD;
    private int samperId=NOT_REGISTERD;
    private boolean changeTexFlag=false;

    // サンプラ―関係の設定
    private int wrapS=GL3.GL_REPEAT;
    private int wrapT=GL3.GL_REPEAT;
    private int magFilter=GL3.GL_LINEAR;
    private int minFilter=GL3.GL_LINEAR;

    // テクスチャデータの設定
    private int texType=GL3.GL_RGBA;
    private int texDataUnit=GL3.GL_UNSIGNED_BYTE;

    /** 引数無しのコンストラクタ */
    public TextureK7() {
        this.image=null;
    }

    /** 画像を設定したコンストラクタ */
    public TextureK7(BufferedImage image){
        this.image=image;
    }

    /** このテクスチャオブジェクトに登録されている画像を取得します<br>
     * 登録した時点の画像なので，GPU側で編集されていた場合にはそれは反映されません． */
    public BufferedImage getImage(){
        return this.image;
    }

    /** このテクスチャオブジェクトにARGBイメージデータを設定します<br>
     * initよりも先に設定しなければなりません<br>
     * また，このメソッドが呼ばれた時点でアップロードフラグが破棄されます．<br>
     * This method sets image data to this object<br>
     * Call this method before init(). Uploaded flag becomes to be false when you call this method. */
    public void setImage(BufferedImage image){
        this.image=image;
        this.changeTexFlag=true;
        this.disableUploadedFlag();
    }

    /** このテクスチャオブジェクトの画像が変更されたことを通知します */
    public void refreshImage(){
        this.changeTexFlag=true;
        this.disableUploadedFlag();
    }

    /** このテクスチャオブジェクトに登録されている画像の幅を取得します<br>
     * ただし，それがVRAM上でのテクスチャの幅であることは保証されません */
    public int getImageWidth(){
        return this.image.getWidth();
    }

    /** このテクスチャオブジェクトに登録されている画像の高さを取得します<br>
     * ただし，それがVRAM上でのテクスチャの高さであることは保証されません */
    public int getImageHeight(){
        return this.image.getHeight();
    }

    /** サンプラ―の動作を設定します<br>
     * 標準では，それぞれGL_REPEAT,  GL_REPEAT, GL_LINEAR, GL_LINEARとなっています<br>
     * initされるタイミングより前に設定しなければ有効になりません */
    public void setSamplerConfig(int wrapS, int wrapT, int magFilter, int minFilter){
        this.wrapS=wrapS;
        this.wrapT=wrapT;
        this.magFilter=magFilter;
        this.minFilter=minFilter;
    }

    /** テクスチャデータの種別を取得します<br>
     * デフォルトではGL_RGBAです */
    public int getTextureType(){
        return this.texType;
    }

    /** テクスチャデータの単位を取得します<br>
     * デフォルトではUNSIGNED_BYTEです */
    public int getTextureDataUnit(){
        return this.texDataUnit;
    }

    /** テクスチャのデータ種別を設定します<br>
     * 標準では，GL_RBGA，GL_UNSIGNED_BYTEが指定されています<br>
     * initされるタイミングより前に設定しなければ有効になりません */
    public void setTextureDataType(int textureType, int dataUnit){
        this.texType=textureType;
        this.texDataUnit=dataUnit;
    }

    /** テクスチャにアルファチャネルを設定します<br>
     * アルファテクスチャはモノクロイメージで設定します．<br>
     * このメソッドはテクスチャイメージを設定してから呼び出してください．*/
    public void setAlphaChannel(BufferedImage alpha){
        if (this.image!=null){
            System.out.println("debug:アルファチャネルの処理です");
            BufferedImage newImage;
            int width=image.getWidth();
            int height=image.getHeight();

            int widthAlpha=alpha.getWidth();
            int heightAlpha=alpha.getHeight();

            newImage=new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
            for(int i=0;i<height;i++){
                for(int j=0;j<width;j++){
                    int alphaValue=alpha.getRGB((int)(widthAlpha/width*j), (int)(heightAlpha/height*i));
                    alphaValue=(alphaValue & 0x000000ff)<<24; // 青で代表させる

                    int pixel=(image.getRGB(j, i) & 0x00ffffff); // アルファ値を削除する
                    pixel=(pixel | alphaValue); // アルファ値を反映させる
                    newImage.setRGB(j, i, pixel);
                }
            }
            this.setImage(newImage);
        }
    }

    /** テクスチャが変更されていたかどうかを取得します */
    public boolean isTextureChanged(){
        return this.changeTexFlag;
    }

    /** テクスチャが変更されたかどうかを設定します */
    public void setTextureChangeFlag(boolean flag){
        this.changeTexFlag=flag;
    }

    /** テクスチャの初期化を行います<br>
     * 返り値はテクスチャIDです */
    public int init(GL3 gl, GraphicEngine eng){
        super.init(gl, eng);
        if (this.changeTexFlag){    // テクスチャが変更された場合
            // 登録したテクスチャをVRAMから剥がし，改めて登録する
            IntBuffer id=IntBuffer.wrap(new int[]{this.texId});
            gl.glDeleteTextures(1, id);
            this.registerTexture(gl);
            this.changeTexFlag=false;
        }
        if (!this.isUploaded()){    // まだVRAMに登録していないなら登録
            this.registerTexture(gl);
        }
        return this.texId;
    }

    /** テクスチャをVRAMに登録します*/
    protected void registerTexture(GL3 gl){
        // テクスチャ番号を取得
        IntBuffer id=IntBuffer.wrap(new int[1]);
        gl.glGenTextures(1, id);
        this.texId=id.get(0);

        this.createBuffer(gl);

        int error=gl.glGetError();
        if (error!=GL3.GL_NO_ERROR){
            logger.severe("Failed to register texture:"+error);
        }

        // サンプラーの設定
        IntBuffer spId=IntBuffer.wrap(new int[1]);
        gl.glGenSamplers(1, spId);
        this.samperId=spId.get(0);
        gl.glSamplerParameteri (this.samperId, GL3.GL_TEXTURE_WRAP_S, this.wrapS);
        gl.glSamplerParameteri (this.samperId, GL3.GL_TEXTURE_WRAP_T, this.wrapT);
        gl.glSamplerParameteri (this.samperId, GL3.GL_TEXTURE_MIN_FILTER, this.minFilter);
        gl.glSamplerParameteri (this.samperId, GL3.GL_TEXTURE_MAG_FILTER, this.magFilter);

        error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Failed to set up texture sampler :"+error);
        }

        this.enableUploadedFlag(); // アップロード済みにする
    }

    protected void createBuffer(GL3 gl){
        // テクスチャイメージが用意されているならバッファを作成
        if (this.image!=null){
            int[] abgr=this.image.getRGB(0, 0, this.image.getWidth(), this.image.getHeight(), null, 0, this.image.getWidth());
            int type=this.image.getType();
            if (type==BufferedImage.TYPE_4BYTE_ABGR){
                for (int i=0;i<abgr.length;i++){
                    int col=abgr[i];
                    int blue=(col) & 0xff;
                    int green=(col >>8) & 0xff;
                    int red=(col>>16) & 0xff;
                    int alpha=(col>>24) & 0xff;
                    abgr[i]=((alpha<<24) & 0xff000000) | ((blue<<16) & 0x00ff0000) | ((green<<8) & 0x0000ff00) | ((red) & 0x000000ff) & 0xffffff;
                }
            }else if (type==BufferedImage.TYPE_3BYTE_BGR){
                for (int i=0;i<abgr.length;i++){
                    int col=abgr[i];
                    int blue=(col) & 0xff;
                    int green=(col >>8) & 0xff;
                    int red=(col>>16) & 0xff;
                    abgr[i]=((blue<<16) & 0x00ff0000) | ((green<<8) & 0x0000ff00) | ((red) & 0x000000ff) & 0xffffff;
                }
            }

            IntBuffer buff=IntBuffer.wrap(abgr);
            // テクスチャを作成してVRAMに登録
            gl.glBindTexture (GL.GL_TEXTURE_2D, this.texId);
            gl.glTexImage2D (GL.GL_TEXTURE_2D, 0, this.texType, this.image.getWidth(), this.image.getHeight(), 0, this.texType, this.texDataUnit, buff);
        }
    }

    /** テクスチャとサンプラーをVRAMから削除します */
    private void deleteTexture(GL3 gl){
        IntBuffer texIdBuff=IntBuffer.wrap(new int[]{texId});
        gl.glDeleteTextures(1,texIdBuff);
        this.texId=NOT_REGISTERD;

        IntBuffer sampIdBuff=IntBuffer.wrap(new int[]{samperId});
        gl.glDeleteSamplers(1, sampIdBuff);
        this.samperId=NOT_REGISTERD;
    }

    /** テクスチャの名前を取得します */
    public int getTextureId(){
        return this.texId;
    }

    /** サンプラーの名前を取得します */
    public int getSamplerId(){
        return this.samperId;
    }

    /** これから利用するシェーダーに対してこのテクスチャをバインドします */
    protected void bind(GL3 gl, int texUnitName, int texUnit){
        if (this.changeTexFlag || !this.isUploaded()){ // もしテクスチャに変更があれば初期化を行う
            this.init(gl, this.getEngine());
        }
        gl.glActiveTexture(texUnit);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, this.texId);
        gl.glBindSampler(texUnitName, this.samperId);
    }

    @Override
    public void vramFlushed() {
        this.disableUploadedFlag();
    }

    @Override
    public void dispose(GL3 gl) {
        this.deleteTexture(gl);
    }
}
