package com.memorease.memorease;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;

public class MemoreaListActivity extends AppCompatActivity implements AddMemoreaDialog.OnAddMemoreaListener, View.OnClickListener {
    private MemoreaListAdapter memoreaListAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorea_list);

        recyclerView = initRecyclerView();

        memoreaListAdapter = new MemoreaListAdapter(new ArrayList<MemoreaInfo>(30), this);
        recyclerView.setAdapter(memoreaListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.app_name);
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private RecyclerView initRecyclerView() {
        final RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return recyclerView;
    }

    private ItemTouchHelper.SimpleCallback createItemTouchHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder target) {
                    memoreaListAdapter.onItemMove(dragged.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    final MemoreaInfo deletedCard = memoreaListAdapter.getItem(viewHolder.getAdapterPosition());
                    final int deletedCardPosition = viewHolder.getAdapterPosition();
                    Snackbar.make(findViewById(R.id.coordinator_layout), "Deleted the " + memoreaListAdapter.getItem(viewHolder.getAdapterPosition()).title + " memorea", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    memoreaListAdapter.onItemAdd(deletedCard, deletedCardPosition);
                                }
                            })
                            .setActionTextColor(Color.RED)
                            .show();
                    memoreaListAdapter.onItemDismiss(viewHolder.getAdapterPosition());
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
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

    public void addMemorea(final View view) {
        //dialog to add memorea
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddMemoreaDialog addMemoreaDialog = new AddMemoreaDialog();
        addMemoreaDialog.show(fragmentManager, "fragment_add_memorea");
    }

    @Override
    public void addMemoreaCard(final MemoreaInfo memoreaInfo) {
        //create card using this info and add to memoreaList
        memoreaListAdapter.onItemAdd(memoreaInfo);
    }

    @Override
    public void onClick(final View view) {
        int position = recyclerView.getChildAdapterPosition(view);
        MemoreaInfo memoreaInfoClicked = memoreaListAdapter.getItem(position);

        // Create new fragment and transaction
        Bundle memoreaInfo = new Bundle();
        String[] info = new String[4];
        info[0] = memoreaInfoClicked.title;
        info[1] = memoreaInfoClicked.question;
        info[2] = memoreaInfoClicked.answer;
        info[3] = memoreaInfoClicked.hint;
        memoreaInfo.putStringArray("memorea_info", info);

        Fragment newFragment = new MemoreaInfoFragment();
        newFragment.setArguments(memoreaInfo);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_memorea_list, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
