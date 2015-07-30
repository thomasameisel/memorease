package com.tarian.memorease;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Activity for when a memorization is occurring<br>
 * Holds the memorize screen fragment and memorize screen mAnswer fragment
 * Must include Extra of String mId, String mTitle, String mQuestion, String mAnswer, String mHint, and boolean continue
 */
public class MemorizeScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_screen);
        ((TextView)findViewById(R.id.text_view_toolbar_title)).setText(getIntent().getExtras().getString("mTitle"));

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }



    /**
     * Displays the mHint in the memorize screen fragment
     */
    public void giveHint(final View view) {
        final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorize_screen);
        memorizeScreenFragment.giveHint();
    }

    /**
     * Changes the current fragment from the memorize screen fragment to the memorize screen mAnswer fragment
     */
    public void showAnswer(final View view) {
        final MemorizeScreenAnswerFragment memorizeScreenAnswerFragment = new MemorizeScreenAnswerFragment();
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_memorize_screen, memorizeScreenAnswerFragment, "fragment_memorize_screen_answer");
        fragmentTransaction.commit();
    }

    /**
     * Starts the MemoreaListActivity to repeat the memorea memorization time
     */
    public void answerWrong(final View view) {
        startActivity(generateIntent(false));
    }

    /**
     * Starts the MemoreaListActivity to go to the next memorea memorization time
     */
    public void answerCorrect(final View view) {
        startActivity(generateIntent(true));
    }

    private Intent generateIntent(final boolean value) {
        final Intent intent = new Intent(this, MemoreaListActivity.class);
        intent.putExtra("mId", getIntent().getExtras().getString("mId"));
        intent.putExtra("continue", value);
        return intent;
    }
}
