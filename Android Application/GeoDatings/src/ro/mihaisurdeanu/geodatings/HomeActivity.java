/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ro.mihaisurdeanu.geodatings.fragments.FragmentHome;
import ro.mihaisurdeanu.geodatings.fragments.FragmentNavigationDrawer;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import ro.mihaisurdeanu.geodatings.R;

import static ro.mihaisurdeanu.geodatings.libraries.UserFunctions.*;

public class HomeActivity extends ActionBarActivity implements 
	FragmentNavigationDrawer.NavigationDrawerCallbacks, 
	FragmentHome.OnDataListSelectedListener,
	SearchView.OnQueryTextListener,
	SearchView.OnSuggestionListener, OnClickListener {

	private SuggestionsAdapter mSuggestionsAdapter;
	
	private FragmentNavigationDrawer navDrawerFragment;
   
	/** Ferestra de dialog curenta asociata acestei activitati */
	private Dialog dialog;
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	/** Instanta a serviciului Google Cloud Messaginig */
	private GoogleCloudMessaging gcm;
	
	private JSONObject jsonGCM;
	
	private String gcmRegisterId;
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		if (!navDrawerFragment.isDrawerOpen()) {
			// Adauga optiunile meniului in activitatea principala
			getMenuInflater().inflate(R.menu.actionbar_home, menu);
			
			// Cand meniul principal este deschis atunci nu se va mai putea cauta.
		    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		    SearchView searchView = (SearchView) menu.findItem(R.id.abSearch).getActionView();
		    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		    searchView.setIconifiedByDefault(false); 
		    
			return true;
		}
		
		return super.onCreateOptionsMenu(menu);     
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		Utility.context = this;
 
		// Google Play Services sunt necesare pentru a continua.
		// Daca aceste servicii nu sunt gasite, utilizatorul va fi notificat.
		// In schimb, daca sunt gasite atunci se va purcede cu inregistrarea
		// dispozitivului curent pentru a putea primi notificari.
		if (Utility.hasInternetConnection()) {
			// Verifica daca servicile Google Play sunt disponibile.
			if (checkPlayServices()) {			
	            gcm = GoogleCloudMessaging.getInstance(this);
	            // Obtine ID-ul cu care se va inregistra acest dispozitiv
	            gcmRegisterId = getRegistrationId(this);
	            // Actiune de inregistrare se va face in background
	            new GCMRegisterTask().execute();
	        }
		}
		
		int paramOverlay = Utility.loadIntPreference(Utility.UTILS_OVERLAY);
		if(paramOverlay != 1) showOverLay();

		// Seteaza meniul principal
		navDrawerFragment = (FragmentNavigationDrawer) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		navDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));


		Bundle bundle = new Bundle();
		bundle.putString(Utility.EXTRA_ACTIVITY, Utility.EXTRA_ACTIVITY_HOME);
		FragmentHome fragmentHome = new FragmentHome();
		fragmentHome.setArguments(bundle);

		getSupportFragmentManager().beginTransaction()
			.add(R.id.frame_content, fragmentHome).commit();
	}	
	
	/**
	 * Defineste ce actiuni se petrec cand o optiune din meniul principal este
	 * aleasa de catre utilizator.
	 */
	@Override
	public void onNavigationDrawerItemSelected(int index) {
		Intent intent;
		switch (index) {
			// Se face un refresh la informatiile deja afisate.
			case 0:
				Bundle bundle = new Bundle();
				bundle.putString(Utility.EXTRA_ACTIVITY, Utility.EXTRA_ACTIVITY_HOME);
				FragmentHome fragmentHome = new FragmentHome();
				fragmentHome.setArguments(bundle);

	            getSupportFragmentManager().beginTransaction()
	            		.add(R.id.frame_content, fragmentHome).commit();
	            break;
			// Redirecteaza utilizatorul catre activitatea cu categoriile
			case 1:
				intent = new Intent(this, CategoryActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.open_next, R.anim.close_main);
				break;	
			// Redirecteaza utilizatorul catre activitatea cu setariile aplicatiei
			case 2:
				intent = new Intent(this, SettingActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.open_next, R.anim.close_main);
				break;	
			// Redirecteaza utilizatorul catre informatiile despre aplicatie.
			case 3:
				intent = new Intent(this, AboutActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.open_next, R.anim.close_main);
				break;
		}
	}
	
	/**
	 * Verifica daca pentru acest dispozitiv exista instalate serviciile Google
	 * Play Services. Daca nu sunt deja instalate, utilizatorul va vedea o
	 * fereastra de dialog prin care va putea fi redirectat catre Play Store
	 * pentru a purcede la descarcarea si fixarea acestei probleme.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            // TODO: Logheaza faptul ca acest device nu e suportat
	            finish();
	            overridePendingTransition (R.anim.open_main, R.anim.close_next);
	        }
	        return false;
	    }
	    
	    Utility.saveIntPreference(Utility.CHECK_PLAY_SERV,1);
	    return true;
	}
	
	/**
	 * Returneaza ID-ul cu care acest dispozitiv este inregistrat in cadrul
	 * platformei Google Cloud Messaging. Daca rezultatul este un string gol,
	 * atunci inseamna ca va trebui sa inregistram dispozitivul.
	 */
	private String getRegistrationId(Context context) {
		String registrationId = Utility.loadStringPreference(Utility.REG_ID);
		
	    if (registrationId.equals(Utility.VALUE_DEFAULT)) {
	    	// TODO: Jurnalizeaza faptul ca ID-ul nu a fost gasit
	        return "";
	    }

	    int registeredVersion = Utility.loadIntPreference(Utility.APP_VERSION);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	    	// TODO: Jurnalizeaza faptul ca versiunea aplicatiei a fost schimbata.
	        return "";
	    }
	    
	    return registrationId;
	}

	/**
	 * Task asincron prin care se incearca inregistrarea aplicatiei curente
	 * in cadrul platformei GCM.
	 */
	public class GCMRegisterTask extends AsyncTask<Void, Void, String> {
		
    	@Override
		protected String doInBackground(Void... params) {
    		if (gcm == null) {
    			gcm = GoogleCloudMessaging.getInstance(Utility.context);
    		}
            	
    		if (gcmRegisterId.isEmpty()) {
    			// Se incearca inregistrarea de 5 ori consecutiv. 
    			for (int i = 0; i < 5; i++) {
    				try {
    					gcmRegisterId = gcm.register(SENDER_ID);
                        break;
    				} catch (IOException e){
    					Log.i("GCM", "Error doInBackground:" + e.getMessage());
    					// Inainte de a incerca din nou e va astepta putin
    					try {
    						Thread.sleep(3000);
    					} catch (InterruptedException e1) {}
    				}
    			}
    		}
    		
    		// Daca procesul de inregistrare a avut loc,
			// atunci se va trimite si la server acesta.
			return sendRegistrationIdToServer();
		}
    	
    	@Override
		protected void onPostExecute(String result) {
    		// Vom considera ca inregistrarea s-a realizat cu succes
    		// doar atunci cand se va intoarce 1, de la server.
    		if ("1".equals(result)) {
    			storeRegistrationId(Utility.context, gcmRegisterId);
    		} else {
    			// TODO: Jurnalizeaza exceptia!
    		}
		}
	}
	
	/**
	 * Trimite rezultatul ID-ul inregistrat catre serverul partener.
	 */
	private String sendRegistrationIdToServer() {
		try {      	
			if (!gcmRegisterId.isEmpty()) {
	        	// Obtine si un ID unic al acestui dispozitiv
				String uniqueId = getUniquePseudoID();
				
				jsonGCM = gcmRegisterId(gcmRegisterId, uniqueId);
				
	            if (jsonGCM != null) {
	            	JSONArray jsonArray = jsonGCM.getJSONArray(array_register_id);
					JSONObject jsonObj = jsonArray.getJSONObject(0);
					
					return jsonObj.getString(KEY_STATUS);
	            }
    		}                               
        } catch (JSONException e) {
            // TODO: Jurnalizeaza exceptia.
        }
		
		// Se returneaza null daca ceva nu este in ordine.
		return null;
	}
	
	/**
	 * Pastreaza ID-ul de inregistrare din GCM si intern, prin sistemul de
	 * preferinte. In asa fel, evitam sa interogam de fiecare serverul GCM.
	 */
	private void storeRegistrationId(Context context, String regId) {
	    int appVersion = getAppVersion(context);
	    Utility.saveIntPreference(Utility.APP_VERSION, appVersion);
	    Utility.saveStringPreference(Utility.REG_ID, regId);
	}
	
	/**
	 * Obtine versiunea curenta a aplicatie.
	 * TODO: Metoda ar trebui mutata in alta clasa.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    checkPlayServices();
	}

	public static String getUniquePseudoID() {
		String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + 
				(Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + 
				(Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + 
				(Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

	    // Multumiri lui @Roman SL
	    // http://stackoverflow.com/a/4789483/950427
	    String serial = null;
	    try {
	        serial = android.os.Build.class.getField("SERIAL").get(null).toString();
	        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	    } catch (Exception e) {
	        serial = "serial";
	    }

	    // Multumiri lui @Joe
	    // http://stackoverflow.com/a/2853253/950427
	    return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		 Toast.makeText(this, "You searched for: " + query, Toast.LENGTH_LONG).show();
	     return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}
	
	private class SuggestionsAdapter extends CursorAdapter {
        public SuggestionsAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView textView = (TextView) view;
            final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
            textView.setText(cursor.getString(textIndex));
        }
    }
	
	@Override
	public boolean onSuggestionSelect(int position) {
		return false;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		Cursor cursor = (Cursor) mSuggestionsAdapter.getItem(position);
        String query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
        Toast.makeText(this, "Suggestion clicked: " + query, Toast.LENGTH_LONG).show();
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        	case R.id.abSearch:
        		break;
	        case R.id.abAroundYou:
	        	Intent iCart = new Intent(this, MapActivity.class);
				startActivity(iCart);
				overridePendingTransition (R.anim.open_next, R.anim.close_main);
	            break;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		return true;
	}

	@Override
	public void onListSelected(String idSelected) {
	}
	
	/**
	 * Metoda folosita pentru afisarea instructiunilor de utilizare a
	 * activitatii principale si a meniului principal.
	 */
	private void showOverLay(){
		dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.overlay_view);
		LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.overlayLayout);
		layout.setOnClickListener(this);
		dialog.show();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.overlayLayout) {
			Utility.saveIntPreference(Utility.UTILS_OVERLAY, 1);
			dialog.dismiss();
		}
	}
	
}