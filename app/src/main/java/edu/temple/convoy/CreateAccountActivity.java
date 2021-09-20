package edu.temple.convoy;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class CreateAccountActivity extends AppCompatActivity {
    EditText firstnameText, lastnameText, usernameText, passwordText;
    final String URL = "https://kamorris.com/lab/convoy/account.php";
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /**text boxes used to collect user info*/
        firstnameText = findViewById(R.id.firstnameText);
        lastnameText = findViewById(R.id.lastnameText);
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        /**button to register user*/
        findViewById(R.id.registerButton).setOnClickListener(v ->{
            register("action", "REGISTER");
            firstnameText.setText("");
            lastnameText.setText("");
            usernameText.setText("");
            passwordText.setText("");
        });

        /**cancel to cancel the registration process and go back to first screen*/
        findViewById(R.id.cancelButton).setOnClickListener(v ->{
            startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
        });
    }

    /**registration function makes a call to volley helper class to make a POST request*/
    private void register(String action, String actionType){
        try {
            final String firstname = firstnameText.getText().toString().trim();
            final String lastname = lastnameText.getText().toString().trim();
            final String username = usernameText.getText().toString().trim();
            final String password = passwordText.getText().toString().trim();
            VolleyHelper.getVolleyRegister(this, action, actionType, firstname, lastname, username, password);
        } catch (Exception e) {
            Log.d("LOGGING", e.getLocalizedMessage());
        }
    }
}