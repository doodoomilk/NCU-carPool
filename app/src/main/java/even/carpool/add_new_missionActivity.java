package even.carpool;

/*
    user add new mission to the app
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import android.util.Log;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.widget.Button;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import static even.carpool.LoginActivity.data;
import static even.carpool.LoginActivity.departmentField;
import static even.carpool.LoginActivity.nameField;
import static even.carpool.LoginActivity.settings;
import static even.carpool.LoginActivity.sexField;
import static even.carpool.LoginActivity.user_account;
import static even.carpool.LoginActivity.user_department;
import static even.carpool.LoginActivity.user_name;


public class add_new_missionActivity extends AppCompatActivity {

    /*
       array of spinner's data
     */

    String[] str_year = {"106","107","108","109","110","111","112","113","114","115","116"};
    String[] str_month = {"01","02","03","04","05","06","07","08","09","10","11","12"};
    String[] str_day = {"01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28","29","30","31"};
    String[] str_day2 = {"01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28","29","30"};
    String[] str_day_Feb = {"01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28"};
    String[] str_day_Feb2= {"01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28","29"};
    String[] str_hour = {"00","01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23"};
    String[] str_min = {"00","01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28","29","30","31","32","33","34","35","36","37","38",
            "39","40","41","42","43","44","45","46","47","48","49","50","51","52","53",
            "54","55","56","57","58","59"};
    String[] str_location = {"中壢火車站","桃園高鐵站","中央大學","台北車站"};
    String[] str_people = {"1","2","3"};

    /*
        some variables use in insert data to db
     */
    protected String mis_year, mis_month, mis_day, mis_hour, mis_min, mis_start, mis_destination, mis_people;
    private EditText txtothers, txtothers2, txtcom;

    //spinner
    private Spinner spinner_year, spinner_month, spinner_day, spinner_hour, spinner_min, spinner_start, spinner_desti, spinner_people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_mission);

        txtothers = (EditText)findViewById(R.id.txtothers);
        txtothers2 = (EditText)findViewById(R.id.txtothers2);
        txtcom = (EditText)findViewById(R.id.txtcom);

        spinner_year = (Spinner)findViewById(R.id.spinner_year);
        spinner_month = (Spinner)findViewById(R.id.spinner_month);
        spinner_day = (Spinner)findViewById(R.id.spinner_day);
        spinner_hour = (Spinner)findViewById(R.id.spinner_hour);
        spinner_min = (Spinner)findViewById(R.id.spinner_min);
        spinner_start = (Spinner)findViewById(R.id.spinner_start);
        spinner_desti = (Spinner)findViewById(R.id.spinner_desti);
        spinner_people = (Spinner)findViewById(R.id.spinner_people);


        /*
        date 的spinner 處理
        get the data from spinner
        每月天數 配置
         */

        ArrayAdapter yearList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_year);
        final ArrayAdapter monthList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_month);
        final ArrayAdapter day_Feb2List = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_day_Feb2); //29day
        final ArrayAdapter day2List = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_day2); //30 day
        final ArrayAdapter dayList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_day); //31 day
        final ArrayAdapter day_FebList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_day_Feb); //28day
        spinner_year.setAdapter(yearList);

        spinner_year.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                mis_year = str_year[num];

                spinner_month.setAdapter(monthList);
                spinner_month.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                        mis_month = str_month[num];

                        if (Integer.parseInt(mis_year)==109 || Integer.parseInt(mis_year)==113)
                        {
                            if (Integer.parseInt(mis_month)==2)
                            {
                                spinner_day.setAdapter(day_Feb2List);
                            }
                            else if (Integer.parseInt(mis_month)==4 ||
                                    Integer.parseInt(mis_month)==6 ||
                                    Integer.parseInt(mis_month)==9 ||
                                    Integer.parseInt(mis_month)==11)
                            {
                                spinner_day.setAdapter(day2List);
                            }
                            else
                            {
                                spinner_day.setAdapter(dayList);
                            }
                        }
                        else
                        {
                            if (Integer.parseInt(mis_month)==2)
                            {
                                spinner_day.setAdapter(day_FebList);
                            }
                            else if (Integer.parseInt(mis_month)==4 ||
                                    Integer.parseInt(mis_month)==6 ||
                                    Integer.parseInt(mis_month)==9 ||
                                    Integer.parseInt(mis_month)==11){
                                spinner_day.setAdapter(day2List);
                            }
                            else
                            {
                                spinner_day.setAdapter(dayList);
                            }
                        }

                        spinner_day.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                                mis_day = str_day[num];
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        /*
        其他 的spinner 處理
         */

        ArrayAdapter hourList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_min);
        spinner_hour.setAdapter(hourList);
        ArrayAdapter minList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_min);
        spinner_min.setAdapter(minList);
        ArrayAdapter startList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_location);
        spinner_start.setAdapter(startList);
        ArrayAdapter destiList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_location);
        spinner_desti.setAdapter(destiList);
        ArrayAdapter peopleList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_people);
        spinner_people.setAdapter(peopleList);




        // get the data from spinner
        spinner_hour.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                mis_hour = str_hour[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner_min.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                mis_min = str_min[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        /*
        起點終點用代號傳
         */
        spinner_start.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                mis_start = Integer.toString(num);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner_desti.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                mis_destination = Integer.toString(num);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner_people.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                mis_people = str_people[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ////確認送出 button
        Button newmissionOk = (Button) findViewById(R.id.newmission_ok_button);
        newmissionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 取出變數
                String mis_others = txtothers.getText().toString();
                String mis_others2 = txtothers2.getText().toString();
                String mis_comment = txtcom.getText().toString();

                //取出當前的user
                settings =getSharedPreferences(data,0);
                String name = settings.getString(nameField, "");
                String department = settings.getString(departmentField, "");
                String sex = settings.getString(sexField, "");

                String static_people = mis_people;

                //呼叫 new class 去 insert data
                new insert_newmissionActivity().execute(department, name, sex, mis_year, mis_month, mis_day, mis_hour, mis_min,
                                                mis_start, mis_others, mis_destination, mis_others2, mis_people, mis_comment, static_people);

                // jump to main activity
                Intent mission_success = new Intent();
                mission_success.setClass(add_new_missionActivity.this, MainActivity.class);
                startActivity(mission_success);

            }
        });
    }

    /*
    new mission 的資料 insert to db
     */

    public class insert_newmissionActivity extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){

        }

        protected String doInBackground(String... arg0)
        {
            //將參數置入變數
            String department = arg0[0];
            String name= arg0[1];
            String sex= arg0[2];
            String year= arg0[3];
            String month = arg0[4];
            String day = arg0[5];
            String hour = arg0[6];
            String min = arg0[7];
            String start = arg0[8];
            String start_others = arg0[9];
            String destination = arg0[10];
            String des_others = arg0[11];
            String people = arg0[12];
            String comment = arg0[13];
            String static_people = arg0[14];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("department", department));
            nameValuePairs.add(new BasicNameValuePair("name", name));
            nameValuePairs.add(new BasicNameValuePair("sex", sex));
            nameValuePairs.add(new BasicNameValuePair("year", year));
            nameValuePairs.add(new BasicNameValuePair("month", month));
            nameValuePairs.add(new BasicNameValuePair("day", day));
            nameValuePairs.add(new BasicNameValuePair("hour", hour));
            nameValuePairs.add(new BasicNameValuePair("min", min));
            nameValuePairs.add(new BasicNameValuePair("start", start));
            nameValuePairs.add(new BasicNameValuePair("start_others", start_others));
            nameValuePairs.add(new BasicNameValuePair("destination", destination));
            nameValuePairs.add(new BasicNameValuePair("des_others", des_others));
            nameValuePairs.add(new BasicNameValuePair("people", people));
            nameValuePairs.add(new BasicNameValuePair("comment", comment));
            nameValuePairs.add(new BasicNameValuePair("static_people", static_people));

            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/missionlist_insert.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();

            }
            catch (Exception e){
               // Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return "mission insert success";
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }


}
