package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "tag")
public class Tag {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PARENT = "parent";
    public static final String SHOW_ICON = "show_icon";
    public static final String ICON_URL = "icon_url";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(NAME)
    @DatabaseField(columnName = NAME)
    private String mName = null;

    @SerializedName(PARENT)
    @DatabaseField(columnName = PARENT)
    private long mParent = 0;

    @SerializedName(SHOW_ICON)
    @DatabaseField(columnName = SHOW_ICON)
    private boolean mShowIcon = false;

    @SerializedName(ICON_URL)
    @DatabaseField(columnName = ICON_URL)
    private String mIconUrl = null;

    public Tag() {
        // Used by ORMLite
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getParentId() {
        return mParent;
    }

    public void setParentId(long parent) { mParent = parent; }

    public boolean isShowIcon() {
        return mShowIcon;
    }

    public void setShowIcon(boolean showIcon) {
        mShowIcon = showIcon;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String icon) {
        mIconUrl = icon;
    }

    public static List<Tag> getAllCategories() {
        return DBManager.getAllCategories();
    }

    public String toString() {
        return mName;
    }
}
