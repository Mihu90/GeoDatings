/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings.libraries;

public class Response {
	public boolean success;
	public String message;
	
	public Response(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
}
