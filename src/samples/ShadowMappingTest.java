package samples;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.Billboard;
import k7system.BlendType;
import k7system.CameraObject;
import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.FloatTextureK7;
import k7system.gpuobjects.FrameBufferObject;
import k7system.gpuobjects.DepthMaterial;
import k7system.gpuobjects.TextureK7;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;
import k7system.primitives.Cube;
import samples.PolygonObject;

/** シャドウマッピングの実験 */
public class ShadowMappingTest extends JFrame implements GameCallBack{
	private static final long serialVersionUID = 8205688416279040094L;
	private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;
    private Model3D model1;

    private FrameBufferObject cameraFbo; // カメラからのデプスマップ
//    private FrameBufferObject lightFbo; // ライトからのデプスマップ
    private Billboard billboard;
    private DepthMaterial shadowBufferMaterial;
    private LightObject skyLight;

    private static final int FBO_SIZE=512;

    public static void main(String[] args){
        JFrame frame=new ShadowMappingTest();
        frame.setVisible(true);
    }

    /** コンストラクタ */
    public ShadowMappingTest() {
        this.logger.setLevel(Level.INFO);
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, 20f);
        this.engine.setCameraPosition(new float[]{0.0f,1.0f,7.0f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.5f,0.5f,0.5f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        BasicMaterial material=new BasicMaterial(); // 基本マテリアルを利用
        material.setColor(1, 0.5f, 0, 1.0f);
        material.setSpecularColor(1.0f, 0.5f, 0.0f, 1f);
        material.setShinness(2);
        VertexArrayObject vaoBunny=PolygonObject.loadData(new File("bun_zipper.ply"),20);
        VertexArrayObject vaoBunny2=PolygonObject.loadData(new File("bun_zipper_res2.ply"),20);
        VertexArrayObject vaoBunny3=PolygonObject.loadData(new File("bun_zipper_res3.ply"),20);
        VertexArrayObject vaoBunny4=PolygonObject.loadData(new File("bun_zipper_res4.ply"),20);
        VertexPackage vPackBunny=new VertexPackage(vaoBunny, material); // 頂点パッケージを生成
        VertexPackage vPackBunny2=new VertexPackage(vaoBunny2, material); // 頂点パッケージを生成
        VertexPackage vPackBunny3=new VertexPackage(vaoBunny3, material); // 頂点パッケージを生成
        VertexPackage vPackBunny4=new VertexPackage(vaoBunny4, material); // 頂点パッケージを生成
        float[] depth=new float[]{6f, 8f, 11f, Float.MAX_VALUE};
        this.model1=new Model3D();   // Model3Dオブジェクトを生成
        this.model1.setLodChangeDepth(depth);

        this.model1.addVertexPackage(vPackBunny,0);
        this.model1.addVertexPackage(vPackBunny2,1);
        this.model1.addVertexPackage(vPackBunny3,2);
        this.model1.addVertexPackage(vPackBunny4,3);
        this.model1.setPosition(0, -2.1f, 0);
        this.engine.addNode(model1);

        VertexArrayObject vao2=new Cube(1.0);

        // カメラからのデプスバッファ
        FloatTextureK7 textureCameraDepth=new FloatTextureK7(FBO_SIZE, FBO_SIZE, 1); // テクスチャを作りまして
        textureCameraDepth.setTextureDataType(GL3.GL_RED, GL3.GL_UNSIGNED_INT);
        textureCameraDepth.setSamplerConfig(GL3.GL_CLAMP_TO_EDGE, GL3.GL_CLAMP_TO_EDGE, GL3.GL_NEAREST, GL3.GL_NEAREST);
        this.cameraFbo=new FrameBufferObject(textureCameraDepth); // それを登録します


        // 床
        BasicMaterial materialFloor=new BasicMaterial(); // 基本マテリアルを利用
        materialFloor.setColor(1.0f, 0.5f, 0.5f, 1f);
        materialFloor.setSpecularColor(0.0f, 0.0f, 0.0f, 1.0f);
        VertexPackage vPack3=new VertexPackage(vao2, materialFloor);
        Model3D model3=new Model3D();
        model3.addVertexPackage(vPack3);
        model3.setScale(4);
        model3.setPosition(0, -5.5f, 0);
        this.engine.addNode(model3);

        // 天空にライトを作ってみる
        this.skyLight=new LightObject();
        this.skyLight.setLightPosition(new float[]{0.1f,-0.9f, -0.1f, 0});
        this.skyLight.setPower(new float[]{0.3f,0.3f,0.3f});
        this.skyLight.setAmbient(new float[]{0.2f,0.2f,0.2f});
        this.skyLight.setShadowFlag(true);
        this.skyLight.enable();
        this.engine.addLightObject(skyLight);

        // 深度バッファ用FBO関係
        // カメラからのデプスバッファ
        TextureK7 cameraDepth=this.engine.getCameraObject().getDepthFbo().getTexture();

        // ライトからのデプスバッファ
//        TextureK7 tex=this.skyLight.getShadowFbo().getTexture();
        //this.billboard=new Billboard(tex); // ビルボードに貼り付けます
        this.billboard=new Billboard(cameraDepth); // ビルボードに貼り付けます
        this.billboard.setOrigineType(Billboard.ORIGIN_TYPE_LEFT_BOTTOM);
        this.billboard.setCoordinateSystem(Billboard.SCREEN_COORDINATES);
        this.billboard.setMirrorY(true);
        this.billboard.setPosition(-1.0f, -1.0f, -0.99f);
        this.billboard.setEngine(this.engine);
        this.billboard.setScale(0.5f);
        this.shadowBufferMaterial=new DepthMaterial();

        this.engine.setGameCallBack(this); // コールバックを登録
        this.engine.start(60); // FPSを指定して描画開始

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void initCall(GLAutoDrawable gla) {
        GL3 gl=(GL3)gla.getGL();
        Object ext = gl.getExtension("OES_texture_float");
        if(ext == null){
            logger.severe("float texture not supported");
            return;
        }
    }

    @Override
    public void initFinish(GLAutoDrawable gla) {
        GL3 gl=(GL3)gla.getGL();
        this.cameraFbo.init(gl, this.engine);
        this.shadowBufferMaterial.init(gl, this.engine);
    }

    float rot=0;
    /** 描画処理の際に呼ばれるコールバックメソッド */
    @Override
    public void displayCall(GLAutoDrawable gla) {
        GL3 gl=(GL3)gla.getGL();
        this.model1.rotate(rot, 0 ,1 , 0);
        this.model1.setTransparent(BlendType.BLEND);
        this.engine.setCameraPosition(new float[]{0.0f,0.0f, 10.0f+(float)(6.0*Math.cos(rot/100))}, new float[]{0f,0f,0}, new float[]{0,1,0});
        rot+=1;

        // カメラからの奥行き画像を生成
        CameraObject camera=this.engine.getCameraObject();
        camera.drawDepthBuffer(gl);

        this.skyLight.drawShadowBuffer(gl);
    }

    @Override
    public void displayFinish(GLAutoDrawable gla) {
        GL3 gl=(GL3)gla.getGL();
        this.billboard.draw(gl);
        //System.out.println("DEBUG: LoD:"+this.model1.getCurrentLod());
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
