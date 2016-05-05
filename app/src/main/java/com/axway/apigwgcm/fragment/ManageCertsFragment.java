package com.axway.apigwgcm.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.axway.apigwgcm.R;
import com.axway.apigwgcm.TrustedCertLoader;
import com.axway.apigwgcm.view.BasicViewHolder;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Created by su on 11/19/2014.
 */
public class ManageCertsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Certificate>> {

    private static final String TAG = ManageCertsFragment.class.getSimpleName();

    public static ManageCertsFragment newInstance() {
        ManageCertsFragment rv = new ManageCertsFragment();
        return rv;
    }

    public interface Callbacks {
        public void onRemoveKeystore();
        public void onRowsCounted(int n);
    }

    private Callbacks callbacks = null;

    public void refresh() {
        Log.d(TAG, "restarting loader");
        getLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            Log.d(TAG, "initializing loader");
            getLoaderManager().initLoader(1, getArguments(), this);
        }
        else {
            Log.d(TAG, "restarting loader");
            getLoaderManager().restartLoader(1, savedInstanceState, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.certs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_remove_keystore) {
            Log.d(TAG, "remove keystore selected");
            if (callbacks != null)
                callbacks.onRemoveKeystore();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: " + activity.getTitle());
        if (activity instanceof Callbacks) {
            Log.d(TAG, "assigning callbacks");
            callbacks = (Callbacks) activity;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        setEmptyText("No trusted certificates");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(android.R.layout.list_content, null);
    }

    @Override
    public Loader<List<Certificate>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        return new TrustedCertLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Certificate>> loader, List<Certificate> data) {
        if (callbacks != null)
            callbacks.onRowsCounted((data == null ? 0 : data.size()));
        setListAdapter(new CertAdapter(data));
    }

    @Override
    public void onLoaderReset(Loader<List<Certificate>> loader) {
        Log.d(TAG, "onLoaderReset");
        setListAdapter(null);
    }

    private class CertAdapter extends BaseAdapter {

        private List<Certificate> list;

        public CertAdapter(List<Certificate> list) {
            super();
            this.list = list;
        }

        @Override
        public int getCount() {
            if (list == null)
                return 0;
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            if (list == null || (position < 0 || position >= list.size()))
                return null;
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Certificate c = (Certificate)getItem(position);
            if (c == null) {
                Log.d(TAG, "cert is null at position " + Integer.toString(position));
                return convertView;
            }
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.listitem_2, null);
            }
            BasicViewHolder holder = (BasicViewHolder)convertView.getTag();
            if (holder == null) {
                holder = new BasicViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder.getImageView().setVisibility(View.GONE);
            if (c instanceof X509Certificate) {
                X509Certificate c509 = (X509Certificate) c;
                Log.d(TAG, "X509 cert at position " + Integer.toString(position) + ": " + c509.getSubjectDN().getName());
                holder.setText1(buildTitle(c509));
                holder.setText2(buildDetails(c509));
            }
            else {
                Log.d(TAG, "cert is not an X509 cert at position " + Integer.toString(position));
            }
            return convertView;
        }

        private String buildTitle(X509Certificate c) {
            StringBuilder sb = new StringBuilder();
            sb.append(c.getSubjectDN().getName());
            return sb.toString();
        }

        private String buildDetails(X509Certificate c) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expires: ").append(c.getNotAfter());
            return sb.toString();
        }
    }
}
