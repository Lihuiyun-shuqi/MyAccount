package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class Coming extends AppCompatActivity {
    private static final String TAG = "Coming";
    ImageView img;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        getSupportActionBar().hide();//隐藏标题栏
        setContentView(R.layout.activity_coming);

        img = (ImageView)findViewById(R.id.comingImg);

        new Thread(){
            @Override
            public void run() {
                try{
                    sleep(2500);//使程序休眠五秒
                    //取登录的token，在第一运行app时，token为空，
                    SharedPreferences sp = getSharedPreferences("token", Activity.MODE_PRIVATE);
                    String token = sp.getString("token","");
                    if (token.isEmpty() || token == null || token.equals("")){
                        //token为空时直接跳转到登录界面
                        Log.i(TAG, "第一次登录!");
                        config = new Intent(Coming.this,Login.class);
                        startActivity(config);
                        finish();//关闭当前活动
                    }else {
                        //token不为空时直接跳转到首页界面
                        Log.i(TAG, "自动登录!");
                        config = new Intent(Coming.this,HomePage.class);
                        startActivity(config);
                        finish();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();//启动线程
    }

}