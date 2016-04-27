package com.example.anhle.kara;

import android.content.Intent;
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
                presenter.showVideoRecord();
            }
        });


    }

    @Override
    public void onSuccess() {
        startActivity(new Intent(this, RecorderActivity.class));
    }
}
