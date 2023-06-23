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

public class CustomerRegistrationActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    EditText fullname;
    Button signup_btn;

    FirebaseAuth mAuth;

    ProgressDialog loadingBar;

    //initialise database reference
    DatabaseReference CustomerDataBaseRef;
    String active_customerID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration);

        email = findViewById(R.id.reg_customer_mail);
        password = findViewById(R.id.reg_customer_password);
        fullname = findViewById(R.id.fname);
        signup_btn = findViewById(R.id.reg_customer_btn);


        mAuth = FirebaseAuth.getInstance();



        loadingBar = new ProgressDialog(this);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customer_email = email.getText().toString();
                String customer_password = password.getText().toString();

                // call this method
                RegisterCustomer(customer_email,customer_password);

            }
        });
    }

    private void RegisterCustomer(String email, String password) {
        // check if email field empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerRegistrationActivity.this, "Email missing...", Toast.LENGTH_SHORT).show();
        }

        // check if password field empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerRegistrationActivity.this, "Password missing...", Toast.LENGTH_SHORT).show();

        }
        else {
            // show loading bar
            loadingBar.setTitle("Registation in Progress");
            loadingBar.setMessage("Please wait....");
            loadingBar.show();

            // register customer on firebase


            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                //store data on database
                                active_customerID = mAuth.getCurrentUser().getUid();

                                // store customerID in Customers sub-node of the Users node
                                CustomerDataBaseRef = FirebaseDatabase.getInstance().getReference().
                                        child("Users").child("Customers").child(active_customerID);

                                // key:value key = active_customerID  value = true
                                CustomerDataBaseRef.setValue(true);


                                // create a toast message if user successfully registered
                                Toast.makeText(CustomerRegistrationActivity.this,"Welcome "+ fullname.getText().toString(),Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                //transition to CustomerMapsActivity
                                Intent mapintent = new Intent(CustomerRegistrationActivity.this,CustomerMapsActivity.class);
                                startActivity(mapintent);


                            }
                            else{
                                // error encountered
                                Toast.makeText(CustomerRegistrationActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();




                            }
                        }
                    });

        }
    }}
