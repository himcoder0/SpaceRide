package com.example.newapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.newapp.services.MyService;
import com.example.newapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    TextView enterAsUser;
    TextView enterAsOwner;
    TextView enterAsAdmin;
    private String loginMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // attaching different variables to their views using view Id's.
        enterAsUser = findViewById(R.id.textView_as_user);
        enterAsOwner = findViewById(R.id.textView_as_owner);
        enterAsAdmin = findViewById(R.id.textView_as_admin);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        getSupportActionBar().hide();


        // If already logged in then open the specific activity.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            getCurrentUserLoginMode();

            Intent intent = null;
            getCurrentUserLoginMode();
            if (loginMode.equals("owner")) {
                String companyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                intent = new Intent(MainActivity.this, AllSpaceShipsListActivity.class);
                intent.putExtra("companyID", companyId);

            } else {
                intent = new Intent(MainActivity.this, AllListActivity.class);
            }
            intent.putExtra("loginMode", loginMode);
            startActivity(intent);
            finish();
        }

        // for entering in user mode.
        enterAsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                intent.putExtra("loginMode", "user");
                startActivity(intent);
            }
        });

        // for entering in admin mode.
        enterAsAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("loginMode", "admin");
                startActivity(intent);
            }
        });

        // for entering in owner mode.
        enterAsOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                intent.putExtra("loginMode", "owner");
                startActivity(intent);
            }
        });

        // Creating an Intent to start the service
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);

    }

    // if firebase user is not null (is logged in) fetch the loginMode of last login
    // and redirect user to same loginMode.
    private void getCurrentUserLoginMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
        String prevLoginMode = sharedPreferences.getString("loginMode", "");
        String prevLoginEmail = sharedPreferences.getString("email", "");
        String currentUserMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (prevLoginEmail.equals(currentUserMail)) {
            loginMode = prevLoginMode;
        }
    }

}