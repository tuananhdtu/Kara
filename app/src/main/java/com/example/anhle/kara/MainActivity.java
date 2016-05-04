package com.example.anhle.kara;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements MainView{

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);

        Button btnRecordKara = (Button) findViewById(R.id.btnRecordKara);
        btnRecordKara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(MainActivity.this)) {
                    presenter.showVideoRecord();
                }else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Thông báo")
                            .setMessage("Yêu cầu có kết nối mạng.")
                            .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).show();
                }
            }
        });


    }

    @Override
    public void onSuccess() {
        startActivity(new Intent(this, RecorderActivity.class));
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
