package even.carpool;

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
import java.util.concurrent.TimeUnit;

import static even.carpool.my_list.joinlist;
import static even.carpool.my_list.launchlist;
import static even.carpool.my_list.passengerlist;
import static even.carpool.my_list.tmp_position;

public class join_rateActivity extends AppCompatActivity {

    TextView name;
    RatingBar mRatingBar;
    float now;
    Button btn_ok;
    int tmp_num=100;
    int static_people;
    float tmp_rate=100;
    int new_num=100;
    float before=100;
    int before_num=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_rate);

        name= (TextView) findViewById(R.id.join_rate_name);
        name.setText("主揪人：" + joinlist.get(tmp_position).get("department") + " " +
                joinlist.get(tmp_position).get("name")+ " " + joinlist.get(tmp_position).get("sex"));

        mRatingBar=(RatingBar)findViewById(R.id.join_rate_ratingBar);
        mRatingBar.setNumStars(5); //設定最大星型數量
        mRatingBar.setStepSize((float) 0.5); //設定步進值
        mRatingBar.setIsIndicator(false); //設定是否可被使用者修改評分

        mRatingBar.setOnRatingBarChangeListener(ratingBarOnRatingBarChange);//設定監聽器


        btn_ok=(Button)findViewById(R.id.join_rate_btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String department, name1;
                department = joinlist.get(tmp_position).get("department");
                name1 = joinlist.get(tmp_position).get("name");
                String rate =String.valueOf(now);

                String id = joinlist.get(tmp_position).get("id");
                new get_static_people().execute(id);
                new get_launcher_rate().execute(department, name1);



                join_rateActivity.this.finish();
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

    public class get_static_people extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String id = arg0[0];
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("id", id));


                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/get_static_people.php");
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
                String j_static_people="";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    j_static_people = jsonData.getString("static_people");
                }
                static_people=Integer.parseInt(j_static_people);

            } catch (Exception e) {
               // Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            String department, name1;
            department = joinlist.get(tmp_position).get("department");
            name1 = joinlist.get(tmp_position).get("name");
            String rate =String.valueOf(now);
            new set_tmp_rate().execute(department, name1, rate);
        }
    }

    public class get_launcher_rate extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String department = arg0[0];
            String name1 = arg0[1];
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("department", department));
                nameValuePairs.add(new BasicNameValuePair("name1", name1));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/get_launcher_rate.php");
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

    public class set_tmp_rate extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String department = arg0[0];
            String name1 = arg0[1];
            String tmp_rate = arg0[2];
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("department", department));
                nameValuePairs.add(new BasicNameValuePair("name1", name1));
                nameValuePairs.add(new BasicNameValuePair("tmp_rate", tmp_rate));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/set_tmp_rate.php");
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
            return "success";
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            String department, name1;
            department = joinlist.get(tmp_position).get("department");
            name1 = joinlist.get(tmp_position).get("name");
            String rate =String.valueOf(now);

            new get_tmp_num().execute(department, name1);

        }
    }

    public class get_tmp_num extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }
        protected String doInBackground(String... arg0) {
            String department = arg0[0];
            String name1 = arg0[1];
            String result = "";

            try {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("department", department));
                nameValuePairs.add(new BasicNameValuePair("name1", name1));

                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/get_tmp_num.php");
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
                String j_tmp_num="", j_tmp_rate="";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    j_tmp_num = jsonData.getString("tmp_num");
                    j_tmp_rate = jsonData.getString("tmp_rate");
                }
                tmp_num=Integer.parseInt(j_tmp_num);
                tmp_rate=Float.parseFloat(j_tmp_rate);

            } catch (Exception e) {
              //  Log.e("catch data Wrong", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            if (tmp_num==static_people){
                String department, name1;
                department = joinlist.get(tmp_position).get("department");
                name1 = joinlist.get(tmp_position).get("name");

                Float new_rate = tmp_rate/tmp_num;
                new_num = before_num+1;
                Float newnew = (before+new_rate)/new_num;
              //  Log.e(String.valueOf(before),String.valueOf(new_rate));
              //  Log.e(String.valueOf(new_num),String.valueOf(newnew));
                new set_launcher_rate().execute(department, name1, Integer.toString(new_num), String.valueOf(newnew));

            }
        }
    }



    public class set_launcher_rate extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){

        }

        protected String doInBackground(String... arg0)
        {
            //將參數置入變數
            String department = arg0[0];
            String name1 = arg0[1];
            String new_num = arg0[2];
            String new_rate = arg0[3];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("department", department));
            nameValuePairs.add(new BasicNameValuePair("name1", name1));
            nameValuePairs.add(new BasicNameValuePair("new_num", new_num));
            nameValuePairs.add(new BasicNameValuePair("new_rate", new_rate));

            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/set_launcher_rate.php");
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
