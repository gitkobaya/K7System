package samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Model3D;
import k7system.Node3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;
import k7system.loaders.StlLoader;
import k7system.primitives.Cube;

/** GraphicEngineを利用した描画 */
public class OGLK7StyleNSX extends JFrame implements GameCallBack{

    private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;

    private Model3D model1;
    private Model3D model2;
    private Model3D model3;
    private LightObject light;
    private LightObject light2;

    public static void main(String[] args){
            JFrame frame=new OGLK7StyleNSX();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLK7StyleNSX() {
        this.logger.setLevel(Level.INFO);
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);
        this.engine.setCameraPosition(new float[]{0.0f,1.0f,6.5f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.5f,0.5f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        // 土台となるノード
        Node3D baseNode=new Node3D();
        baseNode.translate(0, -0.5f, 0);
        baseNode.rotate(-90,1,0,0);
        this.engine.addNode(baseNode);

        BasicMaterial material=new BasicMaterial(); // 基本マテリアルを利用
        material.setColor(1.0f, 0.9f, 0.4f, 1.0f);
        //material.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        material.setSpecularColor(1.0f, 0.2f, 0.0f, 1f);
        //material.setSpecularColor(1.0f, 0.7f, 0.5f, 1f);
        material.setShinness(5);
        VertexArrayObject vao=StlLoader.loadVAOModel(new File("nsx_concept_LOW.stl")); //NSX
        VertexPackage vPack=new VertexPackage(vao, material); // 頂点パッケージを生成
        this.model1=new Model3D();   // Model3Dオブジェクトを生成
        this.model1.addVertexPackage(vPack);
        this.model1.setScale(0.05f);
        baseNode.attach(this.model1);


        VertexArrayObject vao2=new Cube(2.0);
        vao2.createNormals();

        BasicMaterial material2=new BasicMaterial(); // 基本マテリアルを利用
        material2.setColor(1, 1, 1, 1f);
        material2.setSpecularColor(0.0f, 0.0f, 0.0f, 0.0f);
        VertexPackage vPack2=new VertexPackage(vao2, material2); // 頂点パッケージを生成
        this.model2=new Model3D();        // Model3Dオブジェクトを生成
        this.model2.addVertexPackage(vPack2);
        this.model2.useLight(false);
        this.model2.setScale(0.05f);
        this.engine.addNode(model2);

        this.model3=new Model3D();        // Model3Dオブジェクトを生成
        this.model3.addVertexPackage(vPack2);
        this.model3.useLight(false);
        this.model3.setScale(0.05f);
        this.engine.addNode(model3);


        // 床用マテリアル
        BasicMaterial material3=new BasicMaterial(); // 基本マテリアルを利用
        material3.setColor(1.0f, 0.5f, 0.5f, 1f);
        material3.setSpecularColor(0.5f, 0.5f, 0.5f, 1.0f);
        VertexPackage vPack3=new VertexPackage(vao2, material3);

        Model3D model4=new Model3D();
        model4.addVertexPackage(vPack3);
        model4.setScale(4);
        model4.setPosition(0, -4.5f, 0);
        this.engine.addNode(model4);

        // 操作用ライト
        this.light=new LightObject();
        this.light.setLightPosition(new float[]{0,0,3,1});
        this.light.setPower(new float[]{1.5f,1.5f,1.5f});
        //this.light.setAmbient(new float[]{0.1f,0.1f,0.1f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        this.light2=new LightObject();
        this.light2.setLightPosition(new float[]{0,0,-3,1});
        this.light2.setPower(new float[]{1.5f,1.5f,1.5f});
        //this.light2.setAmbient(new float[]{0.1f,0.1f,0.1f});
        this.light2.enable();
        this.engine.addLightObject(this.light2);

        // 天空にライトを作ってみる
        LightObject light3=new LightObject();
        light3.setLightPosition(new float[]{0.0f,-0.7f,-0.7f, 0});
        light3.setPower(new float[]{0.4f,0.4f,0.4f});
        //light3.setAmbient(new float[]{0.2f,0.2f,0.2f});
        light3.enable();
        this.engine.addLightObject(light3);

        // 照り返し用ライト
        LightObject light4=new LightObject();
        light4.setLightPosition(new float[]{0.0f,1f,0f, 0});
        light4.setPower(new float[]{0.1f,0.04f,0.04f});
        light4.enable();
        this.engine.addLightObject(light4);

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
        this.model1.rotate(rot*0.5f, 0 ,0 , 1);

        float x=(float)(3*Math.cos(rot/100));
        float z=(float)(3*Math.sin(rot/100));
        this.model2.setPosition(x, 1.5f, z);
        this.light.setLightPosition(new float[]{x, 1.5f, z,1.0f});

        float x2=(float)(2*Math.cos(rot/80));
        float z2=(float)(2*Math.sin(rot/80));
        this.model3.setPosition(x2, 2.0f, z2);
        this.light2.setLightPosition(new float[]{x2, 2.0f, z2,1.0f});

        //this.engine.setCameraPosition(new float[]{0.0f,0.0f, 5.0f+(float)(1.0*Math.cos(rot/50))}, new float[]{0f,0f,0}, new float[]{0,1,0});
        rot+=1;
        if (rot==200){ // ライト1消滅
            this.model2.detachMe();
            //this.model2.setVisible(false);
            this.light.disable();
        }
        if (rot==300){ // ライト2消滅
            this.model3.detachMe();
            //this.model3.setVisible(false);
            this.light2.disable();
        }
        if (rot==400){ // ライト1復活
            this.engine.addNode(this.model2);
            //this.model2.setVisible(true);
            this.light.enable();
        }
        if (rot==500){ // ライト2復活
            this.engine.addNode(this.model3);
            //this.model3.setVisible(true);
            this.light2.enable();
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


