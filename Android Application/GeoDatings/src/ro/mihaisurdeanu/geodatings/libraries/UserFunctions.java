/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.libraries;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class UserFunctions {

	// ID-ul proiectului Google
    public static final String SENDER_ID = "892554024054";
    
	// Detalii despre serverul mama
	private static final String SERVER = "http://geodatings.com/";
	private static final String SERVER_MAIN = "beta/";
	private static final String SERVER_API  = "api/";
	
	// Adrese web
	private static final String URLApi  = SERVER + SERVER_MAIN + SERVER_API; 
	public static final String URLAdmin = SERVER + SERVER_MAIN;
	public static final String URLImage = SERVER + SERVER_MAIN + "upload/images/";
	
	// Lista de servicii pe care serverul le pune la dispozitie.
	private static final String SERVICE_LATEST_EVENTS = "api_latest_events";
	private static final String SERVICE_GCM_REGISTER = "api_register_id";
	private static final String SERVICE_GET_CATEGORIES = "api_get_categories";
	private static final String SERVICE_BY_CATEGORY = "api_get_by_category";
	private static final String SERVICE_EVENTS_SEARCH = "api_events_search";	
	private static final String SERVICE_EVENTS_AROUND = "api_events_around";
	private static final String SERVICE_EVENTS_ADD = "api_events_add";
	private static final String SERVICE_UPLOAD_IMAGE = "upload_image";
	
	// Parametri trimisi la interogari
	private static final String PARAM_START_INDEX = "start_index";
	private static final String PARAM_PER_PAGE = "per_page";
	private static final String PARAM_TITLE = "title";
	private static final String PARAM_ADDRESS = "address";
	private static final String PARAM_CATEGORY_ID = "category_id";
	private static final String PARAM_START_DATE = "start";
	private static final String PARAM_END_DATE = "end";
	private static final String PARAM_IMAGE_NAME = "image";
	private static final String PARAM_LATITUDE_REFERENCE = "latitude_ref";
	private static final String PARAM_LONGITUDE_REFERENCE = "longitude_ref";	
	private static final String PARAM_KEYWORD = "keyword";
	private static final String PARAM_REGISTER_ID = "registerID";
	private static final String PARAM_DEVICE_ID = "deviceID";
	
	// Chei folosite pentru procesarea datelor obtinute de la server
	public static final String KEY_EVENT_ID	= "eventId";
	public static final String KEY_EVENT_TITLE = "title";
	public static final String KEY_EVENT_STARTDATE = "startDate";
	public static final String KEY_EVENT_ENDDATE = "endDate";
	public static final String KEY_EVENT_IMAGE = "image";
	public static final String KEY_EVENT_ADDRESS = "address";
	public static final String KEY_EVENT_LATITUDE = "latitude";
	public static final String KEY_EVENT_LONGITUDE = "longitude";
	public static final String KEY_CATEGORY_ID = "id";
	public static final String KEY_CATEGORY_NAME = "name";
	public static final String KEY_CATEGORY_ICON = "icon";
	public static final String KEY_STATUS = "status";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_COUNT_ENTRIES = "count";
	
	
	public static final String array_latest_deals = "latestDeals";
	public static final String array_category_list = "categories";
	public static final String array_deal_detail 	   = "dealDetail";
	public static final String array_events_search = "searchEvents";
	public static final String array_events_around = "eventsAround";	
	public static final String array_deal_by_category = "dealByCategory";
	public static final String array_register_id = "registerID";
	
	public static final int ITEMS_PER_PAGE = 5;
		
	public static JSONObject gcmRegisterId(String registerId, String deviceId) {
		return getJSONObject("%s?%s=%s&%s=%s",
							 URLApi + SERVICE_GCM_REGISTER,
							 PARAM_REGISTER_ID, registerId,
							 PARAM_DEVICE_ID, deviceId);
	}
	
	public static JSONObject getCategories() {
		return getJSONObject("%s?", URLApi + SERVICE_GET_CATEGORIES);
	}
	
	public static JSONObject getLatestEvents(int valueStartIndex) {	
		return getJSONObject("%s?%s=%d&%s=%d",
							 URLApi + SERVICE_LATEST_EVENTS,
							 PARAM_START_INDEX, valueStartIndex,
							 PARAM_PER_PAGE, ITEMS_PER_PAGE);
	}
	
	public static JSONObject getEventsAround(double latitude,
											 double longitude) {
		return getJSONObject("%s?%s=%f&%s=%f",
							 URLApi + SERVICE_EVENTS_AROUND,
							 PARAM_LATITUDE_REFERENCE, latitude,
							 PARAM_LONGITUDE_REFERENCE, longitude);
	}
	
	public static JSONObject searchForEvents(String keyword, int startIndex, int perPage) {
		return getJSONObject("%s?%s=%s&%s=%d&%s=%d",
				 			 URLApi + SERVICE_EVENTS_SEARCH,
				 			 PARAM_KEYWORD, keyword,
				 			 PARAM_START_INDEX, startIndex,
				 			 PARAM_PER_PAGE, perPage);
	}
	
	public static JSONObject addEvent(String title, String address, int category,
									  double latitude, double longitude,
									  Date start, Date end, String image)
											  throws UnsupportedEncodingException {
		return getJSONObject("%s?%s=%s&%s=%s&%s=%d&%s=%d&%s=%d&%s=%f&%s=%f&%s=%s",
	 			 			 URLApi + SERVICE_EVENTS_ADD,
	 			 			 PARAM_TITLE, URLEncoder.encode(title, "UTF-8"),
	 			 			 PARAM_ADDRESS, URLEncoder.encode(address, "UTF-8"),
	 			 			 PARAM_CATEGORY_ID, category,
	 			 			 PARAM_START_DATE, start.getTime(),
	 			 			 PARAM_END_DATE, end.getTime(),
	 			 			 PARAM_LATITUDE_REFERENCE, latitude,
	 			 			 PARAM_LONGITUDE_REFERENCE, longitude,
	 			 			 PARAM_IMAGE_NAME, URLEncoder.encode(image, "UTF-8"));
	}
	
	public static JSONObject byCategoryEvent(String categoryId, int startIndex) {
		return getJSONObject("%s?%s=%d&%s=%d&%s=%d",
							 URLApi + SERVICE_BY_CATEGORY,
							 PARAM_CATEGORY_ID, categoryId,
							 PARAM_START_INDEX, startIndex,
							 PARAM_PER_PAGE, ITEMS_PER_PAGE);
	}
	
	public static Response sendImageToServer(File image, Bitmap.CompressFormat type) {
		// Creaza un bitmap pe baza imaginii dorite
		Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
		// Transcrie bitmap-ul creat.
		ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(type, 90, output);
        // Obtine bitii din care e compus bitmap-ul initial
        byte[] bytes = output.toByteArray();
        // Imaginea va fi codificata sub forma unui sir de biti
        // ce este la randul sau encodat base64
        String img = Base64.encodeToString(bytes, Base64.DEFAULT);
        
        // Pregateste perechile cheie - valoare ce vor fi trimise
        // in urma unei cereri de tip POST.
        ArrayList<NameValuePair> httpPairs = new  ArrayList<NameValuePair>();
        httpPairs.add(new BasicNameValuePair("imageContent", img));
        BasicNameValuePair imgType = new BasicNameValuePair("imageType", "jpeg");
        if (Bitmap.CompressFormat.PNG == type) {
        	imgType = new BasicNameValuePair("imageType", "png");
        }
        httpPairs.add(imgType);
        
        Response r = new Response(false, "");
        try {
        	HttpClient httpclient = new DefaultHttpClient();
        	HttpPost httppost = new HttpPost(URLApi + SERVICE_UPLOAD_IMAGE);
        	httppost.setEntity(new UrlEncodedFormEntity(httpPairs));
        	HttpResponse response = httpclient.execute(httppost);
        	String responseString = obtainServerResponse(response);
        	if (responseString.startsWith("ok:")) {
        		r.success = true;	
        	}
        	int index = responseString.indexOf(":");
        	r.message = responseString.substring(index + 1);
        } catch (Exception e) {
        	r.message = e.getMessage();
        }
        
        return r;
	}
	
	private static String obtainServerResponse(HttpResponse response)
			throws IllegalStateException, IOException {
		String result = "";
		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = response.getEntity().getContent();
        
		int contentLength = (int) response.getEntity().getContentLength();
        if (contentLength > 0) {
        	try {
        		byte[] data = new byte[512];
        		int length = 0;
            	   
        		while ((length = inputStream.read(data)) != -1) {
        			buffer.append(new String(data, 0, length));
        		}
        	} catch (IOException e) {
        		// TODO: Jurnalizeaza exceptia!
            } finally {
            	try {
            		inputStream.close();
            	} catch (Exception e) {}
            }
        	result = buffer.toString();
        }
        
        return result;
	}
	
	private static JSONObject getJSONObject(String format, Object... params) {
		String url = String.format(format, params);
		return JSONParser.getJSONFromUrl(url);
	}
}