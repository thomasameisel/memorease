package com.memorease.memorease;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class MemoreaListActivity extends AppCompatActivity implements MemoreaDialog.OnAddMemoreaListener {
    private MemoreaListAdapter memoreaListAdapter;
    private SharedPreferences sharedPrefences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        sharedPrefences = getSharedPreferences(getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPrefences.edit();
        memoreaListAdapter = ((MemoreaListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_list)).getMemoreaListAdapter();
        Map<String,?> allKeys = sharedPrefences.getAll();
        if (allKeys != null) {
            memoreaListAdapter.addAll(initializeListFromSharedPref(allKeys));
        }
        if (getIntent().getExtras() != null) {
            createNotification(getIntent().getExtras().getBoolean("continue"), memoreaListAdapter.getMemoreaByUUID(UUID.fromString(getIntent().getExtras().getString("id"))));
        }
    }

    private ArrayList<MemoreaInfo> initializeListFromSharedPref(Map<String, ?> allKeys) {
        ArrayList<MemoreaInfo> memoreInfoCards = new ArrayList<>(allKeys.size());
        for (Map.Entry<String,?> entry : allKeys.entrySet()) {
            final MemoreaInfo memoreaInfo = initialzeMemoreaFromSharedPref(entry);
            if (memoreaInfo != null) {
                memoreInfoCards.add(memoreaInfo);
            }
        }
        Collections.sort(memoreInfoCards, new Comparator<MemoreaInfo>() {
            @Override
            public int compare(final MemoreaInfo memoreaInfo1, final MemoreaInfo memoreaInfo2) {
                return ((Integer) memoreaInfo1.position).compareTo(memoreaInfo2.position);
            }
        });
        return memoreInfoCards;
    }

    private MemoreaInfo initialzeMemoreaFromSharedPref(final Map.Entry<String,?> entry) {
        MemoreaInfo memoreaInfo = null;
        try {
            String[] fields = getStringArrayFromJSON(new JSONArray((String)entry.getValue()));
            memoreaInfo = new MemoreaInfo(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]));
            memoreaInfo.id = UUID.fromString(entry.getKey());
            memoreaInfo.position = Integer.parseInt(fields[5]);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        return memoreaInfo;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean a = true;
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
        memoreaInfo.putString("dialog_title", getString(R.string.add_memorea_title));
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
        final MemoreaListFragment memoreaListFragment = (MemoreaListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_list);
        memoreaListFragment.addMemoreaCard(memoreaInfo);
        memoreaInfo.position = memoreaListAdapter.getItemCount()-1;
        updateSharedPref(memoreaInfo, sharedPreferencesEditor);
        createNotification(false, memoreaInfo);
    }

    private void createNotification(boolean useNextTime, final MemoreaInfo memoreaInfo) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("title", memoreaInfo.title);
        alarmIntent.putExtra("question", memoreaInfo.question);
        alarmIntent.putExtra("answer", memoreaInfo.answer);
        alarmIntent.putExtra("hint", memoreaInfo.hint);
        alarmIntent.putExtra("id", memoreaInfo.id.toString());

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)Calendar.getInstance().getTimeInMillis(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if (useNextTime) {
            ++memoreaInfo.memorizationLevel;
            updateSharedPref(memoreaInfo, sharedPreferencesEditor);
            memoreaListAdapter.notifyItemChanged(memoreaInfo.position);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + ((long) memoreaInfo.getCurMemorization() * 60 * 1000), pendingIntent);
            // debug
            // alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10 * 1000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + ((long) memoreaInfo.getCurMemorization() * 60 * 1000), pendingIntent);
            // debug
            // alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5 * 1000, pendingIntent);
        }
    }

    @Override
    public void onEditMemoreaCard(final String[] updatedFields, final int memoreaPosition) {
        MemoreaInfoFragment memoreaInfoFragment = (MemoreaInfoFragment)getSupportFragmentManager().findFragmentByTag("fragment_memorea_info");
        memoreaInfoFragment.updateFieldsFromEdit(updatedFields);

        MemoreaInfo memoreaToUpdate = memoreaListAdapter.getMemoreaByUUID(UUID.fromString(updatedFields[0]));
        memoreaToUpdate.updateFields(updatedFields);
        updateSharedPref(memoreaToUpdate, sharedPreferencesEditor);
        memoreaListAdapter.notifyItemChanged(memoreaPosition);
    }

    public void updateSharedPrefOnMove(MemoreaInfo memoreaInfo1, MemoreaInfo memoreaInfo2) {
        updateSharedPref(memoreaInfo1, sharedPreferencesEditor);
        updateSharedPref(memoreaInfo2, sharedPreferencesEditor);
    }

    public void updateSharedPrefOnDelete(final MemoreaInfo deletedCard) {
        sharedPreferencesEditor.remove(deletedCard.id.toString());
        sharedPreferencesEditor.commit();
    }

    public void updateSharedPrefOnAdd(final MemoreaInfo deletedCard) {
        updateSharedPref(deletedCard, sharedPreferencesEditor);
    }

    private void updateSharedPref(MemoreaInfo memoreaInfo, SharedPreferences.Editor sharedPreferencesEditor) {
        sharedPreferencesEditor.putString(memoreaInfo.id.toString(), getJSONStringFromArray(memoreaInfo.getFields()).toString());
        sharedPreferencesEditor.commit();
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

    public JSONArray getJSONStringFromArray(String[] values) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < values.length; ++i) {
            array.put(values[i]);
        }
        return array;
    }

    public String[] getStringArrayFromJSON(JSONArray array) {
        String[] stringArray = new String[array.length()];
        try {
            for (int i = 0; i < array.length(); ++i) {
                stringArray[i] = array.getString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringArray;
    }
}
