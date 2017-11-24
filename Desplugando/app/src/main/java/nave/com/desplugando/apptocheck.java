package nave.com.desplugando;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import java.io.Serializable;

/**
 * Created by Nathan on 10/08/2017.
 */

public class apptocheck implements Serializable{

    public String packagename;
    public int useTime,dayuse;
    //public Drawable icon;
    public boolean twohournot,fourournot;
    public apptocheck(String pack,int time,String value,String value2,int dayusee)
    {
        packagename=pack;
        useTime=time;
        twohournot= Boolean.valueOf(value);
        fourournot= Boolean.valueOf(value2);
        dayuse= dayusee;

    }
    String getTxt()
    {
        return packagename+"°"+useTime+"°"+twohournot+"°"+fourournot+"°"+dayuse;
    }
}
