package k7system.gpuobjects;

/** カメラからの奥行き情報とライトからの奥行き情報，またライトビットマップを取得し，
 * ライト情報が追加されたライトビットマップを出力します<br>
 * そのため，FBOと組み合わせて利用するのが前提です */
public class ShadowMakerMaterial extends Material{
    // スクリーンとテクスチャのサイズ(Sc横ピクセル, Sc縦ピクセル, Tx横ピクセル, Tx縦ピクセル)
    public static final String SCREEN_AND_TEX_SIZE="screenTexSize";

    // テクスチャ関係
    public static final String CAMERA_DEPTH_TEXTURE_UNIT_NAME="cameraDepthTextureUnit";
    public static final String LIGHT_DEPTH_TEXTURE_UNIT_NAME="lightDepthTextureUnit";
    public static final String SCREEN_SIZE="screenSize";

    // 頂点シェーダー
    private String[] vShader=new String[]{
            "#version 330 core\n"+
                    "layout(location = "+VertexArrayObject.LOCATION_VERTEX_POSITION+") in vec3 vertex;\n"+
                    "layout(location = "+VertexArrayObject.LOCATION_TEX_COORDS+") in vec2 texCoord;\n"+
                    "uniform mat4 "+MVP_MATRIX_NAME+";\n"+
                    "uniform vec4 "+SCREEN_AND_TEX_SIZE+";\n"+
                    "uniform float "+SCREEN_SIZE+";\n"+
                    "out vec2 texCoordPixel;\n"+
                    "void main(){\n"+
                    "    vec4 sizes="+SCREEN_AND_TEX_SIZE+";\n"+
                    "    texCoordPixel=texCoord;\n"+ // テクスチャ座標
                    "}\n"
    };

    // フラグメントシェーダー
    private String[] fShader=new String[]{
            "#version 330 core\n"+
                    "uniform mat4 "+APPEARANCE_MATRIX_NAME+";\n"+
                    "uniform sampler2D  "+CAMERA_DEPTH_TEXTURE_UNIT_NAME+";\n"+
                    "uniform sampler2D  "+LIGHT_DEPTH_TEXTURE_UNIT_NAME+";\n"+
                    "in vec2 texCoordPixel;\n"+
                    "out int lightBit;\n"+
                    "void main(){\n"+
                    "    vec2 temp=texCoordPixel;"+
                    "    lightBit=0;\n"+
                    "}\n"
    };

}
