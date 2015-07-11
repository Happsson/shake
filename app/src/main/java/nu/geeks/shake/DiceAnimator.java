package nu.geeks.shake;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.Random;

/**
 * Created by Ali on 15-07-11.
 */
public class DiceAnimator {

    private CountDownTimer diceAnimation;
    private CountDownTimer diceReset;
    private ViewGroup parentLayout;
    private Context context;
    private float change = 125;
    private int[] offset;
    private int[] diceValues;
    private boolean isAnimating;

    /**
     * Konstruktor för diceAnimator.
     *
     * @param context       Behövs för att kunna byta bild. Skicka 'this' från din activity.
     * @param dice          den ImageView som tillhör den aktuella tärningen
     * @param parentLayout    Den layout som tärningarna ska centreras på.
     * @param offset        Offset från center på layouten ovan.
     */
    public DiceAnimator(Context context, ImageView dice, ViewGroup parentLayout, int[] offset){
        this.parentLayout = parentLayout;
        this.context = context;
        this.offset = offset;
        final ImageView dice1 = dice;

        isAnimating = false;

        diceAnimation = new CountDownTimer(5001, 40) {
            @Override
            public void onTick(long millisUntilFinished) {

                animateDice(millisUntilFinished, dice1);
            }

            @Override
            public void onFinish() {

            }
        };

        diceReset = new CountDownTimer(2500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                isAnimating = true;
                if(dice1.getVisibility() == View.VISIBLE) dice1.setVisibility(View.INVISIBLE);
                else if(dice1.getVisibility() == View.INVISIBLE) dice1.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                isAnimating = false;

            }
        };
    }

    /**
     * De bilder som tärningen består av.
     * MÅSTE vara exakt 6 bilder, annars kommer den kasta
     * nullPointerException.
     *
     * @param drawables
     */
    public void addDrawables(int[] drawables){
        diceValues = drawables;
    }

    /**
     * Starta tärningsanimationen.
     */
    public void startAnimation() {

        //Bilden kommer att uppdateras 125 ggr (5000 / 40 = 125).
        //Det kändes lagom att låta den snurra 37.5 grader första gången, sen mindre och mindre
        //varje gång, för att sista gången sluta på 0)

        change = 37.5f;
        diceAnimation.start();

    }

    private void animateDice(long millis, ImageView dice) {

        dice.setRotation( ( dice.getRotation() + change ) );
        change -= 0.3; //Dra bort .3 från change. 0.3 * 125 = 37.5.
        //Log.d(TAG, ""+ dice.getRotation());

        Random rand = new Random();


        //Vi kan låta den studsa runt lite också.
        if(rand.nextBoolean()) {
            dice.setX(dice.getX() + (rand.nextInt(7) - 4) * (change / 20));
            dice.setY(dice.getY() + (rand.nextInt(7) - 4) * (change / 20));
        }else{
            dice.setX(dice.getX() - (rand.nextInt(7) - 4) * (change / 20));
            dice.setY(dice.getY() - (rand.nextInt(7) - 4) * (change / 20));
        }
        if(millis > 1000){
            //Om det är mer än en sekund kvar av animationen, byt sida på tärningen.
            int diceNumber = rand.nextInt(6);

            dice.setBackground(context.getResources().getDrawable(diceValues[diceNumber]));
            //Log.d(TAG, "Byter bild");

        }
    }

    /**
     * Nollställer animationen. Behöver få in den imageview som ska nollställas.
     * Placerar tärningen i mitten av den layout som skickats in, med den offset som skickats in.
     *
     * @param dice
     */
    public void resetAnimation(ImageView dice){
        if(!isAnimating) {
            diceAnimation.cancel();
            dice.setX((parentLayout.getWidth() / 2) - dice.getWidth() / 2 + offset[0]);
            dice.setY((parentLayout.getHeight() / 2) - dice.getHeight() / 2 + offset[1]);
            dice.setRotation(0);
            diceReset.start();
        }
    }
}
