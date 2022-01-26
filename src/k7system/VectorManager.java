package k7system;

/** ベクトルの計算関係を担当するクラスです */
public class VectorManager {

    /** ベクトルの足し算です */
    public static float[] add(float[] vec1,float[] vec2){
        float[] result=new float[3];
        result[0]=vec1[0]+vec2[0];
        result[1]=vec1[1]+vec2[1];
        result[2]=vec1[2]+vec2[2];
        return result;
    }

    /** ベクトルの引き算です<br>
     * vec1-vec2を計算します */
    public static float[] sub(float[] vec1,float[] vec2){
        float[] result=new float[3];
            result[0]=vec1[0]-vec2[0];
            result[1]=vec1[1]-vec2[1];
            result[2]=vec1[2]-vec2[2];
            return result;
    }

    /** ベクトルの外積です */
    public static float[] cross(float[] vec1,float[] vec2){
        float[] result=new float[3];
        result[0]=vec1[1]*vec2[2]-vec2[1]*vec1[2];
        result[1]=vec1[2]*vec2[0]-vec2[2]*vec1[0];
        result[2]=vec1[0]*vec2[1]-vec2[0]*vec1[1];
        return result;
    }

    /** ベクトルの内積です */
    public static float dot(float[] vec1, float[] vec2){
        return vec1[0]*vec2[0]+vec1[1]*vec2[1]+vec1[2]*vec2[2];
    }


    /** ベクトルの正規化です<br>
     * 3次元ベクトルを対象としているため，4次元目があっても無視されます */
    public static float[] normalize3(float[] vec){
        float abs=(float)Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]);
        float[] result=new float[3];
        try{
            result[0]=vec[0]/abs;
            result[1]=vec[1]/abs;
            result[2]=vec[2]/abs;
        }catch(Exception e){
            System.out.println("debug:正規化失敗by割り算");
        }
        return result;
    }

    /** 3次元ベクトルの正規化です */
    public static float[] normalize3(float x,float y,float z){
        float abs=(float)Math.sqrt(x*x+y*y+z*z);
        float[] result=new float[3];
        result[0]=x/abs;
        result[1]=y/abs;
        result[2]=z/abs;
        return result;
    }

    /** 数値とベクトルの掛け算です。<br>*/
    public static float[] multVectorValue(float[] vec,float value){
        float[] resultVector=new float[vec.length];

        for(int i=0;i<vec.length;i++){
            resultVector[i]=vec[i]*value;
        }

        return resultVector;
    }

    /** ベクトルと行列の掛け算です．<br>
     *  当然ですが，行列は正方行列であり，ベクトルの次元と一致している必要があります． */
    public static float[] multMatrixVector(float[] mat,float[] vec){
        int size=vec.length;
        float[] resultVector=new float[size];

        for(int i=0;i<size;i++){
            resultVector[i]=0;
            for(int j=0;j<size;j++){
                resultVector[i]+=vec[j]*mat[j*size+i];
            }
        }
        return resultVector;
    }

    /** 行列と行列の掛け算です */
    public static float[] multMatrix3(float[] mat1,float[] mat2){
        float[] resultMatrix=new float[9];
        for(int i=0;i<3;i++){
            resultMatrix[0+i*3]=mat1[0]*mat2[i*3]+mat1[3]*mat2[1+i*3]+mat1[6]*mat2[2+i*3];
            resultMatrix[1+i*3]=mat1[1]*mat2[i*3]+mat1[4]*mat2[1+i*3]+mat1[7]*mat2[2+i*3];
            resultMatrix[2+i*3]=mat1[2]*mat2[i*3]+mat1[5]*mat2[1+i*3]+mat1[8]*mat2[2+i*3];
        }
        return resultMatrix;
    }

    /** 行列とベクトルの掛け算です */
    public static float[] multMatrixVec3(float[] mat1,float[] vec){
        float[] resultMatrix=new float[3];
        resultMatrix[0]=mat1[0]*vec[0]+mat1[3]*vec[1]+mat1[6]*vec[2];
        resultMatrix[1]=mat1[1]*vec[0]+mat1[4]*vec[1]+mat1[7]*vec[2];
        resultMatrix[2]=mat1[2]*vec[0]+mat1[5]*vec[1]+mat1[8]*vec[2];
        return resultMatrix;
    }

    /** 行列と行列の掛け算です */
    public static float[] multMatrix4(float[] mat1,float[] mat2){
        float[] resultMatrix=new float[16];
        for(int i=0;i<4;i++){
            resultMatrix[0+i*4]=mat1[0]*mat2[i*4]+mat1[4]*mat2[1+i*4]+mat1[8]*mat2[2+i*4]+mat1[12]*mat2[3+i*4];
            resultMatrix[1+i*4]=mat1[1]*mat2[i*4]+mat1[5]*mat2[1+i*4]+mat1[9]*mat2[2+i*4]+mat1[13]*mat2[3+i*4];
            resultMatrix[2+i*4]=mat1[2]*mat2[i*4]+mat1[6]*mat2[1+i*4]+mat1[10]*mat2[2+i*4]+mat1[14]*mat2[3+i*4];
            resultMatrix[3+i*4]=mat1[3]*mat2[i*4]+mat1[7]*mat2[1+i*4]+mat1[11]*mat2[2+i*4]+mat1[15]*mat2[3+i*4];
        }
        return resultMatrix;
    }

    /** 行列とベクトルの掛け算です */
    public static float[] multMatrixVec4(float[] mat1,float[] vec){
        float[] resultMatrix=new float[4];
        resultMatrix[0]=mat1[0]*vec[0]+mat1[4]*vec[1]+mat1[8]*vec[2]+mat1[12]*vec[3];
        resultMatrix[1]=mat1[1]*vec[0]+mat1[5]*vec[1]+mat1[9]*vec[2]+mat1[13]*vec[3];
        resultMatrix[2]=mat1[2]*vec[0]+mat1[6]*vec[1]+mat1[10]*vec[2]+mat1[14]*vec[3];
        resultMatrix[3]=mat1[3]*vec[0]+mat1[7]*vec[1]+mat1[11]*vec[2]+mat1[15]*vec[3];
        return resultMatrix;
    }

    /** 3x3行列の逆行列を求めます */
    public static float[] getInverse3(float[] mat){
        float[] result=new float[9];
        double det=(double)mat[0]*mat[4]*mat[8]+(double)mat[1]*mat[5]*mat[6]+(double)mat[3]*mat[7]*mat[2]
               -(double)mat[0]*mat[5]*mat[7]-(double)mat[1]*mat[3]*mat[8] -(double)mat[2]*mat[4]*mat[6];
        result[0]=(float)(((double)mat[4]*mat[8]-(double)mat[7]*mat[5])/det);
        result[1]=(float)(((double)mat[7]*mat[2]-(double)mat[1]*mat[8])/det);
        result[2]=(float)(((double)mat[1]*mat[5]-(double)mat[4]*mat[2])/det);
        result[3]=(float)(((double)mat[6]*mat[5]-(double)mat[3]*mat[8])/det);
        result[4]=(float)(((double)mat[0]*mat[8]-(double)mat[6]*mat[2])/det);
        result[5]=(float)(((double)mat[3]*mat[2]-(double)mat[0]*mat[5])/det);
        result[6]=(float)(((double)mat[3]*mat[7]-(double)mat[6]*mat[4])/det);
        result[7]=(float)(((double)mat[6]*mat[1]-(double)mat[0]*mat[7])/det);
        result[8]=(float)(((double)mat[0]*mat[4]-(double)mat[3]*mat[1])/det);
        return result;
    }

    /** 行列の転置行列を求めます<br>
     * 正方行列であることが前提となります */
    public static float[] getTransposed(float[] mat){
        int elements=mat.length;
        float[] result=new float[elements];
        int size=(int)Math.sqrt(elements);

        for (int y=0;y<size;y++){
            for (int x=0;x<size;x++){
                result[y*size+x]=mat[y+x*size];
            }
        }
        return result;
    }

    /** 単位行列を生成します */
    public static float[] createIdentityMatrix(int size){
        float[] mat=new float[size*size];
        for(int i=0;i<mat.length;i++){
            if (i % (size+1)==0){
                mat[i]=1;
            }else{
                mat[i]=0;
            }
        }
        return mat;
    }
}
