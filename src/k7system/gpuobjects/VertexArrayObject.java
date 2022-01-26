package k7system.gpuobjects;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;




import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import k7system.GraphicEngine;
import k7system.VectorManager;

/** 頂点配列オブジェクトです<br>
 * 3角形ポリゴン専用であり，4角形ポリゴンには対応していません．<br>
 * インデックス情報を利用して描画することを推奨します */
public class VertexArrayObject extends GPUResource{
    Logger logger=Logger.getGlobal();

    public static final int LOCATION_VERTEX_POSITION=0; // 頂点座標の位置
    public static final int LOCATION_NORMAL_VECTOR=1; // 法線ベクトルの位置
    public static final int LOCATION_TEX_COORDS=2; // テクスチャ座標の位置
    public static final int LOCATION_TANGENT_VECTOR=3; // 接線ベクトルの位置

    private float[] vertices=null; // 頂点情報
    private float[] normals=null; // 法線情報
    private float[] texCoords=null; // テクスチャ座標情報
    private float[] tangents=null; // テクスチャ座標情報
    private int stride; //頂点要素間の距離

    private int[] indices=null;
    private int vboId=-1;
    private int indexId=-1;
    private int vaoId=-1;

    /** 頂点データを取得します */
    public float[] getVertices(){
        return this.vertices;
    }

    /** 頂点データ(x,y,z)を設定します<br>
     * この頂点データの個数が全ての頂点情報の基本になります */
    public void setVertices(float[] verteces){
        this.vertices=verteces;
    }

    /** 頂点vtx0の法線を返します */
    private float[] getNormal(float[] vtx0, float[] vtx1, float[] vtx2){
        float[] vec0=VectorManager.sub(vtx1, vtx0);
        float[] vec1=VectorManager.sub(vtx2, vtx0);
        return VectorManager.cross(vec0, vec1);
    }

    /** 頂点ごとの法線データ(x,y,z)を設定します */
    public void setNormals(float[] normals){
        this.normals=normals;
    }

    /** 法線データを頂点情報から生成します
     * このメソッドを呼び出す前には，必ずsetVerteces()を呼んで頂点情報を設定しなければなりません．<br>
     * また，インデックス情報を利用する場合は，setVertexIndex()を呼んでからでないと結果が破綻します． */
    public void createNormals(){
        this.normals=new float[this.vertices.length];
        if(this.indices==null){  //インデックス情報を利用していない場合
            for(int i=0;i<this.vertices.length/9;i++){ // ポリゴン数ループ
                float[] vtx0=new float[]{vertices[i*3],vertices[i*3+1],vertices[i*3+2]};
                float[] vtx1=new float[]{vertices[i*6],vertices[i*6+1],vertices[i*6+2]};
                float[] vtx2=new float[]{vertices[i*9],vertices[i*9+1],vertices[i*9+2]};
                float[] norm=this.getNormal(vtx0, vtx1, vtx2);
                this.normals[i*9]+=norm[0];
                this.normals[i*9+1]+=norm[1];
                this.normals[i*9+2]+=norm[2];
                norm=this.getNormal(vtx1, vtx2, vtx0);
                this.normals[i*9+3]+=norm[0];
                this.normals[i*9+4]+=norm[1];
                this.normals[i*9+5]+=norm[2];
                norm=this.getNormal(vtx2, vtx0, vtx1);
                this.normals[i*9+6]+=norm[0];
                this.normals[i*9+7]+=norm[1];
                this.normals[i*9+8]+=norm[2];
            }
        }else{ // インデックス情報を使用している場合
            for(int i=0;i<this.indices.length/3;i++){ // ポリゴン数ループ
                int v0=this.indices[i*3];
                int v1=this.indices[i*3+1];
                int v2=this.indices[i*3+2];
                float[] vtx0=new float[]{vertices[v0*3],vertices[v0*3+1],vertices[v0*3+2]};
                float[] vtx1=new float[]{vertices[v1*3],vertices[v1*3+1],vertices[v1*3+2]};
                float[] vtx2=new float[]{vertices[v2*3],vertices[v2*3+1],vertices[v2*3+2]};
                float[] norm=this.getNormal(vtx0, vtx1, vtx2);
                this.normals[v0*3]+=norm[0];
                this.normals[v0*3+1]+=norm[1];
                this.normals[v0*3+2]+=norm[2];
                norm=this.getNormal(vtx1, vtx2, vtx0);
                this.normals[v1*3]+=norm[0];
                this.normals[v1*3+1]+=norm[1];
                this.normals[v1*3+2]+=norm[2];
                norm=this.getNormal(vtx2, vtx0, vtx1);
                this.normals[v2*3]+=norm[0];
                this.normals[v2*3+1]+=norm[1];
                this.normals[v2*3+2]+=norm[2];
            }
        }

        // 法線を正規化します
        for (int i=0;i<this.normals.length/3;i++){ // 頂点数ループ
            float[] norm={this.normals[i*3],this.normals[i*3+1],this.normals[i*3+2],};
            norm=VectorManager.normalize3(norm);
            this.normals[i*3]=norm[0];
            this.normals[i*3+1]=norm[1];
            this.normals[i*3+2]=norm[2];
        }
    }

    /** 頂点ごとのテクスチャデータ(u,v)を設定します */
    public void setTexCoords(float[] texCoords){
        this.texCoords=texCoords;
    }

    /** 頂点インデックスを取得します */
    public int[] getIndices(){
        return this.indices;
    }

    /** 頂点インデックスを設定します<br>
     * ここでnull以外の値を指定した場合，引数の頂点インデックスを使用する設定になります． */
    public void setIndices(int[] ind){
        this.indices=ind;
    }

    /** 頂点インデックスを整数型のリストで設定します */
    public void setIndices(List<Integer> ind){
        int[] tempInd=new int[ind.size()];
        for(int i=0;i<ind.size();i++){
            tempInd[i]=ind.get(i);
        }
        this.indices=tempInd;
    }

    /** 接線ベクトルデータを頂点情報から生成します
     * このメソッドを呼び出す前には，必ずsetVerteces()を呼んで頂点情報を設定しなければなりません．<br>
     * また，インデックス情報を利用する場合は，setVertexIndex()を呼んでからでないと結果が破綻します． */
    public void createTangents(){
        logger.severe("Create tangents is on construction");
    }

    /** 接線ベクトルを設定します */
    public void setTangents(float[] tangents){
        this.tangents=tangents;
    }

    /** VAOの名前を取得します<br>
     * init()メソッドを呼んだ後でなければ有効な値を返しません． */
    public int getVaoId(){
        return this.vaoId;
    }

    /** VRAMフラッシュを通知します */
    @Override
    public void vramFlushed(){
        this.disableUploadedFlag();
    }

    /** 頂点配列オブジェクトを生成し，そのIDを返します<br>
     * このメソッドを呼ぶ前に頂点配列が設定されていなければなりません<br>
     * また、法線データ及びテクスチャ座標を使う場合にはこのメソッドを呼ぶ前に設定されていなければなりません */
    public int init(GL3 gl, GraphicEngine eng){
        super.init(gl, eng);
        if(!this.isUploaded()){

            // 登録されたデータからインターリーブされた頂点データを作成
            this.stride=3; // 要素数で表現．初期値は頂点のみ
            if (this.normals!=null){
               this.stride+=3; // 頂点を追加
            }
            if (this.texCoords!=null){
                this.stride+=2; // テクスチャ座標を追加
            }
            if (this.tangents!=null){
                this.stride+=3; // 接線ベクトルを追加
            }
            float[] vertexData=new float[this.stride*(this.vertices.length/3)]; // 入れ物作成(サイズは頂点数*stride)

            for (int i=0;i<this.vertices.length/3;i++){
                vertexData[i*stride]=this.vertices[i*3];
                vertexData[i*stride+1]=this.vertices[i*3+1];
                vertexData[i*stride+2]=this.vertices[i*3+2];
            }
            if (this.normals!=null){ // 法線情報を織り込み
                for (int i=0;i<this.vertices.length/3;i++){
                    vertexData[i*stride+3]=this.normals[i*3];
                    vertexData[i*stride+4]=this.normals[i*3+1];
                    vertexData[i*stride+5]=this.normals[i*3+2];
                }
            }
            int texOffset=3;
            if (this.texCoords!=null){ // テクスチャ座標情報を織り込み
                if (this.normals!=null){
                    texOffset+=3;
                }
                for (int i=0;i<this.vertices.length/3;i++){
                    vertexData[i*stride+texOffset]=this.texCoords[i*2];
                    vertexData[i*stride+texOffset+1]=this.texCoords[i*2+1];
                }
            }
            int tanOffset=texOffset+2;
            if (this.tangents!=null){ // 接線ベクトル情報を織り込み
                // テクスチャ座標と法線座標は必須
                if (this.normals==null || this.texCoords==null){
                    logger.severe("Tangent requires Normal and TexCoords, but not found them");
                    System.exit(-1);
                }
                for (int i=0;i<this.vertices.length/3;i++){
                    vertexData[i*stride+tanOffset]=this.tangents[i*3];
                    vertexData[i*stride+tanOffset+1]=this.tangents[i*3+1];
                    vertexData[i*stride+tanOffset+2]=this.tangents[i*3+2];
                }
            }

            // VBOの設定
            IntBuffer vbo=IntBuffer.wrap(new int[1]);
            gl.glGenBuffers(1, vbo);
            gl.glBindBuffer (GL.GL_ARRAY_BUFFER, vbo.get(0));
            FloatBuffer pointsBuffer=FloatBuffer.wrap(vertexData);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.SIZE/8*vertexData.length, pointsBuffer, GL.GL_STATIC_DRAW);
            this.vboId=vbo.get(0);
            //System.out.println("DEBUG: VBO設定完了 :"+vbo.get(0));

            // インデックス
            IntBuffer idx=IntBuffer.wrap(new int[1]);
            if (this.indices!=null){
                gl.glGenBuffers(1, idx);
                this.indexId=idx.get(0);
                gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, this.indexId);
                gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, Integer.SIZE/8*this.indices.length, IntBuffer.wrap(this.indices), GL3.GL_STATIC_DRAW);
            }

            // VAOの設定
            IntBuffer vao=IntBuffer.wrap(new int[1]);
            gl.glGenVertexArrays (1, vao);
            gl.glBindVertexArray(vao.get(0));
            // 頂点情報の読み込み
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo.get(0));
            gl.glEnableVertexAttribArray (LOCATION_VERTEX_POSITION);
            gl.glVertexAttribPointer (LOCATION_VERTEX_POSITION, 3, GL.GL_FLOAT, false, this.stride*Float.SIZE/8, 0L); // 最後の引数はnullではない
            if (this.normals!=null){
                // 法線情報の読み込み
                gl.glEnableVertexAttribArray (LOCATION_NORMAL_VECTOR);
                gl.glVertexAttribPointer (LOCATION_NORMAL_VECTOR, 3, GL.GL_FLOAT, false, this.stride*Float.SIZE/8, 3*Float.SIZE/8); // 法線は3要素オフセット
            }
            if (this.texCoords!=null){
                // テクスチャ座標情報の読み込み
                gl.glEnableVertexAttribArray (LOCATION_TEX_COORDS);
                gl.glVertexAttribPointer (LOCATION_TEX_COORDS, 2, GL.GL_FLOAT, false, this.stride*Float.SIZE/8, (texOffset)*Float.SIZE/8); // テクスチャ座標は3(+3)要素オフセット
            }
            if (this.tangents!=null){
                // 接線ベクトル情報の読み込み
                gl.glEnableVertexAttribArray (LOCATION_TANGENT_VECTOR);
                gl.glVertexAttribPointer (LOCATION_TANGENT_VECTOR, 3, GL.GL_FLOAT, false, this.stride*Float.SIZE/8, (tanOffset)*Float.SIZE/8); //接線ベクトルは8要素オフセット
            }
            this.vaoId=vao.get(0);

            int error=gl.glGetError();
            if (error!=GL.GL_NO_ERROR){
                logger.severe("Fail to create VAO:< "+this.vaoId+":"+error+">");
            }

            this.enableUploadedFlag();
            System.out.println("DEBUG: VAO has been initialized !");
        }

        return this.vaoId;
    }

    /** 頂点配列オブジェクトを描画します */
    public void draw(GL3 gl){
        if (this.indices==null){ // インデックスを使わない描画
            gl.glBindVertexArray (this.vaoId);
            gl.glDrawArrays (GL.GL_TRIANGLES, 0, this.vertices.length/3);
            gl.glBindVertexArray (0);
        }else{ // インデックスを使う描画
            gl.glBindVertexArray (this.vaoId);
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, this.indexId);
            gl.glDrawElements(GL3.GL_TRIANGLES, this.indices.length, GL3.GL_UNSIGNED_INT, 0);
            gl.glBindVertexArray (0);
        }

        int error=gl.glGetError();
        if (error!=GL.GL_NO_ERROR){
            logger.severe("Failed to draw VAO <"+this+"> :"+error);
        }

    }

    /** 頂点配列オブジェクトを削除します */
    @Override
    public void dispose(GL3 gl){
        // VBOの後始末
        if (this.indices!=null){
            gl.glDeleteBuffers(1, IntBuffer.wrap(new int[]{this.indexId}));
        }
        gl.glDeleteBuffers(1, IntBuffer.wrap(new int[]{this.vboId}));
        gl.glDeleteVertexArrays(1, IntBuffer.wrap(new int[]{this.vaoId}));
        System.out.println("DEBUG: Vao is dispose!");
        this.disableUploadedFlag();
    }
}
