package com.axway.apigwgcm.fragment;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.axway.apigwgcm.Constants;
import com.axway.apigwgcm.R;
import com.axway.apigwgcm.util.StringUtil;

/**
 * Created by su on 12/2/2014.
 */
public class GcmRegFragment extends Fragment implements View.OnClickListener, TextWatcher {
    private static final String TAG = GcmRegFragment.class.getSimpleName();

    private static final int[] SWITCH_IDS = { R.id.action_show_alerts, R.id.action_show_commands, R.id.action_show_events };
    private static final int[] FRAME_IDS = { R.id.container01, R.id.container02, R.id.container03 };

    public interface Callbacks {
        public void register(final Bundle data);
        public void unregister();
    }

    private Callbacks callbacks;

    private TextView txt01;
    private Button btn01;
    private Button btn02;
    private CheckBox[] switches;
    private EditText edSvcsPort;
    private CheckBox edUseSsl;
    private ProgressBar progressBar;
    private ViewGroup[] frames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.gcm_reg, null);
        progressBar = (ProgressBar)rv.findViewById(android.R.id.progress);
        frames = new ViewGroup[FRAME_IDS.length];
        for (int i = 0; i < FRAME_IDS.length; i++) {
            frames[i] = (ViewGroup) rv.findViewById(FRAME_IDS[i]);
        }
        showProgress(true);
        switches = new CheckBox[SWITCH_IDS.length];
        for (int i = 0; i < SWITCH_IDS.length; i++) {
            switches[i] = (CheckBox)rv.findViewById(SWITCH_IDS[i]);
            switches[i].setOnClickListener(this);
        }
        edSvcsPort = (EditText)rv.findViewById(R.id.services_port);
        edUseSsl = (CheckBox)rv.findViewById(R.id.use_ssl);
        txt01 = (TextView)rv.findViewById(android.R.id.text1);
        btn01 = (Button)rv.findViewById(android.R.id.button1);
        btn01.setOnClickListener(this);
        btn02 = (Button)rv.findViewById(android.R.id.button2);
        btn02.setOnClickListener(this);
        return rv;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String regid = getArguments().getString(Constants.KEY_REGISTRATION_ID, null);
        String acct = getArguments().getString(AccountManager.KEY_ACCOUNT_NAME, "");
        String msg = "";
        if (TextUtils.isEmpty(regid)) {
            msg = "Okay, you've created an account. Now you need to register for GCM.";
            btn01.setText("Register");
            btn01.setTag(true);
            btn02.setVisibility(View.GONE);
        }
        else {
            msg = acct;
            btn01.setText("Modify");
            btn01.setTag(false);
            btn02.setVisibility(View.VISIBLE);
        }
        Bundle regSettings = getArguments().getBundle(Constants.KEY_GCM_PREFS);
        if (regSettings == null)
            regSettings = new Bundle();
        for (int i = 0; i < SWITCH_IDS.length; i++) {
            boolean checked = false;
            switch (SWITCH_IDS[i]) {
                case R.id.action_show_alerts:
                    checked = regSettings.getBoolean(Constants.KEY_GCM_ALERTS, false);
                    break;
                case R.id.action_show_commands:
                    checked = regSettings.getBoolean(Constants.KEY_GCM_COMMANDS, false);
                    break;
                case R.id.action_show_events:
                    checked = regSettings.getBoolean(Constants.KEY_GCM_EVENTS, false);
                    break;
                default:
                    checked = false;
            }
            switches[i].setChecked(checked);
        }
        edSvcsPort.setText(Integer.toString(regSettings.getInt(Constants.KEY_SERVICES_PORT, 7080)));
        edUseSsl.setChecked(regSettings.getBoolean(Constants.KEY_SERVICES_USE_SSL, false));
        txt01.setText(msg);
        updateButtonState();

        //set listeners after view is populated
        edSvcsPort.addTextChangedListener(this);
        for (int i = 0; i < SWITCH_IDS.length; i++) {
            switches[i].setOnClickListener(this);
        }
        showProgress(false);
        updateButtonState();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callbacks) {
            callbacks = (Callbacks)activity;
        }
    }

    private Bundle savePrefs() {
        Bundle rv = new Bundle();
        for (int i = 0; i < SWITCH_IDS.length; i++) {
            switch (SWITCH_IDS[i]) {
                case R.id.action_show_alerts:
                    rv.putBoolean(Constants.KEY_GCM_ALERTS, switches[i].isChecked());
                    break;
                case R.id.action_show_commands:
                    rv.putBoolean(Constants.KEY_GCM_COMMANDS, switches[i].isChecked());
                    break;
                case R.id.action_show_events:
                    rv.putBoolean(Constants.KEY_GCM_EVENTS, switches[i].isChecked());
                    break;
            }
        }
        String s = edSvcsPort.getText().toString();
        int p = StringUtil.strToIntDef(s, 0);
        if (p > 0)
            rv.putInt(Constants.KEY_SERVICES_PORT, p);
        rv.putBoolean(Constants.KEY_SERVICES_USE_SSL, edUseSsl.isChecked());
        return rv;
    }

    private void updateButtonState() {
        int p = StringUtil.strToIntDef(edSvcsPort.getText().toString(), 0);
        boolean validPort = (p > 1024 && p < 65535);
        boolean oneChecked = false;
        if (validPort) {
            for (int i = 0; !oneChecked && i < SWITCH_IDS.length; i++) {
                oneChecked = switches[i].isChecked();
            }
        }
        btn01.setEnabled(validPort && oneChecked);
        btn02.setEnabled(validPort);
    }

    private void showProgress(final boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        for (int i = 0; i < FRAME_IDS.length; i++) {
            frames[i].setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    public void tokenObtained() {
//        showProgress(false);
//        updateButtonState();
//    }

    @Override
    public void onClick(View v) {
        if (callbacks == null)
            return;
        switch (v.getId()) {
            case android.R.id.button1:
                final Bundle b = savePrefs();
                b.putBoolean(Constants.EXTRA_NEW_ACCT, (Boolean) btn01.getTag());
                callbacks.register(b);  //p, (Boolean) btn01.getTag());
                break;
            case android.R.id.button2:
                callbacks.unregister();
                break;
            case R.id.action_show_alerts:
            case R.id.action_show_commands:
            case R.id.action_show_events:
                updateButtonState();
            break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateButtonState();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
