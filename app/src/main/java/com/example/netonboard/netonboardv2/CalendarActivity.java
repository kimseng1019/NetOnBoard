package com.example.netonboard.netonboardv2;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import cz.msebera.android.httpclient.Header;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class CalendarActivity extends AppCompatActivity {
    private static final String TAG = "CalendarActivity";

    Calendar currentDate;

    MaterialCalendarView calendarView;
    GlobalFileIO fileIO;

    HashMap<CalendarDay, String> hm_calendarDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Company Calendar");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fileIO = new GlobalFileIO(getBaseContext());
        hm_calendarDate = new HashMap<>();

        currentDate = Calendar.getInstance();
        calendarView = (MaterialCalendarView) findViewById(R.id.calendar_view);
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(currentDate.get(Calendar.YEAR), 0, 1))
                .setMaximumDate(CalendarDay.from(currentDate.get(Calendar.YEAR), 11, 31))
                .commit();


        if (isOnline(getBaseContext())) {
            loadCalendar(currentDate.get(Calendar.YEAR));
        } else {
            Toast.makeText(getApplicationContext(), "No connection, loading previous data", Toast.LENGTH_SHORT);
            //TODO LOAD PREVIOUS DATA
            updateCalendar();
        }

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                CalendarDay selectedDate = calendarView.getSelectedDate();
                for (CalendarDay obj :
                        hm_calendarDate.keySet()) {
                    if (selectedDate.equals(obj)) {
                        Toast.makeText(getBaseContext(), hm_calendarDate.get(obj), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void loadCalendar(int year) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://cloudsub04.trio-mobile.com/curl/mobile/calendar/company_holiday.php?y=" + year, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String body = new String(responseBody);
                fileIO.writeToFile(fileIO.FILENAMECALENDAR, body);
                System.out.println(body);
                updateCalendar();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getBaseContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateCalendar() {
        File file = new File(getBaseContext().getFilesDir(), fileIO.FILENAMECALENDAR);
        if (file.exists()) {
            try {
                String body = fileIO.readFile(fileIO.FILENAMECALENDAR);
                JSONObject jsonBody = new JSONObject(body);
                JSONArray response = new JSONArray(jsonBody.getString("response"));
                hm_calendarDate.clear();
                SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonObject = response.getJSONObject(i);
                    Date date = serverDateFormat.parse(jsonObject.getString("d_holiday"));
                    hm_calendarDate.put(new CalendarDay(date), jsonObject.getString("s_remark"));
                }
                calendarView.addDecorator(new EventDecorator(Color.MAGENTA, hm_calendarDate.keySet()));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getBaseContext(), "Calendar file doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(15, color));
        }
    }
}
