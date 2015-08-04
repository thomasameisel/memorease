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
    private static final String SHOWING_SCREEN_FRAGMENT = "screenFragment";
    private static final String FRAGMENT_MEMORIZE_SCREEN_TAG = "fragment_memorize_screen";
    private static final String FRAGMENT_MEMORIZE_SCREEN_ANSWER_TAG = "fragment_memorize_screen_answer";

    private boolean mShowingScreenFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_screen);
        getWindow().setBackgroundDrawable(null);
        ((TextView) findViewById(R.id.text_view_toolbar_title)).setText(getIntent().getExtras().getString(MemoreaListActivity.TITLE));

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null || savedInstanceState.getBoolean(SHOWING_SCREEN_FRAGMENT, true)) {
            mShowingScreenFragment = true;
            final MemorizeScreenFragment memorizeScreenFragment = new MemorizeScreenFragment();
            fragmentTransaction.add(R.id.fragment_holder, memorizeScreenFragment, FRAGMENT_MEMORIZE_SCREEN_TAG);
        } else {
            final MemorizeScreenAnswerFragment memorizeScreenAnswerFragment = new MemorizeScreenAnswerFragment();
            mShowingScreenFragment = false;
            fragmentTransaction.add(R.id.fragment_holder, memorizeScreenAnswerFragment, FRAGMENT_MEMORIZE_SCREEN_ANSWER_TAG);
        }
        fragmentTransaction.commit();

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SHOWING_SCREEN_FRAGMENT, mShowingScreenFragment);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Displays the mHint in the memorize screen fragment
     */
    public void giveHint(final View view) {
        final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_MEMORIZE_SCREEN_TAG);
        memorizeScreenFragment.giveHint();
    }

    /**
     * Changes the current fragment from the memorize screen fragment to the memorize screen mAnswer fragment
     */
    public void showAnswer(final View view) {
        mShowingScreenFragment = false;
        final MemorizeScreenAnswerFragment memorizeScreenAnswerFragment = new MemorizeScreenAnswerFragment();
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_holder, memorizeScreenAnswerFragment, FRAGMENT_MEMORIZE_SCREEN_ANSWER_TAG);
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
        intent.putExtra(MemoreaListActivity.ID, getIntent().getExtras().getString(MemoreaListActivity.ID));
        intent.putExtra(MemoreaListActivity.CONTINUE, value);
        return intent;
    }
}
