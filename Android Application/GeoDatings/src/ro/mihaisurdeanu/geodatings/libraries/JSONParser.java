/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.libraries;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

public class JSONParser {

	public static JSONObject getJSONFromUrl(String url) {
		JSONObject reference = null;
		try {
	        HttpClient client = new DefaultHttpClient();
	        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
			// Realizeaza o cerere de tip GET catre server pentru a obtine
			// informatiile afisate sub format JSON.
	        HttpUriRequest request = new HttpGet(url);
	        HttpResponse response = client.execute(request);
	        // Parseaza si interpreteaza rezultatul
	        InputStream inputStream = response.getEntity().getContent();
	        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
	        String line;
	        StringBuilder jsonString = new StringBuilder();
            while ((line = in.readLine()) != null){
            	jsonString.append(line);
            }
            in.close();
            reference = new JSONObject(jsonString.toString());     
        } catch (Exception e) {
            // TODO: Jurnalizeaza exceptia!
        	e.printStackTrace();
        }

		// Returneaza referinta si rezultatul obtinut
		return reference;
	}
}
