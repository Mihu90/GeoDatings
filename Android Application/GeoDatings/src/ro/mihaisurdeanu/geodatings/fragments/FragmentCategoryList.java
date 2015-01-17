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

import ro.mihaisurdeanu.geodatings.adapters.AdapterHome;
import ro.mihaisurdeanu.geodatings.libraries.UserFunctions;
import ro.mihaisurdeanu.geodatings.loadmore.PagingListView;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
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
import android.widget.TextView;
import android.widget.Toast;
import ro.mihaisurdeanu.geodatings.R;

import static ro.mihaisurdeanu.geodatings.libraries.UserFunctions.*;

public class FragmentCategoryList extends Fragment implements OnClickListener {
	private OnDataListSelectedListener callback;
	
	private ArrayList<HashMap<String, String>> items;
	private ProgressDialog dialog;

	private AdapterHome la;
	private PagingListView list;

	private TextView textNoResult, textAlert;
	private Button buttonRetry; 
	private LinearLayout layoutRetry;
	
	private JSONObject json;
	private int currentPage = 0;
	private int previousPage;
    
    private String[] eventsId;
    private String[] eventsTitle;
    private String[] eventsEndDate;
    private String[] eventsIcon;
	
	private int lengthData;
	private String categoryId;
	
	private int loadMore = 0; // 1 vizibil, 0 invizibil
	
	public interface OnDataListSelectedListener{
		public void onListSelected(String idSelected);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		
		list = (PagingListView) view.findViewById(R.id.list);
		textNoResult = (TextView) view.findViewById(R.id.lblNoResult);
		textAlert = (TextView) view.findViewById(R.id.lblAlert);
		layoutRetry = (LinearLayout) view.findViewById(R.id.lytRetry);
		buttonRetry = (Button) view.findViewById(R.id.btnRetry);
		buttonRetry.setOnClickListener(this);
		
		Utility.context = getActivity();
		items = new ArrayList<HashMap<String, String>>();

		Bundle bundle = this.getArguments();
		categoryId = bundle.getString(Utility.EXTRA_CATEGORY_ID);    

		if(Utility.hasInternetConnection()){	
			new LoadFirstListView().execute();
		} else {
			textNoResult.setVisibility(View.GONE);
			layoutRetry.setVisibility(View.VISIBLE);
			textAlert.setText(R.string.no_connection);
		}	

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				HashMap<String, String> item = new HashMap<String, String>();
		        item = items.get(position);
				callback.onListSelected(item.get(KEY_EVENT_ID));
				list.setItemChecked(position, true);
			}
		});

		list.setHasMoreItems(true);
		list.setPagingableListener(new PagingListView.Pagingable() {
			@Override
			public void onLoadMoreItems() {
				if(Utility.hasInternetConnection()){	
					json = null;
					if (loadMore == 1) {
						new LoadMoreListView().execute();
					} else {
						list.onFinishLoading(false, null);
					}
				} else {
					Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		return view;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            callback = (OnDataListSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
	
	private class LoadFirstListView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Please wait..");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }
 
        protected Void doInBackground(Void... unused) {
        	getDataFromServer();
        	return (null);
        }
 
        protected void onPostExecute(Void unused) {
        	if (isAdded()) {
	            if (items.size() != 0) {
	            	layoutRetry.setVisibility(View.GONE);
	            	textNoResult.setVisibility(View.GONE);
	            	list.setVisibility(View.VISIBLE);
	            	
	            	la = new AdapterHome(getActivity(), items);
	            	if (list.getAdapter() == null) {
		            	list.setAdapter(la);
		            }
	            } else {
	            	if (json != null) {
	            		textNoResult.setVisibility(View.VISIBLE);
						layoutRetry.setVisibility(View.GONE);	
		            } else {
		            	textNoResult.setVisibility(View.GONE);
						layoutRetry.setVisibility(View.VISIBLE);
	            		textAlert.setText(R.string.error_server);
	            	}
	            }
        	}
        
            if(dialog.isShowing()) {
            	dialog.dismiss();
			}
        }
    }
	
    private class LoadMoreListView extends AsyncTask<Void, Void, Void> {
    	
        protected Void doInBackground(Void... unused) {
			previousPage = currentPage;
			currentPage += FragmentHome.paramValueItemPerPage;
			getDataFromServer();
            return (null);
        }
 
        protected void onPostExecute(Void unused) {
 
            if(json != null){
	            int currentPosition = list.getFirstVisiblePosition();
	            layoutRetry.setVisibility(View.GONE);
	            la = new AdapterHome(getActivity(),items);
	            if(list.getAdapter() == null){
	            	list.setAdapter(la);
	            }
	            
	            list.setSelectionFromTop(currentPosition + 1, 0);
	            list.onFinishLoading(true, items);

            } else {
            	currentPage = previousPage;
            	Toast.makeText(getActivity(), R.string.error_server, Toast.LENGTH_SHORT).show();
            }

            if(dialog.isShowing()) {
            	dialog.dismiss();
			}
        }
    }
	
	public void getDataFromServer(){
        try {
        	json = byCategoryEvent(categoryId, currentPage);
            if(json != null){
            	JSONArray eventsArray = json.getJSONArray(array_deal_by_category);
            	int countTotal = Integer.valueOf(json.getString(UserFunctions.KEY_COUNT_ENTRIES)) - 1;
            	
            	if (((countTotal -= FragmentHome.paramValueItemPerPage) < currentPage)) {
            		loadMore = 0;
            	} else {
            		loadMore = 1;
            	}
            	
	            lengthData = eventsArray.length();
	            eventsId = new String[lengthData];
	            eventsTitle	= new String[lengthData];
	            eventsEndDate = new String[lengthData];
	            eventsIcon = new String[lengthData];
	            
	            for (int i = 0; i < lengthData; i++) {
	            	JSONObject eventObject = eventsArray.getJSONObject(i);
	            	HashMap<String, String> map = new HashMap<String, String>();
	            	eventsId[i] = eventObject.getString(KEY_EVENT_ID);
	            	eventsTitle[i] = eventObject.getString(KEY_EVENT_TITLE);
	            	eventsEndDate[i] = eventObject.getString(KEY_EVENT_ENDDATE);
	            	eventsIcon[i] = eventObject.getString(KEY_CATEGORY_ICON);

				    map.put(KEY_EVENT_ID, eventsId[i]);
		            map.put(KEY_EVENT_TITLE, eventsTitle[i]);
		            map.put(KEY_EVENT_ENDDATE, eventsEndDate[i]);
		            map.put(KEY_CATEGORY_ICON, eventsIcon[i]);
		            
		            items.add(map);
	            }
            }
                                
        } catch (JSONException e) {
        	// TODO: A aparut o eroare la primirea datelor de la server.
        }      
    }

    @Override
    public void onDestroy() {
    	list.setAdapter(null);
    	super.onDestroy();
    	
    }
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnRetry:
				if (Utility.hasInternetConnection()) {	
					json = null;
					new LoadFirstListView().execute();
				} else {
					textNoResult.setVisibility(View.GONE);
					layoutRetry.setVisibility(View.VISIBLE);
					textAlert.setText(R.string.no_connection);
				}
				break;
			default:
				break;
		}
	}
}
