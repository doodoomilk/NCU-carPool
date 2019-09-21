package even.carpool;
/*
對乘客評分
*/
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

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
import java.util.ArrayList;

import static even.carpool.my_list.j_p1;
import static even.carpool.my_list.j_p2;
import static even.carpool.my_list.launchlist;
import static even.carpool.my_list.passengerlist;
import static even.carpool.my_list.tmp_position;

public class rateActivity2 extends AppCompatActivity {

    RatingBar mRatingBar, mRatingBar2;
    Button btn_ok;
    TextView name21, name22;
    float now, now2;
    int new_num, new_num2;
    float before, before2;
    int before_num, before_num2;
    float new_rate, new_rate2;
    String p1_id = "", p2_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate2);

        name21 = (TextView) findViewById(R.id.name21);
        name21.setText("乘客1: " + passengerlist.get(0).get("department") + " " +
                passengerlist.get(0).get("name") + " " + passengerlist.get(0).get("sex"));
        name22 = (TextView) findViewById(R.id.name22);
        name22.setText("乘客2: " + passengerlist.get(1).get("department") + " " +
                passengerlist.get(1).get("name") + " " + passengerlist.get(1).get("sex"));

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar21);
        mRatingBar.setNumStars(5); //設定最大星型數量
        mRatingBar.setStepSize((float) 0.5); //設定步進值
        mRatingBar.setIsIndicator(false); //設定是否可被使用者修改評分

        mRatingBar2 = (RatingBar) findViewById(R.id.ratingBar22);
        mRatingBar2.setNumStars(5); //設定最大星型數量
        mRatingBar2.setStepSize((float) 0.5); //設定步進值
        mRatingBar2.setIsIndicator(false); //設定是否可被使用者修改評分

        mRatingBar.setOnRatingBarChangeListener(ratingBarOnRatingBarChange);//設定監聽器
        mRatingBar2.setOnRatingBarChangeListener(ratingBarOnRatingBarChange2);//設定監聽器

        if (Integer.parseInt(j_p1)<Integer.parseInt(j_p2)){     //1<2
            p1_id=j_p1;
            p2_id=j_p2;
        }
        else{   //2<1
            p1_id=j_p2;
            p2_id=j_p1;
        }

        new get_passenger_rate().execute(p1_id,p2_id);

        btn_ok=(Button)findViewById(R.id.rate_btn_ok2);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_num=before_num+1;
                new_rate= (before+now)/new_num;
                String t_new_num="", t_new_rate="";
                t_new_num= Integer.toString(new_num);
                t_new_rate= String.valueOf(new_rate);
               // Log.e(t_new_num,t_new_rate);
                new set_passenger_rate().execute(p1_id,t_new_num,t_new_rate);

                new_num2=before_num2+1;
                new_rate2= (before2+now2)/new_num2;
                String t_new_num2="", t_new_rate2="";
                t_new_num2= Integer.toString(new_num2);
                t_new_rate2= String.valueOf(new_rate2);
               // Log.e(t_new_num2,t_new_rate2);
                new set_passenger_rate().execute(p2_id,t_new_num2,t_new_rate2);

                String tmp_id = launchlist.get(tmp_position).get("id");
                new update_finish().execute(tmp_id);

                /*Intent finish = new Intent();
                finish.setClass(rateActivity2.this,MainActivity.class);
                startActivity(finish);*/
                rateActivity2.this.finish();
            }
        });
    }

    private RatingBar.OnRatingBarChangeListener ratingBarOnRatingBarChange
            = new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            now = rating;
        }
    };

    private RatingBar.OnRatingBarChangeListener ratingBarOnRatingBarChange2
            = new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            now2 = rating;
        }
    };

    public class get_passenger_rate extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {
            String p1="", p2="";
            p1 = arg0[0];
            p2 = arg0[1];

            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("p1", p1));
                nameValuePairs.add(new BasicNameValuePair("p2", p2));
                HttpPost httpPost=new HttpPost("http://140.115.52.126/carpool/get_passenger_rate2.php");
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
              //  Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return result;
        }

        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                String j_rate_num = "", j_rate = "";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    j_rate_num = jsonData.getString("rate_num");
                    j_rate = jsonData.getString("rate");
                    if (i == 0) {
                        float tmp1;
                        int tmp2;
                        tmp1 = Float.parseFloat(j_rate);
                        tmp2 = Integer.parseInt(j_rate_num);
                        before_num = tmp2;
                        before = tmp1 * tmp2;
                        Log.e(j_rate_num, j_rate);
                    } else if (i == 1) {
                        float tmp1;
                        int tmp2;
                        tmp1 = Float.parseFloat(j_rate);
                        tmp2 = Integer.parseInt(j_rate_num);
                        before_num2 = tmp2;
                        before2 = tmp1 * tmp2;
                     //   Log.e(j_rate_num, j_rate);
                    }
                }

            } catch (Exception e) {
               // Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }

    }

    public class set_passenger_rate extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){

        }

        protected String doInBackground(String... arg0)
        {
            //將參數置入變數
            String p = arg0[0];
            String rate_num= arg0[1];
            String rate= arg0[2];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("p", p));
            nameValuePairs.add(new BasicNameValuePair("rate_num", rate_num));
            nameValuePairs.add(new BasicNameValuePair("rate", rate));


            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/set_passenger_rate.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();

            }
            catch (Exception e){
             //   Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return "success";
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }

    public class update_finish extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){

        }

        protected String doInBackground(String... arg0)
        {
            //將參數置入變數
            String id = arg0[0];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("id", id));


            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/update_finish.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();

            }
            catch (Exception e){
             //   Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return "success";
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}
