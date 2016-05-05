package com.axway.apigwgcm.fragment;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.axway.apigwgcm.R;
import com.axway.apigwgcm.view.BasicViewHolder;
import com.axway.apigwgcm.view.CursorViewBinder;

/**
 * Created by su on 12/5/2014.
 */
public class DefaultCursorAdapter extends CursorAdapter {

    protected LayoutInflater inflater;
//    private int staticImageId;
    private CursorViewBinder viewBinder;

    public DefaultCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
//        staticImageId = 0;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rv = inflater.inflate(R.layout.listitem_2, null);
        BasicViewHolder holder = new BasicViewHolder(rv);
        rv.setTag(holder);
        return rv;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (getViewBinder() != null)
            getViewBinder().bindListView(view, cursor);
/*
        BasicViewHolder holder = (BasicViewHolder)view.getTag();
        if (holder == null) {
            holder = new BasicViewHolder(view);
            view.setTag(holder);
        }
        holder.setText1(cursor.getString(DbHelper.CommonColumns.NDX_SUBJECT));
        holder.setText2(buildDetails(cursor));
        if (getStaticImageId() != 0)
            holder.setImageResource(getStaticImageId());
*/
    }

    protected CursorViewBinder getViewBinder() {
        if (viewBinder == null) {

        }
        return viewBinder;
    }

    public void setViewBinder(CursorViewBinder newVal) {
        viewBinder = newVal;
    }
/*
    public int getStaticImageId() {
        return staticImageId;
    }

    public void setStaticImageId(int staticImageId) {
        this.staticImageId = staticImageId;
    }
*/
}
