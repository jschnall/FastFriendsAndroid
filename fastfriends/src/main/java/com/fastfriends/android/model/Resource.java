package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "resource")
public class Resource  implements Parcelable {
    public static final String ID = "id";
    public static final String CONTENT_TYPE = "content_type";
    public static final String URL = "url";
    public static final String HASH = "hash";
    public static final String FILE_NAME = "file_name";
    public static final String ALBUM = "album";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String DURATION = "duration";
    public static final String THUMBNAIL = "thumbnail";
    public static final String CAPTION = "caption";
    public static final String MENTIONS = "mentions";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(CONTENT_TYPE)
    @DatabaseField(columnName = CONTENT_TYPE)
    private String mContentType = null;

    @SerializedName(URL)
    @DatabaseField(columnName = URL)
    private String mUrl = null;

    @SerializedName(HASH)
    @DatabaseField(columnName = HASH)
    private String mHash = null;

    @SerializedName(FILE_NAME)
    @DatabaseField(columnName = FILE_NAME)
    private String mFileName = null;

    @SerializedName(ALBUM)
    @DatabaseField(columnName = ALBUM)
    private long mAlbumId;

    @SerializedName(WIDTH)
    @DatabaseField(columnName = WIDTH)
    private int mWidth;

    @SerializedName(HEIGHT)
    @DatabaseField(columnName = HEIGHT)
    private int mHeight;

    @SerializedName(DURATION)
    @DatabaseField(columnName = DURATION)
    private long mDuration; // Millisecs

    @SerializedName(THUMBNAIL)
    @DatabaseField(columnName = THUMBNAIL)
    private String mThumbnail;

    @SerializedName(CAPTION)
    @DatabaseField(columnName = CAPTION)
    private String mCaption;

    @DatabaseField(columnName = MENTIONS)
    private List<Mention> mMentions;

    public static final Parcelable.Creator<Resource> CREATOR = new Parcelable.Creator<Resource>() {
        public Resource createFromParcel(Parcel in) {
            return new Resource(in);
        }

        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };


    public Resource() {
        // Used by ORMLite
    }

    public Resource(String hash, String contentType, long albumId) {
        mHash = hash;
        mContentType = contentType;
        mAlbumId = albumId;
    }

    public Resource(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mContentType = in.readString();
        mUrl = in.readString();
        mHash = in.readString();
        mFileName = in.readString();
        mAlbumId = in.readLong();
        mWidth = in.readInt();
        mHeight = in.readInt();
        mDuration = in.readLong();
        mThumbnail = in.readString();
        mCaption = in.readString();
    }

    // Copy editable fields
    public void copy(Resource resource) {
        mId = resource.mId;
        mContentType = resource.mContentType;
        mUrl = resource.mUrl;
        mHash = resource.mHash;
        mFileName = resource.mFileName;
        mAlbumId = resource.mAlbumId;
        mWidth = resource.mWidth;
        mHeight = resource.mHeight;
        mDuration = resource.mDuration;
        mThumbnail = resource.mThumbnail;
        mCaption = resource.mCaption;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeString(mContentType);
        out.writeString(mUrl);
        out.writeString(mHash);
        out.writeString(mFileName);
        out.writeLong(mAlbumId);
        out.writeInt(mWidth);
        out.writeInt(mHeight);
        out.writeLong(mDuration);
        out.writeString(mThumbnail);
        out.writeString(mCaption);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) { mContentType = contentType; }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String data) {
        mUrl = data;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public List<Mention> getMentions() {
        return mMentions;
    }

    public void setMentions(List<Mention> mentions) {
        mMentions = mentions;
    }

}
