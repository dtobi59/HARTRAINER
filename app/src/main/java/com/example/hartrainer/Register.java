package com.example.hartrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
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
import com.google.common.collect.Range;

public class Register extends AppCompatActivity {

    SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    Button _register_button, _login_button;
    EditText _name, _email, _phone, _password, _weight, _height;
    long user_id;

    //defining AwesomeValidation object
    private AwesomeValidation awesomeValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //initializing awesomevalidation object
        /*
         * The library provides 3 types of validation
         * BASIC
         * COLORATION
         * UNDERLABEL
         * */
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        openHelper = new DatabaseHelper(this);
        _name = (EditText)findViewById(R.id.name);
        _email = (EditText)findViewById(R.id.email);
        _phone = (EditText)findViewById(R.id.phone);
        _weight = (EditText)findViewById(R.id.weight);
        _height = (EditText)findViewById(R.id.height);
        _password = (EditText)findViewById(R.id.password);
        _register_button=(Button)findViewById(R.id.register_button);
        _login_button=(Button)findViewById(R.id.login_button);

        //adding validation to edittexts
        awesomeValidation.addValidation(this, R.id.name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.email, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.phone, "(^[0]\\d{10}$)|(^[\\+]?[234]\\d{12}$)", R.string.phoneerror);
        awesomeValidation.addValidation(this, R.id.weight, Range.closed(30, 500 ), R.string.weighterror);
        awesomeValidation.addValidation(this, R.id.height, Range.closed(3, 20), R.string.heighterror);
        awesomeValidation.addValidation(this, R.id.password, "^(?=.*\\d).{4,8}$", R.string.passworderror);
//      awesomeValidation.addValidation(this, R.id.editTextAge, Range.closed(13, 60), R.string.ageerror);


        _register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awesomeValidation.validate()) {
                    db = openHelper.getWritableDatabase();
                    String name = _name.getText().toString();
                    String email = _email.getText().toString();
                    String phone = _phone.getText().toString();
                    String password = _password.getText().toString();
                    String height = _height.getText().toString();
                    String weight = _weight.getText().toString();


                    user_id = insertData(name, email, phone, password,height,weight);
                    Log.i("User_id", String.valueOf(user_id));
                    String greetings = name + ",";

                    Intent intent = new Intent(Register.this, MainActivity.class);
                    intent.putExtra("greeting", greetings);
                    intent.putExtra("user_id",  String.valueOf(user_id));

                    startActivity(intent);

                    Toast.makeText(getApplicationContext(), "register successfully", Toast.LENGTH_LONG).show();


                }
            }

        });

        _login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });
    }

    public long insertData(String name, String email, String phone, String password, String height, String weight){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TABLE1_COL3, name);
        contentValues.put(DatabaseHelper.TABLE1_COL5, password);
        contentValues.put(DatabaseHelper.TABLE1_COL2, email);
        contentValues.put(DatabaseHelper.TABLE1_COL4, phone);
        contentValues.put(DatabaseHelper.TABLE1_COL6, height);
        contentValues.put(DatabaseHelper.TABLE1_COL7, weight);
        long id = db.insert(DatabaseHelper.TABLE1, null, contentValues);
        Log.i("Database_reg", String.valueOf(id));
        return id;
    }
}
