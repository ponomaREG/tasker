package com.test.taskcurrent.helpers;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.test.taskcurrent.R;

import java.util.ArrayList;

public class FactoryForWidget implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<String> tasks;
    private ArrayList<Integer> pos_of_done_tasks , pos_of_star_tasks;
    private Context context;
    private int widgetID;
    private DBHelper dbhelper;
    private SharedPreferences sp;

    public FactoryForWidget(Context context, Intent intent){
        this.context = context;
        this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
    }


    @Override
    public void onCreate() {
        tasks = new ArrayList<>();

        dbhelper = new DBHelper(context);
        sp = context.getSharedPreferences(context.getResources().getString(R.string.widget_pref),Context.MODE_PRIVATE);
    }

    @Override
    public void onDataSetChanged() {
        tasks.clear();
        int dayID = sp.getInt(context.getResources().getString(R.string.widget_dayID)+widgetID,-1);
        if(dayID!=-1) {
            Cursor c = dbhelper.getTasksByID(dayID);
            pos_of_done_tasks = new ArrayList<>();
            pos_of_star_tasks = new ArrayList<>();
            for(int i = 0;i<c.getCount();i++) {
                tasks.add(c.getString(c.getColumnIndex(context.getResources().getString(R.string.databaseColumnTask))));
                if(c.getInt(c.getColumnIndex(context.getResources().getString(R.string.databaseColumnIsDone))) == 1) pos_of_done_tasks.add(i);
                if(c.getInt(c.getColumnIndex(context.getResources().getString(R.string.databaseColumnIsStar))) == 1) pos_of_star_tasks.add(i);
                c.moveToNext();
            }
            c.close();
        }else{
            Toast.makeText(context,context.getResources().getString(R.string.toastError_no_tasks),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        rv.setTextViewText(R.id.widget_list_item_tv,tasks.get(position));
        if(pos_of_done_tasks.contains(position)) rv.setInt(R.id.widget_list_item_tv,"setPaintFlags",
                Paint.STRIKE_THRU_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
        else rv.setInt(R.id.widget_list_item_tv,"setPaintFlags",Paint.ANTI_ALIAS_FLAG);
        if(pos_of_star_tasks.contains(position)) rv.setInt(R.id.widget_list_item_tv,"setBackgroundResource",
                R.drawable.widget_drawable_star);
        else rv.setInt(R.id.widget_list_item_tv,"setBackgroundResource",
                R.drawable.widget_list_item);
        Intent clickIntent = new Intent();
        clickIntent.putExtra(context.getResources().getString(R.string.widget_list_itemposition),position);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetID);
        rv.setOnClickFillInIntent(R.id.widget_list_item_tv,clickIntent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
