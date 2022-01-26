package k7system.gpuobjects;

import java.lang.reflect.Array;
import java.util.ArrayList;

/** シェーダーに与えるユニフォーム変数クラスです<br>
 * 通常使用する範囲の型に対応しています */
public class Uniform {
    public static final int NULL=0x0000;
    public static final int BOOLEAN=0x0001;
    public static final int INTEGER=0x0002;
    public static final int FLOAT=0x0003;
    public static final int VECTOR_2=0x0011;
    public static final int VECTOR_3=0x0012;
    public static final int VECTOR_4=0x0013;
    public static final int MATRIX_3=0x0101;
    public static final int MATRIX_4=0x0102;
    public static final int VECTOR_2_ARRAY=0x1011;
    public static final int VECTOR_3_ARRAY=0x1012;
    public static final int VECTOR_4_ARRAY=0x1013;
    public static final int MATRIX_3_ARRAY=0x1101;
    public static final int MATRIX_4_ARRAY=0x1102;
    public static final int ILLEGAL_TYPE=0xffff;

    private String name;
    private Object value;
    private int type=Uniform.NULL;
    private int elemNum=1;

    /** コンストラクタで名前だけを設定し，後から値を設定する */
    public Uniform(String name) {
        this.name=name;
    }

    /** コンストラクタで名前と値を設定する */
    public Uniform(String name, Object value) {
        this.name=name;
        this.setValue(value);
    }

    /** ユニフォーム名を取得します */
    public String getName(){
        return this.name;
    }

    /** 値を取得します<br>
     * cloneしないでオブジェクトそのものを渡しているため，ここで得られた値を変更するとユニフォームクラスの値自体を変更することになります． */
    public Object getValue(){
        return this.value;
    }

    /** 値がベクトルや行列の配列だった場合に，1次元配列の形で値を取得します<br>
     * これは，OpenGLのAPIが二次元配列を扱うことができないからです．値が配列の場合のみ有効です<br>
     * 返り値の型はデータ型に依存するため，Object型となっています．それぞれのプリミティブ型の配列にキャストして利用してください．<br>
     * 現在，浮動小数点型にしか対応していません． */
    public Object getArrayValue(){
        ArrayList<Object> list=new ArrayList<Object>();
        Object result=null;
        if (this.type==VECTOR_2_ARRAY || this.type==VECTOR_3_ARRAY || this.type==VECTOR_4_ARRAY || this.type==MATRIX_3_ARRAY || this.type==MATRIX_4_ARRAY){
            int length=Array.getLength(this.value); // 配列の長さです
            this.elemNum=length;
            for (int i=0;i<length;i++){
                float[] array=(float[])Array.get(this.value, i);
                for (float element:array){
                    list.add(element);
                }
            }
            result=new float[list.size()];
            for(int i=0;i<list.size();i++){
                ((float[])result)[i]=(Float)list.get(i);
            }
        }
        return result;
    }

    /** 値を設定します<br>
     * 配列を設定する場合，必ずプリミティブ型の配列にしてください． */
    public void setValue(Object value){
        this.value=value;
        this.type=this.checkType(this.value);
    }

    /** 値が配列だった場合，要素数を取得します */
    public int getNumOfElements(){
        return this.elemNum;
    }

    /** 値の型を取得します */
    public int getType(){
        return this.type;
    }

    /** オブジェクトの型を取得します */
    public int checkType(Object val){
        int result=Uniform.ILLEGAL_TYPE;
        if (val!=null){
            Class<?> thisClass=val.getClass();
            if (thisClass.isArray()){
                Class<?> compType=thisClass.getComponentType();
                if(compType.equals(float.class)){
                    int length=Array.getLength(val);
                    if (length==16){
                        result=Uniform.MATRIX_4;
                    }else if(length==9){
                        result=Uniform.MATRIX_3;
                    }else if(length==4){
                        result=Uniform.VECTOR_4;
                    }else if(length==3){
                        result=Uniform.VECTOR_3;
                    }else if(length==2){
                        result=Uniform.VECTOR_2;
                    }
                }else if(compType.isArray()){ // 中身がまた配列だった場合
                    Class<?> compType2=compType.getComponentType();
                    if(compType2.equals(float.class)){
                        Object value2=Array.get(val,0); // 0番目の要素を取得
                        int length=Array.getLength(value2);
                        if (length==16){
                            result=Uniform.MATRIX_4_ARRAY;
                        }else if(length==9){
                            result=Uniform.MATRIX_3_ARRAY;
                        }else if(length==4){
                            result=Uniform.VECTOR_4_ARRAY;
                        }else if(length==3){
                            result=Uniform.VECTOR_3_ARRAY;
                        }else if(length==2){
                            result=Uniform.VECTOR_2_ARRAY;
                        }
                    }
                }
            }else if (thisClass.equals(boolean.class)||thisClass.equals(Boolean.class)){
                result=Uniform.BOOLEAN;
            }else if (thisClass.equals(int.class)||thisClass.equals(Integer.class)){
                result=Uniform.INTEGER;
            }else if(thisClass.equals(float.class)||thisClass.equals(Float.class)){
                result=Uniform.FLOAT;
            }
        }else{
            result=Uniform.NULL;
        }
        return result;
    }

}
