/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONObject;

import ro.mihaisurdeanu.geodatings.libraries.Response;
import ro.mihaisurdeanu.geodatings.utilities.Utility;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ngohung.form.HBaseFormActivity;
import com.ngohung.form.el.HDatePickerElement;
import com.ngohung.form.el.HElement;
import com.ngohung.form.el.HPickerElement;
import com.ngohung.form.el.HRootElement;
import com.ngohung.form.el.HSection;
import com.ngohung.form.el.HTextEntryElement;
import com.ngohung.form.el.listener.HValidatorListener;
import com.ngohung.form.el.validator.HEmailValidatorListener;
import com.ngohung.form.el.validator.ValidationStatus;

import static ro.mihaisurdeanu.geodatings.libraries.UserFunctions.*;

public class AddEventActivity extends HBaseFormActivity 
		implements HValidatorListener, OnClickListener {
	// Elementele ce alcatuiesc formularul de adaugare a unui
	// nou eveniment.
	private HTextEntryElement title, address, email, phone, image, latitude, longitude;
	private HDatePickerElement startDate, endDate;
	private HPickerElement category;
	
	private ArrayList<String> str = new ArrayList<String>();
	private Item[] fileList;
	private File path = new File(Environment.getExternalStorageDirectory() + "");
	private String chosenFile;
	private Boolean firstLvl = true;
	private ListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.context = this;
		
		setInstructions(getString(R.string.addevent_instructions));
		
		ActionBar actionbar = getSupportActionBar();
     	actionbar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Adauga butonul de trimitere a formularului in coltul dreapta-sus
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar_addevent, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected HRootElement createRootElement() {
		Intent intent = getIntent();
		
		// Vom avea mai multe sectiuni in cadrul acestui formular
		List<HSection> sections = new ArrayList<HSection>();
		
		// Prima sectiune contine informatii generale despre eveniment
		HSection mainSection = new HSection(getString(R.string.addevent_section_main));
		// Vom avea la inceput un EditText pentru titlu
		title = new HTextEntryElement("title",
				getString(R.string.addevent_title),
				getString(R.string.addevent_title_hint),
				true);
		address = new HTextEntryElement("address",
				getString(R.string.addevent_address),
				getString(R.string.addevent_address_hint),
				true);
		address.setValue(intent.getStringExtra(Utility.EXTRA_ADDRESS));
		category = new HPickerElement("category",
				getString(R.string.addevent_category),
				getString(R.string.addevent_category_hint),
				true,
				new String[] {"Option1", "Option2"});
		latitude = new HTextEntryElement("latitude",
				getString(R.string.addevent_latitude),
				getString(R.string.addevent_latitude_hint),
				true);
		latitude.setValue(String.format("%.6f", intent.getDoubleExtra(Utility.EXTRA_DEST_LAT, 0.0d)));
		longitude = new HTextEntryElement("longitude",
				getString(R.string.addevent_longitude),
				getString(R.string.addevent_longitude_hint),
				true);
		longitude.setValue(String.format("%.6f", intent.getDoubleExtra(Utility.EXTRA_DEST_LNG, 0.0d)));
		mainSection.addEl(title);
		mainSection.addEl(address);
		mainSection.addEl(category);
		mainSection.addEl(latitude);
		mainSection.addEl(longitude);
		
		// Urmatoarea sectiune va cuprinde informatii cu privire la data
		// de la care anuntul va deveni activ, pana la data in care el
		// nu va mai fi activ.
		HSection dateSection = new HSection(getString(R.string.addevent_section_date));
		startDate = new HDatePickerElement("startDate",
				getString(R.string.addevent_start),
				getString(R.string.addevent_start_hint),
				true);
		startDate.addValidator(this);
		endDate = new HDatePickerElement("endDate",
				getString(R.string.addevent_end),
				getString(R.string.addevent_end_hint),
				true);
		endDate.addValidator(this);
		// Vom seta si doua date pentru elementele de mai sus.
		// Data de pornire va fi chiar ziua de astazi, pe cand,
		// data de terminare va fi ziua de maine.
		Calendar calendar = Calendar.getInstance();
		startDate.setDateValue(calendar.getTime());
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		endDate.setDateValue(calendar.getTime());
		
		dateSection.addEl(startDate);
		dateSection.addEl(endDate);
		
		// In fine, ultima sectiune este cea cu resurse :)
		HSection resourceSection = new HSection(getString(R.string.addevent_section_resource));
		email = new HTextEntryElement("email",
				getString(R.string.addevent_email),
				getString(R.string.addevent_email_hint),
				false);
		email.addValidator(new HEmailValidatorListener());
		phone = new HTextEntryElement("phone",
				getString(R.string.addevent_phone),
				getString(R.string.addevent_phone_hint),
				false);
		phone.addValidator(this);
		image = new HTextEntryElement("image",
				getString(R.string.addevent_image),
				getString(R.string.addevent_image_hint),
				true);
		image.setReadonly(true);
		image.setOnClickListener(this);
		
		resourceSection.addEl(email);
		resourceSection.addEl(phone);
		resourceSection.addEl(image);
		
		// Adaugam toate sectiunile
		sections.add(mainSection);
		sections.add(dateSection);
		sections.add(resourceSection);
		// Cream nodul principal - root
		return new HRootElement(getString(R.string.addevent_form), sections);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {
		loadFileList();
     	showDialog(1);
	}
	
	@Override
	public ValidationStatus isValid(HElement element) {
		String key = element.getKey();
		if ("startDate".equals(key) || "endDate".equals(key)) {
			return new ValidationStatus(startDate.getDateValue().compareTo(endDate.getDateValue()) < 0,
										getString(R.string.addevent_date_error));
		} else if ("phone".equals(key)) {
			String phoneNumber = element.getValue();
			Pattern pattern = Pattern.compile("\\d{3}-\\d{7}");
		    return new ValidationStatus(pattern.matcher(phoneNumber).matches(),
		    		getString(R.string.addevent_phone_error));
		}
		
		return null;
	}
	
	/**
	 * Incarca lista de fisiere / directoare care se pot gasi sub path-ul
	 * specificat ca data membru a acestei clase.
	 * Sunt afisate numai imagini, adica fisiere PNG sau JPG/JPEG.
	 */
	private void loadFileList() {
		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fName) {
					File f = new File(dir, fName);
					fName = fName.toLowerCase(Locale.ENGLISH);
					return !f.isHidden() && (f.isDirectory() || (f.isFile() && 
							(fName.endsWith(".png") || fName.endsWith(".jpg") || fName.endsWith(".jpeg"))));
				}
			};
			String[] fList = path.list(filter);
			fileList = new Item[fList.length];
			for (int i = 0; i < fList.length; i++) {
				fileList[i] = new Item(fList[i], R.drawable.file_icon);
				File sel = new File(path, fList[i]);
				if (sel.isDirectory()) {
					fileList[i].icon = R.drawable.directory_icon;
				}
			}
			if (!firstLvl) {
				Item temp[] = new Item[fileList.length + 1];
				for (int i = 0; i < fileList.length; i++) {
					temp[i + 1] = fileList[i];
				}
				temp[0] = new Item("Up", R.drawable.directory_up);
				fileList = temp;
			}
		}
		adapter = new ArrayAdapter<Item>(this, android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
				textView.setCompoundDrawablesWithIntrinsicBounds(fileList[position].icon, 0, 0, 0);
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);
				return view;
			}
		};
	}
	
	private class Item {
		public String file;
		public int icon;
		
		public Item(String file, Integer icon) {
			this.file = file;
			this.icon = icon;
		}
	
		@Override
		public String toString() {
			return file;
		}
	}

	/**
	 * Cod preluat de la adresa: https://github.com/mburman/Android-File-Explore
	 */
	@Override
	protected Dialog onCreateDialog(int id) {	
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose your file");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chosenFile = fileList[which].file;
				File sel = new File(path + "/" + chosenFile);
				if (sel.isDirectory()) {
					firstLvl = false;
					str.add(chosenFile);
					fileList = null;
					path = new File(sel + "");
					loadFileList();
					
					removeDialog(1);
					showDialog(1);
				} else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {
					String s = str.remove(str.size() - 1);
					path = new File(path.toString().substring(0,
							path.toString().lastIndexOf(s)));
					fileList = null;
					if (str.isEmpty()) {
						firstLvl = true;
					}
					loadFileList();
					
					removeDialog(1);
					showDialog(1);
				} else {
					image.setValue(chosenFile);
				}
			}
		});

		return builder.show();
	}
	
	/**
	 * Task asincron ce se ocupa cu adaugarea unui nou eveniment,
	 * prin interogarea serverului.
	 */
	private class AddEventTask extends AsyncTask<Void, Void, String> {
		private ProgressDialog dialog;
		
		/**
		 * Ce se intampla inainte de executia propriu-zisa a sarcinii?
		 */
		@Override
		protected void onPreExecute() {
			// O ferestra de dialog va aparea pentru a ne notifica de procesul
			// de trimitere a informatiilor catre server.
			dialog = ProgressDialog.show(Utility.context, "",
					getString(R.string.sending_data), true);
		}

		/**
		 * Actiunile ce compun sarcina curenta.
		 */
		@Override
		protected String doInBackground(Void... params) {
			String lowerName = image.getValue().toLowerCase(Locale.ENGLISH);
			File imgFile = new File(path, image.getValue());
			if (imgFile.exists()) {
				// Descopera formatul imaginii ce se va trimite
				Bitmap.CompressFormat type = Bitmap.CompressFormat.JPEG;
				if (lowerName.endsWith("png")) {
					type = Bitmap.CompressFormat.PNG;
				}
				// Trimite imaginea si asteapta raspunsul...
				Response response = sendImageToServer(imgFile, type);
				
				// Daca raspunsul este null, atunci nu a aparut nicio eroare
				// si se poate continua cu pasul urmator
				if (response.success) {
					// Pasul urmator este sa inregistram evenimentul propriu-zis
					JSONObject json = null;
					try {
						json = addEvent(title.getValue(), address.getValue(), 0,
								Double.parseDouble(latitude.getValue()), Double.parseDouble(longitude.getValue()),
								startDate.getDateValue(), endDate.getDateValue(), response.message);
					} catch (Exception e) {
						// TODO: Jurnalizeaza exceptia!
					}
						
					int status = 0;
					String error = "";
					if (json != null) {
						try {
							status = json.getInt(KEY_STATUS);
							if (status != 1) {
								error = json.getString(KEY_MESSAGE);
							}
						} catch (Exception e) { }
					}
					if (status == 1) {
						return Utility.context.getString(R.string.addevent_added_successfully);
					} else {
						return error;
					}
				} else {
					return response.message;
				}
			} else {
				return Utility.context.getString(R.string.addevent_file_notfound);
			}
		}

		/**
		 * Ce se intampla dupa executia sarcinii?
		 */
		@Override
		protected void onPostExecute(String message) {
			dialog.dismiss();
			Toast.makeText(Utility.context, message, Toast.LENGTH_SHORT).show();	
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.open_main, R.anim.close_next);
			return true;
		} else if (itemId == R.id.addEvent) {
			// Verificam formularul sa nu contina nicio eroare.
			if (checkFormData()) {
				// Daca nu contine nicio eroare putem incepe procesul
				// de creare a unei noi intrari in sistem.
				new AddEventTask().execute();
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.open_main, R.anim.close_next);
	}
}
