package com.example.netonboard.netonboardv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Drawer component
    TextView tv_drawer_title, tv_drawer_username;

    PagerAdapter pagerAdapter;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("NetOnBoard");
        toolbar.setElevation(0);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        tv_drawer_username = (TextView) headerView.findViewById(R.id.tv_drawer_username);
        String s_username = LoginActivity.sp_name.getString("key", "");
        tv_drawer_username.setText(s_username);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.support_viewPager);
        setupPagerAdapter(viewPager);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.support_tab);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void setupPagerAdapter(ViewPager viewPager){
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SupportMain(), "Home");
        adapter.addFragment(new SupportServerDown(), "Server Down");
        adapter.addFragment(new SupportServerHistory(), "Server History");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_setting) {
            startActivity(new Intent(NavActivity.this, SettingsActivity.class));
            return true;
        } else if(id == R.id.action_logout){
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_calendar) {
            startActivity(new Intent(NavActivity.this, CalendarActivity.class));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        LoginActivity.is_login = false;
        stopService(new Intent(this, BackgroundService.class));
        LoginActivity.sp_name = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        LoginActivity.sp_editor = LoginActivity.sp_name.edit();
        LoginActivity.sp_editor.clear();
        LoginActivity.sp_editor.commit();

        PasscodeActivity.sharedPreferences = getSharedPreferences(PasscodeActivity.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = PasscodeActivity.sharedPreferences.edit();
        editor.clear();
        editor.commit();
        stopService(new Intent(NavActivity.this, BackgroundService.class));
        finish();
    }
}
