package edu.temple.convoy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    SharedPreferences sharedPref;
    private String checkSavedName;
   // private String checkSavedPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
       // checkSavedName = sharedPref.getString("username", null);
       // if(checkSavedName!=null){

           // VolleyHelper.getVolleyLogIn(this,"action", "LOGIN", checkSavedName, password);
      //  }
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);

        findViewById(R.id.signinButton).setOnClickListener(v -> {
            final String username = usernameText.getText().toString().trim();
            final String password = passwordText.getText().toString().trim();
            VolleyHelper.getVolleyLogIn(this,"action", "LOGIN", username, password);
            usernameText.setText("");
            passwordText.setText("");
        });


        findViewById(R.id.textView).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));

        });


    }

}