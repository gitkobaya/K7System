package k7system.gpuobjects;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.Params;
import k7system.VectorManager;

/** 基本マテリアルです<br>
 *  固定機能時代のシェーダー機能を再現します<br>
 *  ユニフォーム変数の設定メソッドはシステムから呼び出すことを想定していますので，直接操作はしないでください．<br>
 *  これを継承したマテリアルを作成する場合，シェーダーオブジェクトを差し替えてください．
 *  ただし，頂点シェーダーに与える変換行列としてmvp,view,rotationという名前のユニフォーム変数が必要です．
 *  Basic Mterial<br>
 *  It emulates functions of former OpenGL shaders.<br>
 *  In many cases, this class's function is enough to express polygon object.*/
public class BasicMaterial extends Material{

    public static final int MAXIMUM_LIGHT_NUM=Params.MAXIMUM_LIGHT_NUM;

    public static final String LIGHTS_NAME="lights";
    public static final String USE_LIGHT_FLAG_NAME="isUseLight";
    public static final String SHINESS_NAME="shinnes";

    // テクスチャ関係
    public static final String USE_DIFFUSE_TEXTURE_NAME="isUseDiffeseTexture";
    public static final String DIFFUSE_TEXTURE_UNIT_NAME="diffeseTextureUnit";
    public static final String NORMAL_TEXTURE_UNIT_NAME="normalTextureUnit";

    public static final String USE_SPECULAR_TEXTURE_NAME="isUseSpecularTexture";
    public static final String USE_EMISSION_TEXTURE_NAME="isUseEmissionTexture";
    public static final String USE_NORMAL_TEXTURE_NAME="isUseNormalTexture";
    public static final int UNUSE=0;
    public static final int USE=1;

    private static final int DIFFUSE_TEXTURE=0;
    private static final int SPECULAR_TEXTURE=1;
    private static final int EMISSION_TEXTURE=2;
    private static final int NORMAL_TEXTURE=3;

    /** このマテリアルで利用するテクスチャです[0]が拡散反射，[1]が鏡面反射，[2]が放射光，[3]が法線 */
    private TextureK7[] textures=new TextureK7[4];

    // ユニフォーム変数関係
    private Uniform mvpMatrix;
    private Uniform mvMatrix;
    private Uniform rotationMatrix;
    private Uniform lights;
    private Uniform useLight; // ライティングを実施するかどうかです
    private Uniform appMatrix;    // アピアランスの行列です．4x4行列であり，それぞれ(Diffuse, Specular, Ambient, Emission)を表します．
    private Uniform shinness; // スペキュラ反射の鋭さです
    private Uniform[] useTexture=new Uniform[4]; // 各テクスチャを利用するかどうかです
    private Uniform[] textureUnit=new Uniform[4]; // 拡散反射テクスチャのテクスチャユニットです

    private List<TextureK7> removeTextures=new ArrayList<TextureK7>();

    // 頂点シェーダー
    private String[] vShader=new String[]{
            "#version 330 core\n"+
            "layout(location = "+VertexArrayObject.LOCATION_VERTEX_POSITION+") in vec3 vertex;\n"+
            "layout(location = "+VertexArrayObject.LOCATION_NORMAL_VECTOR+") in vec3 norm;\n"+
            "layout(location = "+VertexArrayObject.LOCATION_TEX_COORDS+") in vec2 texCoord;\n"+
            "layout(location = "+VertexArrayObject.LOCATION_TANGENT_VECTOR+") in vec3 tang;\n"+
            "uniform mat4 "+MVP_MATRIX_NAME+";\n"+
            "uniform mat4 "+MV_MATRIX_NAME+";\n"+
            "uniform mat3 "+ROTATION_MATRIX_NAME+";\n"+
            "uniform mat4 "+APPEARANCE_MATRIX_NAME+";\n"+
            "out vec4 vPosition;\n"+
            "out vec3 normal;\n"+
            "out vec3 tangent;\n"+
            "out vec3 binormal;\n"+
            "out vec2 texCoordPixel;\n"+
            "void main(){\n"+
            "    normal = normalize("+ROTATION_MATRIX_NAME+"*norm);\n"+ // 法線ベクトルを視点座標系に
            "    tangent = normalize("+ROTATION_MATRIX_NAME+"*tang);\n"+ // 接線ベクトルを視点座標系に
            "    binormal = cross(normal,tangent);\n"+ // 従法線ベクトルを取得
            "    vPosition = "+MV_MATRIX_NAME+"*vec4(vertex,1.0);\n"+ // 視点座標系での位置
            "    texCoordPixel=texCoord;\n"+ // テクスチャ座標
            "    gl_Position = "+MVP_MATRIX_NAME+"*vec4(vertex,1.0);\n"+ // 画面座標での位置
            "}\n"
    };

    // フラグメントシェーダー
    private String[] fShader=new String[]{
            "#version 330 core\n"+
            "uniform mat4 "+MVP_MATRIX_NAME+";\n"+
            "uniform mat3 "+ROTATION_MATRIX_NAME+";\n"+
            "uniform mat4 "+APPEARANCE_MATRIX_NAME+";\n"+
            "uniform int "+USE_LIGHT_FLAG_NAME+";\n"+
            "uniform mat4 "+LIGHTS_NAME+"["+MAXIMUM_LIGHT_NUM+"];\n"+
            "uniform float "+SHINESS_NAME+";\n"+
            "uniform int "+USE_DIFFUSE_TEXTURE_NAME+";\n"+
            "uniform int "+USE_NORMAL_TEXTURE_NAME+";\n"+
            "uniform sampler2D  "+DIFFUSE_TEXTURE_UNIT_NAME+";\n"+
            "uniform sampler2D  "+NORMAL_TEXTURE_UNIT_NAME+";\n"+
            "in vec4 vPosition;\n"+ // フラグメントの視点座標系での座標
            "in vec3 normal;\n"+
            "in vec3 tangent;\n"+
            "in vec3 binormal;\n"+
            "in vec2 texCoordPixel;\n"+
            "out vec4 finalcolor;\n"+
            "void main(){\n"+
            "    vec4 brightDiffuse=vec4(0.0);\n"+
            "    vec4 brightSpecular=vec4(0.0);\n"+
            "    vec4 brightAmbient=vec4(0.0);\n"+
            "    float lightReduce=1.0;\n"+
            "    float finalPower=0.0;\n"+
            "    vec3 currentNormal=normal;\n"+
            "    if("+USE_LIGHT_FLAG_NAME+"!=0) {\n"+
            // ライトを使う時点で法線処理を導入
            "        if ("+USE_NORMAL_TEXTURE_NAME+"!=0) { \n"+
            "            vec3 texNormal=texture2D("+NORMAL_TEXTURE_UNIT_NAME+",texCoordPixel).xyz*2.0-1.0;\n"+ // テクセルの法線
            "            mat3 matN=mat3(tangent[0], tangent[1],tangent[2], binormal[0], binormal[1],binormal[2], normal[0], normal[1], normal[2]);\n"+
            "            currentNormal=matN*texNormal;"+
            "        }\n"+
            "        for(int i=0;i<"+MAXIMUM_LIGHT_NUM+";i++){\n"+
            "            mat4 light="+LIGHTS_NAME+"[i];\n"+
            "            if (light[0][0]!=0.0 || light[0][1]!=0.0 || light[0][2]!=0.0 || light[0][3]!=0.0){ \n"+ // ライト計算
            "                vec3 lightVec=vec3(light[0][0],light[0][1],light[0][2]);\n"+
            "                lightReduce=1.0;\n"+
            "                if (light[0][3]==1.0){ \n"+ // 点光源だった場合
            "                    lightVec=(vPosition-light[0]).xyz;\n"+ // 光源からフラグメントまでのベクトル
            "                    lightReduce=1.0/pow(length(lightVec),2);\n"+ //距離による減衰
            "                }"+// 平行光源だった場合はそのままベクトルとして扱う
            "                lightVec=normalize(lightVec);\n"+
            // 拡散反射成分
            "                finalPower=-lightReduce*dot(lightVec,currentNormal);\n"+
            "                if (finalPower>0) {;\n"+
            "                    brightDiffuse+=finalPower*light[1];\n"+
            "                };\n"+
            // 鏡面反射成分
            "                vec3 eyeVec=normalize(vec3(vPosition[0],vPosition[1],vPosition[2]));\n"+ // 視点からフラグメントまでのベクトル
            "                vec3 reflect=normalize(-lightVec+2*dot(lightVec,currentNormal)*currentNormal);\n"+
            "                finalPower=lightReduce*pow(dot(eyeVec,reflect),"+SHINESS_NAME+");\n"+
            "                if (finalPower>0) {;\n"+
            "                    brightSpecular+=finalPower*light[1];\n"+
            "                };\n"+
            // 環境光成分
            "                brightAmbient+=light[2];\n"+
            "            };\n"+
            "        };\n"+
            // 最終的な出力色の調整
            "        finalcolor="+APPEARANCE_MATRIX_NAME+"[0]*brightDiffuse;\n"+
            "        finalcolor+="+APPEARANCE_MATRIX_NAME+"[2]*brightAmbient;\n"+
            "    }else{\n"+
            "        finalcolor="+APPEARANCE_MATRIX_NAME+"[0];\n"+
            "    };\n"+
            // 放射光を加算
            "    finalcolor+="+APPEARANCE_MATRIX_NAME+"[3];\n"+
            // テクスチャ色を反映
            "    if ("+USE_DIFFUSE_TEXTURE_NAME+"!=0) { \n"+
            "        finalcolor*=texture2D("+DIFFUSE_TEXTURE_UNIT_NAME+",texCoordPixel);\n"+
            "    }\n"+
            "    finalcolor+="+APPEARANCE_MATRIX_NAME+"[1]*brightSpecular;\n"+ // スペキュラ色の反映
            // 透明度を設定
            "    finalcolor[3]="+APPEARANCE_MATRIX_NAME+"[0][3];\n"+
            "}\n"
   };

    /** 初期化します */
    public BasicMaterial() {
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

        // アピアランス行列の登録
        this.appMatrix=new Uniform(APPEARANCE_MATRIX_NAME,DEFAULT_APPEARANCE.clone()); // 質感行列ユニフォーム
        this.addUniform(this.appMatrix);

        // テクスチャ利用関係変数の登録
        // 拡散反射テクスチャ
        this.useTexture[DIFFUSE_TEXTURE]=new Uniform(USE_DIFFUSE_TEXTURE_NAME, UNUSE); // 初期状態では非使用
        this.addUniform(this.useTexture[DIFFUSE_TEXTURE]);
        this.textureUnit[DIFFUSE_TEXTURE]=new Uniform(DIFFUSE_TEXTURE_UNIT_NAME, DIFFUSE_TEXTURE); // テクスチャユニット番号を登録する
        this.addUniform(this.textureUnit[DIFFUSE_TEXTURE]);

        // 法線テクスチャ
        this.useTexture[NORMAL_TEXTURE]=new Uniform(USE_NORMAL_TEXTURE_NAME, UNUSE);
        this.addUniform(this.useTexture[NORMAL_TEXTURE]);
        this.textureUnit[NORMAL_TEXTURE]=new Uniform(NORMAL_TEXTURE_UNIT_NAME, NORMAL_TEXTURE); // テクスチャユニット番号を登録する
        this.addUniform(this.textureUnit[NORMAL_TEXTURE]);


        // ライト関係変数の登録
        float[][] lightsArray=new float[MAXIMUM_LIGHT_NUM][16];
        this.lights=new Uniform(LIGHTS_NAME, lightsArray); // 照明配列ユニフォーム
        this.addUniform(this.lights);
        this.shinness=new Uniform(SHINESS_NAME,1.0f);
        this.addUniform(this.shinness);

        this.useLight=new Uniform(USE_LIGHT_FLAG_NAME,1);
        this.addUniform(this.useLight);

    }

    /** 初期化します<br>
     * 返り値は特に使用しないため，値は不定です */
    @Override
    public int init(GL3 gl, GraphicEngine eng) {
        super.init(gl,eng);

        // テクスチャの処理
        if (this.textures[DIFFUSE_TEXTURE]!=null){
            this.textures[DIFFUSE_TEXTURE].init(gl,eng);
        }
        if (this.textures[NORMAL_TEXTURE]!=null){
            this.textures[NORMAL_TEXTURE].init(gl,eng);
        }
        return 0;
    }

    /** 拡散反射テクスチャを取得します */
    public TextureK7 getDiffuseTexture(){
        return this.textures[DIFFUSE_TEXTURE];
    }

    /** 拡散反射テクスチャを設定します */
    public void setDiffuseTexture(TextureK7 tex){
        this.setTexture(tex, DIFFUSE_TEXTURE);
    }

    /** 法線テクスチャを設定します */
    public void setNormalTexture(TextureK7 tex){
        this.setTexture(tex, NORMAL_TEXTURE);
    }

    /** テクスチャを設定するための内部メソッドです<br>
     * 第2引数でテクスチャ種別を設定します */
    protected void setTexture(TextureK7 tex, int type){
        // 従来テクスチャの削除処理
        if (this.textures[type]!=null && this.textures[type]!=tex){
            this.removeTextures.add(this.textures[type]); // 削除対象リストに登録(連続で更新するおばかさんがいるかもしれないので複数登録できるようにする)
        }

        // テクスチャの更新処理
        if (tex!=null){
            tex.addParent(this);
            this.useTexture[type].setValue(USE);
            if (!tex.isUploaded()){
                this.disableUploadedFlag();
            }
        }else{
            this.useTexture[type].setValue(UNUSE);
        }
        this.textures[type]=tex;
    }

    /** 色を設定します<br>
     * 拡散反射，鏡面反射，環境光の全てに係ります */
    public void setColor(float r, float g, float b){
        this.setColor(r, g, b, 1);
    }

    /** 色を設定します<br>
     * 拡散反射，鏡面反射，環境光の全てに係ります */
    public void setColor(float r, float g, float b, float a){
        this.setColor(new float[]{r,g,b,a});
    }

    /** 色を設定します<br>
     * 拡散反射，鏡面反射，環境光の全てに係ります */
    public void setColor(float [] color){
        float[] apps=(float[])this.appMatrix.getValue();
        apps[0]=color[0];
        apps[1]=color[1];
        apps[2]=color[2];
        apps[3]=color[3];
        apps[4]=color[0];
        apps[5]=color[1];
        apps[6]=color[2];
        apps[7]=color[3];
        apps[8]=color[0];
        apps[9]=color[1];
        apps[10]=color[2];
        apps[11]=color[3];
    }

    /** 拡散反射色を設定します */
    public void setDiffuseColor(float r, float g, float b){
        this.setDiffuseColor(new float[]{r, g, b, 1});
    }

    /** 拡散反射色を設定します */
    public void setDiffuseColor(float r, float g, float b, float a){
        this.setDiffuseColor(new float[]{r,g,b,a});
    }

    /** 拡散反射色を設定します */
    public void setDiffuseColor(float [] color){
        float[] appear=(float[])this.appMatrix.getValue();
        appear[0]=color[0];
        appear[1]=color[1];
        appear[2]=color[2];
        appear[3]=color[3];
    }

    /** 鏡面反射色を設定します */
    public void setSpecularColor(float r, float g, float b){
        this.setSpecularColor(new float[]{r, g, b, 1});
    }

    /** 鏡面反射色を設定します */
    public void setSpecularColor(float r, float g, float b, float a){
        this.setSpecularColor(new float[]{r,g,b,a});
    }

    /** 鏡面反射色を設定します */
    public void setSpecularColor(float [] color){
        float[] appear=(float[])this.appMatrix.getValue();
        appear[4]=color[0];
        appear[5]=color[1];
        appear[6]=color[2];
        appear[7]=color[3];
    }

    /** 環境反射色を設定します */
    public void setAmbientColor(float r,float g, float b){
        this.setAmbientColor(new float[]{r,g,b,1});
    }

    /** 環境反射色を設定します */
    public void setAmbientColor(float r,float g, float b, float a){
        this.setAmbientColor(new float[]{r,g,b,a});
    }

    /** 環境反射色を設定します */
    public void setAmbientColor(float [] color){
        float[] appear=(float[])this.appMatrix.getValue();
        appear[8]=color[0];
        appear[9]=color[1];
        appear[10]=color[2];
        appear[11]=color[3];
    }

    /** 放射光を設定します */
    public void setEmissionColor(float r,float g, float b){
        this.setEmissionColor(new float[]{r,g,b,1});
    }

    /** 放射光を設定します */
    public void setEmissionColor(float r,float g, float b, float a){
        this.setEmissionColor(new float[]{r,g,b,a});
    }

    /** 放射光を設定します */
    public void setEmissionColor(float [] color){
        float[] appear=(float[])this.appMatrix.getValue();
        appear[12]=color[0];
        appear[13]=color[1];
        appear[14]=color[2];
        appear[15]=color[3];

    }

    /** 透明度を設定します */
    public void setAlpha(float alpha){
        float[] apps=(float[])this.appMatrix.getValue();
        if (alpha<0){
            alpha=0;
        }else if(alpha>1){
            alpha=1;
        }
        apps[3]=alpha;
        apps[7]=alpha;
        apps[11]=alpha;
    }

    /** 拡散反射の鋭さを指定します */
    public void setShinness(float shine){
        this.shinness.setValue(shine);
    }

    /** このマテリアルが参照するライト情報を設定します<br>
     * このメソッドによって，ライト座標がワールド座標から視点座標に変換されシェーダーに投入される準備をします．
     * 通常，Model3Dクラスから呼び出されます．ビュー行列はシステムで一意なのと，このクラスではここでしか利用しないのでローカル変数として設定しません． */
    public void refleshLights(List<LightObject> lightList, float[] viewMat){
        Uniform lightsUni=this.getUniform(LIGHTS_NAME);
        float[][] lightsData=(float[][])lightsUni.getValue();

        if (lightList!=null){
            for (int i=0;i<lightList.size();i++){
                lightsData[i]=lightList.get(i).getLightParameters().clone(); // 値をいじるので，ライトパラメーターをコピーしてから代入する

                // ライト座標をワールド座標系から視点座標系に変換
                // シェーダー内でもライティングに関しては視点座標系で実施
                float[] lightPos=VectorManager.multMatrixVector(viewMat,new float[]{lightsData[i][0],lightsData[i][1],lightsData[i][2],lightsData[i][3]});
                lightsData[i][0]=lightPos[0];
                lightsData[i][1]=lightPos[1];
                lightsData[i][2]=lightPos[2];
                lightsData[i][3]=lightPos[3];
            }
        }
        lightsUni.setValue(lightsData);
    }

    /** ライティングを実施するかどうかを設定します */
    public void setUseLights(boolean isUse){
        Uniform uni=this.getUniform(USE_LIGHT_FLAG_NAME);
        uni.setValue(isUse);
    }

    @Override
    public void bind(GL3 gl) {
        super.bind(gl);

        // テクスチャ変更があった場合に古いのを捨てておく
        for (TextureK7 tex:this.removeTextures){
            tex.removeParent(this);
        }
        this.removeTextures.clear();

        // テクスチャをバインド
        if ((Integer)this.useTexture[DIFFUSE_TEXTURE].getValue()!=0){ // もし拡散反射テクスチャを使っているのなら
            this.textures[DIFFUSE_TEXTURE].bind(gl, DIFFUSE_TEXTURE, GL3.GL_TEXTURE0);
        }
        if ((Integer)this.useTexture[NORMAL_TEXTURE].getValue()!=0){ // もし法線テクスチャを使っているのなら
            this.textures[NORMAL_TEXTURE].bind(gl, NORMAL_TEXTURE, GL3.GL_TEXTURE3);
        }

    }

    @Override
    public void vramFlushed() {
        super.vramFlushed();
        // テクスチャにもデバイスロストを通知
        if (this.textures[DIFFUSE_TEXTURE].isUploaded()){
            this.textures[DIFFUSE_TEXTURE].vramFlushed();
        }
        if (this.textures[NORMAL_TEXTURE].isUploaded()){
            this.textures[NORMAL_TEXTURE].vramFlushed();
        }
    }

    @Override
    public void dispose(GL3 gl) {
        super.dispose(gl);
        // テクスチャの親から自分を削除
        if (textures[DIFFUSE_TEXTURE]!=null){
            this.textures[DIFFUSE_TEXTURE].removeParent(this);
        }
        if (textures[NORMAL_TEXTURE]!=null){
            this.textures[NORMAL_TEXTURE].removeParent(this);
        }
    }
}
