package nave.com.desplugando;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable{
        Handler h ;
    TextView fb,wpp,insta,twitter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        startService(new Intent(this,ViciService.class));
        h= new Handler();
        h.post(this);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {

        fb= (TextView)findViewById(R.id.fb);
        wpp= (TextView)findViewById(R.id.wpp);
        insta =(TextView)findViewById(R.id.insta);
        twitter= (TextView)findViewById(R.id.twitter);
        super.onStart();
    }

    @Override
    public void run() {
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);
        String fbb = "Facebook TIme "+ String.valueOf(sp.getInt("facebook",0));
        if (fb!=null) {
            fb.setText(fbb);
            String wppp = "Whatsapp Time " + sp.getInt("whatsapp", 0);
            wpp.setText(wppp);
            String instaa = "Instagram Time " + sp.getInt("instagram", 0);
            insta.setText(instaa);
            String twiiter = "Twitter Time " + sp.getInt("twitter", 0);
            twitter.setText(twiiter);
        }

        h.postDelayed(this,1000);
    }
}
