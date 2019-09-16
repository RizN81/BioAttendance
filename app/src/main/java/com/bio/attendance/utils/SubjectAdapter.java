package com.bio.attendance.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bio.attendance.R;

import java.util.ArrayList;

/**
 * Created by Riz on 2/16/2017.
 */

public class SubjectAdapter extends ArrayAdapter<Subject> {
    Activity context;
    private ArrayList<Subject> subjectList;


    public SubjectAdapter(Activity context, int resource, ArrayList<Subject> subjectList) {
        super(context, resource, subjectList);
        this.context = context;
        this.subjectList = subjectList;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
           return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(R.layout.subject_spinner_item, null);
        TextView lbl = (TextView) convertView.findViewById(R.id.subjectName);
        lbl.setText(subjectList.get(position).getName());
        lbl.setTag(subjectList.get(position).getId());
        return convertView;
    }
}
