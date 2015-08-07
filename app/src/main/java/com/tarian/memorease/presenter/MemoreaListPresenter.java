package com.tarian.memorease.presenter;

import android.content.Context;
import android.os.Bundle;

import com.tarian.memorease.model.Memorea;
import com.tarian.memorease.MemoreaSharedPreferences;
import com.tarian.memorease.view.fragment.MemoreaListFragment;

import nucleus.presenter.RxPresenter;

public class MemoreaListPresenter extends RxPresenter<MemoreaListFragment> {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }


    public void addMemoreaSharedPref(Context context, Memorea addedMemorea) {
        addMemoreaSharedPref(context, addedMemorea.mId.toString(), addedMemorea.getFields());
    }

    public void addMemoreaSharedPref(Context context, String id, String[] fields) {
        MemoreaSharedPreferences.add(context, id, fields);
    }

    public void deleteMemoreaSharedPref(Context context, Memorea deletedMemorea) {
        MemoreaSharedPreferences.remove(context, deletedMemorea);
    }

    public void updateMemoreaSharedPref(Context context, String id, String[] fields) {
        MemoreaSharedPreferences.update(context, id, fields);
    }

    public void notifyAllItemsChanged() {
        if (getView() != null) {
            getView().notifyAllItemsChanged();
        }
    }
}
