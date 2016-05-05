package com.axway.apigwgcm.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by su on 11/18/2014.
 */
abstract public class SafeAsyncTask<TIn, TProg, TOut> extends AsyncTask<TIn, TProg, TOut> {

    private WeakReference<Context> ctxRef;

    protected SafeAsyncTask(Context ctx) {
        super();
        ctxRef = new WeakReference<Context>(ctx);
    }

    @Override
    protected final TOut doInBackground(TIn... params) {
        if (getContext() == null)
            return null;
        return run(params);
    }

    @Override
    protected final void onPreExecute() {
        if (getContext() == null)
            return;
        doPreExecute();
    }

    @Override
    protected final void onPostExecute(TOut tOut) {
        if (getContext() == null)
            return;
        doPostExecute(tOut);
        ctxRef = null;
    }

    @Override
    protected final void onProgressUpdate(TProg... values) {
        if (getContext() == null)
            return;
        doProgressUpdate(values);
    }

    @Override
    protected final void onCancelled() {
        if (getContext() == null)
            return;
        doOnCancelled(null);
        ctxRef = null;
    }

    @Override
    protected final void onCancelled(TOut tOut) {
        if (getContext() == null)
            return;
        doOnCancelled(tOut);
        ctxRef = null;
    }

    protected final Context getContext() {
        if (ctxRef == null)
            return null;
        return ctxRef.get();
    }

    abstract protected TOut run(TIn... params);

    protected void doPreExecute() {}

    protected void doPostExecute(TOut tOut) {}

    protected void doProgressUpdate(TProg... values) {}

    protected void doOnCancelled(TOut tOut) {}

    protected void snooze(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Log.d("SafeAsyncTask", "interrupted while snoozing");
        }
    }
}
