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

public class ShakeActivity extends Activity {

    static final String TAG = "ShakeActivity";

    private ToggleButton skak;

    private ImageView dices[] = new ImageView[6];

    private RelativeLayout screen;
    private DiceAnimator[] diceAnimators; //En diceAnimator per tärning.
    private ShakeDetect shakeDetector;

    //Offset används för att placera ut tärningarna när man vill kasta igen.
    private int[][] dicesOffsets = {
            {-100, -50},
            {0, -50},
            {100, -50},
            {-100, 50},
            {0, 50},
            {100, 50}
    };

    //Grafiken på tärningarna.
    int[] diceDrawables = {
                R.drawable._1,
                R.drawable._2,
                R.drawable._3,
                R.drawable._4,
                R.drawable._5,
                R.drawable._6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        //En på/av-knapp för skakningen.
        skak = (ToggleButton) findViewById(R.id.skakOnOff);

        //Initierar alla tärningar
        dices[0] =  (ImageView) findViewById(R.id.dice1);
        dices[1] =  (ImageView) findViewById(R.id.dice2);
        dices[2] =  (ImageView) findViewById(R.id.dice3);
        dices[3] =  (ImageView) findViewById(R.id.dice4);
        dices[4] =  (ImageView) findViewById(R.id.dice5);
        dices[5] =  (ImageView) findViewById(R.id.dice6);


        //Tar fram själva relativeLayout-en också. Behövs i DiceAnimator.
        screen = (RelativeLayout) findViewById(R.id.layout);
        diceAnimators = new DiceAnimator[6];

        //Initierar alla sex diceAnimators. Lägger till grafiken separat också, så det
        //ska bli enklare för dig att byta ut om du känner för det.
        for(int i = 0; i < 6; i++){
            diceAnimators[i] = new DiceAnimator(this, dices[i], screen, dicesOffsets[i]);
            diceAnimators[i].addDrawables(diceDrawables);
        }

        //Skapar shakeDetecorn
        shakeDetector = new ShakeDetect(this);

        //Aktiverar shakedetector.
        shakeDetector.enableShakeDetector();

        //Ny tråd, som bara väntar på att ett skak ska göras. Dör när shakeDetector avregistreas.
        final Thread shakeDetect = new Thread(new Runnable() {
            @Override
            public void run() {
                while(shakeDetector.isEnabled()) {
                    while (!shakeDetector.isShakeDetected());
                    Log.d(TAG, "shake!");
                    shakeDetector.disableShakeDetector();
                    handleShake();
                }

            }
        });

        shakeDetect.start();

        //Om man klickar på knappen
        skak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(skak.isChecked()){
                    resetDices();

                    shakeDetector.enableShakeDetector();
                }else{
                    shakeDetector.disableShakeDetector();
                }
            }
        });

        resetDices();

    }

    /**
     * Återställer alla tärningar. Placerar dem baserat på offsetten som skickats till
     * varje diceAnimator.
     */
    private void resetDices() {
        for(int i = 0; i < 6; i++){
            diceAnimators[i].resetAnimation(dices[i]);
        }
    }

    /**
     * Hantera vad som ska hända när vi skakar mobilen. Samma metod kan anropas vid ett
     * knapptryck istället om man inte vill använda skak-funktionen.
     *
     */
    private void handleShake() {
        for(DiceAnimator d : diceAnimators){
            d.startAnimation();
        }

        //Drygt det här, men om man vill ändra grafik från en annan tråd än main-tråden,
        //så måste man manuellt lägga in händelsen så att den körs på main-tråden.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            skak.setChecked(false);
            }
        });


    }

    /**
     * Behöver döda tråden på något sätt, och döda själva listenern som läser av accelerometern.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "shakeDetector unregistered");
        shakeDetector.unregisterListener();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    return false;
    }


}
