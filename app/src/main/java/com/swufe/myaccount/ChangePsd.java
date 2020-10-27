package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ChangePsd extends AppCompatActivity {
    EditText cpNewPsd1,cpNewPsd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psd);
    }

    public void cp_btn_op(View v){
        switch (v.getId()){
            case R.id.cpSubmit:
                break;
            case R.id.cpReset:
                cpNewPsd1.setText("");
                cpNewPsd2.setText("");
                break;
            default:
                break;
        }
    }
}