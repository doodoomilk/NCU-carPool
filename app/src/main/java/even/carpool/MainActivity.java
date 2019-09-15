package even.carpool;

/*
Tab頁面 設定
Floating button
側邊欄的設定
DrawerLayout
NavigationView
 */

import android.app.ActivityGroup;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TabHost;
import com.firebase.client.Firebase;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import static even.carpool.LoginActivity.accountField;
import static even.carpool.LoginActivity.data;
import static even.carpool.LoginActivity.departmentField;
import static even.carpool.LoginActivity.emailField;
import static even.carpool.LoginActivity.gradeField;
import static even.carpool.LoginActivity.idField;
import static even.carpool.LoginActivity.impeachField;
import static even.carpool.LoginActivity.info_list_outside;
import static even.carpool.LoginActivity.nameField;
import static even.carpool.LoginActivity.phoneField;
import static even.carpool.LoginActivity.rateField;
import static even.carpool.LoginActivity.rate_numField;
import static even.carpool.LoginActivity.settings;
import static even.carpool.LoginActivity.sexField;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //add_new_missionActivity idclass = new add_new_missionActivity();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private TextView nav_user_name, nav_user_account;

    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    //DatabaseReference MissionRef = database.getReference("Mission");
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("NCU CarPool System");

        OfficialCountManTimeThread official_count_mantime_thread = new  OfficialCountManTimeThread();
        official_count_mantime_thread.start();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 側邊欄的header view
        View headerLayout = navigationView.getHeaderView(0);

        // 設定當前使用者帳號和名字
        nav_user_name = (TextView) headerLayout.findViewById(R.id.user_name);
        nav_user_account = (TextView) headerLayout.findViewById(R.id.user_email);

        settings = getSharedPreferences(data, 0);
        nav_user_name.setText(settings.getString(nameField,""));
        nav_user_account.setText(settings.getString(accountField,""));



        //new a object of TabHost ,get xml TabHost and initialize
        TabHost tabHost = (TabHost) findViewById(R.id.tab_host);
        LocalActivityManager mLocalActivityManager = new LocalActivityManager(this, false);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        tabHost.setup(mLocalActivityManager);

        //set 1st tab page, and set its text
        TabHost.TabSpec spec = tabHost.newTabSpec("找共乘");
        //連接到第一個tab去
        spec.setContent(new Intent(this, mission_list.class));
        spec.setIndicator("找共乘",
                getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
        //set tab page to the object
        tabHost.addTab(spec);

        /*TabHost.TabSpec spec2 = tabHost.newTabSpec("好友");
        spec2.setContent(new Intent(this, friends_list.class));
        spec2.setIndicator("好友",
                getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
        tabHost.addTab(spec2);
        */
        TabHost.TabSpec spec3 = tabHost.newTabSpec("我的共乘");
        spec3.setContent(new Intent(this, my_list.class));
        spec3.setIndicator("我的共乘",
                getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
        tabHost.addTab(spec3);

        //set the first tab when we open the app
        tabHost.setCurrentTab(0);

        FloatingActionButton newMission = (FloatingActionButton) findViewById(R.id.newMission);
        newMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //////change to post mission page
                Intent add_newmission = new Intent();
                add_newmission.setClass(MainActivity.this,add_new_missionActivity.class);
                startActivity(add_newmission);


            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();





        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();




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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // Handle the camera action
        if (id == R.id.nav_logout) {
            // 清除登入資訊
            settings = getSharedPreferences(data,0);
            settings.edit().clear().commit();
            info_list_outside.clear();

            // 跳到登入畫面
            Intent login = new Intent();
            login.setClass(this,LoginActivity.class);
            startActivity(login);

        } else if (id == R.id.nav_accountManage) {
            // 跳到個人資料畫面
            Dialog dialog = new Dialog(MainActivity.this);
            // dialog 的版面配置
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box);
            dialog.setContentView(R.layout.dialog_info);

            dialog.setTitle("個人資料");

            // dismiss when touching outside Dialog
            // 可以返回
            dialog.setCancelable(true);

            TextView info_name, info_sex, info_department, info_grade, info_phone, info_email, info_rate, info_impeach;
            Button ok;
            info_name = (TextView) dialog.findViewById(R.id.info_name);
            info_sex = (TextView) dialog.findViewById(R.id.info_sex);
            info_department = (TextView) dialog.findViewById(R.id.info_department);
            info_grade = (TextView) dialog.findViewById(R.id.info_grade);
            info_phone = (TextView) dialog.findViewById(R.id.info_phone);
            info_email = (TextView) dialog.findViewById(R.id.info_email);
            info_rate = (TextView) dialog.findViewById(R.id.info_rate);
            info_impeach = (TextView) dialog.findViewById(R.id.info_impeach);
            ok = (Button) dialog.findViewById(R.id.ok);

            settings =getSharedPreferences(data,0);
            String name = settings.getString(nameField, "");
            String sex = settings.getString(sexField, "");
            String department = settings.getString(departmentField, "");
            String grade = settings.getString(gradeField, "");
            String phone = settings.getString(phoneField, "");
            String email = settings.getString(emailField, "");
            String rate = settings.getString(rateField, "");
            String ratenum = settings.getString(rate_numField, "");
            String impeach = settings.getString(impeachField, "");

            info_name.setText("姓名： " + name);
            info_sex.setText("性別： " + sex);
            info_department.setText("科系： " + department);
            info_grade.setText("年級： " + grade + "年級");
            info_phone.setText("電話： " + phone );
            info_email.setText("email： " + email);
            info_rate.setText("評價： " + rate + "顆星" + "/" + ratenum);
            info_impeach.setText("棄標次數： " + impeach + "次");

            ok.setOnClickListener(onOkListener(dialog));
            dialog.show();

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private View.OnClickListener onOkListener(final Dialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://even.carpool/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://even.carpool/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
    protected void onResume() {
        super.onResume();
    }

}

