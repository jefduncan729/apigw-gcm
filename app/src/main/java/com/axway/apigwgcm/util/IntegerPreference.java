package com.axway.apigwgcm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class IntegerPreference extends DialogPreference {

	private EditText edit01;
	
	public IntegerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		edit01 = null;
	}
	
	protected int getDefault() {
		return 0;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		edit01 = (EditText)view.findViewById(android.R.id.text1);
		SharedPreferences p = getSharedPreferences();
		edit01.setText(Integer.toString(p.getInt(getKey(), getDefault())));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			edit01.clearFocus();
			int n = getDefault();
			try {
				n = Integer.parseInt(edit01.getText().toString());
			}
			catch (NumberFormatException e) {
				n = getDefault();
			}
			SharedPreferences.Editor e = getEditor();
			e.putInt(getKey(), n);
			e.commit();
		}
	}	
}
