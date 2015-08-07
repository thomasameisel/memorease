package com.tarian.memorease.presenter;

import android.content.Context;
import android.os.Bundle;

import com.tarian.memorease.MemoreaSharedPreferences;
import com.tarian.memorease.view.fragment.MemoreaListFragment;

import nucleus.presenter.RxPresenter;

/**
 * Created by Tommy on 8/5/2015.
 */
public class MemorizeScreenPresenter extends RxPresenter<MemoreaListFragment> {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    public void updateMemorea(Context context, String id, String[] updatedFields) {
        MemoreaSharedPreferences.update(context, id, updatedFields);
    }
}
