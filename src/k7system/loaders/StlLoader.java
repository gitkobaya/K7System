package k7system.loaders;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import k7system.gpuobjects.VertexArrayObject;

/** 既存形式の3Dモデルを読み込みます */
public class StlLoader {

    /** STL形式にはアピアランスが存在しないため，形状データのみを読み込みます */
    public static VertexArrayObject loadVAOModel(File file){
        BufferedInputStream inputStream=null;
        VertexArrayObject vao=null;
        try{
            inputStream=new BufferedInputStream(new FileInputStream(file));
            vao=loadVAOModel(inputStream,file.length());
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }finally{
            try{
                inputStream.close();
            }catch(Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return vao;
    }

    /** ストリーム形式でSTLデータを読み込むメソッドです */
    public static VertexArrayObject loadVAOModel(InputStream reader,long length){
        VertexArrayObject vao=new VertexArrayObject();
        DataInputStream dis=new DataInputStream(reader);
        ByteBuffer temp=ByteBuffer.allocate(64);

        // 頂点配列
        List<Float> vtxArray=new ArrayList<Float>();

        // 法線配列
        List<Float> nrmArray=new ArrayList<Float>();

        try{
            // ヘッダーを読み飛ばす
            for (int i=0;i<80;i++){
                dis.readByte();
            }
            temp.rewind();
            temp.putInt(dis.readInt());
            int numOfPols=temp.order(ByteOrder.LITTLE_ENDIAN).getInt(0);

            for (int i=0;i<numOfPols;i++){
                temp.clear();
                temp.order(ByteOrder.LITTLE_ENDIAN);

                temp.putFloat(dis.readFloat()); // 法線X
                temp.putFloat(dis.readFloat()); // 法線Y
                temp.putFloat(dis.readFloat()); // 法線Z
                temp.putFloat(dis.readFloat()); // 座標1X
                temp.putFloat(dis.readFloat()); // 座標1Y
                temp.putFloat(dis.readFloat()); // 座標1Z
                temp.putFloat(dis.readFloat()); // 座標2X
                temp.putFloat(dis.readFloat()); // 座標2Y
                temp.putFloat(dis.readFloat()); // 座標2Z
                temp.putFloat(dis.readFloat()); // 座標3X
                temp.putFloat(dis.readFloat()); // 座標3Y
                temp.putFloat(dis.readFloat()); // 座標3Z
                int dummy=dis.readShort(); //16ビット読み込んで捨てる

                temp.order(ByteOrder.BIG_ENDIAN);

                temp.rewind();
                nrmArray.add(temp.getFloat(0));
                nrmArray.add(temp.getFloat(1*4));
                nrmArray.add(temp.getFloat(2*4));

                temp.rewind();
                nrmArray.add(temp.getFloat(0));
                nrmArray.add(temp.getFloat(1*4));
                nrmArray.add(temp.getFloat(2*4));

                temp.rewind();
                nrmArray.add(temp.getFloat(0));
                nrmArray.add(temp.getFloat(1*4));
                nrmArray.add(temp.getFloat(2*4));

                temp.rewind();
                vtxArray.add(temp.getFloat(3*4));
                vtxArray.add(temp.getFloat(4*4));
                vtxArray.add(temp.getFloat(5*4));
                vtxArray.add(temp.getFloat(6*4));
                vtxArray.add(temp.getFloat(7*4));
                vtxArray.add(temp.getFloat(8*4));
                vtxArray.add(temp.getFloat(9*4));
                vtxArray.add(temp.getFloat(10*4));
                vtxArray.add(temp.getFloat(11*4));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try{
                dis.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        float[] vertices=new float[vtxArray.size()];
        for (int i=0;i<vertices.length;i++){
            vertices[i]=vtxArray.get(i);
        }

        float[] normals=new float[nrmArray.size()];
        for (int i=0;i<normals.length;i++){
            normals[i]=nrmArray.get(i);
        }

        vao.setVertices(vertices);
        vao.setNormals(normals);

        return vao;
    }

}
