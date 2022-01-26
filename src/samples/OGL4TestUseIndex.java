package samples;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

public class OGL4TestUseIndex extends JFrame implements GLEventListener{

    // 三角形の座標
    private float points[] = {
        0.0f,  0.5f,  0.0f,
        0.5f, -0.5f,  0.0f,
       -0.5f, -0.5f,  0.0f
     };

    // インデックス利用
    private int index[]={
            0,1,2
    };

    // シェーダー
    private String[] vertex_shader ={
            "#version 400\n"+
            "in vec3 vp;\n"+
            "void main () {\n"+
            "  gl_Position = vec4 (vp, 1.0);\n"+
            "}"};

    private String[] fragment_shader ={
            "#version 400\n"+
            "out vec4 frag_colour;\n"+
            "void main () {\n"+
            "  frag_colour = vec4 (1.0, 0.0, 0.0, 0.8);\n"+
            "}"};

    private int shader_programme;

    private int vao;

    /** 起動時に使うだけのメインメソッド */
    public static void main(String args[]){
        JFrame frame=new OGL4TestUseIndex();
        frame.setVisible(true);

        /*
        while(true){
            frame.repaint();
            try{
                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        */
    }

    /** コンストラクタ */
    public OGL4TestUseIndex() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void display(GLAutoDrawable gla) {
        GL4 gl=(GL4)gla.getGL();

        // wipe the drawing surface clear
        gl.glClear (GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram (this.shader_programme);
        gl.glBindVertexArray (this.vao);
        // draw points 0-3 from the currently bound VAO with current in-use shader
        gl.glDrawArrays (GL4.GL_TRIANGLES, 0, 3);

        gl.glBindVertexArray(0);
        int err=gl.glGetError();

        System.out.println("DEBUG: Draw! ERROR:"+err);
    }

    @Override
    public void init(GLAutoDrawable gla) {
        GL4 gl=(GL4)gla.getGL();

        gl.glEnable (GL4.GL_DEPTH_TEST); // enable depth-testing
        gl.glDepthFunc (GL4.GL_LESS); // depth-testing interprets a smaller value as "closer"

        gl.glEnable(GL4.GL_BLEND);
        gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);

        String version=gla.getGLProfile().toString();
        System.out.println("DEBUG: プロファイル情報 :"+version);

        boolean isEnabled=gl.glIsEnabled(GL4.GL_BLEND);
        System.out.println("DEBUG: αブレンディング有効？ :"+isEnabled);

        IntBuffer att=IntBuffer.wrap(new int[1]);
        gl.glGetIntegerv(GL4.GL_MAX_VERTEX_ATTRIBS, att);
        System.out.println("DEBUG: 頂点アトリビュート数 :"+att.get(0));

        // VBOの設定
        IntBuffer vbo=IntBuffer.wrap(new int[1]);
        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer (GL4.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, Float.SIZE*9,FloatBuffer.wrap(points), GL4.GL_STATIC_DRAW);
        System.out.println("DEBUG: VBO設定完了 :"+vbo.get(0));

        // VAOの設定
        IntBuffer vao=IntBuffer.wrap(new int[1]);
        gl.glGenVertexArrays (1, vao);
        gl.glBindVertexArray(vao.get(0));
        gl.glEnableVertexAttribArray (0);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo.get(0));
        gl.glVertexAttribPointer (0, 3, GL4.GL_FLOAT, false, 0, 0L); // 最後の引数はnullではない
        this.vao=vao.get(0);
        System.out.println("DEBUG: VAO設定完了 :"+this.vao);

        // シェーダーの設定
        int vs = gl.glCreateShader (GL4.GL_VERTEX_SHADER);
        gl.glShaderSource (vs, 1, this.vertex_shader, null);
        gl.glCompileShader (vs);
        int fs = gl.glCreateShader (GL4.GL_FRAGMENT_SHADER);
        gl.glShaderSource (fs, 1, this.fragment_shader, null);
        gl.glCompileShader (fs);

        this.shader_programme = gl.glCreateProgram ();
        gl.glAttachShader (shader_programme, fs);
        gl.glAttachShader (shader_programme, vs);
        gl.glLinkProgram (shader_programme);
        System.out.println("DEBUG: シェーダーコンパイル完了:"+this.shader_programme);

        gl.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);

        int err=gl.glGetError();
        System.out.println("DEBUG: エラー確認:"+err);
    }

    @Override
    public void reshape(GLAutoDrawable gla, int arg1, int arg2, int arg3, int arg4) {
        System.out.println("DEBUG: Reshape");

        // 臨時措置
        //this.init(gla);
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        System.out.println("DEBUG: Finish");
    }

}
