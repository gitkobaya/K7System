package k7system.gpuobjects;

import java.awt.image.BufferedImage;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.Billboard;
import k7system.GraphicEngine;
import k7system.VectorManager;

/** ビルボード用のマテリアルです<br>
 * ビルボードはカメラに対してカメラ座標系ではZ座標でしか回転しません．<br>
 * また，光源の影響を受けません． */
public class BillboardMaterial extends Material{

    // スクリーンとテクスチャのサイズ(Sc横ピクセル, Sc縦ピクセル, Tx横ピクセル, Tx縦ピクセル)
    public static final String SCREEN_AND_TEX_SIZE="screenTexSize";

    // テクスチャ関係
    public static final String USE_DIFFUSE_TEXTURE_NAME="isUseDiffeseTexture";
    public static final String DIFFUSE_TEXTURE_UNIT_NAME="diffeseTextureUnit";
    public static final String SCREEN_SIZE="screenSize";
    // 座標系
    public static final String COORDINATE_SYSTEM="cooSystem";
    public static final String POSITION="myPositionVector";

    // 反転用
    public static final String MIRROR_Y="mirrorY";

    // ユニフォーム変数関係
    private Uniform mvpMatrix;
    private Uniform appMatrix;    // アピアランスの行列です．4x4行列であり，それぞれ(Diffuse, Specular, Ambient, Emission)を表します．
    private Uniform textureUnit; // テクスチャユニットです
    private Uniform coordSystem; // 座標系です
    private Uniform mirrorY; // Y軸反転用フラグです
    private float scale=1.0f;

    private TextureK7 texture=new TextureK7(new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR)); // デフォルトである程度の大きさを設定
    private float[] screenCoord=new float[3]; // スクリーン座標です

    // 頂点シェーダー
    private String[] vShader=new String[]{
            "#version 330 core\n"+
                    "layout(location = "+VertexArrayObject.LOCATION_VERTEX_POSITION+") in vec3 vertex;\n"+
                    "layout(location = "+VertexArrayObject.LOCATION_TEX_COORDS+") in vec2 texCoord;\n"+
                    "uniform mat4 "+MVP_MATRIX_NAME+";\n"+
                    "uniform vec4 "+SCREEN_AND_TEX_SIZE+";\n"+
                    "uniform float "+SCREEN_SIZE+";\n"+
                    "uniform int "+COORDINATE_SYSTEM+";\n"+
                    "uniform vec3 "+POSITION+";\n"+
                    "out vec2 texCoordPixel;\n"+
                    "void main(){\n"+
                    "    vec4 sizes="+SCREEN_AND_TEX_SIZE+";\n"+
                    "    texCoordPixel=texCoord;\n"+ // テクスチャ座標
                    "    if ("+COORDINATE_SYSTEM+"=="+Billboard.WORLD_COORDINATES+"){"+
                    "        vec4 center="+MVP_MATRIX_NAME+"*vec4(0.0 ,0.0 ,0.0, 1.0);\n"+
                    "        vec4 vtxs=vec4(vertex[0]*center[3]/sizes[0]*sizes[2]*2, vertex[1]*center[3]/sizes[1]*sizes[3]*2, vertex[2]*center[3], 0.0);"+ // 画面サイズは-1から1までの2
                    "        gl_Position = center+vtxs;\n"+ // 画面座標での位置
                    "    }"+
                    "    if ("+COORDINATE_SYSTEM+"=="+Billboard.SCREEN_COORDINATES+"){"+
                    "        vec3 pos="+POSITION+";"+
                    "        vec4 vtxs=vec4(pos[0]+vertex[0]/sizes[0]*sizes[2]*2, pos[1]+vertex[1]/sizes[1]*sizes[3]*2,  pos[2], 1.0);"+
                    "        gl_Position = vtxs;\n"+ // 画面座標での位置
                    "    }"+
                    "}\n"
    };

    // フラグメントシェーダー
    private String[] fShader=new String[]{
            "#version 330 core\n"+
                    "uniform mat4 "+APPEARANCE_MATRIX_NAME+";\n"+
                    "uniform int "+USE_DIFFUSE_TEXTURE_NAME+";\n"+
                    "uniform int "+MIRROR_Y+";\n"+
                    "uniform sampler2D  "+DIFFUSE_TEXTURE_UNIT_NAME+";\n"+
                    "in vec2 texCoordPixel;\n"+
                    "out vec4 finalcolor;\n"+
                    "void main(){\n"+
                    "    vec2 temp=texCoordPixel;"+
                    "    if ("+MIRROR_Y+"!=0){"+
                    "        temp=vec2(texCoordPixel[0], 1.0-texCoordPixel[1]);"+
                    "    }"+
                    "    finalcolor=texture2D("+DIFFUSE_TEXTURE_UNIT_NAME+",temp);\n"+
                    "}\n"
    };

    public BillboardMaterial() {
        // シェーダーの設定
        Shader shader=new Shader();
        shader.setVertexShaderSource(vShader);
        shader.setFragmentShaderSource(fShader);
        this.setShader(shader);

        // ユニフォーム変数を登録
        // 座標変換行列関係の登録
        this.mvpMatrix=new Uniform(MVP_MATRIX_NAME,VectorManager.createIdentityMatrix(4)); // モデルビュープロジェクション変換行列ユニフォーム
        this.addUniform(this.mvpMatrix);

        // 座標系変数の登録
        this.coordSystem=new Uniform(COORDINATE_SYSTEM, Billboard.WORLD_COORDINATES); // デフォルトは世界座標系
        this.addUniform(this.coordSystem);

        // アピアランス行列の登録
        this.appMatrix=new Uniform(APPEARANCE_MATRIX_NAME,DEFAULT_APPEARANCE.clone()); // 質感行列ユニフォーム
        this.addUniform(this.appMatrix);

        // テクスチャ利用関係変数の登録
        this.textureUnit=new Uniform(DIFFUSE_TEXTURE_UNIT_NAME,0); // テクスチャユニット番号を登録する
        this.addUniform(this.textureUnit);

        // Y軸反転変数の登録
        this.mirrorY=new Uniform(MIRROR_Y,0);
        this.addUniform(this.mirrorY);

        Uniform myPos=new Uniform(POSITION, this.getPosition());
        this.addUniform(myPos);
    }

    /** テクスチャを取得します */
    public TextureK7 getTexture(){
        return this.texture;
    }

    /** テクスチャを設定します<br>
     * テクスチャを変更した場合，以前のテクスチャを捨てる必要があります */
    public void setTexture(TextureK7 tex){
        // 従来テクスチャの削除処理
        if (this.texture!=null && this.texture!=tex){
            this.texture.removeParent(this);
        }

        // テクスチャの更新処理
        if (tex!=null){
            tex.addParent(this);
            this.texture=tex;
        }
    }

    /** このビルボードの拡大率を設定します */
    public void setScale(float scale){
        this.scale=scale;
    }

    /** このビルボードの座標系を取得します */
    public int getCoordinatesSystem(){
        return (Integer)this.coordSystem.getValue();
    }

    /** このビルボードの座標系を設定します<br>
     * 世界座標系かスクリーン座標系を指定します */
    public void setCoordinatesSystem(int WorldOrScreen){
        this.coordSystem.setValue(WorldOrScreen);
    }

    /** ビルボードのスクリーン座標を設定します<br>
     * ビルボードの座標系がスクリーン座標系の場合，これが利用されます */
    public void setScreenCoord(float x, float y, float z){
        this.screenCoord=new float[]{x,y,z};
    }

    public void setScreenCoord(float[] position){
        this.screenCoord=new float[]{position[0],position[1],position[2]};
    }

    /** Y軸反転属性を設定します */
    public void setMirrorY(boolean flag){
        if (flag){
            this.mirrorY.setValue(1);
        }else{
            this.mirrorY.setValue(0);
        }
    }


    /** 初期化します<br>
     * 返り値は特に使用しないため，値は不定です */
    @Override
    public int init(GL3 gl, GraphicEngine eng) {
        super.init(gl,eng);

        // テクスチャの処理
        if (this.texture!=null){
            this.texture.init(gl,eng);
        }

        // エンジン情報が必要なユニフォーム設定
        if (eng!=null){
            Uniform scrTexSize=new Uniform(SCREEN_AND_TEX_SIZE, new float[]{eng.getScreenWidth(), eng.getScreenHeight(), this.texture.getImageWidth()*this.scale, this.texture.getImageHeight()*this.scale});
            this.addUniform(scrTexSize);

            Uniform screenSize=new Uniform(SCREEN_SIZE, (float)eng.getPerspectiveMatrix()[5]);
            this.addUniform(screenSize);
        }

        return 0;
    }

    @Override
    public void bind(GL3 gl) {
        super.bind(gl);
        GraphicEngine eng=this.getEngine();

        // ユニフォーム更新
        if (eng!=null){
            Uniform scrTexSize=this.getUniform(SCREEN_AND_TEX_SIZE);
            scrTexSize.setValue(new float[]{eng.getScreenWidth(), eng.getScreenHeight(), this.texture.getImageWidth()*this.scale, this.texture.getImageHeight()*this.scale});
        }
        Uniform myPos=this.getUniform(POSITION);
        myPos.setValue(this.screenCoord);

        this.texture.bind(gl, 0, GL3.GL_TEXTURE0); // 常に0番しか使わない
    }
}
