/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.loadmore;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class PagingBaseAdapter<T> extends BaseAdapter {
	protected List<T> items;

	public PagingBaseAdapter() {
		this.items = new ArrayList<T>();
	}

	public PagingBaseAdapter(List<T> items) {
		this.items = items;
	}

	public void addMoreItems(List<T> newItems) {
		this.items.addAll(newItems);
		notifyDataSetChanged();
	}

	public void removeAllItems() {
		this.items.clear();
		notifyDataSetChanged();
	}
}
