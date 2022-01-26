package samples;
import javax.swing.JFrame;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import k7system.*;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;
import k7system.primitives.Cube;

public class MinimumProg extends JFrame implements GameCallBack{
    private GraphicEngine engine;
    private Model3D model;
    private LightObject light;
    private int counter=0;

    public static void main(String[] argv){
        JFrame frame=new MinimumProg();
        frame.setVisible(true);
    }

    public MinimumProg() {
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();        // カメラパラメータなどを設定
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);
        this.engine.setCameraPosition(new float[]{0.0f,1.0f,6.5f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.5f,0.5f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        this.light=new LightObject();        // ライト追加
        this.light.setLightPosition(new float[]{0,0,3,1});
        this.light.setPower(new float[]{1.5f,1.5f,1.5f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        BasicMaterial material=new BasicMaterial(); // 基本マテリアルを利用
        material.setColor(0.4f, 1.0f, 0.4f, 1.0f); // 色を設定
        material.setSpecularColor(1.0f, 0.4f, 0.4f, 1.0f); // 鏡面反射色を設定
        material.setShinness(10);

        VertexArrayObject vao=new Cube(3); // 立方体設定
        VertexPackage vp=new VertexPackage(vao,material);
        this.model=new Model3D();
        this.model.addVertexPackage(vp,0);
        this.model.useLight(false);
        this.engine.addNode(this.model);

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