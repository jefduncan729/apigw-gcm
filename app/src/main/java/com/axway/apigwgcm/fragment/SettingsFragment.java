package com.axway.apigwgcm.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.util.IntegerPreference;

/**
 * Created by su on 11/19/2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private IntegerPreference prefNumRecents;
    private SwitchPreference prefSyncWifiOnly;
/*
    private SwitchPreference prefCmds;
    private SwitchPreference prefEvents;
//    private SwitchPreference prefMsgs;
*/
    public static SettingsFragment newInstance() {
        SettingsFragment rv = new SettingsFragment();
        return rv;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        initUi();
    }

    private void initUi() {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefNumRecents = (IntegerPreference)findPreference(Constants.KEY_NUM_RECENTS);
        updateNumRecents(prefs);
        prefSyncWifiOnly = (SwitchPreference)findPreference(Constants.KEY_SYNC_WIFI_ONLY);
        updateWifiOnly(prefs);
/*
        prefCmds = (SwitchPreference)findPreference(Constants.KEY_GCM_COMMANDS);
        updateCmds(prefs);
        prefEvents = (SwitchPreference)findPreference(Constants.KEY_GCM_EVENTS);
        updateEvents(prefs);
//        prefMsgs = (SwitchPreference)findPreference(Constants.KEY_GCM_MESSAGES);
//        updateMsgs(prefs);
*/
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        getActivity().setResult(Activity.RESULT_OK);
        if (Constants.KEY_NUM_RECENTS.equals(key))
            updateNumRecents(sharedPreferences);
        else if (Constants.KEY_SYNC_WIFI_ONLY.equals(key))
            updateWifiOnly(sharedPreferences);
    }

    private void updateNumRecents(SharedPreferences prefs) {
        if (prefNumRecents == null)
            return;
        int val = prefs.getInt(Constants.KEY_NUM_RECENTS, Constants.DEF_NUM_RECENTS);
        String msg = null;
        if (val < Constants.MIN_NUM_RECENTS) {
            val = Constants.MIN_NUM_RECENTS;
            msg = "Minimum is " + Integer.toString(Constants.MIN_NUM_RECENTS);
        }
        if (val > Constants.MAX_NUM_RECENTS) {
            val = Constants.MAX_NUM_RECENTS;
            msg = "Maximum is " + Integer.toString(Constants.MAX_NUM_RECENTS);
        }
        if (msg != null) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            prefs.edit().putInt(Constants.KEY_NUM_RECENTS, val).commit();
        }
        StringBuilder sb = new StringBuilder("Dashboard sections will contain ");
        sb.append(val).append(" recent item").append((val == 1 ? "" : "s")).append(" (at most)");
        prefNumRecents.setSummary(sb.toString());
    }

    private void updateWifiOnly(SharedPreferences prefs) {
        if (prefs == null || prefSyncWifiOnly == null)
            return;
        boolean b = prefs.getBoolean(Constants.KEY_SYNC_WIFI_ONLY, false);
        StringBuilder sb = new StringBuilder("Sync will be performed ");
        if (b)
            sb.append("only when a Wi-Fi");
        else
            sb.append("when a Wi-Fi or mobile data");
        sb.append(" connection is available");
        prefSyncWifiOnly.setSummary(sb.toString());
    }

/*
    private void updateCmds(SharedPreferences prefs) {
        updateSwitch(prefs, Constants.KEY_GCM_COMMANDS, prefCmds);
    }

    private void updateEvents(SharedPreferences prefs) {
        updateSwitch(prefs, Constants.KEY_GCM_EVENTS, prefEvents);
    }

    private void updateMsgs(SharedPreferences prefs) {
        updateSwitch(prefs, Constants.KEY_GCM_MESSAGES, prefMsgs);
    }

    private void updateSwitch(SharedPreferences prefs, String key, SwitchPreference pref) {
        if (prefs == null || pref == null)
            return;
        if (prefs.contains(key)) {
            boolean b = prefs.getBoolean(key, false);
            int n = key.lastIndexOf(".");
            String name = StringUtils.capitalize(key.substring(n+1));
            pref.setSummary("Recent " + name + " will " + (b ? "" : "not ") + "be shown");
        }
    }
*/
}
