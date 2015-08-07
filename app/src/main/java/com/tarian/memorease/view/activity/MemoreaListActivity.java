package com.tarian.memorease.view.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.tarian.memorease.view.dialog.MemoreaDialog;
import com.tarian.memorease.R;
import com.tarian.memorease.model.Memorea;
import com.tarian.memorease.presenter.MemoreaListPresenter;
import com.tarian.memorease.view.fragment.FragmentStack;
import com.tarian.memorease.view.fragment.MemoreaInfoFragment;
import com.tarian.memorease.view.fragment.MemoreaListFragment;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusAppCompatActivity;
import nucleus.view.ViewWithPresenter;

/**
 * Main activity<br>
 * Holds the memorea list fragment and memorea info fragment
 */
@RequiresPresenter(MemoreaListPresenter.class)
public class MemoreaListActivity extends NucleusAppCompatActivity<MemoreaListPresenter>
        implements AdapterView.OnItemClickListener, MemoreaDialog.OnSaveMemoreaDialog {

    private static String ID = "memoreaId";

    public static Intent getCallingIntent(Context context, String id) {
        final Intent listIntent = new Intent(context, MemoreaListActivity.class);
        listIntent.putExtra(ID, id);
        return listIntent;
    }

    private FragmentStack mListFragmentStack, mInfoFragmentStack;
    private boolean mDualPane;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        getWindow().setBackgroundDrawable(null);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        mDualPane = findViewById(R.id.fragment_info_container) != null;

        mListFragmentStack = new FragmentStack(this, getSupportFragmentManager(),
                R.id.fragment_container,  new FragmentStack.OnFragmentRemovedListener() {
                    @Override
                    public void onFragmentRemoved(Fragment fragment) {
                        if (fragment instanceof ViewWithPresenter) {
                            ((ViewWithPresenter) fragment).getPresenter().destroy();
                        }
                    }
                });
        if (savedInstanceState == null) {
            mListFragmentStack.replace(new MemoreaListFragment(getIntent().getStringExtra(ID)));
        }

        if (mDualPane) {
            mInfoFragmentStack = new FragmentStack(this, getSupportFragmentManager(),
                    R.id.fragment_info_container,  new FragmentStack.OnFragmentRemovedListener() {
                @Override
                public void onFragmentRemoved(Fragment fragment) {
                    if (fragment instanceof ViewWithPresenter) {
                        ((ViewWithPresenter) fragment).getPresenter().destroy();
                    }
                }
            });
            if (savedInstanceState == null) {
                mInfoFragmentStack.replace(new MemoreaInfoFragment());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Memorea memorea = ((MemoreaListFragment)mListFragmentStack.peek())
                .getMemoreaByPosition(position);

        if (memorea.getTimeUntilNextAlarm(this) < 0 && !memorea.mCompleted) {
            final Intent memorizeScreenIntent = MemorizeScreenActivity
                    .getCallingIntent(this, memorea);
            startActivity(memorizeScreenIntent);
        } else {
            if (mDualPane) {
                if (mInfoFragmentStack.peek() instanceof MemoreaInfoFragment) {
                    ((MemoreaInfoFragment) mInfoFragmentStack.peek())
                            .updateFields(memorea.mTitle, memorea.mQuestion, memorea.mAnswer,
                                    memorea.mHint);
                }
            } else {
                final Intent memoreaInfoIntent = MemoreaInfoActivity
                        .getCallingIntent(this, memorea);
                startActivity(memoreaInfoIntent);
            }
        }
    }

    /*
     * Add memorea
     */
    @Override
    public void onSaveMemoreaDialog(String id, String[] fields) {
        if (mListFragmentStack != null && mListFragmentStack.peek()
                instanceof MemoreaListFragment) {
            fields[4] = Integer.toString(0);
            fields[5] = Integer.toString(0);
            ((MemoreaListFragment) mListFragmentStack.peek()).addMemorea(fields);
            ((MemoreaListFragment) mListFragmentStack.peek()).showNoMemoreasView(false);
        }
    }

    /**
     * Opens the dialog fragment to add a memorea
     */
    public void addMemorea(final View view) {
        // dialog to add memorea
        final Bundle dialogBundle = MemoreaDialog.getCallingArguments(
                getString(R.string.add_memorea_title), false);

        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(dialogBundle);
        memoreaDialog.show(getSupportFragmentManager(), null);
    }
}
