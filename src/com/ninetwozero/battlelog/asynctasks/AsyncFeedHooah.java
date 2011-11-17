/*
	This file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
*/   

package com.ninetwozero.battlelog.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ninetwozero.battlelog.ProfileView.AsyncFeedRefresh;
import com.ninetwozero.battlelog.misc.WebsiteHandler;


public class AsyncFeedHooah extends AsyncTask<String, Integer, Boolean> {

	//Attribute
	Context context;
	long postId;
	boolean fromWidget;
	String httpContent;
	AsyncFeedRefresh asyncFeedRefresh;
	
	//Constructor
	public AsyncFeedHooah( Context c, long pId, boolean w, AsyncFeedRefresh acr ) { 
		
		this.context = c; 
		this.postId = pId;
		this.fromWidget = w;
		this.asyncFeedRefresh = acr;
	
	}
	
	@Override
	protected void onPreExecute() {}
	
	@Override
	protected Boolean doInBackground( String... arg0 ) {
		
		try {
		
    		//Did we manage?
			return WebsiteHandler.doHooahInFeed( postId, arg0[0] );
    		
		} catch( Exception ex ) {
			
			ex.printStackTrace();
			return false;
			
		}
		
	}
	
	@Override
	protected void onPostExecute(Boolean results) {

		if( !fromWidget ) {
			
			//Reload
			asyncFeedRefresh.execute();
			
			if( results ) Toast.makeText( context, "Hooah!", Toast.LENGTH_SHORT).show();
			else Toast.makeText( context, "Hooah failed!", Toast.LENGTH_SHORT).show();
			
		}
		return;
		
	}

	
	
}
