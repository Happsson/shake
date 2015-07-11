package nu.geeks.shake;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Created by Ali on 15-07-10.
 *
 * The shakeDetector is able to detect when the user shakes the device.
 *
 * It looks at the accelerometer on all three axis.
 * It the device is shaken, the dv/dt will be high while the shake is taking place.
 *
 * This looks at the dv/dt over 10 readings at a time, if at least five of those
 * readings are to be considered high, the devices is considered shaken.
 *
 * The values are stored in an Arraylist<float[]>. The data type here doesn't really
 * matter, since we will always use a fixed size of 10, and always overwrite old values at a known
 * index. We will iterate through them for every added value, but there will never be more than 10.
 *
 */

public class ShakeDetect implements SensorEventListener {

    private ArrayList<float[]> recentValues;
    private SensorManager manager;
    private boolean shakeEnabled;
    private int index;
    private boolean isEnabled;
    private boolean shakeDetected;

    /**
     * Create a new ShakeDetector. Needs the context. Just pass 'this' from the active activity.
     *
     * @param context
     */
    public ShakeDetect(Context context){
        shakeEnabled = false;
        isEnabled = true;

        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        shakeDetected = false;

        index = 0;

        recentValues = new ArrayList<float[]>();
        for(int i = 0; i < 10; i++ ) recentValues.add(new float[]{0, 0, 0});
    }

    public void unregisterListener(){
        manager.unregisterListener(this);
        isEnabled = false;
    }

    public void enableShakeDetector(){
        shakeEnabled = true;
    }

    public void disableShakeDetector(){
        shakeEnabled = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(shakeEnabled){
            addAndCheckArray(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void addAndCheckArray(float[] values) {
        //Vi håller 10 senaste värden i arrayen ba.
        recentValues.set(index++ % 10, values);

        //Fullösning här, men vi vill inte att Int-en når maxvalue.
        //Vet inte om det behövs alls egentligen.
        if(index == Integer.MAX_VALUE -1) index = 0;

        //Räkna hur många värden i arrayen som är höga.
        int highValues = 0;

        for(float[] vals : recentValues){

            //Något av värdena kommer alltid att vara som minst runt 9.82 (gravitationsaccelerationen),
            // beroende på hur mobilen är vinklad för tillfället.

            if(vals[0] > 15 || vals[0] < -15) highValues++;
            if(vals[1] > 15 || vals[1] < -15) highValues++;
            if(vals[2] > 15 || vals[2] < -15) highValues++;

            //Log.d(TAG, "[0]: " + vals[0] + "[1]: " + vals[1] + "[2]: " + vals[2]);
        }

        //Om nog många av värdena i arrayen är höga (alltså att enheten har haft en accerlation nog
        // mycket under senaste tiden), räknar vi det som ett skak.
        if(highValues > 5) {
            //Shake is registered, turn of the shakeDetector.
            shakeEnabled = false;
            //Töm arrayen
            for(int i = 0; i < 10; i++) recentValues.set(i, new float[]{0,0,0});
            shakeDetected = true;
        }

    }

    /**
     * Returns true if shake is detected. Will at the same time set shake detected to false again,
     * since this shake is expected to be handled before checking again.
     * @return whether or not shake has been detected.
     */
    public boolean isShakeDetected(){
        if(shakeDetected){
            shakeDetected = false;
            return true;
        }else{
            return false;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
