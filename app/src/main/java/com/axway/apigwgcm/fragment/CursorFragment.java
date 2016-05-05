package com.axway.apigwgcm.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.StringUtil;
import com.axway.apigwgcm.view.BasicViewHolder;
import com.axway.apigwgcm.view.CursorViewBinder;
import com.axway.apigwgcm.view.EventViewBinder;
import com.axway.apigwgcm.view.TriggerViewBinder;

import java.util.Locale;

/**
 * Created by su on 12/5/2014.
 */
public class CursorFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = CursorFragment.class.getSimpleName();

    public static final int PRIMARY_LOADER = 1;

    private CardView cardView;
    private CursorViewBinder viewBinder;

    protected Uri primaryUri;
    protected boolean dirty;

    public CursorFragment() {
        super();
        cardView = null;
        viewBinder = null;
        primaryUri = null;
        dirty = false;
    }

    public static CursorFragment newInstance(final Uri uri) {
        CursorFragment rv = new CursorFragment();
        rv.setPrimaryUri(uri);
        return rv;
    }

    protected void setPrimaryUri(Uri uri) {
        this.primaryUri = uri;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (primaryUri == null) {
            Log.d(TAG, "primaryUri not set, getting from arguments");
            if (getArguments() != null)
                primaryUri = getArguments().getParcelable(Intent.EXTRA_UID);
        }
//        if (primaryUri == null)
//            throw new IllegalStateException("must provide a primaryUri");
        Log.d(TAG, StringUtil.format("primaryUri: %s", primaryUri));
        setHasOptionsMenu(hasOptsMenu());
//        primaryUri = getArguments().getParcelable(Intent.EXTRA_UID);
//        if (primaryUri == null)
//            ;   //throw new IllegalStateException("must provide a primaryUri");
//        else
        if (primaryUri == null)
        {
            Log.d(TAG, "no primaryUri, must be an insert");
        }
        else {
            getLoaderManager().initLoader(PRIMARY_LOADER, getArguments(), this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.single_frame, null);
        View v = inflater.inflate(R.layout.card_view, null);
        cardView = (CardView)v.findViewById(R.id.card01);
        BasicViewHolder holder = new BasicViewHolder(cardView);
        cardView.setTag(holder);
        ((ViewGroup)rv).addView(v);
        return rv;
    }

    protected CursorViewBinder getViewBinder() {
        if (viewBinder == null) {
            int dbType = DbHelper.matchUri(DbHelper.getBaseUri(primaryUri));
            if (DbHelper.EVENTS == dbType)
                viewBinder = new EventViewBinder();
            else if (DbHelper.TRIGGERS == dbType)
                viewBinder = new TriggerViewBinder();
            else
                viewBinder = new CursorViewBinder();
            viewBinder.setIconId(DbHelper.getIconId(dbType));
        }
        return viewBinder;
    }

    public void setViewBinder(CursorViewBinder newVal) {
        viewBinder = newVal;
    }

    protected void updateView(final Cursor data) {
        if (cardView == null || getViewBinder() == null) {
            Log.d(TAG, "updateView: improper view/viewBinder");
            return;
        }
        Log.d(TAG, "updateView");
        getViewBinder().bindDetailView(cardView, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return createLoader(id, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PRIMARY_LOADER) {
            updateView(data);
            if (data != null)
                data.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PRIMARY_LOADER) {
        }
    }

    protected CursorLoader createLoader(int id, Bundle args) {
        CursorLoader rv = null;
        if (id == PRIMARY_LOADER)
            rv = defaultLoader();
        return rv;
    }

    protected boolean hasOptsMenu() {
        return false;
    }

    public void refresh() {
        if (isVisible()) {
            getLoaderManager().restartLoader(PRIMARY_LOADER, getArguments(), this);
        }
    }

    protected CursorLoader defaultLoader() {
        Log.d(TAG, StringUtil.format("create defaultLoader: %s", primaryUri));
        return new CursorLoader(getActivity(), primaryUri, getProjection(), null, null, getSortOrder());
    }

    protected String[] getProjection() {
        return null;
    }

    protected String getSortOrder() {
        return null;
    }

    public boolean isDirty() {
        return dirty;
    }
}
