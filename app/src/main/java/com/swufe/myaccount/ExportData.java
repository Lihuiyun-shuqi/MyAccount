package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ExportData extends AppCompatActivity {
    EditText edEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);
    }

    public void ed_btn_op(View v){
        switch (v.getId()){
            case R.id.edSubmit:
                break;
            case R.id.edReset:
                edEmail.setText("");
                break;
            default:
                break;
        }
    }
}