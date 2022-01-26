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

import k7system.BlendType;
import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;
import k7system.loaders.StlLoader;
import k7system.primitives.Cube;

/** GraphicEngineを利用した描画 */
public class BunnyLodTest extends JFrame implements GameCallBack{

    private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;

    private Model3D model1;
    private Model3D model2;
    private LightObject light;

    public static void main(String[] args){
            JFrame frame=new BunnyLodTest();
            frame.setVisible(true);
    }

    /** コンストラクタ */
    public BunnyLodTest() {
        this.logger.setLevel(Level.INFO);
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -100);
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

        BasicMaterial material2=new BasicMaterial(); // 基本マテリアルを利用
        material2.setColor(1, 1, 1, 1f);
        material2.setSpecularColor(0.0f, 0.0f, 0.0f, 0.0f);
        VertexPackage vPack2=new VertexPackage(vao2, material2); // 頂点パッケージを生成
        this.model2=new Model3D();        // Model3Dオブジェクトを生成
        this.model2.addVertexPackage(vPack2);
        this.model2.useLight(false);
        this.model2.setScale(0.05f);
        this.engine.addNode(model2);

        // 床
        BasicMaterial material3=new BasicMaterial(); // 基本マテリアルを利用
        material3.setColor(1.0f, 0.5f, 0.5f, 1f);
        material3.setSpecularColor(0.0f, 0.0f, 0.0f, 1.0f);
        VertexPackage vPack3=new VertexPackage(vao2, material3);
        Model3D model3=new Model3D();
        model3.addVertexPackage(vPack3);
        model3.setScale(4);
        model3.setPosition(0, -5.5f, 0);
        this.engine.addNode(model3);

        // 操作用ライト
        this.light=new LightObject();
        this.light.setLightPosition(new float[]{0,0,3,1});
        this.light.setPower(new float[]{2.0f,2.0f,2.0f});
        this.light.setAmbient(new float[]{0.1f,0.1f,0.1f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        // 天空にライトを作ってみる
        LightObject light2=new LightObject();
        light2.setLightPosition(new float[]{0.0f,-0.7f,-0.7f, 0});
        light2.setPower(new float[]{0.3f,0.3f,0.3f});
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
        this.model1.rotate(rot, 0 ,1 , 0);
        this.model1.setTransparent(BlendType.BLEND);
        //this.model1.getVertexPackages().get(0).getMaterial().setAlpha((float)(Math.sin(rot /360)+1)/2);

        float x=(float)(3*Math.cos(rot/100));
        float z=(float)(3*Math.sin(rot/100));
        this.model2.setPosition(x, 1.0f, z);
        this.light.setLightPosition(new float[]{x, 1.0f, z,1.0f});

        this.engine.setCameraPosition(new float[]{0.0f,0.0f, 10.0f+(float)(6.0*Math.cos(rot/100))}, new float[]{0f,0f,0}, new float[]{0,1,0});
        rot+=1;
    }

    @Override
    public void displayFinish(GLAutoDrawable gla) {
        System.out.println("DEBUG: LoD:"+this.model1.getCurrentLod());
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

/** ポリゴンで構成された3Dオブジェクトです */
class PolygonObject {

    /** ファイルからデータをロードします */
    public static VertexArrayObject loadData(File file,float scale){
        final int SEEKING_HEADER_MODE=0x01;
        final int LOADING_VERTEX_MODE=0x02;
        final int LOADING_POLYGON_MODE=0x03;

        List<float[]> vtxs=new ArrayList<float[]>();
        List<int[]> idcs=new ArrayList<int[]>();

        int numOfVertex=0;
        int vtxCount=0;
        int numOfPolygons=0;
        int polCount=0;
        int mode=SEEKING_HEADER_MODE;

        VertexArrayObject newData=new VertexArrayObject();

        // ファイル読み込み用のリーダーを宣言
        BufferedReader reader=null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String line;
            // 1行ずつ読み込み
            while ((line = reader.readLine()) != null) {    // ヘッダ検索モード
                if (mode==SEEKING_HEADER_MODE){
                    // 頂点数発見
                    if (line.startsWith("element vertex")){
                        String[] tokens=line.split(" "); // スペースで文字列を分割
                        numOfVertex=Integer.parseInt(tokens[2]); // 分割された3番目の文字列を頂点数とする
                    }

                    // ポリゴン数発見
                    if (line.startsWith("element face")){
                        String[] tokens=line.split(" ");
                        numOfPolygons=Integer.parseInt(tokens[2]);
                    }

                    // ヘッダの終わり発見
                    if (line.startsWith("end_header")){
                        mode=LOADING_VERTEX_MODE;
                    }
                }else if (mode==LOADING_VERTEX_MODE){ // 頂点読み込みモード
                    if (vtxCount<numOfVertex){
                        String[] tokens=line.split(" ");
                        float x=Float.parseFloat(tokens[0]);
                        float y=Float.parseFloat(tokens[1]);
                        float z=Float.parseFloat(tokens[2]);
                        vtxs.add(new float[]{x*scale, y*scale, z*scale});
                        vtxCount++;
                    }else{
                        mode=LOADING_POLYGON_MODE;
                    }
                }else if(mode==LOADING_POLYGON_MODE){ // ポリゴン読み込みモード
                    if (polCount<numOfPolygons){
                        String[] tokens=line.split(" ");
                        int n=Integer.parseInt(tokens[0]); // ポリゴンの頂点数
                        int[] pol=new int[3];
                        for (int i=n-1;i>=0;i--){ // 逆の順番に登録する
                            pol[i]=Integer.parseInt(tokens[i+1]);
                        }
                        idcs.add(pol);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 改めて頂点配列を作成
        float[] vtxArray=new float[vtxs.size()*3];
        for (int i=0;i<vtxArray.length;i+=3){
            vtxArray[i]=vtxs.get(i/3)[0];
            vtxArray[i+1]=vtxs.get(i/3)[1];
            vtxArray[i+2]=vtxs.get(i/3)[2];
        }

        // 改めてインデックス配列を作成
        int[] idxArray=new int[idcs.size()*3];
        for (int i=0;i<idxArray.length;i+=3){
            idxArray[i]=idcs.get(i/3)[0];
            idxArray[i+1]=idcs.get(i/3)[1];
            idxArray[i+2]=idcs.get(i/3)[2];
        }

        newData.setVertices(vtxArray);
        newData.setIndices(idxArray);
        newData.createNormals();

        return newData;
    }
}

