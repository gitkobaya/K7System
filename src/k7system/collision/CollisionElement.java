package k7system.collision;

/** 衝突判定エレメント */
public abstract class CollisionElement {
    private float[] mvMatrix; // モデル座標をカメラ座標に変換する行列

    /** この衝突判定エレメントが所属しているコリジョンエリアのIDです */
    private long areaId;

    /** 衝突判定を実施します*/
    public abstract boolean colidesTo(CollisionElement target);

    /** MV行列を取得します */
    public float[] getMvMatrix(){
        return this.mvMatrix;
    }

    /** MV行列を設定します */
    public void setMvMatrix(float[] mv){
        this.mvMatrix=mv;
    }

    /** 衝突判定エリアのIDを取得します */
    public long getCollisionAreaId(){
        return this.areaId;
    }

    /** 衝突判定エリアのIDを設定します */
    private void setCollisionAreaId(long id){
        this.areaId=id;
    }

    /** 衝突判定エリアのIDを計算します */
    public abstract long computeId();
}
