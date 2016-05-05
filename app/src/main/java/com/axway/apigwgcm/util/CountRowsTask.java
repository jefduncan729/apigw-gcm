package com.axway.apigwgcm.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.axway.apigwgcm.db.DbHelper;

/**
* Created by su on 12/11/2014.
*/
public class CountRowsTask extends SafeAsyncTask<Uri, Void, Integer> {

    public interface Callbacks {
        public void onRowsCounted(final int dbType, final int count);
    }

    private int dbType;
    private Callbacks callbacks;

    public CountRowsTask(Context ctx) {
        super(ctx);
        if (ctx instanceof Callbacks)
            this.callbacks = (Callbacks)ctx;
    }

    @Override
    protected Integer run(Uri... params) {
        final Uri uri = params[0];  //DbHelper.getCountUri(params[0]);
        if (uri == null || getContext() == null)
            return 0;
        dbType = DbHelper.matchUri(uri);
        final Uri countUri = DbHelper.getCountUri(dbType);
        if (countUri == null)
            return 0;
        final Cursor c = getContext().getContentResolver().query(countUri, null, null, null, null, null);
        int rv = 0;
        if (c != null) {
            if (c.moveToFirst())
                rv = c.getInt(0);
            c.close();
        }
        return rv;
    }

    @Override
    protected void doPreExecute() {
        super.doPreExecute();
    }

    @Override
    protected void doPostExecute(Integer integer) {
        if (callbacks != null)
            callbacks.onRowsCounted(dbType, integer);
    }

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }
}
