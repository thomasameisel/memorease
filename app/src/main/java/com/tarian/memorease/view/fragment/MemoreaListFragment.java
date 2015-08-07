package com.tarian.memorease.view.fragment;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tarian.memorease.MemoreaAlarmManager;
import com.tarian.memorease.model.Memorea;
import com.tarian.memorease.MemoreaSharedPreferences;
import com.tarian.memorease.presenter.MemoreaListPresenter;
import com.tarian.memorease.presenter.MemoreaListAdapter;
import com.tarian.memorease.R;

import java.util.UUID;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;


/**
 * Fragment that displays the memoreas in a RecyclerView
 */
@RequiresPresenter(MemoreaListPresenter.class)
public class MemoreaListFragment extends NucleusSupportFragment<MemoreaListPresenter> {

    private static final String NOTIFICATION_READY = "notificationReady";
    private static final String ID_SEPARATOR = ",";

    private String mUpdateMemoreaId;
    private MemoreaListAdapter mMemoreasAdapter;
    private TextView mNoMemoreasText;
    private Button mNoMemoreasButton;
    private BroadcastReceiver mBroadcastReceiver;

    public MemoreaListFragment() {}

    @SuppressLint("ValidFragment")
    public MemoreaListFragment(String id) {
        mUpdateMemoreaId = id;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memorea_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView memoreasView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mNoMemoreasText = (TextView)view.findViewById(R.id.text_view_no_memoreas_set);
        mNoMemoreasButton = (Button)view.findViewById(R.id.button_no_memoreas_set);

        memoreasView.setScrollContainer(false);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        memoreasView.setLayoutManager(linearLayoutManager);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(memoreasView);
        mMemoreasAdapter = new MemoreaListAdapter(getActivity());
        mMemoreasAdapter.setOnItemClickListener(getActivity());
        memoreasView.setAdapter(mMemoreasAdapter);

        mMemoreasAdapter.addAll(MemoreaSharedPreferences.getAll(getActivity()));
        showNoMemoreasView(mMemoreasAdapter.getItemCount() == 0);

        if (mUpdateMemoreaId != null) {
            MemoreaAlarmManager.createNotification(getActivity(), mMemoreasAdapter
                    .getMemoreaByUUID(UUID.fromString(mUpdateMemoreaId)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context ctx, final Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0 ||
                        intent.getAction().compareTo(NOTIFICATION_READY) == 0) {
                    getPresenter().notifyAllItemsChanged();
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(NOTIFICATION_READY);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

        orderMemoreas();

        mMemoreasAdapter.notifyAllItemsChanged();
        removeNotification();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }

        MemoreaSharedPreferences.setMemoreaOrder(getActivity(), getMemoreasOrder());
    }

    public void addMemorea(String[] fields) {
        final Memorea memorea = new Memorea(fields[0], fields[1], fields[2], fields[3],
                Integer.valueOf(fields[4]));
        memorea.generateNewId();
        MemoreaAlarmManager.createNotification(getActivity(), memorea);
        getPresenter().addMemoreaSharedPref(getActivity(), memorea.mId.toString(), fields);
        mMemoreasAdapter.onItemAdd(memorea);
    }

    public void notifyAllItemsChanged() {
        mMemoreasAdapter.notifyAllItemsChanged();
    }

    public Memorea getMemoreaByPosition(int position) {
        return mMemoreasAdapter.getItem(position);
    }

    public void showNoMemoreasView(boolean show) {
        if (show) {
            mNoMemoreasText.setVisibility(View.VISIBLE);
            mNoMemoreasButton.setVisibility(View.VISIBLE);
        } else {
            mNoMemoreasText.setVisibility(View.GONE);
            mNoMemoreasButton.setVisibility(View.GONE);
        }
    }

    private ItemTouchHelper.SimpleCallback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(final RecyclerView recyclerView,
                                  final RecyclerView.ViewHolder dragged,
                                  final RecyclerView.ViewHolder target) {
                mMemoreasAdapter.onItemMove(dragged.getAdapterPosition(),
                        target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final int deletedCardPosition = viewHolder.getAdapterPosition();
                final Memorea deletedCard = mMemoreasAdapter.getItem(deletedCardPosition);
                dismissMemorea(deletedCardPosition, deletedCard);
            }

            @Override
            public int getMovementFlags(final RecyclerView recyclerView,
                                        final RecyclerView.ViewHolder viewHolder) {
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

    private void dismissMemorea(final int deletedCardPosition, final Memorea deletedCard) {
        mMemoreasAdapter.onItemDismiss(deletedCardPosition);
        getPresenter().deleteMemoreaSharedPref(getActivity(), deletedCard);
        MemoreaAlarmManager.cancelNotification(getActivity(), deletedCard.mNotificationId);
        if (deletedCard.getTimeUntilNextAlarm(getActivity()) < 0 &&
                mMemoreasAdapter.getNumMemoreaReadyMemorize() == 0) {
            removeNotification();
        }
        showNoMemoreasView(mMemoreasAdapter.getItemCount() == 0);
        if (getView() != null) {
            Snackbar.make(getView().findViewById(R.id.recycler_view),
                    String.format(getString(R.string.snackbar_deleted_message),
                            deletedCard.mTitle), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getPresenter().addMemoreaSharedPref(getActivity(), deletedCard);
                            mMemoreasAdapter.onItemAdd(deletedCard, deletedCardPosition);
                            showNoMemoreasView(false);
                        }
                    })
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }

    private void removeNotification() {
        final NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void orderMemoreas() {
        if (MemoreaSharedPreferences.getMemoreaOrder(getActivity()) != null) {
            final String[] memoreaOrder = MemoreaSharedPreferences.getMemoreaOrder(getActivity())
                    .split(ID_SEPARATOR);
            if (memoreaOrder.length > 1) {
                mMemoreasAdapter.setIdOrder(memoreaOrder);
            }
        }
    }

    private String getMemoreasOrder() {
        String memoreaOrder = "";
        for (int i = 0; i < mMemoreasAdapter.getItemCount(); ++i) {
            memoreaOrder+= mMemoreasAdapter.getItem(i).mId.toString();
            if (i+1 < mMemoreasAdapter.getItemCount()) {
                memoreaOrder+=ID_SEPARATOR;
            }
        }
        return memoreaOrder;
    }
}
