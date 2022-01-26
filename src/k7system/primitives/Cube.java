package k7system.primitives;

import k7system.gpuobjects.VertexArrayObject;

/** 立方体形状です */
public class Cube extends VertexArrayObject{

    // モデルの座標
    private float points[] = {
       -0.5f,  0.5f,  0.5f,
       -0.5f, -0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f,  -0.5f,
        -0.5f, -0.5f,  -0.5f,
         0.5f, -0.5f,  -0.5f,
         0.5f,  0.5f,  -0.5f,

         -0.5f,  0.5f,  0.5f,
         -0.5f, -0.5f,  0.5f,
          0.5f, -0.5f,  0.5f,
          0.5f,  0.5f,  0.5f,
          -0.5f,  0.5f,  -0.5f,
          -0.5f, -0.5f,  -0.5f,
           0.5f, -0.5f,  -0.5f,
           0.5f,  0.5f,  -0.5f,

           -0.5f,  0.5f,  0.5f,
           -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f,  -0.5f,
            -0.5f, -0.5f,  -0.5f,
             0.5f, -0.5f,  -0.5f,
             0.5f,  0.5f,  -0.5f,
    };

    // 頂点インデックス
    private int index[]={
            0,1,2,  2,3,0,            // 正面
            6,5,4,  4,7,6,            // 裏面

            11,10,15,  10,14,15,            // 右面
            8,12,13,  8,13,9,            // 左面

            16,19,20,  19,23,20,            // 上面
            17,21,18,  18,21,22,            // 下面
    };

    /** コンストラクタで一辺の長さを指定します */
    public Cube(double size) {

        for(int i=0;i<this.points.length;i++){
            this.points[i]*=size;
        }
        this.setVertices(this.points);
        this.setIndices(this.index);
        this.createNormals();
    }
}
