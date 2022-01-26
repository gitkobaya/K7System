package k7system;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

/** スクリーンサイズを制御するクラスです */
public class ScreenManager {

    private GraphicsEnvironment gEnv;
    private GraphicsDevice gDevice;
    private DisplayMode[] dModes;
    private GraphicsConfiguration gConf;
    
    private int screenWidth=0,screenHeight=0,screenBitDepth=0; // 現在のスクリーンパラメータです
    
    private boolean isFullScreen=false; 
    
    private JFrame frame;
    
    /** コンストラクタです */
    public ScreenManager(JFrame frm) {
        frame=frm;
        
        // デバイスを取得します
        gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gDevice = gEnv.getDefaultScreenDevice();
        gConf = gDevice.getDefaultConfiguration();

        // 使用可能なディスプレイモードを取得します
        dModes = gDevice.getDisplayModes();
    }
    
    /** 現在フルスクリーンモードかどうかを問い合わせます */
    public boolean isFullScreen(){
        return isFullScreen;
    }
    
    /** 使用可能なディスプレイモードを返します */
    public DisplayMode[] getAviableDisplayModes(){
        return dModes;
    }
    
    /** 解像度を変更します */
    public void setResolution(ScreenType scr){
        int width=0,height=0,color=0;
        
        switch(scr){
        case QVGA16BIT:
            width=320;
            height=240;
            color=16;
            break;
        case QVGA32BIT:
            width=320;
            height=240;
            color=32;
            break;
        case VGA16BIT:
            width=640;
            height=480;
            color=16;
            break;
        case VGA32BIT:
            width=640;
            height=480;
            color=32;
            break;
        case SVGA16BIT:
            width=800;
            height=600;
            color=16;
            break;
        case SVGA32BIT:
            width=800;
            height=600;
            color=32;
            break;
        case XGA16BIT:
            width=1024;
            height=768;
            color=16;
            break;
        case XGA32BIT:
            width=1024;
            height=768;
            color=32;
            break;
        case SXGA16BIT:
            width=1280;
            height=1024;
            color=16;
            break;
        case SXGA32BIT:
            width=1280;
            height=1024;
            color=32;
            break;
        case SXGAPLUS16BIT:
            width=1400;
            height=1050;
            color=16;
            break;
        case SXGAPLUS32BIT:
            width=1400;
            height=1050;
            color=32;
            break;
        case UXGA16BIT:
            width=1600;
            height=1200;
            color=16;
            break;
        case UXGA32BIT:
            width=1600;
            height=1200;
            color=32;
            break;
        case WUXGA16BIT:
            width=1920;
            height=1200;
            color=16;
            break;
        case WUXGA32BIT:
            width=1920;
            height=1200;
            color=32;
            break;
        }
        setResolution(width, height, color);
    }
    
    public void setResolution(int width,int height,int colorBit){
        
        if(isFullScreen){
            DisplayMode mode = new DisplayMode(width, height, colorBit,DisplayMode.REFRESH_RATE_UNKNOWN);
            this.setDisplayMode(mode);
            System.out.println("debug:フルスクリーンモードで解像度を変更しました x:"+width+" y:"+height);
        }else{
            frame.setSize(width, height);
            frame.setVisible(true); // 大きさ変えてるのが見えてしまうけど、やむをえない
            frame.setVisible(false); // 大きさ変えてるのが見えてしまうけど、やむをえない
            
            // フレームの枠の分を修正
            int paneWidth=frame.getContentPane().getWidth();
            int paneHeight=frame.getContentPane().getHeight();

            int diffX=width-paneWidth;
            int diffY=height-paneHeight;
            
            if(diffX!=0 || diffY!=0){
                frame.setSize(width+diffX,height+diffY);
            }
            System.out.println("debug:ウィンドウモードで解像度を変更しました x:"+width+" y:"+height+" diffX:"+diffX+" diffY:"+diffY);
            frame.setVisible(true);

        }
        screenWidth=this.getWidth();
        screenHeight=this.getHeight();
        
        screenBitDepth=this.getBitDepth();
    }
    
    /** 画面の幅を取得するメソッドです */
    public int getWidth(){
        int result=0;
        if(isFullScreen){
            result=gDevice.getDisplayMode().getWidth();
        }else{
            result=frame.getWidth();
        }
        return result;
    }

    /** 画面の高さを取得するメソッドです */
    public int getHeight(){
        int result=0;
        if(isFullScreen){
            return gDevice.getDisplayMode().getHeight();
        }else{
            result=frame.getHeight();
        }
        return result;
    }

    /** 画面の色数を取得するメソッドです */
    public int getBitDepth(){
        int result=0;
        if(isFullScreen){
            return gDevice.getDisplayMode().getBitDepth();
        }else{
            result=0; // ウィンドウモードのときの返り値は不定です 
        }
        return result;
    }
    
    /** DisplayModeを直接指定するメソッドです */
    public void setDisplayMode(DisplayMode mode){
        gDevice.setDisplayMode(mode);
    }
    
    /** フルスクリーンに設定します */
    public void setFullScreen(DisplayMode mode){
        
        if (mode==null){
            mode=dModes[0];
        }
        
        frame.dispose();
        frame.setUndecorated(true);// タイトルバーを隠します
        gDevice.setFullScreenWindow(frame);
        this.setDisplayMode(mode);
        frame.setVisible(true);
        
        screenWidth=this.getWidth();
        screenHeight=this.getHeight();
        screenBitDepth=this.getBitDepth();
        
        isFullScreen=true;
    }

    
    
    /** ウィンドウ表示に設定します */
    public void setWindow(){
        frame.dispose();
        frame.setUndecorated(false);// タイトルバーを表示します
        gDevice.setFullScreenWindow(null);
        
        isFullScreen=false;
        this.setResolution(screenWidth, screenHeight, screenBitDepth);
    }

}
