package samples;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;

public class OGLTriangleUseK7Style extends JFrame implements GLEventListener{

    private Logger logger=Logger.getGlobal();
    private VertexArrayObject vao;

    private BasicMaterial material;

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

    public static void main(String[] args){
            JFrame frame=new OGLTriangleUseK7Style();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTriangleUseK7Style() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);
        this.logger.setLevel(Level.INFO);

        this.vao=new VertexArrayObject();
        this.vao.setVertices(this.points); // 頂点座標を登録
        this.vao.setIndices(this.index); // 頂点インデックスを登録

        this.material=new BasicMaterial();
        this.material.setColor(1, 0, 0);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void display(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());
        gl.glClear( GL.GL_COLOR_BUFFER_BIT |GL.GL_DEPTH_BUFFER_BIT);

        // マテリアルのバインド
        material.setUseLights(false);
        material.bind(gl);

        // 描画
        this.vao.draw(gl);

    }

    @Override
    public void init(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // 背景色は暗い青
        gl.glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        gl.glEnable (GL.GL_DEPTH_TEST);
        gl.glDepthFunc (GL.GL_LESS);

        // マテリアルの初期化
        this.material.init(gl,null);

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
