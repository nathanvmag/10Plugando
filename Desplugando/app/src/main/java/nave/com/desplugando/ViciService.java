package nave.com.desplugando;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.security.acl.NotOwnerException;
import java.util.Collections;
import java.util.List;

import static android.R.id.list;


/**
 * Created by henrique.filho on 19/06/2017.
 */

public class ViciService extends Service implements Runnable  {
    boolean active = true;
    public int whatsapp,facebook,instagram,twitter;

    Handler h ;
    private final LocalBinder connection = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return connection;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        whatsapp = sp.getInt("whatsapp",0);
        facebook= sp.getInt("facebook",0);
        twitter = sp.getInt("twitter",0);
        instagram= sp.getInt("instagram",0);
        debug("Criou o servico");
        active = true;
        h= new Handler();
        h.post(this);

    }

    //This class returns to Activity the service reference.
    //With this reference is possible to get the Counter value and show to user.
    public class LocalBinder extends Binder
    {
        public ViciService getService() { return ViciService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Toast.makeText(this,"SERVICE SAMPLE onCreate()",Toast.LENGTH_SHORT).show();


        return START_STICKY;
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();


    }


    @Override
    public void run() {
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

      /*  List<ActivityManager.RunningAppProcessInfo> processes = AndroidProcesses.getRunningAppProcessInfo(getBaseContext());
        debug(processes.get(0).processName);
        for (ActivityManager.RunningAppProcessInfo am :processes
             ) {

           // debug(am.processName);

        }
*/
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        for(AndroidAppProcess ap : processes)
        {
            if (ap.getPackageName().equals("com.facebook.katana")&& ap.foreground&&ap.name.equals("com.facebook.katana")){
            // debug(ap.getPackageName() + "  "+ ap.foreground);
            facebook++;
        }
            if (ap.getPackageName().equals("com.whatsapp")&& ap.foreground&&ap.name.equals("com.whatsapp")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                whatsapp++;
            }
            if (ap.getPackageName().equals("com.twitter.android")&& ap.foreground&&ap.name.equals("com.twitter.android")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                twitter++;
            }
            if (ap.getPackageName().equals("com.instagram.android")&& ap.foreground&&ap.name.equals("com.instagram.android")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                instagram++;
            }

        }

        editor.putInt("whatsapp",whatsapp);
        editor.putInt("twitter",twitter);
        editor.putInt("facebook",facebook);
        editor.putInt("instagram",instagram);
        editor.commit();
        h.postDelayed(this,1000);
    }

    private void SetInterval()
    {
        try { Thread.sleep(1000); }
        catch(InterruptedException e) { e.printStackTrace(); }
    }
    void debug(String s)
    {
        Log.d("vicio", s);
    }

    void Notify(int icon,String title,String content,int id ,Class<?> serviceClass)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setLargeIcon( BitmapFactory.decodeResource(this.getResources(),icon))
                        .setVibrate(new long[]{100,100,100,100})
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentText(content);

        Intent resultIntent = new Intent(this, serviceClass);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(serviceClass);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }


    }



