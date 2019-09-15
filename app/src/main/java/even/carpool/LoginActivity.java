package even.carpool;
/*
登入頁面
記住登入的狀態(包含user的各種資料)
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.util.Log;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public static ArrayList<HashMap<String,String>>  info_list_outside = new ArrayList<HashMap<String,String>> ();

    //記住登入狀態
    public static SharedPreferences settings;
    public static String login_account;
    public static String data = "DATA";
    public static String idField = "ID";
    public static String accountField = "ACCOUNT";
    public static String nameField = "NAME";
    public static String sexField = "SEX";
    public static String departmentField = "DEPARTMENT";
    public static String gradeField = "GRADE";
    public static String phoneField = "PHONE";
    public static String emailField = "EMAIL";
    public static String rateField = "RATE";
    public static String rate_numField = "RATE_NUM";
    public static String impeachField = "IMPEACH";

    public static String user_account;
    public static String user_department;
    public static String user_name;

    public  String input_account;


    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mLoginWrong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        setTitle("Sign in");

        OfficialCountManTimeThread official_count_mantime_thread = new  OfficialCountManTimeThread();
        official_count_mantime_thread.start();


        // 先判斷是否已經登入
        settings =getSharedPreferences(data,0);
        login_account = settings.getString(accountField, "");
        if (login_account.length()!=0){
            Intent login_success = new Intent();
            login_success.setClass(LoginActivity.this,MainActivity.class);
            startActivity(login_success);
        }


        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mLoginWrong = (TextView)findViewById(R.id.login_wrong);
        mPasswordView = (EditText) findViewById(R.id.password);

        populateAutoComplete(); //??

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // 登入按鈕
        Button mEmailSignInButton = (Button) findViewById(R.id.email_login_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                input_account = mEmailView.getText().toString();
                String input_password = mPasswordView.getText().toString();

                // 傳入使用者輸入的帳號密碼
                new check_Login().execute(input_account, input_password);


            }
        });

        // 註冊按鈕
        Button mRegisterButton = (Button) findViewById(R.id.email_register_button) ;
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Register_it = new Intent();
                Register_it.setClass(LoginActivity.this,RegisterActivity.class);
                startActivity(Register_it);
            }
        });

        // ScrollView
        mLoginFormView = findViewById(R.id.login_form);

        // ProgressBar 進度條 內建
        mProgressView = findViewById(R.id.login_progress);
    }


    /*
    藉參數的帳號密碼
    上傳至 php 和db上的資料比對
    在php端判斷帳號密碼是否正確
     */
    public class check_Login extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){
        }

        protected String doInBackground(String... arg0)
        {

            String result="";

            //將參數置入變數
            String input_account= arg0[0];
            String input_password= arg0[1];

            //將變數和php端變數對應好
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("input_account", input_account));
            nameValuePairs.add(new BasicNameValuePair("input_password", input_password));


            try {

                // 連上php
                HttpPost httpPost = new HttpPost(
                        "http://140.115.52.126/carpool/check_login.php");
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
                Log.e("Wrong~~","catch~~");
            }
            return result;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);

            int i = Integer.parseInt(result);
            if (i==0){
                // 登入失敗
                mLoginWrong.setText("帳號密碼錯誤或尚未註冊帳號！！");
            }
            else{
                mLoginWrong.setText("");

                // 登入成功
                // 記錄登入狀態
                settings = getSharedPreferences(data,0);
                settings.edit().putString(accountField, input_account).commit();

                // 傳入目前登入的user帳號
                new get_user_info().execute(input_account);


            }
        }
    }

    /*
    藉參數的帳號
    到db 找相對應的user資料
    並傳回來，記入到狀態中
     */
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
                Log.e("Wrong~~","catch~~");
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

                //login_account = mEmailView.getText().toString();
                Intent login_success = new Intent();
                login_success.setClass(LoginActivity.this,MainActivity.class);
                startActivity(login_success);
                Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_LONG).show();
                //Log.e(info_list_outside.get(0).get("id"), info_list_outside.get(0).get("name"));
                /*Log.e(list.get(1).get("start"), list.get(1).get("destination"));
                Log.e(list.get(2).get("start"), list.get(2).get("destination"));*/

            }
            catch (Exception e){
                //Log.e("catch data Wrong","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

        }
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }



    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

