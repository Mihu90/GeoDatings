package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.fragments.FragmentSearch;
import ro.mihaisurdeanu.geodatings.libraries.SuggestionProvider;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBar;
import ro.mihaisurdeanu.geodatings.R;

public class SearchActivity extends AbstractActivity implements
		FragmentSearch.OnSearchListSelectedListener {

	public SearchActivity() {
		super(R.layout.activity_list);
	}

	public void afterOnCreate(Bundle savedInstanceState) {
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		// Reseteaza titlul acestei activitati
		actionbar.setTitle(getString(R.string.page_search));

		// Verifica actiunea curenta si obtine cuvantul cautat de utilizator
		Intent intent = getIntent();
		String searchWord = null;
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			searchWord = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
			suggestions.saveRecentQuery(searchWord, null);
		}

		Bundle bundle = new Bundle();
		bundle.putString(Utility.EXTRA_KEYWORD, searchWord);

		FragmentSearch fragment = new FragmentSearch();
		fragment.setArguments(bundle);

		// Adauga fragmentul in layoutul special
		getSupportFragmentManager().beginTransaction()
				.add(R.id.frame_content, fragment).commit();
	}

	@Override
	public void onListSelected(String idSelected) {
	}
}
