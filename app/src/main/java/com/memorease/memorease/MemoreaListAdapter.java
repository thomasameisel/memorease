package com.memorease.memorease;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Adapter for the RecyclerView
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
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.memorea_card, parent, false);
        return new MemoreaViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final MemoreaViewHolder holder, final int position) {
        MemoreaInfo memoreaInfo = memoreaList.get(position);
        holder.setTitle(memoreaInfo.title);
        setNextMemorizationTime("Memorization ready!", memoreaInfo, holder);
    }

    /**
     * Returns the number of memoreas
     */
    @Override
    public int getItemCount() {
        return memoreaList.size();
    }

    /**
     * Swaps memoreas and notifies
     */
    public void onItemMove(final int from, final int to) {
        Collections.swap(memoreaList, from, to);
        notifyItemMoved(from, to);
    }

    /**
     * Deletes a memorea
     */
    public void onItemDismiss(final int position) {
        memoreaList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Adds entire ArrayList to the memorea list
     * @param memoreaInfoList ArrayList of MemoreaInfo to be added
     */
    public void addAll(final ArrayList<MemoreaInfo> memoreaInfoList) {
        for (MemoreaInfo memoreaInfo : memoreaInfoList) {
            onItemAdd(memoreaInfo);
        }
    }

    /**
     * Adds a memorea to the bottom of the memorea list
     */
    public void onItemAdd(final MemoreaInfo memoreaInfo) {
        onItemAdd(memoreaInfo, getItemCount());
    }

    /**
     * Adds a memorea to the memorea list in the position specified
     */
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

    /**
     * Returns the memorea with the id field matching the parameter<br>
     * If none exist, returns null
     */
    public MemoreaInfo getMemoreaByUUID(final UUID uuid) {
        for (MemoreaInfo memoreaInfo : memoreaList) {
            if (uuid.equals(memoreaInfo.id)) {
                return memoreaInfo;
            }
        }

        return null;
    }

    /**
     * Returns the memorea at the specified position
     */
    public MemoreaInfo getItem(final int position) {
        return memoreaList.get(position);
    }

    private void setNextMemorizationTime(final String memorizationReady, final MemoreaInfo memoreaInfo, final MemoreaViewHolder holder) {
        if (!memoreaInfo.completed) {
            final long timeUntilNextAlarm = memoreaInfo.getTimeUntilNextAlarm();
            if (timeUntilNextAlarm < 0) {
                holder.setSpecialMessage(memorizationReady);
            } else {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                calendar.setTimeInMillis(timeUntilNextAlarm);
                if (calendar.get(Calendar.MONTH) > 0) {
                    holder.setNextMemorization(calendar.get(Calendar.MONTH)+1, "month");
                } else if (calendar.get(Calendar.DAY_OF_MONTH) > 1) {
                    holder.setNextMemorization(calendar.get(Calendar.DAY_OF_MONTH), "day");
                } else if (calendar.get(Calendar.HOUR) > 0) {
                    holder.setNextMemorization(calendar.get(Calendar.HOUR)+1, "hour");
                } else {
                    holder.setNextMemorization(calendar.get(Calendar.MINUTE)+1, "minute");
                }
            }
        } else {
            holder.setSpecialMessage("Completed!");
        }
    }

    /**
     * Calls notifyItemChanged(position) for every position in the memorea list
     */
    public void notifyAllItemsChanged() {
        for (int i = 0; i < memoreaList.size(); ++i) {
            notifyItemChanged(i);
        }
    }

    /**
     * Binds the memorea to its view
     */
    public static class MemoreaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title, nextMemorizationLabel, nextMemorization, specialMessage;
        private MemoreaListAdapter memoreaListAdapter;

        /**
         * Constructor for MemoreaViewHolder
         * @param memoreaView View corresponds to the memorea
         * @param memoreaListAdapter RecyclerView Adapter for the memorea list
         */
        public MemoreaViewHolder(final View memoreaView, final MemoreaListAdapter memoreaListAdapter) {
            super(memoreaView);
            memoreaView.findViewById(R.id.card_view_memorea).setOnClickListener(this);

            title = (TextView) memoreaView.findViewById(R.id.text_view_memorea_title);
            nextMemorization = (TextView) memoreaView.findViewById(R.id.text_view_next_memorization);
            nextMemorizationLabel = (TextView) memoreaView.findViewById(R.id.text_view_next_memorization_label);
            specialMessage = (TextView) memoreaView.findViewById(R.id.text_view_special_info);

            this.memoreaListAdapter = memoreaListAdapter;
        }

        /**
         * Calls onItemHolderClick for this memorea item
         */
        @Override
        public void onClick(final View v) {
            memoreaListAdapter.onItemHolderClick(this);
        }

        /**
         * Sets the title in the view of the memorea
         */
        public void setTitle(String title) {
            this.title.setText(title);
        }

        /**
         * Sets the time remaining until the next memorization in the view of the memorea
         * @param time Time remaining until the next memorization
         * @param timeUnit Unit of the time remaining until the next memorization
         */
        public void setNextMemorization(final int time, final String timeUnit) {
            showSpecialMessage(false);
            String nextMemorizationText;
            if (time > 1) {
                nextMemorizationText = String.format("%d %s%s", time, timeUnit, "s");
            } else if (time == 1) {
                nextMemorizationText = String.format("1 %s", timeUnit);
            } else {
                nextMemorizationText = String.format("Less than 1 %s", timeUnit);
            }
            nextMemorization.setText(nextMemorizationText);
        }

        /**
         * Sets the special message in the view of the memorea
         * @param specialMessage Special message (either "Memorization ready!" and "Completed!")
         */
        public void setSpecialMessage(final String specialMessage) {
            showSpecialMessage(true);
            this.specialMessage.setText(specialMessage);
        }

        /**
         * Enables or disables the special message view
         * @param specialMessageVisible if true, the special message is shown<br>
         *                              if false, the time remaining message is shown
         */
        public void showSpecialMessage(final boolean specialMessageVisible) {
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
