package com.memorease.memorease;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Tommy on 7/22/2015.
 */
public class MemoreaListAdapter extends RecyclerView.Adapter<MemoreaListAdapter.MemoreaViewHolder> {
    private final List<MemoreaInfo> memoreaList;
    private final View.OnClickListener onClickListener;

    public MemoreaListAdapter(final List<MemoreaInfo> memoreaList, final View.OnClickListener listener) {
        this.memoreaList = memoreaList;
        this.onClickListener = listener;
    }

    @Override
    public MemoreaViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.memorea_card, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new MemoreaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MemoreaViewHolder holder, final int position) {
        MemoreaInfo memoreInfo = memoreaList.get(position);
        MemoreaViewHolder.title.setText(memoreInfo.title);
        MemoreaViewHolder.nextMemorization.setText(Integer.toString(memoreInfo.nextMemorization)+" minutes");
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

    public void onItemAdd(final MemoreaInfo memoreaInfo) {
        onItemAdd(memoreaInfo, getItemCount());
    }

    public void onItemAdd(final MemoreaInfo memoreaInfo, final int position) {
        memoreaList.add(position, memoreaInfo);
        notifyItemInserted(position);
    }

    public static class MemoreaViewHolder extends RecyclerView.ViewHolder {
        protected static TextView title;
        protected static TextView nextMemorization;

        public MemoreaViewHolder(final View v) {
            super(v);
            title =  (TextView) v.findViewById(R.id.text_view_memorea_title);
            nextMemorization = (TextView)  v.findViewById(R.id.text_view_next_memorization);
        }
    }

    public MemoreaInfo getItem(final int position) {
        return memoreaList.get(position);
    }
}
