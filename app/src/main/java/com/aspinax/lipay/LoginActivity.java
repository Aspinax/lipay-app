package com.aspinax.lipay;

import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Sign In");

        // check if user is logged in
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // buttons and edittexts
        final EditText emailAddressView = findViewById(R.id.emailAddress);
        final EditText passwordView = findViewById(R.id.password);
        Button loginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.registerBtn);
        Button forgotPassBtn = findViewById(R.id.forgotPassBtn);

        // onclick login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = emailAddressView.getText().toString();
                String password = passwordView.getText().toString();

                if(TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(),"Make sure you completely fill the form.", Toast.LENGTH_LONG).show();
                } else {
                    // authenticate
                    auth.signInWithEmailAndPassword(emailAddress, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        // onclick register button
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
