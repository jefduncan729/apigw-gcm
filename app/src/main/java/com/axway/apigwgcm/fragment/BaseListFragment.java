package com.axway.apigwgcm.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.axway.apigwgcm.R;
import com.axway.apigwgcm.util.ListCallbacks;
import com.axway.apigwgcm.view.ViewBinder;

/**
 * Created by su on 12/23/2014.
 */
abstract public class BaseListFragment extends ListFragment {

    private static final String TAG = BaseListFragment.class.getSimpleName();

    private boolean fromSavedState;
    private ViewBinder viewBinder;
    private ListCallbacks callbacks;

    public BaseListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromSavedState = (savedInstanceState != null);
        setHasOptionsMenu(hasOptsMenu());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ListCallbacks)
            callbacks = (ListCallbacks)activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        setEmptyText(getEmptyTextString());
    }

    protected String getEmptyTextString() {
        return "Empty list";
    }

    public void refresh() {
    }

    protected int getMenuLayoutId() {
        return 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(getMenuLayoutId(), menu);
        MenuItem item = menu.findItem(R.id.action_add);
        if (item != null) {
            item.setVisible(false);
            if (canAdd()) {
                item.setVisible(true);
                final Intent i = new Intent();
                i.setAction(Intent.ACTION_INSERT);
                onIntentCreated(i, null);
                item.setIntent(i);
            }
        }
        item = menu.findItem(R.id.action_sync_now);
        if (item != null) {
            item.setVisible(false);
            if (canSync()) {
                item.setVisible(true);
                final Intent i = new Intent();
                i.setAction(ContentResolver.SYNC_EXTRAS_MANUAL);
                onIntentCreated(i, null);
                item.setIntent(i);
            }
        }
        item = menu.findItem(R.id.action_delete_all);
        if (item != null) {
            item.setVisible(false);
            if (canDeleteAll()) {
                item.setVisible(true);
                final Intent i = new Intent();
                i.setAction("delete_all");
                onIntentCreated(i, null);
                item.setIntent(i);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                if (callbacks != null)
                    callbacks.onAddItem(item.getIntent());
                return true;
            case R.id.action_sync_now:
                if (callbacks != null)
                    callbacks.onRequestSync(item.getIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo)menuInfo;
        int p = 0;
        String name = "";
        if (canDelete()) {
            final MenuItem item = menu.add(0, R.id.action_delete, p++, R.string.action_delete);
            final Intent i = new Intent();
            i.setAction(Intent.ACTION_DELETE);
            onIntentCreated(i, cmi);
            name = i.getStringExtra(Intent.EXTRA_TEXT);
            item.setIntent(i);
        }
        if (canEdit()) {
            final MenuItem item = menu.add(0, R.id.action_edit, p++, R.string.action_edit);
            final Intent i = new Intent();
            i.setAction(Intent.ACTION_EDIT);
            onIntentCreated(i, cmi);
            name = i.getStringExtra(Intent.EXTRA_TEXT);
            item.setIntent(i);
        }
        if (menu.size() > 0)
            menu.setHeaderTitle(name);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (callbacks != null)
            callbacks.onItemSelected(createSelectIntent(l, v, position, id));
    }

    protected void onIntentCreated(final Intent i, final AdapterView.AdapterContextMenuInfo cmi) {

    }

    protected void onSetRowCount(final int count) {
        if (callbacks != null)
            callbacks.onSetRowCount(count);
    }

    abstract protected Intent createSelectIntent(final ListView l, final View v, final int position, final long id);

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Intent i = item.getIntent();
        if (i != null) {
            if (Intent.ACTION_DELETE.equals(i.getAction()) && callbacks != null) {
                callbacks.onDelete(item.getIntent());
                return true;
            }
            if (Intent.ACTION_EDIT.equals(i.getAction()) && callbacks != null) {
                callbacks.onEditItem(item.getIntent());
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    protected boolean isFromSavedState() {
        return fromSavedState;
    }


    protected boolean canDelete() {
        return false;
    }
    protected boolean canDeleteAll() {
        return false;
    }
    protected boolean canAdd() {
        return false;
    }
    protected boolean canEdit() {
        return false;
    }
    protected boolean canSync() {
        return false;
    }

    protected boolean hasOptsMenu() {
        return getMenuLayoutId() != 0;
    }

    protected ViewBinder getViewBinder() {
        if (viewBinder == null) {
            viewBinder = createViewBinder();
        }
        return viewBinder;
    }
    abstract protected ViewBinder createViewBinder();
}
