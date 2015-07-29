package com.memorease.memorease;

import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Activity for when a memorization is occurring<br>
 * Holds the memorize screen fragment and memorize screen answer fragment
 * Must include Extra of String id, String title, String question, String answer, String hint, and boolean continue
 */
public class MemorizeScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_screen);
        final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorize_screen);
        memorizeScreenFragment.setFields(memorizeScreenFragment.getView(), getIntent().getExtras());
        ((TextView)findViewById(R.id.text_view_toolbar_title)).setText(getIntent().getExtras().getString("title"));

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    /**
     * Displays the hint in the memorize screen fragment
     */
    public void giveHint(final View view) {
        final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorize_screen);
        memorizeScreenFragment.giveHint();
    }

    /**
     * Changes the current fragment from the memorize screen fragment to the memorize screen answer fragment
     */
    public void showAnswer(final View view) {
        final MemorizeScreenAnswerFragment memorizeScreenAnswerFragment = new MemorizeScreenAnswerFragment();
        memorizeScreenAnswerFragment.setArguments(getIntent().getExtras());
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
        intent.putExtra("id", getIntent().getExtras().getString("id"));
        intent.putExtra("continue", value);
        return intent;
    }
}
