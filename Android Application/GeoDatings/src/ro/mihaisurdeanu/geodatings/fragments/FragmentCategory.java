/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ro.mihaisurdeanu.geodatings.adapters.AdapterCategory;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ro.mihaisurdeanu.geodatings.R;

import static ro.mihaisurdeanu.geodatings.libraries.UserFunctions.*;

public class FragmentCategory extends Fragment implements 
		OnClickListener, OnItemClickListener {
	OnCategoryListSelectedListener callback;

	private ArrayList<HashMap<String, String>> items;

	private AdapterCategory la;
	private JSONObject json;

	private TextView textNoResult, textAlert;
	private Button buttonRetry;
	private LinearLayout layoutRetry;
	private ListView list;

	public static String[] categories;
	public int mCurrentPosition;

	private int lengthData;

	private String[] categoryId;
	private String[] categoryName;
	private String[] categoryIcon;

	public interface OnCategoryListSelectedListener {

		public void onCategoryListSelected(String categoryId, String categoryName);
	}

	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Utility.context = getActivity();
		items = new ArrayList<HashMap<String, String>>();

		View view = inflater.inflate(R.layout.fragment_category, null);

		list = (ListView) view.findViewById(R.id.list);
		textNoResult = (TextView) view.findViewById(R.id.lblNoResult);
		textAlert = (TextView) view.findViewById(R.id.lblAlert);
		layoutRetry = (LinearLayout) view.findViewById(R.id.lytRetry);
		buttonRetry = (Button) view.findViewById(R.id.btnRetry);
		// Seteaza un ascultator pe butonul de Retry.
		buttonRetry.setOnClickListener(this);

		// Verifica conexiunea la internet si daca exista una activa se va
		// incerca obtinerea de date de la server.
		if (Utility.hasInternetConnection()) {
			new CategoryListTask().execute();
		} else {
			textNoResult.setVisibility(View.GONE);
			layoutRetry.setVisibility(View.VISIBLE);
			textAlert.setText(R.string.no_connection);
		}

		// Seteaza un ascultator atasat listei de elemente
		list.setOnItemClickListener(this);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			callback = (OnCategoryListSelectedListener) activity;
		} catch (ClassCastException e) {
			// TODO: Jurnalizeaza exceptia!
		}
	}

	/**
	 * Task prin care se obtin informatiile despre categoriile existente,
	 * interogand serverul principal.
	 */
	private class CategoryListTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			// O bara de progress va aparea pe ecranul utilizatorului
			dialog = new ProgressDialog(getActivity());
			dialog.setMessage("Please wait..");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				json = getCategories();
				if (json != null) {
					JSONArray eventsArray = json.getJSONArray(array_category_list);
					lengthData = eventsArray.length();
					categoryId = new String[lengthData];
					categoryName = new String[lengthData];
					categoryIcon = new String[lengthData];

					for (int i = 0; i < lengthData; i++) {
						JSONObject event = eventsArray.getJSONObject(i);
						HashMap<String, String> map = new HashMap<String, String>();

						categoryId[i] = event.getString(KEY_CATEGORY_ID);
						categoryName[i] = event.getString(KEY_CATEGORY_NAME);
						categoryIcon[i] = event.getString(KEY_CATEGORY_ICON);

						map.put(KEY_CATEGORY_ID, categoryId[i]);
						map.put(KEY_CATEGORY_NAME, categoryName[i]);
						map.put(KEY_CATEGORY_ICON, categoryIcon[i]);

						items.add(map);
					}
				}
			} catch (JSONException e) {
				// TODO: Jurnalizeaza exceptia!
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (isAdded()) {
				if (items.size() != 0) {
					layoutRetry.setVisibility(View.GONE);
					list.setVisibility(View.VISIBLE);
					textNoResult.setVisibility(View.GONE);
					la = new AdapterCategory(getActivity(), items);
					list.setAdapter(la);
				} else {
					if (json != null) {
						textNoResult.setVisibility(View.VISIBLE);
						layoutRetry.setVisibility(View.GONE);
					} else {
						textNoResult.setVisibility(View.GONE);
						layoutRetry.setVisibility(View.VISIBLE);

						Toast.makeText(getActivity(), R.string.no_connection,
								Toast.LENGTH_SHORT).show();
					}
				}
			}

			// Bara de progress va disparea
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btnRetry) {
			if (Utility.hasInternetConnection()) {
				json = null;
				new CategoryListTask().execute();
			} else {
				textNoResult.setVisibility(View.GONE);
				layoutRetry.setVisibility(View.VISIBLE);
				textAlert.setText(R.string.no_connection);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		HashMap<String, String> item = new HashMap<String, String>();
		item = items.get(position);

		// Cand un element din lista e selectat, activitatea principala
		// este notificata.
		callback.onCategoryListSelected(
				item.get(KEY_CATEGORY_ID),
				item.get(KEY_CATEGORY_NAME));

		// Elementul selectat trebuie sa iasa putin in evidenta
		list.setItemChecked(position, true);
	}
}
