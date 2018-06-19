package com.sma.smartfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

public class ChangePasswordActivity extends BaseActivity {
    private static final String TAG = ChangePasswordActivity.class.getName();

    private EditText oldPass;
    private EditText newPass;
    private EditText newPassRepeat;
    private Button changeBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPass = (EditText) findViewById(R.id.old_password_ch);
        newPass = (EditText) findViewById(R.id.new_password_ch);
        newPassRepeat = (EditText) findViewById(R.id.new_password_repeat_ch);

        changeBut = (Button) findViewById(R.id.change_pass_but_ch);

        oldPass.setFocusable(true);
        newPass.setFocusable(true);
        newPassRepeat.setFocusable(true);

        changeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //oldPass.setFocusable(false);
                //newPass.setFocusable(false);
                //newPassRepeat.setFocusable(false);
                if(newPass.getText().toString().equals(newPassRepeat.getText().toString())) {
                    try {
                        final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();
                        Future<Boolean> loggedIn = HTTPUtility.login(smartFinderApplication.getCameraAddress() + "/login/submit", "userName", smartFinderApplication.getUser(), "userPass", oldPass.getText().toString());
                        if (loggedIn.get()) {
                            Future<byte[]> change = HTTPUtility.changePassword(smartFinderApplication.getCameraAddress() + "/users/password/change", oldPass.getText().toString(), newPass.getText().toString());
                            handleResponse(new String(change.get()));
                        } else {
                            Toast.makeText(getApplicationContext(), "Old password is wrong!", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException|JSONException|InterruptedException|ExecutionException e) {
                        Toast.makeText(getApplicationContext(), "Password change failed! Please try again!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Password doesn't match the repeat!", Toast.LENGTH_LONG).show();
                }
                //oldPass.setFocusable(true);
                //newPass.setFocusable(true);
                //newPassRepeat.setFocusable(true);
            }
        });
    }

    public void handleResponse(String response) {
        if(response != null) {
            if(!response.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Map<String, Object> errors = toMap(jsonObject);
                    if (errors.isEmpty()) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putString("password", newPass.getText().toString()).apply();
                        SmartFinderApplicationHolder.getApplication().updateLoginDetails();
                        startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                        Toast.makeText(getApplicationContext(), "Password change successful!", Toast.LENGTH_LONG).show();
                    } else {
                        String errorString = "";
                        for (String key : errors.keySet()) {
                            errorString += key + ":" + errors.get(key) + "\n";
                        }

                        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.i(TAG, e.getMessage());
                }
            } else {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                preferences.edit().putString("password", newPass.getText().toString()).apply();
                SmartFinderApplicationHolder.getApplication().updateLoginDetails();
                startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "Password change successful!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Password change failed, please try again!", Toast.LENGTH_LONG).show();
        }

    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
