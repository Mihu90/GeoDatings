/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.fragments.FragmentCategoryList;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import ro.mihaisurdeanu.geodatings.R;

public class CategoryListActivity extends AbstractActivity implements
		FragmentCategoryList.OnDataListSelectedListener {
	
	public CategoryListActivity() {
		super(R.layout.activity_list);
	}

	@Override
	public void afterOnCreate(Bundle savedInstanceState) {
		// Obtinem informatiile primite de la activitatea parinte.
		Intent intent = getIntent();
		String categoryId = intent.getStringExtra(Utility.EXTRA_CATEGORY_ID);
		String categoryName = intent.getStringExtra(Utility.EXTRA_CATEGORY_NAME);

		// Foloseste o instanta de tipul ActionBar pentru a schimba titlul
		// activitatii in functie de categoria aleasa (numele ei).
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(categoryName);

		// Incarca un alt fragment ce se va ocupa cu afisarea tuturor
		// evenimentelor ce fac parte din aceasta categorie.
		Bundle bundle = new Bundle();
		bundle.putString(Utility.EXTRA_CATEGORY_ID, categoryId);
		FragmentCategoryList fragCategoryList = new FragmentCategoryList();
		fragCategoryList.setArguments(bundle);

		// Adauga fragmentul in cadrul containerului din layout.
		getSupportFragmentManager().beginTransaction()
				.add(R.id.frame_content, fragCategoryList).commit();
	}

	@Override
	public void onListSelected(String idSelected) {		
	}
}
