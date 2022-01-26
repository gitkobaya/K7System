package samples;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** GraphicEngineを利用した描画 */
public class OGLK7StyleCheckBoundingBox extends JFrame implements GameCallBack{

    private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;

    private Model3D model1;

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
            JFrame frame=new OGLK7StyleCheckBoundingBox();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLK7StyleCheckBoundingBox() {
        this.logger.setLevel(Level.INFO);
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);
        this.engine.setCameraPosition(new float[]{0.0f,0.0f,4.0f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.5f,0.5f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        VertexArrayObject vao=new VertexArrayObject();
        vao.setVertices(this.points); // 頂点座標を登録
        vao.setIndices(this.index); // 頂点インデックスを登録
        vao.createNormals();

        BasicMaterial material=new BasicMaterial(); // 基本マテリアルを利用
        material.setColor(1, 0.5f, 0, 0.5f);
        material.setSpecularColor(0.0f, 0.0f, 0.0f, 0.0f);
        material.setShinness(5);
        VertexPackage vPack=new VertexPackage(vao, material); // 頂点パッケージを生成
        this.model1=new Model3D();        // Model3Dオブジェクトを生成
        this.model1.addVertexPackage(vPack);

        // モデル1をグラフィックエンジンに登録
        this.engine.addNode(this.model1);
        this.model1.setVisible(true);
        this.model1.setName("Test Model");

        // 天空にライトを作ってみる
        this.light=new LightObject();
        this.light.setLightPosition(new float[]{0,0,1.5f,1});
        this.light.setPower(new float[]{2f,2f,2f});
        this.light.setAmbient(new float[]{0.1f,0.1f,0.1f});
        this.light.enable();
        this.engine.addLightObject(this.light);

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


    float rot=-3;
    /** 描画処理の際に呼ばれるコールバックメソッド */
    @Override
    public void displayCall(GLAutoDrawable gla) {
        this.model1.setPosition(rot,0,0);
        this.model1.rotate(rot*30, 0,1,0);
        rot+=0.01;

        if (rot>2){
            rot=-2;
        }
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