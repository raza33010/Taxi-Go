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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegistrationActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button signup_btn;

    FirebaseAuth mAuth;

    ProgressDialog loadingBar;

    DatabaseReference DriverDataBaseRef;
    String active_driverID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        email = findViewById(R.id.reg_driver_mail);
        password = findViewById(R.id.reg_driver_password);
        signup_btn = findViewById(R.id.reg_driver_btn);

        mAuth = FirebaseAuth.getInstance();


        loadingBar = new ProgressDialog(this);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // runs when login button pressed

                String driver_email = email.getText().toString();
                String driver_password = password.getText().toString();

                // call this method
                RegisterDriver(driver_email,driver_password);

            }

        });




    }

    private void RegisterDriver(String email, String password) {

        // check if email field empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(DriverRegistrationActivity.this, "Email missing...", Toast.LENGTH_SHORT).show();
        }

        // check if password field empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(DriverRegistrationActivity.this, "Password missing...", Toast.LENGTH_SHORT).show();

        }
        else {
            // show loading bar
            loadingBar.setTitle("Registation in Progress");
            loadingBar.setMessage("Please wait....");
            loadingBar.show();

            // register driver on firebase


            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                active_driverID = mAuth.getCurrentUser().getUid();

                                // store customerID in Customers sub-node of the Users node
                                DriverDataBaseRef = FirebaseDatabase.getInstance().getReference().
                                        child("Users").child("Drivers").child(active_driverID);

                                // key:value key = active_driverID  value = true
                                DriverDataBaseRef.setValue(true);


                                // create a toast message if user successfully registered
                                Toast.makeText(DriverRegistrationActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                //transition to DriverMapsActivity
                                Intent mapintent = new Intent(DriverRegistrationActivity.this,DriverMapsActivity.class);
                                startActivity(mapintent);


                            }
                            else{
                                // error encountered
                                Toast.makeText(DriverRegistrationActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                        }
                    });

        }
    }}