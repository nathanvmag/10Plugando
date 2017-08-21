package nave.com.desplugando;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;




/**
 * Created by henrique.filho on 19/06/2017.
 */

public class ViciService extends Service implements Runnable  {
    boolean active = true;
    Handler h ;
    Timer t ;
    Calendar calendar;
    int day;
    int NotifyHour = 14;
    int resetHour=0;
    public  List <apptocheck> AppsList;
    long uptadatetime;
    String serialized ;
    private final LocalBinder connection = new LocalBinder();

    // ... do something ...

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
        debug("Criou o servico");
        active = true;
        calendar = Calendar.getInstance();
        day = (int)calendar.get(Calendar.DAY_OF_MONTH);
        uptadatetime= calendar.getTime().getTime();
        h= new Handler();
        h.post(this);
    }

    @Override
    public void run() {

         task();
        h.postDelayed(this,1000);
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

        return START_STICKY;
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();


    }
    private String printForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return currentApp;
    }

    void task()  {


        long estimatedTime = System.currentTimeMillis() - uptadatetime;
        uptadatetime=  System.currentTimeMillis();

        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (AppsList==null)
        {
            List<apptocheck> tempora= new ArrayList<>();
            serialized= sp.getString("apps",null);
            if (serialized!=null)
            {
                debug("Ira Deserializar o "+serialized);
            String[] classes = serialized.split("!");
            for (int i=0;i<classes.length;i++)
            {
                String[]a = classes[i].split("°");
                    tempora.add( new apptocheck(a[0], Integer.parseInt(a[1]),a[2]));
                }
            AppsList= tempora;
                   }
        }

        Date now = calendar.getTime();

        long passtime = now.getTime()- uptadatetime;
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
                    for (apptocheck aps:AppsList
                         ) {aps.twohournot= false;
                        aps.useTime=0;

                    }

                    Notify(R.drawable.r,"As estatisticas Foram resetadas","Veja quanto tempo já foi gasto nas redes sociais",0,MainActivity.class);
                }
            },1800000);
        }

      if (AppsList!=null){
        for(int i=0;i<AppsList.size();i++)
        {
            if (AppsList.get(i)!=null)
            {

                if (printForegroundTask().equals(AppsList.get(i).packagename)){
                    AppsList.get(i).useTime+= estimatedTime/1000;
                    debug("Ta usando o "+AppsList.get(i).packagename+ " " + AppsList.get(i).useTime);

                }
            }
        }
            if (AppsList!=null) {
                for (apptocheck aps : AppsList
                        ) {
                    if (aps.useTime > 7200 && !aps.twohournot) {
                        final PackageManager pm = getApplicationContext().getPackageManager();
                        ApplicationInfo ai;
                        try {
                            ai = pm.getApplicationInfo(aps.packagename, 0);
                        } catch (final PackageManager.NameNotFoundException e) {
                            ai = null;
                        }
                        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
                        Notify(R.drawable.r, "Voce está usando o " + applicationName + " demais", "Você ja passou 2 horas usando", 1, MainActivity.class);
                        aps.twohournot = true;
                    }
                }
            }

        }

       if (AppsList!=null) {
               String serializable= null;
               for (apptocheck ap:AppsList
                    ) {
                   serializable+=ap.getTxt()+"!";
               }
               if (serializable!=null){
           serializable= serializable.replace("null","");
          // debug(serializable);
          editor.putString("apps", serializable);
       }}
        editor.commit();
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



