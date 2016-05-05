package com.axway.apigwgcm.fragment;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.StringUtil;
import com.axway.apigwgcm.view.CursorViewBinder;

import java.util.Locale;

/**
 * Created by su on 12/5/2014.
 */
abstract public class CursorListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = CursorListFragment.class.getSimpleName();

    public static final int PRIMARY_LOADER = 1;

    private CursorAdapter mainAdapter;
    private CursorViewBinder viewBinder;
    private Uri primaryUri;

    public CursorListFragment() {
        super();
        mainAdapter = null;
        viewBinder = null;
        primaryUri = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (primaryUri == null) {
            Log.d(TAG, "primaryUri not set, getting from arguments");
            Bundle args = (savedInstanceState == null ? getArguments() : savedInstanceState);
            if (args != null)
                primaryUri = args.getParcelable(Intent.EXTRA_UID);
        }
        if (primaryUri == null)
            throw new IllegalStateException("must provide a primaryUri");
        Log.d(TAG, StringUtil.format("primaryUri: %s", primaryUri));
        setHasOptionsMenu(hasOptsMenu());
        getLoaderManager().initLoader(PRIMARY_LOADER, getArguments(), this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (primaryUri != null)
            outState.putParcelable(Intent.EXTRA_UID, primaryUri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected boolean hasOptsMenu() {
        return getMenuLayoutId() != 0;
    }

    protected String[] getProjection() {
        return null;
    }

    protected String getSortOrder() {
        return null;
    }

    protected String getSelection(Bundle args) {
        return null;
    }

    protected String[] getSelectionArgs(Bundle args) {
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == PRIMARY_LOADER)
            return new CursorLoader(getActivity(), getPrimaryUri(), getProjection(), getSelection(args), getSelectionArgs(args), getSortOrder());
        return doCreateLoader(id, args);
    }

    protected Loader<Cursor> doCreateLoader(final int id, final Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PRIMARY_LOADER) {
            final int cnt = (data == null ? 0 : data.getCount());
            onSetRowCount(cnt);
            if (mainAdapter == null) {
                mainAdapter = createAdapter();
                setListAdapter(mainAdapter);
            }
            mainAdapter.swapCursor(data);
            setListShown(true);
        }
        else
            doLoadFinished(loader, data);

    }

    protected void doLoadFinished(final Loader<Cursor> loader, final Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
/*
        if (loader.getId() == PRIMARY_LOADER) {
            if (mainAdapter != null)
                mainAdapter.swapCursor(null);
            mainAdapter = null;
        }
        else
            doLoaderReset(loader);
*/
    }

    protected void doLoaderReset(final Loader<Cursor> loader) {
    }

    abstract protected int getNameColumnIndex();

    @Override
    protected void onIntentCreated(final Intent i, final AdapterView.AdapterContextMenuInfo cmi) {
        if (Intent.ACTION_INSERT.equals(i.getAction())) {
            i.setDataAndType(getPrimaryUri(), DbHelper.getMimeType(getPrimaryUri(), true));
            return;
        }
        if (cmi == null)
            return;
        Cursor c = ((Cursor)getListView().getItemAtPosition(cmi.position));
        if (c == null)
            return;
        final Uri uri = ContentUris.withAppendedId(getPrimaryUri(), cmi.id);
        String name = c.getString(getNameColumnIndex());
        i.putExtra(Intent.EXTRA_TEXT, name);
        i.setDataAndType(uri, DbHelper.getMimeType(uri, true));
    }

//    @Override
    protected Intent createSelectIntent(ListView l, View v, int position, long id) {
        final Intent i = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(getPrimaryUri(), id));
        return i;
    }

    private CursorAdapter createAdapter() {
        DefaultCursorAdapter rv = new DefaultCursorAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        rv.setViewBinder(getViewBinder());
        return rv;
    }

    protected Uri getPrimaryUri() {
        return primaryUri;
    }

    protected void setPrimaryUri(Uri uri) {
        primaryUri = uri;
    }

    protected String getEmptyTextString() {
        return "No rows";
    }
    abstract protected CursorViewBinder createViewBinder();

    protected CursorViewBinder getViewBinder() {
        if (viewBinder == null)
            viewBinder = createViewBinder();
        return viewBinder;
    }

    public void refresh() {
        Log.d(TAG, "refresh");
        if (isVisible()) {
            setListShown(false);
            getLoaderManager().restartLoader(PRIMARY_LOADER, getArguments(), CursorListFragment.this);
        }
    }
}
