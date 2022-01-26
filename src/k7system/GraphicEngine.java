package k7system;

import java.awt.DisplayMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.GPUResource;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;

/** OpenGLによる表示用クラスです。<br>
 *  表示ルーチンとして、ゲームの本体とは切り離します。<br>
 *  @version 0.05 26/2/20 adapt to JOGL3.x<br>
 *  @author KUR */
public class GraphicEngine implements GLEventListener{

    public static final int ORTHO=0x1;
    public static final int PERSPECTIVE=0x2;

    private Version version;
    private GLAutoDrawable drawable;
    private GameCallBack callBack=null;

    private int projectionMode=ORTHO;

    Node3D rootNode=new Node3D(); // ルートノード

    private int numOfMaxLights=BasicMaterial.MAXIMUM_LIGHT_NUM; // 最大利用ライト数はBasicMaterial依存
    private ArrayList<LightObject> lightList=new ArrayList<LightObject>();
    private boolean lighting=false; // ライトを利用するかどうかのフラグ

    private FPSAnimator animator;


    private int screenWidth=0; // スクリーンの幅(ピクセル数です)
    private int screenHeight=0;// スクリーンの高さ(ピクセル数です)

    private float aspect = 1; // 画面のアスペクト比です。reshape以外から触らないこと。
    private boolean autoAspect=false;

    private CameraObject camera=null;

    private float[] bgColor=new float[]{0,0,0,0};// 背景色です
    private boolean isChangeBg=true;

    // 透明物体オブジェクト
    private List<Object3D> transModels=new ArrayList<Object3D>();

    // エンジンが管理しているオブジェクト一覧
    private List<Object3D> managedModels=new ArrayList<Object3D>();

    // 親がいなくなって参照されなくなったリソース一覧
    private Set<GPUResource> garbage=new HashSet<GPUResource>();

    // GC用ロック
    private Lock garbageLock=new ReentrantLock();
    private ScreenManager scrManager;
    private String extensions;

    // ログ用
    private Logger logger=Logger.getGlobal();

    /** コンストラクタです */
    public GraphicEngine(GLAutoDrawable gla){
        this.drawable=gla;
        gla.addGLEventListener(this);

        this.camera=new CameraObject(this);

        this.rootNode.setEngine(this);
        this.rootNode.setVisible(true);
        this.rootNode.setName("Root Node");

        Logger logger=Logger.getGlobal();
        logger.setLevel(Level.WARNING);
    }

    /** コールバックオブジェクトを設定します */
    public void setGameCallBack(GameCallBack cb){
        this.callBack=cb;
        this.callBack.graphicEngineIsSet(this); // コールバックメソッドに自分を渡します
    }

    /** スクリーンマネージャを作成します */
    public ScreenManager createScreenManager(JFrame frm){
        scrManager=new ScreenManager(frm);
        return scrManager;
    }

    /** 投影モードを取得します */
    public int getProjectionMode(){
        return this.projectionMode;
    }

    /** 投影モードを指定します<br>
     * ORTHOかPERSPECTIVEで指定します */
    public void setProjectionMode(int mode){
        this.projectionMode=mode;
    }

    /** アスペクト追従モードを取得します */
    public boolean isAutoAspect(){
        return this.autoAspect;
    }

    /** アスペクト追従モードを設定します<br>
     * アスペクト追従モードの場合，カメラパラメータでの縦横比は常に1にしてください．
     * 縦を基準にして，横の描画領域を伸縮させます． */
    public void setAutoAspect(boolean mode){
        this.autoAspect=mode;
    }

    /** 現在のアスペクト比を取得します */
    public float getAspect(){
        return this.aspect;
    }

    /** 透視行列を取得します<br>
     * クローンで渡しているので内容を操作しても実際の表示には影響ありません */
    public float[] getPerspectiveMatrix(){
        return this.camera.getPerspectiveMatrix();
    }

    /** カメラオブジェクトを取得します */
    public CameraObject getCameraObject(){
        return this.camera;
    }

    /** 視点パラメータを取得します<br>
     * 配列にはleft, right, bottom, top, near, farの順番にデータが入っています */
    public float[] getCameraParameters(){
        return this.camera.getCameraParameters();
    }

    /** カメラの視錐台の頂点を取得します<br>
     * 順番は，near側の右上，左上，左下，右下，far側の右上，左上，左下，右下となっています<br>
     * 全て視点座標系です */
    public float[][] getCameraFrustum(){
        return this.camera.getCameraFrustum();
    }

    /** カメラの視錐台の重心座標を取得します<br>
     * 単に頂点座標の平均ですので，実際の重心よりは視点側に寄っています */
    public float[] getFrustumCenter(){
        return this.camera.getFrustumCenter();
    }

    /** カメラの視錐台の重心座標をワールド座標系で取得します */
    public float[] getFrustumCenterWorld(){
        return this.camera.getFrustumCenterWorld();
    }

    /** 視点パラメータを設定します<br>
     * 6要素の存在するfloat配列で指定してください */
    public void setCameraParameters(float[] params){
        this.setCameraParameters(params[0], params[1], params[2], params[3], params[4], params[5]);
    }

    /** 視点パラメータを設定します */
    public void setCameraParameters(float left, float right, float bottom, float top, float near, float far){
        this.camera.setCameraParameters(left, right, bottom, top, near, far);
    }


    /** 視点の位置を取得します<br>
     * 返り値の行列はクローンなので，それを直接操作しても視点位置は変更されません． */
    public float[] getCameraPosition(){
        return this.camera.getCameraPosition();
    }

    /** 視点の位置を設定します */
    public void setCameraPosition(float[] position){
        this.camera.setCameraPosition(position);
    }

    /** 注視点を取得します<br>
     * 返り値の行列はクローンなので，それを直接操作しても注視点は変更されません． */
    public float[] getCameraTarget(){
        return this.camera.getCameraTarget();
    }

    /** 注視点を設定します */
    public void setCameraTarget(float[] target){
        this.camera.setCameraTarget(target);
    }

    /** 画面の上方向ベクトルを取得します<BR>
     * 返り値の行列はクローンなので，それを直接操作しても画面の上方向ベクトルは変更されません． */
    public float[] getCameraUpper(){
        return this.camera.getCameraUpper();
    }

    /** 画面の上方向ベクトルを設定します */
    public void setCameraUpper(float[] upper){
        this.camera.setCameraUpper(upper);
    }

    /** 視点の位置と注視点，画面上での上をセットで設定します */
    public void setCameraPosition(float[] position,float[] target,float[] upper){
        this.camera.setCameraPosition(position, target, upper);
    }

    /** フルスクリーン表示を設定します */
    public void setFullScreen(DisplayMode mode){

    }

    /** フルスクリーン表示を設定します */
    public void setFullScreen(){

    }

    /** 背景の色を設定します */
    public void setBgColor(float[] color){
        bgColor=new float[4];

        for(int i=0;i<4;i++){
            if(i<color.length){
                bgColor[i]=color[i];
            }
        }
        isChangeBg=true;
    }

    /** 表示に利用するノードを追加します */
    public void addNode(Node3D model){
        rootNode.attach(model);
    }

    /** ノードを削除します */
    public void removeNode(Model3D model){
        rootNode.detach(model);
    }

    /** すべてのノードを削除します */
    public void removeAllNodes(){
        List<Node3D> removed=rootNode.getChildObjects();
        for (Node3D node:removed){
            this.rootNode.detach(node);
        }
    }

    /** すべてのノードを破棄します。<br>
     *  モデルは破壊され、再利用もできなくなります。 */
    public void destroyAllNodes(){
        for(Node3D model:rootNode.getChildObjects()){
            model.setDestroyFlag(true);
        }
    }

    /** 親が存在しなくなったリソースを追加します */
    public void trushGarbage(GPUResource garb){
        this.garbageLock.lock();
        try{
            this.garbage.add(garb);
        }finally{
            this.garbageLock.unlock();
        }
    }

    /** 後で描画するために半透明モデルを追加します<br>
     * ここでセットされた値は恒久的なものではなく，描画のたびにリフレッシュされます． */
    protected void addTransModel(Object3D model){
        this.transModels.add(model);
    }

    /** ライティング使用設定を確認します
     * ライティングを行わない場合，マテリアルの色が100%表示されます． */
    public boolean useLighting(){
        return this.lighting;
    }

    /** ライティングを実施するかどうかを決めます<br>
     * ライティングを行わない場合，マテリアルの色が100%表示されます． */
    public void setUseLighting(boolean flag){
        this.lighting=flag;
    }

    /** 光源オブジェクトを取得します */
    public List<LightObject> getLightObjects(){
        return this.lightList;
    }

    /** 光源オブジェクトを追加します */
    public void addLightObject(LightObject light){
        if (lightList.size()<this.numOfMaxLights){
            light.setEngine(this);
            this.lightList.add(light);
        }
    }

    /** 光源オブジェクトを削除します */
    public void removeLightObject(LightObject light){
        lightList.remove(light); // ライトを削除
    }

    /** ビュー変換行列を取得します<br>
     * この行列をワールド座標の左側に掛けるとカメラ座標系になります */
    public float[] getViewMatrix(){
        return this.camera.getViewMatrix();
    }

    /** 透視変換，ビュー変換行列を掛けた行列を取得します<br>
     * この行列をワールド座標の左側に掛けるとスクリーン座標系になります */
    public float[] getPVMatrix(){
        return this.camera.getPVMatrix();
    }

    /** 描画ループを開始します。*/
    public void start(int frameRate){
        if(this.animator!=null){
            this.animator.remove(drawable);
        }

        this.animator=new FPSAnimator(drawable,frameRate,true);
        this.animator.start();
    }

    /** 現在のフレームレートを取得します */
    public int getFrameRate(){
        return this.animator.getFPS();
    }

    /** 現在のフレームレートで描画ループを再開します */
    public void restart(){
        if(!this.animator.isAnimating()){
            this.animator.start();
        }
    }

    /** 描画ループを停止します */
    public void stop(){
        if(this.animator.isAnimating()){
            this.animator.stop();
        }
    }

    /** バージョンを取得します */
    public Version getVersion(){
        return version;
    }

    /** このエンジンが管理するモデル一覧を取得します<br>
     * 返り値のリストオブジェクト自体はクローンなので、変更してもかまいません．
     * ただし、リストの内容は本物を渡しているので、内容を変更した場合には本体に影響します． */
    public List<Object3D> getModels(){
        return new ArrayList<Object3D>(this.managedModels);
    }

    /** このエンジンが管理するモデルを追加します */
    protected void addModel(Model3D model){
        this.managedModels.add(model);
    }

    /** このエンジンが管理するモデルを削除します */
    protected void removeModel(Model3D model){
        this.managedModels.remove(model);
    }

    /** 描画領域の幅を取得します */
    public int getScreenWidth(){
        return this.screenWidth;
    }

    /** 描画領域の高さを取得します */
    public int getScreenHeight(){
        return this.screenHeight;
    }

    /** Zソートを利用したモデルリストの描画処理です<br>
     * 基本的に半透明モデルの描画に利用します．なお，このメソッドを実行後，引数は昇順でソートされています．*/
    public void drawModelsByZ(List<Object3D> targets,GL3 gl){
        Collections.sort(targets, new ZComparator());
        for(Object3D model:targets){
            model.drawObject(gl);
        }
    }

    /** 影を生成します<br>
     * 引数はシャドウバッファのIDです．<br>
     * シャドウバッファはカメラからの視点でレンダリング解像度と同じ解像度のテクスチャです．<br>
     * 画素は32bitフラグで表されており，nビット目が1であるなら，そのピクセルに対してn番目のライティングが有効であるという意味です． */
    public int createShadowBeffer(){
        for (LightObject light:this.lightList){
        }


        return -1;
    }

    /** 表示ループの処理です */
    @Override
    public void display(GLAutoDrawable glad) {

        // glオブジェクトを取得
        GL3 gl=(GL3)glad.getGL();

        // ガーベッジの処理を実施
        // 除去の過程でガーベッジが増えるため，ひとつずつ除去
        do{
            GPUResource garb=null;
            this.garbageLock.lock();
            try{
                if (this.garbage.size()>0){
                    GPUResource[] garbArray=new GPUResource[this.garbage.size()];
                    garbArray=this.garbage.toArray(garbArray);
                    garb=garbArray[0];
                }
            }finally{
                this.garbageLock.unlock();
            }
            if (garb!=null){
                garb.dispose(gl);
                this.garbage.remove(garb);
            }
        }while(!this.garbage.isEmpty());

        // OpenGL描画前のコールバック
        if (this.callBack!=null) {
            this.callBack.displayCall(glad);
            int error=gl.glGetError();
            if (error!=GL3.GL_NO_ERROR){
                logger.severe("Error has occured in preparing of rendering:"+error);
            }
        }

        if(this.isChangeBg){
            gl.glClearColor(bgColor[0],bgColor[1],bgColor[2],bgColor[3]); // 背景色
        }

        // 影の生成
        // まだTBD

        gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT); // 画面クリア
        gl.glEnable(GL.GL_CULL_FACE);              // 裏返ったポリゴンを描画しません

        // 変換行列を計算
        float[] pvMatrix=VectorManager.multMatrix4(this.getPerspectiveMatrix(), this.getViewMatrix());
        this.camera.setPVMatrix(pvMatrix);

        // モデルオブジェクトを描画
        this.rootNode.draw(gl);

        // 半透明オブジェクトを描画
        this.drawModelsByZ(this.transModels, gl);
        this.transModels.clear();

        // 描画後のコールバック
        if (this.callBack!=null) {
            this.callBack.displayFinish(glad);
        }

        int error=gl.glGetError();
        if (error!=GL3.GL_NO_ERROR){
            logger.severe("Error has occured in rendering:"+error);
        }
    }

    /** OpenGLの初期化処理です */
    @Override
    public void init(GLAutoDrawable glad) {
        GL3 gl=(GL3)glad.getGL();

        if (this.callBack!=null) {
            this.callBack.initCall(glad);
            int error=gl.glGetError();
            if (error!=GL3.GL_NO_ERROR){
                logger.severe("Error has occured in user's initializing of OpenGL:"+error);
            }
        }

        if(this.animator!=null){
            this.stop();
        }

        System.out.println("info:vendor :"+gl.glGetString(GL.GL_VENDOR));
        version=new Version(gl.glGetString(GL.GL_VERSION));
        System.out.println("info:version:"+version.getVersion()[0]+"."+version.getVersion()[1]+"."+version.getVersion()[2]);

        // エクステンションを獲得します
        extensions=gl.glGetString(GL.GL_EXTENSIONS);
        System.out.println("info:extensions :"+extensions);

        gl.glEnable(GL.GL_DEPTH_TEST);     //Zバッファを有効にします
        gl.glEnable(GL.GL_BLEND); // 半透明を有効にします

        gl.glClearColor(bgColor[0],bgColor[1],bgColor[2],bgColor[3]); // 背景色

        // 管理下のモデルにVRAMが破壊されていることを通知します
        for (Object3D model:this.managedModels){
            model.vramFlushed();
        }

        // オブジェクトを初期化します
        this.rootNode.init(gl, this);

        if (this.callBack!=null){
            this.callBack.initFinish(glad);
            int error=gl.glGetError();
            if (error!=GL3.GL_NO_ERROR){
                logger.severe("Error has occured in user's after initializing of OpenGL:"+error);
            }
        }

        if(this.animator!=null){
            this.restart();
        }
    }

    /** ウィンドウサイズ変更時の処理です */
    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int width,int height) {
        if (this.callBack!=null) {
            this.callBack.reshapeCall(glad);
        }
        this.aspect = (float)width / (float)height;

        GL3 gl=(GL3)glad.getGL();

        gl.glViewport(0, 0, width, height);        // 描画領域を指定します
        this.screenWidth=width;
        this.screenHeight=height;

        this.camera.refreshPerspectiveMatrix();
        if (this.callBack!=null) {
            this.callBack.reshapeFinish(glad);
        }
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        // TODO 自動生成されたメソッド・スタブ

    }
}

/** Zソート用クラス<br>
 * GraphicEngine以外では利用されません */
class ZComparator implements java.util.Comparator{
    public int compare(Object m1, Object  m2){
        int result=0;
        float value=((Object3D)m1).getWorldPosition()[2]-((Object3D)m2).getWorldPosition()[2];
        if (value<0){
            result=-1;
        }else if (value>0){
            result=1;
        }
        return result;
    }
}


