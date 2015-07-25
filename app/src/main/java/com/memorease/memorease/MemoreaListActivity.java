package com.memorease.memorease;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.design.widget.CollapsingToolbarLayout;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
                onBackPressed();
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        super.onBackPressed();
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
                public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder dragged, final RecyclerView.ViewHolder target) {
                    memoreaListAdapter.onItemMove(dragged.getAdapterPosition(), target.getAdapterPosition());
                    return true;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                    final MemoreaInfo deletedCard = memoreaListAdapter.getItem(viewHolder.getAdapterPosition());
                    final int deletedCardPosition = viewHolder.getAdapterPosition();
                    Snackbar.make(findViewById(R.id.fragment_memorea_list), "Deleted the " + memoreaListAdapter.getItem(viewHolder.getAdapterPosition()).title + " memorea", Snackbar.LENGTH_LONG)
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

    public void addMemorea(final View view) {
        // dialog to add memorea
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final AddMemoreaDialog addMemoreaDialog = new AddMemoreaDialog();
        addMemoreaDialog.show(fragmentManager, null);
    }

    public void editMemorea(final View view) {
        // dialog to edit memorea
        final Bundle memoreaInfo = new Bundle();
        memoreaInfo.putStringArray("edit_memorea_info", getMemoreaInfoFromFragment(getSupportFragmentManager().findFragmentByTag("fragment_memorea_info")));

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final AddMemoreaDialog addMemoreaDialog = new AddMemoreaDialog();
        addMemoreaDialog.setArguments(memoreaInfo);
        addMemoreaDialog.show(fragmentManager, null);
    }

    private String[] getMemoreaInfoFromFragment(final Fragment fragment) {
        return ((MemoreaInfoFragment)fragment).memoreaInfo;
    }

    @Override
    public void addMemoreaCard(final MemoreaInfo memoreaInfo) {
        //create card using this info and add to memoreaList
        memoreaListAdapter.onItemAdd(memoreaInfo);
    }

    @Override
    public void onClick(final View view) {
        final int position = recyclerView.getChildAdapterPosition(view);
        final MemoreaInfo memoreaInfoClicked = memoreaListAdapter.getItem(position);

        // Create new fragment and transaction
        final Bundle memoreaInfo = new Bundle();
        final String[] info = new String[4];
        info[0] = memoreaInfoClicked.title;
        info[1] = memoreaInfoClicked.question;
        info[2] = memoreaInfoClicked.answer;
        info[3] = memoreaInfoClicked.hint;
        memoreaInfo.putStringArray("memorea_info", info);

        final Fragment infoFragment = new MemoreaInfoFragment();
        infoFragment.setArguments(memoreaInfo);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_memorea_list, infoFragment, "fragment_memorea_info");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
