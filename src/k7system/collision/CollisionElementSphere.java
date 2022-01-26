package k7system.collision;

/** 衝突判定モデルの基本モデルです<br>
 * 球で表現します */
public class CollisionElementSphere extends CollisionElement{
    private double[] position; // ローカル座標
    private double size; // 大きさ

    /** コンストラクタでモデル座標系での位置と球の大きさを設定 */
    public CollisionElementSphere(double size,double[] position) {
        this.size=size;
        this.position=position;
    }

    @Override
    public boolean colidesTo(CollisionElement target) {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public long computeId() {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }
}
