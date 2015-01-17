/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.adapters;

import ro.mihaisurdeanu.geodatings.fragments.FragmentNavigationDrawer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ro.mihaisurdeanu.geodatings.R;

public class AdapterMenu extends BaseAdapter {
	private Activity activity;

	public AdapterMenu(Activity activity) {
		this.activity = activity;
	}

	public int getCount() {
		return FragmentNavigationDrawer.listMenu.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}
	
	static class ViewHolder {
		TextView category;
		ImageView menuItem;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_menu, null);
			holder = new ViewHolder();

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.category = (TextView) convertView.findViewById(R.id.category);
		holder.menuItem = (ImageView) convertView.findViewById(R.id.image);
		holder.category.setText(FragmentNavigationDrawer.listMenu[position]);
		holder.menuItem.setBackgroundResource(FragmentNavigationDrawer.imageMenu[position]);
		return convertView;
	}
}