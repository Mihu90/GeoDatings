/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

public class AboutActivity extends ActionBarActivity implements OnClickListener{
   
	private ActionBar actionbar;
	
	private Button btnShare, btnRate;
			
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
     	actionbar = getSupportActionBar();
     	actionbar.setDisplayHomeAsUpEnabled(true);
     	
		btnShare = (Button) findViewById(R.id.btnShare);
		btnRate	= (Button) findViewById(R.id.btnRate);
		
		WebView view = new WebView(this);
	    view.setVerticalScrollBarEnabled(false);

	    ((LinearLayout)findViewById(R.id.linearLayout)).addView(view);

	    view.loadData(getString(R.string.app_description), "text/html", "utf-8");
		
		btnShare.setOnClickListener(this);
		btnRate.setOnClickListener(this);
	    
     	actionbar = getSupportActionBar();
     	actionbar.setDisplayHomeAsUpEnabled(true);
     	        
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	    		// Previous page or exit
	    		finish();
	    		overridePendingTransition (R.anim.open_main, R.anim.close_next);
	    		return true;
	    		
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btnShare:
			Intent iShare = new Intent(Intent.ACTION_SEND);
			iShare.setType("text/plain");
			iShare.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
			iShare.putExtra(Intent.EXTRA_TEXT, "http://geodatings.com");
			startActivity(Intent.createChooser(iShare, getString(R.string.share_via)));
			overridePendingTransition (R.anim.open_next, R.anim.close_main);
			break;
			
		case R.id.btnRate:
			Intent iRate = new Intent(Intent.ACTION_VIEW);
			iRate.setData(Uri.parse("http://geodatings.com"));
			startActivity(iRate);
			overridePendingTransition (R.anim.open_next, R.anim.close_main);
			break;
			
		default:
			break;
		}	
	}	

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition (R.anim.open_main, R.anim.close_next);
	}
}