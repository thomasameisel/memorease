package com.memorease.memorease;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.AdapterView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

/**
 * Main activity<br>
 * Holds the memorea list fragment and memorea info fragment
 */
public class MemoreaListActivity extends AppCompatActivity implements MemoreaDialog.OnSaveMemoreaDialog, AdapterView.OnItemClickListener {
    private static final String MEMOREA_ORDER = "memoreaOrder";
    static SharedPreferences sharedPreferences;
    int numActiveNotifications;

    private MemoreaListAdapter memoreaListAdapter;
    private RecyclerView recyclerView;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver notificationReceiver;
    private final String LOAD_NEXT_NOTIFICATION = "noReload";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        registerReceiver(broadcastReceiver, new IntentFilter("NOTIFICATION_READY"));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.app_name));

        recyclerView = initRecyclerView();
        memoreaListAdapter = new MemoreaListAdapter();
        memoreaListAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(memoreaListAdapter);

        sharedPreferences = getSharedPreferences(getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
        final Map<String,?> allKeys = sharedPreferences.getAll();
        if (allKeys != null) {
            memoreaListAdapter.addAll(initializeListFromSharedPref(allKeys));
        }
        initializeNoMemoreasView();
        if (getIntent().getExtras() != null && savedInstanceState == null) {
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

    private void initializeNoMemoreasView() {
        if (memoreaListAdapter.getItemCount() == 0) {
            findViewById(R.id.text_view_no_memoreas_set).setVisibility(View.VISIBLE);
            findViewById(R.id.button_no_memoreas_set).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.text_view_no_memoreas_set).setVisibility(View.GONE);
            findViewById(R.id.button_no_memoreas_set).setVisibility(View.GONE);
        }
    }

    public void registerReceivers() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context ctx, final Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    memoreaListAdapter.notifyAllItemsChanged();
                }
            }
        };
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context ctx, final Intent intent) {
                if (intent.getAction().compareTo("NOTIFICATION_READY") == 0) {
                    memoreaListAdapter.notifyAllItemsChanged();
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(notificationReceiver, new IntentFilter("NOTIFICATION_READY"));
        clearNotifications();
    }

    public void unregisterReceivers() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (notificationReceiver != null) {
            unregisterReceiver(notificationReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
        String[] memoreaOrder = sharedPreferences.getString(MEMOREA_ORDER, "").split(",");
        if (memoreaOrder.length > 1) {
            memoreaListAdapter.setIdOrder(memoreaOrder);
            memoreaListAdapter.notifyAllItemsChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceivers();
        String orderOfCards = "";
        for (int i = 0; i < memoreaListAdapter.getItemCount(); ++i) {
            orderOfCards+=memoreaListAdapter.getItem(i).id.toString();
            if (i+1 < memoreaListAdapter.getItemCount()) {
                orderOfCards+=",";
            }
        }
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(MEMOREA_ORDER, orderOfCards);
        sharedPreferencesEditor.apply();
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(LOAD_NEXT_NOTIFICATION, false);

        super.onSaveInstanceState(savedInstanceState);
    }

    private RecyclerView initRecyclerView() {
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setScrollContainer(false);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return recyclerView;
    }

    private ItemTouchHelper.SimpleCallback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder dragged, final RecyclerView.ViewHolder target) {
                memoreaListAdapter.onItemMove(dragged.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                dismissMemorea(viewHolder);
            }

            @Override
            public int getMovementFlags(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        };
    }

    private void dismissMemorea(final RecyclerView.ViewHolder viewHolder) {
        final MemoreaInfo deletedCard = memoreaListAdapter.getItem(viewHolder.getAdapterPosition());
        final int deletedCardPosition = viewHolder.getAdapterPosition();
        Snackbar.make(findViewById(R.id.fragment_memorea_list),
                String.format("Deleted the %s Memorea", memoreaListAdapter.getItem(viewHolder.getAdapterPosition()).title),
                Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MemoreaListActivity.addMemoreaSharedPref(deletedCard);
                        memoreaListAdapter.onItemAdd(deletedCard, deletedCardPosition);
                    }
                })
                .setActionTextColor(Color.RED)
                .show();
        MemoreaListActivity.removeMemoreaSharedPref(deletedCard);
        final Intent intent = new Intent(this, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, deletedCard.notificationGeneratorId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        if (deletedCard.getTimeUntilNextAlarm() < 0 && numActiveNotifications == 1) {
            clearNotifications();
        }
        memoreaListAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        initializeNoMemoreasView();
    }

    private void clearNotifications() {
        numActiveNotifications = 0;
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    /**
     * Either opens the info fragment or starts the memorization screen activity based on if it is time for the memorea memorization
     */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final MemoreaInfo memoreaInfoClicked = memoreaListAdapter.getItem(position);

        if (memoreaInfoClicked.getTimeUntilNextAlarm() < 0 && !memoreaInfoClicked.completed) {
            Intent memorizeScreenIntent = new Intent (this, MemorizeScreenActivity.class);
            memorizeScreenIntent.putExtra("title", memoreaInfoClicked.title);
            memorizeScreenIntent.putExtra("question", memoreaInfoClicked.question);
            memorizeScreenIntent.putExtra("answer", memoreaInfoClicked.answer);
            memorizeScreenIntent.putExtra("hint", memoreaInfoClicked.hint);
            memorizeScreenIntent.putExtra("id", memoreaInfoClicked.id.toString());
            startActivity(memorizeScreenIntent);
        } else {
            final String[] info = new String[6];
            info[0] = memoreaInfoClicked.title;
            info[1] = memoreaInfoClicked.question;
            info[2] = memoreaInfoClicked.answer;
            info[3] = memoreaInfoClicked.hint;
            info[4] = Integer.toString(memoreaInfoClicked.memorizationLevel);
            info[5] = Integer.toString(memoreaInfoClicked.notificationGeneratorId);

            openMemoreaInfoFragment(memoreaInfoClicked.id.toString(), info);
        }
    }

    /**
     * Creates an ArrayList of memoreas from the shared preferences
     */
    private ArrayList<MemoreaInfo> initializeListFromSharedPref(final Map<String, ?> allKeys) {
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
        //alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10 * 1000, pendingIntent);
    }

    @Override
    public void onSaveMemoreaDialog(final String[] updatedFields) {
        final MemoreaInfo memoreaInfo = new MemoreaInfo(updatedFields[0], updatedFields[1], updatedFields[2], updatedFields[3], 0);
        memoreaInfo.generateNewId();
        createNotification(true, memoreaInfo);
        memoreaListAdapter.onItemAdd(memoreaInfo);
        recyclerView.smoothScrollToPosition(memoreaListAdapter.getItemCount() - 1);
        addMemoreaSharedPref(memoreaInfo);
        initializeNoMemoreasView();
    }

    /**
     * Removes the memorea from shared preferences
     * @param deletedMemorea Memorea that needs to have its information deleted from shared preferences
     */
    public static void removeMemoreaSharedPref(final MemoreaInfo deletedMemorea) {
        final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.remove(deletedMemorea.id.toString());
        sharedPreferencesEditor.remove(String.format("%s_notification_time", deletedMemorea.id.toString()));
        sharedPreferencesEditor.apply();
    }

    /**
     * Adds the memorea to shared preferences
     * @param addedMemorea Memorea that needs to have its information added to shared preferences
     */
    public static void addMemoreaSharedPref(final MemoreaInfo addedMemorea) {
        final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(addedMemorea.id.toString(), getJSONStringFromArray(addedMemorea.getFields()).toString());
        sharedPreferencesEditor.apply();
    }

    /**
     * Changes the current fragment from the memorea list fragment to the memorea info fragment
     * @param id Id of the memorea that will be displayed in the fragment
     * @param info Information of memorea that will be displayed in the fragment<br>
     *             String array of length 4 with the title, question, answer, and hint
     */
    public void openMemoreaInfoFragment(final String id, final String[] info) {
        //if dual pane send this info to fragment, else
        final Intent memoreaInfoIntent = new Intent(this, MemoreaInfoActivity.class);
        memoreaInfoIntent.putExtra("memorea_id", id);
        memoreaInfoIntent.putExtra("memorea_info", info);
        startActivity(memoreaInfoIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
