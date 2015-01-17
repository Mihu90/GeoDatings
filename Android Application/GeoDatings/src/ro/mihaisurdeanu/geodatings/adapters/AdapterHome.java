/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import ro.mihaisurdeanu.geodatings.libraries.UserFunctions;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ro.mihaisurdeanu.geodatings.R;
import com.squareup.picasso.Picasso;

public class AdapterHome extends BaseAdapter {
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	// Ultima pozitie recunoscuta pentru animatie
	private int lastPosition = -1;

	public AdapterHome(Activity activity, ArrayList<HashMap<String, String>> data) {
		this.activity = activity;
		this.data = data;

		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}
	
	static class ViewHolder {
		private ImageView thumbnail, icon;
		private TextView title, dateEnd;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.adapter_home, null);
			holder = new ViewHolder();

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Translatia continutului se va face printr-o animatie
		Animation animation = AnimationUtils.loadAnimation(activity,
				(position > lastPosition) ? R.anim.up_from_bottom
						: R.anim.down_from_top);
		convertView.startAnimation(animation);
		lastPosition = position;

		// Publica informatia gasita in UI
		holder.title = (TextView) convertView.findViewById(R.id.title);
		holder.dateEnd = (TextView) convertView.findViewById(R.id.endDate);

		holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
		holder.icon = (ImageView) convertView.findViewById(R.id.icon);

		HashMap<String, String> item = new HashMap<String, String>();
		item = data.get(position);

		// Seteaza imaginea folosind Picasso
		Picasso.with(activity)
				.load(UserFunctions.URLImage + item.get(UserFunctions.KEY_EVENT_IMAGE)).fit()
				.centerCrop().tag(activity).into(holder.thumbnail);

		// Seteaza un marcaj folosind acelasi utilitar - Picasso
		Picasso.with(activity)
				.load(UserFunctions.URLAdmin + item.get(UserFunctions.KEY_CATEGORY_ICON)).fit()
				.centerCrop().tag(activity).into(holder.icon);

		holder.title.setText(item.get(UserFunctions.KEY_EVENT_TITLE));
		holder.dateEnd.setText("  "+ item.get(UserFunctions.KEY_EVENT_ENDDATE));
		return convertView;
	}
}