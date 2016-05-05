package com.axway.apigwgcm;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.security.cert.Certificate;
import java.util.List;

/**
 * Created by su on 11/19/2014.
 */
public class TrustedCertLoader extends AsyncTaskLoader<List<Certificate>> {

    private List<Certificate> data;
    private Context ctx;

    public TrustedCertLoader(Context ctx) {
        super(ctx);
        this.ctx = ctx;
        data = null;
    }

    @Override
    public List<Certificate> loadInBackground() {
        List<Certificate> rv = null;
        rv = BaseApp.keystoreManager().getTrustedCerts();
        return rv;
    }

    @Override
    protected void onStartLoading() {
        if (data == null)
            forceLoad();
        else
            deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    public void deliverResult(List<Certificate> data) {
        this.data = data;
        if (isStarted())
            super.deliverResult(data);
    }
}
