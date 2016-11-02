package com.example.marketing.assistant;

import android.content.Context;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class InstrumentationAdapter extends BaseAdapter {

    private PackageManager mPM;

    protected final Context mContext;
    protected final String mTargetPackage;
    protected final LayoutInflater mInflater;

    protected List<InstrumentationInfo> mList;

    public InstrumentationAdapter(Context context, String targetPackage) {
        mContext = context;
        mTargetPackage = targetPackage;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPM = context.getPackageManager();

        mList = context.getPackageManager().queryInstrumentation(mTargetPackage, 0);
        if (mList != null) {
            Collections.sort(mList, new InstrumentationInfo.DisplayNameComparator(mPM));
        }
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public InstrumentationInfo getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            view = convertView;
        }
        bindView(view, mList.get(position));
        return view;
    }

    private void bindView(View view, InstrumentationInfo info) {
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        CharSequence label = info.loadLabel(mPM);
        text.setText(label != null ? label : info.name);
    }
}
