package k7system.gpuobjects;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;

/** GPUリソースを使うオブジェクトの共通機能です<br>
 * 主にリソース管理のための機能が搭載されています<br>
 * Common features for objects that use GPU resources */
public abstract class GPUResource{

    private Set<Object> parents=new TreeSet(new CompareParent()); // このオブジェクトの親一覧です
    private boolean isUploaded=false; // VRAMにアップロードされているかの判定フラグです
    private String name="no name";
    private GraphicEngine gEngine=null;

    /** このオブジェクトを初期化します<br>
     * もしグラフィックエンジンを利用しない場合，nullを入れてください．*/
    public int init(GL3 gl, GraphicEngine engine){
        this.gEngine=engine;
        return 0;
    }

    /** このオブジェクトの名前を取得します */
    public String getName(){
        return this.name;
    }

    /** このオブジェクトの名前を設定します */
    public void setName(String name){
        this.name=name;
    }

    /** このオブジェクトの所属するグラフィックエンジンを取得します<br>
     * まだ登録されていなければnullが返ります */
    public GraphicEngine getEngine(){
        return this.gEngine;
    }

    /** このオブジェクトがVRAMに載っているかどうかを取得します*/
    public boolean isUploaded(){
        return isUploaded;
    }

    /** このオブジェクトがVRAMに載っているかどうかのフラグを立てます */
    protected void enableUploadedFlag(){
        this.isUploaded=true;
    }

    /** このオブジェクトがVRAMに載っているかどうかのフラグを無効にします */
    protected void disableUploadedFlag(){
        this.isUploaded=false;
    }

    /** VRAMが外部から初期化されたことを通知します<br>
     * 通知を受けたオブジェクトは，自分のアップロード済みフラグをfalseにします．<br>
     * 実装は各オブジェクトに依存しますが，必ずそれを配下のオブジェクトに伝えなければなりません． */
    public abstract void vramFlushed();

    /** このオブジェクトの親を取得します<br>
     * 通常，VAOやマテリアルは使いまわされることから親も複数いるため，
     * 親はセットとして返されます */
    public Set<Object> getParents(){
        return this.parents;
    }

    /** このオブジェクトの親を追加します */
    public void addParent(Object parent){
        // 重複定義はしないようにする
        if (!this.parents.contains(parent)){
            this.parents.add(parent);
        }
    }

    /** このオブジェクトから親を削除します */
    public void removeParent(Object parent){
        this.parents.remove(parent);
        if (this.parents.isEmpty()){
            if (this.gEngine!=null){
                this.gEngine.trushGarbage(this);
            }
        }
    }

    /** モデルの後始末を行い，初期化前の状況に戻します<br>
     * 内容は各オブジェクトに依存しますが，このメソッドが呼ばれた場合には，
     * 必ずisUploadフラグをfalseにしなければなりません． */
    public abstract void dispose(GL3 gl);
}

/** どんなクラスであっても強引にソートするコンパレータ<br>
 * とにかく並び方を定義さえできればいいので，hashcodeを利用しています．*/
class CompareParent implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        return (o1.hashCode()-o2.hashCode());
    }

}
