package com.example.sridh.vdiary;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sridh on 10/15/2016.
 */

    class dayListAdapter extends BaseAdapter {
    List<subject> list;
    Context context;
    public static View rowView;
    public static LayoutInflater inflater =null;
    public dayListAdapter(Context c , List<subject> subList){
        list=subList;
        context=c;
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        rowView=inflater.inflate(R.layout.timetablelistlayout,null);
        TextView titleText =(TextView)rowView.findViewById(R.id.title);
        titleText.setText(list.get(position).title);
        TextView typeText=((TextView)rowView.findViewById(R.id.type));
        typeText.setText(list.get(position).type);
        if(typeText.getText().toString().equals("")){
            typeText.setVisibility(View.INVISIBLE);
            titleText.setTextColor(context.getResources().getColor(R.color.Slight_white_orange));
        }
        return rowView;
    }
}

    class  allSubjectAdapter extends BaseAdapter {
    List<subject> allSub;
    Context context;
    public static View rowView;
    public static LayoutInflater inflater =null;

    public allSubjectAdapter(Context c, List<subject> subjectList){
        context=c;
        allSub=subjectList;
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return allSub.size();
    }

    @Override
    public Object getItem(int position) {
        return allSub.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        rowView=inflater.inflate(R.layout.all_subject_list_layout,null);
        ((TextView)rowView.findViewById(R.id.subName)).setText(allSub.get(position).title);
        ((TextView)rowView.findViewById(R.id.subAtt)).setText(allSub.get(position).attString);
        ((TextView)rowView.findViewById(R.id.subTeacher)).setText(allSub.get(position).teacher);
        ((TextView)rowView.findViewById(R.id.subType)).setText(allSub.get(position).type);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showSubject= new Intent(context,show_subject.class);
                showSubject.putExtra("position",position);
                context.startActivity(showSubject);
            }
        });
        return rowView;
    }
}

    class todoAdapter extends BaseAdapter{

        List<todo> todoList;
        Context context;
        public static View rowView;
        public static LayoutInflater inflater =null;

        public todoAdapter(Context c, List<todo> t){
            todoList= t;
            context =c;
            inflater= (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return todoList.size();
        }

        @Override
        public Object getItem(int position) {
            return todoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            rowView= inflater.inflate(R.layout.todo_list_layout,null);
            todo t = todoList.get(position);
            ((TextView)rowView.findViewById(R.id.todoTitle)).setText(t.title);
            ((TextView)rowView.findViewById(R.id.tododeadLine)).setText(t.deadLineDate.toString()+t.deadLineTime.toString());
            return rowView;
        }
    }

