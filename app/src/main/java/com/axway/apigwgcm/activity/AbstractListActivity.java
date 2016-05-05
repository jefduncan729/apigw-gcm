package com.axway.apigwgcm.activity;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by su on 5/4/2016.
 */
abstract public class AbstractListActivity extends BaseListActivity {

    @Override
    protected int updateItem(Bundle data) {
        //override this to do something useful
        return 0;
    }

    @Override
    protected Uri insertItem(Bundle data) {
        //override this to do something useful
        return null;
    }

    @Override
    protected ListFragment createListFrag(Intent intent) {
        //override this to do something useful
        return null;
    }

    @Override
    protected Fragment createEditFrag(Intent intent) {
        //override this to do something useful
        return null;
    }

    @Override
    protected Fragment createDetailFrag(Intent intent) {
        //override this to do something useful
        return null;
    }
}
