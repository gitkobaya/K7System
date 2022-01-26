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

import k7system.BlendType;
import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.Material;
import k7system.gpuobjects.Shader;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;
import k7system.primitives.Cube;

/** リソースの削除がうまく行くか確認 */
public class OGLTestDispose extends JFrame implements GameCallBack{
    private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;

    private Model3D model1;
    private Model3D model2;
    private Model3D model3;
    private LightObject light;

    public static void main(String[] args){
        JFrame frame=new OGLTestDispose();
        frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTestDispose() {
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

        VertexArrayObject vao=new Cube(1);

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
        material3.setColor(0, 0.5f, 0.5f, 1);
        material3.setSpecularColor(0,0,0,0);
        material3.setEmissionColor(0.2f, 0.2f, 0.2f, 1);
        VertexPackage vPack3=new VertexPackage(vao, material3);
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
    /** 描画処理の際に呼ばれるコールバックメソッド<br>
     * オブジェクトを削除した時にリソースが解放されることを確認 */
    @Override
    public void displayCall(GLAutoDrawable gla) {

        if (rot!=200){
            this.model1.rotate(rot, 0 ,1 , 0);
        }else{
            this.model1.detachMe();
            this.model1.dispose((GL3)gla.getGL());
        }

        if (rot!=150){
            this.model2.setTransparent(BlendType.ADDING);
            this.model2.getVertexPackages(0).get(0).getMaterial().setAlpha((float)(Math.abs(Math.sin(rot /360))));
            this.model2.rotate(rot, 1 ,0 , 0);
        }else{
            this.model2.detachMe();
            this.model2.dispose((GL3)gla.getGL());
        }

        if (rot!=100){
            this.model3.setTransparent(BlendType.BLEND);
            this.model3.getVertexPackages(0).get(0).getMaterial().setAlpha((float)(Math.abs(Math.sin(rot/360+180))));
            this.model3.rotate(rot, 0 ,1 , 0);
        }else{
            this.model3.detachMe();
            this.model3.dispose((GL3)gla.getGL());
        }

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
    }
}