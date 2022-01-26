package samples;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** 頂点パッケージを利用した描画 */
public class OGLTriangleUseK7Style2 extends JFrame implements GLEventListener{

	private static final long serialVersionUID = 1L;

	private Logger logger=Logger.getGlobal();

    private VertexPackage vPack;

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
            JFrame frame=new OGLTriangleUseK7Style2();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTriangleUseK7Style2() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);
        this.logger.setLevel(Level.INFO);

        VertexArrayObject vao=new VertexArrayObject();
        vao.setVertices(this.points); // 頂点座標を登録
        vao.setIndices(this.index); // 頂点インデックスを登録

        BasicMaterial material=new BasicMaterial();
        material.setColor(1, 0.5f, 0, 1);
        material.setUseLights(false);

        this.vPack=new VertexPackage(vao, material);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void display(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());
        gl.glClear( GL3.GL_COLOR_BUFFER_BIT |GL3.GL_DEPTH_BUFFER_BIT);

        // 頂点パッケージの描画
        this.vPack.draw(gl);
    }

    @Override
    public void init(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // 背景色は暗い青
        gl.glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        gl.glEnable (GL4.GL_DEPTH_TEST);
        gl.glDepthFunc (GL4.GL_LESS);

        this.vPack.init(gl, null);
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
        this.vPack.dispose(gl);
    }
}
