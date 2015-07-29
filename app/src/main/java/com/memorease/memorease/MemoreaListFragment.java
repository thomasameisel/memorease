package com.memorease.memorease;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MemoreaListFragment extends Fragment implements AdapterView.OnItemClickListener {
    public interface OnMemoreaListFragmentListener {
        void setSupportActionBar(Toolbar view);
        ActionBar getSupportActionBar();

        void openMemoreaInfoFragment(String[] info, int position);
        int getNumNotifications();

        void setNumNotifications(int numNotifications);
    }

    private MemoreaListAdapter memoreaListAdapter;
    private RecyclerView recyclerView;
    private BroadcastReceiver broadcastReceiver;
    private OnMemoreaListFragmentListener listener;

    public MemoreaListFragment() {}

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnMemoreaListFragmentListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnAddMemoreaListener");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_list, container, false);
        listener.setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        listener.getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        recyclerView = initRecyclerView(view);

        memoreaListAdapter = new MemoreaListAdapter();
        memoreaListAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(memoreaListAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    memoreaListAdapter.notifyDataSetChanged();
                }
            }
        };

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        memoreaListAdapter.notifyDataSetChanged();
        clearNotification();
    }

    private void clearNotification() {
        listener.setNumNotifications(0);
        NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    private RecyclerView initRecyclerView(final View view) {
        final RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setScrollContainer(false);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
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
                final MemoreaInfo deletedCard = memoreaListAdapter.getItem(viewHolder.getAdapterPosition());
                final int deletedCardPosition = viewHolder.getAdapterPosition();
                Snackbar.make(getView().findViewById(R.id.fragment_memorea_list), "Deleted the " + memoreaListAdapter.getItem(viewHolder.getAdapterPosition()).title + " Memorea", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MemoreaListActivity.updateSharedPref(deletedCard);
                                memoreaListAdapter.onItemAdd(deletedCard, deletedCardPosition);
                            }
                        })
                        .setActionTextColor(Color.RED)
                        .show();
                MemoreaListActivity.updateSharedPrefOnDelete(deletedCard);
                final Intent intent = new Intent(getActivity(), AlarmReceiver.class);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), deletedCard.notificationGeneratorId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                final AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
                if (deletedCard.getTimeUntilNextAlarm() < 0 && listener.getNumNotifications() == 1) {
                    clearNotification();
                }
                memoreaListAdapter.onItemDismiss(viewHolder.getAdapterPosition());
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

    public MemoreaListAdapter getMemoreaListAdapter() {
        return memoreaListAdapter;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final MemoreaInfo memoreaInfoClicked = memoreaListAdapter.getItem(position);

        if (memoreaInfoClicked.getTimeUntilNextAlarm() < 0 && !memoreaInfoClicked.completed) {
            Intent memorizeScreenIntent = new Intent (getActivity(), MemorizeScreenActivity.class);
            memorizeScreenIntent.putExtra("title", memoreaInfoClicked.title);
            memorizeScreenIntent.putExtra("question", memoreaInfoClicked.question);
            memorizeScreenIntent.putExtra("answer", memoreaInfoClicked.answer);
            memorizeScreenIntent.putExtra("hint", memoreaInfoClicked.hint);
            memorizeScreenIntent.putExtra("id", memoreaInfoClicked.id.toString());
            startActivity(memorizeScreenIntent);
        } else {
            final String[] info = new String[5];
            info[0] = memoreaInfoClicked.id.toString();
            info[1] = memoreaInfoClicked.title;
            info[2] = memoreaInfoClicked.question;
            info[3] = memoreaInfoClicked.answer;
            info[4] = memoreaInfoClicked.hint;

            listener.openMemoreaInfoFragment(info, position);
        }
    }
}
