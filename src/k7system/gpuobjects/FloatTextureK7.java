package k7system.gpuobjects;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;


/** 浮動小数点テクスチャです<br>
 * 実体としてのイメージクラスと関連付けられていません<br>
 * なお，floatと同じサイズの要素なら対応できるため，Integerにも転用できます */
public class FloatTextureK7 extends TextureK7{
    private Logger logger=Logger.getGlobal();
    private int width;
    private int height;
    private int numOfElements;

    /** コンストラクタでテクスチャの大きさと，1テクセルあたりの要素数を指定します<br>
     * たとえば，RGBA32を想定しているならnumOfElementsは4となります */
    public FloatTextureK7(int width, int height, int numOfElements) {
        this.width=width;
        this.height=height;
        this.numOfElements=numOfElements;
        switch (numOfElements){
        case 0:
            break;

        case 1:
            this.setTextureDataType(GL3.GL_RED, GL3.GL_FLOAT);
            break;

        case 2:
            break;

        case 3:
            this.setTextureDataType(GL3.GL_RGB, GL3.GL_FLOAT);
            break;

        case 4:
            this.setTextureDataType(GL3.GL_RGBA, GL3.GL_FLOAT);
            break;
        }
    }

    @Override
    public int getImageHeight() {
        return this.height;
    }

    @Override
    public int getImageWidth() {
        return this.width;
    }

    /** 浮動小数点バッファ作成 */
    @Override
    protected void createBuffer(GL3 gl) {
        int texId=this.getTextureId();

        FloatBuffer buff=FloatBuffer.allocate(this.width*this.height*this.numOfElements);
        // テクスチャを作成してVRAMに登録
        gl.glBindTexture (GL.GL_TEXTURE_2D, texId);
        gl.glTexImage2D (GL.GL_TEXTURE_2D, 0, this.getTextureType(), this.width, this.height, 0, this.getTextureType(), this.getTextureDataUnit(), buff);
    }
}
