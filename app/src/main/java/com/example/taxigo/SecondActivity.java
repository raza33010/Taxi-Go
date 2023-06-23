package com.example.taxigo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.taxigo.R;

public class SecondActivity extends AppCompatActivity {

    ImageButton driver_button;
    ImageButton passenger_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // accessing buttons from xml
        driver_button = findViewById(R.id.driver_btn);
        passenger_button = findViewById(R.id.passenger_btn);

        driver_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent driver_login = new Intent(SecondActivity.this,DriverLoginActivity.class);
                startActivity(driver_login);
            }
        });

        passenger_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customer_login = new Intent(SecondActivity.this,CustomerLoginActivity.class);
                startActivity(customer_login);
            }
        });


    }

}