package com.sma.smartfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

/**
 * Activity for logging a user in
 */
public class LoginActivity extends BaseActivity {
    /**
     * Username
     */
    private EditText userText;

    /**
     * Password
     */
    private EditText passText;

    /**
     * Login button
     */
    private Button loginButton;

    /**
     * Link to registration activity
     */
    private TextView signupView;

    /**
     * Link to password reset activity
     */
    private TextView resetPassView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Retrieve saved user information
        final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();
        final String user = smartFinderApplication.getUser();
        final String pass = smartFinderApplication.getPass();
        final String address = smartFinderApplication.getCameraAddress();
        final boolean tryLogin = smartFinderApplication.tryLogin();

        // Try logging in
        if(user != null && address != null && pass != null && tryLogin) {
            tryLogin(address, user, pass);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userText = findViewById(R.id.login_user);
        passText = findViewById(R.id.login_password);

        if(user != null)
            userText.setText(user);

        if(pass != null)
            passText.setText(pass);

        loginButton = findViewById(R.id.login_button);
        signupView = findViewById(R.id.register_info);
        resetPassView = findViewById(R.id.reset_password);

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
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        resetPassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Future<Boolean> reset = HTTPUtility.resetPassword(smartFinderApplication.getCameraAddress() + "/reset/password/submit", smartFinderApplication.getUser());
                    if (reset.get()) {
                        Toast.makeText(getApplicationContext(), "A link was sent to your email address!", Toast.LENGTH_LONG).show();
                    }
                } catch(InterruptedException|ExecutionException e) {
                    Toast.makeText(getApplicationContext(), "Unable to reset password!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Attempt a log in and finishes this activity if successful
     * @param address
     * @param user
     * @param pass
     */
    public void tryLogin(final String address, final String user, final String pass) {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog.show();
                    Future<Boolean> loggedIn = HTTPUtility.login(address + "/login/submit", "userName", user, "userPass", pass);
                    if (loggedIn.get()) {
                        progressDialog.dismiss();
                        // Update user info
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putString("username", user).apply();
                        preferences.edit().putString("password", pass).apply();
                        preferences.edit().putString("camera_server_address", address).apply();
                        SmartFinderApplicationHolder.getApplication().updateLoginDetails();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        progressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Login failed! Please try again!", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                } catch(JSONException|IOException|InterruptedException|ExecutionException e) {
                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();

    }

    /**
     * Validates the username and password
     * @param user
     * @param password
     * @return
     */
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
