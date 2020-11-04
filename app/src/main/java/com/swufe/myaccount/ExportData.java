package com.swufe.myaccount;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportData extends Activity implements Runnable{
    EditText edEmail;
    Button submit,reset,back;
    Spinner spinner;
    private List<String> list = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    int uid;
    String timeSlot,userEmail;
    SharedPreferences sp;
    Handler handler;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        initView();
    }

    public void initView(){
        edEmail = (EditText)findViewById(R.id.edEmail);
        submit = (Button)findViewById(R.id.edSubmit);
        reset = (Button)findViewById(R.id.edReset);
        back = (Button)findViewById(R.id.edBack);
        spinner = (Spinner)findViewById(R.id.spinner_slot);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);

        //添加下拉列表项的list，这里添加的项就是下拉列表的菜单项
        list.add("最近一周");
        list.add("最近一个月");
        list.add("最近三个月");
        list.add("最近半年");
        list.add("最近一年");
        list.add("所有时间");
        //为下拉列表定义一个适配器，这里用到里前面定义的list。
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);
        //为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到下拉列表上
        spinner.setAdapter(adapter);
        //为下拉列表设置各种事件的响应
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //将所选内容的值赋给timeSlot
                timeSlot = spinner.getSelectedItem().toString();
                //将spinner显示
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                arg0.setVisibility(View.VISIBLE);
            }
        });
    }

    public void ed_btn_op(View v){
        switch (v.getId()){
            case R.id.edSubmit:
                Thread t = new Thread(ExportData.this);
                t.start();
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 6){
                            String sstr = (String)msg.obj;
                            Toast.makeText(getApplicationContext(),sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.edReset:
                spinner.setSelection(0);
                edEmail.setText("");
                break;
            case R.id.edBack:
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
        userEmail = edEmail.getText().toString();
        int eid;//流水号
        //判断手机号是否合法
        String regex = "^([A-Za-z0-9_\\-\\.\\u4e00-\\u9fa5])+\\@([A-Za-z0-9_\\-\\.])+\\.([A-Za-z]{2,8})$";
        if (userEmail != null && !userEmail.equals("")) {
            if (userEmail.matches(regex)) {
                try {
                    PreparedStatement ps1 = DBOpenHelper.getConn().prepareStatement("select * from exportdata");
                    ResultSet rs1 = ps1.executeQuery();
                    if (rs1.last()) {
                        eid = rs1.getInt(1) + 1;
                    } else {
                        eid = 1;
                    }
                    Timestamp timeStamp = new Timestamp(new Date().getTime());
                    PreparedStatement ps2 = DBOpenHelper.getConn().prepareStatement("insert into exportdata values(?,?,?,?,?,?)");
                    ps2.setInt(1,eid);
                    ps2.setInt(2,uid);
                    ps2.setString(3,timeSlot);
                    ps2.setString(4,userEmail);
                    ps2.setTimestamp(5,timeStamp);
                    ps2.setInt(6,1);//1表示后台尚未处理该条请求，0表示后台已处理该条请求
                    ps2.executeUpdate();
                    ps2.close();
                    rs1.close();
                    ps1.close();
                    info = "请求成功!";
                    Log.i("exportData-run","数据库添加成功！");
                    //返回Mine页面
                    config = new Intent(this,Mine.class);
                    startActivity(config);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                info = "邮箱地址不合法!";
            }
        } else {
            info = "邮箱地址不能为空!";
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 6;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}