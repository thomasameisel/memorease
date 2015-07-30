package com.tarian.memorease;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Fragment that displays the memoreas in a RecyclerView
 */
public class MemoreaListFragment extends Fragment {
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memorea_list, container, false);
    }
}
