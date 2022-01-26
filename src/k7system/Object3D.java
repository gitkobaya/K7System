package k7system;

import com.jogamp.opengl.GL3;

/** 3次元空間上の物体です<br>
 * サブクラスとしてモデルとビルボードがあります */
public abstract class Object3D extends Node3D{
    private boolean useLight=true; // 光源計算を行うかどうかのフラグ
    private boolean shadowTarget=true; // 影を作る対象かどうかのフラグ
    private BlendType transparentType=BlendType.NOT;

    /** このモデルの半透明属性を取得します */
    public BlendType getTransparent(){
        return this.transparentType;
    }

    /** このモデルの半透明属性を設定します<br>
     * マテリアルの半透明情報は，ここでフラグを設定されて初めて有効になります． */
    public void setTransparent(BlendType transType){
        this.transparentType=transType;
    }

    /** ライティングの対象かどうかを取得します */
    public boolean isUseLight(){
        return this.useLight;
    }

    /** このモデルをライティングの対象にするかどうかを指定します */
    public void useLight(boolean isUse){
        this.useLight=isUse;
    }

    /** このモデルが影を落とすオブジェクトかどうかを取得します */
    public boolean isShadowable(){
        return this.shadowTarget;
    }

    /** このモデルが影を落とすかどうかを設定します */
    public void setShadowableFlag(boolean shadowFlag){
        this.shadowTarget=shadowFlag;
    }

    /** ノードの描画処理です<br>
     * 不透明なモデルに関しては，この時点で描画してしまいます． */
    @Override
    public void draw(GL3 gl){
        super.draw(gl);
        if (this.transparentType==BlendType.NOT){
            this.drawObject(gl);
        }else{
            this.getEngine().addTransModel(this);
        }
        super.draw(gl); // 子モデルの描画
    }

    public abstract void drawObject(GL3 gl);
}
