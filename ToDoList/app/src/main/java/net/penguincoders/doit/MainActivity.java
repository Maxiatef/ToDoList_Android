package net.penguincoders.doit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.penguincoders.doit.Adapters.ToDoAdapter;
import net.penguincoders.doit.Model.ToDoModel;
import net.penguincoders.doit.Utils.DatabaseHandler;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener{

    private DatabaseHandler db;

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    private FloatingActionButton openweb;
    private CheckBox Checked;
    private int count=0;
    private final String countKey = "count";
    private TextView mShowCountTextView;
    private FloatingActionButton Calendar;
    private SharedPreferences mPreferences;
    private String sharedPreFile = "net.penguincoders.doit";
    private List<ToDoModel> taskList;

    @SuppressLint({"MissingInflatedId", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DatabaseHandler(this);
        db.openDatabase();

        openweb = findViewById(R.id.openweb);
        fab = findViewById(R.id.fab);
        Checked = findViewById(R.id.todoCheckBox);

        mShowCountTextView = findViewById(R.id.Counter);
        mPreferences = getSharedPreferences(sharedPreFile, MODE_PRIVATE);
        count = mPreferences.getInt(countKey, 0);
        mShowCountTextView.setText(String.format("%s", count));


        IntentFilter intent = new IntentFilter("android.intent.action.BATTERY_LOW");
        MyReceiver objReceiver = new MyReceiver();
        registerReceiver(objReceiver, intent);


        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db,MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);


        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);


        taskList = db.getAllTasks();
        Collections.reverse(taskList);

        tasksAdapter.setTasks(taskList);


        if ((Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)){
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager= getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,"My Notification");
                builder.setContentTitle("Need To Do");
                builder.setContentText("New Task has been added");
                builder.setSmallIcon(R.drawable.noti);
                builder.setAutoCancel(true);
                NotificationManagerCompat managerCompat= NotificationManagerCompat.from(MainActivity.this);
                managerCompat.notify(1,builder.build());
                count++;
            }
        });

        openweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.briantracy.com/blog/time-management/8-task-management-tips-to-stop-procrastinating-and-get-things-done/")));
            }
        });

        Calendar = findViewById(R.id.calendar);
        Calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent i = new Intent(MainActivity.this, CalendarView.class);
            startActivity(i);
            }
        });




/*
        Checked.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(Checked.isChecked()){
                    count++;
                }
            }
        });

 */

    }

    protected void onPause(){
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(countKey, count);
        preferencesEditor.apply(); //to save
    }

    @Override
    public void handleDialogClose(DialogInterface dialog){
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}