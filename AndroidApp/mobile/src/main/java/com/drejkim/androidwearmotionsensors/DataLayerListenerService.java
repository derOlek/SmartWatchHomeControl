package com.drejkim.androidwearmotionsensors;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import androidx.core.app.NotificationCompat;

public class DataLayerListenerService extends WearableListenerService {
    private AudioManager am;
    private static final String NOTIF_CHANNEL_NAME = "SensorService";
    private static final int NOTIF_CHANNEL_ID = 145;
    private static boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        setupForeground();
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        byte[] databytes = messageEvent.getData();
        //Log.d("Test", "Received (RAW): " + Arrays.toString(databytes));
        String data = new String(databytes, StandardCharsets.UTF_8);

        Log.d("Test", "Received: " + data);

        if(data.equals("Next")){
            if(am == null){
                am = (AudioManager) getSystemService(AUDIO_SERVICE);
            }


            if(am.isMusicActive()) {
                long eventtime = SystemClock.uptimeMillis() - 1;
                KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                am.dispatchMediaKeyEvent(downEvent);
                am.dispatchMediaKeyEvent(upEvent);
            }else{

            }
        }
    }

    private void setupForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationChannel chan = new NotificationChannel(NOTIF_CHANNEL_NAME, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);

        Notification notification = new NotificationCompat.Builder(this, NOTIF_CHANNEL_NAME)
                .setContentTitle("The sensor service is running")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setSmallIcon(android.R.color.transparent)
                .setContentIntent(pendingIntent)
                .build();

        // Notification ID cannot be 0.
        startForeground(NOTIF_CHANNEL_ID, notification);

    }

    public static boolean isRunning() {
        return isRunning;
    }
}