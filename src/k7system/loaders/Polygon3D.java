package k7system.loaders;

/** ポリゴン整理用のクラスです */
public class Polygon3D {
    int materialID;
    float[] normal=new float[3]; // 法線ベクトル
    float[] tangent=new float[3]; // 接線ベクトル
    int[] index; // 頂点へのインデックス
    float[][] uv;  // UVへのインデックス

    @Override
    public Polygon3D clone(){
        Polygon3D clone=new Polygon3D();
        clone.materialID=materialID;
        clone.normal=normal.clone();
        clone.tangent=tangent.clone();
        clone.index=index.clone();
        if(uv!=null){
            clone.uv=new float[uv.length][];
            for(int i=0;i<uv.length;i++){
                clone.uv[i]=uv[i].clone();
            }
        }

        return clone;
    }
}
