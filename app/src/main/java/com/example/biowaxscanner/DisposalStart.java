package com.example.biowaxscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DisposalStart extends AppCompatActivity {
    ProgressDialog pd;
    String responseBody;
    ArrayAdapter<String> spinnerArrayAdapter;
    List<String> plantsList;
    Spinner spinner;
    String[] plants = new String[]{
            "Select Disposal Type",
    };
    Button b1;
    int send;
    int pos,poss=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disposal_start);

        pd = new ProgressDialog(DisposalStart.this);
        pd.setMessage("getting Data..");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.show();
        spinner = (Spinner) findViewById(R.id.spinner1);
        b1= (Button) findViewById(R.id.btn);
        plantsList = new ArrayList<>(Arrays.asList(plants));

        // Initializing an ArrayAdapter
        spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.support_simple_spinner_dropdown_item,plantsList);

        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        if (Validations.hasActiveInternetConnection(DisposalStart.this)) {
            listApi();
        }else{
            Toast.makeText(getBaseContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
        spinnerArrayAdapter.notifyDataSetChanged();



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {


                if (position!=0){
                    // Your code here
                    poss=position;
                    pos=position-1;

                    try {
                        JSONObject obj= new JSONObject(responseBody);
                        if(obj.getString("status").equals("true")){

                            JSONArray array=obj.getJSONArray("data");
                            JSONObject res = array.getJSONObject(pos);

                            send=res.getInt("id");

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
//                if(position==0){
//                    Toast.makeText(DisposalStart.this, "please select Disposal Type", Toast.LENGTH_SHORT).show();
//                }
                // selectionCurrent= position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spinner.getSelectedItem().toString()!="Select Disposal Type" && poss!=0){

                    Intent i = new Intent(DisposalStart.this,BarcodescannerDis.class);
                    String s= String.valueOf(send);
                    i.putExtra("id",s);
                    startActivity(i);
                }else{
                    Toast.makeText(DisposalStart.this, "please select Disposal Type", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void listApi()
    {


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .url("http://175.101.151.121:8001/api/getdisposal_method")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                //login.setVisibility(View.GONE);
                Log.d("result", e.getMessage().toString());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        Toast.makeText(DisposalStart.this, "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });

                //pd.dismiss();
            }

            @Override
            public void onResponse(okhttp3.Call call, final okhttp3.Response response) throws IOException {



                responseBody = response.body().string();

                final JSONObject obj;


                try {
//                        Toast.makeText(getBaseContext(), "success", Toast.LENGTH_SHORT).show();

                    obj=new JSONObject(responseBody);
                    if(obj.getString("status").equals("true")){

                        JSONArray array=obj.getJSONArray("data");

                        for(int i=0;i<array.length();i++){
                            JSONObject res = array.getJSONObject(i);
                            //   mStrings.add(res.getString("facility_name"));
                            plantsList.add(res.getString("method_name"));

                        }
                        pd.dismiss();
                    }else{


                    }




                } catch (JSONException e) {
                    e.printStackTrace();
                    pd.dismiss();
                }
            }


        });

    }
}
