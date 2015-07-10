package nu.geeks.shake;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Random;

public class ShakeActivity extends Activity implements SensorEventListener {

    static final String TAG = "ShakeActivity";

    ToggleButton skak;
    ImageView dice;
    SensorManager sensorManager;
    ArrayList<float[]> recentValues;
    TextView diceText;

    CountDownTimer animationTimer;
    CountDownTimer resetTimer;

    RelativeLayout screen;

    //En array med bilderna till alla tärningar.
    int[] diceValues = {
            R.drawable._1,
            R.drawable._2,
            R.drawable._3,
            R.drawable._4,
            R.drawable._5,
            R.drawable._6
    };

    //Den aktuella tärningen efter skaket. Börjar på 5.
    int diceValue = 5;

    //Sparar undan tärningens orginalposition så jag kan återställa den sen.
    float origX;
    float origY;

    float change = 125;

    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        //En text så vi ser vad tärningen blev.
        diceText = (TextView) findViewById(R.id.diceText);

        //Behöver något sätt att spara undan några gamla värden bara.
        recentValues = new ArrayList<float[]>();
        for(int i = 0; i < 10; i++ ) recentValues.add(new float[]{0, 0, 0});//Fyller arrayen med 0.

        //En på/av-knapp för skakningen.
        skak = (ToggleButton) findViewById(R.id.skakOnOff);

        //Tar fram själva relativeLayout-en också, vill veta var mitten på skärmen är.
        screen = (RelativeLayout) findViewById(R.id.layout);

        //Ställ in sensormanager och sätt den att titta på accelerometern.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);


        dice = (ImageView) findViewById(R.id.dice);

        Log.d(TAG, "x: " + origX + " y: " + origY);


        //Animera tärningen genom att använda en countdownTimer. Finns säkert andra bättre sätt,
        // men jag pallar inte googla. :)
        //Den kör i 5 sek (5000 ms) och uppdaterar bilden i 25fps (40 ggr per sek).
        //Deklarereas bara här, aktiveras genom att köra timer.start();
        animationTimer = new CountDownTimer(5001, 40) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Sätt rotationen till
                animateDice(dice.getRotation(), millisUntilFinished);
            }

            @Override
            public void onFinish() {

            }
        };



        //En till animation, som blinkar tärningen och återställer den när man klickar på knappen.
        resetTimer = new CountDownTimer(2500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(dice.getVisibility() == View.VISIBLE) dice.setVisibility(View.INVISIBLE);
                else if(dice.getVisibility() == View.INVISIBLE) dice.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {

            }
        };

        skak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(skak.isChecked()){
                    //Återställer tärningens position och kör igång timern som blinkar den.
                    dice.setX(screen.getWidth() / 2 - dice.getWidth() / 2);
                    dice.setY(screen.getHeight() / 2 - dice.getHeight() / 2);
                    emptyRecentValues();

                    dice.setRotation(0);
                    animationTimer.cancel();

                    resetTimer.start();
                }
            }
        });




    }

    private void emptyRecentValues() {
        for(int i = 0; i < 10; i++ ) recentValues.set(i, new float[]{0,0,0});
    }

    //Overrida den här och den under när vi implementar SensorEventListener.
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Vi kör bara om knappan är i på-läge.
        if(skak.isChecked()){
            addAndCheckArray(event.values);
        }

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
            Log.d(TAG, "SKAK! " + highValues);
            //Skaket är registrerat, slå av funktionen.
            skak.setChecked(false);
            //Töm arrayen
            emptyRecentValues();

            //Animera tärningen lite.
            startAnimation();
        }

    }

    private void startAnimation() {

        //Bilden kommer att uppdateras 125 ggr (5000 / 40 = 125).
        //Det kändes lagom att låta den snurra 37.5 grader första gången, sen mindre och mindre
        //varje gång, för att sista gången sluta på 0)

        change = 37.5f;
        animationTimer.start();

    }

    private void animateDice(float rotation, long millis) {

        dice.setRotation( ( dice.getRotation() + change ) );
        change -= 0.3; //Dra bort .3 från change. 0.3 * 125 = 37.5.
        //Log.d(TAG, ""+ dice.getRotation());

        Random rand = new Random();

        //Vi kan låta den studsa runt lite också.
        dice.setX(dice.getX() + (rand.nextInt(8) - 4) * (change / 20));
        dice.setY(dice.getY() + (rand.nextInt(8) - 4) * (change / 20));

        if(millis > 1000){
            //Om det är mer än en sekund kvar av animationen, byt sida på tärningen.
            int diceNumber = rand.nextInt(6);

            dice.setBackground(getResources().getDrawable(diceValues[diceNumber]));
            //Log.d(TAG, "Byter bild");

            diceValue = diceNumber + 1;
            diceText.setText("" + diceValue);
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    return false;
    }

    @Override
    protected void onPause() {
        //Upptäckte att appen fortsatte spy ut data även efter att den stängdes ner.
        //Bäst att förhindra det genom att avregistrera listenern.
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }
}
