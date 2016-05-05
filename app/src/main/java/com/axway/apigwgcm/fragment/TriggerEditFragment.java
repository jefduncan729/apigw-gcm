package com.axway.apigwgcm.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.axway.apigwgcm.R;
import com.axway.apigwgcm.db.DbHelper;
import com.axway.apigwgcm.triggers.EventTrigger;
import com.axway.apigwgcm.triggers.Operation;
import com.axway.apigwgcm.util.EditCallbacks;
import com.axway.apigwgcm.util.HttpUtil;
import com.axway.apigwgcm.util.RequiredFieldException;
import com.axway.apigwgcm.util.ValidationException;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by su on 12/19/2014.
 */
public class TriggerEditFragment extends CursorFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, TextWatcher {

    private static final String TAG = TriggerEditFragment.class.getSimpleName();

    private Spinner spTrigType;
    private Spinner spExpType;
    private Spinner spHdrNames;
    private EditText edOperand;
    private EditText edCustHdr;
    private CheckBox edEnabled;
    private TextView lblOperand;
//    private TextView txtExpr;
    private EditText edExpr;
    private EditText edName;

    private Button btnAdd;
    private Button btnOpenP;
    private Button btnCloseP;
    private Button btnAnd;
    private Button btnOr;
    private Button btnNot;

    private View ctrCustHdr;
    private View ctrHdrVal;
    private View ctrExprTools;

    private ArrayList<String> trigTypes;
    private ArrayList<String> expTypes;

    private ArrayList<String> httpMethods;

    private int curTrigPos;
    private int curExpPos;
    private int curHdrPos;

    private EditCallbacks callbacks;

    public TriggerEditFragment() {
        super();
    }

    public static TriggerEditFragment newInstance(final Uri uri) {
        TriggerEditFragment rv = new TriggerEditFragment();
        rv.setPrimaryUri(uri);
        return rv;
    }
/*

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (primaryUri == null) {
            //must be an insert, just show the UI
        }
        else {
            getLoaderManager().initLoader(PRIMARY_LOADER, getArguments(), this);
        }
    }
*/

    @Override
    protected void updateView(Cursor data) {
//        super.updateView(data);
        if (data == null)
            return;
        if (data.isBeforeFirst())
            data.moveToFirst();
//        txtExpr.setText(data.getString(DbHelper.TriggerColumns.NDX_EXPR));
        edName.setText(data.getString(DbHelper.TriggerColumns.NDX_NAME));
        edExpr.setText(data.getString(DbHelper.TriggerColumns.NDX_EXPR));
        edEnabled.setChecked(data.getInt(DbHelper.TriggerColumns.NDX_STATUS) == DbHelper.STATUS_ENABLED);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.mod_trigger, null);
        spTrigType = (Spinner)rv.findViewById(R.id.spinner01);
        spExpType = (Spinner)rv.findViewById(R.id.spinner02);
        spHdrNames = (Spinner)rv.findViewById(R.id.spinner03);
        //txtExpr = (TextView)rv.findViewById(R.id.txt_cur_expr);
        edExpr = (EditText)rv.findViewById(R.id.txt_cur_expr);
        edName = (EditText)rv.findViewById(R.id.edit_name);
        edEnabled = (CheckBox)rv.findViewById(R.id.edit_enabled);
        edEnabled.setOnClickListener(this);
        edOperand = (EditText)rv.findViewById(R.id.edit_operand);
        lblOperand = (TextView)rv.findViewById(R.id.lbl_operand);
        edCustHdr = (EditText)rv.findViewById(R.id.edit_hdr_name);
        edOperand.addTextChangedListener(this);
        edCustHdr.addTextChangedListener(this);
        spTrigType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getTriggerTypes()));
        spExpType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getExpressionTypes()));
        spHdrNames.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, HttpUtil.getHeaderNames()));
        btnAdd = (Button)rv.findViewById(R.id.action_add);
        btnOpenP = (Button)rv.findViewById(R.id.action_open_paren);
        btnCloseP = (Button)rv.findViewById(R.id.action_close_paren);
        btnAnd = (Button)rv.findViewById(R.id.action_insert_and);
        btnOr = (Button)rv.findViewById(R.id.action_insert_or);
        btnNot = (Button)rv.findViewById(R.id.action_insert_not);
        btnAdd.setOnClickListener(this);
        btnOpenP.setOnClickListener(this);
        btnCloseP.setOnClickListener(this);
        btnAnd.setOnClickListener(this);
        btnOr.setOnClickListener(this);
        btnNot.setOnClickListener(this);
        ctrCustHdr = (View)rv.findViewById(R.id.ctr_hdr_name);
        ctrHdrVal = (View)rv.findViewById(R.id.ctr_operand);
        ctrExprTools = (View)rv.findViewById(R.id.ctr_expr_tools);
        return rv;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTrigPos(0);
        setExpPos(0);
        setHdrPos(1);
        spTrigType.setOnItemSelectedListener(this);
        spExpType.setOnItemSelectedListener(this);
        spHdrNames.setOnItemSelectedListener(this);
        updateButtonState();
        dirty = false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof EditCallbacks)
            callbacks = (EditCallbacks)activity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (callbacks != null) {
            if (item.getItemId() == R.id.action_save) {
                try {
                    validate();
                    callbacks.onSaveItem(collectResults());
                    return true;
                }
                catch (ValidationException e) {
                    callbacks.onValidationError(e.getMessage());
                }
            }
            if (item.getItemId() == R.id.action_cancel) {
                callbacks.onEditCanceled();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);

    }

    private Bundle collectResults() {
        Bundle rv = new Bundle();
        rv.putString(DbHelper.TriggerColumns.EXPRESSION, edExpr.getText().toString());
        rv.putString(DbHelper.TriggerColumns.NAME, edName.getText().toString());    //StringUtil.format("trigger_%d", System.currentTimeMillis()));
        rv.putInt(DbHelper.TriggerColumns.TYPE, curTrigPos + EventTrigger.REQUEST_TRIGGER);
        rv.putInt(DbHelper.TriggerColumns.STATUS, edEnabled.isChecked() ? DbHelper.STATUS_ENABLED : DbHelper.STATUS_DISABLED);
        rv.putParcelable(Intent.EXTRA_UID, primaryUri);
        return rv;
    }

    private void validate() throws ValidationException {
        String s = edName.getText().toString();
        if (TextUtils.isEmpty(s))
            throw new RequiredFieldException("name");
        s = edExpr.getText().toString();
        if (TextUtils.isEmpty(s) || "empty".equals(s))
            throw new RequiredFieldException("expression");
    }

    private String buildExpression() {
        String operator = null;
        switch (curExpPos) {
            case 0:
                operator = Operation.OP_EXISTS;
                break;
            case 1:
                operator = Operation.OP_EQUALS;
                break;
            case 2:
                operator = Operation.OP_CONTAINS;
                break;
            case 3:
                operator = Operation.OP_STARTS_WITH;
                break;
            case 4:
                operator = Operation.OP_ENDS_WITH;
                break;
        }
        if (operator == null)
            return null;
        StringBuilder sb = new StringBuilder();
        String hdrNm = (String)spHdrNames.getItemAtPosition(curHdrPos);
        if (HttpUtil.CUSTOM_HEADER.equals(hdrNm))
            hdrNm = edCustHdr.getText().toString();
        sb.append("header.").append(hdrNm).append(" ").append(operator);
        if (!Operation.OP_EXISTS.equals(operator))
            sb.append(" \"").append(edOperand.getText().toString()).append("\"");
        return sb.toString();
//        addToExpression(sb.toString());
//        txtExpr.setText(sb.toString());
    }

    private void setTrigPos(final int p) {
        if (spTrigType== null || p == curTrigPos)
            return;
        dirty = true;
        curTrigPos = p;
        spTrigType.setSelection(curTrigPos);
    }

    private void setExpPos(final int p) {
        if (spExpType == null || p == curExpPos)
            return;
        dirty = true;
        curExpPos = p;
        spExpType.setSelection(curExpPos);
        ctrHdrVal.setVisibility(curExpPos == 0 ? View.GONE : View.VISIBLE);
//        setHdrPos(1);
    }

    private void setHdrPos(final int p) {
        if (spHdrNames == null || p == curHdrPos)
            return;
        dirty = true;
        curHdrPos = p;
        ctrCustHdr.setVisibility(curHdrPos == 0 ? View.VISIBLE : View.GONE);
        spHdrNames.setSelection(curHdrPos);
    }

    private String getHeaderName() {
        if (spHdrNames == null)
            return null;
        return (String)spHdrNames.getItemAtPosition(curHdrPos);
    }

    private String getExpresionType() {
        if (spExpType == null)
            return null;
        return (String)spExpType.getItemAtPosition(curExpPos);
    }

    private String getTriggerType() {
        if (spTrigType == null)
            return null;
        return (String)spTrigType.getItemAtPosition(curTrigPos);
    }

    private ArrayList<String> getTriggerTypes() {
        if (trigTypes == null) {
            trigTypes = new ArrayList<>();
            trigTypes.add("Request Trigger");
            trigTypes.add("Response Trigger");
        }
        return trigTypes;
    }

    private ArrayList<String> getExpressionTypes() {
        if (expTypes == null) {
            expTypes = new ArrayList<>();
            String base = "Header ";
            expTypes.add(base + Operation.OP_EXISTS);
            expTypes.add(base + Operation.OP_EQUALS);
            expTypes.add(base + Operation.OP_NOT_EQUALS);
            expTypes.add(base + Operation.OP_CONTAINS);
            expTypes.add(base + Operation.OP_STARTS_WITH);
            expTypes.add(base + Operation.OP_ENDS_WITH);
            expTypes.add(base + Operation.OP_LESS_THAN);
            expTypes.add(base + Operation.OP_LESS_THAN_OR_EQUAL);
            expTypes.add(base + Operation.OP_GREATER_THAN);
            expTypes.add(base + Operation.OP_GREATER_THAN_OR_EQUAL);

            expTypes.add("Method " + Operation.OP_EQUALS);
            expTypes.add("Method " + Operation.OP_NOT_EQUALS);

/*
            base = "Content-Length ";
            expTypes.add(base + Operation.OP_EQUALS);
            expTypes.add(base + Operation.OP_LESS_THAN);
            expTypes.add(base + Operation.OP_LESS_THAN_OR_EQUAL);
            expTypes.add(base + Operation.OP_GREATER_THAN);
            expTypes.add(base + Operation.OP_GREATER_THAN_OR_EQUAL);

            base = "Content-Type ";
            expTypes.add(base + Operation.OP_EXISTS);
            expTypes.add(base + Operation.OP_EQUALS);
            expTypes.add(base + Operation.OP_NOT_EQUALS);
*/
        }
        return expTypes;
    }
    public ArrayList<String> getHttpMethods() {
        if (httpMethods == null) {
            httpMethods = new ArrayList<>();
            httpMethods.add("GET");
            httpMethods.add("POST");
            httpMethods.add("PUT");
            httpMethods.add("DELETE");
            httpMethods.add("OPTIONS");
            httpMethods.add("HEAD");
        }
        return httpMethods;
    }


    private void updateButtonState() {
        String cur = edExpr.getText().toString();
        ctrExprTools.setVisibility(View.VISIBLE);  //("empty".equals(cur)) ? View.GONE :
        boolean needName = (HttpUtil.CUSTOM_HEADER.equals(getHeaderName()));
        String et = getExpresionType();
        boolean needVal = (et != null && !et.endsWith(Operation.OP_EXISTS));
        boolean enable = true;
        if (needName)
            enable = !TextUtils.isEmpty(edCustHdr.getText().toString());
        if (enable && needVal)
            enable = !TextUtils.isEmpty(edOperand.getText().toString());
        btnAdd.setEnabled(enable);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinid = parent.getId();
        switch (spinid) {
            case R.id.spinner01:
                setTrigPos(position);
                break;
            case R.id.spinner02:
                setExpPos(position);
                break;
            case R.id.spinner03:
                setHdrPos(position);
                break;
        }
        updateButtonState();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String add = null;
        switch (id) {
            case R.id.action_add:
                add = buildExpression();
            break;
            case R.id.action_open_paren:
                add = "(";
            break;
            case R.id.action_close_paren:
                add = ")";
            break;
            case R.id.action_insert_and:
                add = "AND";
            break;
            case R.id.action_insert_or:
                add = "OR";
            break;
            case R.id.action_insert_not:
                add = "NOT";
            break;
            case R.id.edit_enabled:
                dirty = true;
            break;
        }
        if (!TextUtils.isEmpty(add))
            addToExpression(add);
    }

    private void addToExpression(final String s) {
        if (TextUtils.isEmpty(s))
            return;
        String expr = edExpr.getText().toString();
        StringBuilder sb = new StringBuilder();
        if (!"empty".equals(expr)) {
            sb.append(expr);
        }
        if (sb.length() > 0)
            sb.append(" ");
        sb.append(s);
        edExpr.setText(sb.toString().trim());
        edOperand.setText("");
        setExpPos(0);
        setHdrPos(1);
        dirty = true;
        updateButtonState();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        dirty = true;
        updateButtonState();
    }

    @Override
    protected String[] getProjection() {
        return DbHelper.TriggerColumns.DEF_PROJECTION;
    }

    @Override
    protected String getSortOrder() {
        return DbHelper.TriggerColumns.DEF_SORT_ORDER;
    }

    @Override
    protected boolean hasOptsMenu() {
        return true;
    }
}
