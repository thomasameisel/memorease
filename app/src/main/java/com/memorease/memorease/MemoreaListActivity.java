package com.memorease.memorease;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.support.v4.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class MemoreaListActivity extends AppCompatActivity implements MemoreaDialog.OnAddMemoreaListener, MemoreaListFragment.OnMemoreaListFragmentListener, MemoreaInfoFragment.OnMemoreaInfoFragment{
    private MemoreaListAdapter memoreaListAdapter;
    public static SharedPreferences sharedPreferences;
    private String[] memoreaViewedInfo;
    private int memoreaViewedPosition;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        sharedPreferences = getSharedPreferences(getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
        memoreaListAdapter = ((MemoreaListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_list)).getMemoreaListAdapter();
        Map<String,?> allKeys = sharedPreferences.getAll();
        if (allKeys != null) {
            memoreaListAdapter.addAll(initializeListFromSharedPref(allKeys));
        }
        if (getIntent().getExtras() != null) {
            MemoreaInfo memoreaInfo = memoreaListAdapter.getMemoreaByUUID(UUID.fromString(getIntent().getExtras().getString("id")));
            if (checkIfFinishedLastTime(getIntent().getExtras().getBoolean("continue"), memoreaInfo)) {
                DialogFragment dialogFragment = BasicDialog.newInstance(getString(R.string.completed), String.format(getString(R.string.completed_message), memoreaInfo.title));
                dialogFragment.show(getSupportFragmentManager(), "dialog");
                ++memoreaInfo.memorizationLevel;
                memoreaInfo.completed = true;
            } else {
                createNotification(getIntent().getExtras().getBoolean("continue"), memoreaInfo);
            }
        }
    }

    private ArrayList<MemoreaInfo> initializeListFromSharedPref(Map<String, ?> allKeys) {
        ArrayList<MemoreaInfo> memoreInfoCards = new ArrayList<>(allKeys.size());
        for (Map.Entry<String,?> entry : allKeys.entrySet()) {
            if (entry.getValue().getClass() == String.class) {
                final MemoreaInfo memoreaInfo = initializeMemoreaFromSharedPref(entry);
                if (memoreaInfo != null) {
                    memoreInfoCards.add(memoreaInfo);
                }
            }
        }

        return memoreInfoCards;
    }

    private MemoreaInfo initializeMemoreaFromSharedPref(final Map.Entry<String, ?> entry) {
        MemoreaInfo memoreaInfo = null;
        try {
            String[] fields = getStringArrayFromJSON(new JSONArray((String)entry.getValue()));
            memoreaInfo = new MemoreaInfo(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]));
            memoreaInfo.id = UUID.fromString(entry.getKey());
            return memoreaInfo;
        } catch (final JSONException e) {
            e.printStackTrace();
            return memoreaInfo;
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
        memoreaInfo.putStringArray("edit_memorea_info", memoreaViewedInfo);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(memoreaInfo);
        memoreaDialog.show(fragmentManager, null);
    }

    @Override
    public void onAddMemoreaCard(final MemoreaInfo memoreaInfo) {
        //create card using this info and add to memoreaList
        createNotification(false, memoreaInfo);
        memoreaListAdapter.onItemAdd(memoreaInfo);
        updateSharedPref(memoreaInfo);
    }

    private boolean checkIfFinishedLastTime(final boolean useNextTime, final MemoreaInfo memoreaInfo) {
        return useNextTime && memoreaInfo.memorizationLevel == memoreaInfo.getTotalMemorizationLevels();
    }

    private void createNotification(final boolean useNextTime, final MemoreaInfo memoreaInfo) {
        final int notificationId = (int)Calendar.getInstance().getTimeInMillis();
        memoreaInfo.notificationId = notificationId;

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("title", memoreaInfo.title);
        alarmIntent.putExtra("question", memoreaInfo.question);
        alarmIntent.putExtra("answer", memoreaInfo.answer);
        alarmIntent.putExtra("hint", memoreaInfo.hint);
        alarmIntent.putExtra("id", memoreaInfo.id.toString());
        alarmIntent.putExtra("notification_id", memoreaInfo.notificationId);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)Calendar.getInstance().getTimeInMillis(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if (useNextTime) {
            ++memoreaInfo.memorizationLevel;
            updateSharedPref(memoreaInfo);
            memoreaListAdapter.notifyDataSetChanged();
        }
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + memoreaInfo.getCurMemorization(), pendingIntent);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putLong(memoreaInfo.id.toString()+"_notification_time", SystemClock.elapsedRealtime() + memoreaInfo.getCurMemorization());
        sharedPreferencesEditor.commit();
        // debug
        /*alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10 * 1000, pendingIntent);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putLong(memoreaInfo.id.toString() + "_notification_time", SystemClock.elapsedRealtime() + 10 * 1000);
        sharedPreferencesEditor.apply();*/
    }

    @Override
    public void onEditMemoreaCard(final String[] updatedFields) {
        MemoreaInfoFragment memoreaInfoFragment = (MemoreaInfoFragment)getSupportFragmentManager().findFragmentByTag("fragment_memorea_info");
        memoreaInfoFragment.updateFieldsFromEdit(updatedFields);

        MemoreaInfo memoreaToUpdate = memoreaListAdapter.getMemoreaByUUID(UUID.fromString(updatedFields[0]));
        memoreaToUpdate.updateFields(updatedFields);
        updateSharedPref(memoreaToUpdate);
        memoreaListAdapter.notifyItemChanged(memoreaViewedPosition);
    }

    public void updateSharedPrefOnDelete(final MemoreaInfo deletedCard) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.remove(deletedCard.id.toString());
        sharedPreferencesEditor.apply();
    }

    public void updateSharedPrefOnAdd(final MemoreaInfo deletedCard) {
        updateSharedPref(deletedCard);
    }

    private void updateSharedPref(final MemoreaInfo memoreaInfo) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(memoreaInfo.id.toString(), getJSONStringFromArray(memoreaInfo.getFields()).toString());
        sharedPreferencesEditor.apply();
    }

    public void clearMemoreaNotification(final MemoreaInfo memoreaInfo) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(memoreaInfo.notificationId);
    }

    public void openMemoreaInfoFragment(final String[] info, final int position) {
        // Create new fragment and transaction
        final Bundle memoreaInfo = new Bundle();
        memoreaInfo.putStringArray("memorea_info", info);

        memoreaViewedInfo = info;
        memoreaViewedPosition = position;

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
        for (String value : values) {
            array.put(value);
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
