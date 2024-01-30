package com.fastfriends.android.model;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fastfriends.android.FastFriendsApplication;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


/**
 * Created by jschnall on 1/20/14.
 */
public class DBOpenHelper extends OrmLiteSqliteOpenHelper {
    private static final String LOGTAG = DBOpenHelper.class.getSimpleName();
    public static final String DATABASE_NAME = "FastFriends.db";
    private static final int DATABASE_VERSION = 2;

    private static final AtomicInteger mUsageCounter = new AtomicInteger(0);
    private static DBOpenHelper mInstance;


    public static synchronized DBOpenHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DBOpenHelper();
            mUsageCounter.set(0);
        }
        mUsageCounter.incrementAndGet();
        return mInstance;
    }

    public DBOpenHelper() {
        super(FastFriendsApplication.getAppContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void close() {
        if (mUsageCounter.decrementAndGet() <= 0) {
            super.close();
            mInstance = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        createTables(connectionSource);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(LOGTAG, "Database version changed. Upgrading database.");
        try {
            if (newVersion > oldVersion) {
                clearDatabase(connectionSource);
            }
        } catch (SQLException e) {
            Log.e(LOGTAG, "Can't upgrade Database.", e);
        }
    }

    private static void createTables(ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, AuthToken.class);
            TableUtils.createTableIfNotExists(connectionSource, Tag.class);
            TableUtils.createTableIfNotExists(connectionSource, User.class);

            // Notifications
            TableUtils.createTableIfNotExists(connectionSource, Message.class); // Notified, unread messages
            TableUtils.createTableIfNotExists(connectionSource, Comment.class); // Notified, unread comments
            TableUtils.createTableIfNotExists(connectionSource, Event.class); // Notified, updated events
        } catch (Exception e) {
            Log.e(LOGTAG, "could not create table", e);
        }
    }

    private static void clearDatabase(ConnectionSource connectionSource) throws SQLException {
        TableUtils.dropTable(connectionSource, AuthToken.class, true);
        TableUtils.dropTable(connectionSource, Tag.class, true);
        TableUtils.dropTable(connectionSource, User.class, true);

        // Notifications
        TableUtils.dropTable(connectionSource, Message.class, true);
        TableUtils.dropTable(connectionSource, Comment.class, true);
        TableUtils.dropTable(connectionSource, Event.class, true);
        createTables(connectionSource);
    }

    public static boolean clearDatabase() {
        DBOpenHelper dbOpenHelper = DBOpenHelper.getInstance();
        try {
            clearDatabase(dbOpenHelper.getConnectionSource());
            return true;
        } catch (Exception e) {
            Log.e(LOGTAG, "could not clear database", e);
        } finally {
            dbOpenHelper.close();
        }
        return false;
    }


}
