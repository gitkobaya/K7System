package samples;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

public class OGLTriangle extends JFrame implements GLEventListener{

    private int vertexbuffer;
    private int vertexArrayID;
    private int programId;

    // 三角形の座標
    private float points[] = {
        0.0f,  0.5f,  0.0f,
        0.5f, -0.5f,  0.0f,
       -0.5f, -0.5f,  0.0f
     };

    private String[] vShader=new String[]{
         "#version 330 core\n"+
         "layout(location = 0) in vec3 vp;\n"+
         "void main(){\n"+
         "    gl_Position = vec4(vp,1.0);\n"+
         "}\n"
    };

    private String[] fShader=new String[]{
         "#version 330 core\n"+
         "out vec4 color;\n"+
         "void main(){\n"+
         "        color = vec4(1.0, 0.0, 0.0, 1.0);\n"+
         "}\n"
    };

    public static void main(String[] args){
            JFrame frame=new OGLTriangle();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTriangle() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void display(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        gl.glClear( GL3.GL_COLOR_BUFFER_BIT |GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(this.programId);

        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexbuffer);
        gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, 0, 0);

        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

        gl.glDisableVertexAttribArray(0);

    }

    @Override
    public void init(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // 背景色は暗い青
        gl.glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        gl.glEnable (GL4.GL_DEPTH_TEST);
        gl.glDepthFunc (GL4.GL_LESS);

        IntBuffer vArrayId=IntBuffer.wrap(new int[1]);
        gl.glGenVertexArrays(1,vArrayId);
        this.vertexArrayID=vArrayId.get(0);
        gl.glBindVertexArray(vertexArrayID);

        // VBOの設定
        IntBuffer vBuff=IntBuffer.wrap(new int[1]);
        gl.glGenBuffers(1, vBuff);
        this.vertexbuffer=vBuff.get(0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexbuffer);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, Float.SIZE*9, FloatBuffer.wrap(points), GL3.GL_STATIC_DRAW);
        System.out.println("DEBUG: VBO設定完了 :"+vBuff.get(0));

        // シェーダーの設定
        int vs = gl.glCreateShader (GL3.GL_VERTEX_SHADER);
        gl.glShaderSource (vs, 1, this.vShader, null);
        gl.glCompileShader (vs);
        int fs = gl.glCreateShader (GL3.GL_FRAGMENT_SHADER);
        gl.glShaderSource (fs, 1, this.fShader, null);
        gl.glCompileShader (fs);

        this.programId = gl.glCreateProgram ();
        gl.glAttachShader (programId, fs);
        gl.glAttachShader (programId, vs);
        gl.glLinkProgram (programId);
        System.out.println("DEBUG: シェーダーコンパイル完了:"+this.programId);

    }

    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
            int arg4) {
        System.out.println("DEBUG: Reshape");
    }
    @Override
    public void dispose(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // VBOの後始末
        gl.glDeleteBuffers(1, IntBuffer.wrap(new int[]{this.vertexbuffer}));
        gl.glDeleteVertexArrays(1, IntBuffer.wrap(new int[]{this.vertexArrayID}));
    }

}
