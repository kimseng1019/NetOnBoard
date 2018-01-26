package com.example.netonboard.netonboardv2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    private static final String TAG = "CalendarActivity";
    CalendarView calendarView;
    List<EventDay> eventDays = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Company Calendar");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        calendarView = (CalendarView)findViewById(R.id.calendar_view);
        Calendar calendar = Calendar.getInstance();
        eventDays.add(new EventDay(calendar, R.drawable.ic_cloud_off_black_24dp));

        calendarView.setEvents(eventDays);
        Calendar min = Calendar.getInstance();
        calendarView.setMinimumDate(min);
    }



}
