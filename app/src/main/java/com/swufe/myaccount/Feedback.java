package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class Feedback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
    }

    public void fb_btn_op(View v){
        switch (v.getId()){
            case R.id.fbSubmit:
                break;
            case R.id.fbReset:
                //edEmail.setText("");
                break;
            default:
                break;
        }
    }
}