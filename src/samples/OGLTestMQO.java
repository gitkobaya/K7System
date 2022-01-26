package samples;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.*;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import k7system.*;
import k7system.loaders.MQOLoader;

/** GraphicEngineを利用した描画 */
public class OGLTestMQO extends JFrame implements GameCallBack,MouseMotionListener,MouseListener, MouseWheelListener, DropTargetListener{
    private Logger logger=Logger.getGlobal();
    private GraphicEngine engine;
    private Model3D model1=null;
    private LightObject light;
    private int mouseX,mouseY,mDiffX,mDiffY;
    int mButton; // どのボタンを押しているか

    public static void main(String[] args){
        JFrame frame=new OGLTestMQO();
        frame.setVisible(true);
    }

    /** コンストラクタ */
    public OGLTestMQO() {
        this.logger.setLevel(Level.INFO);
        this.setSize(640, 480);

        GLJPanel panel=new GLJPanel();
        this.engine=new GraphicEngine(panel);
        this.engine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.engine.setCameraParameters(-1.0f, 1.0f, -1.0f, 1.0f, 2.0f, -1000f);
        this.engine.setCameraPosition(new float[]{0.0f,0.0f,400.0f}, new float[]{0f,0f,-1}, new float[]{0,1,0});
        this.engine.setBgColor(new float[]{0.3f,0.3f,0.3f,1.0f});
        this.engine.setAutoAspect(true);
        this.add(panel);

        // 天空にライトを作ってみる
        this.light=new LightObject();
        this.light.setLightPosition(new float[]{0,0,-1,0});
        this.light.setPower(new float[]{0.9f,0.9f,0.9f});
        this.light.enable();
        this.engine.addLightObject(this.light);

        this.engine.setGameCallBack(this); // コールバックを登録
        this.engine.start(60); // FPSを指定して描画開始

        //D&Dのドロップを可能にしておく
        DropTarget dtg=new DropTarget(this,DnDConstants.ACTION_COPY,this);

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
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
        if (this.model1!=null){
            this.model1.rotate(rot, 0 ,1 , 0);
            rot+=0.5;
        }
    }

    @Override
    public void displayFinish(GLAutoDrawable gla) {}

    @Override
    public void reshapeCall(GLAutoDrawable gla) {}

    @Override
    public void reshapeFinish(GLAutoDrawable gla) {}

    @Override
    public void graphicEngineIsSet(GraphicEngine engine) {}

    /** 現在押しているボタンを取得します */
    public int getPressingButton(){
        return this.mButton;
    }
    /** マウスドラッグの監視<br>
     * マウスドラッグは，カメラの座標とフォーカスを制御します．フォーカスとは，カメラがどこを見ているかです．
     * マウスドラッグにより，カメラをカメラ座標系で水平移動させることでフォーカスを移動させます．*/
    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX=e.getX();
        int currentY=e.getY();

        mDiffX=currentX-mouseX;
        mDiffY=currentY-mouseY;

        mouseX=currentX;
        mouseY=currentY;
        //System.out.println("DEBUG: currentX:"+currentX);

        // ドラッグの処理
        if (this.getPressingButton()==MouseEvent.BUTTON1){
        }

        if (this.getPressingButton()==MouseEvent.BUTTON2){
            float[] position=this.engine.getCameraPosition();
            position[1]+=mDiffY*Math.sqrt(position[0]*position[0]+position[2]*position[2])*0.005;
            this.engine.setCameraPosition(position);
            this.engine.setCameraTarget(new float[]{0,position[1],0});
            //System.out.println("DEBUG: marker dragged x:"+mDiffX+" y:"+mDiffY+" camera_Y:"+position[1]);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int currentX=e.getX();
        int currentY=e.getY();
        mDiffX=currentX-mouseX;
        mDiffY=currentY-mouseY;
        mouseX=currentX;
        mouseY=currentY;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO 自動生成されたメソッド・スタブ

    }

    /** マウスボタンが押されていた場合 */
    @Override
    public void mousePressed(MouseEvent e) {
        mButton=e.getButton();
    }

    /** マウスリリースの場合の処理 */
    @Override
    public void mouseReleased(MouseEvent e) {
        mButton=0;
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void dragEnter(DropTargetDragEvent dtde) { }

    @Override
    public void dragOver(DropTargetDragEvent dtde) { }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) { }

    @Override
    public void dragExit(DropTargetEvent dte) { }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int click=e.getWheelRotation();
        float[] params=this.engine.getCameraParameters();
        if (click>0){
            params[2]=params[2]*1.05f;
            params[3]=params[3]*1.05f;
        }else{
            params[2]=params[2]/1.05f;
            params[3]=params[3]/1.05f;
        }
        params[0]=params[2];
        params[1]=params[3];
        this.engine.setCameraParameters(params[0], params[1], params[2], params[3], params[4], params[5]);
    }

    /** D&Dの処理 */
    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        boolean flg = false;
        List<File> list=null;
        try {
            Transferable tr = dtde.getTransferable();

            if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                flg = true;
            } else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                list = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                flg = true;
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 転送官僚
            dtde.dropComplete(flg);
        }

        if (flg){
            engine.destroyAllNodes();
            this.model1=null;
            MQOLoader loader=new MQOLoader();
            this.model1=loader.loadModel(list.get(0));
            this.engine.addNode(this.model1);
        }
    }

}