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
import android.util.DisplayMetrics;
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

public class FragmentHome extends Fragment implements OnClickListener {
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
    
    private String[] eventIds;
    private String[] eventTitles;
    private String[] startDates;
    private String[] endDates;
    private String[] eventIcons;
    private String[] eventImages;
	
	private int loadMore = 0;
	
	public static int paramValueItemPerPage;
	
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
		
		setItemPerPage();

		if (Utility.hasInternetConnection()) {	
			new LoadFirstListView().execute();
		} else {
			textNoResult.setVisibility(View.GONE);
			layoutRetry.setVisibility(View.VISIBLE);
			textAlert.setText(R.string.no_connection);
		}	
				        
		// Listener to get selected id when list item clicked
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
				if (Utility.hasInternetConnection()) {	
					json = null;
					if (loadMore==1) {
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
	            if (!items.isEmpty()) {
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
        	
            if (dialog.isShowing()) {
            	dialog.dismiss();
			}
        }
    }
	
    private class LoadMoreListView extends AsyncTask<Void, Void, Void> {
 
        protected Void doInBackground(Void... unused) {
			previousPage = currentPage;
			currentPage += paramValueItemPerPage;
			getDataFromServer();
            return (null);
        }
 
        protected void onPostExecute(Void unused) {
            if (json != null) {
	            int currentPosition = list.getFirstVisiblePosition();
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
	
	public void getDataFromServer() {
        try {
			json = getLatestEvents(currentPage);
			
            if (json != null) {
            	JSONArray eventsArray = json.getJSONArray(array_latest_deals);
            	
            	// Numarul total de evenimente returnate de catre server
            	int total = Integer.valueOf(json.getString(UserFunctions.KEY_COUNT_ENTRIES));
            	total -= 1;
            	
            	// Verificam daca mai sunt evenimente de incarcat, sau au fost deja
            	// incarcate toate
            	if ((total -= paramValueItemPerPage) < currentPage) {
            		loadMore = 0;
            	} else {
            		loadMore = 1;
            	}
            	
	            int lengthData = eventsArray.length();
	            eventIds = new String[lengthData];
	            eventTitles = new String[lengthData];
	            startDates = new String[lengthData];
	            endDates = new String[lengthData];
	            eventIcons = new String[lengthData];
	            eventImages = new String[lengthData];
	            
	            // Incarcam toate evenimentele obtinute intr-un map intern
	            for (int i = 0; i < lengthData; i++) {
	            	JSONObject eventsObject = eventsArray.getJSONObject(i);
	            	HashMap<String, String> map = new HashMap<String, String>();
	            	eventIds[i] = eventsObject.getString(KEY_EVENT_ID);
	            	eventTitles[i] = eventsObject.getString(KEY_EVENT_TITLE);
	            	startDates[i] = Utility.standardDateFormat(Long.parseLong(eventsObject.getString(KEY_EVENT_STARTDATE))).toString();
	            	endDates[i] = Utility.standardDateFormat(Long.parseLong(eventsObject.getString(KEY_EVENT_ENDDATE))).toString();
	            	eventIcons[i] = eventsObject.getString(KEY_CATEGORY_ICON);
	            	eventImages[i] = eventsObject.getString(KEY_EVENT_IMAGE);

				    map.put(KEY_EVENT_ID, eventIds[i]);
		            map.put(KEY_EVENT_TITLE, eventTitles[i]);
		            map.put(KEY_EVENT_STARTDATE, startDates[i]);
		            map.put(KEY_EVENT_ENDDATE, endDates[i]);
		            map.put(KEY_CATEGORY_ICON, eventIcons[i]);
		            map.put(KEY_EVENT_IMAGE, eventImages[i]);
		            
		            items.add(map);
	            }
            }                      
                               
        } catch (JSONException e) {
            // TODO: Jurnalizeaza exceptia!
        }      
    }
	
	private void setItemPerPage(){
		// Specifica numarul de evenimente afisate pe pagina.
		// Acest numar se calculeaza in functie de dimensiunea curenta a ecranului.
		paramValueItemPerPage = Utility.loadIntPreference(Utility.ITEM_PAGE_LIST);
 		if (paramValueItemPerPage == Integer.valueOf(Utility.VALUE_DEFAULT)) {
 	     	
 			DisplayMetrics dm = new DisplayMetrics();
 			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
 			int wPixDefault = dm.widthPixels;
 			int hPixDefault = dm.heightPixels;
 			
 			Utility.saveIntPreference(Utility.PARAM_WIDTH_PIX, wPixDefault);
    		Utility.saveIntPreference(Utility.PARAM_HIGHT_PIX, hPixDefault);
	    		
 			double x = Math.pow(wPixDefault / dm.xdpi, 2);
 		    double y = Math.pow(hPixDefault / dm.ydpi, 2);
 		    double screenInches = Math.sqrt(x + y);
 		   
			if ((screenInches < Utility.MEDIUM)){
				Utility.saveIntPreference(Utility.ITEM_PAGE_LIST, Utility.ITEM_MEDIUM);
				paramValueItemPerPage = Utility.ITEM_MEDIUM;
			} else if((screenInches < Utility.LARGE)){
				Utility.saveIntPreference(Utility.ITEM_PAGE_LIST, Utility.ITEM_LARGE);
				paramValueItemPerPage = Utility.ITEM_LARGE;
			} else if((screenInches <= Utility.XLARGE)){
				Utility.saveIntPreference(Utility.ITEM_PAGE_LIST, Utility.ITEM_XLARGE);
				paramValueItemPerPage = Utility.ITEM_XLARGE;
			} else {
				Utility.saveIntPreference(Utility.ITEM_PAGE_LIST, Utility.ITEM_XXLARGE);
				paramValueItemPerPage = Utility.ITEM_XXLARGE;
			}			
		}
 		
 		paramValueItemPerPage += 1;
	}
	
    @Override
    public void onDestroy() {
    	list.setAdapter(null);
    	super.onDestroy();
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnRetry:
				if(Utility.hasInternetConnection()){	
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
