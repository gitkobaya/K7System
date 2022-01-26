package k7system;

import com.jogamp.opengl.GLAutoDrawable;


/** OpenGLから呼び出される処理です<br>
 * K7を実装する場合，呼び出し元のルーチンはGameCallBackを実装することができます．<br>
 * GameCallBackを登録することで，ゲーム側でJOGLイベントを取得することができます． */
public interface GameCallBack {

    /** GraphicEngineへの登録直後にこのメソッドがコールバックされます */
    public void graphicEngineIsSet(GraphicEngine engine);

    /** GL.init()メソッドの呼び出し直後にこのメソッドがコールバックされます */
    public void initCall(GLAutoDrawable gla);

    /** GL.init()メソッドの呼び出し終了時にこのメソッドがコールバックされます */
    public void initFinish(GLAutoDrawable gla);


    /** GL.display()メソッドの呼び出し直後にこのメソッドがコールバックされます */
    public void displayCall(GLAutoDrawable gla);

    /** GL.display()メソッドの終了時にこのメソッドがコールバックされます */
    public void displayFinish(GLAutoDrawable gla);

    /** GL.reshape()メソッドの呼び出し直後にこのメソッドがコールバックされます */
    public void reshapeCall(GLAutoDrawable gla);

    /** GL.reshape()メソッドの呼び出し終了時にこのメソッドがコールバックされます */
    public void reshapeFinish(GLAutoDrawable gla);
}
