package com.axway.apigwgcm.util;

import android.content.ContentValues;
import android.os.Bundle;

/**
 * Created by su on 12/23/2014.
 */
public interface EditCallbacks {

    public void onSaveItem(final Bundle data);
    public void onEditCanceled();
    public void onValidationError(String msg);
}
