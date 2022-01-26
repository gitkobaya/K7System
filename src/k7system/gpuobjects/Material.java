package k7system.gpuobjects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;

/** テクスチャ、材質、色等のマテリアルを設定します */
public abstract class Material extends GPUResource{

    public static final int COLOR=0x1; // 色情報を有するマテリアルか 0001
    public static final int TEXTURE=0x2; // テクスチャ情報を有するマテリアルか 0010
    public static final int ALPHA=0x4; // 半透明設定されているマテリアルか 0100

    /** このマテリアルが利用するuniform変数の名前を定数として定義します */
    public static final String MVP_MATRIX_NAME="mvpMatrix";
    public static final String MV_MATRIX_NAME="viewMatrix";
    public static final String ROTATION_MATRIX_NAME="rotationMatrix";
    public static final String APPEARANCE_MATRIX_NAME="appearanceMatrix";

    public Shader shader; // このマテリアルが利用するシェーダークラス

    /** 標準では白で全ての反射率が1となります */
    public static final float[] DEFAULT_APPEARANCE=new float[]{
        1.0f,1.0f,1.0f,1.0f, // Diffuse色(いわゆる物体の色)
        1.0f,1.0f,1.0f,1.0f, // Specular色(これをいじると金属っぽい色になる)
        1.0f,1.0f,1.0f,1.0f, // Ambient色
        0.0f,0.0f,0.0f,1.0f, // Emission色(放射光)
    };

    /** マテリアルタイプをビット列で表現していますが，今はメモ程度にしか使われていません */
    private int materialType=Material.COLOR;

    private Map<String, Uniform> uniforms=new HashMap<String,Uniform>();

    private static Logger logger = Logger.getGlobal();

    private boolean isUploadMaterial=false; // このマテリアル自身の要素がVRAMに載っているか

    /** このマテリアルで利用するユニフォーム変数をまとめて設定します */
    public void setUniforms(Collection<Uniform> uniformList){
        for(Uniform uni:uniformList){
            this.uniforms.put(uni.getName(), uni);
        }
    }

    /** このマテリアルにユニフォーム変数を追加します */
    public void addUniform(Uniform uni){
        this.uniforms.put(uni.getName(),uni);
    }

    /** このマテリアルに設定されたユニフォーム変数を名前を指定して取得します */
    public Uniform getUniform(String name){
        return this.uniforms.get(name);
    }

    /** このマテリアルに設定されたユニフォーム変数をまとめて取得します */
    public Collection<Uniform> getUniforms(){
        return this.uniforms.values();
    }

    /** マテリアルのシェーダーオブジェクトを取得します */
    public Shader getShader(){
        return shader;
    }

    /** マテリアルにシェーダーオブジェクトを設定します */
    public void setShader(Shader shader){
        this.shader=shader;
    }

    /** マテリアルタイプを取得します */
    public int getMaterialType(){
        return this.materialType;
    }

    /** マテリアルが半透明かどうかを取得します */
    public boolean isTransparent(){
        boolean result=false;
        if ((this.materialType | ALPHA)!=0){
            result=true;
        }
        return result;
    }

    /** マテリアルに半透明属性を設定します */
    public void setTransparent(boolean flag){
        if (flag){
            this.materialType=this.materialType | ALPHA;
        }else{
            this.materialType=this.materialType & ~ALPHA;
        }
    }

    /** VRAMフラッシュを通知します */
    @Override
    public void vramFlushed(){
        if (this.isUploaded()){
            this.disableUploadedFlag();
        }
        if (this.shader.isUploaded()){
            this.shader.vramFlushed();
        }
    }

    /** このマテリアルの要素全体がVRAMに載っているかを取得します */
    @Override
    public boolean isUploaded(){
        return (this.isUploadMaterial & this.shader.isUploaded());
    }

    /** このマテリアルのアップロード済みフラグをfalseにします */
    @Override
    protected void disableUploadedFlag(){
        this.isUploadMaterial=false;
    }

    /** マテリアルを初期化します<br>
     * 既にこのマテリアルがVRAMに載っている場合には何もしません． */
    public int init(GL3 gl, GraphicEngine eng){

        // 自分がVRAMに載っていなかった場合
        if (!this.isUploadMaterial){
            super.init(gl, eng);
            this.isUploadMaterial=true;
            System.out.println("DEBUG: Material "+this.getName()+" has been initialized !");
        }

        // シェーダーがVRAMに載っていなかった場合
        if (!this.shader.isUploaded()){
            // シェーダーをコンパイルします
            this.shader.init(gl,eng);
            int error=gl.glGetError();
            if (error!=GL.GL_NO_ERROR){
                logger.severe("Fail to compile a shader <"+this.shader+":"+this.shader.getProgramHandle()+"> :"+error);
            }
            this.enableUploadedFlag();
            this.shader.addParent(this);
        }
        return 0;
    }

    /** ビュー行列を設定します */
    public void setMvMatrix(float[] matrix){
        Uniform uni=this.getUniform(MV_MATRIX_NAME);
        uni.setValue(matrix);
    }

    /** ビュー行列を取得します */
    public float[] getMvMatrix(){
        Uniform uni=this.getUniform(MV_MATRIX_NAME);
        return (float[])uni.getValue();
    }

    /** MVP変換行列を取得します */
    public float[] getMvpMatrix(){
        Uniform uni=this.getUniform(MVP_MATRIX_NAME);
        return (float[])uni.getValue();
    }

    /** MVP変換行列を設定します */
    public void setMvpMatrix(float[] matrix){
        Uniform uni=this.getUniform(MVP_MATRIX_NAME);
        uni.setValue(matrix);
    }

    /** 回転行列を取得します */
    public float[] getRotationMatrix(){
        Uniform uni=this.getUniform(ROTATION_MATRIX_NAME);
        return (float[])uni.getValue();
    }

    /** 変換行列を設定します */
    public void setRotationMatrix(float[] matrix){
        Uniform uni=this.getUniform(ROTATION_MATRIX_NAME);
        uni.setValue(matrix);
    }

    /** このマテリアルが適用される頂点パッケージの座標を取得します<br>
     * 返り値はfloat[3]となります */
    public float[] getPosition(){
        float[] mat=this.getMvpMatrix();
        return new float[]{mat[12],mat[13],mat[14]};
    }

    /** このマテリアルが適用される頂点パッケージの座標を設定します */
    public void setPosition(float[] coord){
        if (coord.length==2){
            this.setPosition(coord[0],coord[1],0);
        }else{
            this.setPosition(coord[0],coord[1],coord[2]);
        }
    }

    /** このマテリアルが適用される頂点パッケージの座標を設定します */
    public void setPosition(float x, float y, float z){
        float[] mat=this.getMvpMatrix();
        mat[12]=x;
        mat[13]=y;
        mat[14]=z;
        Uniform uni=this.getUniform(MVP_MATRIX_NAME);
        uni.setValue(mat);
    }


    /** マテリアルをバインドします<br>
     * VAOを描画する前にこのメソッドを呼ぶと，そのVAOの描画にこのマテリアルが使われます */
    public void bind(GL3 gl){
        // これから使うシェーダーを設定
        int pHandle=this.shader.getProgramHandle();
        gl.glUseProgram(pHandle);
        int error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            String errorMess="";
            if (pHandle==-1){
                errorMess="Shader <"+this.shader.getName()+"> is not initialized";
            }
            logger.severe("Failed to bind shader <"+this.shader+":"+this.shader.getProgramHandle()+"> :"+error+"\n "+errorMess);
        }

        // マテリアルユニフォームをシェーダーに設定
        for(Uniform uni:this.uniforms.values()){
            this.setUniform(uni, gl);
        }
    }

    /** マテリアルのバインドを解除します<br>
     * VAOの描画が終わった後はこのメソッドを呼ぶことを推奨します． */
    public void unbind(GL3 gl){
        gl.glUseProgram(0);
        int error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Fail to unbind shader <"+this.shader+":"+this.shader.getProgramHandle()+"> :"+error);
        }
    }

    /** シェーダーにユニフォーム変数を設定します */
    protected void setUniform(Uniform uni, GL3 gl){
        int location=gl.glGetUniformLocation(this.shader.getProgramHandle(), uni.getName()); // 場所を取得
        int error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Can not get uniform location of <"+uni.getName()+"> on shader <"+this.shader+":"+this.shader.getProgramHandle()+"> :"+error);
        }
        int type=uni.getType();
        if (type==Uniform.MATRIX_4){
            gl.glUniformMatrix4fv(location, 1, false,(float[])uni.getValue() , 0);
        }else if (type==Uniform.MATRIX_3){
            gl.glUniformMatrix3fv(location, 1, false,(float[])uni.getValue() , 0);
        }else if (type==Uniform.VECTOR_4){
            gl.glUniform4fv(location, 1, (float[])uni.getValue() , 0);
        }else if (type==Uniform.VECTOR_3){
            gl.glUniform3fv(location, 1, (float[])uni.getValue() , 0);
        }else if (type==Uniform.VECTOR_2){
            gl.glUniform2fv(location, 1, (float[])uni.getValue() , 0);
        }else if (type==Uniform.FLOAT){
            float value=(Float)uni.getValue();
            gl.glUniform1f(location, value);
        }else if (type==Uniform.INTEGER){
            gl.glUniform1i(location, (Integer)uni.getValue());
        }else if (type==Uniform.MATRIX_4_ARRAY){
            float[] value=(float[])uni.getArrayValue() ;
            gl.glUniformMatrix4fv(location, uni.getNumOfElements(), false,value, 0);
        }else if (type==Uniform.BOOLEAN){
            int value=0;
            if ((Boolean)uni.getValue()){
                value=1;
            }
            gl.glUniform1i(location, value);
        }
        error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Can not set uniform <"+uni.getName()+":"+uni.getType()+"> for shader <"+this.shader+":"+this.shader.getProgramHandle()+"> :"+error);
        }
    }

    /** このマテリアルの構成要素をVRAMから除去します */
    @Override
    public void dispose(GL3 gl){
        // シェーダーの後始末
        this.shader.removeParent(this);
        this.disableUploadedFlag();
        System.out.println("DEBUG: material is disposed !");
    }
}
