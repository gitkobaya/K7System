package k7system.gpuobjects;

import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;
import k7system.VectorManager;

/** 深度値を出力するマテリアルです */
public class DepthMaterial extends Material{

    public static final int UNUSE=0;
    public static final int USE=1;

    // ユニフォーム変数関係
    private Uniform mvpMatrix;
    private Uniform mvMatrix;
    private Uniform rotationMatrix;

    // 頂点シェーダー
    private String[] vShader=new String[]{
            "#version 330 core\n"+
            "layout(location = "+VertexArrayObject.LOCATION_VERTEX_POSITION+") in vec3 vertex;\n"+
            "uniform mat4 "+MVP_MATRIX_NAME+";\n"+
            "uniform mat4 "+MV_MATRIX_NAME+";\n"+
            "uniform mat3 "+ROTATION_MATRIX_NAME+";\n"+
            "out vec4 vPosition;"+
            "void main(){\n"+
            "    gl_Position = "+MVP_MATRIX_NAME+"*vec4(vertex,1.0);\n"+ // 画面座標での位置
            "    vPosition=gl_Position;"+
            "}\n"
    };

    // フラグメントシェーダー
    private String[] fShader=new String[]{
            "#version 330 core\n"+
            "uniform mat4 "+MVP_MATRIX_NAME+";\n"+
            "uniform mat3 "+ROTATION_MATRIX_NAME+";\n"+
            "in vec4 vPosition;"+
            "out float depth;\n"+
            "void main(){\n"+
            "    depth=vPosition[2];\n"+ // Z値を出力する(-1~1に正規化されているはず)
            "}\n"
   };

    /** 初期化します */
    public DepthMaterial() {
        // シェーダーの設定
        Shader shader=new Shader();
        shader.setVertexShaderSource(vShader);
        shader.setFragmentShaderSource(fShader);
        this.setShader(shader);

        // ユニフォーム変数を登録
        // 座標変換行列関係の登録
        this.mvpMatrix=new Uniform(MVP_MATRIX_NAME,VectorManager.createIdentityMatrix(4)); // モデルビュープロジェクション変換行列ユニフォーム
        this.addUniform(this.mvpMatrix);
        this.mvMatrix=new Uniform(MV_MATRIX_NAME,VectorManager.createIdentityMatrix(4)); // モデルビュー変換行列ユニフォーム
        this.addUniform(this.mvMatrix);
        this.rotationMatrix=new Uniform(ROTATION_MATRIX_NAME,VectorManager.createIdentityMatrix(3)); // 回転行列ユニフォーム
        this.addUniform(this.rotationMatrix);

    }

    /** 初期化します<br>
     * 返り値は特に使用しないため，値は不定です */
    @Override
    public int init(GL3 gl, GraphicEngine eng) {
        super.init(gl,eng);
        this.getShader().setName("Shadow Shader");
        return 0;
    }

    @Override
    public void bind(GL3 gl) {
        super.bind(gl);
    }

    @Override
    public void vramFlushed() {
        super.vramFlushed();
    }

    @Override
    public void dispose(GL3 gl) {
        super.dispose(gl);
    }
}
