package nave.com.desplugando;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements Runnable,ServiceConnection
{
    ViciService mService;
    final ServiceConnection mConnection = this;
    Intent intent;
    boolean mBound = false;
    LinearLayout ll ;
    Handler h ;
    TextView fb,wpp,insta,twitter;
    Button usobt,voltar,selectapp,removebt;
    RelativeLayout inicial,uso,modelo;
    List <apptocheck> AppsList;
    String PackToADD= null;

    boolean  done;

    String[] InitialApps = new String[] {"com.facebook.katana","com.whatsapp","com.twitter.android","com.instagram.android"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this,ViciService.class);
        ServiceStart();
        h= new Handler();
        h.post(this);
        done = false;
        ll = (LinearLayout) findViewById(R.id.ll);
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);

        String applist =sp.getString("apps",null);
        if (applist==null)
        {
        AppsList = new ArrayList<>() ;
        for (int i=0;i<InitialApps.length;i++)
        {
            if (isPackageExisted(InitialApps[i]))
            {
                Drawable icon = null;
                try {
                    icon = getPackageManager().getApplicationIcon(InitialApps[i]);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                AppsList.add(new apptocheck(InitialApps[i],0,"false"));
                addnewRelative(ll,icon,InitialApps[i],0);
            }
        }
            if (mBound)
            {   mService.AppsList= AppsList;
                debug("manda p la ");
            }

        }
        else {
            String serialized;
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
                for(int i=0;i<AppsList.size();i++)
                {
                    Drawable icon =null ;
                    try {
                     icon = getPackageManager().getApplicationIcon(AppsList.get(i).packagename);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    addnewRelative(ll,icon,AppsList.get(i).packagename,AppsList.get(i).useTime);
                }
            }

        }
        if (!isAccessGranted()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Você deve permitir que o aplicativo tenha acesso a os aplicativos em background para continar" )
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }


        }private boolean isAccessGranted() {

        if (Build.VERSION.SDK_INT>=19){
            try {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
        }
        return (mode == AppOpsManager.MODE_ALLOWED);

    } catch (PackageManager.NameNotFoundException e) {
        return false;
    }}
    return  true ;
}



    void debug(String s)
    {
        Log.d("vicio", s);
    }

    @Override
    protected void onStart() {


        removebt= (Button)findViewById(R.id.Remove);

        selectapp= (Button)findViewById(R.id.MonitorarApp);
        inicial =(RelativeLayout)findViewById(R.id.InicialLayout);
        uso = (RelativeLayout)findViewById(R.id.UsoLayout);
        usobt= (Button)findViewById(R.id.usoBt);
        inicial.setVisibility(View.INVISIBLE);
        uso.setVisibility(View.VISIBLE);
        usobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicial.setVisibility(View.INVISIBLE);
                uso.setVisibility(View.VISIBLE);
            }
        });
        voltar = (Button)findViewById(R.id.voltarBt);
        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inicial.setVisibility(View.INVISIBLE);
                uso.setVisibility(View.VISIBLE);
            }
        });
        selectapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent;

                appIntent=new Intent(view.getContext(),Chooser.class);
                startActivityForResult(appIntent, 1);
            }
        });
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cpicker, null);
        final Spinner sp= (Spinner) dialogView.findViewById(R.id.spinner);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (sp.getSelectedItem().toString()!=null)
                {
                    if (mBound){
                    String[] bala =getappspackage(mService.AppsList);
                    debug(bala[sp.getSelectedItemPosition()]);
                        apptocheck apptoremove= null;
                        for (apptocheck aps:mService.AppsList) {
                            if (aps.packagename.equals(bala[sp.getSelectedItemPosition()]))
                            {
                                apptoremove= aps;
                                break;
                            }

                        }
                        mService.AppsList.remove(apptoremove);
                        for(int z =0;z<ll.getChildCount();z++)
                        {

                            if (ll.getChildAt(z) instanceof RelativeLayout)
                            {
                                RelativeLayout rl =(RelativeLayout) ll.getChildAt(z);
                                for (int w =0;w<rl.getChildCount();w++)
                                {
                                    if (rl.getChildAt(w) instanceof TextView) {
                                        if (( rl.getChildAt(w)).getVisibility() == View.INVISIBLE) {
                                           if (((TextView)rl.getChildAt(w)).getText().equals(apptoremove.packagename))
                                           {
                                               ll.removeView(rl);
                                           }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).setCancelable(true).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog alertDialog = dialogBuilder.create();

        removebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                String[] bala =getAppsName(mService.AppsList);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, bala); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp.setAdapter(spinnerArrayAdapter);
                    if (bala.length!=0) {
                        alertDialog.show();
                    }
                    else Toast.makeText(getBaseContext(),"Você não pode remover as redes socias iniciais",Toast.LENGTH_LONG).show();
            }
            }
        });

        if (PackToADD!=null)
        {


            uso.setVisibility(View.VISIBLE);

        }

        super.onStart();
    }
    String[] getappspackage(List<apptocheck>l)
    {
        List<String >temp = new ArrayList<>();
        if (l!=null)
        {
            for (apptocheck app:l  ) {
                int counter=0;
                for(int i =0;i<InitialApps.length;i++) {
                    if (!app.packagename.equals(InitialApps[i])) {
                        counter++;

                    }
                }if (counter==4)
                {
                    temp.add(app.packagename);
                }


            }
        }
        return temp.toArray(new String[0]);
    }
    String[] getAppsName(List<apptocheck> l)
    {
        List<String >temp = new ArrayList<>();
        if (l!=null)
        {
            for (apptocheck app:l  ) {
                int counter=0;
               for(int i =0;i<InitialApps.length;i++)
               {
                  if (!app.packagename.equals(InitialApps[i]))
                   { counter++;
               }
               if (counter==4)
               {
                   PackageManager packageManager= getApplicationContext().getPackageManager();
                   String appName = null; try {
                   appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(app.packagename, PackageManager.GET_META_DATA));
               } catch (PackageManager.NameNotFoundException e) {
                   e.printStackTrace();
               }
                   temp.add(appName);

               }
               }

            }
        }
        return temp.toArray(new String[0]);
    }
    boolean checkIfisMonitoring(String s,List<apptocheck> list)

    {
        for (apptocheck ap:list
             ) {if (ap.packagename.equals( s))
            return true;
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if(null!=data){

            if(requestCode==1){
                //Do something
                String message=data.getStringExtra("MESSAGE_package_name");

               PackToADD= message;

            }
        }
    }
    String CriadordeHorario(int segundos)
    {
        int minutos=0,horas=0,segundos2 =0;
        if (segundos>=60)
        {
            minutos = segundos/60;
            segundos2= segundos%60;
            if (minutos>=60)
            {
                horas= minutos/60;
                minutos= minutos%60;
            }
        }
        else segundos2=segundos;

        return  horas+"h "+minutos+"min "+segundos2+"seg ";
    }
    void addnewRelative(LinearLayout tolayout,Drawable resID,String pkgname,int value)
    {

        RelativeLayout temp = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.tey,null);
        for(int i =0;i<temp.getChildCount();i++)
        {
            if (temp.getChildAt(i) instanceof TextView)
            {
               if(( (TextView) temp.getChildAt(i)).getVisibility() == View.INVISIBLE
                       )
                   (
                               (TextView) temp.getChildAt(i)).setText(pkgname);
                else ( (TextView) temp.getChildAt(i)).setText(CriadordeHorario(value));

            }
            else if (temp.getChildAt(i) instanceof ImageView)
            {
                ((ImageView) temp.getChildAt(i)).setImageDrawable(resID);
            }
        }
        tolayout.addView(temp,tolayout.getChildCount()-3);

    }
    public boolean isPackageExisted(String targetPackage){
        PackageManager pm=getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if (mBound&&PackToADD!=null)
        {


            if (!checkIfisMonitoring(PackToADD,mService.AppsList)){
                Drawable icon = null;
                try {
                    icon = getPackageManager().getApplicationIcon(PackToADD);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                mService.AppsList.add(new apptocheck(PackToADD,0,"false"));
                addnewRelative(ll,icon,PackToADD,0);
                PackToADD= null;
            }else {
                Toast.makeText(this,"Você já monitora esse App",Toast.LENGTH_LONG).show();
                PackToADD= null;
            }
        }
        if (mBound&&!done)
        {
            mService.AppsList= AppsList;
            done = true;
        }
        if (mBound)
        {

            WriteValues(ll, mService.AppsList);
        }
        h.postDelayed(this,1000);
    }
    void ServiceStart()
    {
        if(!isMyServiceRunning(ViciService.class))        {

            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
        else {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }
    void WriteValues(LinearLayout ll,List<apptocheck>Apps)
    {
        for(int i =0;i<ll.getChildCount();i++)
        {

            if (ll.getChildAt(i) instanceof RelativeLayout)
            {

                TextView packname= null, usetime=null;
                RelativeLayout rl =(RelativeLayout) ll.getChildAt(i);
                for (int z =0;z<rl.getChildCount();z++)
                {
                if (rl.getChildAt(z) instanceof TextView) {
                    if (( rl.getChildAt(z)).getVisibility() == View.INVISIBLE) {
                        packname = (TextView) rl.getChildAt(z);
                    } else usetime = (TextView) rl.getChildAt(z);
                }
              }

              if (packname.getText().toString()== FindWithPack(Apps,packname.getText().toString()).packagename)
              {

                  usetime.setText(CriadordeHorario( FindWithPack(Apps,packname.getText().toString()).useTime));
              }
              }

        }
    }
    apptocheck FindWithPack(List<apptocheck> aps,String pack)
    {
        for (apptocheck apss:aps    ) {
            if (apss.packagename == pack)return  apss;

        }
        return  null;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

        ViciService.LocalBinder binder = (ViciService.LocalBinder) service;
        mService = binder.getService();
        mBound = true;
        Log.d("bind service", "BINDDDDDDDDDDDD222222222222222222222222222");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mBound = false;
    }
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
