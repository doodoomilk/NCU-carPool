package even.carpool;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static even.carpool.LoginActivity.data;
import static even.carpool.LoginActivity.departmentField;
import static even.carpool.LoginActivity.idField;
import static even.carpool.LoginActivity.impeachField;
import static even.carpool.LoginActivity.nameField;
import static even.carpool.LoginActivity.rateField;
import static even.carpool.LoginActivity.rate_numField;
import static even.carpool.LoginActivity.settings;
import static even.carpool.LoginActivity.sexField;

/**
 * Created by even on 2016/12/11.
 */

public class my_list extends Activity {

    public static ArrayList<HashMap<String,String>> joinlist = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String,String>> launchlist = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String,String>> passengerlist = new ArrayList<HashMap<String, String>>();

    String[] str_my = {"請選擇查看項目","我加入的共乘","我發起的共乘"};
    private Spinner spinner_my;
    int index;  //index=1 join      index=2 launch

    //RecyclerView
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private MylistAdapter mAdapter;
    int[] check = {0};

    public static String j_p1="", j_p2="", j_p3="";
    public static int tmp_position;

    int c_year,c_month,c_day,c_hour,c_minute;
    String sc_year="",sc_month="",sc_day="",sc_hour="",sc_minute="";
    String time="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_list);

        OfficialCountManTimeThread official_count_mantime_thread = new  OfficialCountManTimeThread();
        official_count_mantime_thread.start();

        spinner_my = (Spinner)findViewById(R.id.spinner_my);

        ArrayAdapter myList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_my);
        spinner_my.setAdapter(myList);

        /*
        設定選項之onclick事件
         */
        spinner_my.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                if (num==1){
                    //我加入的共乘
                    index = 1;
                    //取得自己的id
                    settings =getSharedPreferences(data,0);
                    final String user_id = settings.getString(idField, "");

                    //先清空各種list
                    joinlist.clear();
                    passengerlist.clear();
                    //再重新進行get data動作 才不會導致資料重複get
                    new get_joinlist().execute(user_id);

                    //下拉刷新
                    mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.layout_swipe_refresh2);
                    mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                        public void onRefresh() {
                            //先清空list
                            joinlist.clear();
                            passengerlist.clear();
                            //再重新進行get data動作 才不會導致資料重複get
                            new get_joinlist().execute(user_id);

                            //数据重新加载完成后，提示数据发生改变，并且设置现在不在刷新
                            mAdapter.notifyDataSetChanged();
                            mRefreshLayout.setRefreshing(false);
                        }
                    });
                }
                else if (num==2){
                    //我發起的共乘
                    index =2;
                    //取得自己的name, department
                    settings =getSharedPreferences(data,0);
                    final String name = settings.getString(nameField, "");
                    final String department = settings.getString(departmentField, "");

                    //先清空list
                    launchlist.clear();
                    passengerlist.clear();
                    //再重新進行get data動作 才不會導致資料重複get
                    new get_launchlist().execute(name,department);

                    //下拉刷新
                    mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.layout_swipe_refresh2);
                    mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                        public void onRefresh() {

                            //先清空list
                            launchlist.clear();
                            passengerlist.clear();
                            //再重新進行get data動作 才不會導致資料重複get
                            new get_launchlist().execute(name,department);

                            //数据重新加载完成后，提示数据发生改变，并且设置现在不在刷新
                            mAdapter.notifyDataSetChanged();
                            mRefreshLayout.setRefreshing(false);
                        }
                    });

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    /*
    獲得加入過的共乘資料
     */
    public class get_joinlist extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String user_id = arg0[0]; //使用者id
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/joinlist_get.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();          //取得收到的內容

                BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = bufReader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                inputStream.close();
                result = builder.toString();

            } catch (Exception e) {
               // Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

            return result;
        }

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
                    String j_p1 = jsonData.getString("p1");
                    String j_p2 = jsonData.getString("p2");
                    String j_p3 = jsonData.getString("p3");
                    String j_finish = jsonData.getString("finish");
                    String j_static_people = jsonData.getString("static_people");
                    String j_rate = jsonData.getString("rate");
                    String j_rate_num = jsonData.getString("rate_num");
                    String j_impeach = jsonData.getString("impeach");

                    Double r = Double.parseDouble(j_rate);
                    BigDecimal bd = new BigDecimal(r);
                    bd = bd.setScale(1, RoundingMode.HALF_UP);
                    j_rate = bd.toString();


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
                    mission_detail.put("p1", j_p1);
                    mission_detail.put("p2", j_p2);
                    mission_detail.put("p3", j_p3);
                    mission_detail.put("finish", j_finish);
                    mission_detail.put("static_people", j_static_people);
                    mission_detail.put("rate", j_rate);
                    mission_detail.put("rate_num", j_rate_num);
                    mission_detail.put("impeach", j_impeach);
                    joinlist.add(mission_detail);
                }

                //Log.e(list.get(0).get("start"), list.get(0).get("condi"));
                /*Log.e(list.get(1).get("start"), list.get(1).get("destination"));
                Log.e(list.get(2).get("start"), list.get(2).get("destination"));*/


                // 呼叫 recycler view 版面
                recycle_view();
            } catch (Exception e) {
               // Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                joinlist.clear();
                passengerlist.clear();
                recycle_view();
            }
        }
    }
    /*
        獲得自己發起的共乘資料
    */
    public class get_launchlist extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String name = arg0[0]; //name
            String department = arg0[1]; //department
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", name));
                nameValuePairs.add(new BasicNameValuePair("department", department));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/launchlist_get.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();          //取得收到的內容

                BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = bufReader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                inputStream.close();
                result = builder.toString();

            } catch (Exception e) {
                //Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return result;
        }

        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
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
                    String j_p1 = jsonData.getString("p1");
                    String j_p2 = jsonData.getString("p2");
                    String j_p3 = jsonData.getString("p3");
                    String j_finish = jsonData.getString("finish");

                    HashMap<String, String> mission_detail = new HashMap<>();
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
                    mission_detail.put("p1", j_p1);
                    mission_detail.put("p2", j_p2);
                    mission_detail.put("p3", j_p3);
                    mission_detail.put("finish", j_finish);
                    launchlist.add(mission_detail);
                }

                //Log.e(list.get(0).get("start"), list.get(0).get("condi"));
                /*Log.e(list.get(1).get("start"), list.get(1).get("destination"));
                Log.e(list.get(2).get("start"), list.get(2).get("destination"));*/


                // 呼叫 recycler view 版面
                recycle_view();
            } catch (Exception e) {
               // Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                launchlist.clear();
                passengerlist.clear();
                recycle_view();
            }
        }
    }

    // 設定recyclerView
    public void recycle_view(){
        //RecyclerView

        mRecyclerView = (RecyclerView) findViewById(R.id.mylist_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //RecyclerView實作
        mRecyclerView.setAdapter(mAdapter = new MylistAdapter());
    }

    class MylistAdapter extends RecyclerView.Adapter<MylistAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(my_list.this)
                    .inflate(R.layout.few_detail, parent, false));
            if (index == 1) {
                holder = new MyViewHolder(LayoutInflater.from(my_list.this)
                        .inflate(R.layout.join_few_detail, parent, false));
            } else if (index == 2) {
                holder = new MyViewHolder(LayoutInflater.from(my_list.this)
                        .inflate(R.layout.launch_few_detail, parent, false));

            }
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            MyViewHolder myHolder = holder;

            if (index == 1) {

                Calendar c = Calendar.getInstance();

                c_year = c.get(Calendar.YEAR)-1911;
                c_month = c.get(Calendar.MONTH)+1;
                c_day = c.get(Calendar.DAY_OF_MONTH);
                c_hour = c.get(Calendar.HOUR_OF_DAY);
                c_minute = c.get(Calendar.MINUTE);

                int tmp_year, tmp_month, tmp_day, tmp_hour, tmp_min;
                tmp_year= Integer.valueOf(joinlist.get(position).get("year"));
                tmp_month= Integer.valueOf(joinlist.get(position).get("month"));
                tmp_day= Integer.valueOf(joinlist.get(position).get("day"));
                tmp_hour= Integer.valueOf(joinlist.get(position).get("hour"));
                tmp_min= Integer.valueOf(joinlist.get(position).get("min"));

                if (Integer.parseInt(joinlist.get(position).get("people")) == 0) {
                    myHolder.jo_condition.setText("人數已滿");
                    myHolder.jo_condition.setTextColor(getResources().getColor(R.color.red));
                }
                if (Integer.parseInt(joinlist.get(position).get("finish")) == 1){
                    myHolder.jo_condition.setText("已完成");
                    myHolder.jo_condition.setTextColor(getResources().getColor(R.color.purple));
                }

                if (Integer.parseInt(joinlist.get(position).get("people")) != 0 && Integer.parseInt(joinlist.get(position).get("finish")) != 1){
                    myHolder.jo_condition.setText("人數未滿");
                    myHolder.jo_condition.setTextColor(getResources().getColor(R.color.green));
                }

                if (Integer.parseInt(joinlist.get(position).get("finish")) != 1){
                    if (tmp_year<c_year){
                        myHolder.jo_condition.setText("已過期");
                        myHolder.jo_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month<c_month){
                        myHolder.jo_condition.setText("已過期");
                        myHolder.jo_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day<c_day){
                        myHolder.jo_condition.setText("已過期");
                        myHolder.jo_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour<c_hour){
                        myHolder.jo_condition.setText("已過期");
                        myHolder.jo_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour==c_hour && tmp_min<c_minute){
                        myHolder.jo_condition.setText("已過期");
                        myHolder.jo_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                }



                myHolder.jo_name.setText("主揪人：" + joinlist.get(position).get("department") + " " +
                        joinlist.get(position).get("name")+ " " +joinlist.get(position).get("sex"));
                myHolder.jo_rate.setText("評價： " + joinlist.get(position).get("rate") + "顆星/" + joinlist.get(position).get("rate_num")
                        + "  棄標次數："+ joinlist.get(position).get("impeach") + "次");
                myHolder.jo_time.setText("時間 : " + joinlist.get(position).get("year") + "-"
                        + joinlist.get(position).get("month") + "-"
                        + joinlist.get(position).get("day") + "  "
                        + joinlist.get(position).get("hour") + ":"
                        + joinlist.get(position).get("min"));
                myHolder.jo_start.setText("起點 : " + joinlist.get(position).get("start"));
                myHolder.jo_destination.setText("終點： " + joinlist.get(position).get("destination"));
                myHolder.jo_people.setText("還需要人數： " + joinlist.get(position).get("people"));


                // click on join cardview
                myHolder.container.setOnClickListener(onClickListener_join(position, holder));

            } else if (index == 2) {

                Calendar c = Calendar.getInstance();

                c_year = c.get(Calendar.YEAR)-1911;
                c_month = c.get(Calendar.MONTH)+1;
                c_day = c.get(Calendar.DAY_OF_MONTH);
                c_hour = c.get(Calendar.HOUR_OF_DAY);
                c_minute = c.get(Calendar.MINUTE);

                int tmp_year, tmp_month, tmp_day, tmp_hour, tmp_min;
                tmp_year= Integer.valueOf(launchlist.get(position).get("year"));
                tmp_month= Integer.valueOf(launchlist.get(position).get("month"));
                tmp_day= Integer.valueOf(launchlist.get(position).get("day"));
                tmp_hour= Integer.valueOf(launchlist.get(position).get("hour"));
                tmp_min= Integer.valueOf(launchlist.get(position).get("min"));

                if (Integer.parseInt(launchlist.get(position).get("people")) == 0) {
                    myHolder.l_condition.setText("人數已滿");
                    myHolder.l_condition.setTextColor(getResources().getColor(R.color.red));
                }
                if (Integer.parseInt(launchlist.get(position).get("finish")) == 1){
                    myHolder.l_condition.setText("已完成");
                    myHolder.l_condition.setTextColor(getResources().getColor(R.color.purple));
                }
                if (Integer.parseInt(launchlist.get(position).get("people")) != 0 && Integer.parseInt(launchlist.get(position).get("finish")) != 1){
                    myHolder.l_condition.setText("人數未滿");
                    myHolder.l_condition.setTextColor(getResources().getColor(R.color.green));
                }


                if (Integer.parseInt(launchlist.get(position).get("finish")) != 1){
                    if (tmp_year<c_year){
                        myHolder.l_condition.setText("已過期");
                        myHolder.l_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month<c_month){
                        myHolder.l_condition.setText("已過期");
                        myHolder.l_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day<c_day){
                        myHolder.l_condition.setText("已過期");
                        myHolder.l_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour<c_hour){
                        myHolder.l_condition.setText("已過期");
                        myHolder.l_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour==c_hour && tmp_min<c_minute){
                        myHolder.l_condition.setText("已過期");
                        myHolder.l_condition.setTextColor(getResources().getColor(R.color.red));
                    }
                }

                settings =getSharedPreferences(data,0);
                String name = settings.getString(nameField, "");
                String department = settings.getString(departmentField, "");
                String sex = settings.getString(sexField, "");
                String rate = settings.getString(rateField, "");
                String rate_num = settings.getString(rate_numField, "");
                String impeach = settings.getString(impeachField, "");

                // 顯示資料在dialog上
                myHolder.l_name.setText("主揪人：" + department + " "+
                        name + " "+ sex  );
                myHolder.l_rate.setText("評價： " + rate + "顆星/" + rate_num
                        + "  棄標次數："+ impeach + "次");
                myHolder.l_time.setText("時間 : " + launchlist.get(position).get("year") + "-"
                        + launchlist.get(position).get("month") + "-"
                        + launchlist.get(position).get("day") + "  "
                        + launchlist.get(position).get("hour") + ":"
                        + launchlist.get(position).get("min"));
                myHolder.l_start.setText("起點 : " + launchlist.get(position).get("start"));
                myHolder.l_destination.setText("終點： " + launchlist.get(position).get("destination"));
                myHolder.l_people.setText("還需要人數： " + launchlist.get(position).get("people"));


                // click on launch cardview
                myHolder.container2.setOnClickListener(onClickListener_launch(position, holder));
            }

        }

        @Override
        public int getItemCount() {
            int result = 0;
            if (index == 1) {
                result = joinlist.size();
            } else if (index == 2) {
                result = launchlist.size();
            }
            return result;
        }

        Dialog dialog_join = new Dialog(my_list.this);
        Dialog dialog_launch = new Dialog(my_list.this);

        /*
        查看其他乘客（我加入的）
         */
        private View.OnClickListener onClickListener_join(final int position, final MyViewHolder holder) {
            return new View.OnClickListener(){

                TextView dia_name, dia_time, dia_start, dia_start_others, dia_desti, dia_desti_others, dia_people, dia_comment, dia_rate;
                Button dia_btn_look, dia_btn_dropout, dia_btn_cancel;

                @Override
                public void onClick(View v) {

                    passengerlist.clear();
                    // dialog 的版面配置
                    dialog_join.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box);
                    dialog_join.setContentView(R.layout.dialog_join_detail);

                    dialog_join.setTitle("共乘詳細資料");

                    // dismiss when touching outside Dialog
                    // 可以返回
                    dialog_join.setCancelable(true);


                    dia_name = (TextView) dialog_join.findViewById(R.id.diaj_name);
                    dia_time = (TextView) dialog_join.findViewById(R.id.diaj_time);
                    dia_start = (TextView) dialog_join.findViewById(R.id.diaj_start);
                    dia_start_others = (TextView) dialog_join.findViewById(R.id.diaj_start_others);
                    dia_desti = (TextView) dialog_join.findViewById(R.id.diaj_destination);
                    dia_desti_others = (TextView) dialog_join.findViewById(R.id.diaj_destination_others);
                    dia_people = (TextView) dialog_join.findViewById(R.id.diaj_people);
                    dia_comment = (TextView) dialog_join.findViewById(R.id.diaj_comment);
                    dia_btn_look = (Button) dialog_join.findViewById(R.id.diaj_btn_look);
                    dia_btn_dropout = (Button) dialog_join.findViewById(R.id.diaj_dropout);
                    dia_btn_cancel = (Button) dialog_join.findViewById(R.id.diaj_btn_cancel);
                    dia_rate = (TextView) dialog_join.findViewById(R.id.diaj_rate);


                    // 顯示資料在dialog上
                    dia_name.setText("主揪人：" + joinlist.get(position).get("department") + " " +
                            joinlist.get(position).get("name")+ " " + joinlist.get(position).get("sex"));
                    dia_rate.setText("評價： " + joinlist.get(position).get("rate") + "顆星/" + joinlist.get(position).get("rate_num")
                            + "  棄標次數："+ joinlist.get(position).get("impeach") + "次");
                    dia_time.setText("時間 : " + joinlist.get(position).get("year") + "-"
                            + joinlist.get(position).get("month") + "-"
                            + joinlist.get(position).get("day") + "  "
                            + joinlist.get(position).get("hour") + ":"
                            + joinlist.get(position).get("min"));
                    dia_start.setText("起點： " + joinlist.get(position).get("start"));
                    dia_start_others.setText("起點-補充 : " + joinlist.get(position).get("start_others"));
                    dia_desti.setText("終點： " + joinlist.get(position).get("destination"));
                    dia_desti_others.setText("終點-補充 : " + joinlist.get(position).get("des_others"));
                    dia_people.setText("還需要人數： " + joinlist.get(position).get("people"));
                    dia_comment.setText("備註：" + joinlist.get(position).get("comment"));


                    Calendar c = Calendar.getInstance();

                    c_year = c.get(Calendar.YEAR)-1911;
                    c_month = c.get(Calendar.MONTH)+1;
                    c_day = c.get(Calendar.DAY_OF_MONTH);
                    c_hour = c.get(Calendar.HOUR_OF_DAY);
                    c_minute = c.get(Calendar.MINUTE);

                    int tmp_year, tmp_month, tmp_day, tmp_hour, tmp_min;
                    tmp_year= Integer.valueOf(joinlist.get(position).get("year"));
                    tmp_month= Integer.valueOf(joinlist.get(position).get("month"));
                    tmp_day= Integer.valueOf(joinlist.get(position).get("day"));
                    tmp_hour= Integer.valueOf(joinlist.get(position).get("hour"));
                    tmp_min= Integer.valueOf(joinlist.get(position).get("min"));

                    if (Integer.parseInt(joinlist.get(position).get("finish"))==1){     // 當共乘完成
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);


                        dia_btn_look.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_look.setText("查看乘客名單");
                        dia_btn_cancel.setText("確認");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));

                        // 評分
                        dia_btn_dropout.setText("評分");
                        dia_btn_dropout.setOnClickListener(onJoinRateListener(dialog_join, position, holder));

                        dialog_join.show();
                    }else if (tmp_year<c_year){
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_dropout.setText("查看共乘名單");
                        dia_btn_dropout.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_cancel.setText("確認");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));
                        dialog_join.show();
                    }
                    else if (tmp_year==c_year && tmp_month<c_month){
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_dropout.setText("查看共乘名單");
                        dia_btn_dropout.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_cancel.setText("確認");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));
                        dialog_join.show();
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day<c_day){
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_dropout.setText("查看共乘名單");
                        dia_btn_dropout.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_cancel.setText("確認");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));
                        dialog_join.show();
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour<c_hour){
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_dropout.setText("查看共乘名單");
                        dia_btn_dropout.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_cancel.setText("確認");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));
                        dialog_join.show();
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour==c_hour && tmp_min<c_minute){
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_dropout.setText("查看共乘名單");
                        dia_btn_dropout.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_cancel.setText("確認");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));
                        dialog_join.show();
                    }
                    else{
                        /*
                    獲得mission id 對應之 p1 p2 p3之id
                     */
                        String m_id="";
                        m_id = joinlist.get(position).get("id");
                        passengerlist.clear();
                        new get_passenger().execute(m_id);

                        dia_btn_look.setOnClickListener(onConfirmListener(dialog_join, position, holder));
                        dia_btn_dropout.setOnClickListener(onDropoutListener(dialog_join, position, holder));
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_join));
                        dialog_join.show();
                    }
                }
            };

        }
        /*
        查看其他乘客（我發起的）
         */
        private View.OnClickListener onClickListener_launch(final int position, final MyViewHolder holder) {
            return new View.OnClickListener(){

                TextView dia_name, dia_time, dia_start, dia_start_others, dia_desti, dia_desti_others, dia_people, dia_comment, dia_rate;
                Button dia_btn_look, dia_btn_delete, dia_btn_complete, dia_btn_cancel;

                @Override
                public void onClick(View v) {

                    passengerlist.clear();
                    // dialog 的版面配置
                    dialog_launch.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box);
                    dialog_launch.setContentView(R.layout.dialog_launch_detail);

                    dialog_launch.setTitle("共乘詳細資料");

                    // dismiss when touching outside Dialog
                    // 可以返回
                    dialog_launch.setCancelable(true);


                    dia_name = (TextView) dialog_launch.findViewById(R.id.dial_name);
                    dia_time = (TextView) dialog_launch.findViewById(R.id.dial_time);
                    dia_start = (TextView) dialog_launch.findViewById(R.id.dial_start);
                    dia_start_others = (TextView) dialog_launch.findViewById(R.id.dial_start_others);
                    dia_desti = (TextView) dialog_launch.findViewById(R.id.dial_destination);
                    dia_desti_others = (TextView) dialog_launch.findViewById(R.id.dial_destination_others);
                    dia_people = (TextView) dialog_launch.findViewById(R.id.dial_people);
                    dia_comment = (TextView) dialog_launch.findViewById(R.id.dial_comment);
                    dia_btn_look = (Button) dialog_launch.findViewById(R.id.dial_btn_look);
                    dia_btn_delete = (Button) dialog_launch.findViewById(R.id.dia_btn_delete);
                    dia_btn_cancel = (Button) dialog_launch.findViewById(R.id.dial_btn_cancel);
                    dia_btn_complete = (Button) dialog_launch.findViewById(R.id.dial_btn_complete);
                    dia_rate = (TextView) dialog_launch.findViewById(R.id.dial_rate);


                    settings =getSharedPreferences(data,0);
                    String name = settings.getString(nameField, "");
                    String department = settings.getString(departmentField, "");
                    String sex = settings.getString(sexField, "");
                    String rate = settings.getString(rateField, "");
                    String rate_num = settings.getString(rate_numField, "");
                    String impeach = settings.getString(impeachField, "");

                    // 顯示資料在dialog上
                    dia_name.setText("主揪人：" + department + " "+
                            name + " "+ sex );
                    dia_rate.setText("評價： " + rate + "顆星/" + rate_num
                            + "  棄標次數："+ impeach + "次");
                    dia_time.setText("時間 : " + launchlist.get(position).get("year") + "-"
                            + launchlist.get(position).get("month") + "-"
                            + launchlist.get(position).get("day") + "  "
                            + launchlist.get(position).get("hour") + ":"
                            + launchlist.get(position).get("min"));
                    dia_start.setText("起點： " + launchlist.get(position).get("start"));
                    dia_start_others.setText("起點-補充 : " + launchlist.get(position).get("start_others"));
                    dia_desti.setText("終點： " + launchlist.get(position).get("destination"));
                    dia_desti_others.setText("終點-補充 : " + launchlist.get(position).get("des_others"));
                    dia_people.setText("還需要人數： " + launchlist.get(position).get("people"));
                    dia_comment.setText("備註：" + launchlist.get(position).get("comment"));

                    Calendar c = Calendar.getInstance();

                    c_year = c.get(Calendar.YEAR)-1911;
                    c_month = c.get(Calendar.MONTH)+1;
                    c_day = c.get(Calendar.DAY_OF_MONTH);
                    c_hour = c.get(Calendar.HOUR_OF_DAY);
                    c_minute = c.get(Calendar.MINUTE);

                    int tmp_year, tmp_month, tmp_day, tmp_hour, tmp_min;
                    tmp_year= Integer.valueOf(launchlist.get(position).get("year"));
                    tmp_month= Integer.valueOf(launchlist.get(position).get("month"));
                    tmp_day= Integer.valueOf(launchlist.get(position).get("day"));
                    tmp_hour= Integer.valueOf(launchlist.get(position).get("hour"));
                    tmp_min= Integer.valueOf(launchlist.get(position).get("min"));


                    if (Integer.parseInt(launchlist.get(position).get("finish"))==1){     // 當共乘完成
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_complete.setVisibility(View.INVISIBLE);
                        dia_btn_delete.setText("查看乘客名單");
                        dia_btn_delete.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_cancel.setText("關閉");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }else if (tmp_year<c_year){
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_complete.setVisibility(View.INVISIBLE);
                        dia_btn_delete.setText("查看乘客名單");
                        dia_btn_delete.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_cancel.setText("關閉");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }
                    else if (tmp_year==c_year && tmp_month<c_month){
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_complete.setVisibility(View.INVISIBLE);
                        dia_btn_delete.setText("查看乘客名單");
                        dia_btn_delete.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_cancel.setText("關閉");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day<c_day){
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_complete.setVisibility(View.INVISIBLE);
                        dia_btn_delete.setText("查看乘客名單");
                        dia_btn_delete.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_cancel.setText("關閉");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour<c_hour) {
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_complete.setVisibility(View.INVISIBLE);
                        dia_btn_delete.setText("查看乘客名單");
                        dia_btn_delete.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_cancel.setText("關閉");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }
                    else if (tmp_year==c_year && tmp_month==c_month && tmp_day==c_day && tmp_hour==c_hour && tmp_min<c_minute){
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setVisibility(View.INVISIBLE);
                        dia_btn_complete.setVisibility(View.INVISIBLE);
                        dia_btn_delete.setText("查看乘客名單");
                        dia_btn_delete.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_cancel.setText("關閉");
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }
                    else{
                        String m_id="";
                        m_id = launchlist.get(position).get("id");
                        new get_passenger().execute(m_id);

                        dia_btn_look.setOnClickListener(onConfirmListener(dialog_launch, position, holder));
                        dia_btn_delete.setOnClickListener(onDeleteListener(dialog_launch, position, holder));
                        dia_btn_complete.setOnClickListener(onCompleteListener(dialog_launch, position, holder));
                        dia_btn_cancel.setOnClickListener(onCancelListener(dialog_launch));
                        dialog_launch.show();
                    }
                }
            };

        }

        /*
            獲得mission id 對應之 p1 p2 p3之id
        */
        public class get_passenger extends AsyncTask<String, Void, String> {

            protected void onPreExecute() {

            }
            protected String doInBackground(String... arg0) {
                String m_id = arg0[0]; //m_id
                String result = "";

                try {

                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("m_id", m_id));

                    HttpPost httpPost = new HttpPost(
                            "http://140.115.52.126/carpool/get_passenger.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));       //送出請求

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();          //取得收到的內容

                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while ((line = bufReader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    inputStream.close();
                    result = builder.toString();

                } catch (Exception e) {
                   // Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                return result;
            }

            protected void onPostExecute(String result) {
                try {

                    JSONArray jsonArray = new JSONArray(result);
                    j_p1="";
                    j_p2="";
                    j_p3="";
                    JSONObject jsonData = jsonArray.getJSONObject(0);
                    j_p1 = jsonData.getString("p1");
                    j_p2 = jsonData.getString("p2");
                    j_p3 = jsonData.getString("p3");

                    new get_passenger_info().execute(j_p1,j_p2,j_p3);

                }
                catch (Exception e) {
                    //Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        }

        /*
        獲得p1 p2 p3之個人資料
         */
        public class get_passenger_info extends AsyncTask<String, Void, String> {

            protected void onPreExecute() {

            }
            protected String doInBackground(String... arg0) {
                String p1 = arg0[0];
                String p2 = arg0[1];
                String p3 = arg0[2];
                String result = "";

                try {

                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("p1", p1));
                    nameValuePairs.add(new BasicNameValuePair("p2", p2));
                    nameValuePairs.add(new BasicNameValuePair("p3", p3));

                    HttpPost httpPost = new HttpPost(
                            "http://140.115.52.126/carpool/get_passenger_info.php");
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

                } catch (Exception e) {
                   // Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                return result;
            }

            protected void onPostExecute(String result) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonData = jsonArray.getJSONObject(i);
                        String j_name = jsonData.getString("name");
                        String j_sex = jsonData.getString("sex");
                        String j_department = jsonData.getString("department");
                        String j_rate = jsonData.getString("rate");
                        String j_ratenum = jsonData.getString("rate_num");
                        //String j_impeach = jsonData.getString("impeach");


                        Double r = Double.parseDouble(j_rate);
                        BigDecimal bd = new BigDecimal(r);
                        bd = bd.setScale(1, RoundingMode.HALF_UP);
                        j_rate = bd.toString();

                        HashMap<String, String> info_list = new HashMap<>();
                        info_list.put("name", j_name);
                        info_list.put("sex", j_sex);
                        info_list.put("department", j_department);
                        info_list.put("rate", j_rate);
                        info_list.put("rate_num", j_ratenum);
                        //info_list.put("impeach", j_impeach);
                        passengerlist.add(info_list);
                    }

                } catch (Exception e) {
                   // Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        }

        /*
        查看其他乘客的資料
         */
        private View.OnClickListener onConfirmListener(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                Button btn_confirm;
                TextView diap_name, diap_name3, diap_name4, diap_name5, diap_rate;
                @Override
                public void onClick(View v) {
                    Dialog dialog_people = new Dialog(my_list.this);
                    // dialog 的版面配置
                    dialog_people.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box);
                    dialog_people.setContentView(R.layout.dialog_people);

                    dialog_people.setTitle("共乘之乘客");

                    btn_confirm = (Button) dialog_people.findViewById(R.id.diap_btn_confirm);
                    diap_name = (TextView) dialog_people.findViewById(R.id.diap_name);
                    diap_name3 = (TextView) dialog_people.findViewById(R.id.diap_name3);
                    diap_name4 = (TextView) dialog_people.findViewById(R.id.diap_name4);
                    diap_name5 = (TextView) dialog_people.findViewById(R.id.diap_name5);
                    diap_rate =  (TextView) dialog_people.findViewById(R.id.diap_rate);

                    dialog_people.setCancelable(true);

                    settings =getSharedPreferences(data,0);
                    String name = settings.getString(nameField, "");
                    String department = settings.getString(departmentField, "");
                    String sex = settings.getString(sexField, "");
                    String rate = settings.getString(rateField, "");
                    String rate_num = settings.getString(rate_numField, "");
                    String impeach = settings.getString(impeachField, "");

                    // 顯示資料在dialog上
                    if (index==1){
                        diap_name.setText("主揪人： " + joinlist.get(positon).get("department") + " "+
                                joinlist.get(positon).get("name")+ " "+ joinlist.get(positon).get("sex"));
                        diap_rate.setText("評價： " + joinlist.get(positon).get("rate") + "顆星/" + joinlist.get(positon).get("rate_num")
                                + "  棄標次數："+ joinlist.get(positon).get("impeach") + "次");

                    }
                    else if (index==2){
                        diap_name.setText("主揪人：" + department + " " + name + " "+ sex +
                                " " + rate + "顆星/" + rate_num);
                        diap_rate.setText("評價： " + rate + "顆星/" + rate_num
                                + "  棄標次數："+ impeach + "次");
                    }

                    if (passengerlist.size()==1){
                        diap_name3.setText(passengerlist.get(0).get("department") + " " +
                                passengerlist.get(0).get("name") + " " + passengerlist.get(0).get("sex")+
                                "  評價：" + passengerlist.get(0).get("rate") + "顆星/" + passengerlist.get(0).get("rate_num"));
                    }
                    else if (passengerlist.size()==2){
                        diap_name3.setText(passengerlist.get(0).get("department") + " " +
                                passengerlist.get(0).get("name") + " " + passengerlist.get(0).get("sex")+
                                "  評價：" + passengerlist.get(0).get("rate") + "顆星/" + passengerlist.get(0).get("rate_num"));
                        diap_name4.setText(passengerlist.get(1).get("department") + " " +
                                passengerlist.get(1).get("name") + " " + passengerlist.get(1).get("sex")+
                                "  評價：" + passengerlist.get(1).get("rate") + "顆星/" + passengerlist.get(1).get("rate_num"));
                    }
                    else if (passengerlist.size()==3){
                        diap_name3.setText(passengerlist.get(0).get("department") + " " +
                                passengerlist.get(0).get("name") + " " + passengerlist.get(0).get("sex")+
                                "  評價：" + passengerlist.get(0).get("rate") + "顆星/" + passengerlist.get(0).get("rate_num"));
                        diap_name4.setText(passengerlist.get(1).get("department") + " " +
                                passengerlist.get(1).get("name") + " " + passengerlist.get(1).get("sex")+
                                "  評價：" + passengerlist.get(1).get("rate") + "顆星/" + passengerlist.get(1).get("rate_num"));
                        diap_name5.setText(passengerlist.get(2).get("department") + " " +
                                passengerlist.get(2).get("name") + " " + passengerlist.get(2).get("sex")+
                                "  評價：" + passengerlist.get(2).get("rate") + "顆星/" + passengerlist.get(2).get("rate_num"));
                    }
                    else if (passengerlist.size()==0){
                        diap_name3.setText("目前無其他乘客");
                    }

                    btn_confirm.setOnClickListener(onConfirmListener_inpeople(dialog_people, positon, holder));
                    dialog_people.show();
                    //close dialog after all
                    dialog.dismiss();

                }
            };
        }

        /*
        確認之後返回上一個dialog
         */
        private View.OnClickListener onConfirmListener_inpeople(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (index==1){
                        dialog_join.show();
                    }
                    else if (index==2){
                        dialog_launch.show();
                    }
                    //close dialog after all
                    dialog.dismiss();

                }
            };
        }

        /*
        退出共乘
         */
        private View.OnClickListener onDropoutListener(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int a = Integer.parseInt(joinlist.get(positon).get("people"));
                    a++;
                    String tmp_people = Integer.toString(a);

                    String m_id = joinlist.get(positon).get("id");

                    settings =getSharedPreferences(data,0);
                    String p_id = settings.getString(idField, "");

                    new delete_passenger().execute(m_id, p_id, tmp_people);
                    Toast.makeText(getApplicationContext(), "您已退出共乘！", Toast.LENGTH_LONG).show();
                    //close dialog after all
                    dialog.dismiss();

                }
            };
        }

        /*
        刪除共乘
         */
        private View.OnClickListener onDeleteListener(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    String m_id = launchlist.get(positon).get("id");
                    new delete_mission().execute(m_id);


                    //close dialog after all
                    dialog.dismiss();

                }
            };
        }

        /*
        共乘完成後的相關措施
         */
        private View.OnClickListener onCompleteListener(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tmp_position=positon;
                    // string 不能放進if判斷 所以要轉成int
                    int tmp = Integer.parseInt(launchlist.get(positon).get("people"));
                    if (tmp!=0){
                        Toast.makeText(getApplicationContext(), "您的共乘尚未完成！", Toast.LENGTH_LONG).show();
                    }
                    else if (passengerlist.size()==1){
                        //跳轉到rate頁面
                        Intent finish_rate = new Intent();
                        finish_rate.setClass(my_list.this,rateActivity.class);
                        startActivity(finish_rate);
                    }
                    else if (passengerlist.size()==2){
                        //跳轉到rate頁面
                        Intent finish_rate = new Intent();
                        finish_rate.setClass(my_list.this,rateActivity2.class);
                        startActivity(finish_rate);
                    }
                    else if (passengerlist.size()==3){
                        //跳轉到rate頁面
                        Intent finish_rate = new Intent();
                        finish_rate.setClass(my_list.this,rateActivity3.class);
                        startActivity(finish_rate);
                    }
                    //Log.e("ffff",String.valueOf(passengerlist.size()));

                    //close dialog after all
                    dialog.dismiss();

                }
            };
        }

        private View.OnClickListener onJoinRateListener(final Dialog dialog, final int positon, final MyViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    tmp_position=positon;

                    if (check[positon]==0){
                        check[positon]=1;
                        Intent join_rate = new Intent();
                        join_rate.setClass(my_list.this,join_rateActivity.class);
                        startActivity(join_rate);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "已做過評分", Toast.LENGTH_LONG).show();
                    }

                    //close dialog after all
                    dialog.dismiss();

                }
            };
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

        /*
        退出共乘的相關措施
         */
        public class delete_passenger extends AsyncTask<String, Void, String> {
            protected void onPreExecute(){
            }

            protected String doInBackground(String... arg0)
            {
                //將參數置入變數
                String m_id= arg0[0];
                String p_id = arg0[1];
                String tmp_people = arg0[2];

                //將變數和php端變數對應好
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("m_id", m_id));
                nameValuePairs.add(new BasicNameValuePair("p_id", p_id));
                nameValuePairs.add(new BasicNameValuePair("tmp_people", tmp_people));

                try {

                    // 連上php
                    HttpPost httpPost = new HttpPost(
                            "http://140.115.52.126/carpool/delete_passenger.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                    HttpEntity entity = response.getEntity();

                }
                catch (Exception e){
                  //  Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                return "update success";
            }

            protected void onPostExecute(String result){
                super.onPostExecute(result);

                //因為mysql上的people人數有更動 所以要重新執行get data的動作
                joinlist.clear();
                settings =getSharedPreferences(data,0);
                final String user_id = settings.getString(idField, "");
                new get_joinlist().execute(user_id);
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }

        /*
        刪除共乘
         */
        public class delete_mission extends AsyncTask<String, Void, String> {
            protected void onPreExecute(){
            }

            protected String doInBackground(String... arg0)
            {
                //將參數置入變數
                String m_id= arg0[0];
                String result="";

                //將變數和php端變數對應好
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("m_id", m_id));

                try {

                    // 連上php
                    HttpPost httpPost = new HttpPost(
                            "http://140.115.52.126/carpool/delete_mission.php");
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
                   // Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                return result;
            }

            protected void onPostExecute(String result){
                super.onPostExecute(result);

                int i = Integer.parseInt(result);
                if (i==1){
                    //刪除成功
                    Toast.makeText(getApplicationContext(), "您的共乘已刪除！", Toast.LENGTH_LONG).show();
                    launchlist.clear();

                    settings =getSharedPreferences(data,0);
                    final String name = settings.getString(nameField, "");
                    final String department = settings.getString(departmentField, "");
                    new get_launchlist().execute(name,department);

                }
                else if (i==0){
                    //刪除失敗
                   // Log.e("刪除失敗","ㄎㄎ");

                    AlertDialog.Builder alertdialog = new AlertDialog.Builder(my_list.this);
                    alertdialog.setTitle("注意！");
                    alertdialog.setMessage("乘客須全數退出才能刪除共乘，\n故請先通知其他乘客退出共乘。");
                    alertdialog.setPositiveButton("確認",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
                    alertdialog.show();
                }
            }
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView jo_name, jo_time, jo_start, jo_destination, jo_people, jo_condition, jo_rate;
            private TextView l_name, l_time, l_start, l_destination, l_people, l_condition, l_rate;
            private CardView container, container2;

            public MyViewHolder(View view) {
                super(view);
                jo_name = (TextView) view.findViewById(R.id.jo_name);
                jo_time = (TextView) view.findViewById(R.id.jo_time);
                jo_start = (TextView) view.findViewById(R.id.jo_start);
                jo_destination = (TextView) view.findViewById(R.id.jo_destination);
                jo_people = (TextView) view.findViewById(R.id.jo_people);
                jo_condition = (TextView) view.findViewById(R.id.jo_condition);
                jo_rate = (TextView) view.findViewById(R.id.jo_rate);


                l_name = (TextView) view.findViewById(R.id.l_name);
                l_time = (TextView) view.findViewById(R.id.l_time);
                l_start = (TextView) view.findViewById(R.id.l_start);
                l_destination = (TextView) view.findViewById(R.id.l_destination);
                l_people = (TextView) view.findViewById(R.id.l_people);
                l_condition = (TextView) view.findViewById(R.id.l_condition);
                l_rate = (TextView) view.findViewById(R.id.l_rate);

                container = (CardView)view.findViewById(R.id.join_cardview);
                container2 = (CardView)view.findViewById(R.id.launch_cardview);
            }

        }
    }

}