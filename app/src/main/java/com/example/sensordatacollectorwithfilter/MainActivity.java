package com.example.sensordatacollectorwithfilter;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.wear.ambient.AmbientModeSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider, SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private Sensor gSensor, accelerometer, mAccel, mGyro, mLina, mMagno, mRot;
    private boolean isLinaPresent = false;
    private boolean isGyroPresent  = false;
    private boolean isMagnoPresent = false;
    private boolean isAccPresent = false;
    private boolean isRotPresent = false;
    //    private static final long START_TIME_IN_MILLIS= 60000; //60s
//    private static final long START_TIME_IN_MILLIS= 600000; //10m
    ImageView image;
    private TextView tdate;

    Button save, record;
    float x,y,z, x_gy, y_gy, z_gy, x_lin, y_lin, z_lin, x_magno, y_magno, z_magno;
    String x_rot,y_rot,z_rot,s_rot;
    double Mag_accel, Mag_gyro,Mag_lin,Mag_magnet;
    String activityInput;
    String DATA = "";
    String newline = "";
    String modified_DATA = "";
    String dateCurrent;
    String dateCurrentTemp = "";
    String x_val, y_val, z_val, xG_val, yG_val, zG_val, xL_val, yL_val, zL_val, xM_val, yM_val, zM_val, x_Mag, a_Mag, g_Mag, l_Mag, m_Mag;
    private FileWriter writer;

    Context context = this;
    File gpxfile;

    int CounterForSave = 0;
    int SamplingRate;
    private boolean permission_to_record = false;



    private ScalarKalmanFilter mFiltersCascade[] = new ScalarKalmanFilter[3];

    private CountDownTimer mCountDownTimer;

    private EditText Activity;
    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private boolean mTimerRunning;

    private static final long START_TIME_IN_MILLIS= 90000; //1.5m
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tdate = (TextView) findViewById(R.id.tdate) ;
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AmbientModeSupport.attach(this);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
//        mLina = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        mRot = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        mFiltersCascade[0] = new ScalarKalmanFilter(1, 1, 0.01f, 0.0025f);
        mFiltersCascade[1] = new ScalarKalmanFilter(1, 1, 0.01f, 0.0025f);
        mFiltersCascade[2] = new ScalarKalmanFilter(1, 1, 0.01f, 0.0025f);

        mButtonStartPause = findViewById(R.id.Record);
        mButtonReset = findViewById(R.id.Save);
        save = (Button) findViewById(R.id.Save);
        record = (Button) findViewById(R.id.Record);
        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.spinner1);
//        Spinner dropdown2 = findViewById(R.id.spinner2);
        Spinner dropdown3 = findViewById(R.id.activity);
        //create a list of items for the spinner.
        String[] items1 = new String[]{"20 dps", "25 dps", "30 dps"};
//        String[] items2 = new String[]{"1.5 min", "2 min", "3 min"};
        String[] items3 = new String[]{"walk", "stand", "jump","fall","breaststroke","backstroke","crawl","butterfly"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items1);
//        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items3);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
//        dropdown2.setAdapter(adapter2);
        dropdown3.setAdapter(adapter3);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        SamplingRate = 20;
                        break;
                    case 1:
                        SamplingRate = 25;
                        break;
                    case 2:
                        SamplingRate = 30;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                switch (position){
//                    case 0:
//                        START_TIME_IN_MILLIS = 90000;
//                        break;
//                    case 1:
//                        START_TIME_IN_MILLIS = 120000;
//                        break;
//                    case 2:
//                        START_TIME_IN_MILLIS = 180000;
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        dropdown3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                record.setEnabled(!activityInput.isEmpty());
                switch (position){
                    case 0:
                        activityInput = "walk";
                        break;
                    case 1:
                        activityInput = "stand";
                        break;
                    case 2:
                        activityInput = "jump";
                        break;
                    case 3:
                        activityInput = "fall";
                        break;
                    case 4:
                        activityInput = "breaststroke";
                        break;
                    case 5:
                        activityInput = "backstroke";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        updateCountDownText();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission_to_record = true;
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                vibrator.vibrate(500);
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                    Toast.makeText(MainActivity.this, "Start Recording...", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Touch Screen Disabled", Toast.LENGTH_SHORT).show();
                    record.setBackgroundColor(Color.RED);
                }


            }

        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
                record.setBackgroundColor(Color.DKGRAY);

            }
        });
    }
//    private TextWatcher activityTextWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            activityInput = Activity.getText().toString();
//
//            record.setEnabled(!activityInput.isEmpty());
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//
//        }
//    };

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();

            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                permission_to_record = false;

                save.setBackgroundColor(Color.GREEN);
                record.setBackgroundColor(Color.DKGRAY);
                Toast.makeText(MainActivity.this, "File Created & Saved", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Touch screen enabled", Toast.LENGTH_SHORT).show();
                // File management
                long date2 = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH-mm-ss");
                String dateString = sdf.format(date2);
                resetTimer();

                File folder = context.getExternalFilesDir("/storage");
                gpxfile = new File(folder, "SmartWatch"+dateString+"_"+activityInput+".csv");
                try {
                    writer = new FileWriter(gpxfile);

                    String line = "DATE,TIME,ax,ay,az,gx,gy,gz,ma,mg,label\n";
                    writer.write(line);

                    writer.write(modified_DATA);
                    writer.close();
                }
                catch(FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "File Not Found!", Toast.LENGTH_SHORT).show();
                }
                catch (IOException e) {
                    e.printStackTrace(); Toast.makeText(MainActivity.this, "Error saving!", Toast.LENGTH_SHORT).show();
                }

                vibrator.vibrate(1500);
                Activity.getText().clear();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                mButtonStartPause.setText("Record");
                mButtonStartPause.setVisibility(View.INVISIBLE);
                mButtonReset.setVisibility(View.VISIBLE);

            }
        }.start();
        mTimerRunning = true;
        mButtonStartPause.setText("pause");
        mButtonReset.setVisibility(View.INVISIBLE);
    }
    private void pauseTimer() {
        mCountDownTimer.cancel();
        onPause();
        mTimerRunning = false;
        mButtonStartPause.setText("Start");
        mButtonReset.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        mButtonReset.setVisibility(View.INVISIBLE);
        mButtonStartPause.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_STEM_1:
                Toast.makeText(this, "Touch screen enabled", Toast.LENGTH_SHORT).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                return true;


        }

        return super.onKeyDown(keyCode, event);

    }
    @Override
    protected void onResume() {
        super.onResume();

        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            isAccPresent = true;
        }
        if (mGyro != null) {
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
            isGyroPresent = true;
        }
//        if (mRot != null) {
//            mSensorManager.registerListener(this, mRot, SensorManager.SENSOR_DELAY_NORMAL);
//            isRotPresent = true;
//        }
//        if (mLina != null) {
//            mSensorManager.registerListener(this, mLina, SensorManager.SENSOR_DELAY_NORMAL);
//            isLinaPresent = true;
//        }
    }
    @Override
    protected void onPause() {
        super.onPause();

//        if(isLinaPresent) {
//            mSensorManager.unregisterListener(this, mLina);
//        }
        if(isAccPresent) {
            mSensorManager.unregisterListener(this, accelerometer);
        }
        if(isGyroPresent) {
            mSensorManager.unregisterListener(this, mGyro);
        }
//        if(isRotPresent) {
//            mSensorManager.unregisterListener(this, mRot);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Smoothes the signal from accelerometer
     */
    private float filter(float measurement){
        float f1 = mFiltersCascade[0].correct(measurement);
        float f2 = mFiltersCascade[1].correct(f1);
        float f3 = mFiltersCascade[2].correct(f2);
        return f3;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
        dateCurrent = sdf.format(date);
        tdate.setText(dateCurrent);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

//            x = filter(event.values[0]);
//            y = filter(event.values[1]);
//            z = filter(event.values[2]);
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            x_val = String.valueOf(x);
            y_val = String.valueOf(y);
            z_val = String.valueOf(z);

            Mag_accel = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
            a_Mag = String.valueOf(Mag_accel);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

//            x_gy = filter(event.values[0]);
//            y_gy = filter(event.values[1]);
//            z_gy = filter(event.values[2]);

            x_gy = event.values[0];
            y_gy = event.values[1];
            z_gy = event.values[2];

            xG_val = String.valueOf(x_gy);
            yG_val = String.valueOf(y_gy);
            zG_val = String.valueOf(z_gy);

            Mag_gyro = Math.sqrt(Math.pow(x_gy,2)+Math.pow(y_gy,2)+Math.pow(z_gy,2));
            g_Mag = String.valueOf(Mag_gyro);

        }
//        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
//
//            x_lin = event.values[0];
//            y_lin = event.values[1];
//            z_lin = event.values[2];
//
//            xL_val = String.valueOf(event.values[0]);
//            yL_val = String.valueOf(event.values[1]);
//            zL_val = String.valueOf(event.values[2]);
////            Mag_lin = Math.sqrt(Math.pow(x_lin,2)+Math.pow(y_lin,2)+Math.pow(z_lin,2));
////            l_Mag = String.valueOf(Mag_lin);
//
//        }
//        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//
//            x_magno = event.values[0];
//            y_magno = event.values[1];
//            z_magno = event.values[2];
//            xM_val = String.valueOf(event.values[0]);
//            yM_val = String.valueOf(event.values[1]);
//            zM_val = String.valueOf(event.values[2]);
//            Mag_magnet = Math.sqrt(Math.pow(x_magno,2)+Math.pow(y_magno,2)+Math.pow(z_magno,2));
//            m_Mag = String.valueOf(Mag_magnet);
//        }
//        if(event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
//            x_rot = String.valueOf(event.values[0]);
//            y_rot = String.valueOf(event.values[1]);
//            z_rot = String.valueOf(event.values[2]);
//            s_rot = String.valueOf(event.values[3]);
//        }
        if (!dateCurrentTemp.equals(dateCurrent)){
            dateCurrentTemp = dateCurrent;
            CounterForSave = 0;
        }
        if (CounterForSave<SamplingRate & permission_to_record) {   DATA = dateCurrent + "," + x_val+ "," + y_val + "," + z_val + "," + xG_val +","+yG_val+","+zG_val+","+ a_Mag +","+g_Mag+","+activityInput+"\n"; //"DATE,TIME,WALK,JUMP,STATIC,FALLDOWN\n"

            modified_DATA = newline + DATA;
            newline = modified_DATA;
            CounterForSave = CounterForSave +1;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No op.
    }


    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    /** Customizes appearance for Ambient mode. (We don't do anything minus default.) */
    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /** Prepares the UI for ambient mode. */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);
        }

        /**
         * Updates the display in ambient mode on the standard interval. Since we're using a custom
         * refresh cycle, this method does NOT update the data in the display. Rather, this method
         * simply updates the positioning of the data in the screen to avoid burn-in, if the display
         * requires it.
         */
        @Override
        public void onUpdateAmbient() {
            super.onUpdateAmbient();
        }

        /** Restores the UI to active (non-ambient) mode. */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
        }
    }
}