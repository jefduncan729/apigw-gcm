package com.axway.apigwgcm.view;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by su on 11/3/2014.
 */
public class BasicViewHolder {

    private static final int DEF_TEXT1_ID = android.R.id.text1;
    private static final int DEF_TEXT2_ID = android.R.id.text2;
    private static final int DEF_IMAGE_ID = android.R.id.icon;

    private TextView txt01;
    private TextView txt02;
    private ImageView img01;
    private int viewType;
    private SparseArray<View> auxViews;

    public BasicViewHolder(View rv) {
        this(rv, DEF_TEXT1_ID, DEF_TEXT2_ID, DEF_IMAGE_ID);
    }

    public BasicViewHolder(View rv, int txt1Id) {
        this(rv, txt1Id, DEF_TEXT2_ID, DEF_IMAGE_ID);
    }

    public BasicViewHolder(View rv, int txt1Id, int txt2Id) {
        this(rv, txt1Id, txt2Id, DEF_IMAGE_ID);
    }

    public BasicViewHolder(View rv, int txt1Id, int txt2Id, int imgId) {
        super();
        viewType = 0;
        txt01 = (TextView) rv.findViewById(txt1Id);
        txt02 = (TextView) rv.findViewById(txt2Id);
        img01 = (ImageView) rv.findViewById(imgId);
        auxViews = null;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public void setText1(String txt) {
        if (txt01 != null)
            txt01.setText(txt);
    }

    public void setText2(String txt) {
        if (txt02 != null)
            txt02.setText(txt);
    }

    public TextView getTextView1() {
        return txt01;
    }

    public TextView getTextView2() {
        return txt02;
    }

    public ImageView getImageView() {
        return img01;
    }

    public void setImageDrawable(Drawable d) {
        if (img01 == null)
            return;
        img01.setImageDrawable(d);
    }

    public void setImageResource(int resId) {
        if(img01==null)
            return;
        img01.setImageResource(resId);
    }

    public void setImageBitmap(Bitmap b) {
        if (img01 == null)
            return;
        img01.setImageBitmap(b);
    }

    public void showImageView(boolean show) {
        if (img01 == null)
            return;
        img01.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void addAuxView(final View v) {
        if (v == null)
            return;
        if (auxViews == null)
            auxViews = new SparseArray<View>();
        int id = v.getId();
        if (auxViews.get(id) != null)
            auxViews.remove(id);
        auxViews.put(id, v);
    }

    public View getAuxView(final int id) {
        if (auxViews == null)
            return null;
        return auxViews.get(id);
    }

    public Object getText1Tag() {
        if (txt01 == null)
            return null;
        return txt01.getTag();
    }

    public Object getText2Tag() {
        if (txt02 == null)
            return null;
        return txt02.getTag();
    }

    public Object getImageTag() {
        if (img01 == null)
            return null;
        return img01.getTag();
    }
}
