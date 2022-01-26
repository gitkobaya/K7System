package samples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.TextureK7;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** テクスチャのテスト */
public class OGLTestFloatTexture extends JFrame implements GLEventListener{

    private Logger logger=Logger.getGlobal();
    private VertexArrayObject vao;

    private BasicMaterial material;

    // 三角形の座標
    private float points[] = {
            0.0f,  0.5f,  0.0f,
            0.5f, -0.5f,  0.0f,
            -0.5f, -0.5f,  0.0f
    };

    // テクスチャのUV座標
    private float uv[]={
            0.5f,  1.0f,
            1.0f,  0.0f,
            0.0f,  0.0f
    };

    // 頂点インデックス
    private int index[]={
            0,1,2
    };

    // テクスチャ
    private List<TextureK7> textures=new ArrayList<TextureK7>();

    // 頂点データパッケージ
    private VertexPackage pack;

    public static void main(String[] args){
        JFrame frame=new OGLTestFloatTexture();
        frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTestFloatTexture() {
        this.setSize(640, 480);
        GLJPanel panel=new GLJPanel();
        panel.addGLEventListener(this);
        this.add(panel);
        this.logger.setLevel(Level.INFO);

        this.vao=new VertexArrayObject();
        this.vao.setVertices(this.points); // 頂点座標を登録
        this.vao.setTexCoords(this.uv); // 頂点座標を登録
        this.vao.setIndices(this.index); // 頂点インデックスを登録

        try{
            TextureK7 tex=new TextureK7(ImageIO.read(new File("dice.png")));
            tex.setName("texture 0");
            tex.setTextureDataType(GL3.GL_DEPTH_COMPONENT, GL3.GL_UNSIGNED_BYTE); // これがちゃんと通るか
            this.textures.add(tex);
            tex=new TextureK7(ImageIO.read(new File("blueDice.png")));
            tex.setName("texture 1");
            this.textures.add(tex);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }

        this.material=new BasicMaterial();
        this.material.setDiffuseTexture(this.textures.get(1));
        this.material.setUseLights(false);

        this.pack=new VertexPackage(vao, material);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    int counter=0;
    @Override
    public void display(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());
        gl.glClear( GL.GL_COLOR_BUFFER_BIT |GL.GL_DEPTH_BUFFER_BIT);

        // 描画
        this.pack.draw(gl);

        if (counter % 200==0){
            this.material.setDiffuseTexture(this.textures.get(1));
            System.out.println("DEBUG: texture 1");
        }else if (counter % 100==0){
            this.material.setDiffuseTexture(this.textures.get(0));
            System.out.println("DEBUG: texture 0");
        }

        counter++;
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
        //System.out.println("DEBUG: Reshape");
    }
    @Override
    public void dispose(GLAutoDrawable gla) {
        GL3 gl=(GL3)(gla.getGL());

        // VAOの後始末
        this.vao.dispose(gl);

    }

}
