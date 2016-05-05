package com.axway.apigwgcm.view;

import android.view.View;

/**
 * Created by su on 12/9/2014.
 */
public interface ViewBinder<T> {

    public int getIconId();
    public void setIconId(int id);

    public void bindListView(View view, T item);
    public void bindDetailView(View view, T item);
}
