package samples;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;

public class OGL3Test extends JFrame implements GLEventListener{

    // 三角形の座標
    private float points[] = {
        0.0f,  0.5f,  0.0f,
        0.5f, -0.5f,  0.0f,
       -0.5f, -0.5f,  0.0f
     };

    // シェーダー
    private String[] vertex_shader ={
            "#version 140\n"+
            "in vec3 vp;\n"+
            "void main () {\n"+
            "  gl_Position = vec4 (vp, 1.0);\n"+
            "}"};

    private String[] fragment_shader ={
            "#version 140\n"+
            "out vec4 frag_colour;\n"+
            "void main () {\n"+
            "  frag_colour = vec4 (0.5, 0.0, 0.5, 1.0);\n"+
            "}"};

    private int shader_programme;

    private int vao;

    /** 起動時に使うだけのメインメソッド */
    public static void main(String args[]){
        JFrame frame=new OGL3Test();
        frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGL3Test() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void display(GLAutoDrawable gla) {
        GL3 gl=(GL3)gla.getGL();

        // wipe the drawing surface clear
        gl.glClear (GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        gl.glUseProgram (this.shader_programme);
        gl.glBindVertexArray (this.vao);
        // draw points 0-3 from the currently bound VAO with current in-use shader
        gl.glDrawArrays (GL3.GL_TRIANGLES, 0, 3);

        gl.glFinish();

        System.out.println("DEBUG: Draw!");
    }

    @Override
    public void init(GLAutoDrawable gla) {
        GL3 gl=(GL3)gla.getGL();

        gl.glEnable (GL3.GL_DEPTH_TEST); // enable depth-testing
        gl.glDepthFunc (GL3.GL_LESS); // depth-testing interprets a smaller value as "closer"

        // VBOの設定
        IntBuffer vbo=IntBuffer.wrap(new int[1]);
        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer (GL3.GL_ARRAY_BUFFER, vbo.get(0));
        FloatBuffer pointsBuffer=FloatBuffer.wrap(points);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, Float.SIZE*9, pointsBuffer, GL3.GL_STATIC_DRAW);
        System.out.println("DEBUG: VBO設定完了 :"+vbo.get(0));

        // VAOの設定
        IntBuffer vao=IntBuffer.wrap(new int[1]);
        gl.glGenVertexArrays (1, vao);
        gl.glBindVertexArray(vao.get(0));
        gl.glEnableVertexAttribArray (0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glVertexAttribPointer (0, 3, GL3.GL_FLOAT, false, 0, 0L); // 最後の引数はnullではない
        this.vao=vao.get(0);
        System.out.println("DEBUG: VAO設定完了 :"+this.vao);

        // シェーダーの設定
        int vs = gl.glCreateShader (GL3.GL_VERTEX_SHADER);
        gl.glShaderSource (vs, 1, this.vertex_shader, null);
        gl.glCompileShader (vs);
        int fs = gl.glCreateShader (GL3.GL_FRAGMENT_SHADER);
        gl.glShaderSource (fs, 1, this.fragment_shader, null);
        gl.glCompileShader (fs);

        this.shader_programme = gl.glCreateProgram ();
        gl.glAttachShader (shader_programme, fs);
        gl.glAttachShader (shader_programme, vs);
        gl.glLinkProgram (shader_programme);
        System.out.println("DEBUG: シェーダーコンパイル完了:"+this.shader_programme);

        int err=gl.glGetError();
        System.out.println("DEBUG: エラー確認:"+err);
    }

    @Override
    public void reshape(GLAutoDrawable gla, int arg1, int arg2, int arg3, int arg4) {

    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
    // TODO 自動生成されたメソッド・スタブ

    }


}
