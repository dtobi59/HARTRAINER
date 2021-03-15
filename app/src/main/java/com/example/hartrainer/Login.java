package com.example.hartrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

import org.json.JSONObject;

public class Login extends AppCompatActivity {

    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Cursor cursor;
    Button _btnLogin, _register_button;
    EditText _email, _password;
    private AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initializing awesomevalidation object
        /*
         * The library provides 3 types of validation
         * BASIC
         * COLORATION
         * UNDERLABEL
         * */
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        _email=(EditText)findViewById(R.id.email);
        _password=(EditText)findViewById(R.id.password);
        _btnLogin=(Button)findViewById(R.id.login_button);
        _register_button=(Button)findViewById(R.id.register_button);
        openHelper=new DatabaseHelper(this);
        db = openHelper.getReadableDatabase();

        awesomeValidation.addValidation(this, R.id.email, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.password, "^(?=.*\\d).{4,8}$", R.string.passworderror);


        _btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (awesomeValidation.validate()) {
                    String email = _email.getText().toString();
                    String pass = _password.getText().toString();

                    Cursor cursor = db.rawQuery("SELECT *FROM " + DatabaseHelper.TABLE1 + " WHERE " + DatabaseHelper.TABLE1_COL2 + "=? AND " + DatabaseHelper.TABLE1_COL5 + "=?", new String[]{email, pass});
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();

                            String name = "";
                            String user_id = "";
                            if (cursor.moveToFirst()) {
                                do {
                                    user_id = cursor.getString(0);
                                    name = cursor.getString(1);
                                    Log.d("User Name", name);

                                }
                                while (cursor.moveToNext());
                            }

                            String greeting = "Hello, " + name;
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("greeting", greeting);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);


                        }

                    } else {
                        Toast.makeText(getBaseContext(), "Login error!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        _register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }
}
