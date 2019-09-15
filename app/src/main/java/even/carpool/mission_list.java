package even.carpool;

/*
第一個tab分頁 顯示mission清單
Recycler view
card view
詳細資料dialog
排序
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

/**
 * Created by even on 2016/12/11.
 */

public class mission_list extends Activity {

    ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();

    private Spinner spinner_sort_year, spinner_sort_month, spinner_sort_day, spinner_sort_start, spinner_sort_desti;
    String[] str_year = {"---","106","107"};
    String[] str_month = {"---","01","02","03","04","05","06","07","08","09","10","11","12"};
    String[] str_day = {"---","01","02","03","04","05","06","07","08","09","10","11","12",
            "13","14","15","16","17","18","19","20","21","22","23","24","25",
            "26","27","28","29","30","31"};
    String[] str_location = {"---","中壢火車站","桃園高鐵站","中央大學","台北車站"};
    String year="0", month="0", day="0", start="0", desti="0";
    String sort_year, sort_month, sort_day, sort_desti, sort_start;
    private Button btn_sort;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private MissionAdapter mAdapter;

    //check passenger
    int check_p = 0;


    int c_year,c_month,c_day,c_hour,c_minute;
    String sc_year="",sc_month="",sc_day="",sc_hour="",sc_minute="";
    String time="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mission_list);


        OfficialCountManTimeThread official_count_mantime_thread = new  OfficialCountManTimeThread();
        official_count_mantime_thread.start();


        /*sc_year = Integer.toString(c_year);
        if (c_month<10){
            sc_month = "0"+Integer.toString(c_month);
           // c_month = Integer.valueOf(sc_month);
        }else{
            sc_month = Integer.toString(c_month);
        }
        if (c_day<10){
            sc_day = "0" +Integer.toString(c_day);
            //c_day = Integer.valueOf(sc_day);
        }else{
            sc_day = Integer.toString(c_day);
        }
        sc_hour = Integer.toString(c_hour);
        sc_minute = Integer.toString(c_minute);

        time=sc_year+"-"+sc_month+"-"+sc_day+" "+sc_hour+":"+sc_minute;*/


        btn_sort = (Button) findViewById(R.id.btn_sort);
        spinner_sort_year = (Spinner)findViewById(R.id.spinner_sort_year);

        /*
        如果是0表示沒有選擇排序項目
        1表示有選擇
         */
        ArrayAdapter yearList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_year);
        spinner_sort_year.setAdapter(yearList);
        spinner_sort_year.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                if (num!=0){
                    year = "1";
                }else {
                    year = "0";
                }
                sort_year = str_year[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                year = "0";
                sort_year = "0";
            }
        });

        spinner_sort_month = (Spinner)findViewById(R.id.spinner_sort_month);
        ArrayAdapter monthList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_month);
        spinner_sort_month.setAdapter(monthList);
        spinner_sort_month.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                if (num!=0){
                    month = "1";
                }else {
                    month = "0";
                }
                sort_month = str_month[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                month = "0";
                sort_month = "0";
            }
        });

        spinner_sort_day = (Spinner)findViewById(R.id.spinner_sort_day);
        ArrayAdapter dayList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_day);
        spinner_sort_day.setAdapter(dayList);
        spinner_sort_day.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                if (num!=0){
                    day = "1";
                }
                else {
                    day = "0";
                }
                sort_day = str_day[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                day = "0";
                sort_day = "0";
            }
        });

        spinner_sort_start = (Spinner)findViewById(R.id.spinner_sort_start);
        ArrayAdapter startList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_location);
        spinner_sort_start.setAdapter(startList);
        spinner_sort_start.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                if (num!=0){
                    start = "1";
                    num--;
                }
                else {
                    start = "0";
                }
                sort_start = Integer.toString(num);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                start = "0";
                sort_start = "0";
            }
        });

        spinner_sort_desti = (Spinner)findViewById(R.id.spinner_sort_desti);
        ArrayAdapter destiList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_location);
        spinner_sort_desti.setAdapter(destiList);
        spinner_sort_desti.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                if (num!=0){
                    desti = "1";
                    num--;
                }
                else {
                    desti = "0";
                }
                sort_desti = Integer.toString(num);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                desti = "0";
                sort_desti = "0";
            }
        });



        btn_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                new getActivity().execute(year, month, day, start, desti,sort_year, sort_month, sort_day, sort_start, sort_desti);
            }
        });

        settings =getSharedPreferences(data,0);
        String name = settings.getString(nameField, "");
        final String account = settings.getString(accountField, "");
        String id = settings.getString(idField, "");
        String sex = settings.getString(sexField, "");
        String department = settings.getString(departmentField, "");
        String grade = settings.getString(gradeField,"");
        String phone = settings.getString(phoneField, "");
        String email = settings.getString(emailField, "");
        String rate_num = settings.getString(rate_numField, "");
        String rate = settings.getString(rateField, "");
        String impeach = settings.getString(impeachField, "");


        //下拉刷新
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.layout_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            public void onRefresh() {
                OfficialCountManTimeThread official_count_mantime_thread = new  OfficialCountManTimeThread();
                official_count_mantime_thread.start();


                //先清空list
                list.clear();
                info_list_outside.clear();
                new get_user_info().execute(account);
                //再重新進行get data動作 才不會導致資料重複get
                new getActivity().execute(year, month, day, start, desti,sort_year, sort_month, sort_day, sort_start, sort_desti);
                //数据重新加载完成后，提示数据发生改变，并且设置现在不在刷新
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
            }
        });

        // 呼叫 new class of get data
        new getActivity().execute(year, month, day, start, desti,sort_year, sort_month, sort_day, sort_start, sort_desti);

    }


    public class get_user_info extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
        }

        protected String doInBackground(String... arg0)
        {

            String result="";

            //將參數置入變數
            String input_account= arg0[0];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("input_account", input_account));

            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/get_user_info.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();          //取得收到的內容

                ////將剛剛所取得的Content利用StringBuilder轉換為字串
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder builder = new StringBuilder();
                String line = null;
                while((line = bufReader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                inputStream.close();
                result = builder.toString();
            }
            catch (Exception e){
                //Log.e("Wrong~~","catch~~");
            }
            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    String j_id = jsonData.getString("id");
                    String j_name = jsonData.getString("name");
                    String j_sex = jsonData.getString("sex");
                    String j_department = jsonData.getString("department");
                    String j_grade = jsonData.getString("grade");
                    String j_phone = jsonData.getString("phone");
                    String j_email = jsonData.getString("email");
                    String j_ratenum = jsonData.getString("rate_num");
                    String j_rate = jsonData.getString("rate");
                    String j_impeach = jsonData.getString("impeach");

                    Double r = Double.parseDouble(j_rate);
                    BigDecimal bd = new BigDecimal(r);
                    bd = bd.setScale(1, RoundingMode.HALF_UP);
                    j_rate = bd.toString();

                    HashMap<String, String> info_list = new HashMap<>();
                    info_list.put("id", j_id);
                    info_list.put("name", j_name);
                    info_list.put("sex", j_sex);
                    info_list.put("department", j_department);
                    info_list.put("grade", j_grade);
                    info_list.put("phone", j_phone);
                    info_list.put("email", j_email);
                    info_list.put("rate_num", j_ratenum);
                    info_list.put("rate", j_rate);
                    info_list.put("impeach", j_impeach);

                    info_list_outside.add(info_list);
                }


                // 記錄user 資料
                settings = getSharedPreferences(data,0);
                settings.edit()
                        .putString(idField, info_list_outside.get(0).get("id"))
                        .putString(nameField, info_list_outside.get(0).get("name"))
                        .putString(sexField, info_list_outside.get(0).get("sex"))
                        .putString(departmentField, info_list_outside.get(0).get("department"))
                        .putString(gradeField, info_list_outside.get(0).get("grade"))
                        .putString(phoneField, info_list_outside.get(0).get("phone"))
                        .putString(emailField, info_list_outside.get(0).get("email"))
                        .putString(rate_numField, info_list_outside.get(0).get("rate_num"))
                        .putString(rateField, info_list_outside.get(0).get("rate"))
                        .putString(impeachField, info_list_outside.get(0).get("impeach"))
                        .commit();


            }
            catch (Exception e){
               //Log.e("catch data Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

        }
    }


    /*
    接收 mysql上的mission資料
     */
    public class getActivity extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        /*
        connect to db
        get the result of data
         */
        protected String doInBackground(String... arg0) {
            String year = arg0[0];
            String month= arg0[1];
            String day= arg0[2];
            String start= arg0[3];
            String desti = arg0[4];
            String sort_year = arg0[5];
            String sort_month = arg0[6];
            String sort_day = arg0[7];
            String sort_start = arg0[8];
            String sort_desti = arg0[9];
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("year", year));
                nameValuePairs.add(new BasicNameValuePair("month", month));
                nameValuePairs.add(new BasicNameValuePair("day", day));
                nameValuePairs.add(new BasicNameValuePair("start", start));
                nameValuePairs.add(new BasicNameValuePair("desti", desti));
                nameValuePairs.add(new BasicNameValuePair("sort_year", sort_year));
                nameValuePairs.add(new BasicNameValuePair("sort_month", sort_month));
                nameValuePairs.add(new BasicNameValuePair("sort_day", sort_day));
                nameValuePairs.add(new BasicNameValuePair("sort_start", sort_start));
                nameValuePairs.add(new BasicNameValuePair("sort_desti", sort_desti));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/missionlist_get.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();          //取得收到的內容


                ////將剛剛所取得的Content利用StringBuilder轉換為字串
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = bufReader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                inputStream.close();
                result = builder.toString();
                //Log.e(year, month);
                //Log.e(day, start);
                //Log.e(desti, "hi");

            } catch (Exception e) {
                //Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return result;
        }

        /*
        convert result of data into each variables
        並對應到php的相對應變數上
         */
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    String j_mission_department = jsonData.getString("department");
                    String j_mission_name = jsonData.getString("name1");
                    String j_sex = jsonData.getString("sex");
                    String j_year = jsonData.getString("year");
                    String j_month = jsonData.getString("month");
                    String j_day = jsonData.getString("day");
                    String j_hour = jsonData.getString("hour");
                    String j_min = jsonData.getString("min");
                    String j_start = "";
                    if (Integer.parseInt(jsonData.getString("start"))==0){
                        j_start = "中壢火車站";
                    }
                    else if (Integer.parseInt(jsonData.getString("start"))==1){
                        j_start = "桃園高鐵站";
                    }
                    else if (Integer.parseInt(jsonData.getString("start"))==2){
                        j_start = "中央大學";
                    }
                    else if (Integer.parseInt(jsonData.getString("start"))==3){
                        j_start = "台北車站";
                    }
                    String j_start_others = jsonData.getString("start_others");
                    String j_destination = "";
                    if (Integer.parseInt(jsonData.getString("destination"))==0){
                        j_destination = "中壢火車站";
                    }
                    else if (Integer.parseInt(jsonData.getString("destination"))==1){
                        j_destination = "桃園高鐵站";
                    }
                    else if (Integer.parseInt(jsonData.getString("destination"))==2){
                        j_destination = "中央大學";
                    }
                    else if (Integer.parseInt(jsonData.getString("destination"))==3){
                        j_destination = "台北車站";
                    }
                    String j_des_others = jsonData.getString("des_others");
                    String j_people = jsonData.getString("people");
                    String j_comment = jsonData.getString("comment");
                    String j_id = jsonData.getString("id");
                    String j_ratenum = jsonData.getString("rate_num");
                    String j_rate = jsonData.getString("rate");
                    String j_impeach = jsonData.getString("impeach");

                    Double r = Double.parseDouble(j_rate);
                    BigDecimal bd = new BigDecimal(r);
                    bd = bd.setScale(1, RoundingMode.HALF_UP);
                    j_rate = bd.toString();


                    // 依照一筆一筆資料存進hashmap
                    // 若要第一筆則 list.get(0).get("year")
                    // 若要第二筆則 list.get(1).get("year")
                    HashMap<String, String> mission_detail = new HashMap<>();
                    mission_detail.put("department", j_mission_department);
                    mission_detail.put("name", j_mission_name);
                    mission_detail.put("sex", j_sex);
                    mission_detail.put("year", j_year);
                    mission_detail.put("month", j_month);
                    mission_detail.put("day", j_day);
                    mission_detail.put("hour", j_hour);
                    mission_detail.put("min", j_min);
                    mission_detail.put("start", j_start);
                    mission_detail.put("start_others", j_start_others);
                    mission_detail.put("destination", j_destination);
                    mission_detail.put("des_others", j_des_others);
                    mission_detail.put("people", j_people);
                    mission_detail.put("comment", j_comment);
                    mission_detail.put("id", j_id);
                    mission_detail.put("rate_num", j_ratenum);
                    mission_detail.put("rate", j_rate);
                    mission_detail.put("impeach", j_impeach);
                    list.add(mission_detail);
                }

                //Log.e(list.get(0).get("start"), list.get(0).get("destination"));
                //Log.e(sort_start,sort_desti);

                /*Log.e(list.get(1).get("start"), list.get(1).get("destination"));
                Log.e(list.get(2).get("start"), list.get(2).get("destination"));*/


                // 呼叫 recycler view 版面
                recycle_view();
            } catch (Exception e) {
                /*
                沒有data時 也要處理
                 */
                list.clear();
                recycle_view();
                Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    }

    // 設定recyclerView
    public void recycle_view(){
        //RecyclerView

        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //RecyclerView實作
        mRecyclerView.setAdapter(mAdapter = new MissionAdapter());
    }


    /*
    RecyclerView實作
    並get data from db
    由mysql id 大的開始取 因為最新的資料要在最上面
    （but 注意 最新的資料 position(index) 會最小 因為最先放入list）
     */
    class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MyViewHolder>
    {


        //inflate layout of mission, the form of missionlist card
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // few_detail 是 cardView的顯示形式
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mission_list.this)
                    .inflate(R.layout.few_detail, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position)
        {
            // 取得現在的position in recyclerView
            // 將get到的資料放入
            MyViewHolder myHolder= holder;

            Calendar c = Calendar.getInstance();

            c_year = c.get(Calendar.YEAR)-1911;
            c_month = c.get(Calendar.MONTH)+1;
            c_day = c.get(Calendar.DAY_OF_MONTH);
            c_hour = c.get(Calendar.HOUR_OF_DAY);
            c_minute = c.get(Calendar.MINUTE);

            int tmp_year, tmp_month, tmp_day, tmp_hour, tmp_min;
            tmp_year= Integer.valueOf(list.get(position).get("year"));
            tmp_month= Integer.valueOf(list.get(position).get("month"));
            tmp_day= Integer.valueOf(list.get(position).get("day"));
            tmp_hour= Integer.valueOf(list.get(position).get("hour"));
            tmp_min= Integer.valueOf(list.get(position).get("min"));


            if (Integer.parseInt(list.get(position).get("people")) == 0){
                myHolder.m_condition.setText("人數已滿");
                myHolder.m_condition.setTextColor(getResources().getColor(R.color.red));
            }

            if (tmp_year<c_year){
                myHolder.m_condition.setText("已過期");
                myHolder.m_condition.setTextColor(getResources().getColor(R.color.red));
            }
            else if (tmp_year==c_year && tmp_month<c_month){
                myHolder.m_condition.setText("已過期");
                myHolder.m_condition.setTextColor(getResources().getColor(R.color.red));
            }
            else if (tmp_year==c_year && tmp_month==c_month && tmp_day<c_day){
                myHolder.m_condition.setText("已過期");
                myHolder.m_condition.setTextColor(getResources().getColor(R.color.red));
            }
            else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour<c_hour){
                myHolder.m_condition.setText("已過期");
                myHolder.m_condition.setTextColor(getResources().getColor(R.color.red));
            }
            else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour==c_hour && tmp_min<c_minute){
                myHolder.m_condition.setText("已過期");
                myHolder.m_condition.setTextColor(getResources().getColor(R.color.red));
            }

            myHolder.m_name.setText("主揪人：" + list.get(position).get("department") + " "+
                                                  list.get(position).get("name")+ " "+ list.get(position).get("sex"));
            myHolder.m_rate.setText("評價： " + list.get(position).get("rate") + "顆星/" + list.get(position).get("rate_num")
                    + "  棄標次數："+ list.get(position).get("impeach") + "次");
            myHolder.m_time.setText("時間 : " + list.get(position).get("year") + "-"
                                            + list.get(position).get("month") + "-"
                                            + list.get(position).get("day") + "  "
                                            + list.get(position).get("hour") + ":"
                                                + list.get(position).get("min"));
            myHolder.m_start.setText("起點 : " + list.get(position).get("start"));
            myHolder.m_destination.setText("終點： " + list.get(position).get("destination"));
            myHolder.m_people.setText("還需要人數： " + list.get(position).get("people"));


            // click
            myHolder.container.setOnClickListener(onClickListener(position, holder));
        }

        @Override
        public int getItemCount()
        {
            return list.size();
        }

        /*
        click 觸發

         */
        private View.OnClickListener onClickListener(final int position, final MyViewHolder holder) {
            return new View.OnClickListener(){

                TextView dia_name, dia_time, dia_start, dia_start_others, dia_desti, dia_desti_others, dia_people, dia_comment, dia_rate;
                Button dia_btn_ok, dia_btn_cancel;

                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(mission_list.this);
                    // dialog 的版面配置
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box);
                    dialog.setContentView(R.layout.dialog_all_detail);

                    dialog.setTitle("共乘詳細資料");

                    // dismiss when touching outside Dialog
                    // 可以返回
                    dialog.setCancelable(true);


                    dia_name = (TextView) dialog.findViewById(R.id.dia_name);
                    dia_time = (TextView) dialog.findViewById(R.id.dia_time);
                    dia_start = (TextView) dialog.findViewById(R.id.dia_start);
                    dia_start_others = (TextView) dialog.findViewById(R.id.dia_start_others);
                    dia_desti = (TextView) dialog.findViewById(R.id.dia_destination);
                    dia_desti_others = (TextView) dialog.findViewById(R.id.dia_destination_others);
                    dia_people = (TextView) dialog.findViewById(R.id.dia_people);
                    dia_comment = (TextView) dialog.findViewById(R.id.dia_comment);
                    dia_btn_ok = (Button) dialog.findViewById(R.id.dia_btn_ok);
                    dia_btn_cancel = (Button) dialog.findViewById(R.id.dia_btn_cancel);
                    dia_rate = (TextView) dialog.findViewById(R.id.dia_rate);

                    settings =getSharedPreferences(data,0);
                    String name = settings.getString(nameField, "");
                    String department = settings.getString(departmentField, "");

                    // 顯示資料在dialog上
                    dia_name.setText("主揪人：" + list.get(position).get("department") + " "+
                                            list.get(position).get("name")+ " "+ list.get(position).get("sex"));
                    dia_rate.setText("評價： " + list.get(position).get("rate") + "顆星/" + list.get(position).get("rate_num")
                            + "  棄標次數："+ list.get(position).get("impeach") + "次");
                    dia_time.setText("時間 : " + list.get(position).get("year") + "-"
                            + list.get(position).get("month") + "-"
                            + list.get(position).get("day") + "  "
                            + list.get(position).get("hour") + ":"
                            + list.get(position).get("min"));
                    dia_start.setText("起點： " + list.get(position).get("start"));
                    dia_start_others.setText("起點-補充 : " + list.get(position).get("start_others"));
                    dia_desti.setText("終點： " + list.get(position).get("destination"));
                    dia_desti_others.setText("終點-補充 : " + list.get(position).get("des_others"));
                    dia_people.setText("還需要人數： " + list.get(position).get("people"));
                    dia_comment.setText("備註：" + list.get(position).get("comment"));

                    // 當人數已滿
                    if (Integer.parseInt(list.get(position).get("people"))==0){
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("已滿");
                    }


                    String tmp_name = list.get(position).get("name");
                    String tmp_department = list.get(position).get("department");

                    // 當點到自己的mission
                    if (name.equals(tmp_name) &&  department.equals(tmp_department)) {
                        dialog.setTitle("本共乘為您發起的共乘");
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("確認");
                    }

                    Calendar c = Calendar.getInstance();

                    c_year = c.get(Calendar.YEAR)-1911;
                    c_month = c.get(Calendar.MONTH)+1;
                    c_day = c.get(Calendar.DAY_OF_MONTH);
                    c_hour = c.get(Calendar.HOUR_OF_DAY);
                    c_minute = c.get(Calendar.MINUTE);

                    int tmp_year, tmp_month, tmp_day, tmp_hour, tmp_min;
                    tmp_year= Integer.valueOf(list.get(position).get("year"));
                    tmp_month= Integer.valueOf(list.get(position).get("month"));
                    tmp_day= Integer.valueOf(list.get(position).get("day"));
                    tmp_hour= Integer.valueOf(list.get(position).get("hour"));
                    tmp_min= Integer.valueOf(list.get(position).get("min"));

                    if (tmp_year<c_year){
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("已過期");
                    }
                    else if (tmp_year==c_year && tmp_month<c_month){
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("已過期");
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day<c_day){
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("已過期");
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour<c_hour) {
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("已過期");
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour==c_hour && tmp_min<c_minute){
                        dia_btn_ok.setVisibility(View.INVISIBLE); // 隱藏
                        dia_btn_cancel.setText("已過期");
                    }
                    String t_id = list.get(position).get("id"); //mission的id
                    //取得user id
                    settings =getSharedPreferences(data,0);
                    String user_id = settings.getString(idField, "");
                    new check_passenger().execute(t_id,user_id);


                    dia_btn_ok.setOnClickListener(onConfirmListener(dialog, position, holder));
                    dia_btn_cancel.setOnClickListener(onCancelListener(dialog));
                    dialog.show();
                }
            };

        }

        /*
        按下ok後
        會將人數-1
        並將減完的人數 setText
        再上傳到mysql
        最後close dialog
         */
        private View.OnClickListener onConfirmListener(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //記住id位置 以便用來更改people的值
                    String t_id = list.get(positon).get("id"); //mission的id

                    //people值 轉換成 int 做加減
                    int temp =Integer.parseInt(list.get(positon).get("people"));
                    if (check_p==0){
                        temp--;
                    }
                    holder.m_people.setText("還需要人數： " + temp);

                    //減完換回String
                    String t_people = Integer.toString(temp);

                    //取得user id
                    settings =getSharedPreferences(data,0);
                    String user_id = settings.getString(idField, "");

                    /*
                    1 表示已經加入過共乘
                    0 表示還沒
                     */
                    if (check_p==1){
                        Toast.makeText(getApplicationContext(), "您已加入過共乘\n請至我的共乘查看", Toast.LENGTH_LONG).show();
                    }
                    else {
                        new update_people().execute(t_people, t_id, user_id);
                    }

                    //close dialog after all
                    dialog.dismiss();

                }
            };
        }

        public class check_passenger extends AsyncTask<String, Void, String> {
            protected void onPreExecute(){
            }

            protected String doInBackground(String... arg0)
            {
                String result="";

                //將參數置入變數
                String id= arg0[0];
                String user_id = arg0[1];

                //將變數和php端變數對應好
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("id", id));
                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));


                try {

                    // 連上php
                    HttpPost httpPost = new HttpPost(
                            "http://140.115.52.126/carpool/check_passenger.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();          //取得收到的內容

                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while((line = bufReader.readLine()) != null) {
                        builder.append(line);
                    }
                    inputStream.close();
                    result = builder.toString();

                }
                catch (Exception e){
                  //  Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                return result;
            }

            protected void onPostExecute(String result){
                super.onPostExecute(result);

                int i = Integer.parseInt(result);
                if (i==1){      //已加入過
                    check_p=1;
                }
                else if (i==0) {
                    check_p=0;
                }
            }
        }


        /*
        用來update_people到mysql
        上傳新的人數
        藉由紀錄中的id 在db找到該筆mission
        並修改他的資料
         */
        public class update_people extends AsyncTask<String, Void, String> {
            protected void onPreExecute(){
            }

            protected String doInBackground(String... arg0)
            {
                //將參數置入變數
                String people= arg0[0];
                String id = arg0[1];
                String user_id = arg0[2];

                //將變數和php端變數對應好
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("people", people));
                nameValuePairs.add(new BasicNameValuePair("id", id));
                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));


                try {

                    // 連上php
                    HttpPost httpPost = new HttpPost(
                            "http://140.115.52.126/carpool/update_people_passenger.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                    HttpEntity entity = response.getEntity();

                }
                catch (Exception e){
                 //   Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                return "update success";
            }

            protected void onPostExecute(String result){
                super.onPostExecute(result);

                //因為mysql上的people人數有更動 所以要重新執行get data的動作
                list.clear();
                new getActivity().execute(year, month, day, start, desti,sort_year, sort_month, sort_day, sort_start, sort_desti);

                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }

        // 按取消
        private View.OnClickListener onCancelListener(final Dialog dialog) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            };
        }

        public class MyViewHolder extends RecyclerView.ViewHolder
        {

            private TextView m_name, m_time, m_start, m_destination, m_people, m_condition, m_rate;
            private CardView container;

            public MyViewHolder(View view)
            {
                super(view);
                m_name = (TextView) view.findViewById(R.id.m_name);
                m_time = (TextView) view.findViewById(R.id.m_time);
                m_start = (TextView) view.findViewById(R.id.m_start);
                m_destination = (TextView) view.findViewById(R.id.m_destination);
                m_people = (TextView) view.findViewById(R.id.m_people);
                m_condition = (TextView) view.findViewById(R.id.m_condition);
                m_rate = (TextView) view.findViewById(R.id.m_rate);

                container = (CardView)view.findViewById(R.id.card_view);
            }

        }
    }



}
