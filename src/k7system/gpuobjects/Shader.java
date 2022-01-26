package k7system.gpuobjects;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;

public class Shader extends GPUResource{
    public static final int NOT_COMPILED=-1;
    public static final int MESSAGE_LENGTH=1024;


    private String vsSource[];
    private String fsSource[];
    private int programId=NOT_COMPILED;

    private Logger logger=Logger.getGlobal();

    /** 頂点シェーダーのソースを設定します */
    public void setVertexShaderSource(String[] source){
        this.vsSource=source;
    }

    /** フラグメントシェーダーのソースを設定します */
    public void setFragmentShaderSource(String[] source){
        this.fsSource=source;
    }

    /** このシェーダーの名前を取得します */
    public int getProgramHandle(){
        return programId;
    }

    /** VRAMフラッシュを通知します */
    @Override
    public void vramFlushed(){
        this.disableUploadedFlag();
    }

    /** シェーダーをコンパイル，リンクまで実行し，プログラムIDを返します<br>
     * システムから呼ばれるメソッドであり，ユーザーが呼び出すことはほぼありません．<br>
     * ソース及びユニフォーム変数の設定はこのメソッドが呼ばれる前に完了する必要があります． */
    public int init(GL3 gl,GraphicEngine eng){
        super.init(gl, eng);

        // シェーダーの設定
        int vs = gl.glCreateShader (GL3.GL_VERTEX_SHADER);
        int fs = gl.glCreateShader (GL3.GL_FRAGMENT_SHADER);
        gl.glShaderSource (vs, 1, this.vsSource, null);
        gl.glCompileShader (vs);
        IntBuffer errorNo=IntBuffer.wrap(new int[1]);
        IntBuffer buffSize=IntBuffer.wrap(new int[1]);
        ByteBuffer message=ByteBuffer.wrap(new byte[MESSAGE_LENGTH]);
        int error=gl.glGetError();
        try{
            gl.glGetShaderiv(vs, GL3.GL_COMPILE_STATUS,errorNo);
            if (errorNo.get(0)!=GL.GL_TRUE){
                gl.glGetProgramInfoLog(vs, MESSAGE_LENGTH, buffSize, message);
                char[] mess=new char[buffSize.get(0)];
                for (int i=0;i<buffSize.get(0);i++){
                    mess[i]=(char)message.array()[i];
                }
                logger.severe("Failed to compile vertex shader <"+this+":handle "+this.getProgramHandle()+"> error:"+error+"\n"+String.copyValueOf(mess));
            }

            error=gl.glGetError();
            gl.glShaderSource (fs, 1, this.fsSource, null);
            gl.glCompileShader (fs);
            gl.glGetShaderiv(fs, GL3.GL_COMPILE_STATUS,errorNo);
            if (errorNo.get(0)!=GL.GL_TRUE){
                gl.glGetProgramInfoLog(vs, MESSAGE_LENGTH, buffSize, message);
                char[] mess=new char[buffSize.get(0)];
                for (int i=0;i<buffSize.get(0);i++){
                    mess[i]=(char)message.array()[i];
                }
                logger.severe("Failed to compile flagment shader <"+this+":handle "+this.getProgramHandle()+"> error:"+error+"\n"+String.copyValueOf(mess));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        this.programId = gl.glCreateProgram ();
        gl.glAttachShader (programId, fs);
        gl.glAttachShader (programId, vs);
        gl.glLinkProgram (programId);
        //System.out.println("DEBUG: シェーダーコンパイル完了:"+this.programId);

        error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Fail to link shader <"+this+"> :"+error);
        }
        this.enableUploadedFlag();

        System.out.println("DEBUG: Shader is compiled !");
        return programId;
    }

    /** シェーダーをプログラムから削除する */
    @Override
    public void dispose(GL3 gl){
        System.out.println("DEBUG: shader is disposed !");
        this.disableUploadedFlag();
    }
}
