package com.jb.sharedmatting;

import android.graphics.Path;

/**
 * Created by xiaoyu on 2017/5/22 22:41.
 */

public class PathBean {
    public static final int TYPE_FORE = 0;
    public static final int TYPE_MEDIUM = 1;
    public static final int TYPE_BACK = 2;
    public Path mPath;
    public int type;

    public PathBean(Path path, int type) {
        mPath = path;
        this.type = type;
    }
}
