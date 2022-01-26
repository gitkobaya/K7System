package k7system.collision;

import java.util.List;

/** 衝突対象オブジェクトのインターフェースです */
public interface CollisionObject {
    /** オブジェクトのワールド座標を配列で取得します<br>
     * 返り値は{x, y, z}となります */
    public float[] getPositionByArray();

    /** オブジェクトのワールド座標でのAABBバウンディングを配列で取得します<br>
     * 返り値は{minX, minY, minZ, maxX, maxY, maxZ}となります */
    public float[] getBoundByArray();

    /** このオブジェクトの衝突判定エレメントを取得します<br>
     * NULLが返ってきた場合，衝突判定を実施しません */
    public List<CollisionElement> getCollisionElements();

}
