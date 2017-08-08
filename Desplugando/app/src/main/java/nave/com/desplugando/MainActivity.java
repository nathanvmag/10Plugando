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
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable{
        Handler h ;
    TextView fb,wpp,insta,twitter;
    Button usobt,voltar;
    RelativeLayout inicial,uso;
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
        twitter= (TextView)findViewById(R.id.tt);
        inicial =(RelativeLayout)findViewById(R.id.InicialLayout);
        uso = (RelativeLayout)findViewById(R.id.UsoLayout);
        usobt= (Button)findViewById(R.id.usoBt);
        inicial.setVisibility(View.VISIBLE);
        uso.setVisibility(View.INVISIBLE);
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
                inicial.setVisibility(View.VISIBLE);
                uso.setVisibility(View.INVISIBLE);
            }
        });
        super.onStart();
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

    @Override
    public void run() {
        SharedPreferences sp = getSharedPreferences("prefs", Activity.MODE_PRIVATE);

        if (fb!=null) {
            String fbb = CriadordeHorario(sp.getInt("facebook",0));
            fb.setText(fbb);
            String wppp = CriadordeHorario(sp.getInt("whatsapp",0));
            wpp.setText(wppp);
            String instaa = CriadordeHorario(sp.getInt("instagram",0));
            insta.setText(instaa);
            String twiiter = CriadordeHorario(sp.getInt("twitter",0));
            twitter.setText(twiiter);
        }

        h.postDelayed(this,1000);
    }
}
