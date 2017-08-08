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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.R.id.list;


/**
 * Created by henrique.filho on 19/06/2017.
 */

public class ViciService extends Service  {
    boolean active = true;
    public int whatsapp,facebook,instagram,twitter;

    Handler h ;
    Timer t ;
    Calendar calendar;
    int day;
    int NotifyHour = 14;
    int resetHour=0;
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
        NotifyHour=sp.getInt("hora",14);
        whatsapp = sp.getInt("whatsapp",0);
        facebook= sp.getInt("facebook",0);
        twitter = sp.getInt("twitter",0);
        instagram= sp.getInt("instagram",0);
        debug("Criou o servico");
        active = true;
        calendar = Calendar.getInstance();
        day = (int)calendar.get(Calendar.DAY_OF_MONTH);

        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                 task();

            }
        }, 0, 1, TimeUnit.SECONDS);


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
    void task()
    {
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        debug("1 seg");
        if (calendar.get(Calendar.HOUR_OF_DAY) == NotifyHour)
        {
            if (NotifyHour==14){
                NotifyHour=21;
                editor.putInt("hora",NotifyHour);
            }

            else if (NotifyHour==21) {
            NotifyHour = 14;
                editor.putInt("hora",NotifyHour);
        }
            Notify(R.drawable.r,"Veja o uso nas redes","Veja quanto tempo já foi gasto nas redes",0,MainActivity.class);
        }
        if (calendar.get(Calendar.DAY_OF_MONTH)!=day&&calendar.get(Calendar.HOUR_OF_DAY)==resetHour)
        {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            Notify(R.drawable.r,"As estatisticas serão resetadas","Veja quanto tempo já foi gasto nas redes sociais",0,MainActivity.class);
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    whatsapp=0;
                    facebook=0;
                    twitter=0;
                    instagram=0;
                    Notify(R.drawable.r,"As estatisticas Foram resetadas","Veja quanto tempo já foi gasto nas redes sociais",0,MainActivity.class);
                }
            },1800000);
        }

        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        for(AndroidAppProcess ap : processes)
        {
            if (ap.getPackageName().equals("com.facebook.katana")&& ap.foreground&&ap.name.equals("com.facebook.katana")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                facebook+=5;
            }
            if (ap.getPackageName().equals("com.whatsapp")&& ap.foreground&&ap.name.equals("com.whatsapp")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                whatsapp+=5;
            }
            if (ap.getPackageName().equals("com.twitter.android")&& ap.foreground&&ap.name.equals("com.twitter.android")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                twitter+=5;
            }
            if (ap.getPackageName().equals("com.instagram.android")&& ap.foreground&&ap.name.equals("com.instagram.android")){
                // debug(ap.getPackageName() + "  "+ ap.foreground);
                instagram+=5;
            }
             int[] temp = new int[]{facebook,whatsapp,twitter,instagram};
            String[]temptx = new String[]{"facebook","whatsapp","twitter","instagram"};
            for (int i = 0;i<temp.length;i++){
            if (temp[i]==7200)
            {
                Notify(R.drawable.r,"Voce está usando o "+temptx[i]+" demais","Você ja passou 2 horas usando",1,MainActivity.class);
            }

        }

        editor.putInt("whatsapp",whatsapp);
        editor.putInt("twitter",twitter);
        editor.putInt("facebook",facebook);
        editor.putInt("instagram",instagram);
        editor.commit();
    }
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



