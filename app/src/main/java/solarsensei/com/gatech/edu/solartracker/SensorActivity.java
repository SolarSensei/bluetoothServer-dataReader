package solarsensei.com.gatech.edu.solartracker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by timothybaba on 5/19/17.
 */
public class SensorActivity extends Activity implements SensorEventListener {

    private TextView mPressureView;
    private TextView mTempView;
    private TextView mLightView;
    private TextView mHumidityView;
    private  TextView mMagneticView;



    private SensorManager mSensorManager;

    //Environmental sensors
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private Sensor mRelativeHumidity;


    private Sensor mMagneticField;



    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mPressureView = (TextView) findViewById(R.id.pressureReading);
        mTempView = (TextView) findViewById(R.id.tempReading);
        mLightView = (TextView) findViewById(R.id.lightReading);
        mHumidityView = (TextView) findViewById(R.id.rHumidity);
        mMagneticView = (TextView) findViewById(R.id.magneticField);



        // Gets an instance of the sensor service, and uses that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        //To Do
        // Data gets sent to solar panels only when accuracy is high
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // Reads in sensor data.
        try {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_PRESSURE:
                    mPressureView.setText(String.valueOf(event.values[0]) + "mbar");
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    mTempView.setText(String.valueOf(event.values[0]) + "°C");
                    break;
                case Sensor.TYPE_LIGHT:
                    mLightView.setText(String.valueOf(event.values[0]) + "lx");
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    mHumidityView.setText(String.valueOf(event.values[0]) + "%");
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagneticView.setText(String.valueOf(event.values[0]) + "μT");
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    protected void onResume() {
        // Registers a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRelativeHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Unregisters the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}