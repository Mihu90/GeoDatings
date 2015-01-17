/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.fragments;

import ro.mihaisurdeanu.geodatings.adapters.AdapterMenu;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ro.mihaisurdeanu.geodatings.R;

public class FragmentNavigationDrawer extends Fragment {
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	private NavigationDrawerCallbacks callbacks;

	private ActionBarDrawerToggle drawerToggle;

	private DrawerLayout drawerLayout;
	private ListView drawerListView;
	private View fragmentContainerView;

	private boolean fromSavedInstanceState;
	private boolean userLearnedDrawer;

	private AdapterMenu adapterMenu;
	
	public static String[] listMenu;
	public static int[] imageMenu= new int[]{
		R.drawable.ic_latest, R.drawable.ic_collection, 
		R.drawable.ic_settings, R.drawable.ic_about
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		adapterMenu = new AdapterMenu(getActivity());
		
		drawerListView = (ListView) inflater.inflate(
				R.layout.fragment_navigation_drawer, container, false);
		drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectItem(position);
			}
		});
		
		new MenuListTask().execute();
			
		return drawerListView;
	}

	public boolean isDrawerOpen() {
		return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
	}

	/**
	 * Aceasta metoda este folosita pentru a initializa meniul principal.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		fragmentContainerView = getActivity().findViewById(fragmentId);
		this.drawerLayout = drawerLayout;

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(getActivity(),
				drawerLayout, R.drawable.ic_drawer,
				R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) return;

				getActivity().supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!userLearnedDrawer) {
					userLearnedDrawer = true;
					SharedPreferences sharedPref = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sharedPref.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true)
							.commit();
				}

				getActivity().supportInvalidateOptionsMenu();
			}
		};

		if (!userLearnedDrawer && !fromSavedInstanceState) {
			drawerLayout.openDrawer(fragmentContainerView);
		}

		drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				drawerToggle.syncState();
			}
		});

		drawerLayout.setDrawerListener(drawerToggle);
	}

	/**
	 * Metoda apelata cand un element din meniu este selectat.
	 */
	private void selectItem(int position) {
		if (drawerListView != null) {
			drawerListView.setItemChecked(position, true);
		}
		if (drawerLayout != null) {
			drawerLayout.closeDrawer(fragmentContainerView);
		}
		if (callbacks != null) {
			callbacks.onNavigationDrawerItemSelected(position);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			callbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			// TODO: Jurnalizeaza exceptia
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		callbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private ActionBar getActionBar() {
		return ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

	public static interface NavigationDrawerCallbacks {
		/**
		 * Metoda apelata cand un element din meniul principal este selectat.
		 * 
		 * @param position
		 * 			Pozitia din lista a elementului ce a fost selectat.
		 */
		void onNavigationDrawerItemSelected(int position);
	}
	
	/**
	 * Task asincron prin care sunt preluate si construite optiunile din meniul
	 * principal atasat acestei aplicatii.
	 */
	private class MenuListTask extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			Resources res = getResources();
			listMenu = res.getStringArray(R.array.menu_list);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			drawerListView.setAdapter(adapterMenu);
		}
	}
}
