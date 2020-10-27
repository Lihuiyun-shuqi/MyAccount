package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

//登录页面
public class Login extends AppCompatActivity implements Runnable{
    private static final String TAG = "Login";
    EditText logTel,logPsd,logImgcode;
    Button logLogin,logReset;
    TextView toRegister;
    ImageView logImg;
    ImgCode LogimgCode;
    Bitmap bitmap;
    Handler handler;
    Toast toast;
    Intent config;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logTel = (EditText)findViewById(R.id.logTeleditTextPhone);
        logPsd = (EditText)findViewById(R.id.logPsdeditTextTextPassword);
        logImgcode = (EditText)findViewById(R.id.logImgcodeeditText);
        logLogin = (Button)findViewById(R.id.logLoginbutton);
        logReset = (Button)findViewById(R.id.logResetbutton);
        toRegister = (TextView)findViewById(R.id.toRegister);
        logImg = (ImageView)findViewById(R.id.logImgimageView);

        LogimgCode = ImgCode.getInstance();
        bitmap = LogimgCode.createBitmap();
        logImg.setImageBitmap(bitmap);
    }

    public void log_btn_op(View v){
        switch (v.getId()){
            case R.id.logImgimageView:
                LogimgCode = ImgCode.getInstance();
                bitmap = LogimgCode.createBitmap();
                logImg.setImageBitmap(bitmap);
                break;
            case R.id.logLoginbutton:
                Thread t = new Thread(this);
                t.start();

                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 2){
                            uid = (String)msg.obj;
                            Log.i(TAG,"用户ID: " + uid);
                        }
                        super.handleMessage(msg);
                    }
                };
                //转到记账本首页
                config = new Intent(this,FrameActivity.class);
                config.putExtra("userId",uid);
                startActivity(config);

                break;
            case R.id.logResetbutton:
                logTel.setText("");
                logPsd.setText("");
                logImgcode.setText("");
                break;
            case R.id.toRegister:
                config = new Intent(this,MainActivity.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        Message msg = handler.obtainMessage(2);
        String tel = logTel.getText().toString();
        String psd = logPsd.getText().toString();
        String imgCode = logImgcode.getText().toString();
        String id;
        boolean flag = false;
        int i = 0;
        String correctImg = LogimgCode.getCode();//获取正确验证码
        if (tel.length() != 0) {
            if (imgCode.equalsIgnoreCase(correctImg)) {
                try {
                    PreparedStatement ps = DBOpenHelper.getConn().prepareStatement("select * from user");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        if (tel.equals(rs.getString("telnumber"))) {
                            flag = true;
                            i = rs.getRow();//获取当前信息的行数，用于定位
                        }
                    }
                    if (flag == true) {
                        rs.absolute(i);//直接定位到上一条信息
                        if (psd.equals(rs.getString("psd"))) {
                            id = rs.getString("no");
                            msg.obj = id;
                            ps.close();
                            rs.close();
                        } else {
                            toast = Toast.makeText(this, "您的密码输入有误！", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                    } else {
                        toast = Toast.makeText(this, "您的手机号码输入有误！", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                } catch (Exception e) {
                }
            } else {
                toast = Toast.makeText(this, "验证码不正确！", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        } else {
            toast = Toast.makeText(this, "手机号码不能为空！", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
        handler.sendMessage(msg);
    }

}