package samples;

import java.awt.Image;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.BlendType;
import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.Material;
import k7system.gpuobjects.Shader;
import k7system.gpuobjects.TextureK7;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** GraphicEngineを利用した描画 */
public class OGLTexturedBoxedUseK7Style extends JFrame implements GameCallBack{

    private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;

    private Model3D model1;
    private Model3D model2;
    private Model3D model3;

    private LightObject light;

    // モデルの座標
    private float points[] = {
       -0.5f,  0.5f,  0.5f,
       -0.5f, -0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f,  -0.5f,
        -0.5f, -0.5f,  -0.5f,
         0.5f, -0.5f,  -0.5f,
         0.5f,  0.5f,  -0.5f,

         -0.5f,  0.5f,  0.5f,
         -0.5f, -0.5f,  0.5f,
          0.5f, -0.5f,  0.5f,
          0.5f,  0.5f,  0.5f,
          -0.5f,  0.5f,  -0.5f,
          -0.5f, -0.5f,  -0.5f,
           0.5f, -0.5f,  -0.5f,
           0.5f,  0.5f,  -0.5f,

           -0.5f,  0.5f,  0.5f,
           -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f,  -0.5f,
            -0.5f, -0.5f,  -0.5f,
             0.5f, -0.5f,  -0.5f,
             0.5f,  0.5f,  -0.5f,
    };

    // テクスチャ座標
    private float uv[]={
            0.0f, 0.0f, // 正面(1)
            0.25f, 0.0f,
            0.25f, 0.25f,
            0.0f, 0.25f,

            0.25f, 0.25f, //裏面(6)
            0.5f, 0.25f,
            0.5f, 0.5f,
            0.25f, 0.5f,

            0.25f, 0.0f, // 右面(2)
            0.5f, 0.0f,
            0.5f, 0.25f,
            0.25f, 0.25f,

            0.0f, 0.25f, // 左面(5)
            0.25f, 0.25f,
            0.25f, 0.5f,
            0.0f, 0.5f,

            0.5f, 0.0f, // 上面(3)
            0.75f,0.0f,
            0.75f, 0.25f,
            0.5f,0.25f,

            0.75f, 0.0f, // 下面(4)
            1.0f, 0.0f,
            1.0f, 0.25f,
            0.75f, 0.25f
    };

    // 頂点インデックス
    private int index[]={
            0,1,2,  2,3,0,            // 正面
            6,5,4,  4,7,6,            // 裏面

            11,10,15,  10,14,15,            // 右面
            8,12,13,  8,13,9,            // 左面

            16,19,20,  19,23,20,            // 上面
            17,21,18,  18,21,22,            // 下面
    };


    public static void main(String[] args){
            JFrame frame=new OGLTexturedBoxedUseK7Style();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTexturedBoxedUseK7Style() {
        this.logger.setLevel(Level.INFO);
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);
        this.engine.setCameraPosition(new float[]{0.0f,0.0f,5.0f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.5f,0.5f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        // フラットポリゴン
        VertexArrayObject vao=new VertexArrayObject();
        vao.setVertices(this.points); // 頂点座標を登録
        vao.setIndices(this.index); // 頂点インデックスを登録
        vao.createNormals();

        // テクスチャ付きポリゴン
        VertexArrayObject vao2=new VertexArrayObject();
        vao2.setVertices(this.points); // 頂点座標を登録
        vao2.setTexCoords(this.uv);
        vao2.setIndices(this.index); // 頂点インデックスを登録
        vao2.createNormals();


        BasicMaterial material=new BasicMaterial(); // 基本マテリアルを利用
        material.setColor(1, 0.5f, 0, 0.5f);
        material.setSpecularColor(0.0f, 0.0f, 0.0f, 0.0f);
        material.setShinness(5);
        VertexPackage vPack=new VertexPackage(vao, material); // 頂点パッケージを生成
        this.model1=new Model3D();        // Model3Dオブジェクトを生成
        this.model1.addVertexPackage(vPack);

        BasicMaterial material2=new BasicMaterial();
        material2.setColor(0.5f, 1.0f, 0f, 0.5f);
        material2.setShinness(20);
        VertexPackage vPack2=new VertexPackage(vao, material2);
        this.model2=new Model3D();
        this.model2.addVertexPackage(vPack2);

        BasicMaterial material3=new BasicMaterial();
        material3.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        try{
            material3.setDiffuseTexture(new TextureK7(ImageIO.read(new File("dice.png"))));
        }catch(Exception e){
            e.printStackTrace();
        }
        VertexPackage vPack3=new VertexPackage(vao2, material3);
        this.model3=new Model3D();
        this.model3.addVertexPackage(vPack3);

        // モデル1をグラフィックエンジンに登録
        this.engine.addNode(this.model1);
        this.model1.setVisible(true);
        this.model1.setName("Test Model");

        // モデル2をモデル1に登録
        this.model1.attach(model2);
        this.model2.setVisible(true);
        this.model2.setName("Test Model2");
        this.model2.translate(1, 0, 0);

        // モデル3をモデル2に登録
        this.model2.attach(model3);
        this.model3.setVisible(true);
        this.model3.setName("Test Model3");
        this.model3.translate(0, 1, 0);

        // 天空にライトを作ってみる
        this.light=new LightObject();
        this.light.setLightPosition(new float[]{0,0,3f,1});
        this.light.setPower(new float[]{3f,3f,3f});
        this.light.setAmbient(new float[]{0.0f,0.0f,0.0f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        LightObject light2=new LightObject();
        light2.setLightPosition(new float[]{0,0,-1,0});
        light2.setPower(new float[]{1.5f,1.5f,1.5f});
        light2.enable();
        this.engine.addLightObject(light2);

        this.engine.setGameCallBack(this); // コールバックを登録
        this.engine.start(60); // FPSを指定して描画開始

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void initCall(GLAutoDrawable gla) {
    }

    @Override
    public void initFinish(GLAutoDrawable gla) {
    }


    float rot=0;
    /** 描画処理の際に呼ばれるコールバックメソッド */
    @Override
    public void displayCall(GLAutoDrawable gla) {
        float length=(float)(Math.sqrt(2)/2);
        //this.model1.rotate(rot,length ,length , 0);
        this.model1.rotate(rot, 0 ,1 , 0);
        this.model2.rotate(rot, 1 ,0 , 0);

        this.model2.setTransparent(BlendType.ADDING);
        this.model2.getVertexPackages(0).get(0).getMaterial().setAlpha((float)(Math.abs(Math.sin(rot /360))));

        this.model3.setTransparent(BlendType.BLEND);
        this.model3.getVertexPackages(0).get(0).getMaterial().setAlpha((float)(Math.abs(Math.sin(rot/360+180))));
        this.model3.rotate(rot, 0 ,1 , 0);
        //this.engine.setCameraPosition(new float[]{0.0f,0.0f, 5.0f+(float)(1.0*Math.cos(rot/50))}, new float[]{0f,0f,0}, new float[]{0,1,0});

        /*
        float x=3*(float)Math.cos(rot/50);
        float y=3*(float)Math.sin(rot/50);
        this.engine.setCameraPosition(new float[]{x,0, y}, new float[]{0f,0f,0}, new float[]{0,1,0});
        */
        rot+=0.5;
    }

    @Override
    public void displayFinish(GLAutoDrawable gla) {
    }

    @Override
    public void reshapeCall(GLAutoDrawable gla) {
    }

    @Override
    public void reshapeFinish(GLAutoDrawable gla) {
    }

    @Override
    public void graphicEngineIsSet(GraphicEngine engine) {
        // TODO 自動生成されたメソッド・スタブ

    }
}