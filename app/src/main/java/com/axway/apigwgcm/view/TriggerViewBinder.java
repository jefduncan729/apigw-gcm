package com.axway.apigwgcm.view;

import android.database.Cursor;
import android.graphics.Paint;
import android.view.View;

import com.axway.apigwgcm.db.DbHelper;

/**
 * Created by su on 12/8/2014.
 */
public class TriggerViewBinder extends CursorViewBinder {

    private int iconId;

    public TriggerViewBinder() {
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
            holder.getTextView1().setTag(cursor.getLong(DbHelper.TriggerColumns.NDX_ID));
//            holder.getTextView2().setTag(holder.getAuxView(R.id.chron01));
            int pf = holder.getTextView1().getPaintFlags();
            if (cursor.getInt(DbHelper.CommonColumns.NDX_STATUS) == DbHelper.STATUS_ENABLED)
                pf = pf & ~Paint.STRIKE_THRU_TEXT_FLAG;    //pf holder.getTextView1().setPaintFlags(holder.getTextView1().getPaintFlags()  & ~Paint.STRIKE_THRU_TEXT_FLAG);
            else
                pf = pf | Paint.STRIKE_THRU_TEXT_FLAG;  //holder.getTextView1().setPaintFlags(holder.getTextView1().getPaintFlags()  | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.getTextView1().setPaintFlags(pf);
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
            sb.append(data.getString(DbHelper.TriggerColumns.NDX_NAME));
        return sb.toString();
    }

    protected String buildDetails(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append(cursor.getString(DbHelper.TriggerColumns.NDX_EXPR)).append("\n");
        sb.append("id: ").append(cursor.getLong(DbHelper.TriggerColumns.NDX_ID))
                .append(", type: ").append(cursor.getInt(DbHelper.TriggerColumns.NDX_TYPE));
//                .append(", state: ");
        int flag = cursor.getInt(DbHelper.TriggerColumns.NDX_FLAG);
        switch (flag) {
            case DbHelper.FLAG_INSYNC:
//                sb.append("Synced");
            break;
            case DbHelper.FLAG_NEW:
                sb.append(" +");
                break;
            case DbHelper.FLAG_UPDATED:
                sb.append(" *");
                break;
        }
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
