package com.sma.smartfinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

public class RegisterActivity extends BaseActivity {
    private EditText userText;
    private EditText emailText;
    private EditText passText;
    private Button registerButton;
    private TextView loginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();

        userText = findViewById(R.id.register_user);
        passText = findViewById(R.id.register_password);
        emailText = findViewById(R.id.register_mail);
        registerButton = findViewById(R.id.register_button);
        loginView = findViewById(R.id.login_info);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate(userText.getText().toString(), emailText.getText().toString(), passText.getText().toString())) {
                    tryRegister(smartFinderApplication.getCameraAddress(), emailText.getText().toString(), userText.getText().toString(), passText.getText().toString());
                }
            }
        });

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void tryRegister(String address, String mail, String user, String pass) {
        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        try {
            progressDialog.show();
            Future<Boolean> registered = HTTPUtility.register(address + "/users", "userName", user, "userMail", mail, "userPass", pass);
            if (registered.get()) {
                progressDialog.dismiss();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                preferences.edit().putString("username", user).apply();
                preferences.edit().putString("password", pass).apply();
                preferences.edit().putString("camera_server_address", address).apply();
                SmartFinderApplicationHolder.getApplication().updateLoginDetails();
                Log.i("TEST", "Registered in successfuly!");
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            } else {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), "Username or email in use!", Toast.LENGTH_LONG).show();
            }
        } catch(JSONException |IOException |InterruptedException|ExecutionException e) {
            progressDialog.hide();
            Toast.makeText(getApplicationContext(), "Registration failed! Please try again!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validate(String user, String mail, String password) {
        boolean valid = true;

        if (user.isEmpty()) {
            userText.setError("Invalid username!");
            valid = false;
        } else {
            userText.setError(null);
        }

        if (mail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            emailText.setError("Invalid email!");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            passText.setError("At least 6 characters required!");
            valid = false;
        } else {
            passText.setError(null);
        }

        return valid;
    }
}
