package com.example.mycall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity  implements View.OnClickListener {
    private static final String TAG = "SignUpActivity";
    TextInputLayout text_input_layout_phone_number;
    MaterialButton btn_sign_up;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        text_input_layout_phone_number = findViewById(R.id.text_input_layout_phone_number);
        btn_sign_up = findViewById(R.id.btn_sign_up);

        btn_sign_up.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        if (v.getId()==text_input_layout_phone_number.getId()){
            text_input_layout_phone_number.getEditText().setCursorVisible(true);
        }
        else if (v.getId()==btn_sign_up.getId()){
            String phoneNumber = text_input_layout_phone_number.getEditText().getText().toString();
            if (phoneNumber.length() != 11) {
                Toast.makeText(this, "Valid 11 digit Phone Number required!", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences sp = getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("phoneNumber", phoneNumber);
            editor.apply();
            startActivity(new Intent(SignUpActivity.this, MainActivity.class)
            .putExtra("phoneNumber", phoneNumber));
             finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp = getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE);
        String phoneNumber = sp.getString("phoneNumber", null);
        if (phoneNumber != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class)
                    .putExtra("phoneNumber", phoneNumber));
            finish();
        }
    }
}