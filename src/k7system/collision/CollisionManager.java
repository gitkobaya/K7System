package k7system.collision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import k7system.Model3D;
import k7system.Node3D;

/** 衝突判定のための8分木空間を管理します */
public class CollisionManager {
    private Map<Long, CollisionSpace> octTree=new HashMap<Long, CollisionSpace>();
    private double minX=Double.MAX_VALUE;
    private double minY=Double.MAX_VALUE;
    private double minZ=Double.MAX_VALUE;
    private double maxX=Double.MIN_VALUE;
    private double maxY=Double.MIN_VALUE;
    private double maxZ=Double.MIN_VALUE;

    /** 木空間を構成します */
    public void createTreeSpace(Node3D rootNode){
        this.octTree.clear();

    }

    /** 木空間を構成します<br>
     * 衝突対象オブジェクトを登録してから実施してください */
    public void createTreeSpace(){

    }

    /** ノードを木空間に追加します */
    private void addCollisionObject(CollisionObject co){

    }

    /** 指定されたモデルと衝突可能性のあるモデルを返します */
    public List<CollisionObject> getCollisionCandidates(CollisionObject obj){
        return null;
    }

    /** 指定されたエリアと衝突可能性のあるモデルを返します */
    public List<CollisionObject> getCollisionCandidates(long id){
        return null;
    }
}


