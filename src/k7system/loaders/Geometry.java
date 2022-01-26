package k7system.loaders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** 頂点データと頂点インデクスオブジェクトの組です */
public class Geometry {
    private ArrayList<float[]> baredVertices=new ArrayList<float[]>(100);
    private ArrayList<Vertex> vertices=new ArrayList<Vertex>(100);

    /** 生の頂点アレイの取得です */
    public ArrayList<float[]> getBaredVertices(){
        return baredVertices;
    }

    /** 生の頂点アレイの登録です */
    public void setBaredVertices(ArrayList<float[]> baredVtc){
        baredVertices=baredVtc;
    }

    /** 頂点インデクスアレイの取得です */
    public ArrayList<Vertex> getVertices(){
        return vertices;
    }

    /** 頂点インデクスアレイの登録です */
    public void setVertices(ArrayList<Vertex> vtc){
        vertices=vtc;
    }

    /** 頂点インデクスアレイの追加です */
    public void addVertices(ArrayList<Vertex> vtc){
        vertices.addAll(vtc);
    }

    /** 頂点インデクスの追加です */
    public void addVertex(Vertex vtx){
        vertices.add(vtx);
    }

    /** ジオメトリの融合です */
    public void merge(Geometry newGmt){
        int numOfVertex=baredVertices.size();
        baredVertices.addAll(newGmt.getBaredVertices());

        ArrayList<Vertex> vtc=newGmt.getVertices();
        for(int i=0;i<vtc.size();i++){
            Vertex vtx=vtc.get(i);
            vtx.setPositionIndex(vtx.getPositionIndex()+numOfVertex);
        }
        vertices.addAll(vtc);
    }

    /** ジオメトリに登録された頂点情報からモデルデータを生成します */
    public Model3D createModel3D(List<BasicMaterial> materials){
        Model3D model=new Model3D();
        List<Vertex>[] vtxList=new ArrayList[materials.size()];

        for (int i=0;i<vtxList.length;i++){
            vtxList[i]=new ArrayList<Vertex>();
        }

        // 頂点をマテリアルごとに分離
        for(Vertex vtx:this.getVertices()){
            int id=vtx.getMaterialId();
            vtxList[id].add(vtx);
        }

        VertexPackage[] vps=new VertexPackage[vtxList.length];
        for (int i=0;i<vtxList.length;i++){
            BasicMaterial material=materials.get(i);
            System.out.println("debug: マテリアル名:"+material.getName());
            boolean useTexture=false;
            if (material.getDiffuseTexture()!=null){
                useTexture=true;
            }
            vps[i]=new VertexPackage(this.getVao(vtxList[i], useTexture), material);
            model.addVertexPackage(vps[i]);
        }

        return model;
    }

    /** 頂点統合を実施し，引数の頂点リストを操作するとともに頂点インデックスを返します */
    public int[] cleanVertexList(List<Vertex> vtxList){
        List<Vertex> copied=new ArrayList<Vertex>(vtxList); // 元の頂点リストをコピー
        Set<Vertex> checked=new HashSet<Vertex>(vtxList.size());
        // 存在確認はListよりもSetの方が遥かに早いため、専用setを作ったほうが効率的
        Set<Vertex> vtxHash=new HashSet<Vertex>(vtxList.size());


        System.out.println("debug: 初期頂点数:"+vtxList.size());

        vtxList.clear(); // リストをクリア
        for (int i=0;i<copied.size();i++){
            Vertex currentVertex=copied.get(i);
            if (!checked.contains(currentVertex)){
                if (!vtxHash.contains(currentVertex)){
                    vtxList.add(currentVertex);
                    vtxHash.add(currentVertex);
                }
            }
            checked.add(currentVertex);
        }

        int[] index=new int[copied.size()];
        for (int i=0;i<vtxList.size();i++){
            vtxList.get(i).setPositionIndex(i);
        }
        for (int i=0;i<index.length;i++){
            index[i]=copied.get(i).getPositionIndex();
        }
        System.out.println("debug: ダイエット後頂点数:"+vtxList.size());
        return index;
    }

    /** VAOを作成します */
    public VertexArrayObject getVao(List<Vertex> vtxList, boolean texFlag){
        VertexArrayObject vao=new VertexArrayObject();
        vtxList=new ArrayList<Vertex>(vtxList);

        int[] index=this.cleanVertexList(vtxList);
        float[] vertices=new float[vtxList.size()*3];
        float[] normals=new float[vtxList.size()*3];
        float[] texCoords=new float[vtxList.size()*2];
        float[] tangents=new float[vtxList.size()*3];

        for (int i=0;i<vtxList.size();i++){
            Vertex vtx=vtxList.get(i);
            float[] position=vtx.getPosition();
            vertices[i*3]=position[0];
            vertices[i*3+1]=position[1];
            vertices[i*3+2]=position[2];
            float[] normal=vtx.getNormal();
            normals[i*3]=normal[0];
            normals[i*3+1]=normal[1];
            normals[i*3+2]=normal[2];
            if (texFlag){ // テクスチャ情報がある場合，テクスチャ座標と接線ベクトル情報を登録しておく
                float[] uv=vtx.getTexCoord();
                texCoords[i*2]=uv[0];
                texCoords[i*2+1]=uv[1];
                float[] tan=vtx.getTangent();
                tangents[i*3]=tan[0];
                tangents[i*3+1]=tan[1];
                tangents[i*3+2]=tan[2];
            }
            //index[i]=i;
        }

        //System.out.println("debug: 頂点数:"+vertices.length);
        //System.out.println("debug: 法線数:"+normals.length);

        vao.setVertices(vertices);
        vao.setNormals(normals);
        //vao.setTangents(tangents);
        vao.setIndices(index);
        if (texFlag){
            vao.setTexCoords(texCoords);
            vao.setTangents(tangents);
            System.out.println("debug: UV数:"+texCoords.length);
        }

        return vao;
    }

}
