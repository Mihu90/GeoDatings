/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;

import ro.mihaisurdeanu.geodatings.R;
import ro.mihaisurdeanu.geodatings.dialogs.TwoOptionsDialog;
import ro.mihaisurdeanu.geodatings.utilities.Utility;

public class SettingActivity extends PreferenceActivity implements 
	OnSharedPreferenceChangeListener,
	Preference.OnPreferenceChangeListener,
	OnPreferenceClickListener {
	
	private PendingAction pendingAction = PendingAction.NONE;	
	private UiLifecycleHelper uiHelper;
    
	// Cateva obiecte instantiate
	private CheckBoxPreference facebookBox, overlayBox;

	/** Numele utilizatorului care este autentificat pe contul de Facebook. */
	private String facebookName = "";
	
	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        // Salveaza contextul curent al aplicatiei.
 		Utility.context = this;
 		
 		// Incarca setarile dintr-un fisier XML.
        addPreferencesFromResource(R.xml.setting_preference);
        
        // Inregistreaza un ascultator pentru a prinde modificarile
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
    	ActionBar actionBar = getActionBar();
     	actionBar.setDisplayHomeAsUpEnabled(true);
     	
     	// Inregistreaza un ascultator atunci cand se doreste schimbarea
        // adresei web a serverului de date.
        EditTextPreference serverAddress = (EditTextPreference) findPreference(getString(R.string.setting_prefkey_server));
		serverAddress.setOnPreferenceChangeListener(this);
		
		// Verifica daca sunt versiuni deja setate
		ListPreference apis = (ListPreference)findPreference(getString(R.string.setting_prefkey_apis));
		apis.setOnPreferenceChangeListener(this);
		// Daca exista atunci se vor seta, altfel setarea va fi dezactivata
		String data = Utility.loadStringPreference(Utility.APIS_ENTRIES);
		if (data != null) {
			List<String> entries = Utility.deserializeList(data, ",");
			apis.setEntries(entries.toArray(new CharSequence[entries.size()]));
			data = Utility.loadStringPreference(Utility.APIS_ENTRY_VALUES);
			entries = Utility.deserializeList(data, ",");
			apis.setEntryValues(entries.toArray(new CharSequence[entries.size()]));
			// Setarea devine activa si totodata valoarea implicita este aleasa
			apis.setDefaultValue(Utility.loadStringPreference(Utility.APIS_DEFAULT_VALUE));
			apis.setEnabled(true);
		} else {
			apis.setEnabled(false);
		}
		
		// Inregistreaza un ascultator si pe butonul de conectare cu Facebook
        facebookBox = (CheckBoxPreference) findPreference(getString(R.string.setting_prefkey_facebook));
        facebookBox.setOnPreferenceClickListener(this);
        facebookBox.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			public boolean onPreferenceClick(Preference preference) {
				onFacebookClick();
				return false;
			}
		});
        // Seteaza ID-ul Facebook
    	facebookName = Utility.loadStringPreference(Utility.FACEBOOK_NAME);
		
		// Seteaza ascultatori si pe celelalte elemente ramase.
     	ListPreference viewType = (ListPreference) findPreference(getString(R.string.preferences_type));   
     	// Ne asiguram ca setarile de tip lista au valori implicite
        if (viewType.getValue() == null) {
        	viewType.setValueIndex(0);
        }
        viewType.setSummary(viewType.getValue().toString());
        viewType.setOnPreferenceChangeListener(this);
       
    	// Verifica parametrul prin care se afiseaza instructiunile de folosire.
		int paramOverlay = Utility.loadIntPreference(Utility.UTILS_OVERLAY);
        overlayBox = (CheckBoxPreference) findPreference(getString(R.string.preferences_overlay));
		if (paramOverlay != 1) {
			overlayBox.setChecked(true);
		} else {
			overlayBox.setChecked(false);
		}
		// Seteaza si un ascultator
		overlayBox.setOnPreferenceClickListener(this);
	}
    
    /**
     * Eveniment apelat atunci cand se modifica valoarea unei setari. Setarea
     * trebuie sa fie de tipul EditTextPreference sau ListPreference pentru a
     * putea fi receptionata aici.
     */
    @Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
    	String key = preference.getKey();
		if (key.equals(getString(R.string.setting_prefkey_server))) {
			new RetrieveVersionsTask().execute((String)newValue);
		} else if (key.equals(getString(R.string.setting_prefkey_apis))) {
			// Salveaza modificarea facuta de utilizator
			Utility.saveStringPreference(Utility.APIS_DEFAULT_VALUE, (String)newValue);
		} else if (key.equals(getString(R.string.preferences_type))) {
			String textValue = newValue.toString();
        	ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(textValue);
            listPreference.setDefaultValue(index);
            listPreference.setValueIndex(index);
            Utility.saveIntPreference(getString(R.string.preferences_type), index);               
            preference.setSummary(textValue);
		}
		
		// Setarile sunt mereu salvate
		return true;
	}
    
    private class VersionsResult {
    	public List<String> entries;
    	public List<String> entryValues;
    }
    
    /**
     * Task prin care sunt obtinute versiunile valide de pe server, in vederea
     * utilizarii lor in receptionarea datelor.
     */
    private class RetrieveVersionsTask extends AsyncTask<String, Void, VersionsResult> {
    	
        protected VersionsResult doInBackground(String... urls) {
        	String url = (String)urls[0];
			if (!url.startsWith("http://")) {
				url = "http://" + url;
			}
			if (!url.endsWith("/")) {
				url = url + "/";
			}
			url = url + "versions.xml";
			
			// Incearca conectarea la server si preluarea API-urilor curente.
			try {
			    URL urlObj = new URL(url);
			    
			    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document document = builder.parse(urlObj.openStream());
			    
			    VersionsResult result = new VersionsResult();
			    result.entries = new ArrayList<String>();
			    result.entryValues = new ArrayList<String>();
			    
			    String humanFormat = getString(R.string.setting_apis_human);
			    
			    Element rootElement = document.getDocumentElement();
				NodeList listApis = rootElement.getElementsByTagName("api");
				int countApis = listApis.getLength();
				for (int i = 0; i < countApis; i++) {
					Element api = (Element)listApis.item(i);
					String apiVersion = api.getAttribute("version");
					String appVersion = Utility.getCurrentAppVersion();
					// Versiunea API-ului este compatibila cu cea a aplicatiei?
					boolean compatibleVersion = true;
					if (appVersion != null) {
						// Verificam sa avem o versiune mai noua decat cea minima.
						String minVersion = api.getAttribute("min");
						if (!minVersion.isEmpty() && 
								Utility.compareVersions(appVersion, minVersion) < 0) {
							compatibleVersion = false;
						}
						// Verificam sa avem o versiune mai veche decat cea maxima.
						String maxVersion = api.getAttribute("max");
						if (!maxVersion.isEmpty() && 
								Utility.compareVersions(appVersion, maxVersion) > 0) {
							compatibleVersion = false;
						}
					}
					
					// Numai daca versiunea e compatibila.
					if (compatibleVersion) {
						result.entries.add(String.format(humanFormat, apiVersion));
						result.entryValues.add(apiVersion);
					}
				}
				return result;
			}
			// Adresa specifica nu este valida sau fisierul XML principal nu exista
			catch (Exception e) {
				// TODO: Jurnalizeaza exceptia
				return null;
			}
        }

        protected void onPostExecute(VersionsResult result) {
        	// Avem un rezultat cu cel putin o versiune?
        	if (result != null && !result.entries.isEmpty()) {
        		@SuppressWarnings("deprecation")
        		ListPreference apis = (ListPreference)findPreference(getString(R.string.setting_prefkey_apis));
        		apis.setEntries(result.entries.toArray(new CharSequence[result.entries.size()]));
        		Utility.saveStringPreference(Utility.APIS_ENTRIES,
        									 Utility.serializeList(result.entries, ","));
        		apis.setEntryValues(result.entryValues.toArray(new CharSequence[result.entryValues.size()]));
        		Utility.saveStringPreference(Utility.APIS_ENTRY_VALUES,
						 					 Utility.serializeList(result.entryValues, ","));
        		// Valoarea implicita va fi chiar ultimul element din lista,
        		// asta pentru ca vom considera ultima valoare ca fiind cea
        		// mai noua.
        		int index = result.entryValues.size() - 1;
        		String defaultOption = result.entryValues.get(index);
        		apis.setDefaultValue(defaultOption);
        		Utility.saveStringPreference(Utility.APIS_DEFAULT_VALUE,
        									 defaultOption);
        		// Se activeaza setarea
        		apis.setEnabled(true);
        	}
        }
    }
 
    /**
     * Incearca deschiderea unei sesiuni Facebook.
     */
    private void openFacebookSession() {
    	Session.openActiveSession(this, true, new Session.StatusCallback() {
    		@Override
    		public void call(Session session, SessionState state, Exception exception) {
    			final Session fSession = session;
	    		if (session.isOpened()) {
	    			Request.newMeRequest(session, new Request.GraphUserCallback() {
	    				@Override
	    				public void onCompleted(GraphUser user, Response response) {
	    					if (fSession == Session.getActiveSession()) {
	    						if (user != null) {
	    							facebookName = user.getName();
	    							facebookBox.setChecked(true);
	    							facebookBox.setSummary(getString(R.string.connected_social) + " " + facebookName);
	    						}
	    					}
	    					if (response.getError() != null) {
				            	// TODO: Jurnalizeaza exceptii / erori
	    					}
	    				}
	    			}).executeAsync();
	            }
	    		
	    	    if (exception != null) {
					Utility.saveStringPreference(Utility.FACEBOOK_NAME, facebookName);
	    	    }
	    	}
    	});
	}
	    
	/**
	 * Ce se intampla cand a fost apasat pe butonul de conectare cu platforma
	 * Facebook?
	 */
	private void onFacebookClick() {
		// Daca sesiunea deja exista, inseamna ca utilizatorul vrea sa fie
		// deconectat. Inainte sa facem asta, ar cam trebui sa il mai intrebam
		// inca o data ca sa fim siguri.
		if (hasFacebookSession()) {
	  		TwoOptionsDialog confirmDialog = new TwoOptionsDialog(this);
	  		confirmDialog.setTitle(R.string.confirm);
	  		confirmDialog.setMessage(R.string.delete_facebook_connection);
	  		confirmDialog.setCancelable(false);
	  		confirmDialog.setOptionNames(R.string.yes, R.string.no);
	  		confirmDialog.display(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							logoutFacebook();
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							dialog.cancel();
							facebookBox.setChecked(true);
							break;
					}
				}
			});
		} else {
			facebookBox.setChecked(false);
			openFacebookSession();
			handlePendingAction();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (pendingAction != PendingAction.NONE &&
				(exception instanceof FacebookOperationCanceledException ||
				exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(SettingActivity.this)
				.setTitle(getString(R.string.msgbox_title_cancel))
				.setMessage(getString(R.string.msgbox_message_not_granted))
				.setPositiveButton((getString(R.string.msgbox_button_ok)), null)
				.show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		if (enableButtons) {
			Request.newMeRequest(session, new GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (response != null) {
						try {
							facebookName = user.getName();
							Utility.saveStringPreference(Utility.FACEBOOK_NAME, facebookName);
						} catch (Exception e) {
							// TODO: Jurnalizeaza exceptie
						}
					}
				}
			}).executeAsync();
	 	        	
			facebookBox.setChecked(true);
			String connectedAsUser = getString(R.string.connected_social) + " " + facebookName;
			facebookBox.setSummary(connectedAsUser);
		} else {
			facebookBox.setChecked(false);
			facebookBox.setSummary(getString(R.string.connect));
		}
	}

	private void handlePendingAction() {
		pendingAction = PendingAction.NONE;
	}
	    
	public void logoutFacebook() {
		Session session = Session.getActiveSession();
		if (session != null) {
			session.closeAndClearTokenInformation();
			facebookBox.setChecked(false);
			facebookBox.setSummary(getString(R.string.connected_social) + " " + facebookName);
		}
		Session.setActiveSession(null);
	}
		
	public boolean hasFacebookSession() {
		Session session = Session.getActiveSession();
		return session != null && session.isOpened();
	}
 	
	@Override
	public void onResume() {
		super.onResume();
		    
		uiHelper.onResume();
        AppEventsLogger.activateApp(this);

        updateUI();
	}
		
	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();

	    AppEventsLogger.deactivateApp(this);
	}
	    
	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy() {
 		super.onDestroy();
 		uiHelper.onDestroy();
 		
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	 	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(getString(R.string.preferences_overlay))) {
			if (overlayBox.isChecked()){
				Utility.saveIntPreference(Utility.UTILS_OVERLAY, 0);
				Toast.makeText(getApplicationContext(), "Overlay was activated.", Toast.LENGTH_SHORT).show();
				overlayBox.setChecked(true);
			} else {
				Utility.saveIntPreference(Utility.UTILS_OVERLAY, 1);
				overlayBox.setChecked(false);
			}
		}
	 		
		return true;
	}	 	
	 	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.preferences_zoom))) {
			int value = sharedPreferences.getInt(getString(R.string.preferences_zoom), 0);
			Utility.saveIntPreference(getString(R.string.preferences_zoom),value);			    
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransition (R.anim.open_main, R.anim.close_next);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
 		
	@Override
	public void onBackPressed() {
		super.onBackPressed();
 		overridePendingTransition (R.anim.open_main, R.anim.close_next);
	}
 }
