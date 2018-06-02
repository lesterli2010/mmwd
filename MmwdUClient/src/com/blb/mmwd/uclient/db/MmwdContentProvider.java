/* Copyright 2014 - Jamdeo
 */

package com.blb.mmwd.uclient.db;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;


public class MmwdContentProvider extends ContentProvider {
    private static final String TAG = "MmwdContentProvider";
    private static final boolean DEBUG = true;

    /**
     * The database that the provider uses as its underlying data store
     */
    private static final String DATABASE_NAME = "mmwd_uclient.db";

    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 1;

    /*
     * Constants used by the Uri matcher to choose an action based on the
     * pattern of the incoming URI
     */
    // The incoming URI matches the Cards URI pattern
    private static final int CARTS = 1;
    
    private static final int CART_ID = 2;

    // The incoming URI matches the Events URI pattern
    private static final int ADDRESSES = 3;

    // The incoming URI matches the Event ID URI pattern
    private static final int ADDRESS_ID = 4;
    
    private static final int MISCS = 5;
    private static final int MISC_NAME = 6;

    /**
     * A UriMatcher instance
     */
    private static final UriMatcher sUriMatcher;

    // Handle to a new DatabaseHelper.
    private DatabaseHelper mOpenHelper;

    /**
     * A block that instantiates and sets static objects
     */
    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add a pattern that routes URIs terminated with "cards" to a CARDS
        // operation
        sUriMatcher.addURI(MmwdContentContract.AUTHORITY, MmwdContentContract.Cart.TABLE_NAME, CARTS);
        
        sUriMatcher.addURI(MmwdContentContract.AUTHORITY, MmwdContentContract.Cart.TABLE_NAME + "/#", CART_ID);

        // Add a pattern that routes URIs terminated with "events" to an EVENTS
        // operation
        sUriMatcher.addURI(MmwdContentContract.AUTHORITY, MmwdContentContract.Address.TABLE_NAME, ADDRESSES);

        // Add a pattern that routes URIs terminated with "events" plus an
        // integer to an event ID operation
        sUriMatcher.addURI(MmwdContentContract.AUTHORITY, MmwdContentContract.Address.TABLE_NAME + "/#", ADDRESS_ID);
        
        sUriMatcher.addURI(MmwdContentContract.AUTHORITY, MmwdContentContract.Misc.TABLE_NAME, MISCS);
        sUriMatcher.addURI(MmwdContentContract.AUTHORITY, MmwdContentContract.Misc.TABLE_NAME + "/*", MISC_NAME);
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // A string that defines the SQL statement for creating the table cards
        private static final String SQL_CREATE_TABLE_CARTS = "CREATE TABLE "
                + MmwdContentContract.Cart.TABLE_NAME + " ("
                + MmwdContentContract.Cart._ID + " INTEGER PRIMARY KEY,"
                + MmwdContentContract.Cart.COLUMN_CART_TIME + " INTEGER DEFAULT 0,"
                + MmwdContentContract.Cart.COLUMN_MM_SHOP_ID + " INTEGER DEFAULT 0,"
                + MmwdContentContract.Cart.COLUMN_MM_SHOP_NAME + " TEXT,"
                + MmwdContentContract.Cart.COLUMN_MM_SHOP_IMG + " TEXT,"
                + MmwdContentContract.Cart.COLUMN_FOOD_ID + " INTEGER DEFAULT 0,"
                + MmwdContentContract.Cart.COLUMN_FOOD_NAME + " TEXT,"
                + MmwdContentContract.Cart.COLUMN_FOOD_COUNT + " INTEGER DEFAULT 0,"
                + MmwdContentContract.Cart.COLUMN_FOOD_CROSS_AREA + " INTEGER DEFAULT 0,"
                + MmwdContentContract.Cart.COLUMN_FOOD_NOTE + " TEXT"
                + ");";

        // A string that defines the SQL statement for creating the table events
      /*
        private static final String SQL_CREATE_TABLE_ADDRESSES = "CREATE TABLE "
                + MmwdContentContract.Address.TABLE_NAME + " ("
                + MmwdContentContract.Address._ID + " INTEGER PRIMARY KEY,"
                + MmwdContentContract.Address.COLUMN_ADDRESS_PHONE + " TEXT,"
                + MmwdContentContract.Address.COLUMN_ADDRESS_COMMUNITY_ID + " INTEGER DEFAULT 0,"
                + MmwdContentContract.Address.COLUMN_ADDRESS_COMMUNITY_NAME + " TEXT,"
                + MmwdContentContract.Address.COLUMN_ADDRESS_DETAILS + " TEXT,"
                + MmwdContentContract.Address.COLUMN_ADDRESS_IS_DEFAULT + " INTEGER DEFAULT 0"
                + ");";
*/
        private static final String SQL_CREATE_TABLE_MISC = "CREATE TABLE "
                + MmwdContentContract.Misc.TABLE_NAME + " ("
                + MmwdContentContract.Misc._ID + " INTEGER PRIMARY KEY,"
                + MmwdContentContract.Misc.COLUMN_NAME + " TEXT,"
                + MmwdContentContract.Misc.COLUMN_VALUE + " TEXT"
                + ");";

        
        DatabaseHelper(Context context) {
            // calls the super constructor, requesting the default cursor
            // factory.
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Creates the underlying database with table name and column names
         * taken from the MoxAppContract class.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create all the needed tables
            if (DEBUG) {
                Log.d(TAG, "create table: " + MmwdContentContract.Cart.TABLE_NAME);
            }

            db.execSQL(SQL_CREATE_TABLE_CARTS);

            if (DEBUG) {
                Log.d(TAG, "create table: " + MmwdContentContract.Address.TABLE_NAME);
            }

           // db.execSQL(SQL_CREATE_TABLE_ADDRESSES);
            
            db.execSQL(SQL_CREATE_TABLE_MISC);
        }

        /**
         * Demonstrates that the provider must consider what happens when the
         * underlying datastore is changed.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Logs that the database is being upgraded
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
        }
    }

    /**
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is
     * called automatically when Android creates the provider in response to a
     * resolver request from a client.
     */
    @Override
    public boolean onCreate() {

        // Creates a new helper object. Note that the database itself isn't
        // opened until something tries to access it, and it's only created if
        // it doesn't already exist.
        mOpenHelper = new DatabaseHelper(getContext());

        // Assumes that any failures will be reported by a thrown exception.
        return true;
    }

    /**
     * This method is called when a client calls
     * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}
     * . Queries the database and returns a cursor containing the results.
     * 
     * @return A cursor containing the results of the query. The cursor exists
     *         but is empty if the query returns no results or an exception
     *         occurs.
     * @throws IllegalArgumentException if the incoming URI pattern is invalid.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (DEBUG) {
            Log.d(TAG, "query(" + uri + ", " + Arrays.toString(projection) + ", " + selection
                    + ", " + Arrays.toString(selectionArgs) + ")");
        }

        // Constructs a new query builder and sets its table name
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        /**
         * Choose table, projection and adjust the "where" clause based on URI
         * pattern-matching.
         */
        switch (sUriMatcher.match(uri)) {
        // If the incoming URI is for cards, chooses the cards table
            case CARTS:
                qb.setTables(MmwdContentContract.Cart.TABLE_NAME);
                break;

           
            // If the incoming URI is for events, chooses the events table
            case ADDRESSES:
                qb.setTables(MmwdContentContract.Address.TABLE_NAME);
                break;

            /*
             * If the incoming URI is for a single event identified by its ID,
             * chooses the event ID projection, and appends "_ID = <eventID>" to
             * the where clause, so that it selects that single event
             */
            case ADDRESS_ID:
                qb.setTables(MmwdContentContract.Address.TABLE_NAME);
                qb.appendWhere(
                        MmwdContentContract.Address._ID + // the name of the ID column
                                "=" +
                                // the position of the event ID itself in the
                                // incoming URI
                                uri.getPathSegments().get(
                                        MmwdContentContract.Address.ADDRESS_ID_PATH_POSITION));
                break;
            case MISCS:
                qb.setTables(MmwdContentContract.Misc.TABLE_NAME);
                break;

            case MISC_NAME:
                qb.setTables(MmwdContentContract.Misc.TABLE_NAME);
                qb.appendWhere(
                        MmwdContentContract.Misc.COLUMN_NAME + // the name of the ID column
                                "=" +
                                // the position of the event ID itself in the
                                // incoming URI
                                uri.getPathSegments().get(
                                        MmwdContentContract.Misc.MISC_NAME_PATH_POSITION));
                break;
            default:
                // If the URI doesn't match any of the known patterns, throw an
                // exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = null;
        synchronized (this) {

            // Opens the database object in "read" mode, since no writes need to
            // be done.
            final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

            /*
             * Performs the query. If no problems occur trying to read the
             * database, then a Cursor object is returned; otherwise, the cursor
             * variable contains null. If no records were selected, then the
             * Cursor object is empty, and Cursor.getCount() returns 0.
             */
            c = qb.query(
                    db, // The database to query
                    projection, // The columns to return from the query
                    selection, // The columns for the where clause
                    selectionArgs, // The values for the where clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    sortOrder // The sort order
            );

            // Tells the Cursor what URI to watch, so it knows when its source
            // data changes
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }

    /**
     * This is called when a client calls
     * {@link android.content.ContentResolver#getType(Uri)}. Returns the MIME
     * data type of the URI given as a parameter.
     * 
     * @param uri The URI whose MIME type is desired.
     * @return The MIME type of the URI.
     * @throws IllegalArgumentException if the incoming URI pattern is invalid.
     */
    @Override
    public String getType(Uri uri) {
        if (DEBUG) {
            Log.d(TAG, "getType(" + uri + ")");
        }

        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {

        // If the pattern is for cards, returns the general
        // content type.
            case CARTS:
                return MmwdContentContract.Cart.CONTENT_TYPE;
                // If the pattern is for events, returns the general
                // content type.
            case CART_ID:
                return MmwdContentContract.Cart.CONTENT_ITEM_TYPE;
            case ADDRESSES:
                return MmwdContentContract.Address.CONTENT_TYPE;

                // If the pattern is for event IDs, returns the card ID content
                // type.
            case ADDRESS_ID:
                return MmwdContentContract.Address.CONTENT_ITEM_TYPE;

            case MISCS:
                return MmwdContentContract.Misc.CONTENT_TYPE;
            case MISC_NAME:
                return MmwdContentContract.Misc.CONTENT_ITEM_TYPE;
                // If the URI pattern doesn't match any permitted patterns,
                // throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * This is called when a client calls
     * {@link android.content.ContentResolver#insert(Uri, ContentValues)}.
     * Inserts a new row into the database. This method sets up default values
     * for any columns that are not included in the incoming map. If rows were
     * inserted, then listeners are notified of the change.
     * 
     * @return The row ID of the inserted row.
     * @throws SQLException if the insertion fails.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Validates the incoming URI. Only the full provider URI is allowed for
        // inserts.
        final String table = getTableFromUri(uri);
        long rowId = -1;

        Log.d(TAG, "insert uri:" + uri + ",value:");
        synchronized (this) {

            // Opens the database object in "write" mode.
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            // Performs the insert and returns the ID of the new row.
            rowId = db.insert(
                    table, // The table to insert into.
                    null, // SQLite sets this column value to null if values is
                          // empty.
                    values // A map of column names, and the values to insert
                           // into the columns.
                    );
        }

        if (rowId > 0) {
            // Notifies observers registered against this provider that the data
            // changed.
            getContext().getContentResolver().notifyChange(uri, null);
            // Creates a URI with the card ID pattern and the new row ID
            // appended to it.
            return ContentUris.withAppendedId(uri, rowId);
        }
        // If the insert didn't succeed, then the rowID is <= 0. Throws an
        // exception.
        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * This is called when a client calls
     * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
     * Deletes records from the database. If the incoming URI matches the
     * card/event ID URI pattern, this method deletes the one record specified
     * by the ID in the URI. Otherwise, it deletes a a set of records. The
     * record or records must also match the input selection criteria specified
     * by where and whereArgs. If rows were deleted, then listeners are notified
     * of the change.
     * 
     * @return If a "where" clause is used, the number of rows affected is
     *         returned, otherwise 0 is returned. To delete all rows and get a
     *         row count, use "1" as the where clause.
     * @throws IllegalArgumentException if the incoming URI pattern is invalid.
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final String table = getTableFromUri(uri);
        final String finalWhere = getIdWhereFromUri(uri, where);
        int rows = 0;

        if (DEBUG) {
            Log.d(TAG, "delete " + table + "   where: " + finalWhere + "   whereArgs: "
                    + whereArgs);
        }

        synchronized (this) {
            // Opens the database object in "write" mode.
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            // Performs the delete.
            rows = db.delete(
                    table, // The database table name.
                    finalWhere, // The final WHERE clause
                    whereArgs // The incoming where clause values.
                    );
        }

        if (rows != 0) {
            /*
             * Gets a handle to the content resolver object for the current
             * context, and notifies it that the incoming URI changed. The
             * object passes this along to the resolver framework, and observers
             * that have registered themselves for the provider are notified.
             */
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of rows deleted.
        return rows;
    }

    /**
     * This is called when a client calls
     * {@link android.content.ContentResolver#update(Uri,ContentValues,String,String[])}
     * Updates records in the database. The column names specified by the keys
     * in the values map are updated with new data specified by the values in
     * the map. If the incoming URI matches the card/event ID URI pattern, then
     * the method updates the one record specified by the ID in the URI;
     * otherwise, it updates a set of records. The record or records must match
     * the input selection criteria specified by where and whereArgs. If rows
     * were updated, then listeners are notified of the change.
     * 
     * @param uri The URI pattern to match and update.
     * @param values A map of column names (keys) and new values (values).
     * @param where An SQL "WHERE" clause that selects records based on their
     *            column values. If this is null, then all records that match
     *            the URI pattern are selected.
     * @param whereArgs An array of selection criteria. If the "where" param
     *            contains value placeholders ("?"), then each placeholder is
     *            replaced by the corresponding element in the array.
     * @return The number of rows updated.
     * @throws IllegalArgumentException if the incoming URI pattern is invalid.
     */
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        final String table = getTableFromUri(uri);
        final String finalWhere = getIdWhereFromUri(uri, where);
        int rows = 0;

        if (DEBUG) {
            Log.d(TAG, "update " + table + "   where: " + finalWhere + "   whereArgs: "
                    + whereArgs);
        }

        synchronized (this) {

            // Opens the database object in "write" mode.
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            // Does the update and returns the number of rows updated.
            rows = db.update(
                    table, // The database table name.
                    values, // A map of column names and new values to use.
                    finalWhere, // The final WHERE clause to use
                                // placeholders for whereArgs
                    whereArgs // The where clause column values to select
                              // on, or
                              // null if the values are in the where
                              // argument.
                    );
        }

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else if (MmwdContentContract.Misc.TABLE_NAME.equals(table)) {
            // insert new
            insert(uri, values);
        }

        // Returns the number of rows updated.
        return rows;
    }

    /**
     * Gets the table from the Uri using the {@link #sUriMatcher}.
     * 
     * @param uri
     * @return the table name
     */
    private String getTableFromUri(Uri uri) throws IllegalArgumentException {
        final String table;
        switch (sUriMatcher.match(uri)) {
            case CARTS:
            case CART_ID:
                table = MmwdContentContract.Cart.TABLE_NAME;
                break;

            case ADDRESSES:
            case ADDRESS_ID:
                table = MmwdContentContract.Address.TABLE_NAME;
                break;
            case MISCS:
            case MISC_NAME:
                table = MmwdContentContract.Misc.TABLE_NAME;
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI=" + uri);
        }
        return table;
    }
    /**
     * Gets the id from the Uri using the {@link #sUriMatcher}.
     * 
     * @param uri
     * @param where
     * @return the table name
     */
    private String getIdWhereFromUri(final Uri uri, final String where) {
        final String idWhere;

        switch (sUriMatcher.match(uri)) {
            case CARTS:
                // do nothing
                break;
            case CART_ID:
                idWhere = MmwdContentContract.Cart._ID + " = "
                        + uri.getLastPathSegment();
                if (where != null) {
                    return where + " AND " + idWhere;
                } else {
                    return idWhere;
                }
            

            case ADDRESSES:
                // do nothing
                break;

            // If the incoming URI matches a single event ID, modifies the where
            // clause to restrict it to the particular event ID.
            case ADDRESS_ID:
                idWhere = MmwdContentContract.Address._ID + " = "
                        + uri.getLastPathSegment();
                if (where != null) {
                    return where + " AND " + idWhere;
                } else {
                    return idWhere;
                }
            case MISCS:
            case MISC_NAME:
                break;
                // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return where;
    }

}
