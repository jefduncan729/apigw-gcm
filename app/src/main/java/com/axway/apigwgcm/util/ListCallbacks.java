package com.axway.apigwgcm.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by su on 12/23/2014.
 */
public interface ListCallbacks {
    public void onItemSelected(final Intent intent);
    public void onSetRowCount(final int count);
    public void onDelete(final Intent intent);
    public void onAddItem(final Intent intent);
    public void onEditItem(final Intent intent);
    public void onRequestSync(final Intent intent);
}
