package solarsensei.com.gatech.edu.solartracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by timothybaba on 5/19/17.
 */
public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private TextView mPressureView;
    private TextView mTempView;
    private TextView mLightView;
    private TextView mHumidityView;
    private TextView mMagneticView;
    private TextView azimuthView;
    private TextView pitchView;
    private TextView rollView;
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationValues = new float[3];

    private SensorManager mSensorManager;

    //Environmental sensors
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private Sensor mRelativeHumidity;
    private Sensor mMagneticField;

    //motion sensors
    private Sensor mRotation;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mPressureView = (TextView) findViewById(R.id.pressureReading);
        mTempView = (TextView) findViewById(R.id.tempReading);
        mLightView = (TextView) findViewById(R.id.lightReading);
        mHumidityView = (TextView) findViewById(R.id.rHumidity);
        mMagneticView = (TextView) findViewById(R.id.magneticField);
        azimuthView = (TextView) findViewById(R.id.azimuth);
        pitchView = (TextView) findViewById(R.id.pitch);
        rollView = (TextView) findViewById(R.id.roll);


        // Gets an instance of the sensor service, and uses that to get an instance of
        // a particular sensor.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mRelativeHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
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
                    mPressureView.setText(String.format(getString(R.string.displayResult), event.values[0], "mbars"));
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    mTempView.setText(String.format(getString(R.string.displayResult), event.values[0], "°C"));
                    break;
                case Sensor.TYPE_LIGHT:
                    mLightView.setText(String.format(getString(R.string.displayResult), event.values[0], "°lx"));
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    mHumidityView.setText(String.format(getString(R.string.displayResult), event.values[0], "%"));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagneticView.setText(String.format(getString(R.string.displayResult), event.values[0], "μT"));
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    SensorManager.getOrientation(mRotationMatrix, mOrientationValues);
                    azimuthView.setText(String.format(getString(R.string.displayResult), mOrientationValues[0], "°"));
                    pitchView.setText(String.format(getString(R.string.displayResult), mOrientationValues[1], "°"));
                    rollView.setText(String.format(getString(R.string.displayResult), mOrientationValues[2], "°"));
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
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Unregisters the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}