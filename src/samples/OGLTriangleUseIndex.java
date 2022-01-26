package samples;

import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.gpuobjects.Shader;
import k7system.gpuobjects.VertexArrayObject;

public class OGLTriangleUseIndex extends JFrame implements GLEventListener{

    private Logger logger=Logger.getGlobal();
    private VertexArrayObject vao;
    private Shader shader;

    // 三角形の座標
    private float points[] = {
        0.0f,  0.5f,  0.0f,
        0.5f, -0.5f,  0.0f,
       -0.5f, -0.5f,  0.0f
     };

    // 頂点インデックス
    private int index[]={
            0,1,2
    };

    private String[] vShader=new String[]{
         "#version 330 core\n"+
         "layout(location = 0) in vec3 vp;\n"+
         "uniform mat4 mvpMatrix;"+
         "void main(){\n"+
         "    gl_Position = vec4(vp,1.0);\n"+
         "}\n"
    };

    private String[] fShader=new String[]{
         "#version 330 core\n"+
         "out vec4 color;\n"+
         "void main(){\n"+
         "     color = vec4(1.0, 0.0, 0.0, 1.0);\n"+
         "}\n"
    };

    public static void main(String[] args){
            JFrame frame=new OGLTriangleUseIndex();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTriangleUseIndex() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);

        this.vao=new VertexArrayObject();
        this.vao.setVertices(this.points); // 頂点座標を登録
        this.vao.setIndices(this.index); // 頂点インデックスを登録

        this.shader=new Shader();
        this.shader.setVertexShaderSource(vShader);
        this.shader.setFragmentShaderSource(fShader);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void display(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        gl.glClear( GL3.GL_COLOR_BUFFER_BIT |GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(this.shader.getProgramHandle());
        int error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Fail to bind shader <"+this.shader+":"+this.shader.getProgramHandle()+"> :"+error);
        }

        this.vao.draw(gl);

    }

    @Override
    public void init(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // 背景色は暗い青
        gl.glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        gl.glEnable (GL4.GL_DEPTH_TEST);
        gl.glDepthFunc (GL4.GL_LESS);

        // シェーダーの設定
        this.shader.init(gl, null);
        System.out.println("DEBUG: シェーダーコンパイル完了:"+this.shader.getProgramHandle());

        // VAOの設定
        this.vao.init(gl,null);
    }

    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
            int arg4) {
        System.out.println("DEBUG: Reshape");
    }
    @Override
    public void dispose(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // VAOの後始末
        this.vao.dispose(gl);
    }

}
