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

package com.ninetwozero.battlelog.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.text.TextUtils;
import android.util.Log;

import com.coveragemapper.android.Map.ExternalCacheDirectory;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.PersonaStats;
import com.ninetwozero.battlelog.datatypes.PlatoonData;
import com.ninetwozero.battlelog.datatypes.PlatoonInformation;
import com.ninetwozero.battlelog.datatypes.ProfileInformation;


/* 
 * Methods of this class should be loaded in AsyncTasks, as they would probably lock up the GUI
 */

public class CacheHandler {
	
	public static class Persona {
		
		public static long insert(Context context, PersonaStats stats) {
			
			try {
				//Use the SQLiteManager to get a cursor
				SQLiteManager manager = new SQLiteManager( context );
				
				//Get them!!
				long results = manager.insert( 
						
					DatabaseStructure.PersonaStatistics.TABLE_NAME, 
					DatabaseStructure.PersonaStatistics.getColumns(),
					stats.toStringArray()
					
				);
				
				manager.close();
				return results;
				
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				return -1;
				
				
			}
			
		}
		
		public static long insert(Context context, HashMap<Long, PersonaStats> statsArray) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			long results = 0;
			
			try {
				
				//Loop loop
				for( long key : statsArray.keySet() ) {

					//Get the object
					PersonaStats stats = statsArray.get( key );
					
					//Get them!!
					results += manager.insert( 
							
						DatabaseStructure.PersonaStatistics.TABLE_NAME, 
						DatabaseStructure.PersonaStatistics.getColumns(),
						stats.toStringArray()
						
					);
				
				}
				
				manager.close();
				return results;
				
			} catch( SQLiteConstraintException ex ) { //Duplicate input, no worries!

				manager.close();
				return 0;
			
			} catch( Exception ex ) {

				manager.close();
				ex.printStackTrace();
				return -1;
				
					
			}
			
		}
		
		public static boolean update(Context context, PersonaStats stats) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );

			try {
				//UPDATE them!!
				manager.update(
						
					DatabaseStructure.PersonaStatistics.TABLE_NAME, 
					DatabaseStructure.PersonaStatistics.getColumns(),
					stats.toStringArray(),
					DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PERSONA,
					stats.getPersonaId()
					
				);
				
				manager.close();
				return true;
				
			} catch( Exception ex ) {
				
				manager.close();
				ex.printStackTrace();
				return false;
				
				
			}
		
		}
			
		public static boolean update(Context context, HashMap<Long, PersonaStats> statsArray) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			int results = 0;
			
			try {
				
				//Loop over the keys
				for( long key : statsArray.keySet() ) {

					//Get the object
					PersonaStats stats = statsArray.get( key );
					
					//UPDATE them!!
					results += manager.update(
							
						DatabaseStructure.PersonaStatistics.TABLE_NAME, 
						DatabaseStructure.PersonaStatistics.getColumns(),
						stats.toStringArray(),
						DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PERSONA,
						stats.getPersonaId()
						
					);
					
				}

				//Close the manager
				manager.close();
				
				//Check the results
				return ( results > 0 );
				
			} catch( Exception ex ) {

				manager.close();
				ex.printStackTrace();
				return false;
				
			}
			
		}
	
		public static HashMap<Long, PersonaStats> select(final Context context, final long[] personaId) {

		
			SQLiteManager manager = new SQLiteManager( context );
			
			try {
	
				//Init
				String strQuestionMarks = "?";
				String[] personaIdArray = new String[personaId.length];
				HashMap<Long, PersonaStats> stats = new HashMap<Long, PersonaStats>();
				
				//Loop to string the array
				for( int i = 0, max = personaId.length; i < max; i++ ) { 
					
					if( i > 0 ) { strQuestionMarks += ",?"; }
					personaIdArray[i] = String.valueOf( personaId[i] ); 
					
				}
				
				//Use the SQLiteManager to get a cursor
				Cursor results = manager.query(
						
					DatabaseStructure.PersonaStatistics.TABLE_NAME, 
					null, 
					DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PERSONA + " IN (" + strQuestionMarks + ")", 
					personaIdArray,
					null, 
					null, 
					DatabaseStructure.PersonaStatistics.DEFAULT_SORT_ORDER
					
				);
				
				//Loop over the results
				if( results.moveToFirst() ) {
				
					do {
						
						stats.put( 

							results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PERSONA) ),							
							new PersonaStats(
									
								results.getString( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_ACCOUNT_NAME) ),
								results.getString( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_PERSONA_NAME) ),
								results.getString( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_RANK) ), 
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_RANK) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PERSONA) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_USER) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PLATFORM) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_TIME) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_POINTS_THIS) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_POINTS_NEXT) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_KILLS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_ASSISTS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_VEHICLES) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_VASSISTS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_HEALS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_REVIVES) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_REPAIRS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_RESUPPLIES) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_DEATHS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_WINS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_NUM_LOSSES) ),
								results.getDouble( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_KDR) ),
								results.getDouble( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_ACCURACY) ),
								results.getDouble( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_LONGEST_HS) ),
								results.getDouble( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_LONGEST_KS) ),
								results.getDouble( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_SKILL) ),
								results.getDouble( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_STATS_SPM) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_ASSAULT) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_ENGINEER) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_SUPPORT) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_RECON) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_VEHICLE) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_COMBAT) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_AWARDS) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_UNLOCKS) ),
								results.getLong( results.getColumnIndex(DatabaseStructure.PersonaStatistics.COLUMN_NAME_SCORE_TOTAL ) )
						
							)	
								
						);
						
					} while( results.moveToNext() );
					
				}
				
				//PERSONA PERSONA BABY
				results.close();
				manager.close();
				return stats;
			
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				manager.close();
				return null;
				
			}
			
		}
		
		public static boolean delete(final Context context, final long[] personaId) {

			SQLiteManager manager = new SQLiteManager( context );
			
			try {
				
				//Loop to string the array
				String[] personaIdArray = new String[personaId.length];				
				for( int i = 0, max = personaId.length; i < max; i++ ) { personaIdArray[i] = String.valueOf( personaId[i] ); }
				
				//Use the SQLiteManager to get a cursor
				int results = manager.delete( 
						
					DatabaseStructure.PersonaStatistics.TABLE_NAME,	
					DatabaseStructure.PersonaStatistics.COLUMN_NAME_ID_PERSONA,
					personaIdArray
					
				);

				manager.close();
				return (results > 0);
				
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				manager.close();
				return false;
			
			}
			
		}
	}
		
	public static class Profile {
		
		public static long insert(Context context, ProfileInformation stats) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );

			try {
	
				//Get them!!
				long results = manager.insert( 
						
					DatabaseStructure.UserProfile.TABLE_NAME, 
					DatabaseStructure.UserProfile.getColumns(),
					stats.toStringArray()
					
				);

				manager.close();
				return results;
				
			} catch( SQLiteConstraintException ex ) { //Duplicate input, no worries!

				manager.close();
				return 0;
			
			} catch( Exception ex ) {

				manager.close();
				ex.printStackTrace();
				return -1;
				
				
			}
			
		}
		
		public static boolean update(Context context, ProfileInformation stats) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			int results = 0;
			
			try {

				//UPDATE them!!
				results += manager.update(
						
					DatabaseStructure.UserProfile.TABLE_NAME, 
					DatabaseStructure.UserProfile.getColumns(),
					stats.toStringArray(),
					DatabaseStructure.UserProfile.COLUMN_NAME_NUM_UID,
					stats.getUserId()
					
				);
				
				manager.close();
				return (results > 0);
				
			} catch( Exception ex ) {

				manager.close();
				ex.printStackTrace();
				return false;
				
				
			}
			
		}
	
		public static ProfileInformation select(final Context context, final long userId) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			try {
				
				//Get the cursor
				Cursor results = manager.query(
						
					DatabaseStructure.UserProfile.TABLE_NAME, 
					null, 
					DatabaseStructure.UserProfile.COLUMN_NAME_NUM_UID + " = ?", 
					new String[] { userId + "" },
					null, 
					null, 
					DatabaseStructure.UserProfile.DEFAULT_SORT_ORDER
					
				);
				
				//Loop over the results
				if( results.moveToFirst() ) {
				
					//Any platoons?
					ArrayList<PlatoonData> platoons = new ArrayList<PlatoonData>();
					
					//Get the strings
					String personaIdString = results.getString( results.getColumnIndex("persona_id") );
					String platformIdString = results.getString( results.getColumnIndex("platform_id") );
					String personaNameString = results.getString( results.getColumnIndex("persona_name") );
					String platoonIdString = results.getString( results.getColumnIndex("platoons") );
					
					//Split them
					String[] personaStringArray = TextUtils.split( personaIdString, ":" );
					String[] platformStringArray = TextUtils.split( platformIdString, ":" );
					String[] personaNameStringArray = TextUtils.split( personaNameString, ":" );
					String[] platoonStringArray = TextUtils.split( platoonIdString, ":" );
					
					//How many do we have? -1 due to last occurence being empty
					int numPersonas = personaStringArray.length-1;
					int numPlatoons = platoonStringArray.length-1;
					
					//Create two new arrays for this
					String[] personaNameArray = new String[numPersonas];
					long[] personaIdArray = new long[numPersonas];
					long[] platformIdArray = new long[numPersonas];
					long[] platoonIdArray = new long[numPlatoons];
					
					//Loop for the personas
					for( int i = 0; i < numPersonas; i++ ) { 
						
						personaIdArray[i] = Long.parseLong( personaStringArray[i] );
						platformIdArray[i] = Long.parseLong( platformStringArray[i] );
						personaNameArray[i] = personaNameStringArray[i];
					
					}
					
					//loop the platoons
					for( int i = 0; i < numPlatoons; i++ ) { 
						
						platoonIdArray[i] = Long.parseLong(platoonStringArray[i]); 
						
					}
					
					//Do we have any platoons?
					if( numPlatoons > 0 ) { 
						
						//Let's get that array brah
						platoons = CacheHandler.Platoon.selectPlatoonsForUser( context, platoonIdArray );
						
					}
					
					//Get the profile
					ProfileInformation profile = new ProfileInformation(
							
						results.getInt( results.getColumnIndex("age") ),
						results.getLong( results.getColumnIndex("user_id") ),
						results.getLong( results.getColumnIndex("birth_date") ),
						results.getLong( results.getColumnIndex("last_login") ),
						results.getLong( results.getColumnIndex("status_changed") ),
						personaIdArray,
						platformIdArray,
						platoonIdArray,
						results.getString( results.getColumnIndex("name") ),
						results.getString( results.getColumnIndex("username") ),
						results.getString( results.getColumnIndex("presentation") ),
						results.getString( results.getColumnIndex("location") ), 
						results.getString( results.getColumnIndex("status_message") ),
						results.getString( results.getColumnIndex("current_server") ),
						personaNameArray,
						( results.getString( results.getColumnIndex("allow_friendrequests") ).equalsIgnoreCase( "true" ) ),
						( results.getString( results.getColumnIndex("is_online") ).equalsIgnoreCase( "true" ) ),
						( results.getString( results.getColumnIndex("is_playing") ).equalsIgnoreCase( "true" ) ),
						( results.getString( results.getColumnIndex("is_friend") ).equalsIgnoreCase( "true" ) ),
						null,
						platoons
						
					);

					//PERSONA PERSONA BABY
					results.close();
					manager.close();
					
					//RETURN
					return profile;
					
					
				} else {
					
					results.close();
					manager.close();
					
					return null;
					
				}
			
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				manager.close();
				return null;
				
			}
			
		}
		
		public static boolean delete(final Context context, final long[] userId) {
	
			try {
				
				//Loop to string the array
				String[] userIdArray = new String[userId.length];				
				for( int i = 0, max = userId.length; i < max; i++ ) { userIdArray[i] = String.valueOf( userId[i] ); }
				
				//Use the SQLiteManager to get a cursor
				SQLiteManager manager = new SQLiteManager( context );
				int results = manager.delete( 
						
					DatabaseStructure.UserProfile.TABLE_NAME,	
					DatabaseStructure.UserProfile.COLUMN_NAME_NUM_UID,
					userIdArray
					
				);

				//Close it
				manager.close();
				return (results > 0);
				
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				return false;
			
			}
			
		}
			
	}
	
	public static class Platoon {
		
		public static long insert(Context context, PlatoonInformation stats) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			
			try {
				
				//Get them!!
				long results = manager.insert( 
						
					DatabaseStructure.PlatoonProfile.TABLE_NAME, 
					DatabaseStructure.PlatoonProfile.getColumns(),
					stats.toStringArray()
					
				);
				
				manager.close();
				return results;
				
			} catch( SQLiteConstraintException ex ) { //Duplicate input, no worries!

				manager.close();
				return 0;
			
			} catch( Exception ex ) {
				
				manager.close();
				ex.printStackTrace();
				return -1;
				
				
			}
			
		}
		
		public static long insert(Context context, HashMap<Long, PlatoonInformation> statsArray) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			long results = 0;
			
			try {
				
				//Loop loop
				for( long key : statsArray.keySet() ) {

					//Get the object
					PlatoonInformation stats = statsArray.get( key );
					
					//Get them!!
					results += manager.insert( 
							
						DatabaseStructure.PlatoonProfile.TABLE_NAME, 
						DatabaseStructure.PlatoonProfile.getColumns(),
						stats.toStringArray()
						
					);
				
				}
				
				manager.close();
				return results;
				
			} catch( SQLiteConstraintException ex ) { //Duplicate input, no worries!

				manager.close();
				return 0;
			
			} catch( Exception ex ) {

				manager.close();
				ex.printStackTrace();
				return -1;
				
					
			}
			
		}
		
		public static boolean update(Context context, PlatoonInformation stats) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );

			try {
				//UPDATE them!!
				manager.update(
						
					DatabaseStructure.PlatoonProfile.TABLE_NAME, 
					DatabaseStructure.PlatoonProfile.getColumns(),
					stats.toStringArray(),
					DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID,
					stats.getId()
					
				);
				
				manager.close();
				return true;
				
			} catch( Exception ex ) {
				
				manager.close();
				ex.printStackTrace();
				return false;
				
				
			}
		
		}
			
		public static boolean update(Context context, HashMap<Long, PlatoonInformation> statsArray) {

			//Use the SQLiteManager to get a cursor
			SQLiteManager manager = new SQLiteManager( context );
			int results = 0;
			
			try {
				
				//Loop over the keys
				for( long key : statsArray.keySet() ) {

					//Get the object
					PlatoonInformation stats = statsArray.get( key );
					
					//UPDATE them!!
					results += manager.update(
							
						DatabaseStructure.PlatoonProfile.TABLE_NAME, 
						DatabaseStructure.PlatoonProfile.getColumns(),
						stats.toStringArray(),
						DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID,
						stats.getId()
						
					);
					
				}

				//Close the manager
				manager.close();
				
				//Check the results
				return ( results > 0 );
				
			} catch( Exception ex ) {

				manager.close();
				ex.printStackTrace();
				return false;
				
			}
			
		}
		
		public static PlatoonInformation select(final Context context, final long platoonId) {

			
			SQLiteManager manager = new SQLiteManager( context );
			PlatoonInformation tempPlatoon = null;
			
			try {

				//Use the SQLiteManager to get a cursor
				Cursor results = manager.query(
						
					DatabaseStructure.PlatoonProfile.TABLE_NAME, 
					null, 
					DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID + " = ?", 
					new String[] { platoonId + "" },
					null, 
					null, 
					DatabaseStructure.PlatoonProfile.DEFAULT_SORT_ORDER
					
				);
				
				//Loop over the results
				if( results.moveToFirst() ) {
				
					do {
						
						tempPlatoon = new PlatoonInformation(

							results.getLong( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID) ),
							results.getLong( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_DATE) ),
							results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_PLATFORM_ID) ),
							results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_GAME_ID) ),
							results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_FANS) ),
							results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_MEMBERS) ),
							results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_BLAZE_ID) ),
							results.getString( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_STRING_NAME) ),
							results.getString( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_STRING_TAG) ),
							results.getString( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_STRING_INFO) ),
							results.getString( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_STRING_WEB) ),
							results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_BOOL_VISIBLE) )
							
						);
						
					} while( results.moveToNext() );
					
				}
				
				//Platoon Platoon BABY
				results.close();
				manager.close();
				return tempPlatoon;
			
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				manager.close();
				return null;
				
			}
			
		}
	
		public static ArrayList<PlatoonData> selectPlatoonsForUser(final Context context, final long[] platoonId) {

		
			SQLiteManager manager = new SQLiteManager( context );
			
			try {
	
				//Init
				String strQuestionMarks = "?";
				String[] PlatoonIdArray = new String[platoonId.length];
				ArrayList<PlatoonData>  stats = new ArrayList<PlatoonData> ();
				
				//Loop to string the array
				for( int i = 0, max = platoonId.length; i < max; i++ ) { 
					
					if( i > 0 ) { strQuestionMarks += ",?"; }
					PlatoonIdArray[i] = String.valueOf( platoonId[i] ); 
					
				}
				
				//Use the SQLiteManager to get a cursor
				Cursor results = manager.query(
						
					DatabaseStructure.PlatoonProfile.TABLE_NAME, 
					null, 
					DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID + " IN (" + strQuestionMarks + ")", 
					PlatoonIdArray,
					null, 
					null, 
					DatabaseStructure.PlatoonProfile.DEFAULT_SORT_ORDER
					
				);
				
				//Loop over the results
				if( results.moveToFirst() ) {
				
					do {
						
						stats.add( 

							new PlatoonData(

								results.getLong( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_FANS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_MEMBERS) ),
								results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_PLATFORM_ID) ),
								results.getString( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_STRING_NAME) ),
								results.getString( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_STRING_TAG) ),
								null,
								( results.getInt( results.getColumnIndex(DatabaseStructure.PlatoonProfile.COLUMN_NAME_BOOL_VISIBLE) ) == 1 )
								
							)	
								
						);
						
					} while( results.moveToNext() );
					
				}
				
				//Platoon Platoon BABY
				results.close();
				manager.close();
				return stats;
			
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				manager.close();
				return null;
				
			}
			
		}
		
		public static boolean delete(final Context context, final long[] PlatoonId) {

			SQLiteManager manager = new SQLiteManager( context );
			
			try {
				
				//Loop to string the array
				String[] platoonIdArray = new String[PlatoonId.length];				
				for( int i = 0, max = PlatoonId.length; i < max; i++ ) { platoonIdArray[i] = String.valueOf( PlatoonId[i] ); }
				
				//Use the SQLiteManager to get a cursor
				int results = manager.delete( 
						
					DatabaseStructure.PlatoonProfile.TABLE_NAME,	
					DatabaseStructure.PlatoonProfile.COLUMN_NAME_NUM_ID,
					platoonIdArray
					
				);

				manager.close();
				return (results > 0);
				
			} catch( Exception ex ) {
				
				ex.printStackTrace();
				manager.close();
				return false;
			
			}
			
		}
	}
	
	public static boolean isCached(Context c, String f ) { 
		
		return( new File(ExternalCacheDirectory.getInstance( c ).getExternalCacheDirectory(), f ).exists() ); 
		
	}
	
}