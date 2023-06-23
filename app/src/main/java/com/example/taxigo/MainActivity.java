package com.example.taxigo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.taxigo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create a thread object
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                //Overriding the run method of Thread class
                // this method defines the thread responsible for the transitions from
                // first activity to second activity

                try
                {
                    // screen loads for 5 seconds
                    sleep(5000);

                }
                catch (Exception e){

                    e.getStackTrace();

                }
                finally {
                    // creating an Intent to the next activity
                    Intent welcomeIntent = new Intent(MainActivity.this,SecondActivity.class);
                    startActivity(welcomeIntent);

                }
            }

        };
        // calling the run method
        thread.start();


    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}