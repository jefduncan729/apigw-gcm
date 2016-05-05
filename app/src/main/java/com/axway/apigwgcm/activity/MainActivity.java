package com.axway.apigwgcm.activity;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.GcmSvc;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.fragment.DashboardFragment;
import com.axway.apigwgcm.oauth.GetTokenTask;
import com.axway.apigwgcm.oauth.OAuthToken;
import com.axway.apigwgcm.triggers.EventTrigger;
import com.axway.apigwgcm.util.AccountUtil;
import com.axway.apigwgcm.util.GcmUtil;
import com.axway.apigwgcm.util.SafeAsyncTask;
import com.axway.apigwgcm.util.StringUtil;
import com.axway.apigwgcm.view.BasicViewHolder;
import com.axway.apigwgcm.view.SwipeRefreshHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements
        DashboardFragment.Callbacks,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, GetTokenTask.Callbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int[] NAV_ACTION_IDS = new int[] { R.id.action_show_alerts, R.id.action_show_commands, R.id.action_show_events, R.id.action_triggers, R.id.action_settings, R.id.action_manage_certs, R.id.action_registration };
    private static final int[] NAV_ICON_IDS = new int[] { R.mipmap.ic_nav_action_warning, R.mipmap.ic_nav_action_flash_on, R.mipmap.ic_nav_action_import_export, R.mipmap.ic_nav_action_attachment, R.mipmap.ic_action_settings_holo_light, R.mipmap.ic_action_secure_holo_light, R.mipmap.ic_action_group_holo_light };
    private static final int[] NAV_LABEL_IDS = new int[] { R.string.action_show_alerts, R.string.action_show_commands, R.string.action_show_events, R.string.action_triggers, R.string.action_settings, R.string.action_manage_certs, R.string.action_registration };

    private static final int[] FRAME_IDS = { R.id.container01, R.id.container02, R.id.container03 };

    private static final Uri[] DASHBOARD_URIS = {
            DbHelper.getRecentsUri(DbHelper.ALERTS),
            DbHelper.getRecentsUri(DbHelper.EVENTS),
            DbHelper.getRecentsUri(DbHelper.COMMANDS)};

    private static final int MSG_ACCOUNT_CREATED = 101;
    private static final int MSG_REFRESH = 102;
    private static final int MSG_OP_CANCELED = 103;
    private static final int MSG_OP_FAILED = 104;

//    private static final int MSG_TOKEN_INFO = 106;
//    private static final int MSG_TOKEN_REVOKED = 107;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView navList;
    private SwipeRefreshHelper swipeHelper;

    private BroadcastReceiver broadcastReceiver;
    private DbHelper dbHelper;
    private AccountUtil acctUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acctUtil = AccountUtil.getInstance(this);
        if (acctUtil.getSingleAccount() == null) {
            Log.d(TAG, "no account found, calling createAccount");
            createAccount();
            return;
        }
        if (getPrefs().getInt(Constants.KEY_NUM_RECENTS, 0) == 0)
            setDefaultPrefs();
        if (GcmUtil.needGcmRegistration(getPrefs())) {
            Log.d(TAG, "starting registration activity");
            Intent i = new Intent(this, RegistrationActivity.class);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        showProgressBar(true);
        swipeHelper = new SwipeRefreshHelper(this, this);

//        fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        for (int i = 0; i < FRAME_IDS.length; i++){
            View v = findViewById(FRAME_IDS[i]);
            if (v != null) {
//                v.setAnimation(fadeIn);
                v.setVisibility(View.GONE);
            }
        }
        navList = (ListView)findViewById(R.id.nav_drawer);
        navList.setAdapter(new NavListAdapter());
        navList.setOnItemClickListener(this);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                getDrawerToggle().syncState();
            }
        });
        drawerLayout.setDrawerListener(getDrawerToggle());
        showDashboard();
    }

    @Override
    protected void setupToolbar() {
        toolbar.setTitle(R.string.dashboard);
        toolbar.setNavigationIcon(R.mipmap.ic_ab_drawer_holo_light);
    }

    private ActionBarDrawerToggle getDrawerToggle() {
        if (drawerToggle == null) {
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, false, R.mipmap.ic_nav_menu, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                    swipeHelper.enableRefreshing(false, false);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    invalidateOptionsMenu();
                    swipeHelper.enableRefreshing(true);
                }
            };
        }
        return drawerToggle;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null)
            drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setDefaultPrefs() {
        getPrefs().edit()
                .putBoolean(Constants.KEY_GCM_ALERTS, true)
                .putBoolean(Constants.KEY_GCM_COMMANDS, true)
                .putBoolean(Constants.KEY_GCM_EVENTS, true)
                .putInt(Constants.KEY_NUM_RECENTS, Constants.DEF_NUM_RECENTS)
                .apply();
    }

    @Override
    protected boolean onHandleMessage(Message msg) {
        if (msg.what == MSG_ACCOUNT_CREATED) {
            accountCreated(msg.getData());
            return true;
        }
        if (msg.what == MSG_REFRESH) {
            swipeHelper.enableRefreshing(false);
            showDashboard();
            return true;
        }
        if (msg.what == MSG_OP_CANCELED) {
            showToast("operation cancelled");
            finish();
            return true;
        }
        if (msg.what == MSG_OP_FAILED) {
            showToast("operation failed");
            finish();
            return true;
        }
/*
        if (msg.what == MSG_TOKEN_REVOKED) {
            onTokenRevoked(msg.getData().getBoolean(Intent.EXTRA_LOCAL_ONLY, true));
            return true;
        }
        if (msg.what == MSG_TOKEN_INFO) {
            onTokenInfo(msg.getData().getString(Intent.EXTRA_LOCAL_ONLY, null));
            return true;
        }
*/
        return super.onHandleMessage(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == R.id.action_settings) {
            if (resultCode == RESULT_OK) {
                showDashboard();
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void accountCreated(Bundle b) {
        String name = (b == null ? null : b.getString(AccountManager.KEY_ACCOUNT_NAME));
        if (name == null)
            showToast("account creation failed");
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void testBrowserIntent() {
        String url = "https://admin:changeme@192.168.1.101:8090";
        Uri uri = Uri.parse(url);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }

    private void createAccount() {
        getMsgHandler();
        final AccountManagerFuture<Bundle> future = acctUtil.getAccountManager().addAccount(Constants.ACCOUNT_TYPE, Constants.AUTH_TOKEN_TYPE, null, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = null;
                try {
                    Bundle bnd = future.getResult();
                    msg = getMsgHandler().obtainMessage(MSG_ACCOUNT_CREATED);
                    msg.setData(bnd);
                }
                catch (OperationCanceledException e) {
                    msg = getMsgHandler().obtainMessage(MSG_OP_CANCELED);  //showToast("operation canceled");
                }
                catch (Exception e) {
                    msg = getMsgHandler().obtainMessage(MSG_OP_FAILED);  //showToast("operation canceled");
                    Log.e(TAG, "exception", e);
                }
                if (msg != null)
                    getMsgHandler().sendMessage(msg);
            }
        }).start();
    }

    private void startMessagesActivity(int actionId) {
        int dbType = dbTypeFromActionId(actionId);
        final Uri uri = DbHelper.getBaseUri(dbType);
        if (uri == null)
            return;
        Log.d(TAG, "starting MessagesActivity");
        Intent i = new Intent(this, MessagesActivity.class);
        i.setData(uri);
//        i.setDataAndType(uri, DbHelper.getMimeType(uri));
        i.setAction(Intent.ACTION_VIEW);
        startActivity(i);
    }

    private void showDashboard() {
        showProgressBar(false);
        swipeHelper.showRefreshing(true);
        invalidateOptionsMenu();
        loadFragments();
    }

     private void loadFragments() {
         for (int i = 0; i < DASHBOARD_URIS.length; i++) {
             showFrame(FRAME_IDS[i], false);
             if (useSection(DbHelper.matchUri(DASHBOARD_URIS[i]))) {
                 DashboardFragment frag = DashboardFragment.newInstance(DASHBOARD_URIS[i]);
                 getFragmentManager().beginTransaction().replace(FRAME_IDS[i], frag).commit();
                 showFrame(FRAME_IDS[i], true);
             }
         }
         swipeHelper.showRefreshing(false);
     }

    private void startCertMgrActivity() {
        final Intent i = new Intent(this, ManageCertsActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isDrawerOpen()) {
//        if (mNavFragment != null && !mNavFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isDrawerOpen()) {
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (getDrawerToggle().onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRegistrationActivity() {
        Log.d(TAG, "starting RegistrationActivity");
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivityForResult(i, R.id.action_gcm_register);
    }

    @Override
    public void onDashboardItemSelected(final Uri uri) {
        Intent i = null;
        int t = DbHelper.matchUri(uri);
        if (t == DbHelper.ALERT_ID || t == DbHelper.COMMAND_ID || t == DbHelper.EVENT_ID) {
            Log.d(TAG, StringUtil.format("start MessageDetailActivity: %s", uri));
            i = new Intent(this, MessageDetailActivity.class);
            i.setData(uri);
        }
        else {
            Log.d(TAG, StringUtil.format("start MessagesActivity: %s", uri));
            i = new Intent(this, MessagesActivity.class);
            i.setData(DbHelper.getBaseUri(uri));
        }
        i.setAction(Intent.ACTION_VIEW);
        startActivity(i);
    }

    private void onBroadcastReceived(final Intent intent) {
        String action = intent.getAction();
        if (GcmSvc.BROADCAST_REFRESH.equals(action)) {
//            showDashboard();
            updateFragment(intent.getData());
        }
    }

    private BroadcastReceiver getBroadcastReceiver() {
        if (broadcastReceiver == null) {
            Log.d(TAG, "instantiating broadcast receiver");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onBroadcastReceived(intent);
                }
            };
        }
        return broadcastReceiver;
    }

    private void registerBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addCategory(GcmSvc.BROADCAST_CATEGORY);
        for (int i = 0; i < GcmSvc.BROADCAST_ACTIONS.length; i++) {
            filter.addAction(GcmSvc.BROADCAST_ACTIONS[i]);
        }
        for (int i = 0; i < DbHelper.DASHBOARD_MIMETYPES.length; i++) {
            try {
                filter.addDataType(DbHelper.DASHBOARD_MIMETYPES[i]);
            }
            catch (IntentFilter.MalformedMimeTypeException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
        Log.d(TAG, "registering broadcast receiver");
        registerReceiver(getBroadcastReceiver(), filter);
    }

    private void unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            Log.d(TAG, "unregistering broadcast receiver");
            unregisterReceiver(broadcastReceiver);
        }
        broadcastReceiver = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectNavItem(position);
    }

    private void selectNavItem(int position) {
        NavListAdapter.Entry e = (NavListAdapter.Entry)navList.getItemAtPosition(position);
        if (e == null)
            return;
        boolean chkItem = true;
        switch (e.actionId) {
            case R.id.action_show_alerts:
            case R.id.action_show_commands:
            case R.id.action_show_events:
                startMessagesActivity(e.actionId);
            break;
            case R.id.action_select_acct:
                startRegistrationActivity();
                chkItem = false;
            break;
            case R.id.action_triggers:
                startTriggersActivity();
            break;
            case R.id.action_settings:
                showSettings(null);
            break;
            case R.id.action_manage_certs:
                startCertMgrActivity();
            break;
            case R.id.action_registration:
                startRegistrationActivity();
            break;
            default:
                chkItem = false;
        }
        if (chkItem)
            navList.setItemChecked(position, true);
        closeDrawer();
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(navList);
    }

    public void closeDrawer() {
        if (isDrawerOpen()) {
            drawerLayout.closeDrawer(navList);
            getDrawerToggle().syncState();
        }
    }

    private void startTriggersActivity() {
        Intent i = new Intent();
        i.setDataAndType(DbHelper.TriggerColumns.CONTENT_URI, DbHelper.TriggerColumns.CONTENT_TYPE);
        i.setAction(Intent.ACTION_VIEW);
        try {
            startActivity(i);
        }
        catch (ActivityNotFoundException e) {
            Log.d(TAG, StringUtil.format("no activity to handle intent: %s", i));
        }
    }

    @Override
    public void onRefresh() {
        if (isDrawerOpen())
            swipeHelper.showRefreshing(false);
        else
            getMsgHandler().sendEmptyMessage(MSG_REFRESH);
    }

    private void updateFragment(final Uri uri) {
        int m = DbHelper.matchUri(uri);
        if (useSection(m)) {
            Uri ruri = DbHelper.getRecentsUri(m);
            int ndx = -1;
            for (int i = 0; i < DASHBOARD_URIS.length; i++) {
                if (DASHBOARD_URIS[i].equals(ruri)) {
                    ndx = i;
                    break;
                }
            }
            if (ndx == -1)
                return;
            Log.d(TAG, StringUtil.format("refresh broadcast received: %d, %s", ndx, ruri));
            showFrame(FRAME_IDS[ndx], false);
            DashboardFragment frag = DashboardFragment.newInstance(ruri);
            getFragmentManager().beginTransaction().replace(FRAME_IDS[ndx], frag).commit();
            showFrame(FRAME_IDS[ndx], true);
        }
    }

    private void showFrame(int id, boolean show) {
        View v = findViewById(id);
        if (v != null) {
            v.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private int getFrameId(final int dbType) {
        int ctr = -1;
        switch (dbType) {
            case DbHelper.ALERTS:
            case DbHelper.RECENT_ALERTS:
            case DbHelper.ALERT_ID:
                ctr = 0;
                break;
            case DbHelper.COMMANDS:
            case DbHelper.COMMAND_ID:
            case DbHelper.RECENT_COMMANDS:
                ctr = 1;
                break;
            case DbHelper.EVENTS:
            case DbHelper.EVENT_ID:
            case DbHelper.RECENT_EVENTS:
                ctr = 2;
                break;
        }
        if (ctr >= 0 && ctr < FRAME_IDS.length)
            return FRAME_IDS[ctr];
        return 0;
    }

    private boolean useSection(int id) {
        int action = 0;
        switch (id) {
            case DbHelper.RECENT_ALERTS:
            case DbHelper.ALERTS:
                action = R.id.action_show_alerts;
                break;
            case DbHelper.RECENT_COMMANDS:
            case DbHelper.COMMANDS:
                action = R.id.action_show_commands;
                break;
            case DbHelper.RECENT_EVENTS:
            case DbHelper.EVENTS:
                action = R.id.action_show_events;
                break;
        }
        return showEntry(action);
    }

    private boolean showEntry(int actionId) {
        String key = null;
        switch (actionId) {
            case R.id.action_triggers:
            case R.id.action_settings:
            case R.id.action_manage_certs:
            case R.id.action_registration:
                return true;
            case R.id.action_show_alerts:
                key = Constants.KEY_GCM_ALERTS;
                break;
            case R.id.action_show_commands:
                key = Constants.KEY_GCM_COMMANDS;
                break;
            case R.id.action_show_events:
                key = Constants.KEY_GCM_EVENTS;
                break;
        }
        if (key == null)
            return false;
        return getPrefs().getBoolean(key, false);
    }

    private int dbTypeFromActionId(final int actionId) {
        switch (actionId) {
            case R.id.action_show_alerts:
                return DbHelper.ALERTS;
            case R.id.action_show_commands:
                return DbHelper.COMMANDS;
            case R.id.action_show_events:
                return DbHelper.EVENTS;
        }
        return DbHelper.NO_MATCH;
    }

    @Override
    public void onSuccess(OAuthToken token) {
        showProgressBar(false);
        Log.d(TAG, "OAuthToken obtained");
    }

    @Override
    public void onCanceled() {
        showProgressBar(false);
        Log.d(TAG, "getToken task canceled");
    }

    private class NavListAdapter extends BaseAdapter {

        class Entry {
            public static final int KIND_ACCT = 1;
            public static final int KIND_NAV = 2;
            public static final int KIND_SEPARATOR = 3;

            int kind;
            int actionId;
            int iconId;
            String title;

            public Entry(int kind, int actionId, int iconId, String title) {
                super();
                this.kind = kind;
                this.actionId = actionId;
                this.iconId = iconId;
                this.title = title;
            }
        }

        private List<Entry> entries;

        public NavListAdapter() {
            super();
            createEntries();
        }

        private void createEntries() {
            entries = new ArrayList<Entry>();
            entries.add(new Entry(Entry.KIND_ACCT, R.id.action_select_acct, R.mipmap.ic_action_person, acctUtil.getSingleAccount().name));
            String lbl;
            for (int i = 0; i < NAV_ACTION_IDS.length; i++) {
                if (showEntry(NAV_ACTION_IDS[i])) {
                    lbl = getString(NAV_LABEL_IDS[i]);
                    entries.add(new Entry(Entry.KIND_NAV, NAV_ACTION_IDS[i], NAV_ICON_IDS[i], lbl));
                    if (NAV_ACTION_IDS[i] == R.id.action_triggers)
                        entries.add(new Entry(Entry.KIND_SEPARATOR, 0, 0, null));
                }
            }
        }

        @Override
        public int getCount() {
            if (entries == null)
                return 0;
            return entries.size();
        }

        @Override
        public Object getItem(int position) {
            if (entries == null || (position < 0 || position >= entries.size()))
                return null;
            return entries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private int getLayoutId(int kind) {
            if (kind == Entry.KIND_ACCT)
                return R.layout.acct;
            if (kind == Entry.KIND_SEPARATOR)
                return R.layout.section_divider;
            return R.layout.nav_drawer_item;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Entry e = (Entry)getItem(position);
            if (e == null)
                return convertView;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, getLayoutId(e.kind), null);
            }
            if (e.kind != Entry.KIND_SEPARATOR) {
                BasicViewHolder holder = (BasicViewHolder)convertView.getTag();
                if (holder == null || (e.kind != holder.getViewType())) {
                    holder = new BasicViewHolder(convertView);
                    convertView.setTag(holder);
                }
                updateView(holder, e);
            }
            return convertView;
        }

        private void updateView(final BasicViewHolder holder, final Entry e) {
            holder.setViewType(e.kind);
            holder.setText1(e.title);
            boolean showImg = false;
            switch (e.kind) {
                case Entry.KIND_NAV:
                    holder.getTextView1().setCompoundDrawablesWithIntrinsicBounds(e.iconId, 0, 0, 0);
                break;
                case Entry.KIND_ACCT:
                    holder.setImageResource(e.iconId);
                    showImg = true;
                break;
            }
            if (holder.getImageView() != null)
                holder.getImageView().setVisibility(showImg ? View.GONE : View.VISIBLE);
        }
    }

    private void startValidateDbTask() {
        dbHelper = new DbHelper(this);
        ValidateDbTask task = new ValidateDbTask(this);
        task.execute();
    }

    private class ValidateDbTask extends SafeAsyncTask<Void, Void, Void> {

        private ValidateDbTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected Void run(Void... params) {
            if (dbHelper == null)
                return null;
            dbHelper.ensureTablesExist();
            return null;
        }

        @Override
        protected void doPostExecute(Void aVoid) {
            showToast("database validated");
            dbHelper = null;
        }
    }

/*
    private void startPopulateDbTask() {
        PopulateDbTask task = new PopulateDbTask(this);
        task.execute();
    }

    private class PopulateDbTask extends SafeAsyncTask<Void, Void, Void> {

        private PopulateDbTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected Void run(Void... params) {
            ContentValues values = new ContentValues();
            values.put(DbHelper.TriggerColumns.TYPE, EventTrigger.REQUEST_TRIGGER);
            values.put(DbHelper.TriggerColumns.NAME, "Has X-GCM header");
            values.put(DbHelper.TriggerColumns.EXPRESSION, "header.X-GCM exists");
            getContentResolver().insert(DbHelper.TriggerColumns.CONTENT_URI, values);
            values.clear();
            values.put(DbHelper.TriggerColumns.TYPE, EventTrigger.REQUEST_TRIGGER);
            values.put(DbHelper.TriggerColumns.NAME, "X-GCM header is true");
            values.put(DbHelper.TriggerColumns.EXPRESSION, "header.X-GCM equals 'true'");
            getContentResolver().insert(DbHelper.TriggerColumns.CONTENT_URI, values);
            values.clear();
            values.put(DbHelper.TriggerColumns.TYPE, EventTrigger.REQUEST_TRIGGER);
            values.put(DbHelper.TriggerColumns.NAME, "Is Json");
            values.put(DbHelper.TriggerColumns.EXPRESSION, "header.Content-Type contains 'json'");
            getContentResolver().insert(DbHelper.TriggerColumns.CONTENT_URI, values);
            return null;
        }

        @Override
        protected void doPostExecute(Void aVoid) {
            showToast("database populated");
            dbHelper = null;
        }
    }

    protected void confirmRevokeToken() {
        String s = acctUtil.peekAuthToken();
        if (TextUtils.isEmpty(s))
            return;
        final OAuthToken t = OAuthToken.from(s);
        if (t == null)
            return;
        String msg = getString(R.string.confirm_msg, "revoke access token for " + acctUtil.getSingleAccount().name);
        confirmDialog(msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                revokeToken(t);
            }
        });
    }

    private void revokeToken(final OAuthToken t) {
        showProgressBar(true);
        client.revokeAccessToken(acctUtil.getSingleAccount().name, t, new RevokeCallback(this));
    }

    private void onTokenRevoked(boolean revoked) {
        showProgressBar(false);
        Log.d(TAG, "onTokenRevoked: " + Boolean.toString(revoked));
        acctUtil.clearPassword();
        showToast(R.string.auth_revoked);
        finish();
    }

    protected void confirmInvalidateToken(final boolean forceLogin) {
        Account acct = acctUtil.getSingleAccount();
        if (acct == null)
            return;
        String msg = getString(R.string.confirm_msg, "invalidate access token for " + acct.name);
        confirmDialog(getString(R.string.confirm), msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                invalidateAuthToken(forceLogin);
            }
        }, Constants.NOOP_LISTENER);
    }

    private void invalidateAuthToken(final boolean forceLogin) {
        String t = acctUtil.peekAuthToken();
        OAuthToken oauthToken = OAuthToken.from(t);
        if (oauthToken != null) {
            acctUtil.invalidateToken(oauthToken.toString());
        }
        if (forceLogin) {
            Log.d(TAG, "clearing account password");
            acctUtil.clearPassword();
        }
        showToast("Access token invalidated");
    }

    protected void getTokenInfo() {
        String s = acctUtil.peekAuthToken();
        final OAuthToken t = OAuthToken.from(s);
        final String user = acctUtil.getSingleAccount().name;
        if (t == null || t.isExpired()) {
            notifyInvalidToken(t);
            return;
        }
        client.getTokenInfo(user, t, new InfoCallback(this));
    }

    private void notifyInvalidToken(final OAuthToken token) {
        Log.d(TAG, "invalid token");
        String msg = getString(R.string.confirm_msg, "refresh access token");
        String title = (token == null ? "Invalid Token" : "Access Token Expired");
        alertDialog(title, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (token == null) {
                    Log.d(TAG, "refreshing token");
                    refreshToken();
                } else {
                    Log.d(TAG, "invalidating token");
                    invalidateAuthToken(false);
                    refreshToken();
                }
            }
        }, Constants.NOOP_LISTENER);
    }

    private void onTokenInfo(String s) {
        showProgressBar(false);
        JsonObject json = JsonUtil.getInstance().parseAsJsonObject(s);
        if (json == null) {
            showToast("invalid response from server");
            return;
        }
        String u = json.get("user_id").getAsString();
        int e = json.get("expires_in").getAsInt();
        String units = "minute";
        if (e < DateUtil.SECONDS_PER_MINUTE) {
            units = "second";
        }
        else {
            e = (e / DateUtil.SECONDS_PER_MINUTE);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Token for ").append(u).append(" expires in about ");
        sb.append(e).append(" ").append(units).append((e == 1 ? "" : "s"));
        showToast(sb.toString());
    }

    private void refreshToken() {
        if (tokenTask != null)
            return;
        tokenTask = new GetTokenTask(this, this);
        tokenTask.execute();
    }

    private class InfoCallback extends ActivityCallback {

        public InfoCallback(Activity activity) {
            super(activity);
        }

        @Override
        protected void onSuccessResponse(int code, String msg, final String body) {
            onTokenInfo(body);
        }
    }

    private class RevokeCallback extends ActivityCallback {

        public RevokeCallback(Activity activity) {
            super(activity);
        }

        @Override
        protected void onSuccessResponse(int code, String msg, String body) {
            onTokenRevoked(true);
        }
    }
*/
}

