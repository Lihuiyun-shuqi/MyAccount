package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    public void initView(){
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
                            String sstr = (String)msg.obj;
                            //Log.i("Login-handler",sstr);
                            Toast.makeText(Login.this,sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.logResetbutton:
                logTel.setText("");
                logPsd.setText("");
                logImgcode.setText("");
                break;
            case R.id.toRegister:
                config = new Intent(this, Register.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        String info = "noInfo";
        String tel = logTel.getText().toString();
        String psd = logPsd.getText().toString();
        String imgCode = logImgcode.getText().toString();
        int id;//用户ID
        String name;//用户昵称
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
                        if (psd.equals(rs.getString("password"))) {
                            id = rs.getInt("uid");
                            name = rs.getString("uname");
                            info = "登录成功!";
                            ps.close();
                            rs.close();
                            //使用SharedPreferences对象保存用户ID，用户昵称
                            SharedPreferences sp1 = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor1 = sp1.edit();
                            editor1.putInt("user_id",id);
                            editor1.putString("user_name",name);
                            editor1.apply();
                            //将返回的token数据用SharedPreferences将token持久化
                            String andID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);//获取android设备ID
                            SharedPreferences sp2 = getSharedPreferences("token",Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor2 = sp2.edit();
                            String token = andID;
                            editor2.putString("token",token);
                            editor2.apply();
                            //转到记账本首页
                            config = new Intent(this,HomePage.class);
                            startActivity(config);
                        } else {
                            info = "您的密码输入有误!";
                        }
                    } else {
                        info = "您的手机号码输入有误!";
                    }
                } catch (Exception e) {
                }
            } else {
                info = "验证码不正确!";
            }
        } else {
            info = "手机号码不能为空!";
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 2;
        handler.sendMessageAtFrontOfQueue(msg);
        //Log.i("test", String.valueOf(msg.obj));
    }

}