package com.tarian.memorease;

import android.content.Context;
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
import java.util.TimeZone;
import java.util.UUID;

/**
 * Adapter for the RecyclerView
 */
public class MemoreaListAdapter extends RecyclerView.Adapter<MemoreaListAdapter.MemoreaViewHolder> {
    private static final String GMT = "GMT";

    private List<MemoreaInfo> mMemoreaList;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    private Context mContext;

    public MemoreaListAdapter(final Context context) {
        this.mMemoreaList = new ArrayList<>();
        this.mContext = context;
    }

    public void setMOnItemClickListener(final AdapterView.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public MemoreaViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.memorea_card, parent, false);
        return new MemoreaViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final MemoreaViewHolder holder, final int position) {
        final MemoreaInfo memoreaInfo = mMemoreaList.get(position);
        holder.setTitle(memoreaInfo.mTitle);
        setNextMemorizationTime(memoreaInfo, holder);
    }

    /**
     * Returns the number of memoreas
     */
    @Override
    public int getItemCount() {
        return mMemoreaList.size();
    }

    /**
     * Swaps memoreas and notifies
     */
    public void onItemMove(final int from, final int to) {
        Collections.swap(mMemoreaList, from, to);
        notifyItemMoved(from, to);
    }

    /**
     * Deletes a memorea
     */
    public void onItemDismiss(final int position) {
        mMemoreaList.remove(position);
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
        mMemoreaList.add(position, memoreaInfo);
        notifyItemInserted(position);
    }

    /**
     * Returns the memorea with the mId field matching the parameter<br>
     * If none exist, returns null
     */
    public MemoreaInfo getMemoreaByUUID(final UUID uuid) {
        return getMemoreaByUUID(mMemoreaList, uuid);
    }

    /**
     * Returns the memorea at the specified position
     */
    public MemoreaInfo getItem(final int position) {
        return mMemoreaList.get(position);
    }

    /**
     * Calls notifyItemChanged(position) for every position in the memorea list
     */
    public void notifyAllItemsChanged() {
        for (int i = 0; i < mMemoreaList.size(); ++i) {
            notifyItemChanged(i);
        }
    }

    /**
     * Sets the order of the memorea list to be the same order as the IDs in the String parameter
     * @param memoreaOrder correct order of the IDs of the memoreas
     */
    public void setIdOrder(final String[] memoreaOrder) {
        final List<MemoreaInfo> sortedMemoreaList = new ArrayList<>();
        for (String aMemoreaOrder : memoreaOrder) {
            final MemoreaInfo memoreaInfo = getMemoreaByUUID(UUID.fromString(aMemoreaOrder));
            if (memoreaInfo != null) {
                sortedMemoreaList.add(memoreaInfo);
            }
        }
        mMemoreaList = sortedMemoreaList;
    }

    private MemoreaInfo getMemoreaByUUID(final List<MemoreaInfo> list, final UUID uuid) {
        for (MemoreaInfo memoreaInfo : list) {
            if (uuid.equals(memoreaInfo.mId)) {
                return memoreaInfo;
            }
        }

        return null;
    }

    private void onItemHolderClick(final MemoreaViewHolder memoreaViewHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, memoreaViewHolder.itemView,
                    memoreaViewHolder.getAdapterPosition(), memoreaViewHolder.getItemId());
        }
    }

    private void setNextMemorizationTime(final MemoreaInfo memoreaInfo, final MemoreaViewHolder holder) {
        if (!memoreaInfo.mCompleted) {
            final long timeUntilNextAlarm = memoreaInfo.getTimeUntilNextAlarm();
            if (timeUntilNextAlarm < 0) {
                holder.setSpecialMessage(mContext.getString(R.string.memorization_ready));
            } else {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(GMT));
                calendar.setTimeInMillis(timeUntilNextAlarm);
                if (calendar.get(Calendar.MONTH) > 0) {
                    holder.setNextMemorization(calendar.get(Calendar.MONTH)+1, mContext.getString(R.string.month_unit));
                } else if (calendar.get(Calendar.DAY_OF_MONTH) > 1) {
                    holder.setNextMemorization(calendar.get(Calendar.DAY_OF_MONTH), mContext.getString(R.string.day_unit));
                } else if (calendar.get(Calendar.HOUR) > 0) {
                    holder.setNextMemorization(calendar.get(Calendar.HOUR)+1, mContext.getString(R.string.hour_unit));
                } else {
                    holder.setNextMemorization(calendar.get(Calendar.MINUTE)+1, mContext.getString(R.string.minute_unit));
                }
            }
        } else {
            holder.setSpecialMessage(mContext.getString(R.string.completed));
        }
    }

    public int getNumMemoreaReadyMemorize() {
        int i = 0;
        for (MemoreaInfo memoreaInfo : mMemoreaList) {
            if (memoreaInfo.getTimeUntilNextAlarm() < 0) {
                ++i;
            }
        }
        return i;
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
         * Sets the mTitle in the view of the memorea
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
