package com.example.anhle.kara;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements MainView{

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
