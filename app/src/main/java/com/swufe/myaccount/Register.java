package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

//注册页面
public class Register extends AppCompatActivity implements Runnable{
    private static final String TAG = "Register";
    EditText regisTel,regisName,regisPsd1,regisPsd2,regisImgcode;
    Button regisRegister,regisReset;
    TextView toLogin;
    ImageView regisImg;
    ImgCode RegisimgCode;
    Bitmap bitmap;
    Handler handler;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    public void initView(){
        regisTel = (EditText)findViewById(R.id.regisTeleditTextPhone);
        regisName = (EditText)findViewById(R.id.regisNameeditText);
        regisPsd1 = (EditText)findViewById(R.id.regisPsdeditTextTextPassword1);
        regisPsd2 = (EditText)findViewById(R.id.regisPsdeditTextTextPassword2);
        regisImgcode = (EditText)findViewById(R.id.regisImgcodeeditText);
        regisRegister = (Button)findViewById(R.id.regisRegisbutton);
        regisReset = (Button)findViewById(R.id.regisResetbutton);
        toLogin = (TextView)findViewById(R.id.toLogin);
        regisImg = (ImageView)findViewById(R.id.regisImgimageView);

        RegisimgCode = ImgCode.getInstance();
        bitmap = RegisimgCode.createBitmap();
        regisImg.setImageBitmap(bitmap);
    }

    public void regis_btn_op(View v){
        switch (v.getId()){
            case R.id.regisImgimageView:
                RegisimgCode = ImgCode.getInstance();
                bitmap = RegisimgCode.createBitmap();
                regisImg.setImageBitmap(bitmap);
                break;
            case R.id.regisRegisbutton:
                Thread t = new Thread(this);
                t.start();

                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 1){
                            String sstr = (String)msg.obj;
                            //Log.i("Register-handler",sstr);
                            Toast.makeText(Register.this,sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.regisResetbutton:
                regisTel.setText("");
                regisName.setText("");
                regisPsd1.setText("");
                regisPsd2.setText("");
                regisImgcode.setText("");
                break;
            case R.id.toLogin:
                config = new Intent(this,Login.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        String info = "noInfo";
        String tel = regisTel.getText().toString();
        String name = regisName.getText().toString();
        String psd1 = regisPsd1.getText().toString();
        String psd2 = regisPsd2.getText().toString();
        String imgCode = regisImgcode.getText().toString();
        boolean flag = false;
        int id = 0;//数据库表的用户ID
        String correctImg = RegisimgCode.getCode();//获取正确验证码
        //用正则表达式来控制密码强度，(?=.*?[a-zA-Z])表示至少一个字母，(?=.*?[0-9])表示至少一个数字，[a-z0-9A-Z]只能是数字或字母
        String regex = "^(?=.*?[a-zA-Z])(?=.*?[0-9])[a-z0-9A-Z]+$";
        //判断手机号是否合法，表示以1开头，第二位可能是3/4/5/6/7/8/9等的任意一个，在加上后面的\d表示数字[0-9]的9位，总共加起来11位结束
        String detail = "^1(3|4|5|6|7|8|9)\\d{9}$";
        if (tel != null && name != null && psd1 != null && psd2 != null && imgCode != null
                && !tel.equals("") && !name.equals("") && !psd1.equals("") && !psd2.equals("") && !imgCode.equals("")) {
            if (psd1.equals(psd2)) {
                if (psd1.length() > 10 || psd1.length() < 6) {
                    info = "密码长度不符合要求!";
                } else {
                    if (imgCode.equalsIgnoreCase(correctImg)) {
                        if (psd1.matches(regex)) {
                            if (tel.length() != 11) {
                                info = "手机号码位数不符合要求!";
                            } else {
                                if (tel.matches(detail)) {
                                    try {
                                        PreparedStatement ps1 = DBOpenHelper.getConn().prepareStatement("select * from user");
                                        ResultSet rs1 = ps1.executeQuery();
                                        if (rs1.last()) {
                                            id = rs1.getInt(1) + 1;
                                        } else {
                                            id = 1;
                                        }
                                        PreparedStatement ps2 = DBOpenHelper.getConn().prepareStatement("select telnumber from user");
                                        ResultSet rs2 = ps2.executeQuery();
                                        while (rs2.next()) {
                                            if (tel.equals(rs2.getString("telnumber"))) {
                                                flag = true;
                                            }
                                        }
                                        if (flag == false) {
                                            try {
                                                PreparedStatement ps3 = DBOpenHelper.getConn().prepareStatement("insert into user values(?,?,?,?)");
                                                ps3.setInt(1, id);
                                                ps3.setString(2, tel);
                                                ps3.setString(3, name);
                                                ps3.setString(4, psd1);
                                                ps3.executeUpdate();
                                                ps3.close();
                                                rs2.close();
                                                ps2.close();
                                                rs1.close();
                                                ps1.close();

                                                info = "成功注册，请登录!";
                                                Log.i("Register-run","注册成功");
                                                //转到登录页面
                                                config = new Intent(this,Login.class);
                                                startActivity(config);
                                            } catch (Exception e) {
                                                info = "注册失败!";
                                            }
                                        } else {
                                            info = "该手机号已被注册!";
                                        }
                                    } catch (Exception e) {
                                    }
                                } else {
                                    info = "手机号码不合法!";
                                }
                            }
                        } else {
                            info = "密码格式不符合要求!";
                        }
                    } else {
                        info = "验证码不正确!";
                    }
                }
            } else {
                info = "两次输入的密码不相同!";
            }
        } else {
            info = "您的信息未完整填写!";
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 1;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}