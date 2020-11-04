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

public class ChangePsd extends AppCompatActivity implements Runnable{
    EditText cpNewPsd1,cpNewPsd2;
    Button submit,reset,back;
    SharedPreferences sp;
    int uid;
    Handler handler;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psd);
        initView();
    }

    public void initView(){
        cpNewPsd1 = (EditText)findViewById(R.id.cpNewPsd1);
        cpNewPsd2 = (EditText)findViewById(R.id.cpNewPsd2);
        submit = (Button)findViewById(R.id.cpSubmit);
        reset = (Button)findViewById(R.id.cpReset);
        back = (Button)findViewById(R.id.cpBack);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);
    }

    public void cp_btn_op(View v){
        switch (v.getId()){
            case R.id.cpSubmit:
                Thread t = new Thread(ChangePsd.this);
                t.start();
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 5){
                            String sstr = (String)msg.obj;
                            Toast.makeText(getApplicationContext(),sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.cpReset:
                cpNewPsd1.setText("");
                cpNewPsd2.setText("");
                break;
            case R.id.cpBack:
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
        String psd1 = cpNewPsd1.getText().toString();
        String psd2 = cpNewPsd2.getText().toString();
        //用正则表达式来控制密码强度，(?=.*?[a-zA-Z])表示至少一个字母，(?=.*?[0-9])表示至少一个数字，[a-z0-9A-Z]只能是数字或字母
        String regex = "^(?=.*?[a-zA-Z])(?=.*?[0-9])[a-z0-9A-Z]+$";
        if(psd1.equals(psd2)){
            if (psd1.length() > 10 || psd1.length() < 6) {
                info = "密码长度不符合要求!";
            } else {
                if(psd1.matches(regex)){
                    try {
                        PreparedStatement ps = DBOpenHelper.getConn().prepareStatement("select * from user where uid=?");
                        ps.setInt(1, uid);
                        ResultSet rs = ps.executeQuery();
                        if(rs.next()){
                            ps = DBOpenHelper.getConn().prepareStatement("update user set password=? where uid=?");
                            ps.setString(1,psd1);
                            ps.setInt(2,uid);
                            ps.executeUpdate();
                            Log.i("ChangePsd-run","密码已修改！");
                            info = "密码已修改，请重新登录!";
                            rs.close();
                            ps.close();
                            //转到登录页面
                            config = new Intent(this,Login.class);
                            startActivity(config);
                        } else {
                            info = "无数据!";
                            Log.i("ChangePsd-run","无数据！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    info = "密码格式不符合要求!";
                }
            }
        } else {
            info = "两次输入的密码不相同!";
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 5;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}