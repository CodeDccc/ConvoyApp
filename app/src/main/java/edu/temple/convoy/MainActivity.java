package edu.temple.convoy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
   // private String username; //= usernameText.getText().toString().trim();
  //  private String password;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);

        findViewById(R.id.signinButton).setOnClickListener(v -> {
            final String username = usernameText.getText().toString().trim();
            final String password = passwordText.getText().toString().trim();
            VolleyHelper.getVolleyLogIn(this,"action", "LOGIN", username, password);
        });


        findViewById(R.id.textView).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));

        });
    }

}