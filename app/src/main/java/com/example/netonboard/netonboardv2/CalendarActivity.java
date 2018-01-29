package com.example.netonboard.netonboardv2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import cz.msebera.android.httpclient.Header;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class CalendarActivity extends AppCompatActivity {
    private static final String TAG = "CalendarActivity";

    //    GridView grid_calendar;
//    ImageView left_imageButton, right_imageButton;
//    TextView tv_month;
    Calendar currentDate;

    MaterialCalendarView calendarView;
    GlobalFileIO fileIO;

    ArrayList<HolidayObject> al_holiday;
    HashSet<CalendarDay> hs_calenderDate;
    HashSet<CalendarDay> al_calendarDay;

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
        al_holiday = new ArrayList<>();
        hs_calenderDate = new HashSet<>();

        currentDate = Calendar.getInstance();
        calendarView = (MaterialCalendarView) findViewById(R.id.calendar_view);
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(currentDate.get(Calendar.YEAR), 0, 1))
                .setMaximumDate(CalendarDay.from(currentDate.get(Calendar.YEAR), 11, 31))
                .commit();


        if (isOnline(getBaseContext())){
            loadCalendar(currentDate.get(Calendar.YEAR));
        }else{
            Toast.makeText(getApplicationContext(), "No connection, loading previous data", Toast.LENGTH_SHORT);
            //TODO LOAD PREVIOUS DATA
            updateCalendar();
        }

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
                Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_SHORT);
            }
        });

    }

    public void updateCalendar(){
        File file = new File(getBaseContext().getFilesDir(), fileIO.FILENAMECALENDAR);
        if(file.exists()){
            try {
                String body = fileIO.readFile(fileIO.FILENAMECALENDAR);
                JSONObject jsonBody = new JSONObject(body);
                JSONArray response = new JSONArray(jsonBody.getString("response"));
                al_holiday.clear();
                hs_calenderDate.clear();
                SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonObject = response.getJSONObject(i);
                    System.out.println(serverDateFormat.parse(jsonObject.getString("d_holiday")));
                    Date date = serverDateFormat.parse(jsonObject.getString("d_holiday"));
                    hs_calenderDate.add(new CalendarDay(date));

                    calendarView.addDecorator(new EventDecorator(3669815, hs_calenderDate));
                }
                System.out.println(hs_calenderDate);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Calendar file doesn't exist", Toast.LENGTH_SHORT);
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private class HolidayObject{
        private int companyHolidayId;
        private String remark;
        private String date;

        public HolidayObject(int companyHolidayId, String remark, String date) {
            this.companyHolidayId = companyHolidayId;
            this.remark = remark;
            this.date = date;
        }

        public int getCompanyHolidayId() {
            return companyHolidayId;
        }

        public String getRemark() {
            return remark;
        }

        public String getDate() {
            return date;
        }
    }
}
