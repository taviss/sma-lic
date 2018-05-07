package com.sma.smartfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class LoginActivity extends BaseActivity {
    private EditText userText;
    private EditText passText;
    private Button loginButton;
    private TextView signupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userText = findViewById(R.id.login_user);
        passText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupView = findViewById(R.id.register_info);

        final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();
        final String user = smartFinderApplication.getUser();
        final String pass = smartFinderApplication.getPass();
        final String address = smartFinderApplication.getCameraAddress();

        if(user != null && address != null && pass != null) {
            tryLogin(address, user, pass);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate(userText.getText().toString(), passText.getText().toString())) {
                    tryLogin(smartFinderApplication.getCameraAddress(), userText.getText().toString(), passText.getText().toString());
                }
            }
        });

        signupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void tryLogin(String address, String user, String pass) {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        try {
            progressDialog.show();
            Future<Boolean> loggedIn = HTTPUtility.login(address + "/login/submit", "userName", user, "userPass", pass);
            if (loggedIn.get()) {
                progressDialog.dismiss();
                Log.i("TEST", "Logged in successfuly!");
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), "Login failed! Please try again!", Toast.LENGTH_LONG).show();
            }
        } catch(JSONException|IOException|InterruptedException|ExecutionException e) {
            progressDialog.hide();
            Toast.makeText(getApplicationContext(), "Login failed! Please try again!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validate(String user, String password) {
        boolean valid = true;

        if (user.isEmpty()) {
            userText.setError("Invalid username!");
            valid = false;
        } else {
            userText.setError(null);
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
