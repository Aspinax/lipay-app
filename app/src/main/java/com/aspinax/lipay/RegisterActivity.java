package com.aspinax.lipay;

import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.lang.System;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9+._%-+]{1,256}" + "@" + "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" + "(" + "." + "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" + ")+");
    private FirebaseFirestore db;
    ImageView profile_image;
    public final int RESULT_LOAD_IMG = 1024;
    public String profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Register");

        // check if user is logged in
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();

        // buttons and edittexts
        final EditText emailAddressView = findViewById(R.id.emailAddress);
        final EditText passwordView = findViewById(R.id.password);
        final EditText cPasswordView = findViewById(R.id.cPassword);
        final EditText fnameView = findViewById(R.id.fname);
        final EditText lnameView = findViewById(R.id.lname);
        final EditText phoneView = findViewById(R.id.phoneNumber);
        final EditText phonePrefix = findViewById(R.id.phonePrefix);
        Button loginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.registerBtn);
        profile_image = findViewById(R.id.profile_image);

        // onclick register button
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailAddress = emailAddressView.getText().toString();
                final String password = passwordView.getText().toString();
                final String cPassword = cPasswordView.getText().toString();
                final String fname = fnameView.getText().toString();
                final String lname = lnameView.getText().toString();
                final String phoneNumber = phonePrefix.getText().toString() + phoneView.getText().toString();

                /* check if profilePic base64 string is empty
                 * if empty, use the default R.drawable.default_profile as the profile picture */
                if(TextUtils.isEmpty(profilePic)) {
                    Bitmap default_profile = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
                    profilePic = ImageUtil.convert(default_profile);
                }

                // register user after form validation
                if(TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password) || TextUtils.isEmpty(cPassword) || TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname)) {
                    Toast.makeText(getApplicationContext(), "Make sure you completely fill the form.", Toast.LENGTH_LONG).show();
                } else if(!EMAIL_ADDRESS_PATTERN.matcher(emailAddress).matches()) {
                    Toast.makeText(getApplicationContext(), "Invalid Email.", Toast.LENGTH_LONG).show();
                } else if (!password.equals(cPassword)) {
                    Toast.makeText(getApplicationContext(), "Passwords not similar.", Toast.LENGTH_LONG).show();
                } else {
                    auth.createUserWithEmailAndPassword(emailAddress, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // registration successful
                                        final FirebaseUser user = auth.getCurrentUser();
                                        // update the user's profile
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fname + " " + lname).build();
                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // create a hashmap of user info
                                                            HashMap <String, Object> userInfo = new HashMap<>();
                                                            userInfo.put("balance", 0);
                                                            userInfo.put("createdAt", System.currentTimeMillis() / 1000);
                                                            userInfo.put("email", emailAddress);
                                                            userInfo.put("fname", fname);
                                                            userInfo.put("id", user.getUid());
                                                            userInfo.put("lname", lname);
                                                            userInfo.put("phone", phoneNumber);
                                                            userInfo.put("profilePic", profilePic);

                                                            db.collection("users").document(user.getUid()).set(userInfo)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> t) {
                                                                            if (t.isSuccessful()) {
                                                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            } else {
                                                                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        // registration failed
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        // onclick login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // onclick profile picture
        profile_image.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
               photoPickerIntent.setType("image/*");
               startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
           }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        // process the result of the photo picking
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profile_image.setImageBitmap(selectedImage);

                // generate a base64 version of the image
                profilePic = ImageUtil.convert(selectedImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(RegisterActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }
}