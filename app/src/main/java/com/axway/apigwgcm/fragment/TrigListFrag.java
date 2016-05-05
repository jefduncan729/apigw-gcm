package com.axway.apigwgcm.fragment;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.axway.apigwgcm.R;
import com.axway.apigwgcm.util.StringUtil;
import com.axway.apigwgcm.view.ViewBinder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * Created by su on 4/21/2016.
 */
public class TrigListFrag extends ListFragment implements AdapterView.OnItemClickListener {

    public static final String TAG = TrigListFrag.class.getSimpleName();
    private JsonArray data;
    private LayoutInflater inflater;

    public static TrigListFrag newInstance(JsonArray data) {
        TrigListFrag rv = new TrigListFrag();
        rv.data = data;
        return rv;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(android.R.layout.list_content, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, StringUtil.format("onItemClick: %d", i));
    }

    public void refresh() {
        if (data == null)
            setListAdapter(null);
        else
            setListAdapter(new TrigAdapter());
    }

    private class TrigBinder implements ViewBinder<JsonObject> {

        private TextView txt01;
        private TextView txt02;

        public TrigBinder(View v) {
            txt01 = null;
            txt02 = null;
            if (v != null) {
                txt01 = (TextView)v.findViewById(android.R.id.text1);
                txt02 = (TextView)v.findViewById(android.R.id.text2);
            }
        }

        @Override
        public int getIconId() {
            return 0;
        }

        @Override
        public void setIconId(int id) {

        }

        @Override
        public void bindListView(View view, JsonObject item) {
            if (item == null)
                return;
            if (txt01 != null && item.has("name")) {
                txt01.setText(item.get("name").getAsString());
            }
            if (txt02 != null && item.has("expression")) {
                txt02.setText(item.get("expression").getAsString());
            }
        }

        @Override
        public void bindDetailView(View view, JsonObject item) {
            bindListView(view, item);
        }
    }

    private class TrigAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (data == null)
                return 0;
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            if (data == null)
                return null;
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(android.R.layout.simple_list_item_activated_2, null);
                view.setTag(new TrigBinder(view));
            }
            JsonObject obj = (data == null ? null : data.get(i).getAsJsonObject());
            TrigBinder binder = (TrigBinder)view.getTag();
            binder.bindListView(view, obj);
            return view;
        }
    }
}
