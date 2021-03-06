package com.dudeonfireandCO.vipul.mytodolist;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import com.example.vipul.mytodolist.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SearchResultListviewActivity extends Activity {
    private ArrayList<MyObject> names;
    private String[] ids;
    private int i=0;
    private SQLiteDatabase db;
    private ListView searchListUpcoming;
    private ListView searchListRunning;
    private ListView searchListCompleted;
    private ArrayList<MyObject> taskArrayUpcoming;
    private ArrayList<MyObject> taskArrayRunning;
    private ArrayList<MyObject> taskArrayCompleted;
    private TabHost tabHost;
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        names = (ArrayList<MyObject>)getIntent().getExtras().get("ArrayList");
        ids = new String[names.size()];
        for(MyObject o:names){
            ids[i] = Integer.toString(o.getId());
            i++;
        }

        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Upcoming List");
        tabSpec.setContent(R.id.task_listview_upcoming);
        tabSpec.setIndicator("Upcoming");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Running List");
        tabSpec.setContent(R.id.task_listview_running);
        tabSpec.setIndicator("Running");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Completed List");
        tabSpec.setContent(R.id.task_listview_completed);
        tabSpec.setIndicator("Completed");
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);

        tabHost.setOnTabChangedListener(new AnimatedTabHostListener(getApplicationContext(), tabHost));

        gestureDetector = new GestureDetector(this,new MyGestureDetector());

        searchListCompleted = (ListView)findViewById(R.id.task_listview_completed);
        searchListRunning = (ListView)findViewById(R.id.task_listview_running);
        searchListUpcoming = (ListView)findViewById(R.id.task_listview_upcoming);

        searchListRunning.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        searchListUpcoming.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        searchListCompleted.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        new UpdateTaskListTask().execute();

        searchListUpcoming.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchResultListviewActivity.this, TaskDetailActivity.class);
                int c = taskArrayUpcoming.get(position).getId();
                intent.putExtra(TaskDetailActivity.EXTRA_ROW, c);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        searchListRunning.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchResultListviewActivity.this, TaskDetailActivity.class);
                int c = taskArrayRunning.get(position).getId();
                intent.putExtra(TaskDetailActivity.EXTRA_ROW, c);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        searchListCompleted.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchResultListviewActivity.this, TaskDetailActivity.class);
                int c = taskArrayCompleted.get(position).getId();
                intent.putExtra(TaskDetailActivity.EXTRA_ROW, c);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.anim_leave, R.anim.anim_enter);
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        private static final int SWIPE_MIN_DISTANCE = 100;
        private static final int SWIPE_MAX_OFF_PATH = 80;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;
        private int maxTabs;

        /**
         * An empty constructor that uses the tabhosts content view to decide how many tabs there are.
         */
        public MyGestureDetector(){
            maxTabs = tabHost.getTabContentView().getChildCount();
        }

        /**
         * Listens for the onFling event and performs some calculations between the touch down point and the touch up
         * point. It then uses that information to calculate if the swipe was long enough. It also uses the swiping
         * velocity to decide if it was a "true" swipe or just some random touching.
         */
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY){
            int newTab = 0;
            if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH){
                return false;
            }
            if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
                // Swipe right to left
                newTab = tabHost.getCurrentTab() + 1;
            }
            else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
                // Swipe left to right
                newTab = tabHost.getCurrentTab() - 1;
            }
            if (newTab < 0 || newTab > (maxTabs - 1)){
                return false;
            }
            tabHost.setCurrentTab(newTab);
            return super.onFling(event1, event2, velocityX, velocityY);
        }
    }

    private class UpdateTaskListTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteOpenHelper taskDatabaseHelper = new TODOListDatabaseHelper(SearchResultListviewActivity.this);
            db = taskDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("NEWTASK6", new String[]{"_id", "TASK_NAME", "PRIORITY","MODIFIED_DATE","START_DATE","END_DATE","START_TIME","END_TIME","IMAGE","REPEAT_TASK","REPEAT_AFTER","REMINDER","DESCRIPTION"}, null, null, null, null, "_id DESC");
            taskArrayRunning = new ArrayList<MyObject>();
            taskArrayUpcoming = new ArrayList<MyObject>();
            taskArrayCompleted = new ArrayList<MyObject>();
            Date cur = new Date();
            i=0;
            while(cursor.moveToNext()){
                if(Integer.parseInt(ids[i])==cursor.getInt(0)) {
                    SimpleDateFormat f = new SimpleDateFormat("dd/MM/yy HH:mm");
                    Date start = null, end = null;
                    try {
                        start = f.parse(cursor.getString(4) + " " + cursor.getString(6));
                        end = f.parse(cursor.getString(5) + " " + cursor.getString(7));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (cur.compareTo(start) < 0)
                        taskArrayUpcoming.add(new MyObject(cursor.getInt(0), cursor.getInt(2), cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(9), cursor.getString(10), cursor.getString(12), cursor.getBlob(8), cursor.getInt(11)));
                    else if (cur.compareTo(end) > 0)
                        taskArrayCompleted.add(new MyObject(cursor.getInt(0), cursor.getInt(2), cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(9), cursor.getString(10), cursor.getString(12), cursor.getBlob(8), cursor.getInt(11)));
                    else
                        taskArrayRunning.add(new MyObject(cursor.getInt(0), cursor.getInt(2), cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(9), cursor.getString(10), cursor.getString(12), cursor.getBlob(8), cursor.getInt(11)));
                    i++;
                    if(i==ids.length)
                        break;
                }
            }
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            TaskAdapter taskAdapterRunning = new TaskAdapter(SearchResultListviewActivity.this,R.layout.listview_layout,taskArrayRunning);
            TaskAdapter taskAdapterCompleted = new TaskAdapter(SearchResultListviewActivity.this,R.layout.listview_layout,taskArrayCompleted);
            TaskAdapter taskAdapterUpcoming = new TaskAdapter(SearchResultListviewActivity.this,R.layout.listview_layout,taskArrayUpcoming);
            searchListRunning.setAdapter(taskAdapterRunning);
            searchListCompleted.setAdapter(taskAdapterCompleted);
            searchListUpcoming.setAdapter(taskAdapterUpcoming);
            db.close();
        }
    }

}
