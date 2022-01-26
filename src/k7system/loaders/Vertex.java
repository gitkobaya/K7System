package k7system.loaders;

/** 頂点クラスです */
class Vertex{
    private static final float EQUAL_TH=0.00001f;
    private int positionIndex;      // 頂点インデクスです。
    private float[] position;       // 頂点座標です。
    private float[] normal=new float[]{0,0,0};  // 法線ベクトルです。
    private float[] texCoord=new float[]{0,0};  // テクスチャ座標です。
    private float[] tangent=new float[]{0,0,0};  // 接線ベクトルです。
    private int materialId=0;   // マテリアルに何を利用するかです。

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    /** テクスチャ座標の取得です */
    public float[] getTexCoord() {
        return texCoord;
    }

    /** テクスチャ座標の設定です */
    public void setTexCoord(float[] texCoord) {
        this.texCoord = texCoord;
    }

    /** 頂点番号の取得です */
    public int getPositionIndex() {
        return positionIndex;
    }

    /** 頂点番号の設定です */
    public void setPositionIndex(int positionId) {
        this.positionIndex = positionId;
    }

    /** 頂点座標の取得です */
    public float[] getPosition() {
        return position;
    }

    /** 頂点座標の設定です */
    public void setPosition(float[] pos) {
        position=pos;
    }

    /** 法線ベクトルの取得です */
    public float[] getNormal() {
        return normal;
    }

    /** 法線ベクトルの設定です */
    public void setNormal(float[] normal) {
        this.normal = normal.clone();
    }

    /** 接線ベクトルの取得です */
    public float[] getTangent() {
        return this.tangent;
    }

    /** 接線ベクトルの設定です */
    public void setTangent(float[] tangent) {
        this.tangent = tangent.clone();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result=false;
        if (obj instanceof Vertex){
            Vertex target=(Vertex)obj;
            float diffPos=0;
            if (this.position!=null && target.position!=null){
                diffPos=
                        Math.abs(this.position[0]-target.position[0])+
                        Math.abs(this.position[1]-target.position[1])+
                        Math.abs(this.position[2]-target.position[2]);
                if (diffPos>EQUAL_TH){
                    return false;
                }
            }else return false;

            float diffNorm=0;
            if (this.normal!=null && target.normal!=null){
                diffNorm=
                        Math.abs(this.normal[0]-target.normal[0])+
                        Math.abs(this.normal[1]-target.normal[1])+
                        Math.abs(this.normal[2]-target.normal[2]);
            }
            if (Float.isNaN(diffNorm)){
                diffNorm=0; // ノーカウントにする
            }

            float diffTexC=0;
            if (this.texCoord!=null && target.texCoord!=null){
                diffTexC=
                        Math.abs(this.texCoord[0]-target.texCoord[0])+
                        Math.abs(this.texCoord[1]-target.texCoord[1]);
            }
            if (Float.isNaN(diffTexC)){
                diffTexC=0;
            }

            float diff=diffPos+diffNorm+diffTexC;
            if (diff<EQUAL_TH & this.materialId==target.materialId){
                result=true;
            }
        }
        return result;
    }
}
