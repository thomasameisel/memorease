package com.memorease.memorease;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.UUID;


/**
 * A placeholder fragment containing a simple view.
 */
public class MemoreaListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private MemoreaListAdapter memoreaListAdapter;
    private RecyclerView recyclerView;

    public MemoreaListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_memorea_list, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        recyclerView = initRecyclerView(view);

        memoreaListAdapter = new MemoreaListAdapter();
        memoreaListAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(memoreaListAdapter);

        return view;
    }

    private RecyclerView initRecyclerView(final View view) {
        final RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
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
                final int draggedPosition = dragged.getAdapterPosition();
                final int targetPosition = target.getAdapterPosition();
                memoreaListAdapter.getItem(draggedPosition).position = targetPosition;
                memoreaListAdapter.getItem(targetPosition).position = draggedPosition;
                ((MemoreaListActivity)getActivity()).updateSharedPrefOnMove(memoreaListAdapter.getItem(draggedPosition), memoreaListAdapter.getItem(targetPosition));
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
                                ((MemoreaListActivity)getActivity()).updateSharedPrefOnAdd(deletedCard);
                                memoreaListAdapter.onItemAdd(deletedCard, deletedCardPosition);
                            }
                        })
                        .setActionTextColor(Color.RED)
                        .show();
                ((MemoreaListActivity)getActivity()).updateSharedPrefOnDelete(deletedCard);
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

    public void addMemoreaCard(final MemoreaInfo memoreaInfo) {
        memoreaListAdapter.onItemAdd(memoreaInfo);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final MemoreaInfo memoreaInfoClicked = memoreaListAdapter.getItem(position);

        // Create new fragment and transaction
        final String[] info = new String[5];
        info[0] = memoreaInfoClicked.id.toString();
        info[1] = memoreaInfoClicked.title;
        info[2] = memoreaInfoClicked.question;
        info[3] = memoreaInfoClicked.answer;
        info[4] = memoreaInfoClicked.hint;

        ((MemoreaListActivity)getActivity()).openMemoreaInfoFragment(info, position);
    }
}
