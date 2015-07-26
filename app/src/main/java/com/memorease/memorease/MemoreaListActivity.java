package com.memorease.memorease;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import java.util.UUID;

public class MemoreaListActivity extends AppCompatActivity implements MemoreaDialog.OnAddMemoreaListener {
    MemoreaListAdapter memoreaListAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        memoreaListAdapter = ((MemoreaListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_list)).getMemoreaListAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        super.onBackPressed();
    }

    public void addMemorea(final View view) {
        // dialog to add memorea
        final Bundle memoreaInfo = new Bundle();
        memoreaInfo.putString("dialog_title", getResources().getString(R.string.add_memorea_title));
        memoreaInfo.putBoolean("is_editing", false);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(memoreaInfo);
        memoreaDialog.show(fragmentManager, null);
    }

    public void editMemorea(final View view) {
        // dialog to edit memorea
        final Bundle memoreaInfo = new Bundle();
        memoreaInfo.putString("dialog_title", getResources().getString(R.string.edit_memorea_title));
        memoreaInfo.putBoolean("is_editing", true);
        memoreaInfo.putStringArray("edit_memorea_info", getMemoreaInfoFromFragment(getSupportFragmentManager().findFragmentByTag("fragment_memorea_info")));
        memoreaInfo.putInt("memorea_position", getMemoreaPositionFromFragment(getSupportFragmentManager().findFragmentByTag("fragment_memorea_info")));

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(memoreaInfo);
        memoreaDialog.show(fragmentManager, null);
    }

    private int getMemoreaPositionFromFragment(final Fragment fragment) {
        return ((MemoreaInfoFragment)fragment).memoreaPosition;
    }

    private String[] getMemoreaInfoFromFragment(final Fragment fragment) {
        return ((MemoreaInfoFragment)fragment).memoreaInfo;
    }

    @Override
    public void onAddMemoreaCard(final MemoreaInfo memoreaInfo) {
        //create card using this info and add to memoreaList
        MemoreaListFragment memoreaListFragment = (MemoreaListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_list);
        memoreaListFragment.addMemoreaCard(memoreaInfo);
    }

    @Override
    public void onEditMemoreaCard(final String[] updatedFields, final int memoreaPosition) {
        MemoreaInfoFragment memoreaInfoFragment = (MemoreaInfoFragment)getSupportFragmentManager().findFragmentByTag("fragment_memorea_info");
        FragmentManager.BackStackEntry backEntry=getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1);
        String str=backEntry.getName();

        memoreaInfoFragment.updateFieldsFromEdit(updatedFields);
        MemoreaInfo memoreaToUpdate = memoreaListAdapter.getMemoreaByUUID(UUID.fromString(updatedFields[0]));
        memoreaToUpdate.updateFields(updatedFields);
        memoreaListAdapter.notifyItemChanged(memoreaPosition);
    }

    public void openMemoreaInfoFragment(final String[] info, final int position) {
        // Create new fragment and transaction
        final Bundle memoreaInfo = new Bundle();
        memoreaInfo.putStringArray("memorea_info", info);
        memoreaInfo.putInt("memorea_position", position);

        final Fragment infoFragment = new MemoreaInfoFragment();
        infoFragment.setArguments(memoreaInfo);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_memorea_list, infoFragment, "fragment_memorea_info");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
