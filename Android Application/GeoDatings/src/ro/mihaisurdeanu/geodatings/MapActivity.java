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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ro.mihaisurdeanu.geodatings.dialogs.TwoOptionsDialog;
import ro.mihaisurdeanu.geodatings.utilities.LocationUtils;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ro.mihaisurdeanu.geodatings.R;
import static ro.mihaisurdeanu.geodatings.libraries.UserFunctions.*;

public class MapActivity extends AbstractActivity implements
		OnInfoWindowClickListener, LocationListener, LocationSource,
		OnMapLongClickListener, OnMarkerDragListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private JSONObject json;

	// Se declara obiectele folosite pentru manipularea hartii
	private GoogleMap map;
	private GoogleMapOptions optionsMap = new GoogleMapOptions();
	private OnLocationChangedListener geoLocationListener;
	private LocationManager locationManager;

	private SupportMapFragment fragmentMap;
	private ProgressDialog dialog;

	private int mapType;
	private float mapZoom;

	private LocationRequest locationRequest;

	private GoogleApiClient googleApiClient;

	private double userLatitude;
	private double userLongitude;
	private int lengthData;

	private String eventTitles[];
	private String eventAddress[];
	private double eventLatitudes[];
	private double eventLongitudes[];
	private String eventIds[];
	private String eventIcons[];

	private Marker temporaryMarker;
	private String addressMarker;
	private AddressState addressState = AddressState.READY;
	
	enum AddressState {
		PENDING,
		READY
	}

	public MapActivity() {
		super(R.layout.activity_around_you);
	}

	/**
	 * Metoda ruleaza imediat dupa crearea activitatii.
	 */
	public void afterOnCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Obtine o referinta catre harta ce este vizibila utilizatorului
		fragmentMap = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		// Incarca setarile pe care utilizatorul le-a ales pentru harta
		mapType = Utility.loadIntPreference(getString(R.string.preferences_type));
		mapZoom = Utility.loadIntPreference(getString(R.string.preferences_zoom));

		// Ajusteaza valorile introduse de utilizator
		if (mapZoom == 0) {
			mapZoom = 10;
		}

		// Verifica conexiunea la internet
		if (!Utility.hasInternetConnection()) {
			Toast.makeText(this, getString(R.string.internet_alert),
					Toast.LENGTH_SHORT).show();
		}

		// Apeleaza metoda de setare a hartii.
		// Exista posibiltatea ca harta sa fie deja setata.
		setupMap();

		// Referinta catre obiectul prin care se creaza cereri de obtinere
		// a locatiei curente.
		locationRequest = LocationRequest.create();

		// Intervalul de actualizare este de 5 secunde.
		locationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Acuratetea va fi setata la cele mai inalte standarde.
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Creaza un nou client necesar determinarii locatiei curente, folosind
		// API-ul pus la dispozitie de Google Inc.
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
	}

	/**
	 * Metoda prin care utilizatorul va fi instiintat precum ca ar fi bine
	 * sa activeze serviciile GPS de localizare in vedere obtinerii unor
	 * rezultate mai bune in stabilirea locatiei.
	 */
	private void createGpsDisabledAlert(int FLAG) {
		final int flag = FLAG;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(R.string.notice);

		// Ce mesaj ii va fi transmis utilizatorului?
		builder.setMessage(getString(R.string.direction_alert));

		// Butonul prin care recomandarea se pune in practica.
		builder.setPositiveButton(getString(R.string.settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showGpsOptions(flag);
					}
				});
		// Butonul prin care nu se doreste punerea in aplicare a recomandarii.
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
						overridePendingTransition(R.anim.open_main,
								R.anim.close_next);
					}
				});

		builder.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				finish();
				overridePendingTransition(R.anim.open_main, R.anim.close_next);
			}
		});

		// Produce afisarea casutii de dialog.
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Redirecteaza utilizatorul catre sectiunea de setari - Location.
	 */
	private void showGpsOptions(int result) {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, result);
		overridePendingTransition(R.anim.open_next, R.anim.close_main);
	}

	/**
	 * Prelucreaza rezultatele primite de aceasta activitate dn partea altora
	 * au fost lansate in executie folosind metoda 
	 * {@link #startActivityForResult(Intent, int)}.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Decizia actiunii urmatoare se bazeaza pe codul primit.
		if (requestCode == 
				LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST) {
			// Conexiunea nu s-a putut realiza.
			if (resultCode == Activity.RESULT_OK) {
				Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
				Log.d(LocationUtils.APPTAG, getString(R.string.connected));
				Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
			} else {
				Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
				Log.d(LocationUtils.APPTAG, getString(R.string.disconnected));
				Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
			}
		} else {
			// Raporteaza primirea unui cod neasteptat.
			Log.d(LocationUtils.APPTAG,
				  getString(R.string.unknown_activity_request_code, requestCode));
		}
	}

	/**
	 * Inainte de a folosi serviciile puse la dispozitie de Google Play, va
	 * trebuie sa ne asiguram ca acesta este disponibil pe dispozitivul curent.
	 * 
	 * @return Adevarat daca serviciul este disponibil sau fals daca nu este.
	 */
	private boolean servicesConnected() {
		// Verifica daca serviciul este disponibil
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// Orice alt cod in afara de cel de succes inseamna eroare.
		if (resultCode != ConnectionResult.SUCCESS) {
			// Afiseaza o eroare utilizatorului
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
			}
			return false;
		}
		
		return true;
	}

	/**
	 * Obtine adresa locatiei curente, folosind decodarea acesteia.
	 * Functioneaza doar daca serviciul de geolocatie este activ.
	 * 
	 * @param location
	 * 		Referinta catre locatia curenta.
	 */
	@SuppressLint("NewApi")
	public void getAddress(LatLng location) {	
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& !Geocoder.isPresent()) {
			// Decodificarea nu se poate realiza.
			Toast.makeText(this, R.string.no_geocoder_available,
					Toast.LENGTH_LONG).show();
			return;
		}
		
		// Asteapta pana cand adresa anterioara este calculata
		waitForAddress();

		// Lanseaza task-ul ce se ocupa cu gasirea adresei curente
		new AddressTask(this).execute(location);
	}
	
	/**
	 * Asteapta pana cand adresa anterioara este calculata.
	 */
	private void waitForAddress() {
		while (addressState != AddressState.READY) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO: Jurnalizeaza exceptia!
			}
		}
	}

	/**
	 * Trimite o cerere de pornire a actualizarii locatiei curente.
	 */
	public void startUpdates() {
		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}

	/**
	 * Inregistreaza o cerere de oprire a actualizarii locatiei curente.
	 */
	public void stopUpdates() {
		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}

	/**
	 * Metoda apelata de serviciile de localizare atunci cand cererea de
	 * conectare cu clientul s-a realizat cu succes. Din acest moment, se
	 * pot realiza actualizari periodice.
	 */
	@Override
	public void onConnected(Bundle bundle) {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (gpsIsEnabled) {
			startUpdates();
		} else {
			createGpsDisabledAlert(1);
		}
	}

	/**
	 * Metoda apelata de serviciile de localizare atunci cand conexiunea cu
	 * clientul a cazut datorita unei erori.
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		// Nu se va intampla nimic. Asta e.
	}

	/**
	 * Functie apelata de serviciile de localizare atunci cand incercarea de
	 * conectare esueaza.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Unele dintre erori pot fi rezolvate chiar de serviciul Google Play.
		// Din pacate nu ne putem astepta sa se gaseasca o rezolutie intotdeauna.
		if (connectionResult.hasResolution()) {
			try {
				// Invoca o activitate noua care va incerca sa rezolve eroarea.
				connectionResult.startResolutionForResult(this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				// TODO: Jurnalizeaza exceptia.
			}
		} else {
			// Daca nici dupa aceasta incercare problema a ramas nerezolvata,
			// atunci va trebui sa il notificam pe utilizator.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	/**
	 * Functie necesara pentru a porni actualizarile periodice necesare
	 * determinarii pozitiei geografice.
	 */
	private void startPeriodicUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, locationRequest, this);
	}

	/**
	 * Functie necesara pentru a opri actualizarile periodice necesare
	 * determinarii pozitiei geografice.
	 */
	private void stopPeriodicUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				googleApiClient, this);
	}

	/**
	 * Arata o fereastra de dialog cu codul de eroare produs de Google Play
	 * services atunci cand conectarea nu s-a putut realiza.
	 * 
	 * @param errorCode
	 * 			Codul de eroare obtinut.
	 */
	private void showErrorDialog(int errorCode) {
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// Daca Google Play services nu a putut oferi o fereastra custom de
		// dialog, va trebui sa cream noi una.
		if (errorDialog != null) {
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog(errorDialog);
			errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
		}
	}

	/**
	 * Fragment necesar pentru afisarea unor erori generate in urma unor
	 * actiuni petrecute in diverse momente cheie.
	 */
	private static class ErrorDialogFragment extends DialogFragment {
		/** Camp ce contine o referinta catre instanta ferestrei de dialog */
		private Dialog dialog;

		public ErrorDialogFragment() {
			super();
		}

		public void setDialog(Dialog dialog) {
			this.dialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return dialog;
		}
	}

	/**
	 * Verifica daca Google Maps este deja setat. Daca este atunci nu se mai
	 * intampla nimic, dar daca nu este atunci se va purcede la initializarea
	 * ei.
	 */
	private void setupMap() {
		// O valoare diferita de null inseamna ca setarea a avut loc cu succes.
		if (map == null) {
			map = fragmentMap.getMap();
			if (map != null) {
				setupMapNow();
			}
		}
	}

	/**
	 * Metoda utilitara folosita pentru construirea unei harti Google Maps
	 * folosind API-ul pus la dispozitie de cei de la Google.
	 */
	private void setupMapNow() {
		// Seteaza tipul hartii
		setMapType(mapType);

		// Care vor fi optiunile noii harti?
		optionsMap.compassEnabled(true);
		optionsMap.rotateGesturesEnabled(true);
		optionsMap.tiltGesturesEnabled(true);
		optionsMap.zoomControlsEnabled(true);

		// Se creaza o noua instanta
		SupportMapFragment.newInstance(optionsMap);
		map.setLocationSource(this);
		map.setMyLocationEnabled(true);

		// Seteaza si o serie de ascultatori asupra hartii ce ne vor ajuta
		// sa putem avea mai multe actiuni distincte asupra ei.
		map.setOnMapLongClickListener(this);
		map.setOnMarkerDragListener(this);
		map.setOnInfoWindowClickListener(this);
	}

	/**
	 * Metoda prin care este calculat tipul hartii care se va afisa
	 * utilizatorului. Tipul poate fi schimbat folosind sectiunea de setari a
	 * aplicatiei.
	 */
	private void setMapType(int choice) {
		switch (choice) {
			case 0:
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case 1:
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case 2:
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case 3:
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
		}
	}
	
	@Override
	public void activate(OnLocationChangedListener listener) {
		geoLocationListener = listener;
	}

	@Override
	public void deactivate() {
		geoLocationListener = null;
	}

	/**
	 * Functie de callback apelata atunci cand activitatea devine invizibila. In
	 * acest moment actualiarile se opresc si se produce deconectarea
	 * clientului.
	 */
	@Override
	public void onStop() {
		if (googleApiClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// Imediat dupa executia liniei de mai jos, conexiunea se considera a
		// fi terminata.
		googleApiClient.disconnect();

		super.onStop();
	}

	/**
	 * Functie de callback apelata atunci cand activitatea este restartata.
	 */
	@Override
	public void onStart() {
		super.onStart();
		
		// Se produce incercarea de conectare a clientului.
		googleApiClient.connect();
	}

	/**
	 * Functie de callback apelata atunci cand sistemul detecteaza activitatea
	 * va fiind din nou vizibila utilizatorului.
	 */
	@Override
	public void onResume() {
		super.onResume();
		// Harta va fi resetata
		setupMap();
	}

	/**
	 * Functie ce defineste lista de actiuni ce se petrec atunci cand se da
	 * click pe casuta informativa a unui marker. In principiu, dorim sa obtinem
	 * mai multe informatii despre evenimentul respectiv.
	 */
	@SuppressLint("InflateParams")
	@Override
	public void onInfoWindowClick(Marker marker) {
		// Obtine titlul markerului curent
		String title = marker.getTitle();
		// Cauta markerul in lista de markere interna
		int countEntries = eventIds.length;
		boolean found = false;
		for (int i = 0; i < countEntries; i++) {
			if (eventTitles[i].equals(title)) {
				found = true;
				break;
			}
		}

		// S-a gasit markerul respectiv?
		// Daca nu s-a gasit atunci e cel mai probabil sa avem de-a face cu
		// unul care nu a fost inca adaugat in sistem de utilizator.
		if (!found) {
			// Ii oferim posibilitatea utilizatorului sa adauge un nou
			// eveniment in cadrul sistemului intern.
			Intent intent = new Intent(this, AddEventActivity.class);
			LatLng position = temporaryMarker.getPosition();
			intent.putExtra(Utility.EXTRA_DEST_LAT, position.latitude);
			intent.putExtra(Utility.EXTRA_DEST_LNG, position.longitude);
			waitForAddress();
			intent.putExtra(Utility.EXTRA_ADDRESS, addressMarker);
			startActivity(intent);
			overridePendingTransition(R.anim.open_next, R.anim.close_main);
		}
	}

	/**
	 * Ce se intampla atunci cand utilizatorul va tine apasat mai mult timp pe o
	 * pozitie de pe harta? Raspunsul e foarte simplu. Doreste sa adauge un nou
	 * marker pe harta.
	 */
	@Override
	public void onMapLongClick(final LatLng position) {
		// Un utilizator nu poate adauga decat un singur marker la un moment
		// dat.
		if (temporaryMarker == null) {
			addTemporaryMarker(position);
		} else {
			// Utilizatorul va trebui sa confirme daca doreste stergerea
			// vechiului marcaj temporar si inlocuirea cu un altul.
			TwoOptionsDialog dialog = new TwoOptionsDialog(this);
			dialog.setTitle(R.string.confirm);
			dialog.setMessage(R.string.map_temp_marker_delmsg);
			dialog.setOptionNames(R.string.yes, R.string.no);
			dialog.display(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// S-a confirmat actiunea?
					if (DialogInterface.BUTTON_POSITIVE == which) {
						// Daca da, atunci vom sterge vechiul marcaj.
						if (temporaryMarker != null) {
							temporaryMarker.remove();
						}
						// Si vom adauga un altul.
						addTemporaryMarker(position);
					}
				}
			});
		}
	}
	
	/**
	 * Metoda utilitara prin care se adauga un nou marcaj pe harta.
	 * 
	 * @param position
	 * 			Unde se va adauga marcajul?
	 */
	private void addTemporaryMarker(LatLng position) {
		// Acest marker este unul special pentru ca poate fi mutat. :)
		temporaryMarker = map.addMarker(new MarkerOptions()
				.position(position)
				.title(getString(R.string.map_temp_marker_title))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_action_pin))
				.draggable(true));
		temporaryMarker.setSnippet(String.format(getString(R.string.map_lat_lng_info),
												 position.latitude,
												 position.longitude));
		getAddress(position);
	}

	/**
	 * La fiecare schimbare a locatiei utilizatorului se va apela aceasta
	 * metoda.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (geoLocationListener != null) {
			// Notificam ascultatorii de schimbarea pozitiei.
			geoLocationListener.onLocationChanged(location);

			// Obtine locatia curenta a dispozitivului
			userLatitude = (double) location.getLatitude();
			userLongitude = (double) location.getLongitude();

			// Camera va trebui sa se indrepte catre locatia determinata.
			map.setMyLocationEnabled(true);
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.getLatitude(), location.getLongitude()), mapZoom));

			// Actualizarile periodice vor fi oprite.
			stopPeriodicUpdates();

			// In acest moment toate marcajele deja existente vor disparea.
			map.clear();
			temporaryMarker = null;

			// Task-ul de calculare a noii pozitii va fi planificat spre
			// executie.
			new LocationTask().execute();
		}
	}
	
	/**
	 * Task asincron ce se ocupa cu stabilirea geolocatiei curente.
	 */
	private class LocationTask extends AsyncTask<Void, Void, Void> {
		/**
		 * Ce se intampla inainte de executia propriu-zisa a sarcinii?
		 */
		@Override
		protected void onPreExecute() {
			// O ferestra de dialog va aparea pentru a ne notifica de procesul
			// de incarcare.
			dialog = ProgressDialog.show(MapActivity.this, "",
					getString(R.string.loading_data), true);
		}

		/**
		 * Actiunile ce compun sarcina curenta.
		 */
		@Override
		protected Void doInBackground(Void... params) {
			// Se obtin informatiile de la serverul principal.
			getDataFromServer();
			return null;
		}

		/**
		 * Ce se intampla dupa executia sarcinii?
		 */
		@Override
		protected void onPostExecute(Void result) {
			// Serverul a returnat ceva?
			if (json != null) {
				new LoadServerMarkers().execute();
			} else {
				Toast.makeText(MapActivity.this,
						getString(R.string.no_connection), Toast.LENGTH_SHORT)
						.show();
				dialog.dismiss();
			}
		}
	}
	
	/**
	 * Metoda utilitara prin care se obtin mai multe evenimente prin interogarea
	 * serverului.
	 */
	private void getDataFromServer() {
		try {
			json = getEventsAround(userLatitude, userLongitude);
			if (json != null) {
				JSONArray eventsArray = json.getJSONArray(array_events_around);
				lengthData = eventsArray.length();
				eventIds = new String[lengthData];
				eventTitles = new String[lengthData];
				eventAddress = new String[lengthData];
				eventLatitudes = new double[lengthData];
				eventLongitudes = new double[lengthData];
				eventIcons = new String[lengthData];

				for (int i = 0; i < lengthData; i++) {
					JSONObject eventObject = eventsArray.getJSONObject(i);
					eventIds[i] = eventObject.getString(KEY_EVENT_ID);
					eventTitles[i] = eventObject.getString(KEY_EVENT_TITLE);
					eventAddress[i] = eventObject.getString(KEY_EVENT_ADDRESS);
					eventLatitudes[i] = eventObject.getDouble(KEY_EVENT_LATITUDE);
					eventLongitudes[i] = eventObject.getDouble(KEY_EVENT_LONGITUDE);
					eventIcons[i] = eventObject.getString(KEY_CATEGORY_ICON);
				}
			}
		} catch (JSONException e) {
			// TODO: Jurnalizeaza cumva exceptia.
		}
	}
	
	/**
	 * Pentru fiecare eveniment obtinut prin interogarea serverului se va crea
	 * un marcaj pe harta. Acesta este de fapt metoda prin care evenimentele vor
	 * putea fi vazute de utilizatori.
	 */
	private class LoadServerMarkers extends AsyncTask<Void, Void, Void> {
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();

		/**
		 * Ce se intampla inainte de executia propriu-zisa a sarcinii?
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setMessage(getString(R.string.getting_marker));
		}

		/**
		 * Actiunile ce compun sarcina curenta.
		 */
		@Override
		protected Void doInBackground(Void... params) {
			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = Bitmap.createBitmap(80, 80, conf);
			Canvas canvas = new Canvas(bitmap);

			Paint color = new Paint();
			color.setTextSize(35);
			color.setColor(Color.BLACK);

			for (int i = 0; i < lengthData; i++) {
				try {
					URL url = new URL(URLAdmin + eventIcons[i]);
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setDoInput(true);
					connection.connect();
					InputStream is = connection.getInputStream();
					bitmaps.add(BitmapFactory.decodeStream(is));

					canvas.drawBitmap(BitmapFactory.decodeResource(
							getResources(), R.drawable.ic_launcher), 0, 0,
							color);
					//canvas.drawText("User Name!", 30, 40, color);
				} catch (MalformedURLException e) {
					// TODO: Jurnalizeaza exceptia!
				} catch (IOException e) {
					// TODO: Jurnalizeaza exceptia!
				}
			}

			return null;
		}

		/**
		 * Ce se intampla dupa executia sarcinii?
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Va trebui sa adaugam fizic toate marcajele
			for (int i = 0; i < lengthData; i++) {
				// Se adauga un marcaj pe harta
				map.addMarker(new MarkerOptions()
						.position(new LatLng(eventLatitudes[i], eventLongitudes[i]))
						.icon(BitmapDescriptorFactory.fromBitmap(bitmaps.get(i)))
						.anchor(0.5f, 1)
						.title(eventTitles[i])
						.snippet(eventAddress[i]));
			}

			// Procesul de obtinere si adaugare a marcajelor s-a incheiat.
			dialog.dismiss();
		}
	}

	private class AddressTask extends AsyncTask<LatLng, Void, String> {
		/** Contextul curent */
		Context context;

		public AddressTask(Context context) {
			super();
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			// Am inceput procesul de calculare a unei posibile adrese
			addressState = AddressState.PENDING;
		}

		/**
		 * get a geocoding service instance, pass latitude and longitude to it,
		 * format the returned address, and return the address to the UI thread.
		 */
		@Override
		protected String doInBackground(LatLng... params) {
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());

			// Obtine locatia curenta folosind datele de intrare
			LatLng location = params[0];

			// Creaza o lista unde vor fi pus informatiile obtinute
			List<Address> addresses = null;

			try {
				// Se apeleaza in mod sincron metoda prin care se obtine
				// adresa din coordonatele geografice.
				addresses = geocoder.getFromLocation(location.latitude,
						location.longitude, 1);

			} catch (IOException e1) {
				// TODO: Jurnalizeaza exceptia!

				// Intoarce si un mesaj de eroare.
				return (getString(R.string.cannot_get_address));
			} catch (IllegalArgumentException e2) {

				// Un alt mesaj de eroare se afiseaza la argumente invalide
				return getString(R.string.illegal_argument_exception,
						location.latitude, location.longitude);
			}
			// Avem o adresa returnata?
			if (addresses != null && addresses.size() > 0) {
				// Din lista de adrese se va alege prima
				Address address = addresses.get(0);
				// Se formateaza adresa
				String addressText = getString(
						R.string.address_output_string,
						// Avem si o strada?
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",
						// Oras
						address.getLocality(),
						// Tara
						address.getCountryName());
				return addressText;				
			}
			// Ce se intampla daca nu exista nicio adresa...
			else {
				return getString(R.string.no_address_found);
			}
		}

		/**
		 * Metoda este apelata o data cu incheierea activitatii doInBackground().
		 * Acesta adresa va fi utilizata pentru adaugarea unor noi evenimente.
		 * De asemenea, important de mentinut este faptul ca ruleaza in firul
		 * de executie de UI.
		 */
		@Override
		protected void onPostExecute(String address) {
			addressMarker = address;
			// Am terminat procesul de determinare a unei posibile adrese
			addressState = AddressState.READY;
		}
	}
	
	@Override
	public void onMarkerDragStart(Marker marker) {
		// Nu se intampla nimic.
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// Cat timp un marker este in proces de mutare nu ne intereseaza.
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// Cand procesul de translatie s-a terminat va trebui sa actualizam
		// pozitia curenta a acestuia.
		LatLng location = marker.getPosition();
		marker.setSnippet(String.format(getString(R.string.map_lat_lng_info),
										location.latitude,
										location.longitude));
		// Va trebui sa recalculam adresa pentru pozitia respectiva
		getAddress(location);
	}
}