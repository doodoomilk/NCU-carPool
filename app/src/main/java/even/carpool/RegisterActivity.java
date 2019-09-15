package even.carpool;

/*
註冊頁面
帳號是否已註冊
檢查填寫的資料是否符合規定
 */

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


public class RegisterActivity extends AppCompatActivity {

    private EditText register_account;
    private EditText register_password;
    private EditText register_name ;
    private EditText register_phone;
    private EditText register_email;

    private String re_department, re_grade, re_sex;
    private Spinner sp_re_department, sp_re_grade, sp_re_sex;

    private TextView correct;


    String[] str_sex ={"男","女"};
    String[] str_department = {"中文系","英文系","法文系","理學院學士班","物理系","數學系","化學系","化材系",
            "土木系","機械系","企管系","資管系","經濟系","財金系","電機系","資工系","通訊系","地科系",
            "大氣系","客家系","生科系","生醫系",};
    String[] str_grade ={"1","2","3","4"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle("Register");


        register_account = (EditText) findViewById(R.id.account);
        register_password = (EditText) findViewById(R.id.password);
        register_name = (EditText) findViewById(R.id.name);
        register_phone = (EditText) findViewById(R.id.phone);
        register_email = (EditText) findViewById(R.id.email);
        sp_re_department = (Spinner) findViewById(R.id.sp_re_depart);
        sp_re_grade = (Spinner) findViewById(R.id.sp_re_year);
        sp_re_sex = (Spinner) findViewById(R.id.sp_re_sex);

        // 顯示哪裡錯誤
        correct = (TextView) findViewById(R.id.correct);

        final Button okBtn = (Button) findViewById(R.id.okbtn);

        register_account.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        register_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        register_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        register_phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });
        register_email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });


        ArrayAdapter sexList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_sex);
        sp_re_sex.setAdapter(sexList);
        ArrayAdapter departmentList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_department);
        sp_re_department.setAdapter(departmentList);
        ArrayAdapter gradeList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, str_grade);
        sp_re_grade.setAdapter(gradeList);

        // get the data from spinner
        sp_re_sex.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                re_sex = str_sex[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp_re_department.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                re_department = str_department[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp_re_grade.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int num, long l) {
                re_grade = str_grade[num];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        okBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                /// 取出變數
                String account = register_account.getText().toString();

                // 檢查是否註冊過該帳號
                // 傳入申請者輸入的帳號
                new checkIsNewUserActivity().execute(account);

            }
        });

    }

    /*
    藉參數 申請者輸入的帳號
    傳入php 和 db的資料比對
    傳回是否已經被註冊
     */

    public class checkIsNewUserActivity extends AsyncTask<String, Void, String> {
        String account = register_account.getText().toString();
        String password = register_password.getText().toString();
        String name = register_name.getText().toString();
        String phone = register_phone.getText().toString();
        String email = register_email.getText().toString();

        protected void onPreExecute(){
        }

        protected String doInBackground(String... arg0)
        {

            String result="";

            //將參數置入變數
            String checkaccount= arg0[0];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("checkaccount", checkaccount));


            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/check_account.php");
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
                    builder.append(line);
                }
                inputStream.close();
                result = builder.toString();


            }
            catch (Exception e){
               // Log.e("Wrong~~","catch~~");
            }
            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            int i = Integer.parseInt(result);
            if (i==0){
                // 已經有人註冊
                register_already();
            }
            else{
                // 沒人註冊
                if (!isNcuEmail(account)){
                    correct.setText("帳號格式錯誤\n非正確的中央大學信箱");
                }
                else if (password.length()<6||password.length()>20){
                    correct.setText("密碼格式錯誤\n長度應為6~20字元");
                }
                else if (name.length()==0){
                    correct.setText("全名為必填欄位");
                }
                else if (!isEmail(email)){
                    correct.setText("email格式錯誤");
                }
                else{
                    // 註冊成功

                    // 將要insert到database的資料傳入
                    new insertuserlistActivity().execute(account, password, name, re_sex, re_department, re_grade, phone, email);


                }
            }
        }
    }


    // 跳出註冊失敗的訊息
    // 帳號已被申請
    // 按ok後 留在原本頁面
    private void register_already() {
        new AlertDialog.Builder(RegisterActivity.this)
                .setTitle("註冊失敗")
                .setMessage("該帳號已被申請。")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .show();

    }


    /*
    將申請者輸入的data傳入php，
    再由php端寫入mysql
     */
    public class insertuserlistActivity extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){

        }

        protected String doInBackground(String... arg0)
        {
            String account = arg0[0];
            String password = arg0[1];
            String name = arg0[2];
            String sex = arg0[3];
            String department = arg0[4];
            String grade = arg0[5];
            String phone = arg0[6];
            String email = arg0[7];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("account", account));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            nameValuePairs.add(new BasicNameValuePair("name", name));
            nameValuePairs.add(new BasicNameValuePair("sex", sex));
            nameValuePairs.add(new BasicNameValuePair("department", department));
            nameValuePairs.add(new BasicNameValuePair("grade", grade));
            nameValuePairs.add(new BasicNameValuePair("phone", phone));
            nameValuePairs.add(new BasicNameValuePair("email", email));


            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/userlist_insert.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));       //送出請求

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);           //傳回狀態

                HttpEntity entity = response.getEntity();

            }
            catch (Exception e){
              //  Log.e("Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

            return arg0[0];
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Intent register_success = new Intent();
            register_success.setClass(RegisterActivity.this,LoginActivity.class);
            startActivity(register_success);
            //register_success(register_account.toString(), register_password.toString());
        }
    }


    // 跳出註冊成功的訊息
    // 按ok後 跳回登入畫面
    /*private void register_success(final String account,final String password) {
        new AlertDialog.Builder(RegisterActivity.this)
                .setTitle("註冊成功")
                .setMessage("請至申請之信箱做認證。")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent register_success = new Intent();
                                register_success.setClass(RegisterActivity.this,LoginActivity.class);
                                startActivity(register_success);
                            }
                        })
                .show();

    }*/



    // 判斷是否為ncu 學生
    public static boolean isNcuEmail(String strEmail) {
        //why 原為："^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9] [\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$"
        //https://developer.android.com/reference/java/util/regex/Pattern.html
        //https://zh.wikipedia.org/wiki/正则表达式
        String strPattern = "^10\\d{7}@cc.ncu.edu.tw$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    // 判斷是否為email 格式
    public static boolean isEmail(String strEmail) {
        String strPattern = "^\\w+([-.]\\w+)*@\\w+([-]\\w+)*\\.(\\w+([-]\\w+)*\\.)*[a-z]{2,3}$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

}
