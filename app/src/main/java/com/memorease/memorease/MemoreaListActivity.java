package com.memorease.memorease;

import android.app.AlarmManager;
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
import java.util.Map;
import java.util.UUID;

/**
 * Main activity<br>
 * Holds the memorea list fragment and memorea info fragment
 */
public class MemoreaListActivity extends AppCompatActivity implements MemoreaDialog.OnAddMemoreaListener, MemoreaListFragment.OnMemoreaListFragmentListener, MemoreaInfoFragment.OnMemoreaInfoFragment{
    private MemoreaListAdapter memoreaListAdapter;
    public static SharedPreferences sharedPreferences;
    private String[] memoreaViewedInfo;
    private int memoreaViewedPosition;
    public int numActiveNotifications;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        sharedPreferences = getSharedPreferences(getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
        memoreaListAdapter = ((MemoreaListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_list)).getMemoreaListAdapter();
        final Map<String,?> allKeys = sharedPreferences.getAll();
        if (allKeys != null) {
            memoreaListAdapter.addAll(initializeListFromSharedPref(allKeys));
        }
        if (getIntent().getExtras() != null) {
            final MemoreaInfo memoreaInfo = memoreaListAdapter.getMemoreaByUUID(UUID.fromString(getIntent().getExtras().getString("id")));
            if (memoreaInfo != null) {
                final boolean useNextTime = getIntent().getExtras().getBoolean("continue");
                if (checkIfFinishedLastTime(useNextTime, memoreaInfo)) {
                    DialogFragment dialogFragment = BasicDialog.newInstance(getString(R.string.completed), String.format(getString(R.string.completed_message), memoreaInfo.title));
                    dialogFragment.show(getSupportFragmentManager(), "dialog");
                    ++memoreaInfo.memorizationLevel;
                    memoreaInfo.completed = true;
                } else {
                    if (useNextTime) {
                        ++memoreaInfo.memorizationLevel;
                        addMemoreaSharedPref(memoreaInfo);
                        memoreaListAdapter.notifyDataSetChanged();
                    }
                    createNotification(true, memoreaInfo);
                }
            }
            numActiveNotifications = 0;
        }
    }

    /**
     * Creates an ArrayList of memoreas from the shared preferences
     */
    private ArrayList<MemoreaInfo> initializeListFromSharedPref(Map<String, ?> allKeys) {
        final ArrayList<MemoreaInfo> memoreInfoCards = new ArrayList<>(allKeys.size());
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

    /**
     * Initializes a memorea from the JSONArray entry in shared preferences<br>
     * Returns null if there is some type of error
     */
    private MemoreaInfo initializeMemoreaFromSharedPref(final Map.Entry<String, ?> entry) {
        MemoreaInfo memoreaInfo = null;
        try {
            String[] fields = getStringArrayFromJSON(new JSONArray((String)entry.getValue()));
            if (fields.length == 6) {
                memoreaInfo = new MemoreaInfo(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]));
                memoreaInfo.notificationGeneratorId = Integer.parseInt(fields[5]);
                memoreaInfo.id = UUID.fromString(entry.getKey());
            }
        } finally {
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

    /**
     * Opens the dialog fragment to add a memorea
     */
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

    /**
     * Opens the dialog fragment to edit a memorea
     */
    public void editMemorea(final View view) {
        // dialog to edit memorea
        final Bundle memoreaInfoBundle = new Bundle();
        memoreaInfoBundle.putString("dialog_title", getResources().getString(R.string.edit_memorea_title));
        memoreaInfoBundle.putBoolean("is_editing", true);
        memoreaInfoBundle.putStringArray("edit_memorea_info", memoreaViewedInfo);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(memoreaInfoBundle);
        memoreaDialog.show(fragmentManager, null);
    }

    @Override
    public void onAddMemoreaCard(final MemoreaInfo memoreaInfo) {
        createNotification(true, memoreaInfo);
        memoreaListAdapter.onItemAdd(memoreaInfo);
        addMemoreaSharedPref(memoreaInfo);
    }

    private boolean checkIfFinishedLastTime(final boolean useNextTime, final MemoreaInfo memoreaInfo) {
        return useNextTime && memoreaInfo.memorizationLevel == memoreaInfo.getTotalMemorizationLevels();
    }

    private void createNotification(final boolean newNotification, final MemoreaInfo memoreaInfo) {
        final int notificationGeneratorId;
        final long timeUntilMemorization;
        if (newNotification) {
            ++numActiveNotifications;
            notificationGeneratorId = (int)(Calendar.getInstance().getTimeInMillis() & 0xfffffff);
            memoreaInfo.notificationGeneratorId = notificationGeneratorId;
            timeUntilMemorization = SystemClock.elapsedRealtime() + memoreaInfo.getCurMemorization();
            final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putLong(String.format("%s_notification_time", memoreaInfo.id.toString()), timeUntilMemorization);
            sharedPreferencesEditor.commit();
        } else {
            notificationGeneratorId = memoreaInfo.notificationGeneratorId;
            timeUntilMemorization = memoreaInfo.getTimeNextAlarm();
        }

        final Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        if (numActiveNotifications > 1) {
            alarmIntent.putExtra("multiple_notifications", true);
        } else {
            alarmIntent.putExtra("multiple_notifications", false);
            alarmIntent.putExtra("title", memoreaInfo.title);
            alarmIntent.putExtra("question", memoreaInfo.question);
            alarmIntent.putExtra("answer", memoreaInfo.answer);
            alarmIntent.putExtra("hint", memoreaInfo.hint);
            alarmIntent.putExtra("id", memoreaInfo.id.toString());
        }

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationGeneratorId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.ELAPSED_REALTIME, timeUntilMemorization, pendingIntent);
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
        createNotification(false, memoreaToUpdate);
        addMemoreaSharedPref(memoreaToUpdate);
        memoreaListAdapter.notifyItemChanged(memoreaViewedPosition);
    }

    /**
     * Removes the memorea from shared preferences
     * @param deletedMemorea Memorea that needs to have its information deleted from shared preferences
     */
    public static void removeMemoreaSharedPref(final MemoreaInfo deletedMemorea) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.remove(deletedMemorea.id.toString());
        sharedPreferencesEditor.remove(String.format("%s_notification_time", deletedMemorea.id.toString()));
        sharedPreferencesEditor.apply();
    }

    /**
     * Adds the memorea to shared preferences
     * @param addedMemorea Memorea that needs to have its information added to shared preferences
     */
    public static void addMemoreaSharedPref(final MemoreaInfo addedMemorea) {
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(addedMemorea.id.toString(), getJSONStringFromArray(addedMemorea.getFields()).toString());
        sharedPreferencesEditor.apply();
    }

    /**
     * Changes the current fragment from the memorea list fragment to the memorea info fragment
     * @param info Information of memorea that will be displayed in the fragment<br>
     *             String array of length 5 with the id, title, question, answer, and hint
     * @param position Position of the memorea in the memorea list
     */
    public void openMemoreaInfoFragment(final String[] info, final int position) {
        // Create new fragment and transaction
        final Bundle memoreaInfoBundle = new Bundle();
        memoreaInfoBundle.putStringArray("memorea_info", info);

        memoreaViewedInfo = info;
        memoreaViewedPosition = position;

        final Fragment infoFragment = new MemoreaInfoFragment();
        infoFragment.setArguments(memoreaInfoBundle);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_memorea_list, infoFragment, "fragment_memorea_info");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public int getNumActiveNotifications() {
        return numActiveNotifications;
    }

    @Override
    public void setNumActiveNotifications(int numActiveNotifications) {
        this.numActiveNotifications = numActiveNotifications;
    }

    /**
     * Generates a JSON array from a String array
     * @param values String array to convert to JSON
     */
    public static JSONArray getJSONStringFromArray(final String[] values) {
        JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }

    /**
     * Generates a String array from a JSON array
     * @param array JSON array to convert to a String array
     */
    public static String[] getStringArrayFromJSON(final JSONArray array) {
        final String[] stringArray = new String[array.length()];
        try {
            for (int i = 0; i < array.length(); ++i) {
                stringArray[i] = array.getString(i);
            }
        } finally {
            return stringArray;
        }
    }
}
