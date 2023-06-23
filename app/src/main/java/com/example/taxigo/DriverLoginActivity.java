package com.example.taxigo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class DriverLoginActivity extends AppCompatActivity {


    TextView driver_register;
    EditText email,password;

    Button signin;

    FirebaseAuth mAuth;

    ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);


    // for registration activity
    driver_register = findViewById(R.id.driver_registrtion_tv);

    // for login activity
    email = findViewById(R.id.login_driver_email);
    password = findViewById(R.id.login_driver_password);
    signin = findViewById(R.id.driver_login_btn);

    mAuth = FirebaseAuth.getInstance();

    loadingBar = new ProgressDialog(this);



    // call when "get registered" pressed
    driver_register.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // transition to DriverRegistrationActivity
            Intent driver_registration = new Intent(DriverLoginActivity.this,DriverRegistrationActivity.class);
            startActivity(driver_registration);
        }
    });


    // call when "login" button pressed
    signin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            String driver_email = email.getText().toString();
            String driver_password = password.getText().toString();

            LoginDriver(driver_email,driver_password);


        }
    });


    }

    private void LoginDriver(String email, String password)
    {
        // check if email field empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(DriverLoginActivity.this, "Email missing...", Toast.LENGTH_SHORT).show();
        }

        // check if password field empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(DriverLoginActivity.this, "Password missing...", Toast.LENGTH_SHORT).show();

        }
        else {
            // show loading bar
            loadingBar.setTitle("Please Wait");
            loadingBar.setMessage("Logging in....");
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                // create a toast message if user successfully registered
                                Toast.makeText(DriverLoginActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                //transition to DriverMapsActivity
                                Intent mapintent = new Intent(DriverLoginActivity.this,DriverMapsActivity.class);
                                startActivity(mapintent);


                            }
                            else{
                                // error encountered
                                Toast.makeText(DriverLoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                        }
                    });


        }
}
}