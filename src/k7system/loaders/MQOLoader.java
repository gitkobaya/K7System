package k7system.loaders;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import k7system.Model3D;
import k7system.VectorManager;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.TextureK7;

/** メタセコイアのファイルを読み込んでModel3Dオブジェクトを出力するためのクラスです．<br>
 * LoD0番のみが登録されたModel3Dが返ります */
public class MQOLoader extends ModelLoader{
    public static int X_AXIS=0x01;
    public static int Y_AXIS=0x02;
    public static int Z_AXIS=0x04;

    public static int FLAT_SHADING=0;
    public static int SMOOTH_SHADING=1;

    private static int numOfMaterials=0;


    /** MQO形式をバイトイメージから読み込むメソッドです */
    public Model3D loadModel(byte[] data,String path){
        Geometry geometry=new Geometry();
        ByteArrayInputStream bais=new ByteArrayInputStream(data);
        InputStreamReader isr=new InputStreamReader(bais);
        BufferedReader br=new BufferedReader(isr);
        StreamTokenizer st=new StreamTokenizer(br);

        List<BasicMaterial> matList=new ArrayList<BasicMaterial>(); // MQOに含まれるマテリアルのリスト

        int kind;

        numOfMaterials=0;

        st.quoteChar('\"'); // ダブルクォーテーションを囲み文字として扱います
        st.ordinaryChar('_'); // アンダーバーを通常文字として扱います
        st.wordChars('_','_');// アンダーバーを単語文字として扱います
        st.eolIsSignificant(true); // 行末を認識します

        // 解析開始です
        // Sceneはスキップします
        try{
            while((kind = st.nextToken()) != StreamTokenizer.TT_EOF ){
                switch ( kind ) {
                case StreamTokenizer.TT_WORD : //System.out.println("String: " + st.sval);
                    if(st.sval.equals("Material")){ // マテリアル情報を発見した場合
                        st.nextToken();
                        numOfMaterials=(int)st.nval;
                        for(int i=0;i<numOfMaterials;i++){
                            BasicMaterial mat=analyzeMaterialData(st,path);
                            matList.add(mat);
                        }
                    }else if(st.sval.equals("Object")){ // オブジェクト情報を発見した場合
                        //System.out.println("debug:オブジェクト情報発見:");
                        Geometry gmt=analyzeObjectData(st);
                        geometry.merge(gmt);
                    }
                    break ;

                case StreamTokenizer.TT_NUMBER : //System.out.println("Number: " + st.nval);
                    break ;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return geometry.createModel3D(matList);
    }

    /** マテリアル情報を解析します<br>
     *  テクスチャ読み込みのためにファイルパスが必要です。 */
    private BasicMaterial analyzeMaterialData(StreamTokenizer st,String path){
        BasicMaterial mat=new BasicMaterial();
        MQOLoaderState state=MQOLoaderState.MATERIAL_NAME;
        boolean start=false;
        boolean isFinish=false;
        int kind;
        float[] color=new float[4];
        float diffuse=1;
        float specular=0;
        float ambient=0;
        float emission=0;

        String textureFileName=null;
        String alphaFileName=null;
        System.out.println("debug:マテリアル情報を解析します");

        st.pushBack();

        // マテリアル情報を吸い出します
        try{
            while((kind = st.nextToken()) != StreamTokenizer.TT_EOF  && !isFinish){
                //System.out.println("debug: sval:"+st.sval+" nval:"+st.nval);
                switch ( kind ) {
                case '\"': // 区切り文字発見
                    switch(state){
                    case MATERIAL_NAME:
                        System.out.println("debug: materialName:"+st.sval);
                        mat.setName(st.sval);
                        start=true; // 読み込み開始
                        break;

                    case TEXTURE_NAME:
                        textureFileName=st.sval;

                        System.out.println("debug:テクスチャ名 "+textureFileName);

                        // テクスチャを設定します
                        BufferedImage image=null;
                        File texFile=new File(path+"/"+textureFileName);
                        if(!texFile.canRead()){ // mqoファイルと同じディレクトリを探してみます
                            texFile=new File(textureFileName); // カレントを探してみます
                            if(!texFile.canRead()){
                                throw new IOException("テクスチャファイル"+textureFileName+"が見つかりません");
                            }
                        }

                        // テクスチャ読み込み
                        try {
                            image=ImageIO.read(texFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mat.setDiffuseTexture(new TextureK7(image)); // マテリアルにテクスチャを設定
                        state=MQOLoaderState.UNKNOWN;
                        break;

                    case ALPHAPLANE_NAME:
                        alphaFileName=st.sval;

                        System.out.println("debug:アルファテクスチャ名 "+alphaFileName);

                        // テクスチャを設定します
                        BufferedImage alphaImage=null;
                        File alphaFile=new File(path+"/"+alphaFileName);
                        if(!alphaFile.canRead()){ // mqoファイルと同じディレクトリを探してみます
                            alphaFile=new File(alphaFileName); // カレントを探してみます
                            if(!alphaFile.canRead()){
                                throw new IOException("テクスチャファイル"+alphaFileName+"が見つかりません");
                            }
                        }

                        // アルファテクスチャ読み込み
                        try {
                            alphaImage=ImageIO.read(alphaFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mat.getDiffuseTexture().setAlphaChannel(alphaImage);
                        state=MQOLoaderState.UNKNOWN;
                        break;

                    default:
                        break;
                    }

                case StreamTokenizer.TT_WORD :
                    if(st.sval.equals("col")){
                        state=MQOLoaderState.COLOR;
                    }else if(st.sval.equals("dif")){
                        state=MQOLoaderState.DIFFUSE;
                    }else if(st.sval.equals("amb")){
                        state=MQOLoaderState.AMBIENT;
                    }else if(st.sval.equals("emi")){
                        state=MQOLoaderState.EMISSION;
                    }else if(st.sval.equals("spc")){
                        state=MQOLoaderState.SPECULAR;
                    }else if(st.sval.equals("power")){
                        state=MQOLoaderState.POWER;
                    }else if(st.sval.equals("tex")){
                        state=MQOLoaderState.TEXTURE_NAME;
                    }else if(st.sval.equals("aplane")){
                        state=MQOLoaderState.ALPHAPLANE_NAME;
                    }
                    break ;

                case StreamTokenizer.TT_NUMBER :
                    switch(state){
                    case COLOR: // 色情報読み込み
                        st.pushBack();
                        color=this.loadFloatValues(st, 4);
                        break ;

                    case DIFFUSE: // 拡散反射率読み込み
                        diffuse=(float)st.nval;
                        break ;

                    case AMBIENT: // 環境光反射率読み込み
                        ambient=(float)st.nval;
                        break ;

                    case EMISSION: // 自己発光読み込み
                        emission=(float)st.nval;
                        break ;

                    case SPECULAR: // 鏡面反射率読み込み
                        specular=(float)st.nval;
                        break ;

                    case POWER: // 鏡面反射率読み込み
                        mat.setShinness((float)st.nval);
                        break ;

                    default:
                        break;
                    }
                default: // 行末まできたら終了
                    //System.out.println("token:"+kind);
                    if(kind==StreamTokenizer.TT_EOL && start){
                        isFinish=true;
                        System.out.println("debug:行末発見！");
                        System.out.flush();
                    }
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        // 色情報の設定
        mat.setDiffuseColor(color[0]*diffuse, color[1]*diffuse, color[2]*diffuse, color[3]);
        mat.setSpecularColor(color[0]*specular, color[1]*specular, color[2]*specular, color[3]);
        mat.setEmissionColor(color[0]*emission, color[1]*emission, color[2]*emission, color[3]);
        mat.setAmbientColor(color[0]*ambient, color[1]*ambient, color[2]*ambient, color[3]);

        return mat;
    }

    /** オブジェクト情報を解析します */
    private Geometry analyzeObjectData(StreamTokenizer st){
        boolean isFinish=false;
        ArrayList<float[]> baredVertices=new ArrayList<float[]>(); // 頂点のリスト
        ArrayList<Polygon3D> polygons=new ArrayList<Polygon3D>();// ポリゴンクラスのリスト
        ArrayList<Vertex> vertices=new ArrayList<Vertex>(); // GLに渡す頂点データ
        String objName=null;
        float[][] normals=null;  // 法線データ
        float[][] tangents=null;  // 接線データ

        Geometry gmt=new Geometry();

        int faceCounter=0;  // 現在の頂点番号
        int shadingMode=0; // 0:フラット 1:スムース
        int kind;
        boolean faceStart=false;
        MQOLoaderState state=MQOLoaderState.OBJECT_NAME;

        int mirrorType=0;
        int mirrorAxis=0;
        int mirrorDistance=0;
        int numOfVertex=0;
        int numOfFaces=65535;

        int thisPolygonsVertex=0; // 現在対象としているポリゴンの頂点数

        Polygon3D currentPol=null;

        System.out.println("debug:オブジェクト情報を解析します");

        // オブジェクト情報を吸い出します
        try{
            while((kind = st.nextToken()) != StreamTokenizer.TT_EOF  && !isFinish){
                switch ( kind ) {
                case '\"': // 区切り文字発見
                    switch(state){
                    case OBJECT_NAME:
                        objName=st.sval;
                        //System.out.println("debug:オブジェクト <"+objName+"> 読み込み");
                        break;

                    default:
                        break;
                    }

                case StreamTokenizer.TT_WORD :
                    //System.out.println("debug: 文字列発見"+st.sval);

                    if(st.sval.equals("shading")){ // フラットかスムースか
                        state=MQOLoaderState.SHADING;
                        shadingMode=this.loadIntegerValues(st,1)[0];
                        //System.out.println("debug: 陰影:"+shadingMode);
                    }else if(st.sval.equals("mirror_axis")){ // 鏡面設定確認
                        state=MQOLoaderState.MIRROR_AXIS;
                        mirrorAxis=(int)this.loadIntegerValues(st,1)[0]; // 鏡面軸読み込み
                        //System.out.println("debug: 鏡面軸:"+mirrorAxis);
                    }else if(st.sval.equals("mirror")){ // 鏡面設定確認
                        state=MQOLoaderState.MIRROR;
                        mirrorType=(int)this.loadIntegerValues(st,1)[0]; // 鏡面設定読み込み
                        //System.out.println("debug: 鏡面設定:"+mirrorType);
                    }else if(st.sval.equals("vertex")){
                        state=MQOLoaderState.VERTECES;
                        numOfVertex=(int)this.loadFloatValues(st,1)[0]; // 頂点数読み込み
                        System.out.println("debug: 頂点数:"+numOfVertex);
                    }else if(st.sval.equals("face")){
                        state=MQOLoaderState.FACES;
                        numOfFaces=(int)this.loadFloatValues(st,1)[0]; // ポリゴン数読み込み
                        polygons=new ArrayList<Polygon3D>(numOfFaces);
                        faceStart=true;
                        //System.out.println("debug: ポリゴン数:"+numOfFaces);
                    }else if(st.sval.equals("M")){
                        state=MQOLoaderState.MATERIAL_ID;
                        //System.out.println("debug: マテリアル情報読み込み");
                    }else if(st.sval.equals("UV")){
                        state=MQOLoaderState.UV;
                    }

                    break ;

                case StreamTokenizer.TT_NUMBER :
                    switch(state){
                    case SHADING:
                        break;

                    case VERTECES: // 頂点データ読み込み
                        for(int i=0;i<numOfVertex;i++){
                            st.pushBack();
                            float[] vertex=this.loadFloatValues(st, 3);
                            baredVertices.add(vertex);
                            //System.out.println("debug: 頂点("+i+"):"+vertex[0]+" "+vertex[1]+" "+vertex[2]);
                        }
                        break;

                    case FACES: // ポリゴン頂点インデクス読み込み
                        currentPol=new Polygon3D();
                        thisPolygonsVertex=(int)st.nval; // このポリゴンの頂点数

                        if(thisPolygonsVertex>2){ //線と点は無視する

                            // debug
                            int[] index=null;
                            try{
                                index=this.loadIntegerValues(st,thisPolygonsVertex);
                            }catch(Exception e){
                                e.printStackTrace();
                                System.exit(-1);
                            }

                            currentPol.index=this.reverseIndex(index);
                            currentPol.materialID=numOfMaterials; //材質未定を示すID
                            polygons.add(currentPol);

                            //System.out.print("debug:ポリゴン読み込み("+faceCounter+") 頂点数:"+thisPolygonsVertex);
                            //System.out.flush();
                            //System.out.println(" ("+index[0]+" "+index[1]+" "+index[2]+")");

                            faceCounter++;
                        }else{
                            this.toEndOfLine(st);
                            numOfFaces--; // スキップしたポリゴンを除く
                            if(faceCounter==numOfFaces){
                                isFinish=true;
                            }
                        }
                        break;

                    case MATERIAL_ID: // マテリアル番号の読み込み
                        st.pushBack();
                        int materialID=this.loadIntegerValues(st,1)[0]; // マテリアルIDの読み込み
                        currentPol.materialID=materialID;
                        //System.out.println("debug:マテリアル番号 Mat("+materialID+")");
                        break;

                    case UV: // UVデータ読み込み(頂点カラー要素は無視)
                        //System.out.println("debug: UVマップ読み込み Pol."+(faceCounter-1)+" 頂点数:"+thisPolygonsVertex);
                        float[][] uvMap=new float[thisPolygonsVertex][2];
                        for(int i=0;i<thisPolygonsVertex;i++){
                            st.pushBack();
                            uvMap[i]=this.loadFloatValues(st,2);
                            //System.out.println("debug: U:"+uvMap[i][0]+" V:"+uvMap[i][1]);
                        }

                        try{
                            polygons.get(faceCounter-1).uv=this.reverseUV(uvMap);
                        }catch(Exception e){
                            System.err.println("error:オブジェクト名"+objName);
                            System.err.println("error:状況 faceCounter:"+faceCounter+" ポリゴン数:"+polygons.size());
                            e.printStackTrace();
                            System.out.flush();
                            System.exit(-1);
                        }

                        state=MQOLoaderState.FACES;

                        // 最後のポリゴンならばオブジェクト終了
                        if(faceCounter==numOfFaces){
                            isFinish=true;
                        }
                        break;

                    default:
                        break;
                    }

                default: // 行末まできたら終了
                    if(kind==StreamTokenizer.TT_EOL && faceStart){
                        state=MQOLoaderState.FACES;
                        //System.out.println("debug:行末発見！");
                        //System.out.flush();
                        // 最後のポリゴンならばオブジェクト終了
                        if(faceCounter==numOfFaces){
                            //System.out.println("debug:オブジェクト読み込み終了 解析開始");
                            isFinish=true;
                        }
                    }
                    break;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        int num=polygons.size();

        // 鏡面生成
        if(mirrorAxis!=0){
            // とりあえず全頂点を反転コピー
            for(int j=0;j<numOfVertex;j++){
                baredVertices.add(this.mirrorVertex(baredVertices.get(j).clone(), mirrorAxis));
            }

            for(int i=0;i<num;i++){
                Polygon3D pol=polygons.get(i);
                Polygon3D mirrorPol=pol.clone();// 鏡面ポリゴンを作成する

                if(pol.index.length==3){ // 3角形
                    mirrorPol.index=new int[]{pol.index[2]+numOfVertex,pol.index[1]+numOfVertex,pol.index[0]+numOfVertex};
                    if(mirrorPol.uv!=null){
                        mirrorPol.uv=new float[][]{pol.uv[2],pol.uv[1],pol.uv[0]};
                    }
                }else{ //3角で無いなら4角形
                    mirrorPol.index=new int[]{pol.index[3]+numOfVertex,pol.index[2]+numOfVertex,pol.index[1]+numOfVertex,pol.index[0]+numOfVertex};
                    if(mirrorPol.uv!=null){
                        mirrorPol.uv=new float[][]{pol.uv[3],pol.uv[2],pol.uv[1],pol.uv[0]};
                    }
                }
                polygons.add(mirrorPol);
            }
            //System.out.println("debug: 鏡面作成　頂点数:"+baredVertices.size());
        }

        num=polygons.size();

        // 3角分割
        for(int i=0;i<num;i++){
            Polygon3D pol=polygons.get(i);

            // 4角形ポリゴンの場合
            if(pol.index.length==4){
                int index[]=pol.index;
                float uvMap[][]=pol.uv;
                pol.index=new int[]{index[0],index[1],index[2]};
                if(pol.uv!=null){
                    pol.uv=new float[][]{uvMap[0],uvMap[1],uvMap[2]};
                }
                Polygon3D pol2=pol.clone();
                pol2.index=new int[]{index[2],index[3],index[0]};
                if(pol.uv!=null){
                    pol2.uv=new float[][]{uvMap[2],uvMap[3],uvMap[0]};
                }
                polygons.add(pol2);
            }
            if (pol.index.length>4){
                System.err.println("ERROR: Over 5 vertices in a polygon !!");
                System.exit(-1);
            }
        }

        // 頂点要素の操作
        num=polygons.size();
        normals=new float[baredVertices.size()][3];
        tangents=new float[baredVertices.size()][3];
        for(int i=0;i<num;i++){
            Polygon3D pol=polygons.get(i);
            float vtc[][]=new float[3][];
            vtc[0]=new float[3];
            vtc[1]=new float[3];
            vtc[2]=new float[3];
            for(int j=0;j<3;j++){
                try{
                    vtc[j]=baredVertices.get(pol.index[j]).clone();
                }catch(Exception e){
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            // 法線生成
            pol.normal=VectorManager.normalize3(VectorManager.cross(VectorManager.sub(vtc[1], vtc[0]), VectorManager.sub(vtc[2], vtc[1])));
            //System.out.println("debug:pol["+i+"] normal x:"+pol.normal[0]+" y:"+pol.normal[1]+" z:"+pol.normal[2]);
            // 接線生成(x, u, vによる平面を想定する)
            if (pol.uv!=null){
                float[] a=new float[3];
                float[] b=new float[3];
                for (int xyz=0;xyz<3;xyz++){ // x:0 y:1 z:2
                    float[] v0=new float[]{baredVertices.get(pol.index[0])[xyz], pol.uv[0][0],  pol.uv[0][1]};
                    float[] v1=new float[]{baredVertices.get(pol.index[1])[xyz], pol.uv[1][0],  pol.uv[1][1]};
                    float[] v2=new float[]{baredVertices.get(pol.index[2])[xyz], pol.uv[2][0],  pol.uv[2][1]};
                    float[] vec0=VectorManager.sub(v1, v0);
                    float[] vec1=VectorManager.sub(v2, v1);
                    float[] norm=VectorManager.cross(vec0, vec1);
                    a[xyz]=norm[0];
                    b[xyz]=norm[1];
                }
                pol.tangent=VectorManager.normalize3(new float[]{-b[0]/a[0],-b[1]/a[1],-b[2]/a[2]});
            }

            Vertex[] vertex=new Vertex[3];
            for(int j=0;j<3;j++){
                vertex[j]=new Vertex();
                vertex[j].setPositionIndex(pol.index[j]);
                vertex[j].setPosition(baredVertices.get(pol.index[j]));
                // UV設定
                if(pol.uv!=null){
                    vertex[j].setTexCoord(pol.uv[j]);
                }
                vertex[j].setMaterialId(pol.materialID);
                vertices.add(vertex[j]);
            }

            if(shadingMode==FLAT_SHADING){ // フラットシェーディングモードの場合
                vertex[0].setNormal(pol.normal); // 全頂点が同じ法線
                vertex[1].setNormal(pol.normal); // 全頂点が同じ法線
                vertex[2].setNormal(pol.normal); // 全頂点が同じ法線
                vertex[0].setTangent(pol.tangent); // 全頂点が同じ接線
                vertex[1].setTangent(pol.tangent); // 全頂点が同じ接線
                vertex[2].setTangent(pol.tangent); // 全頂点が同じ接線
            }else{ // スムースシェーディングモードの場合
                // ポリゴンの法線を各頂点の法線バッファに加算します
                for(int j=0;j<3;j++){
                    normals[pol.index[j]][0]+=pol.normal[0];
                    normals[pol.index[j]][1]+=pol.normal[1];
                    normals[pol.index[j]][2]+=pol.normal[2];

                    tangents[pol.index[j]][0]+=pol.tangent[0];
                    tangents[pol.index[j]][1]+=pol.tangent[1];
                    tangents[pol.index[j]][2]+=pol.tangent[2];
                }
            }
        }

        // スムースシェーディングの場合の法線設定処理です
        if(shadingMode==SMOOTH_SHADING){
            System.out.println("debug:法線処理");
            for(int i=0;i<num;i++){
                Polygon3D pol=polygons.get(i);
                vertices.get(i*3).setNormal(VectorManager.normalize3(normals[pol.index[0]]));
                vertices.get(i*3+1).setNormal(VectorManager.normalize3(normals[pol.index[1]]));
                vertices.get(i*3+2).setNormal(VectorManager.normalize3(normals[pol.index[2]]));
                //System.out.println("debug:pol["+i+"]");
                vertices.get(i*3).setTangent(VectorManager.normalize3(tangents[pol.index[0]]));
                vertices.get(i*3+1).setTangent(VectorManager.normalize3(tangents[pol.index[1]]));
                vertices.get(i*3+2).setTangent(VectorManager.normalize3(tangents[pol.index[2]]));
            }
        }

        gmt.setBaredVertices(baredVertices);
        gmt.setVertices(vertices);
        return gmt;
    }

    /** インデックスを反転させます */
    private int[] reverseIndex(int[] index){
        int length=index.length;
        int[] result=new int[length];

        for(int i=0;i<length;i++){
            result[i]=index[length-i-1];
        }

        return result;
    }

    /** UVを反転させます */
    private float[][] reverseUV(float[][] uv){
        int length=uv.length;
        float[][] result=new float[length][];

        for(int i=0;i<length;i++){
            result[i]=uv[length-i-1];
        }

        return result;
    }


    /** 頂点を反転させます */
    private float[][] mirrorVerteces(float[][] vtx,int mirrorAxis){
        float[][] verteces=new float[vtx.length][];
        int numOfVertex=vtx.length;
        float xMirror=1;
        float yMirror=1;
        float zMirror=1;

        if(mirrorAxis==1) xMirror=-1;
        if(mirrorAxis==2) yMirror=-1;
        if(mirrorAxis==4) zMirror=-1;

        for(int i=0;i<numOfVertex;i++){
            verteces[i]=new float[3];
            verteces[i][0]=vtx[i][0]*xMirror;
            verteces[i][1]=vtx[i][1]*yMirror;
            verteces[i][2]=vtx[i][2]*zMirror;
        }
        return verteces;
    }

    /** 頂点を反転させます */
    private float[]mirrorVertex(float[] vtx,int mirrorAxis){
        float[] vertex=new float[3];
        float xMirror=1;
        float yMirror=1;
        float zMirror=1;

        if(mirrorAxis==1) xMirror=-1;
        if(mirrorAxis==2) yMirror=-1;
        if(mirrorAxis==4) zMirror=-1;

        vertex[0]=vtx[0]*xMirror;
        vertex[1]=vtx[1]*yMirror;
        vertex[2]=vtx[2]*zMirror;
        return vertex;
    }


    /** 連続したn個の数字を読み込みます */
    private float[] loadFloatValues(StreamTokenizer st,int num){
        float[] values=new float[num];
        int kind;

        try{
            int counter=0;
            while((kind = st.nextToken()) != StreamTokenizer.TT_EOF && counter<num){
                switch ( kind ) {
                case StreamTokenizer.TT_WORD :
                    break ;
                case StreamTokenizer.TT_NUMBER :
                    values[counter]=(float)st.nval;
                    counter++;
                    break ;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return values;
    }

    /** 連続したn個の数字を読み込みます */
    private int[] loadIntegerValues(StreamTokenizer st,int num){
        int[] values=new int[num];
        int kind;

        try{
            int counter=0;
            while((kind = st.nextToken()) != StreamTokenizer.TT_EOF && counter<num){
                switch ( kind ) {
                case StreamTokenizer.TT_WORD :
                    break ;
                case StreamTokenizer.TT_NUMBER :
                    values[counter]=(int)st.nval;
                    counter++;
                    break ;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return values;
    }

    /** ストリームを行末まで進行させます */
    private void toEndOfLine(StreamTokenizer st){
        int kind;

        try{
            int counter=0;
            while((kind = st.nextToken()) != StreamTokenizer.TT_EOL){
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}

/** MQOファイル読み込み時に使用する状態列挙です */
enum MQOLoaderState {
    MATERIAL_NAME,
    COLOR,
    DIFFUSE,
    AMBIENT,
    EMISSION,
    SPECULAR,
    POWER,
    TEXTURE_NAME,
    ALPHAPLANE_NAME,
    OBJECT_NAME,
    PATCH,
    SEGMENT,
    SHADING,
    FACET,
    UV,
    VERTEX_SIZE,
    MIRROR,
    MIRROR_AXIS,
    MIRROR_DISTANCE,
    VERTECES,
    FACE_SIZE,
    FACES,
    MATERIAL_ID,
    LOAD_VERTECES,
    UNKNOWN,
}