package samples;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import k7system.*;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.TextureK7;
import k7system.loaders.MQOLoader;
import k7system.primitives.Cube;

public class NormalMapTest extends JFrame implements GameCallBack{
    private GraphicEngine engine;
    private Model3D model;
    private LightObject light;
    private int counter=0;

    public static void main(String[] argv){
        JFrame frame=new NormalMapTest();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public NormalMapTest() {
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();        // カメラパラメータなどを設定
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);
        this.engine.setCameraPosition(new float[]{0.0f,0.0f,4.5f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.5f,0.5f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        this.light=new LightObject();        // ライト追加
        this.light.setLightPosition(new float[]{0,0,3,1});
        this.light.setPower(new float[]{1.5f,1.5f,1.5f});
        this.light.setAmbient(new float[]{0.3f,0.3f,0.3f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        MQOLoader loader=new MQOLoader();
        this.model=loader.loadModel(new File("dice.mqo"));
        this.model.setScale(0.01f);
        //this.model.useLight(false);
        this.engine.addNode(this.model);

        BasicMaterial material=this.model.getVertexPackages().get(0).getMaterial();
        material.setSpecularColor(0.5f,0.5f, 0.5f);
        try{
            TextureK7 normalMap=new TextureK7(ImageIO.read(new File("diceNormal2.png")));
            normalMap.setName("Normal Texture");
            material.setNormalTexture(normalMap);
        }catch(Exception e){
            e.printStackTrace();
        }

        // エンジン設定
        this.engine.setGameCallBack(this);
        this.engine.start(60);
    }

    @Override
    public void graphicEngineIsSet(GraphicEngine engine) {
    }
    @Override
    public void initCall(GLAutoDrawable gla) {
    }
    @Override
    public void initFinish(GLAutoDrawable gla) {
    }
    /**ディスプレイ表示*/
    @Override
    public void displayCall(GLAutoDrawable gla) {
        this.model.rotate(counter, 0, 1, 0);
        counter++;
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
}