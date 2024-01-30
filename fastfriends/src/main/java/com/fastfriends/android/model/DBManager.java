package com.fastfriends.android.model;

import android.content.Context;
import android.util.Log;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.Settings;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by jschnall on 1/22/14.
 */
public class DBManager {
    private final static String LOGTAG = DBManager.class.getSimpleName();

    public static <T> int delete(Class<T> clazz, Long id) {
        String name = clazz.getSimpleName();
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<T, Long> dao = dbHelper.getDao(clazz);
            DeleteBuilder<T, Long> deleteBuilder = dao.deleteBuilder();
            if (id == null) {
                Log.d(LOGTAG, "Deleting all " + name);
            } else {
                deleteBuilder.where().eq("id", id);
                Log.d(LOGTAG, "Deleting " + name + " with id: " + id);
            }
            return deleteBuilder.delete();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot delete " + name + " with id: " + id, e);
            return 0;
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    public static <T> List<T> getAll(Class<T> clazz) {
        List<T> list = null;

        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<T, Long> dao = dbHelper.getDao(clazz);
            list = dao.queryForAll();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot get all " + clazz.getSimpleName(), e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        return list;
    }

    public static List<Tag> getAllCategories() {
        List<Tag> list = null;

        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<Tag, Long> dao = dbHelper.getDao(Tag.class);
            QueryBuilder<Tag, Long> queryBuilder = dao.queryBuilder();
            Where where = queryBuilder.where();
            where.le(Tag.PARENT, 0);
            list = dao.query(queryBuilder.prepare());
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot get all categories", e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        return list;
    }

    public static <T> T get(Class<T> clazz, long id) {
        T t = null;

        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<T, Long> dao = dbHelper.getDao(clazz);
            t = dao.queryForId(id);
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot get " + clazz.getSimpleName() + " with id: " + id, e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        return t;
    }

    public static <T> int save(T t) {
        Class clazz =  t.getClass();

        if (t == null) {
            return 0;
        }
        int count = 0;
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<T, Long> dao = dbHelper.getDao(clazz);
            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(t);
            count = status.getNumLinesChanged();
            Log.d(LOGTAG, "Saved " + count + " " + clazz.getSimpleName());
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot save " + clazz.getSimpleName() + ". ", e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        return count;
    }

    public static <T> int save(final T[] array) {
        if (array == null || array.length < 1) {
            return 0;
        }
        Class clazz = array[0].getClass();

        int count = 0;
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            final Dao<T, Long> dao = dbHelper.getDao(clazz);
            count = dao.callBatchTasks(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int linesChanged = 0;
                    for (T t : array) {
                        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(t);
                        linesChanged += status.getNumLinesChanged();
                    }
                    return linesChanged;
                }
            });
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't save " + clazz.getSimpleName() + ".", e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        Log.i(LOGTAG, "Saved " + count + " " + clazz.getSimpleName() + ".");
        return count;
    }

    public static <T> int save(final List<T> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        Class clazz = list.get(0).getClass();

        int count = 0;
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            final Dao<T, Long> dao = dbHelper.getDao(clazz);
            count = dao.callBatchTasks(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int linesChanged = 0;
                    for (T t : list) {
                        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(t);
                        linesChanged += status.getNumLinesChanged();
                    }
                    return linesChanged;
                }
            });
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't save " + clazz.getSimpleName() + ".", e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        Log.i(LOGTAG, "Saved " + count + " " + clazz.getSimpleName() + ".");
        return count;
    }

    // Deletes all messages in a conversation
    public static int deleteMessages(long otherUserId) {
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<Message, Long> dao = dbHelper.getDao(Message.class);
            DeleteBuilder<Message, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(Message.SENDER, otherUserId).or().eq(Message.RECEIVER, otherUserId);
            Log.d(LOGTAG, "Deleting messages with sender or receiver: " + otherUserId);
            return deleteBuilder.delete();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot delete delete messages with sender or receiver: " + otherUserId, e);
            return 0;
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    // Deletes all comments on an event
    public static int deleteEventComments(long eventId) {
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<Comment, Long> dao = dbHelper.getDao(Comment.class);
            DeleteBuilder<Comment, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(Comment.EVENT, eventId);
            Log.d(LOGTAG, "Deleting comments with eventId: " + eventId);
            return deleteBuilder.delete();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot delete comments with eventId: " + eventId, e);
            return 0;
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    // Deletes all comments on an event
    public static int deletePlanComments(long planId) {
        OrmLiteSqliteOpenHelper dbHelper = null;
        try {
            dbHelper = DBOpenHelper.getInstance();
            Dao<Comment, Long> dao = dbHelper.getDao(Comment.class);
            DeleteBuilder<Comment, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(Comment.PLAN, planId);
            Log.d(LOGTAG, "Deleting comments with planId: " + planId);
            return deleteBuilder.delete();
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot delete comments with planId: " + planId, e);
            return 0;
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

}
