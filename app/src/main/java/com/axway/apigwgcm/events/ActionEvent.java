package com.axway.apigwgcm.events;

import android.content.Intent;

/**
 * Created by su on 4/27/2016.
 */
public class ActionEvent {

    public int id;
    public Intent intent;

    protected ActionEvent() {
        id = 0;
        intent = null;
    }

    public ActionEvent(int id) {
        this();
        this.id = id;
    }

    public ActionEvent(int id, Intent intent) {
        this(id);
        this.intent = intent;
    }

    public ActionEvent(Intent intent) {
        this();
        this.intent = intent;
    }
}
