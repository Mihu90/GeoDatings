/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.utilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {
	public static final String EXTRA_EVENT_ID = "eventId";
	public static final String EXTRA_DEAL_TITLE	= "eventTitle";
	public static final String EXTRA_ADDRESS = "eventAddress";
	public static final String EXTRA_DEST_LAT = "destLatitude";
	public static final String EXTRA_DEST_LNG = "destLongitude";
	public static final String EXTRA_CATEGORY_MARKER = "categoryIcon";
	public static final String EXTRA_CATEGORY_ID = "categoryId";
	public static final String EXTRA_CATEGORY_NAME = "categoryName";
	public static final String EXTRA_ACTIVITY = "activityFlag";
	public static final String EXTRA_KEYWORD = "keywordSearch";
	
	public static final String EXTRA_ACTIVITY_CATEGORY = "activityCategory";
	public static final String EXTRA_ACTIVITY_HOME 	= "activityHome";
	
	// Declare variables for setting preference
	public static final String REG_ID  = "registerID";
	public static final String VALUE_DEFAULT = "0";
	public static final String APP_VERSION = "appVersion";
	public static String UTILS_OVERLAY = "0";
	public static String UTILS_PARAM_NOTIF	= "0";
	public static final String FACEBOOK_NAME = "facebookName";
	public static final String CHECK_PLAY_SERV = "playService";
	public static final String PARAM_WIDTH_PIX = "wPix";
	public static final String PARAM_HIGHT_PIX = "hPix";
	
	public static final String APIS_DEFAULT_VALUE = "apisDefaultValue";
	public static final String APIS_ENTRIES = "apisEntries";
	public static final String APIS_ENTRY_VALUES = "apisEntryValues";
    
	public static final String ITEM_PAGE_LIST	= "itemPageList";
	
	// Dimensiuni ale unor ecrane de telefoane mobile
	public static final int ITEM_XXLARGE = 12;
	public static final int ITEM_XLARGE = 8;
	public static final int ITEM_LARGE = 6;
	public static final int ITEM_MEDIUM = 5;
	public static final int XLARGE = 7;
	public static final int LARGE = 5;
	public static final int MEDIUM = 4;
	
	/** Contextul current al aplicatiei. */
	public static Context context;

	/**
	 * Metoda verifica daca exista o conexiune la internet. Nu conteaza ce tip
	 * de conexiune avem.
	 * 
	 * @return Returneaza adevarat daca exista conexiune la internet sau fals
	 *         daca nu exista.
	 */
	public static boolean hasInternetConnection() {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			// Cautam cel putin o retea care sa fie conectata la internet.
			NetworkInfo[] networks = connectivity.getAllNetworkInfo();
			for (NetworkInfo network : networks) {
				if (network.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Intoarce versiunea curenta a aplicatiei de fata. Daca s-a produs o
	 * eroare la obtinerea acestei informatii se va intoarce null.
	 */
	public static String getCurrentAppVersion() {
		try {
		    PackageManager manager = context.getPackageManager();
		    PackageInfo info = manager.getPackageInfo("ro.mihaisurdeanu.geodatings", 0);
		    return info.versionName;
		} catch(NameNotFoundException nnf) {
			// TODO: Jurnalizeaza exceptia
		}
		
		return null;
	}
	
	/**
	 * Compara doua versiuni de string-uri primite ca si parametru.
	 * 
	 * @param version1
	 * 			Prima versiune pasata.
	 * @param version2
	 * 			Cea de a doua versiune pasata ca parametru.
	 * @return Un numar negativ se returneaza daca prima versiune este mai
	 * 		   veche decat a doua, un numar pozitiv daca prima versiune e mai
	 * 		   noua decat a doua sau zero daca versiunile sunt egale.
	 */
	public static int compareVersions(String version1, String version2) {
        String v1 = normalisedVersion(version1);
        String v2 = normalisedVersion(version2);
        return v1.compareTo(v2);
    }
	
	private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder builder = new StringBuilder();
        for (String section : split) {
            builder.append(String.format("%" + maxWidth + 's', section));
        }
        return builder.toString();
    }
    
    /**
     * Desface o lista dupa un separator specificat ca si parametru.
     * 
     * @param list
     * 		Lista care va fi desfacuta (serialziata)
     * @param sep
     * 		Elementul de separare dintre doua elemente.
     * @return Lista desfacuta sub forma unui sir de caractere.
     */
    public static String serializeList(List<String> list, String sep) {
    	StringBuilder builder = new StringBuilder();
    	int count = list.size();
    	for (int i = 0; i < count - 1; i++) {
    		builder.append(list.get(i)).append(sep);
    	}
    	if (count > 0) {
    		builder.append(list.get(count - 1));
    	}
    	
    	return builder.toString();
    }
    
    /**
     * Impacheteaza o lista dupa un separator specificat ca si parametru.
     * 
     * @param list
     * 		Lista care va fi impachetata (deserialziata)
     * @param sep
     * 		Elementul de separare dintre doua elemente.
     * @return Instanta catre lista impachetata
     */
    public static List<String> deserializeList(String list, String sep) {
    	List<String> result = new ArrayList<String>();
    	String[] tokens = list.split(sep);
    	for (String token : tokens) {
    		result.add(token);
    	}
    	return result;
    }
    
    /**
     * Returneaza data formatata in functie de numarul de milisecunde trecute
     * de la 1 ianuarie 1970.
     * 
     * @param timestamp
     * 			Numarul de millisecunde ce au trecut de la 1 ianuarie 1970.
     * @return
     * 			Data formatata - MM/dd/yyyy
     */
    public static String standardDateFormat(long timestamp) {
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
    	Date date = new Date(timestamp);      
    	return sdf.format(date);
    }

	/**
	 * Metoda folosita pentru salvarea unei perechi cheie - valoare in sistemul
	 * intern de preferinte ale utilizatorului.
	 * 
	 * @param key
	 *            Cheia utilizata pentru asocierea valorii.
	 * @param value
	 *            O valoare intreaga.
	 */
	public static void saveIntPreference(String key, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"user_data", 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * Metoda folosita pentru salvarea unei perechi cheie - valoare in sistemul
	 * intern de preferinte ale utilizatorului.
	 * 
	 * @param key
	 *            Cheia utilizata pentru asocierea valorii.
	 * @param value
	 *            O valoare de tip sir de caractere.
	 */
	public static void saveStringPreference(String key, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"user_data1", 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Obtine valoarea unei preferinte din cadrul sistemului asociata unei chei.
	 * 
	 * @param key
	 *            Cheia dupa care se va face cautarea.
	 * @return Valorea intreaga asociata cheii. Daca nu se gaseste nimic atunci
	 *         se va returna 0.
	 */
	public static int loadIntPreference(String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"user_data", 0);
		return sharedPreferences.getInt(key, 0);
	}

	/**
	 * Obtine valoarea unei preferinte din cadrul sistemului asociata unei chei.
	 * 
	 * @param key
	 *            Cheia dupa care se va face cautarea.
	 * @return Valorea de stip string asociata cheii. Daca nu se gaseste nimic
	 *         atunci se va returna null.
	 */
	public static String loadStringPreference(String param) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"user_data1", 0);
		return sharedPreferences.getString(param, null);
	}
}
