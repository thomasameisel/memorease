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


public class MemorizeScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_screen);
        final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorize_screen);
        memorizeScreenFragment.setFields(getIntent().getExtras());
        ((TextView)findViewById(R.id.text_view_toolbar_title)).setText(getIntent().getExtras().getString("title"));

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    public void giveHint(final View view) {
        final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorize_screen);
        memorizeScreenFragment.giveHint();
    }

    public void showAnswer(final View view) {
        final MemorizeScreenAnswerFragment memorizeScreenAnswerFragment = new MemorizeScreenAnswerFragment();
        memorizeScreenAnswerFragment.setArguments(getIntent().getExtras());
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_memorize_screen, memorizeScreenAnswerFragment, "fragment_memorize_screen_answer");
        fragmentTransaction.commit();
    }

    public void answerWrong(final View view) {
        final Intent intent = new Intent(this, MemoreaListActivity.class);
        intent.putExtra("id", getIntent().getExtras().getString("id"));
        intent.putExtra("continue", false);
        startActivity(intent);
    }

    public void answerCorrect(final View view) {
        final Intent intent = new Intent(this, MemoreaListActivity.class);
        intent.putExtra("id", getIntent().getExtras().getString("id"));
        intent.putExtra("continue", true);
        startActivity(intent);
    }
}
