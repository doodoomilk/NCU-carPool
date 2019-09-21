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
import java.util.HashMap;

import static even.carpool.my_list.j_p1;
import static even.carpool.my_list.launchlist;
import static even.carpool.my_list.passengerlist;
import static even.carpool.my_list.tmp_position;

public class rateActivity extends AppCompatActivity {

    RatingBar mRatingBar;
    Button btn_ok;
    TextView name11;
    float now;
    int new_num;
    float before;
    int before_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        name11= (TextView) findViewById(R.id.name11);
        name11.setText("乘客1: " + passengerlist.get(0).get("department") + " " +
                passengerlist.get(0).get("name") + " " + passengerlist.get(0).get("sex"));

        mRatingBar=(RatingBar)findViewById(R.id.ratingBar11);
        mRatingBar.setNumStars(5); //設定最大星型數量
        mRatingBar.setStepSize((float) 0.5); //設定步進值
        mRatingBar.setIsIndicator(false); //設定是否可被使用者修改評分

        mRatingBar.setOnRatingBarChangeListener(ratingBarOnRatingBarChange);//設定監聽器

        final String p1_id = j_p1;
        //Log.e("p1p1",p1_id);
        new get_passenger_rate().execute(p1_id);

        btn_ok=(Button)findViewById(R.id.rate_btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float new_rate;
                new_num=before_num+1;
                new_rate= (before+now)/new_num;

                String t_new_num="", t_new_rate="";
                t_new_num= Integer.toString(new_num);
                t_new_rate= String.valueOf(new_rate);
                //Log.e(t_new_num,t_new_rate);
                new set_passenger_rate().execute(p1_id,t_new_num,t_new_rate);


                String tmp_id = launchlist.get(tmp_position).get("id");
                new update_finish().execute(tmp_id);

                rateActivity.this.finish();
            }
        });

    }

    private RatingBar.OnRatingBarChangeListener ratingBarOnRatingBarChange
            = new RatingBar.OnRatingBarChangeListener()
    {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating,boolean fromUser)
        {
            now = rating;
        }
    };


    public class get_passenger_rate extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String p1 = arg0[0];
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("p1", p1));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/get_passenger_rate.php");
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
                //Log.e("Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return result;
        }

        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                String j_rate_num="", j_rate="";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    j_rate_num = jsonData.getString("rate_num");
                    j_rate = jsonData.getString("rate");
                }
                float tmp1;
                int tmp2;
                tmp1 = Float.parseFloat(j_rate);
                tmp2 = Integer.parseInt(j_rate_num);
                before_num=tmp2;
                before=tmp1*tmp2;
                //Log.e(j_rate_num,j_rate);
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
               // Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
               // Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            return "success";
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}

