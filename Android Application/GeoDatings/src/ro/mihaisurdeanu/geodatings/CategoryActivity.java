/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.fragments.FragmentCategory;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import ro.mihaisurdeanu.geodatings.R;

public class CategoryActivity extends AbstractActivity implements
		FragmentCategory.OnCategoryListSelectedListener {

	public CategoryActivity() {
		super(R.layout.activity_list);
	}

	/**
	 * Metoda este apelata imediat dupa ce activitatea a fost creata.
	 */
	@Override
	public void afterOnCreate(Bundle savedInstanceState) {
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);

		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.frame_content, new FragmentCategory());
		t.commit();
	}

	/**
	 * Functie executata in urma evenimentului de selectie a unei categorii.
	 */
	@Override
	public void onCategoryListSelected(String categoryId, String categoryName) {
		// Apeleaza activitatea CategoryListActivity cu parametrii asociati.
		Intent i = new Intent(this, CategoryListActivity.class);
		i.putExtra(Utility.EXTRA_CATEGORY_ID, categoryId);
		i.putExtra(Utility.EXTRA_CATEGORY_NAME, categoryName);
		// Afiseaza activitatea
		startActivity(i);
		
		// Translatia dintre cele doua activitati se va face cu o animatie.
		overridePendingTransition(R.anim.open_next, R.anim.close_main);
	}
}
