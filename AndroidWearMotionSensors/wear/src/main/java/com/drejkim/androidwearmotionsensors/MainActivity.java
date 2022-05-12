package com.drejkim.androidwearmotionsensors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class MainActivity extends Activity {

    Button btn_startService;
    static Intent sensorServiceIntent;
    public static final String wakelockTag = "SensorService::MyWakelockTag";
    public static PowerManager.WakeLock wakeLock;

    private final View.OnClickListener startServiceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(MainActivity.this, "Started Service", Toast.LENGTH_SHORT).show();
            startForegroundService(sensorServiceIntent);

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateStartButton();
                }
            }, 100);

        }
    };
    private final View.OnClickListener stopServiceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(MainActivity.this, "Stop Service", Toast.LENGTH_SHORT).show();
            stopService(sensorServiceIntent);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateStartButton();
                }
            }, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btn_startService = findViewById(R.id.btn_startService);
        sensorServiceIntent = new Intent(MainActivity.this, SensorService.class);


        updateStartButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStartButton();
    }

    public void updateStartButton(){
        if(btn_startService == null){
            btn_startService = findViewById(R.id.btn_startService);
        }

        if(SensorService.isRunning()){
            btn_startService.setText("Stop Service");
            btn_startService.setOnClickListener(stopServiceClickListener);
            releaseWakelock();
        }else{
            btn_startService.setText("Start Service");
            btn_startService.setOnClickListener(startServiceClickListener);
            aquireWakelock();
        }
    }


    public void aquireWakelock(){
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,wakelockTag);
        wakeLock.acquire();
    }

    public void releaseWakelock(){
        if(wakeLock != null){
            wakeLock.release();
        }
    }
}