package name.domain.com.blemuseumprot1;

import android.content.res.TypedArray;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ayush Agarwal on 10/13/2016.
 */
public class Artifact {
    String uuid,major,minor;
    double curdist;
    String descrip;
    //Bitmap img;
    List<Integer> imgs;
    Artifact(String uid,String maj,String min)
    {
        uuid=uid;
        major=maj;
        min=minor;
        //Assign Objects
    }
    void setCurdist(double d)
    {
        curdist=d;
    }

    public void setImgs(List<Integer> imgs) {
        this.imgs = imgs;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

}
