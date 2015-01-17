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

public class FragmentSearch extends Fragment implements OnClickListener{
	private OnSearchListSelectedListener callback;

	private ArrayList<HashMap<String, String>> items;
	private ProgressDialog dialog;
	
	// Creza o instanta de lista si adaptor de lista
	private PagingListView list;
	private AdapterHome la;
	
	private String keyword;
	
	// Flag utilizat pentru a incarca si alte pagini
	private int currentPositon = 0;
	
	private TextView textNoResult, textAlert;
	private Button buttonRetry; 
	private LinearLayout layoutRetry;
	
	// Flag utilizat pentru a marca pagina curenta
	private JSONObject json;
    private int currentPage = 0;
    private int previousPage;
	
	private int intLengthData;
	
	// Creaza un vector unde sunt stocate informatiile
	private String[] eventsId;
	private String[] eventsTitle;
	private String[] eventsEndDate;
	private String[] eventsImage;
	private String[] eventsIcon;
	
	private int loadMore = 0;
	
	public interface OnSearchListSelectedListener{
		public void onListSelected(String idSelected);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		
		list = (PagingListView) view.findViewById(R.id.list);
		textNoResult = (TextView) view.findViewById(R.id.lblNoResult);
		textAlert = (TextView) view.findViewById(R.id.lblAlert);
		layoutRetry = (LinearLayout) view.findViewById(R.id.lytRetry);
		buttonRetry = (Button) view.findViewById(R.id.btnRetry);
		buttonRetry.setOnClickListener(this);
		
		Utility.context = getActivity();
		items = new ArrayList<HashMap<String, String>>();

  		Bundle bundle = this.getArguments();
  		keyword = bundle.getString(Utility.EXTRA_KEYWORD);
  		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if (currentPositon != position) {
					HashMap<String, String> item = new HashMap<String, String>();
			        item = items.get(position);
			        
					callback.onListSelected(item.get(KEY_EVENT_ID));
					list.setItemChecked(position, true);	
				}
			}
		});
		
		if (Utility.hasInternetConnection()) {	
			if (list.getAdapter() != null) {
				json = null;
				items.clear();	
		    	list.setAdapter(null);
				currentPage = 0;
		        list.invalidateViews();
			}

			new loadFirstListView().execute();
		} else {
			textNoResult.setVisibility(View.GONE);
			layoutRetry.setVisibility(View.VISIBLE);
    		textAlert.setText(R.string.no_connection);
		}
		
		// Ofera posibilitatea utilizatorului de a incarca mai multa informatie
		list.setHasMoreItems(true);
		list.setPagingableListener(new PagingListView.Pagingable() {
			@Override
			public void onLoadMoreItems() {
				if(Utility.hasInternetConnection()){	
					json = null;
					if (loadMore == 1) {
						new loadMoreListView().execute();
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
            callback = (OnSearchListSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
		
	private class loadFirstListView extends AsyncTask<Void, Void, Void> {
		 
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
	            if (!items.isEmpty()) {
	            	layoutRetry.setVisibility(View.GONE);
	            	list.setVisibility(View.VISIBLE);
	            	textNoResult.setVisibility(View.GONE);
	            	            	
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

            if (dialog.isShowing()) {
            	dialog.dismiss();
			}
        }
    }
	
    private class loadMoreListView extends AsyncTask<Void, Void, Void> {
 
        protected Void doInBackground(Void... unused) {      	
			previousPage = currentPage;
			currentPage += FragmentHome.paramValueItemPerPage;
			getDataFromServer();
            return (null);
        }
 
        protected void onPostExecute(Void unused) {
            if (json != null) {
	            int currentPosition = list.getFirstVisiblePosition();
	            list.setVisibility(View.VISIBLE);
	            
	            layoutRetry.setVisibility(View.GONE);
	            la = new AdapterHome(getActivity(), items);
	            
	            if (list.getAdapter() == null) {
	            	list.setAdapter(la);
	            }
	            
	            list.setSelectionFromTop(currentPosition + 1, 0);
	            list.onFinishLoading(true, items);
        	
	        } else {
	        	currentPage = previousPage;
	    		Toast.makeText(getActivity(), R.string.error_server, Toast.LENGTH_SHORT).show();
	        }
            
	        if (dialog.isShowing()) {
	        	dialog.dismiss();
			}
        }
    }
    
	
	public void getDataFromServer(){
        try {
        	keyword = keyword.replace(" ", "%20");
        	json = searchForEvents(keyword, currentPage, FragmentHome.paramValueItemPerPage);
        	keyword = keyword.replace("%20", " ");
           
            if (json != null) {
	            JSONArray eventsArray = json.getJSONArray(array_events_search);

            	int countTotal = Integer.valueOf(json.getString(KEY_COUNT_ENTRIES)) - 1;
            	if (((countTotal -= FragmentHome.paramValueItemPerPage) < currentPage)) {
            		loadMore = 0;
            	} else {
            		loadMore = 1;
            	}
            	
	            intLengthData = eventsArray.length();
	            eventsId = new String[intLengthData];
	            eventsTitle = new String[intLengthData];
	            eventsEndDate = new String[intLengthData];
	            eventsImage = new String[intLengthData];
	            eventsIcon 	= new String[intLengthData];
	            
	            // Store data to variable array
	            for (int i = 0; i < intLengthData; i++) {
	            	JSONObject eventObject = eventsArray.getJSONObject(i);
	
	            	HashMap<String, String> map = new HashMap<String, String>();
	            	
	            	eventsId[i] = eventObject.getString(KEY_EVENT_ID);
	            	eventsTitle[i] = eventObject.getString(KEY_EVENT_TITLE);
	            	eventsEndDate[i] = eventObject.getString(KEY_EVENT_ENDDATE);
	            	eventsImage[i]= eventObject.getString(KEY_EVENT_IMAGE);
	            	eventsIcon[i] = eventObject.getString(KEY_CATEGORY_ICON);
	            	
				    map.put(KEY_EVENT_ID, eventsId[i]);
		            map.put(KEY_EVENT_TITLE, eventsTitle[i]);
		            map.put(KEY_EVENT_ENDDATE, eventsEndDate[i]);
		            map.put(KEY_EVENT_IMAGE, eventsImage[i]);
		            map.put(KEY_CATEGORY_ICON, eventsIcon[i]);
	            
		            items.add(map);
	            }
	            
				currentPositon += FragmentHome.paramValueItemPerPage;
            }        
                               
        } catch (JSONException e) {
        	// TODO: Jurnalizeaza exceptia!
        }   
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnRetry:
				if (Utility.hasInternetConnection()) {	
					json = null;
					new loadFirstListView().execute();
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
