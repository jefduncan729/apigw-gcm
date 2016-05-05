package com.axway.apigwgcm.view;

import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.View;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.activity.BaseActivity;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.util.DateUtil;

/**
 * Created by su on 12/8/2014.
 */
public class CursorViewBinder implements ViewBinder<Cursor> {

    private int iconId;

    public CursorViewBinder() {
        super();
        iconId = 0;
    }

    @Override
    public void bindListView(final View view, final Cursor cursor) {
        BasicViewHolder holder = (BasicViewHolder)view.getTag();
        if (holder == null) {
            holder = new BasicViewHolder(view);
            view.setTag(holder);
        }
        if (cursor != null) {
            holder.setText1(buildSummary(cursor));
            holder.getTextView1().setTag(cursor.getLong(DbHelper.CommonColumns.NDX_ID));
//            holder.getTextView2().setTag(holder.getAuxView(R.id.chron01));
            holder.setText2(buildDetails(cursor));
            if (getIconId() != 0) {
                holder.showImageView(true);
                holder.setImageResource(getIconId());
            }
            else
                holder.showImageView(false);
        }
    }

    @Override
    public void bindDetailView(final View view, final Cursor cursor) {
        if (cursor == null)
            return;
        if (cursor.isBeforeFirst())
            cursor.moveToFirst();
        bindListView(view, cursor);
    }

    protected String buildSummary(final Cursor data) {
        StringBuilder sb = new StringBuilder();
        if (data != null)
            sb.append(data.getString(DbHelper.MsgColumns.NDX_SUBJECT));
        return sb.toString();
    }

    protected String buildDetails(Cursor cursor) {
/*
        Chronometer c = (Chronometer)holder.getAuxView(R.id.chron01);
        if (c != null) {
            c.setBase(cursor.getLong(DbHelper.CommonColumns.NDX_MODIFY_DATE));
            c.setVisibility(View.VISIBLE);
            c.start();
        }
*/
        StringBuilder sb = new StringBuilder();
        sb.append(cursor.getString(DbHelper.MsgColumns.NDX_MESSAGE));
        sb.append("\nReceived: ").append(DateUtil.relativeTime(cursor.getLong(DbHelper.CommonColumns.NDX_CREATE_DATE)));
//        sb.append("\n").append(DateUtil.formatDatetime(cursor.getLong(DbHelper.CommonColumns.NDX_CREATE_DATE)));
//        sb.append("\nModified: ").append(DateUtil.asPastTime(cursor.getLong(DbHelper.CommonColumns.NDX_MODIFY_DATE)));
        return sb.toString();
    }

    @Override
    public int getIconId() {
        return iconId;
    }

    @Override
    public void setIconId(int id) {
        iconId = id;
    }
}
