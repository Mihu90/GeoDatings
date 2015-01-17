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

public class AdapterCategory extends BaseAdapter {
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	private int lastPosition = -1;

	public AdapterCategory(Activity activity, ArrayList<HashMap<String, String>> data) {
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
		private TextView lblNameCategory;
		private ImageView icMarker;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.adapter_category, null);
			holder = new ViewHolder();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// O alta animatie pentru translatie
		Animation animation = AnimationUtils.loadAnimation(activity,
				(position > lastPosition) ? R.anim.up_from_bottom
						: R.anim.down_from_top);
		convertView.startAnimation(animation);
		lastPosition = position;

		holder.lblNameCategory = (TextView) convertView
				.findViewById(R.id.nameCategory);
		holder.icMarker = (ImageView) convertView.findViewById(R.id.icon);

		HashMap<String, String> item = new HashMap<String, String>();
		item = data.get(position);

		holder.lblNameCategory
				.setText(item.get(UserFunctions.KEY_CATEGORY_NAME));

		Picasso.with(activity)
				.load(UserFunctions.URLAdmin
						+ item.get(UserFunctions.KEY_CATEGORY_ICON)).fit()
				.centerCrop().tag(activity).into(holder.icMarker);
		return convertView;
	}
}