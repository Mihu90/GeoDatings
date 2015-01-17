/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.R;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;

public class SplashActivity extends Activity {
	/** Bara de progres pe care utilizatorul o va vedea. */
	private ProgressBar progressBar;
	/** Procentul de incarcare a aplicatiei. */
	private int progress = 0;

	/**
	 * Metoda este apelata atunci cand activitatea este creata pentru prima oara.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ActionBar-ul este ascuns cat timp aceasta activitate este afisata.
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    getActionBar().hide();
	    
		// Este setat un ContentView specificat prin intermediul unui XML
		setContentView(R.layout.activity_splash);

		// Bara de progres va fi initial pe 0.
		progressBar = (ProgressBar)findViewById(R.id.prgLoading);
		progressBar.setProgress(progress);

    	// Procesul de incarcare se va face pe un alt fir de executie.
		new LoadingTask().execute();
	}
	
	public class LoadingTask extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute() { }
    	
		@Override
		protected Void doInBackground(Void... params) {
			// Simuleaza procesul de incarcare al aplicatiei.
			while (progress < 100) {
				try {
					Thread.sleep(500);
					progress += 25;
					progressBar.setProgress(progress);
				} catch (InterruptedException e) {
					// TODO: Exceptiile trebuie logate!
					e.printStackTrace();
				}
			}
			// Rezultatul procesului de incarcare nu ne intereseaza inca
			return null;
		}
    	
		/**
		 * Metoda de tip callback ce se executa imediat dupa finalizarea procesului
		 * de incarcare a aplicatiei. In principiu, dupa incarcarea aplicatiei,
		 * se va ceda executia catre activitatea Home.
		 */
		@Override
		protected void onPostExecute(Void result) {
			Intent i = new Intent(SplashActivity.this, HomeActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.open_next, R.anim.close_main);
		}
    }
}
