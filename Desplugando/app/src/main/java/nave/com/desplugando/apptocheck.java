package nave.com.desplugando;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import java.io.Serializable;

/**
 * Created by Nathan on 10/08/2017.
 */

public class apptocheck implements Serializable{

    public String packagename;
    public int useTime;
    //public Drawable icon;
    public boolean twohournot;
    public apptocheck(String pack,int time,String value)
    {
        packagename=pack;
        useTime=time;
        twohournot= Boolean.valueOf(value);

    }
    String getTxt()
    {
        return packagename+"°"+useTime+"°"+twohournot;
    }
}
