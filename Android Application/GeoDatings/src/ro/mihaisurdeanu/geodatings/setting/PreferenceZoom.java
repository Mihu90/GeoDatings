/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import ro.mihaisurdeanu.geodatings.R;

public final class PreferenceZoom extends DialogPreference implements OnSeekBarChangeListener {
    // Spatiile de nume de unde sunt citite atributele
    private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/ro.mihaisurdeanu.geodatings.setting";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Numele atributelor ce vor fi citite
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "minValue";
    private static final String ATTR_MAX_VALUE = "maxValue";

    // Valorile implicite pentru atribute
    private static final int DEFAULT_CURRENT_VALUE = 10;
    private static final int DEFAULT_MIN_VALUE = 1;
    private static final int DEFAULT_MAX_VALUE = 20;

    // Valorile reale
    private final int defaultValue;
    private final int maxValue;
    private final int minValue;
    
    // Valoarea curenta
    private int currentValue;
    
    // Elemente ce fac parte din View
    private SeekBar seekBar;
    private TextView valueText;

    public PreferenceZoom(Context context, AttributeSet attrs) {
    	super(context, attrs);

    	// Citeste parametrii folosind atributele ca punct de reper
    	minValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
    	maxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);
    	defaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
    }

    @SuppressLint("InflateParams")
	@Override
    protected View onCreateDialogView() {
    	// Obtine valoarea curenta
    	currentValue = getPersistedInt(defaultValue);

    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.setting_preference_zoom, null);

    	// Seteaza valorile minime si maxime pe label-uri
    	((TextView) view.findViewById(R.id.min_value)).setText(Integer.toString(minValue));
    	((TextView) view.findViewById(R.id.max_value)).setText(Integer.toString(maxValue));

    	// Seteaza bara de progres
    	seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
    	seekBar.setMax(maxValue - minValue);
    	seekBar.setProgress(currentValue - minValue);
    	seekBar.setOnSeekBarChangeListener(this);

    	// Seteaza textul pentru labelul curent
    	valueText = (TextView) view.findViewById(R.id.current_value);
    	valueText.setText(Integer.toString(currentValue));

    	return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
    	super.onDialogClosed(positiveResult);

		// Schimbarea a fost realizata?
    	if (!positiveResult) {
    		return;
    	}
	
		if (shouldPersist()) {
			persistInt(currentValue);
		}

		// Notifica activitatea principala asupra schimbarilor efectuate
		notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
    	String summary = super.getSummary().toString();
    	int value = getPersistedInt(defaultValue);
    	return String.format(summary, value);
    }
    
    /**
     * Ce se intampla cand bara de progres este folosita pentru a seta o noua
     * valoare asupra zoom-ului hartii?
     */
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
    	currentValue = value + minValue;
    	valueText.setText(Integer.toString(currentValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
		// Eveniment neutilizat.
    }

    public void onStopTrackingTouch(SeekBar seek) {
    	// Eveniment neutilizat.
    }
}