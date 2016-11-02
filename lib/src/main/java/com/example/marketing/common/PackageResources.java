package com.example.marketing.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PackageResources {

    private Context mContext;

    public PackageResources(Context targetContext, String packageName) {
        try {
            mContext = targetContext.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            // Nothing to do!
        }
    }

    /**
     * Return a resource identifier for the given resource name.
     *
     * @param type Optional default resource type to find. if "type/" is
     *             not included in the name.  Can be null to require an
     *             explicit type.
     * @param name The name of the desired resource.
     * @return int The associated resource identifier.  Return 0 if no such
     *         resource was found.  (0 is not a valid resourceID.)
     */
    public int getIdentifier(String type, String name) {
        if (mContext != null) {
            return mContext.getResources().getIdentifier(name, type, mContext.getPackageName());
        }
        return 0;
    }

    public String getString(String name) {
        String str = null;
        if (mContext != null) {
            int id = getIdentifier("string", name);
            if (id > 0) {
                str = mContext.getString(id);
            }
        }
        return str;
    }

    public String getQuantityString(String name, int quantity) {
        String str = null;
        if (mContext != null) {
            int id = getIdentifier("plurals", name);
            if (id > 0) {
                str = mContext.getResources().getQuantityString(id, quantity, quantity);
            }
        }
        return str;
    }

    public Bitmap getBitmap(String name) {
        Bitmap bitmap = null;
        if (mContext != null) {
            int id = getIdentifier("drawable", name);
            if (id > 0) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
            }
        }
        return bitmap;
    }
}
