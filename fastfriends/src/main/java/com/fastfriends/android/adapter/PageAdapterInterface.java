package com.fastfriends.android.adapter;

import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Page;

/**
 * Created by jschnall on 8/25/14.
 */
public interface PageAdapterInterface<T> {
    public void addPage(Page<T> page);
    public void reset(Page<T> page);
}
