/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public abstract class AbstractActivity extends ActionBarActivity {
	/** ID-ul resursei care va reprezenta layout-ul acestei activitati. */
	private int layoutResource;

	public AbstractActivity(int layoutResource) {
		this.layoutResource = layoutResource;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutResource);
		
		// Salveaza contextul curent al aplicatiei
		Utility.context = this;
		
		// Ofera posibilitatea extinderii cu usurinta a functionalitatii
		afterOnCreate(savedInstanceState);
	}
	
	/**
	 * Metoda este apelata imediat dupa apelarea crearea si setarea layout-ului
	 * activitatii curente.
	 */
	public abstract void afterOnCreate(Bundle savedInstanceState);
	
	/**
	 * Metoda de tip ascultator pentru optiunile din meniul principal.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.open_main, R.anim.close_next);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.open_main, R.anim.close_next);
	}
}
