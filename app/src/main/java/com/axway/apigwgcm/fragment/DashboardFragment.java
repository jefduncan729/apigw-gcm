package com.axway.apigwgcm.fragment;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.view.BasicViewHolder;
import com.axway.apigwgcm.util.CountRowsTask;
import com.axway.apigwgcm.view.CursorViewBinder;
import com.axway.apigwgcm.view.EventViewBinder;

/**
 * Created by su on 11/21/2014.
 */
public class DashboardFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, CountRowsTask.Callbacks {

    private static final String TAG = DashboardFragment.class.getSimpleName();

    public interface Callbacks {
        public void onDashboardItemSelected(final Uri uri);
    }

    private LayoutInflater inflater;
    private ViewGroup parentView;
    private TextView txtTitle;
    private Chronometer chronometer;
    private CursorViewBinder viewBinder;
    private Callbacks callbacks;
    private Uri primaryUri;
    private boolean fromSaved;
    private int totalRows;

    public static DashboardFragment newInstance(final Uri uri) {
        DashboardFragment rv = new DashboardFragment();
        Bundle args = new Bundle();
        args.putParcelable(Intent.EXTRA_UID, uri);
        rv.setArguments(args);
        return rv;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        primaryUri = getArguments().getParcelable(Intent.EXTRA_UID);
        if (savedInstanceState != null) {
            fromSaved = true;
            totalRows = savedInstanceState.getInt(Constants.EXTRA_ROW_COUNT);
            getLoaderManager().restartLoader(1, getArguments(), this);
        }
        else {
            fromSaved = false;
            totalRows = 0;
            CountRowsTask task = new CountRowsTask(getActivity());
            task.setCallbacks(this);
            task.execute(DbHelper.getBaseUri(primaryUri));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.EXTRA_ROW_COUNT, totalRows);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callbacks)
            callbacks = (Callbacks)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.section, null);
        this.inflater = inflater;
        txtTitle = (TextView)rv.findViewById(android.R.id.title);
        int n = DbHelper.matchUri(primaryUri);
        ImageView img = (ImageView) rv.findViewById(android.R.id.icon);
        if (img != null) {
            img.setImageResource(DbHelper.getIconId(n));
            img.setVisibility(View.VISIBLE);
        }
        parentView = (ViewGroup)rv.findViewById(R.id.container06);
        return rv;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void buildSection(Cursor c) {
        if (c == null)
            return;
        String t = DbHelper.getTitle(primaryUri);
        txtTitle.setText("Recent " + t);
        while (c.moveToNext()) {
            View itemView = inflater.inflate(R.layout.recent_item, null);
            BasicViewHolder holder = new BasicViewHolder(itemView);
            holder.addAuxView(itemView.findViewById(R.id.chron01));
            itemView.setTag(holder);
            buildItemView(itemView, c);
            if (parentView.getChildCount() > 0)
                parentView.addView(newDivider());
            parentView.addView(itemView);
        }
        final View showAll = inflater.inflate(R.layout.recent_item, null);
        BasicViewHolder holder = new BasicViewHolder(showAll);
        showAll.setTag(holder);
        showAll.setOnClickListener(this);
        holder.setText1("\nAll " + t + " (" + totalRows + ")");
        holder.getTextView1().setTag(null);
        holder.getTextView2().setVisibility(View.GONE);
        if (parentView.getChildCount() > 0)
            parentView.addView(newDivider());
        parentView.addView(showAll);
    }

    private View newDivider() {
        return inflater.inflate(R.layout.section_divider, null);
    }

    private void buildItemView(final View v, final Cursor c) {
        if (c == null)
            return;
        BasicViewHolder holder = (BasicViewHolder)v.getTag();
        if (holder == null)
            return;
        v.setOnClickListener(this);
        getViewBinder().bindListView(v, c);
    }

    private CursorViewBinder getViewBinder() {
        if (viewBinder == null) {
            int m = DbHelper.matchUri(DbHelper.getBaseUri(primaryUri));
            if (m == DbHelper.EVENTS)
                viewBinder = new EventViewBinder();
            else
                viewBinder = new CursorViewBinder();
        }
        return viewBinder;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (primaryUri == null)
            return null;
        return new CursorLoader(getActivity(), primaryUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        buildSection(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        buildSection(null);
    }

    @Override
    public void onClick(View v) {
        Object t = (v == null ? null : v.getTag());
        if (t == null || !(t instanceof BasicViewHolder))
            return;
        BasicViewHolder h = (BasicViewHolder)t;
        Long tag = null;
        if (h.getText1Tag() != null) {
            tag = (Long)h.getText1Tag();
        }
        final long id = (tag == null ? 0 : (Long)tag);
        itemClicked(id);
//        animateClick(v.findViewById(R.id.container01));
    }

    private void animateClick(final View view) {
        if (view == null)
            return;
        BasicViewHolder holder = (BasicViewHolder)view.getTag();
        if (holder == null)
            return;
        Long tag = null;
        if (holder.getTextView1().getTag() != null) {
            Object o = holder.getTextView1().getTag();
            if (o != null)
                tag = (Long)holder.getTextView1().getTag();
        }
        final long id = (tag == null ? 0 : tag);
        view.setActivated(true);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setActivated(false);
                itemClicked(id);
            }
        }, 100);
    }

    private void itemClicked(long id) {
        if (callbacks == null)
            return;
        callbacks.onDashboardItemSelected(id == 0 ? DbHelper.getBaseUri(primaryUri) : ContentUris.withAppendedId(DbHelper.getBaseUri(primaryUri), id));
    }

    @Override
    public void onRowsCounted(final int dbType, final int count) {
        totalRows = count;
        if (fromSaved)  //isFromSavedState())
            getLoaderManager().restartLoader(1, getArguments(), this);
        else
            getLoaderManager().initLoader(1, getArguments(), this);
    }
}
