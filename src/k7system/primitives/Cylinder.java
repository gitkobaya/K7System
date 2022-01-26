package k7system.primitives;

import java.util.ArrayList;
import java.util.List;

import k7system.Model3D;
import k7system.gpuobjects.VertexArrayObject;

/** 円筒形状です */
public class Cylinder extends VertexArrayObject{

    /** 作成する円筒のパラメータを設定します
     * 標準状態では，円筒の天面及び底面はワールド座標のX-Y平面に平行で，円筒の重心がローカル座標の原点となります
     * @param radius 円筒の半径
     * @param hight 円筒の高さ
     * @param division 円筒の分割数です．必ず3以上の数字を指定してください*/
    public Cylinder(double radius, double hight, int division) {
        if (division<3){
            division=3;
        }
        this.getVAO(radius, hight, division);
    }

    /** シリンダーの形状を生成し，その頂点情報を取得します<br>
     * 分割数nの場合，0が天面中心，1~nが天面の頂点，n+1が底面中心，n+2~2n+1が底面の頂点となります*/
    private void getVAO(double radius, double hight, int division){
        float[] vertices=new float[(division*2+1)*2*3];
        List<Integer> indices=new ArrayList<Integer>();

        // 天面と底面の頂点を作成し、インデックスを作成する
        int count=0;
        for (int upDown=0;upDown<2;upDown++){
            double z;
            if (upDown==0){
                z=hight/2;
            }else{
                z=-hight/2;
            }
            vertices[count*3]=0;
            vertices[count*3+1]=0;
            vertices[count*3+2]=(float)z;
            count++;
            for(int i=0;i<division;i++){
                double rad=2*Math.PI*(((double)i)/division);
                double x=radius*Math.cos(rad);
                double y=radius*Math.sin(rad);
                vertices[count*3]=(float)x;
                vertices[count*3+1]=(float)y;
                vertices[count*3+2]=(float)z;

                if (i>0){
                    if (upDown==0){
                        indices.add(0);
                        indices.add(i);
                        indices.add(i+1);
                    }else{
                        indices.add(division+1);
                        indices.add(division+2+i);
                        indices.add(division+1+i);
                    }
                }
                count++;
            }
            if (upDown==0){
                indices.add(0);
                indices.add(division);
                indices.add(1);
            }else{
                indices.add(division+1);
                indices.add(division+2);
                indices.add(2*division+1);
            }
        }

        // 側面のインデックスを生成する
        for(int i=0;i<division-1;i++){
            indices.add(i+1);
            indices.add(i+division+2);
            indices.add(i+2);

            indices.add(i+2);
            indices.add(division+2+i);
            indices.add(division+3+i);
        }
        indices.add(1);
        indices.add(division+1+division);
        indices.add(division+2);

        indices.add(1);
        indices.add(division);
        indices.add(division+1+division);

        this.setVertices(vertices);
        this.setIndices(indices);
        this.createNormals();

    }
}
