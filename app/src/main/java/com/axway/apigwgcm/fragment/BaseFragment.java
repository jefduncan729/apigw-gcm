package com.axway.apigwgcm.fragment;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by su on 12/23/2014.
 */
public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private boolean fromSavedState;

    public BaseFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromSavedState = (savedInstanceState != null);
    }

    public boolean isFromSavedState() {
        return fromSavedState;
    }

    protected boolean hasOptsMenu() {
        return false;
    }
}
