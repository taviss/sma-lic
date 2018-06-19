package com.sma.smartfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import sma.com.smartfinder.R;

public class SettingsActivity extends BaseActivity {
    private Button changePass;
    private Button logout;
    private Button cameras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        changePass = (Button) findViewById(R.id.change_password_but);
        logout = (Button) findViewById(R.id.logout_but);
        cameras = (Button) findViewById(R.id.cameras_but);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();
                smartFinderApplication.disableLogin();
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class));
            }
        });

        cameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, CamerasActivity.class));
            }
        });
    }

}
