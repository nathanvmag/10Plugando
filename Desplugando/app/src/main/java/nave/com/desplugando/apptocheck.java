package nave.com.desplugando;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

/**
 * Created by Nathan on 10/08/2017.
 */

public class apptocheck {
    public String packagename;
    public int useTime;
    public Drawable icon;
    public boolean twohournot;
    public apptocheck(String pack,int time,Drawable resource)
    {
        packagename=pack;
        useTime=time;
        icon= resource;
    }
}
