/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.dialogs;

import ro.mihaisurdeanu.geodatings.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.ContextThemeWrapper;

/**
 * Clasa prin care se creaza o fereastra de dialog cu doua optiuni posibile.
 * Va fi folosita in special pentru confirmarea unor actiuni.
 */
public class TwoOptionsDialog extends AlertDialog.Builder {
	// Optiunile care vor fi afisate utilizatorilor
	private Object positive, negative;

	public TwoOptionsDialog(Context context) {
		this(context, R.style.DarkTwoOptionsStyle);
	}
	
	public TwoOptionsDialog(Context context, int styleResource) {
		super(new ContextThemeWrapper(context, styleResource));
	}
	
	/**
	 * Sunt setate optiunile posibile pentru fereastra de dialog ce urmeaza
	 * sa fie creata si afisata utilizatorului.
	 * 
	 * @param positive
	 * 			Textul ce va aparea pe butonul pozitiv.
	 * @param negative
	 * 			Textul ce va aparea pe butonul negativ.
	 */
	public void setOptionNames(Object positive, Object negative) {
		this.positive = positive;
		this.negative = negative;
	}
	
	/**
	 * Afiseaza casuta de dialog pe care am dorit-o.
	 * 
	 * @param listener
	 * 			Se poate inregistra si un ascultator pentru a prinde toate
	 * 			evenimentele pe care utilizatorul le poate face.
	 */
	public void display(OnClickListener listener) {
		// Butonul pozitiv este setat.
		if (positive instanceof String) {
			setPositiveButton((String)positive, listener);
		} else if (positive instanceof Integer) {
			setPositiveButton((Integer)positive, listener);
		}
		
		// Urmeaza butonul negativ.
		if (negative instanceof String) {
			setNegativeButton((String)negative, listener);
		} else if (negative instanceof Integer) {
			setNegativeButton((Integer)negative, listener);
		}
		
		// In cele din urma se afiseaza ferestra de dialog.
		show();
	}
	
	/**
	 * Varianta a metodei {@link #display(OnClickListener)} ce nu are niciun
	 * ascultator inregistrat.
	 */
	public void display() {
		display(null);
	}
}
