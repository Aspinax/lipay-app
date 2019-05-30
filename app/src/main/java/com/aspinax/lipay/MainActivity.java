package com.aspinax.lipay;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    ImageView profile_image;
    public String profilePic;
    public static final String USERINFO_PREF = "UserInfoFile";
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if user is logged in
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db = FirebaseFirestore.getInstance();
        profile_image = findViewById(R.id.profile_image);
        prefs = getSharedPreferences(USERINFO_PREF, MODE_PRIVATE);

        // check if user info exists in Shared Preferences
        if (prefs.getInt("createdAt", -1) != -1 && prefs.getString("id", null).equals(user.getUid())) {
            // updateUI
            updateUI();
        }

        // if online, add a real time listener for document changes
        final DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    updateSharedPreferences(snapshot);
                    updateUI();
                }
            }
        });

        //Bottom Sheet code for the Quick Pay feature
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(STATE_COLLAPSED);
        //Hide action bar and set the status bar to white and hide soft keyboard on start.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        getSupportActionBar().hide();

        //Let's work
        LinearLayout request_payment_btn = findViewById(R.id.request_payment_btn);
        request_payment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast=Toast.makeText(getApplicationContext(),"OMG! I've been Clicked!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        //Some Simple animation
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(5000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        RelativeLayout new_contact = findViewById(R.id.rotatecircle);

        new_contact.startAnimation(rotate);
    }

    public void updateUI() {
        // retrieve user info from Shared Preferences
        int balance = prefs.getInt("balance", -1);
        int createdAt = prefs.getInt("createdAt", -1);
        String email = prefs.getString("email", null);
        String fname = prefs.getString("fname", null);
        String id = prefs.getString("id", null);
        String lname = prefs.getString("lname", null);
        String phone = prefs.getString("phone", null);
        String profilePic = prefs.getString("profilePic", null);

        // set profile picture
        profile_image.setImageBitmap(ImageUtil.convert(profilePic));
    }

    public void updateSharedPreferences(DocumentSnapshot document) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("balance", document.getLong("balance").intValue());
        editor.putInt("createdAt", document.getLong("createdAt").intValue());
        editor.putString("email", document.get("email").toString());
        editor.putString("fname", document.get("fname").toString());
        editor.putString("id", document.get("id").toString());
        editor.putString("lname", document.get("lname").toString());
        editor.putString("phone", document.get("phone").toString());
        editor.putString("profilePic", document.get("profilePic").toString());
        editor.apply();
    }
}
