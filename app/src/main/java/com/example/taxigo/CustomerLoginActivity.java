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

public class CustomerLoginActivity extends AppCompatActivity {

    TextView customer_register;

    EditText email,password;

    Button signin;

    FirebaseAuth mAuth;

    ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        customer_register = findViewById(R.id.driver_registrtion_tv);


        // for login activity
        email = findViewById(R.id.login_customer_email);
        password = findViewById(R.id.login_customer_password);
        signin = findViewById(R.id.login_customer_btn);

        mAuth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);



        // call when "get registered" pressed
        customer_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customer_registration = new Intent(CustomerLoginActivity.this,CustomerRegistrationActivity.class);
                startActivity(customer_registration);
            }
        });


        // call when "login" button pressed
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String customer_email = email.getText().toString();
                String customer_password = password.getText().toString();

                LoginCustomer(customer_email,customer_password);


            }
        });


    }

    private void LoginCustomer(String email, String password)
    {
        // check if email field empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginActivity.this, "Email missing...", Toast.LENGTH_SHORT).show();
        }

        // check if password field empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerLoginActivity.this, "Password missing...", Toast.LENGTH_SHORT).show();

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
                                // send message to the user successfully signed in
                                Toast.makeText(CustomerLoginActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                //transition to DriverMapsActivity
                                Intent mapintent = new Intent(CustomerLoginActivity.this,CustomerMapsActivity.class);
                                startActivity(mapintent);

                            }
                            else{
                                // send message that an error encountered
                                Toast.makeText(CustomerLoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                        }
                    });


        }
}}