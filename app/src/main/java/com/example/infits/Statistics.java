package com.example.infits;

import static java.lang.System.out;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Statistics extends AppCompatActivity {

    ImageButton steps_btn, heart_btn, water_btn, sleep_btn, weight_btn;
    CardView moreBtn;
    TextView daily,weekly,monthly,total,dailytv,weeklytv,monthlytv,totaltv;
    ImageView plus;
    private ImageView instagramShare,fbShare,twitterShare,moreShare;
    private static final int REQUEST_EXTERNAL_STORAGe = 1;
    private static String[] permissionstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        verifystoragepermissions(this);

//        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
//            @Override
//            public void handleOnBackPressed() {
//                finish();
//                startActivity(new Intent(getApplicationContext(),DashBoardMain.class));
//            }
//        };
//        getOnBackPressedDispatcher().addCallback(this, callback);

        steps_btn = findViewById(R.id.steps_btn);
        heart_btn = findViewById(R.id.heart_btn);
        water_btn = findViewById(R.id.water_btn);
        sleep_btn = findViewById(R.id.sleep_btn);
        weight_btn = findViewById(R.id.weight_btn);

        instagramShare = findViewById(R.id.imageView41);
        fbShare = findViewById(R.id.imageView43);
        twitterShare = findViewById(R.id.imageView44);
        moreShare = findViewById(R.id.moreShare2);


        daily = findViewById(R.id.daily_count);
        weekly = findViewById(R.id.weekly_Avg);
        monthly = findViewById(R.id.monthly_Avg);
        total = findViewById(R.id.total);

        dailytv = findViewById(R.id.dailytv);
        weeklytv = findViewById(R.id.weeklytv);
        monthlytv = findViewById(R.id.monthlytv);
        totaltv = findViewById(R.id.totaltv);

        moreBtn = findViewById(R.id.moreBtn);

        plus = findViewById(R.id.plus);

        stepsCount();

        steps_btn.setOnClickListener(v ->{
            steps_btn.setBackgroundResource(R.drawable.step_stat_selected);
            heart_btn.setBackgroundResource(R.drawable.heart_stat_unselected);
            water_btn.setBackgroundResource(R.drawable.water_stat_unselected);
            sleep_btn.setBackgroundResource(R.drawable.sleep_stat_unselected);
            weight_btn.setBackgroundResource(R.drawable.weight_stat_unselected);
            daily.setBackground(getDrawable(R.drawable.graph_button));
            weekly.setBackground(getDrawable(R.drawable.graph_button));
            monthly.setBackground(getDrawable(R.drawable.graph_button));
            total.setBackground(getDrawable(R.drawable.graph_button));
            plus.setImageDrawable(getDrawable(R.drawable.plus));

            dailytv.setText("Steps");
            weeklytv.setText("Steps");
            monthlytv.setText("Steps");
            totaltv.setText("Steps");

            moreBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.steps));

            stepsCount();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2,new StepsFragment()).commit();
        });

        heart_btn.setOnClickListener(v ->{
            steps_btn.setBackgroundResource(R.drawable.step_stat_unselected);
            heart_btn.setBackgroundResource(R.drawable.heart_selected);
            water_btn.setBackgroundResource(R.drawable.water_stat_unselected);
            sleep_btn.setBackgroundResource(R.drawable.sleep_stat_unselected);
            weight_btn.setBackgroundResource(R.drawable.weight_stat_unselected);
            daily.setBackground(getDrawable(R.drawable.graph_button_heart));
            weekly.setBackground(getDrawable(R.drawable.graph_button_heart));
            monthly.setBackground(getDrawable(R.drawable.graph_button_heart));
            total.setBackground(getDrawable(R.drawable.graph_button_heart));
            plus.setImageDrawable(getDrawable(R.drawable.plus_heart));

            dailytv.setText("BPM");
            weeklytv.setText("BPM");
            monthlytv.setText("BPM");
            totaltv.setText("BPM");

            moreBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.heartpink));

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2,new HeartFragment()).commit();
        });

        instagramShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                takeScreenshot("com.instagram.android");

            }
        });

        fbShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                takeScreenshot("com.facebook.katana");

            }
        });

        twitterShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 takeScreenshot("com.twitter.android");
            }
        });

        moreShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Date date = new Date();
                    CharSequence format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date);

                    //
                    File mainDir = new File(Statistics.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FilShare");
                    if (!mainDir.exists()) {
                        boolean mkdir = mainDir.mkdir();
                    }
                    String path = mainDir + "/" + "Health Stats" + "-" + format + ".jpeg";

                    View vv = getWindow().getDecorView().getRootView();
                    vv.setDrawingCacheEnabled(true);
                    vv.buildDrawingCache(true);
                    Bitmap b = Bitmap.createBitmap(vv.getDrawingCache());
                    vv.setDrawingCacheEnabled(false);

                    File imageFile = new File(path);
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    b.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();


                    Uri uri = FileProvider.getUriForFile(Statistics.this,
                            BuildConfig.APPLICATION_ID + "." + getLocalClassName() + ".provider",
                            imageFile);

                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("image/png");
                    startActivity(intent);

                } catch (Throwable e) {
                    // Several error may come out with file handling or DOM
                    e.printStackTrace();
                }


            }
        });

        water_btn.setOnClickListener(v ->{
            steps_btn.setBackgroundResource(R.drawable.step_stat_unselected);
            heart_btn.setBackgroundResource(R.drawable.heart_stat_unselected);
            water_btn.setBackgroundResource(R.drawable.water_selected);
            sleep_btn.setBackgroundResource(R.drawable.sleep_stat_unselected);
            weight_btn.setBackgroundResource(R.drawable.weight_stat_unselected);
            waterCount();

            dailytv.setText("ML");
            weeklytv.setText("ML");
            monthlytv.setText("ML");
            totaltv.setText("ML");

            daily.setBackground(getDrawable(R.drawable.graph_button_water));
            weekly.setBackground(getDrawable(R.drawable.graph_button_water));
            monthly.setBackground(getDrawable(R.drawable.graph_button_water));
            total.setBackground(getDrawable(R.drawable.graph_button_water));
            plus.setImageDrawable(getDrawable(R.drawable.plus_water));

            moreBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.waterblue));

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2,new WaterFragment()).commit();
        });

        sleep_btn.setOnClickListener(v ->{
            steps_btn.setBackgroundResource(R.drawable.step_stat_unselected);
            heart_btn.setBackgroundResource(R.drawable.heart_stat_unselected);
            water_btn.setBackgroundResource(R.drawable.water_stat_unselected);
            sleep_btn.setBackgroundResource(R.drawable.sleep_selected);
            weight_btn.setBackgroundResource(R.drawable.weight_stat_unselected);
            plus.setImageDrawable(getDrawable(R.drawable.plus_sleep));
            sleepCount();
            daily.setBackground(getDrawable(R.drawable.graph_button_sleep));
            weekly.setBackground(getDrawable(R.drawable.graph_button_sleep));
            monthly.setBackground(getDrawable(R.drawable.graph_button_sleep));
            total.setBackground(getDrawable(R.drawable.graph_button_sleep));

            moreBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.sleeppurple));

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2,new SleepFragment()).commit();

            dailytv.setText("Hours");
            weeklytv.setText("Hours");
            monthlytv.setText("Hours");
            totaltv.setText("Hours");


        });

        weight_btn.setOnClickListener(v ->{
            steps_btn.setBackgroundResource(R.drawable.step_stat_unselected);
            heart_btn.setBackgroundResource(R.drawable.heart_stat_unselected);
            water_btn.setBackgroundResource(R.drawable.water_stat_unselected);
            sleep_btn.setBackgroundResource(R.drawable.sleep_stat_unselected);
            weight_btn.setBackgroundResource(R.drawable.weight_selected);
            daily.setBackground(getDrawable(R.drawable.graph_button_weight));
            weekly.setBackground(getDrawable(R.drawable.graph_button_weight));
            monthly.setBackground(getDrawable(R.drawable.graph_button_weight));
            total.setBackground(getDrawable(R.drawable.graph_button_weight));
            weightCount();
            plus.setImageDrawable(getDrawable(R.drawable.plus_weight));

            moreBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.weightgreen));

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2,new WeightFragment()).commit();
            dailytv.setText("KG");
            weeklytv.setText("KG");
            monthlytv.setText("KG");
            totaltv.setText("KG");

        });
    }

    private void sleepCount() {
        String url = String.format("%ssleepFragment.php",DataFromDatabase.ipConfig);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, response -> {
            if (!response.equals("failure")){
                Log.d("Fragment","success");
                Log.d("Fragment response",response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject object0 = jsonArray.getJSONObject(0);
                    JSONObject object1 = jsonArray.getJSONObject(1);
                    JSONObject object2 = jsonArray.getJSONObject(2);
                    JSONObject object3 = jsonArray.getJSONObject(3);
                    String stepsWeek = object0.getString("SumWeek");
                    if (stepsWeek.equals("null")){
                        stepsWeek ="0";
                    }
                    weekly.setText(stepsWeek);
                    String stepsMonth = object1.getString("SumMonth");
                    if (stepsMonth.equals("null")){
                        stepsMonth ="0";
                    }
                    monthly.setText(stepsMonth);
                    String stepsDaily = object2.getString("SumDaily");
                    if (stepsDaily.equals("null")){
                        stepsDaily ="0";
                    }
                    daily.setText(stepsDaily);
                    String stepsTotal = object3.getString("SumTotal");
                    if (stepsTotal.equals("null")){
                        stepsTotal ="0";
                    }
                    total.setText(stepsTotal);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (response.equals("failure")){
                Log.d("Fragment","failure");
                Toast.makeText(getApplicationContext(), "Fragment failed", Toast.LENGTH_SHORT).show();
            }
        },error -> {
            Toast.makeText(getApplicationContext(),error.toString().trim(),Toast.LENGTH_SHORT).show();})
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("userID", DataFromDatabase.clientuserID);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    private void weightCount() {
        String url = String.format("%sweightFragment.php",DataFromDatabase.ipConfig);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, response -> {
            if (!response.equals("failure")){
                Log.d("Fragment","success");
                Log.d("Fragment response",response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject object0 = jsonArray.getJSONObject(0);
                    JSONObject object1 = jsonArray.getJSONObject(1);
                    JSONObject object2 = jsonArray.getJSONObject(2);
                    JSONObject object3 = jsonArray.getJSONObject(3);
                    String stepsWeek = object0.getString("SumWeek");
                    if (stepsWeek.equals("null")){
                        stepsWeek ="0";
                    }
                    weekly.setText(stepsWeek);
                    String stepsMonth = object1.getString("SumMonth");
                    if (stepsMonth.equals("null")){
                        stepsMonth ="0";
                    }
                    monthly.setText(stepsMonth);
                    String stepsDaily = object2.getString("SumDaily");
                    if (stepsDaily.equals("null")){
                        stepsDaily ="0";
                    }
                    daily.setText(stepsDaily);
                    String stepsTotal = object3.getString("SumTotal");
                    if (stepsTotal.equals("null")){
                        stepsTotal ="0";
                    }
                    total.setText(stepsTotal);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (response.equals("failure")){
                Log.d("Fragment","failure");
                Toast.makeText(getApplicationContext(), "Fragment failed", Toast.LENGTH_SHORT).show();
            }
        },error -> {
            Toast.makeText(getApplicationContext(),error.toString().trim(),Toast.LENGTH_SHORT).show();})
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("userID", DataFromDatabase.clientuserID);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void waterCount() {
        String url = String.format("%swaterFragment.php",DataFromDatabase.ipConfig);
                StringRequest stringRequest = new StringRequest(Request.Method.POST,url, response -> {
            if (!response.equals("failure")){
                Log.d("Fragment","success");
                Log.d("Fragment response",response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject object0 = jsonArray.getJSONObject(0);
                    JSONObject object1 = jsonArray.getJSONObject(1);
                    JSONObject object2 = jsonArray.getJSONObject(2);
                    JSONObject object3 = jsonArray.getJSONObject(3);
                    String stepsWeek = object0.getString("SumWeek");
                    if (stepsWeek.equals("null")){
                        stepsWeek ="0";
                    }
                    weekly.setText(stepsWeek);
                    String stepsMonth = object1.getString("SumMonth");
                    if (stepsMonth.equals("null")){
                        stepsMonth ="0";
                    }
                    monthly.setText(stepsMonth);
                    String stepsDaily = object2.getString("SumDaily");
                    if (stepsDaily.equals("null")){
                        stepsDaily ="0";
                    }
                    daily.setText(stepsDaily);
                    String stepsTotal = object3.getString("SumTotal");
                    if (stepsTotal.equals("null")){
                        stepsTotal ="0";
                    }
                    total.setText(stepsTotal);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (response.equals("failure")){
                Log.d("Fragment","failure");
                Toast.makeText(getApplicationContext(), "Fragment failed", Toast.LENGTH_SHORT).show();
            }
        },error -> {
            Toast.makeText(getApplicationContext(),error.toString().trim(),Toast.LENGTH_SHORT).show();})
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
//                Log.d("Fragment","clientuserID = "+dataFromDatabase.clientuserID);
                data.put("userID", DataFromDatabase.clientuserID);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    private void stepsCount() {
        String url = String.format("%sstepsFragment.php",DataFromDatabase.ipConfig);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, response -> {
            if (!response.equals("failure")){
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject object0 = jsonArray.getJSONObject(0);
                    JSONObject object1 = jsonArray.getJSONObject(1);
                    JSONObject object2 = jsonArray.getJSONObject(2);
                    JSONObject object3 = jsonArray.getJSONObject(3);
                    String stepsWeek = object0.getString("stepsSumWeek");
                    if (stepsWeek.equals("null")){
                        stepsWeek ="0";
                    }
                    weekly.setText(stepsWeek);
                    String stepsMonth = object1.getString("stepsSumMonth");
                    if (stepsMonth.equals("null")){
                        stepsMonth ="0";
                    }
                    monthly.setText(stepsMonth);
                    String stepsDaily = object2.getString("stepsSumDaily");
                    if (stepsDaily.equals("null")){
                        stepsDaily ="0";
                    }
                    daily.setText(stepsDaily);
                    String stepsTotal = object3.getString("stepsSumTotal");
                    if (stepsTotal.equals("null")){
                        stepsTotal ="0";
                    }
                    total.setText(stepsTotal);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (response.equals("failure")){
                Log.d("HeartFragment","failure");
                Toast.makeText(getApplicationContext(), "heartFragment failed", Toast.LENGTH_SHORT).show();
            }
        },error -> {

        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("userID", DataFromDatabase.clientuserID);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    private void takeScreenshot(String pac) {


        try {

            Date date = new Date();
            CharSequence format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date);


            //
            File mainDir = new File(
                    this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FilShare");
            if (!mainDir.exists()) {
                boolean mkdir = mainDir.mkdir();
            }
            String path = mainDir + "/" + "Health Stats" + "-" + format + ".jpeg";

            View v = getWindow().getDecorView().getRootView();
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache(true);
            Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

            File imageFile = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            b.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();


            Uri uri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + "." + getLocalClassName() + ".provider",
                    imageFile);

            shareImageUri(uri,pac);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
    public static void verifystoragepermissions(Activity activity) {

        int permissions = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If storage permission is not given then request for External Storage Permission
        if (permissions != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permissionstorage, REQUEST_EXTERNAL_STORAGe);
        }
    }
    private void shareImageUri(Uri uri,String pac){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setPackage(pac);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        startActivity(intent);
    }
}