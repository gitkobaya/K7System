package k7system.gpuobjects;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;

/** フレームバッファオブジェクトを扱うためのクラスです<br>
 * このクラスを利用することで，レンダリング対象を画面ではなくテクスチャにすることができます */
public class FrameBufferObject extends GPUResource{
    private TextureK7 texture=null; // このFBOと関連付けるテクスチャ
    private int rboId=-1; // このFBOで利用するrboのIDです
    private int fboId=-1; // このFBOのIDです

    /** コンストラクタでフレームバッファの大きさを指定します */
    public FrameBufferObject(int width, int height) {
        this.texture=new TextureK7(new BufferedImage(width, height,BufferedImage.TYPE_4BYTE_ABGR));
        this.texture.setSamplerConfig(GL3.GL_CLAMP_TO_EDGE, GL3.GL_CLAMP_TO_EDGE, GL3.GL_NEAREST, GL3.GL_NEAREST);
    }

    /** コンストラクタでテクスチャを設定します */
    public FrameBufferObject(TextureK7 texture){
        this.texture=texture;
    }

    /** このFBOに関連付けられているテクスチャを取得します */
    public TextureK7 getTexture(){
        return this.texture;
    }

    /** このFBOにテクスチャを関連付けます<br>
     * 初期化前に実行してください */
    public void setTexture(TextureK7 texture){
        this.texture=texture;
    }

    /**このメソッドを呼ぶことで描画対象がこのFBOになります*/
    public void bind(GL3 gl){
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, this.fboId);
        gl.glViewport(0, 0, this.texture.getImageWidth(), this.texture.getImageHeight()); // 描画領域をテクスチャサイズに合わせます
    }

    /**このメソッドを呼ぶことで描画対象がこのFBOから解除されます<br>
     * bindした時点でビューポートがFBOのサイズになっているため，unbind後にビューポートを戻すのを忘れないようにしてください*/
    public void unbind(GL3 gl){
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
    }

    /** FBOを初期化します．返り値はFBOのIDです */
    @Override
    public int init(GL3 gl, GraphicEngine engine) {
        super.init(gl, engine);
        // テクスチャオブジェクトが存在しないケースは明らかにエラーなので想定しない
        this.texture.init(gl, engine);

        // RBOを登録(深度バッファとして利用)
        IntBuffer id=IntBuffer.wrap(new int[1]);
        // RBO作成
        if (this.rboId==-1){
            gl.glGenRenderbuffers(1, id);
            this.rboId=id.get(0);
            gl.glBindRenderbuffer(GL3.GL_RENDERBUFFER, this.rboId);
            gl.glRenderbufferStorage(GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, this.texture.getImageWidth(), this.texture.getImageHeight());
        }
        gl.glBindRenderbuffer(GL3.GL_RENDERBUFFER, 0); // デフォルトに戻す

        // FBOを登録
        if (this.fboId==-1){
            id=IntBuffer.wrap(new int[1]);
            gl.glGenFramebuffers(1, id);
            this.fboId=id.get(0);
            gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, this.fboId);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER,  GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, this.texture.getTextureId(), 0);
            gl.glFramebufferRenderbuffer(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER, this.rboId);
        }
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0); // デフォルトに戻す
        this.enableUploadedFlag();
        return this.fboId;
    }

    @Override
    public void vramFlushed() {
        this.disableUploadedFlag();
    }

    @Override
    public void dispose(GL3 gl) {
        // FBO解放
        IntBuffer fboBuff=IntBuffer.wrap(new int[]{this.fboId});
        gl.glDeleteFramebuffers(1, fboBuff);
        this.fboId=-1;

        // RBO解放
        IntBuffer rboBuff=IntBuffer.wrap(new int[]{this.rboId});
        gl.glDeleteRenderbuffers(1, rboBuff);
        this.rboId=-1;

        // テクスチャから自分を外す
        this.texture.removeParent(this);
    }
}
