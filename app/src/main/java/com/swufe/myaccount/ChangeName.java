package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ChangeName extends AppCompatActivity {
    EditText cnNewName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);
    }

    public void cn_btn_op(View v){
        switch (v.getId()){
            case R.id.cnSubmit:
                break;
            case R.id.cnReset:
                cnNewName.setText("");
                break;
            default:
                break;
        }
    }
}