package samples;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import k7system.*;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;
import k7system.primitives.Cube;

public class BillboardTest extends JFrame implements GameCallBack{
    private GraphicEngine engine;
    private Billboard billboard1;
    private Billboard billboard2;
    private Model3D model;

    private LightObject light;
    private int counter=0;

    public static void main(String[] argv){
        JFrame frame=new BillboardTest();
        frame.setVisible(true);
    }

    public BillboardTest() {
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();        // カメラパラメータなどを設定
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);

        float modelX=1;
        float modelY=1;
        float modelZ=1;

        this.engine.setCameraPosition(new float[]{0.0f,0.0f,6.5f}, new float[]{modelX,modelY,modelZ}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.3f,0.3f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        this.light=new LightObject();        // ライト追加
        this.light.setLightPosition(new float[]{0,0,3,1});
        this.light.setPower(new float[]{1.5f,1.5f,1.5f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        // 座標確認用デバッグ物体
        BasicMaterial material=new BasicMaterial(); // 基本マテリアルを利用
        material.setColor(0.4f, 1.0f, 0.4f, 1.0f); // 色を設定
        material.setSpecularColor(1.0f, 0.4f, 0.4f, 1.0f); // 鏡面反射色を設定
        material.setShinness(10);
        VertexArrayObject vao=new Cube(1.0); // 立方体設定
        VertexPackage vp=new VertexPackage(vao,material);
        this.model=new Model3D();
        this.model.setPosition(modelX, modelY, modelZ);
        this.model.addVertexPackage(vp,0);
        this.model.useLight(true);
        this.engine.addNode(this.model);

        // ビルボード生成
        try{
            this.billboard1=new Billboard(ImageIO.read(new File("blueDice.png")));
            this.billboard1.setPosition(0.55f, 0, 0);
            //this.billboard1.setTransparent(BlendType.BLEND);
            this.billboard1.setScale(0.5f);
            this.model.attach(this.billboard1);

            this.billboard2=new Billboard(ImageIO.read(new File("test.png")));
            this.billboard2.setCoordinateSystem(Billboard.SCREEN_COORDINATES);
            this.billboard2.setOrigineType(Billboard.ORIGIN_TYPE_LEFT_BOTTOM);
            this.billboard2.setPosition(-1.0f, -1.0f, -1.0f);
            this.engine.addNode(billboard2);


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
        //this.billboard.setPosition((float)Math.cos((float)counter/100), (float)Math.sin((float)counter/100),  0);

        //this.model.setPosition((float)Math.cos((float)counter/100), (float)Math.sin((float)counter/100),  0);
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