/*
 * Copyright 2014 - Jamdeo
 */

package com.blb.mmwd.uclient.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * <p>
 * The class defines a contract between Mox content provider and Mox
 * application. A contract defines the information that a client needs to access
 * the provider as one or more data tables. A contract is a public,
 * non-extendable (final) class that contains constants defining column names
 * and URIs. A well-written client depends only on the constants in the
 * contract.
 * </p>
 * <p>
 * Tables include:
 * </p>
 * <ul>
 * <li>
 * {@link Cards}, which contains all cards saved in the local</li>
 * <li>
 * {@link Events}, which contains all events saved in the local</li>
 * </ul>
 * 
 * @author brljoche
 */
public final class MmwdContentContract {
    /** The authority for the contacts provider */
    public static final String AUTHORITY = "com.blb.mmwd.uclient";
    /** A content:// style uri to the authority for the contacts provider */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    // This class cannot be instantiated
    private MmwdContentContract() {
    }

    /**
     * Cards table contract
     */
    public static final class Cart implements BaseColumns {
        // This class cannot be instantiated
        private Cart() {
        }

        /**
         * The table name offered by this provider
         */
        public final static String TABLE_NAME = "carts";

        /*
         * URI definitions
         */

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                AUTHORITY_URI, TABLE_NAME);

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of cards.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/carts";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/carts";
        
        public static final String COLUMN_CART_TIME = "cart_time";
        public static final String COLUMN_MM_SHOP_ID = "mm_shop_id";
        public static final String COLUMN_MM_SHOP_NAME = "mm_shop_name";
        public static final String COLUMN_MM_SHOP_IMG = "mm_shop_img";
        public static final String COLUMN_FOOD_ID = "food_id";
        public static final String COLUMN_FOOD_NAME = "food_name";
        public static final String COLUMN_FOOD_COUNT = "food_count";
        public static final String COLUMN_FOOD_CROSS_AREA = "food_cross_area";
        public static final String COLUMN_FOOD_NOTE = "food_note";
    }

    /**
     * Events table contract not used so far
     */
    public static final class Address implements BaseColumns {
        // This class cannot be instantiated
        private Address() {
        }

        /**
         * The table name offered by this provider
         */
        public final static String TABLE_NAME = "addresses";

        /*
         * URI definitions
         */

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                AUTHORITY_URI, TABLE_NAME);

        /**
         * 0-relative position of an event ID segment in the path part of an event ID URI
         */
        public static final int ADDRESS_ID_PATH_POSITION = 1;

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * events.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/addresses";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * event.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/address";

        /*
         * Column definitions
         */


        /**
         * Column name for the type of the event action
         * <p>
         * TYPE: Text
         * </p>
         * <ul>
         * <li>VALUE: INVALID card not valid to pass around. ie. received a card, but got deleted
         * <p>
         * invalid action
         * </p>
         * </li>
         * </p>
         * </li>
         * </ul>
         */
        public static final String COLUMN_ADDRESS_PHONE = "phone";

        /**
         * Column name for the card uid of the event
         * <p>
         * TYPE: text
         * </p>
         */
        public static final String COLUMN_ADDRESS_COMMUNITY_ID = "community_id";
        public static final String COLUMN_ADDRESS_COMMUNITY_NAME = "community_name";
        public static final String COLUMN_ADDRESS_DETAILS = "details";
        public static final String COLUMN_ADDRESS_IS_DEFAULT = "is_default";
    }
    
    
    /**
     * misc table contract
     */
    public static final class Misc implements BaseColumns {
        // This class cannot be instantiated
        private Misc() {
        }

        /**
         * The table name offered by this provider
         */
        public final static String TABLE_NAME = "misc";

        /*
         * URI definitions
         */

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                AUTHORITY_URI, TABLE_NAME);

        /**
         * 0-relative position of an event ID segment in the path part of an event ID URI
         */

        public static final int MISC_NAME_PATH_POSITION = 1;
        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * events.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/miscs";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * event.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/misc";

        /*
         * Column definitions
         */


        /**
         * Column name for the type of the event action
         * <p>
         * TYPE: Text
         * </p>
         * <ul>
         * <li>VALUE: INVALID card not valid to pass around. ie. received a card, but got deleted
         * <p>
         * invalid action
         * </p>
         * </li>
         * </p>
         * </li>
         * </ul>
         */
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_VALUE = "value";
    }
}
