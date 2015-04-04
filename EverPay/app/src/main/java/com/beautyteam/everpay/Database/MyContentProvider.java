package com.beautyteam.everpay.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.beautyteam.everpay.Constants;

/**
 * Created by Admin on 27.02.2015.
 */

public class MyContentProvider extends ContentProvider {
    final String LOG_TAG = "myLogs";
    // // Константы для БД
// БД
    static final String DB_NAME = "mydb";
    static final int DB_VERSION = 6;
    //=========================
// Таблица
    static final String CONTACT_TABLE = "contacts";
    // Поля
    public static final String CONTACT_ID = "_id";
    public static final String CONTACT_NAME = "name";
    public static final String CONTACT_EMAIL = "email";
    public static final String IMG_NAME = "img_name";
    public static final String STATE = "state";
    public static final String RESULT = "result";
    // Скрипт создания таблицы
    static final String DB_CREATE = "create table " + CONTACT_TABLE + "("
            + CONTACT_ID + " integer primary key autoincrement, "
            + CONTACT_NAME + " text, "
            + CONTACT_EMAIL + " text,"
            + IMG_NAME + " text,"
            + STATE + " integer,"
            + RESULT + " integer" + ");";
    // // Uri
// authority
    static final String AUTHORITY = "com.beautyteam.everpay.AdressBook";
    // path
    static final String CONTACT_PATH = "contacts";
    // Общий Uri
    public static final Uri CONTACT_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + CONTACT_PATH);
    // Типы данных
// набор строк
    static final String CONTACT_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + CONTACT_PATH;
    // одна строка
    static final String CONTACT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + CONTACT_PATH;
    //// UriMatcher
// общий Uri
    static final int URI_CONTACTS = 1;
    // Uri с указанным ID
    static final int URI_CONTACTS_ID = 2;
    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CONTACT_PATH, URI_CONTACTS);
        uriMatcher.addURI(AUTHORITY, CONTACT_PATH + "/#", URI_CONTACTS_ID);
    }
    DBHelper dbHelper;
    SQLiteDatabase db;
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        dbHelper = new DBHelper(getContext());
        return true;
    }

    // чтение
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query, " + uri.toString());
        // проверяем Uri
        if (TextUtils.isEmpty(selection)) {
            selection = RESULT + "!=" + Constants.Result.ERROR;
        } else {
            selection += "AND" + RESULT + "!=" + Constants.Result.ERROR;
        }
        switch (uriMatcher.match(uri)) {
            case URI_CONTACTS: // общий Uri
                Log.d(LOG_TAG, "URI_USERS");
                // если сортировка не указана, ставим свою - по имени
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = CONTACT_NAME + " DESC";
                }
                break;
            case URI_CONTACTS_ID: // Uri с ID
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_USERS_ID, " + id);
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = CONTACT_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CONTACT_ID + " = " + id;
                }

                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(CONTACT_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);
        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в USERS_CONTENT_URI
        cursor.setNotificationUri(getContext().getContentResolver(),
                CONTACT_CONTENT_URI);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert, " + uri.toString());

        if (uriMatcher.match(uri) != URI_CONTACTS)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(CONTACT_TABLE, null, values);
        Uri resultUri = ContentUris.withAppendedId(CONTACT_CONTENT_URI, rowID);
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_CONTACTS:
                Log.d(LOG_TAG, "URI_USERS");
                break;
            case URI_CONTACTS_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_USERS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = CONTACT_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CONTACT_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(CONTACT_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(LOG_TAG, "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_CONTACTS:
                Log.d(LOG_TAG, "URI_USERS");

                break;
            case URI_CONTACTS_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_USERS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = CONTACT_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CONTACT_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(CONTACT_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_CONTACTS:
                return CONTACT_CONTENT_TYPE;
            case URI_CONTACTS_ID:
                return CONTACT_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.d(Constants.LOG, "OnCreateDB");
            Log.d(Constants.LOG, DB_CREATE);
            db.execSQL(DB_CREATE);
            ContentValues cv = new ContentValues();
            for (int i = 1; i <= 10; i++) {
                cv.put(CONTACT_NAME, "name " + i);
                cv.put(CONTACT_EMAIL, "email " + i);
                cv.put(IMG_NAME, i+".png");
                cv.put(RESULT, Constants.Result.OK);
                db.insert(CONTACT_TABLE, null, cv);
            }

            cv.put(CONTACT_NAME, "name " + 11);
            cv.put(CONTACT_EMAIL, "email " + 11);
            cv.put(IMG_NAME, 11+".png");
            cv.put(RESULT, Constants.Result.ERROR);
            db.insert(CONTACT_TABLE, null, cv);

            cv.put(CONTACT_NAME, "name " + 2);
            cv.put(CONTACT_EMAIL, "email " + 2);
            cv.put(IMG_NAME, 12+".png");
            cv.put(RESULT, Constants.Result.ERROR);
            db.insert(CONTACT_TABLE, null, cv);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(Constants.LOG, "in Upgrade");
            if (oldVersion == 3 && newVersion == 4) {
                db.delete(CONTACT_TABLE, "", null);
                ContentValues cv = new ContentValues();
                for (int i = 1; i <= 3; i++) {
                    cv.put(CONTACT_NAME, "name " + i);
                    cv.put(CONTACT_EMAIL, "email " + i);
                    cv.put(RESULT, Constants.Result.OK);
                    db.insert(CONTACT_TABLE, null, cv);
                }


                Log.d(Constants.LOG, "onUpgrade");

            }
        }
    }
}

