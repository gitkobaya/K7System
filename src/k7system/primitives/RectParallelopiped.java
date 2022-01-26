package k7system.primitives;

import k7system.gpuobjects.VertexArrayObject;

/** 直方体形状です */
public class RectParallelopiped extends VertexArrayObject{

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

    /** コンストラクタでX,Y,Z方向の長さを指定します */
    public RectParallelopiped(double xSize, double ySize, double zSize) {

        for(int i=0;i<this.points.length;i+=3){
            this.points[i]*=xSize;
            this.points[i+1]*=ySize;
            this.points[i+2]*=zSize;
        }
        this.setVertices(this.points);
        this.setIndices(this.index);
        this.createNormals();
    }
}
