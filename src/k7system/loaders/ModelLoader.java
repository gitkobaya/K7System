package k7system.loaders;

import java.io.File;
import java.io.FileInputStream;

import k7system.Model3D;

/** 3Dモデルを読み込む抽象クラスです */
public abstract class ModelLoader {

    /** 3Dモデルをファイルから読み込むメソッドです */
    public Model3D loadModel(String filename){
        return this.loadModel(new File(filename));
    }


    /** 3Dモデルをファイルから読み込むメソッドです */
    public Model3D loadModel(File file){
        Model3D model=null;
        FileInputStream fis=null;

        try{
            fis=new FileInputStream(file);
            long fileSize=file.length();
            byte[] data=new byte[(int)fileSize];
            fis.read(data);

            System.out.println("fileName:"+file.getAbsolutePath()+" path:"+file.getParent());
            String path=file.getParent(); // ファイルのパスです
            model=this.loadModel(data,path);
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(fis!=null){
                try{
                    fis.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        return model;
    }

    /** 3Dモデルをメモリイメージから読み込むメソッドです */
    public abstract Model3D loadModel(byte[] data,String path);
}
