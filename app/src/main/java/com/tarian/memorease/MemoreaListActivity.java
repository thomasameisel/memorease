package com.tarian.memorease;

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
    public static final String NOTIFICATION_TIME = "%s_notification_time";
    public static final String MEMOREA_ORDER = "memoreaOrder";
    public static final String LOAD_NEXT_NOTIFICATION = "noReload";
    public static final String NOTIFICATION_READY = "notificationReady";
    public static final String TITLE = "title";
    public static final String QUESTION = "question";
    public static final String ANSWER = "answer";
    public static final String HINT = "hint";
    public static final String ID = "id";
    public static final String DIALOG_TITLE = "dialogTitle";
    public static final String IS_EDITING = "isEditing";
    public static final String MEMOREA_INFO = "memoreaInfo";
    public static final String CONTINUE = "continue";
    public static final String DIALOG = "dialog";

    public static SharedPreferences sSharedPreferences;

    private static final String ID_SEPARATOR = ",";

    private MemoreaListAdapter mMemoreaListAdapter;
    private RecyclerView mRecyclerView;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mNotificationReceiver;
    private MemoreaInfo mMemoreaClicked;
    private boolean mDualPane;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);
        getWindow().setBackgroundDrawable(null);
        registerReceiver(mBroadcastReceiver, new IntentFilter(NOTIFICATION_READY));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        mDualPane = findViewById(R.id.fragment_memorea_info) != null;

        mRecyclerView = initRecyclerView();
        mMemoreaListAdapter = new MemoreaListAdapter(this);
        mMemoreaListAdapter.setMOnItemClickListener(this);
        mRecyclerView.setAdapter(mMemoreaListAdapter);

        sSharedPreferences = getSharedPreferences(getString(R.string.prefence_file_key), Context.MODE_PRIVATE);
        final Map<String,?> allKeys = sSharedPreferences.getAll();
        if (allKeys != null) {
            mMemoreaListAdapter.addAll(initializeListFromSharedPref(allKeys));
        }
        initializeNoMemoreasView();
        if (getIntent().getExtras() != null && savedInstanceState == null) {
            updateMemoreaAfterMemorization(mMemoreaListAdapter.getMemoreaByUUID(UUID.fromString(getIntent().getExtras().getString(ID))));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        clearNotifications();
        registerReceivers();
        if (sSharedPreferences.getString(MEMOREA_ORDER, "") != null) {
            final String[] memoreaOrder = sSharedPreferences.getString(MEMOREA_ORDER, "").split(ID_SEPARATOR);
            if (memoreaOrder.length > 1) {
                mMemoreaListAdapter.setIdOrder(memoreaOrder);
            }
        }
        mMemoreaListAdapter.notifyAllItemsChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceivers();
        String orderOfCards = "";
        for (int i = 0; i < mMemoreaListAdapter.getItemCount(); ++i) {
            orderOfCards+= mMemoreaListAdapter.getItem(i).mId.toString();
            if (i+1 < mMemoreaListAdapter.getItemCount()) {
                orderOfCards+=ID_SEPARATOR;
            }
        }
        final SharedPreferences.Editor sharedPreferencesEditor = sSharedPreferences.edit();
        sharedPreferencesEditor.putString(MEMOREA_ORDER, orderOfCards);
        sharedPreferencesEditor.apply();
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putBoolean(LOAD_NEXT_NOTIFICATION, false);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Either opens the info fragment or starts the memorization screen activity based on if it is time for the memorea memorization
     */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final MemoreaInfo memoreaInfoClicked = mMemoreaListAdapter.getItem(position);

        if (memoreaInfoClicked.getTimeUntilNextAlarm() < 0 && !memoreaInfoClicked.mCompleted) {
            final Intent memorizeScreenIntent = new Intent(this, MemorizeScreenActivity.class);
            memorizeScreenIntent.putExtra(TITLE, memoreaInfoClicked.mTitle);
            memorizeScreenIntent.putExtra(QUESTION, memoreaInfoClicked.mQuestion);
            memorizeScreenIntent.putExtra(ANSWER, memoreaInfoClicked.mAnswer);
            memorizeScreenIntent.putExtra(HINT, memoreaInfoClicked.mHint);
            memorizeScreenIntent.putExtra(ID, memoreaInfoClicked.mId.toString());
            startActivity(memorizeScreenIntent);
        } else {
            if (mDualPane) {
                mMemoreaClicked = memoreaInfoClicked;
                ((MemoreaInfoFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_memorea_info)).updateFields(memoreaInfoClicked.getFields());
            } else {
                final String[] info = new String[6];
                info[0] = memoreaInfoClicked.mTitle;
                info[1] = memoreaInfoClicked.mQuestion;
                info[2] = memoreaInfoClicked.mAnswer;
                info[3] = memoreaInfoClicked.mHint;
                info[4] = Integer.toString(memoreaInfoClicked.mMemorizationLevel);
                info[5] = Integer.toString(memoreaInfoClicked.mNotificationGeneratorId);

                startMemoreaInfoActivity(memoreaInfoClicked.mId.toString(), info);
            }
        }
    }

    @Override
    public void onSaveMemoreaDialog(final String[] updatedFields) {
        final MemoreaInfo memoreaInfo = new MemoreaInfo(updatedFields[0], updatedFields[1], updatedFields[2], updatedFields[3], 0);
        memoreaInfo.generateNewId();
        createNotification(true, memoreaInfo);
        mMemoreaListAdapter.onItemAdd(memoreaInfo);
        mRecyclerView.smoothScrollToPosition(mMemoreaListAdapter.getItemCount() - 1);
        addMemoreaSharedPref(memoreaInfo);
        initializeNoMemoreasView();
    }

    /**
     * Opens the dialog fragment to add a memorea
     */
    public void addMemorea(final View view) {
        // dialog to add memorea
        final Bundle memoreaInfo = new Bundle();
        memoreaInfo.putString(DIALOG_TITLE, getString(R.string.add_memorea_title));
        memoreaInfo.putBoolean(IS_EDITING, false);

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
        memoreaInfoBundle.putString(DIALOG_TITLE, getString(R.string.edit_memorea_title));
        memoreaInfoBundle.putBoolean(IS_EDITING, true);
        memoreaInfoBundle.putStringArray(MEMOREA_INFO, mMemoreaClicked.getFields());

        final MemoreaDialog memoreaDialog = new MemoreaDialog();
        memoreaDialog.setArguments(memoreaInfoBundle);
        memoreaDialog.show(getSupportFragmentManager(), null);
    }

    private void updateMemoreaAfterMemorization(final MemoreaInfo memoreaInfo) {
        if (memoreaInfo != null) {
            final boolean useNextTime = getIntent().getExtras().getBoolean(CONTINUE);
            if (checkIfFinishedLastTime(useNextTime, memoreaInfo)) {
                DialogFragment dialogFragment = BasicDialog.newInstance(getString(R.string.completed_memorea), String.format(getString(R.string.completed_message), memoreaInfo.mTitle));
                dialogFragment.show(getSupportFragmentManager(), DIALOG);
                ++memoreaInfo.mMemorizationLevel;
                memoreaInfo.mCompleted = true;
                mMemoreaListAdapter.notifyAllItemsChanged();
            } else {
                if (useNextTime) {
                    ++memoreaInfo.mMemorizationLevel;
                    addMemoreaSharedPref(memoreaInfo);
                    mMemoreaListAdapter.notifyDataSetChanged();
                }
                createNotification(true, memoreaInfo);
            }
        }
    }

    private void initializeNoMemoreasView() {
        if (mMemoreaListAdapter.getItemCount() == 0) {
            findViewById(R.id.text_view_no_memoreas_set).setVisibility(View.VISIBLE);
            findViewById(R.id.button_no_memoreas_set).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.text_view_no_memoreas_set).setVisibility(View.GONE);
            findViewById(R.id.button_no_memoreas_set).setVisibility(View.GONE);
        }
    }

    private void registerReceivers() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context ctx, final Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    mMemoreaListAdapter.notifyAllItemsChanged();
                }
            }
        };
        mNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context ctx, final Intent intent) {
                if (intent.getAction().compareTo(NOTIFICATION_READY) == 0) {
                    mMemoreaListAdapter.notifyAllItemsChanged();
                }
            }
        };

        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(mNotificationReceiver, new IntentFilter(NOTIFICATION_READY));
    }

    private void unregisterReceivers() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if (mNotificationReceiver != null) {
            unregisterReceiver(mNotificationReceiver);
        }
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
                mMemoreaListAdapter.onItemMove(dragged.getAdapterPosition(), target.getAdapterPosition());
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
        final MemoreaInfo deletedCard = mMemoreaListAdapter.getItem(viewHolder.getAdapterPosition());
        final int deletedCardPosition = viewHolder.getAdapterPosition();
        final long deletedCardNotificationTime = sSharedPreferences.getLong(String.format(NOTIFICATION_TIME, deletedCard.mId.toString()), 0);
        Snackbar.make(findViewById(R.id.fragment_memorea_list),
                String.format(getString(R.string.snackbar_deleted_message), mMemoreaListAdapter.getItem(viewHolder.getAdapterPosition()).mTitle),
                Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MemoreaListActivity.addMemoreaSharedPref(deletedCard);
                        MemoreaListActivity.sSharedPreferences.edit()
                                .putLong(String.format(NOTIFICATION_TIME, deletedCard.mId.toString()), deletedCardNotificationTime)
                                .commit();
                        mMemoreaListAdapter.onItemAdd(deletedCard, deletedCardPosition);
                        initializeNoMemoreasView();
                    }
                })
                .setActionTextColor(Color.RED)
                .show();
        MemoreaListActivity.removeMemoreaSharedPref(deletedCard);
        cancelCardNotificationGenerator(deletedCard);
        if (deletedCard.getTimeUntilNextAlarm() < 0 && mMemoreaListAdapter.getNumMemoreaReadyMemorize() == 1) {
            clearNotifications();
        }
        mMemoreaListAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        initializeNoMemoreasView();
    }

    private void cancelCardNotificationGenerator(final MemoreaInfo deletedCard) {
        final Intent intent = new Intent(this, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, deletedCard.mNotificationGeneratorId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private void clearNotifications() {
        final NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
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
                memoreaInfo.mNotificationGeneratorId = Integer.parseInt(fields[5]);
                memoreaInfo.mId = UUID.fromString(entry.getKey());
            }
        } finally {
            return memoreaInfo;
        }
    }

    private boolean checkIfFinishedLastTime(final boolean useNextTime, final MemoreaInfo memoreaInfo) {
        return useNextTime && memoreaInfo.mMemorizationLevel == memoreaInfo.getTotalMemorizationLevels();
    }

    private void createNotification(final boolean newNotification, final MemoreaInfo memoreaInfo) {
        final int notificationGeneratorId;
        final long timeUntilMemorization;
        if (newNotification) {
            notificationGeneratorId = (int)(Calendar.getInstance().getTimeInMillis() & 0xfffffff);
            memoreaInfo.mNotificationGeneratorId = notificationGeneratorId;
            timeUntilMemorization = SystemClock.elapsedRealtime() + memoreaInfo.getCurMemorization();
            final SharedPreferences.Editor sharedPreferencesEditor = sSharedPreferences.edit();
            sharedPreferencesEditor.putLong(String.format(NOTIFICATION_TIME, memoreaInfo.mId.toString()), timeUntilMemorization);
            sharedPreferencesEditor.apply();
        } else {
            notificationGeneratorId = memoreaInfo.mNotificationGeneratorId;
            timeUntilMemorization = sSharedPreferences.getLong(String.format(NOTIFICATION_TIME, memoreaInfo.mId.toString()), 0);
        }

        final Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationGeneratorId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.ELAPSED_REALTIME, timeUntilMemorization, pendingIntent);
    }

    /**
     * Removes the memorea from shared preferences
     * @param deletedMemorea Memorea that needs to have its information deleted from shared preferences
     */
    private static void removeMemoreaSharedPref(final MemoreaInfo deletedMemorea) {
        final SharedPreferences.Editor sharedPreferencesEditor = sSharedPreferences.edit();
        sharedPreferencesEditor.remove(deletedMemorea.mId.toString());
        sharedPreferencesEditor.remove(String.format(NOTIFICATION_TIME, deletedMemorea.mId.toString()));
        sharedPreferencesEditor.apply();
    }

    /**
     * Adds the memorea to shared preferences
     * @param addedMemorea Memorea that needs to have its information added to shared preferences
     */
    private static void addMemoreaSharedPref(final MemoreaInfo addedMemorea) {
        final SharedPreferences.Editor sharedPreferencesEditor = sSharedPreferences.edit();
        sharedPreferencesEditor.putString(addedMemorea.mId.toString(), getJSONStringFromArray(addedMemorea.getFields()).toString());
        sharedPreferencesEditor.apply();
    }

    /**
     * Changes the current fragment from the memorea list fragment to the memorea info fragment
     * @param id Id of the memorea that will be displayed in the fragment
     * @param info Information of memorea that will be displayed in the fragment<br>
     *             String array of length 4 with the mTitle, mQuestion, mAnswer, and mHint
     */
    private void startMemoreaInfoActivity(final String id, final String[] info) {
        //if dual pane send this info to fragment, else
        final Intent memoreaInfoIntent = new Intent(this, MemoreaInfoActivity.class);
        memoreaInfoIntent.putExtra(ID, id);
        memoreaInfoIntent.putExtra(MEMOREA_INFO, info);
        startActivity(memoreaInfoIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Generates a JSON array from a String array
     * @param values String array to convert to JSON
     */
    private static JSONArray getJSONStringFromArray(final String[] values) {
        final JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }

    /**
     * Generates a String array from a JSON array
     * @param array JSON array to convert to a String array
     */
    private static String[] getStringArrayFromJSON(final JSONArray array) {
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
