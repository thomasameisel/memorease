package com.tarian.memorease.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tarian.memorease.R;
import com.tarian.memorease.model.Memorea;
import com.tarian.memorease.presenter.MemoreaListPresenter;
import com.tarian.memorease.view.dialog.MemoreaDialog;
import com.tarian.memorease.view.fragment.FragmentStack;
import com.tarian.memorease.view.fragment.MemoreaInfoFragment;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusAppCompatActivity;
import nucleus.view.ViewWithPresenter;

@RequiresPresenter(MemoreaListPresenter.class)
public class MemoreaInfoActivity extends NucleusAppCompatActivity<MemoreaListPresenter>
        implements MemoreaDialog.OnSaveMemoreaDialog {

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";
    private static final String HINT = "hint";
    private static final String LEVEL = "level";
    private static final String NOTIFICATION_GENERATOR = "notificationGenerator";

    public static Intent getCallingIntent(Context context, Memorea memorea) {
        final Intent intent = new Intent(context, MemoreaInfoActivity.class);
        intent.putExtra(ID, memorea.mId.toString());
        intent.putExtra(TITLE, memorea.mTitle);
        intent.putExtra(QUESTION, memorea.mQuestion);
        intent.putExtra(ANSWER, memorea.mAnswer);
        intent.putExtra(HINT, memorea.mHint);
        intent.putExtra(LEVEL, memorea.mMemorizationLevel);
        intent.putExtra(NOTIFICATION_SERVICE, memorea.mNotificationId);
        return intent;
    }

    private FragmentStack mFragmentStack;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_info);
        getWindow().setBackgroundDrawable(null);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFragmentStack = new FragmentStack(this, getSupportFragmentManager(),
                R.id.fragment_container,  new FragmentStack.OnFragmentRemovedListener() {
            @Override
            public void onFragmentRemoved(Fragment fragment) {
                if (fragment instanceof ViewWithPresenter) {
                    ((ViewWithPresenter) fragment).getPresenter().destroy();
                }
            }
        });
        if (savedInstanceState == null) {
            mFragmentStack.replace(new MemoreaInfoFragment(getIntent().getStringExtra(TITLE),
                    getIntent().getStringExtra(QUESTION), getIntent().getStringExtra(ANSWER),
                    getIntent().getStringExtra(HINT)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens the dialog fragment to edit a memorea
     */
    public void editMemorea(final View view) {
        // dialog to edit memorea
        final Bundle dialogBundle = MemoreaDialog.getCallingArguments(
                getString(R.string.edit_memorea_title), true, getIntent().getExtras());

        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(dialogBundle);
        memoreaDialog.show(getSupportFragmentManager(), null);
    }

    /*
     * Edit memorea
     */
    @Override
    public void onSaveMemoreaDialog(final String id, final String[] fields) {
        String[] updatedFields = new String[6];
        System.arraycopy(fields, 0, updatedFields, 0, 4);
        updatedFields[4] = Integer.toString(getIntent().getIntExtra(LEVEL, 0));
        updatedFields[5] = Integer.toString(getIntent().getIntExtra(NOTIFICATION_GENERATOR, 0));
        getPresenter().updateMemoreaSharedPref(this, getIntent().getStringExtra(ID), updatedFields);
        if (mFragmentStack != null && mFragmentStack.peek() instanceof MemoreaInfoFragment) {
            ((MemoreaInfoFragment) mFragmentStack.peek()).updateFields(fields[0], fields[1],
                    fields[2], fields[3]);
        }
    }
}
