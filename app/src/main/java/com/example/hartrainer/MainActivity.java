package com.example.hartrainer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hartrainer.services.AppService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    ScheduledThreadPoolExecutor startRecordExec = new ScheduledThreadPoolExecutor(1);
    ScheduledThreadPoolExecutor stopRecordExec = new ScheduledThreadPoolExecutor(1);

    String[] activities = {
            "Sitting",
            "Running",
            "Walking",
            "Jogging",
            "Upstairs Walking",
            "Downstairs Walking",
            "Sleeping"
    };

    String _selected_activity = null;
    String user_id = null;

    TextView _state;
    Button _record, _stop;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.initial);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
//
//        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        /// File sdcard = Environment.getExternalStorageDirectory();
//        file = new File(folder, "hartrainer.txt");
//
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            writer = new FileOutputStream(file);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Log.d("File PATH:", file.toString());

        Spinner _activities = (Spinner) findViewById(R.id.activities);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, activities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _activities.setAdapter(adapter);
        _activities.setOnItemSelectedListener(this);

        TextView _greeting = findViewById(R.id.greeting);

        String greeting = getIntent().getStringExtra("greeting");
        user_id = getIntent().getStringExtra("user_id");
        Log.i("User id main",user_id);

        _greeting.setText(greeting);

        _state = findViewById(R.id.state);
        _record = findViewById(R.id.record_button);
        _stop = findViewById(R.id.stop_button);

        _record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                _state.setText("Recording initiated");
                //Make sound
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.initiated);
                mediaPlayer.setLooping(false);
                mediaPlayer.start();

                long period = 15; // the period between successive executions
                ScheduledFuture d = startRecordExec.schedule(startRecording, period, TimeUnit.SECONDS);
            }
        });

        _stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                _state.setText("Stopped!");
                stopService(new Intent(MainActivity.this, AppService.class));
            }
        });

    }

    private Callable stopRecording = new Callable() {

        @Override
        public Object call() throws Exception {
            _state.setText("Auto Stopped!");
            stopService(new Intent(MainActivity.this, AppService.class));
            return null;
        }
    };

    private Callable startRecording = new Callable() {
        @Override
        public Object call() throws Exception {
            _state.setText("Recording...");

            String[] user_data = {user_id,_selected_activity};
            Intent intent = new Intent(MainActivity.this, AppService.class);
            intent.putExtra("user_data", user_data);

            startService(intent);

            long period; // the period between successive executions
            switch (_selected_activity){
                case "Sitting":
                    period = 40;
                    break;
                case "Running":
                    period = 60;
                    break;
                case "Jogging":
                    period = 40;
                    break;
                case "Upstairs Walking":
                    period = 40;
                    break;
                case "Downstairs Walking":
                    period = 40;
                    break;
                case "Sleeping":
                    period = 30;
                    break;
                default:
                    period = 1;
            }
            ScheduledFuture d = stopRecordExec.schedule(stopRecording, period, TimeUnit.SECONDS);

            return null;
        }
    };



//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        Log.d("Current Acce", Arrays.toString(a) );
//        float[] a = getCurrentAccelerometerValue(event);
//   }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//
//    }


//    public float []  getCurrentGyroscopeValue(SensorEvent  event){
//        // This timestep's delta rotation to be multiplied by the current rotation
//        // after computing it from the gyro sample data.
//        float[] axis = new float[3];
//        if (timestamp != 0) {
//            final float dT = (event.timestamp - timestamp) * NS2S;
//            // Axis of the rotation sample, not normalized yet.
//            axis[0] = event.values[0];
//            axis[1] = event.values[1];
//            axis[2] = event.values[2];
//
//            // Calculate the angular speed of the sample
//            float omegaMagnitude = (float) sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
//
//            // Normalize the rotation vector if it's big enough to get the axis
//            // (that is, EPSILON should represent your maximum allowable margin of error)
//            if (omegaMagnitude > EPSILON) {
//                axis[0] /= omegaMagnitude;
//                axis[1] /= omegaMagnitude;
//                axis[2] /= omegaMagnitude;
//            }
//        }
//        timestamp = event.timestamp;
//
//        return  axis;
//    }
//
//    public float [] getCurrentAccelerometerValue(SensorEvent  event){
//        // In this example, alpha is calculated as t / (t + dT),
//        // where t is the low-pass filter's time-constant and
//        // dT is the event delivery rate.
//        final float alpha = (float) 0.8;
//
//        // Isolate the force of gravity with the low-pass filter.
//        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
//        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
//        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
//
//        // Remove the gravity contribution with the high-pass filter.
//        linear_acceleration[0] = event.values[0] - gravity[0];
//        linear_acceleration[1] = event.values[1] - gravity[1];
//        linear_acceleration[2] = event.values[2] - gravity[2];
//
//        return linear_acceleration;
//    }

    //Dropdown
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        _selected_activity = activities[position];
        Toast.makeText(getApplicationContext(), "You Selected: "+ _selected_activity,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO - Custom Code
    }



//    public String isExternalStorageWritable(){
//        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
//           return  "false";
//        }
//        return  "true";
//    }
//
//    public String isExternalStorageReadable(){
//        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())){
//            return  "false";
//        }
//        return  "true";
//    }







}
