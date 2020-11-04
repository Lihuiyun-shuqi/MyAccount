package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class Feedback extends AppCompatActivity implements Runnable{
    EditText feedbackText;
    Button submit,reset,back;
    SharedPreferences sp;
    int uid;
    Handler handler;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initView();
    }

    public void initView(){
        feedbackText = (EditText)findViewById(R.id.fbEditText);
        submit = (Button)findViewById(R.id.fbSubmit);
        reset = (Button)findViewById(R.id.fbReset);
        back = (Button)findViewById(R.id.fbBack);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);
    }


    public void fb_btn_op(View v){
        switch (v.getId()){
            case R.id.fbSubmit:
                Thread t = new Thread(Feedback.this);
                t.start();
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 7){
                            String sstr = (String)msg.obj;
                            Toast.makeText(getApplicationContext(),sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.fbReset:
                feedbackText.setText("");
                break;
            case R.id.fbBack:
                //返回Mine页面
                config = new Intent(this,Mine.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        String info = "noInfo";
        String advice = feedbackText.getText().toString();
        int fid;//流水号
        if(advice != null && !advice.equals("")){
            try {
                PreparedStatement ps1 = DBOpenHelper.getConn().prepareStatement("select * from feedback");
                ResultSet rs1 = ps1.executeQuery();
                if (rs1.last()) {
                    fid = rs1.getInt(1) + 1;
                } else {
                    fid = 1;
                }
                Timestamp timeStamp = new Timestamp(new Date().getTime());
                PreparedStatement ps2 = DBOpenHelper.getConn().prepareStatement("insert into feedback values(?,?,?,?,?)");
                ps2.setInt(1,fid);
                ps2.setInt(2,uid);
                ps2.setString(3,advice);
                ps2.setTimestamp(4, timeStamp);
                ps2.setInt(5,1);//1表示后台尚未处理该条意见，0表示后台已处理该条意见
                ps2.executeUpdate();
                ps2.close();
                rs1.close();
                ps1.close();
                info = "请求成功！";
                Log.i("exportData-run","数据库添加成功！");
                //返回Mine页面
                config = new Intent(this,Mine.class);
                startActivity(config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            info = "反馈意见不能为空!";
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 7;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}