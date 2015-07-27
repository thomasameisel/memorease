package com.memorease.memorease;

import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaListAdapter extends RecyclerView.Adapter<MemoreaListAdapter.MemoreaViewHolder> {
    private final List<MemoreaInfo> memoreaList;
    private AdapterView.OnItemClickListener onItemClickListener;

    public MemoreaListAdapter() {
        this.memoreaList = new ArrayList<>();
    }

    public void setOnItemClickListener(final AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MemoreaViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.memorea_card, parent, false);
        return new MemoreaViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final MemoreaViewHolder holder, final int position) {
        MemoreaInfo memoreaInfo = memoreaList.get(position);
        memoreaInfo.holder = holder;
        holder.setTitle(memoreaInfo.title);
        correctNextMemorizationTime("Memorization ready!", memoreaInfo);
    }

    @Override
    public int getItemCount() {
        return memoreaList.size();
    }

    public void onItemMove(final int from, final int to) {
        Collections.swap(memoreaList, from, to);
        notifyItemMoved(from, to);
    }

    public void onItemDismiss(final int position) {
        memoreaList.remove(position);
        notifyItemRemoved(position);
    }

    public void addAll(ArrayList<MemoreaInfo> memoreaInfoList) {
        for (MemoreaInfo memoreaInfo : memoreaInfoList) {
            onItemAdd(memoreaInfo);
        }
    }

    public void onItemAdd(final MemoreaInfo memoreaInfo) {
        onItemAdd(memoreaInfo, getItemCount());
    }

    public void onItemAdd(final MemoreaInfo memoreaInfo, final int position) {
        memoreaList.add(position, memoreaInfo);
        notifyItemInserted(position);
    }

    private void onItemHolderClick(final MemoreaViewHolder memoreaViewHolder) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(null, memoreaViewHolder.itemView,
                    memoreaViewHolder.getAdapterPosition(), memoreaViewHolder.getItemId());
        }
    }

    public MemoreaInfo getMemoreaByUUID(UUID uuid) {
        for (MemoreaInfo memoreaInfo : memoreaList) {
            if (uuid.equals(memoreaInfo.id)) {
                return memoreaInfo;
            }
        }

        return null;
    }

    public MemoreaInfo getItem(final int position) {
        return memoreaList.get(position);
    }

    public void correctNextMemorizationTimesAll(final String memorizationReady) {
        for (int i = 0; i < getItemCount(); ++i) {
            final MemoreaInfo memoreaInfo = getItem(i);
            if (memoreaInfo.holder != null) {
                correctNextMemorizationTime(memorizationReady, memoreaInfo);
            }
        }
    }

    private void correctNextMemorizationTime(final String memorizationReady, final MemoreaInfo memoreaInfo) {
        if (!memoreaInfo.completed) {
            final float timeUntilNextAlarm = memoreaInfo.getTimeUntilNextAlarm();
            if (timeUntilNextAlarm < 0) {
                memoreaInfo.holder.showSpecialMessage(memorizationReady);
            } else {
                int minutesUntilNextAlarm = (int) timeUntilNextAlarm / 60000;
                if (minutesUntilNextAlarm <= 0) {
                    minutesUntilNextAlarm = 1;
                }
                memoreaInfo.holder.setNextMemorization(minutesUntilNextAlarm);
            }
        } else {
            memoreaInfo.holder.showSpecialMessage("Completed!");
        }
    }

    public static class MemoreaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public boolean specialMessageDisplayed = false;

        private TextView title, nextMemorizationLabel, nextMemorization, specialMessage;
        private MemoreaListAdapter memoreaListAdapter;

        public MemoreaViewHolder(final View v, final MemoreaListAdapter memoreaListAdapter) {
            super(v);
            v.findViewById(R.id.card_view_memorea).setOnClickListener(this);

            title = (TextView) v.findViewById(R.id.text_view_memorea_title);
            nextMemorization = (TextView) v.findViewById(R.id.text_view_next_memorization);
            nextMemorizationLabel = (TextView) v.findViewById(R.id.text_view_next_memorization_label);
            specialMessage = (TextView) v.findViewById(R.id.text_view_special_info);

            this.memoreaListAdapter = memoreaListAdapter;
        }

        @Override
        public void onClick(final View v) {
            memoreaListAdapter.onItemHolderClick(this);
        }

        public void setTitle(String title) {
            this.title.setText(title);
        }

        public void setNextMemorization(final int nextMemorizationDisplayed) {
            if (nextMemorizationDisplayed > 1) {
                this.nextMemorization.setText(Integer.toString(nextMemorizationDisplayed) + " minutes");
            } else {
                this.nextMemorization.setText("1 minute");
            }
        }

        public void showSpecialMessage(final String specialMessage) {
            setSpecialMessageViews(true);
            this.specialMessage.setText(specialMessage);
        }

        public void setSpecialMessageViews(final boolean specialMessageVisible) {
            specialMessageDisplayed = specialMessageVisible;
            if (specialMessageVisible) {
                nextMemorization.setVisibility(View.INVISIBLE);
                nextMemorizationLabel.setVisibility(View.INVISIBLE);
                specialMessage.setVisibility(View.VISIBLE);
            } else {
                nextMemorization.setVisibility(View.VISIBLE);
                nextMemorizationLabel.setVisibility(View.VISIBLE);
                specialMessage.setVisibility(View.GONE);
            }
        }
    }
}
