package com.example.biowaxscanner;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class BarcodescannerDis extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    ProgressDialog pd;
    String res;
    Boolean sss=false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(me.dm7.barcodescanner.zbar.Result result) {



        // Do something with the result here
        Log.v("kkkk", result.getContents()); // Prints scan results
        Log.v("uuuu", result.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)
     //res=result.getContents();
        pd = new ProgressDialog(BarcodescannerDis.this);
        pd.setMessage("Searching the Barcode..");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.dismiss();
        pd.show();

        if (Validations.hasActiveInternetConnection(BarcodescannerDis.this)) {
            String res = result.getContents();
            sendDataDis(res);
            //onResume();
        }else{
            Toast.makeText(getBaseContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            onResume();
            pd.dismiss();
        }


//            Biowastageform.barcodeNumber.setText(result.getContents());

          //  onBackPressed();

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    private void sendDataDis(final String result) {



        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .url("http://175.101.151.121:8001/api/getdisposal/"+result+"/"+getIntent().getStringExtra("id"))
                .get()
                .build();


        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("idvlaue=",getIntent().getStringExtra("id"));
                Log.d("result", e.getMessage().toString());
                e.printStackTrace();
                pd.dismiss();
            }

            @Override
            public void onResponse(okhttp3.Call call, final okhttp3.Response response) throws IOException {
                //  pd.dismiss();
                pd.dismiss();
                if (!response.isSuccessful()) {
                    pd.dismiss();
                    Log.d("result", response.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BarcodescannerDis.this,"Failed",Toast.LENGTH_SHORT).show();
                            onResume();

                        }
                    });


                    throw new IOException("Unexpected code " + response);

                } else {
                    String responseBody = response.body().string();
                    final JSONObject obj;
                    pd.dismiss();

                    try {
                        obj=new JSONObject(responseBody);
                        if (obj.getString("status").equals("success")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog(BarcodescannerDis.this,"Success "+result, "true");
                                }
                            });



                        }else if(obj.getString("error").equals("Disposal is Already Done !") || obj.getString("error").equals("Invalid Barcode !")){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                      //  showDialog(BarcodescannerDis.this, obj.getString("error"), "true");
                                        showDialog(BarcodescannerDis.this,obj.getString("error")+" "+result, "true");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }

                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        showDialog(BarcodescannerDis.this,obj.getString("error")+" "+result, "true");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        });



    }
    public void showDialog(Activity activity, String msg, final String status) {
        final Dialog dialog = new Dialog(activity, R.style.PauseDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView text = dialog.findViewById(R.id.text_dialog);
        text.setText(msg);

        ImageView b = dialog.findViewById(R.id.b);

        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                onResume();
            }
        });
        dialog.show();
    }


}



