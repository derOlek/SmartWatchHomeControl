package com.drejkim.androidwearmotionsensors;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.core.app.NotificationCompat;

public class SensorService extends Service implements SensorEventListener {

    private final IBinder localBinder = new MyBinder();
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Vibrator vibrator;
    private AudioManager audioManager;

    private final int QUEUE_SIZE = 50;

    private CircularFifoQueue<Float> rotXAxis = new CircularFifoQueue<>(QUEUE_SIZE);
    private CircularFifoQueue<Float> rotYAxis = new CircularFifoQueue<>(QUEUE_SIZE);
    private CircularFifoQueue<Float> rotZAxis = new CircularFifoQueue<>(QUEUE_SIZE);

    private static boolean isRunning = false;

    private static final String NOTIF_CHANNEL_NAME = "SensorService";
    private static final int NOTIF_CHANNEL_ID = 145;

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return localBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            Log.d("Test", "Sensor: " + s.getName() + "   (" + s.getStringType() + ")");
        }

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        isRunning = true;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        setupForeground();


        return super.onStartCommand(intent, flags, startId);
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
                .setContentTitle("Sensor Service")
                .setContentText("The sensor service is running")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.preference_wrapped_icon)
                .setContentIntent(pendingIntent)
                .build();

        // Notification ID cannot be 0.
        startForeground(NOTIF_CHANNEL_ID, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        isRunning = false;
    }

    Float[] rotXArray = new Float[QUEUE_SIZE];
    Float[] rotYArray = new Float[QUEUE_SIZE];
    Float[] rotZArray = new Float[QUEUE_SIZE];
    int i = 0;
    int eventCounter = 0;
    long lastWakeEventTime = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        eventCounter++;
        //Log.d("Test", System.currentTimeMillis() + " -> " + sensorEvent.values[0] + ", " + sensorEvent.values[1] +  ", " + sensorEvent.values[2]);
        rotXAxis.add(sensorEvent.values[0]);
        rotYAxis.add(sensorEvent.values[1]);
        rotZAxis.add(sensorEvent.values[2]);

        if (rotXAxis.isAtFullCapacity() && eventCounter > 15) {
            eventCounter = 0;
            rotXAxis.toArray(rotXArray);
            rotYAxis.toArray(rotYArray);
            rotZAxis.toArray(rotZArray);

            /*float[] dRotX = new float[QUEUE_SIZE - 1];
            float[] dRotY = new float[QUEUE_SIZE - 1];
            float[] dRotZ = new float[QUEUE_SIZE - 1];
            for (i = 1; i < rotXArray.length; i++) {
                dRotX[i - 1] = simplifyValue(rotXArray[i]);
                dRotY[i - 1] = simplifyValue(rotYArray[i]);
                dRotZ[i - 1] = simplifyValue(rotZArray[i]);*/
                    /*dRotX[i-1] = simplifyValue(rotXArray[i] - rotXArray[0]);
                    dRotY[i-1] = simplifyValue(rotYArray[i] - rotYArray[0]);
                    dRotZ[i-1] = simplifyValue(rotZArray[i] - rotZArray[0]);*/
            /*}
            Log.d("Test", "++++++++++++++");
            Log.d("Test", Arrays.toString(dRotX));
            Log.d("Test", Arrays.toString(dRotY));
            Log.d("Test", Arrays.toString(dRotZ));*/

            boolean wristFlicked = PatternDetector.detectWristFlickUp(rotXArray, rotYArray, rotZArray);
            if (wristFlicked && lastWakeEventTime + 1000 < System.currentTimeMillis()) {
                lastWakeEventTime = System.currentTimeMillis();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));


                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            try {
                                MessagingHelper msgHelper = new MessagingHelper(SensorService.this);
                                Log.d("Test", "ID: " + msgHelper.transcriptionNodeId);
                                msgHelper.requestTranscription(SensorService.this, "Next");
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    t.start();


                } else {
                    Toast.makeText(this, "Vibration not possible", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private float simplifyValue(float value) {
        if (Math.abs(value) < 0.1) {
            value = 0.0f;
        } else {
            value = (float) (Math.floor(value * 100) / 100);  //Only for debugging
        }
        return value;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class MyBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }
}