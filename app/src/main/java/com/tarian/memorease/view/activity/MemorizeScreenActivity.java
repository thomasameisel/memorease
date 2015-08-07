package com.tarian.memorease.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.tarian.memorease.R;
import com.tarian.memorease.model.Memorea;
import com.tarian.memorease.presenter.MemorizeScreenPresenter;
import com.tarian.memorease.view.fragment.FragmentStack;
import com.tarian.memorease.view.fragment.MemorizeScreenAnswerFragment;
import com.tarian.memorease.view.fragment.MemorizeScreenFragment;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusAppCompatActivity;
import nucleus.view.ViewWithPresenter;

/**
 * Activity for when a memorization is occurring<br>
 * Holds the memorize screen fragment and memorize screen mAnswer fragment
 * Must include Extra of String mId, String mTitle, String mQuestion, String mAnswer, String mHint, and boolean continue
 */
@RequiresPresenter(MemorizeScreenPresenter.class)
public class MemorizeScreenActivity extends NucleusAppCompatActivity<MemorizeScreenPresenter> {
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";
    private static final String HINT = "hint";
    private static final String LEVEL = "level";
    private static final String NOTIFICATION_GENERATOR = "notificationGenerator";

    public static Intent getCallingIntent(Context context, Memorea memorea) {
        final Intent intent = new Intent(context, MemorizeScreenActivity.class);
        intent.putExtra(ID, memorea.mId.toString());
        intent.putExtra(TITLE, memorea.mTitle);
        intent.putExtra(QUESTION, memorea.mQuestion);
        intent.putExtra(ANSWER, memorea.mAnswer);
        intent.putExtra(HINT, memorea.mHint);
        intent.putExtra(LEVEL, memorea.mMemorizationLevel);
        intent.putExtra(NOTIFICATION_GENERATOR, memorea.mNotificationId);
        return intent;
    }

    private FragmentStack mFragmentStack;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_screen);
        getWindow().setBackgroundDrawable(null);
        ((TextView) findViewById(R.id.text_view_toolbar_title)).setText(getIntent()
                .getStringExtra(TITLE));

        mFragmentStack = new FragmentStack(this, getSupportFragmentManager(),
                R.id.frame_layout_fragment_holder, new FragmentStack.OnFragmentRemovedListener() {
            @Override
            public void onFragmentRemoved(Fragment fragment) {
                if (fragment instanceof ViewWithPresenter) {
                    ((ViewWithPresenter) fragment).getPresenter().destroy();
                }
            }
        });

        if (savedInstanceState == null) {
            mFragmentStack.replace(new MemorizeScreenFragment(getIntent().getStringExtra(QUESTION),
                    getIntent().getStringExtra(HINT)));
        }
    }

    /**
     * Displays the mHint in the memorize screen fragment
     */
    public void giveHint(final View view) {
        if (mFragmentStack.peek() instanceof MemorizeScreenFragment) {
            final MemorizeScreenFragment memorizeScreenFragment = (MemorizeScreenFragment)
                    mFragmentStack.peek();
            memorizeScreenFragment.giveHint();
        }
    }

    /**
     * Changes the current fragment from the memorize screen fragment to the memorize screen mAnswer fragment
     */
    public void showAnswer(final View view) {
        mFragmentStack.push(new MemorizeScreenAnswerFragment(getIntent().getStringExtra(ANSWER)));
    }

    /**
     * Starts the MemoreaListActivity to repeat the memorea memorization time
     */
    public void answerWrong(final View view) {
        startActivity(MemoreaListActivity.getCallingIntent(this, getIntent().getStringExtra(ID)));
    }

    /**
     * Starts the MemoreaListActivity to go to the next memorea memorization time
     */
    public void answerCorrect(final View view) {
        String[] fields = new String[6];
        fields[0] = getIntent().getStringExtra(TITLE);
        fields[1] = getIntent().getStringExtra(QUESTION);
        fields[2] = getIntent().getStringExtra(ANSWER);
        fields[3] = getIntent().getStringExtra(HINT);
        fields[4] = Integer.toString(getIntent().getIntExtra(LEVEL, 0) + 1);
        fields[5] = Integer.toString(getIntent().getIntExtra(NOTIFICATION_GENERATOR, 0));
        getPresenter().updateMemorea(this, getIntent().getStringExtra(ID), fields);
        startActivity(MemoreaListActivity.getCallingIntent(this, getIntent().getStringExtra(ID)));
    }
}
