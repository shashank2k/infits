package com.example.infits;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScrollCaptureCallback;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link mealInfoWithPhoto#newInstance} factory method to
 * create an instance of this fragment.
 */
public class mealInfoWithPhoto extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    TextView mealName, calorieValue, carbsValue, fatValue, proteinValue;
    String url = String.format("%sgetFavouriteFoodItems.php", DataFromDatabase.ipConfig);
    String url1 = String.format("%saddFavouriteFoodItems.php", DataFromDatabase.ipConfig);
    String url2 = String.format("%sdeleteFavouriteFoodItems.php", DataFromDatabase.ipConfig);

    ImageView calorieImgback;
    boolean IsFavourite = false;

    ArrayList<String> mealInfotransfer;
    JSONObject mainJSONobj;
    JSONArray mainJsonArray;
    NumberPicker numberPicker1, numberPicker2;
    RequestQueue requestQueue,requestQueue1,requestQueue2;
    ImageButton uparrow1, uparrow2, downarrow1, downarrow2, FavouriteMealButton;
    Button TakeaPhotoButton;
    String[] numberPicker1List = new String[]{"0.5", "1", "1.5", "2"};
    String[] numberPicker2List = new String[]{"Small", "Regular", "Large"};
    int REQUEST_IMAGE_CAPTURE = 1;

    public mealInfoWithPhoto() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment mealInfoWithPhoto.
     */
    // TODO: Rename and change types and number of parameters
    public static mealInfoWithPhoto newInstance(String param1, String param2) {
        mealInfoWithPhoto fragment = new mealInfoWithPhoto();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_meal_info_with_photo, container, false);
        hooks(view);
        getFavouriteFoodItems();
        Bundle args = getArguments();
        mainJSONobj=new JSONObject();
        mealInfotransfer = args.getStringArrayList("mealInfotransfer");
        //set TextView
        mainJsonArray=new JSONArray();
        mealName.setText(mealInfotransfer.get(0));
        calorieValue.setText(mealInfotransfer.get(1));
        carbsValue.setText(mealInfotransfer.get(2));
        proteinValue.setText(mealInfotransfer.get(3) + " g");
        fatValue.setText(mealInfotransfer.get(4) + " g");

        //Add Recent Meal Data
        AddingDataForRecentMeal();
        //numberPicker 1
        numberPicker1.setDisplayedValues(numberPicker1List);
        numberPicker1.setMaxValue(numberPicker1List.length - 1);
        numberPicker1.setValue(1);
        numberPicker1.setMinValue(0);
        uparrow1 = view.findViewById(R.id.uparrow1);
        downarrow1 = view.findViewById(R.id.downarrow1);
        uparrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberPicker1.setValue(numberPicker1.getValue() + 1);
            }
        });
        downarrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberPicker1.setValue(numberPicker1.getValue() - 1);
            }
        });

        //numberPicker 2
        numberPicker2.setDisplayedValues(numberPicker2List);
        numberPicker2.setMaxValue(numberPicker2List.length - 1);
        numberPicker2.setMinValue(0);
        numberPicker2.setValue(0);
        uparrow2 = view.findViewById(R.id.uparrow2);
        downarrow2 = view.findViewById(R.id.downarrow2);
        uparrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberPicker2.setValue(numberPicker2.getValue() + 1);
            }
        });
        downarrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberPicker2.setValue(numberPicker2.getValue() - 1);
            }
        });
        //TakeaPhotoButton
        TakeaPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), CameraForMealTracker.class);
                startActivity(intent);
            }
        });


        //FavouriteMealButton'
        FavouriteMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsFavourite == false) {
                    AddFavourite();
                } else {
                    RemoveFavourite();
                }
            }
        });
        return view;
    }
    private void AddingDataForRecentMeal(){
        try {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("RecentMeal", Context.MODE_PRIVATE);

            if(sharedPreferences.contains("RecentMealInfo")) {
                JSONObject jsonObject2 = new JSONObject(sharedPreferences.getString("RecentMealInfo", ""));
                JSONArray jsonArray1 = jsonObject2.getJSONArray("RecentMealInfo");
                for (int i = 0; i < jsonArray1.length(); i++) {
                    if (jsonArray1.getString(i).contains(mealName.getText().toString()) == false) {
                        mainJsonArray.put(jsonArray1.getJSONObject(i));
                    }
                }
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("name", mealName.getText().toString());
            jsonObject1.put("calorie", calorieValue.getText().toString());
            jsonObject1.put("protin", proteinValue.getText().toString());
            jsonObject1.put("carb", carbsValue.getText().toString());
            jsonObject1.put("fat", fatValue.getText().toString());
            jsonObject1.put("icon",mealInfotransfer.get(5));
            mainJsonArray.put(jsonObject1);
            mainJSONobj.put("RecentMealInfo", mainJsonArray);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("RecentMealInfo", mainJSONobj.toString());
            editor.commit();
            Log.d("RecentMeal", sharedPreferences.getString("RecentMealInfo", ""));
        }catch (Exception exception){
            Log.d("Exception",exception.toString());
        }
    }
    private void AddFavourite() {
        requestQueue1 = Volley.newRequestQueue(getContext());
        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, url1, response -> {
            Log.d("response123",response);
            if (response.equals("success")) {
                Toast.makeText(getContext(), "Food added to favourite successfully", Toast.LENGTH_LONG).show();
                IsFavourite = true;
                FavouriteMealButton.setImageDrawable(getActivity().getDrawable(R.drawable.favourite_clicked));
            } else if (response.equals("failed")){
                Toast.makeText(getContext(), "failed to add Food to favourite", Toast.LENGTH_LONG).show();
                FavouriteMealButton.setImageDrawable(getActivity().getDrawable(R.drawable.favorite));
                IsFavourite = false;
            }

        }, error -> {
            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("clientuserID", DataFromDatabase.clientuserID);
                params.put("FavouriteFoodName", mealName.getText().toString());
                params.put("calorie", calorieValue.getText().toString());
                params.put("protein", proteinValue.getText().toString());
                params.put("carb", carbsValue.getText().toString());
                params.put("fat", fatValue.getText().toString());
                return params;
            }

        };
        requestQueue1.add(stringRequest1);

    }
    private void RemoveFavourite() {
        requestQueue2 = Volley.newRequestQueue(getContext());
        StringRequest stringRequest2 = new StringRequest(Request.Method.POST, url2, response -> {
            Log.d("response123",response);
            if (response.equals("success")) {
                Toast.makeText(getContext(), "Food removed from favourite successfully", Toast.LENGTH_LONG).show();
                IsFavourite = false;
                FavouriteMealButton.setImageDrawable(getActivity().getDrawable(R.drawable.favorite));
            } else if (response.equals("failed")){
                Toast.makeText(getContext(), "failed to remove Food from favourite", Toast.LENGTH_LONG).show();
                FavouriteMealButton.setImageDrawable(getActivity().getDrawable(R.drawable.favourite_clicked));
                IsFavourite = true;
            }

        }, error -> {
            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("clientuserID", DataFromDatabase.clientuserID);
                params.put("FavouriteFoodName", mealName.getText().toString());
                return params;
            }

        };
        requestQueue2.add(stringRequest2);

    }
    private void hooks(View view) {


        mealName = view.findViewById(R.id.mealName);
        fatValue = view.findViewById(R.id.fatValue);
        calorieValue = view.findViewById(R.id.calorieValue);
        proteinValue = view.findViewById(R.id.proteinValue);
        carbsValue = view.findViewById(R.id.carbsValue);


        numberPicker1 = view.findViewById(R.id.numberPicker1);
        numberPicker2 = view.findViewById(R.id.numberPicker2);

        TakeaPhotoButton = view.findViewById(R.id.TakeaPhotoButton);


        FavouriteMealButton = view.findViewById(R.id.favouriteMealButton);


        //calorieImgback
        calorieImgback = view.findViewById(R.id.calorieImgback);
        calorieImgback.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    public void getFavouriteFoodItems() {
        requestQueue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("favouriteFoodItems");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);
                    if (jsonObject1.getString("nameofFoodItem").equals(mealName.getText().toString())==true) {
                        IsFavourite=true;
                        FavouriteMealButton.setImageDrawable(getActivity().getDrawable(R.drawable.favourite_clicked));
                        break;
                    } else {
                        FavouriteMealButton.setImageDrawable(getActivity().getDrawable(R.drawable.favorite));
//                        break;
                    }
                }
            }catch (JSONException e){
                Log.d("JSONException",e.toString());
            }
        },error -> {
            Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("clientuserID", DataFromDatabase.clientuserID);
                return data;
            }
        };
        requestQueue.add(stringRequest);
    }

}