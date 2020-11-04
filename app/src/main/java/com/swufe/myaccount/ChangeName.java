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
import android.widget.TextView;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangeName extends AppCompatActivity implements Runnable{
    EditText newName;
    TextView oldName;
    Button submit,reset,back;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    int uid;
    String uname;
    Handler handler;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        initView();
    }

    public void initView(){
        newName = (EditText)findViewById(R.id.cnNameeditText);
        oldName = (TextView)findViewById(R.id.cnOldName2);
        submit = (Button)findViewById(R.id.cnSubmit);
        reset = (Button)findViewById(R.id.cnReset);
        back = (Button)findViewById(R.id.cnBack);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);
        uname = sp.getString("user_name","");
        oldName.setText(uname);
    }

    public void cn_btn_op(View v){
        switch (v.getId()){
            case R.id.cnSubmit:
                Thread t = new Thread(ChangeName.this);
                t.start();
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 4){
                            String sstr = (String)msg.obj;
                            Toast.makeText(getApplicationContext(),sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.cnReset:
                newName.setText("");
                break;
            case R.id.cnBack:
                //返回Mine页面
                config = new Intent(this, Mine.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        String info = "noInfo";
        String nname = newName.getText().toString();
        try {
            if (nname != null && !nname.equals("")) {
                PreparedStatement ps = DBOpenHelper.getConn().prepareStatement("select * from user where uid=?");
                ps.setInt(1, uid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ps = DBOpenHelper.getConn().prepareStatement("update user set uname=? where uid=?");
                    ps.setString(1, nname);
                    ps.setInt(2, uid);
                    ps.executeUpdate();
                    Log.i("ChangeName-run", "昵称已修改！");
                    info = "昵称已修改!";
                    sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putString("user_name",nname);
                    editor.apply();
                    rs.close();
                    ps.close();
                    //返回Mine页面
                    config = new Intent(this, Mine.class);
                    startActivity(config);
                } else {
                    info = "无数据!";
                    Log.i("ChangeName-run", "无数据！");
                }
            } else {
                info = "昵称不能为空！";
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 4;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}