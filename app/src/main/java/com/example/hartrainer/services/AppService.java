package com.example.hartrainer.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.example.hartrainer.MainActivity;
import com.example.hartrainer.R;
import com.example.hartrainer.SensorCal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;


public class AppService extends Service implements SensorEventListener {


    public int dav_ser_id;

    private SensorManager mSensorManager = null;

    public FileOutputStream accel_stream = null;
    public FileOutputStream gyro_stream = null;

    String accel_sensor_values = "";
    String gyro_sensor_values = "";

    int counter = 0;


    // private SensorEventListener sensor_listener = null;

    //LIFE HAS ONLY ONE INK DON'T WASTE THE INK BECAUSE YOU HAVE MANY PAPER
    //LIFE IS LIKE A VAPOUR, IT VINISHES INTO THE AIR
    //LIFE IS LIKE A SOFTWARE THERE WILL ALWAY BE BUG, ALWAY USE GOD'S UNIT TEST BEFORE ROLLOUT
    //LIFE IS LIKE  SMART PHONE, ONLY USE YOUR BATTERY FOR PROFITABLE THINGS, THINGS THAT WHEN YOU'RE
    //DISCOUNTED FROM THE INTERNET YOU CAN STILL WATCH

    SensorCal sensorCal = null;

    String[] user_data = null;

    public class ServiceBinder extends Binder {
        public AppService getService(){
            return AppService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorCal = new SensorCal();
        mSensorManager = (SensorManager) AppService.this.getSystemService(SENSOR_SERVICE);
        initListeners();

        user_data = intent.getStringArrayExtra("user_data");

        //accelarometer data storing
        File accel_path = new File(AppService.this.getFilesDir(),"accel");
        if (!accel_path.exists()) {
            accel_path.mkdir();
        }
        File accel_file = new File(accel_path, "data_"+user_data[0]+"_accel_phone.txt");

        //gyroscope data storing
        File gyro_path = new File(AppService.this.getFilesDir(),"gyro");
        if (!gyro_path.exists()) {
            gyro_path.mkdir();
        }
        File gyro_file = new File(gyro_path, "data_"+user_data[0]+"_gyro_phone.txt");
        try {
            accel_stream = new FileOutputStream(accel_file,true);
            gyro_stream = new FileOutputStream(gyro_file,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//      Thread thread = new Thread(new SensorThread(startId));
//      thread.start();


        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] gyroscope = new float[3];
        float[] acc_mag = new float[3];
        float[] accel = new float[3];
        switch(event.sensor.getType()) {
            case android.hardware.Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array
                // then calculate new orientation
                //System.arraycopy(event.values, 0, accel, 0, 3);
                accel[0] = event.values[0];
                accel[1] = event.values[1];
                accel[2] = event.values[2];

                //acc_mag = sensorCal.calculateAccMagOrientation();
                //sensorCal.accel = accel;

                //Write to file
                accel_sensor_values += user_data[0]+","+user_data[1]+","+event.timestamp+",";
                accel_sensor_values += accel[0]+","+accel[1]+","+accel[2]+";\n";
                //System.out.println(counter++);


                try {
                    accel_stream.write(accel_sensor_values.getBytes());
                    accel_stream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case android.hardware.Sensor.TYPE_GYROSCOPE:
                //Write to file
                gyroscope[0] = event.values[0];
                gyroscope[1] = event.values[1];
                gyroscope[2] = event.values[2];

                long gyro_timeInMillis = (new Date()).getTime()
                        + (event.timestamp - System.nanoTime()) / 1000000L;

                gyro_sensor_values += user_data[0]+","+user_data[1]+","+event.timestamp+",";
                gyro_sensor_values += gyroscope[0]+","+gyroscope[1]+","+gyroscope[2]+";\n";
                //System.out.println(sensor_values);

                try {
                    gyro_stream.write(gyro_sensor_values.getBytes());
                    gyro_stream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //                process gyro data from optimized function
//                gyroscope = sensorCal.gyroFunction(event);
//                if(sensorCal.accMagOrientation != null){
//                    //Write to file
//                    long gyro_timeInMillis = (new Date()).getTime()
//                            + (event.timestamp - System.nanoTime()) / 1000000L;
//
//                    gyro_sensor_values += user_data[0]+","+user_data[1]+","+event.timestamp+",";
//                    gyro_sensor_values += gyroscope[0]+","+gyroscope[1]+","+gyroscope[2]+";\n";
//                    //System.out.println(sensor_values);
//
//                    try {
//                        gyro_stream.write(gyro_sensor_values.getBytes());
//                        gyro_stream.flush();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }


                //end write to file
                break;

            case android.hardware.Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                //System.arraycopy(event.values, 0, sensorCal.magnet, 0, 3);
                break;
        }

//        HashMap<String,String> sensor_data = new HashMap<>();
//        sensor_data.put("Accelerometer1)", Arrays.toString(sensorCal.accel));
//        sensor_data.put("AccMagOrientation)",Arrays.toString(acc_mag));
//        sensor_data.put("Gyroscope)",Arrays.toString(gyroscope));
//        String values = sensor_data.toString();
//        System.out.println("Sensor:"+ values);


    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.stop);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();

        try {
            accel_stream.close();
            gyro_stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initListeners() {
        mSensorManager.registerListener( this ,
                mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void createNotification() {
       //TODO: Display Notification(HARTRAINER IS CURRENTLY RUNNING)

    }

//    final class SensorThread implements Runnable{
//        int service_id;
//
//        public SensorThread(int service_id){
//            this.service_id = service_id;
//        }
//
//        @Override
//        public void run() {
//            System.out.println("This is service id: "+dav_ser_id);
//            System.out.println("This is service Main id: "+this.service_id);
//
//            stopSelf(this.service_id);
//        }
//    }



//    public void saveFile(){
//
//        System.out.println("On Paused Called");
//
//        //SET THE SEPERATOR
//        easyCsv.setSeparatorColumn("*");
//        easyCsv.setSeperatorLine("$");
//
//        /**
//         * SAVE CSV FILE
//         * @param fileName Name of the file to be created
//         * @param WRITE_PERMISSON_REQUEST_CODE EasyCsv request runtime permission for Write permission to user. When user "Accept" or "Decline" for you can handler
//         */
//        easyCsv.createCsvFile("hartrainer", headerList, dataList,WRITE_PERMISSON_REQUEST_CODE, new FileCallback() {
//            @Override
//            public void onSuccess(File file) {
//                System.out.println(file.getAbsolutePath()+" Successfully Saved");
//            }
//
//            @Override
//            public void onFail(String err) {
//                System.out.println("An Error Occur while saving file:"+ err);
//            }
//        });
    }







