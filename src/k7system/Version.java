package k7system;

import java.util.HashMap;
import java.util.StringTokenizer;

/** 対象のOpenGLのバージョンをあらわすクラスです */
public class Version {

    int[] version=new int[3]; //major,minor,subminor
    HashMap<Feature, Boolean> availableFeature=new HashMap<Feature, Boolean>();

    /** コンストラクタです */
    public Version(String ver) {
        String[] versions=ver.split("\\.");

        try{
            for (int i=0;i<3;i++){
                if (versions[i].matches("^[0-9]+$")){
                    version[i]=Integer.valueOf(versions[i]);
                }else{
                    version[i]=0;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("debug:"+ver);
        }

        // 機能の判定
        if(this.newerThan(new int[]{1,5,0})){ // 1.5以上ならVBO有効
            availableFeature.put(Feature.VERTEX_BUFFER_OBJECT,true);
        }else{
            availableFeature.put(Feature.VERTEX_BUFFER_OBJECT,false);
        }
    }

    /** 機能を有しているかを判定します */
    public boolean isAvailable(Feature feat){
        return availableFeature.get(feat);
    }

    /** バージョンを配列で取得します */
    public int[] getVersion(){
        return version;
    }

    /** 現在のバージョンとの新旧判定を行います */
    public boolean newerThan(int[] ver){
        if(version[0]>ver[0]){
            return true;
        }else if(version[0]==ver[0] && version[1]>ver[1]){
            return true;
        }else if(version[0]==ver[0] && version[1]==ver[1] && version[2]>=ver[2]){
            return true;
        }

        return false;
    }
}
