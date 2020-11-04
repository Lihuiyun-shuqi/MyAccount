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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddAccount extends AppCompatActivity implements Runnable{
    private static final String TAG = "AddAccount";

    EditText addMoney,addRemark;
    Button submit,reset,back;
    Spinner spinner;
    private List<String> list = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    String typeName;//消费类型名称
    DatePicker datePicker;
    SharedPreferences sp;
    int uid,cid;//用户ID，消费类型编号
    String dateStr,money,remark;
    Handler handler;
    Intent config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        initView();
    }

    public void initView(){
        addMoney = (EditText)findViewById(R.id.addMoneyEdit);
        addRemark = (EditText)findViewById(R.id.addRemarkEdit);
        submit = (Button)findViewById(R.id.addSubmit);
        reset = (Button)findViewById(R.id.addReset);
        back = (Button)findViewById(R.id.addBack);
        spinner = (Spinner)findViewById(R.id.spinner_type);
        datePicker = (DatePicker)findViewById(R.id.add_date);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);

        list.add("餐饮");
        list.add("购物");
        list.add("娱乐");
        list.add("交通");
        list.add("通讯");
        list.add("居家");
        list.add("社交");
        list.add("医疗");
        list.add("学习");
        list.add("礼物");
        list.add("办公");
        list.add("亲友");
        list.add("其他");

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                typeName = spinner.getSelectedItem().toString();
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                arg0.setVisibility(View.VISIBLE);
            }
        });
    }

    public void add_btn_op(View v){
        switch (v.getId()){
            case R.id.addSubmit:
                Thread t = new Thread(AddAccount.this);
                t.start();
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        if(msg.what == 10){
                            String sstr = (String)msg.obj;
                            Toast.makeText(getApplicationContext(),sstr,Toast.LENGTH_SHORT).show();
                        }
                        super.handleMessage(msg);
                    }
                };
                break;
            case R.id.addReset:
                spinner.setSelection(0);
                addMoney.setText("");
                addRemark.setText("");
                break;
            case R.id.addBack:
                //返回首页
                config = new Intent(this, HomePage.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        String info = "noInfo";
        int rid;//流水号
        dateStr = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-"
                + datePicker.getDayOfMonth();
        money = addMoney.getText().toString();
        remark = addRemark.getText().toString();
        if (money.length() !=0 && !money.equals("")){
            try {
                PreparedStatement ps1 = DBOpenHelper.getConn().prepareStatement("select * from records");
                ResultSet rs1 = ps1.executeQuery();
                if (rs1.last()) {
                    rid = rs1.getInt(1) + 1;
                } else {
                    rid = 1;
                }
                PreparedStatement ps2 = DBOpenHelper.getConn().prepareStatement("select cid from category where cname=?");
                ps2.setString(1,typeName);
                ResultSet rs2 = ps2.executeQuery();
                if(rs2.next()){
                    cid = rs2.getInt(1);
                }
                PreparedStatement ps3 = DBOpenHelper.getConn().prepareStatement("insert into records values(?,?,?,?,?,?,?)");
                ps3.setInt(1,rid);
                ps3.setInt(2,uid);
                ps3.setInt(3,cid);
                ps3.setDouble(4,Double.parseDouble(money));
                ps3.setDate(5, java.sql.Date.valueOf(dateStr));
                ps3.setString(6,remark);
                ps3.setInt(7,1);//1表示该条记录存在，0表示删除了该条数据（表现为用户能否看到该条记录）
                ps3.executeUpdate();
                ps3.close();
                rs2.close();
                ps2.close();
                rs1.close();
                ps1.close();
                info = "消费记录成功!";
                Log.i("AddAccount-run","数据库添加成功！");
                //返回首页
                config = new Intent(this,HomePage.class);
                startActivity(config);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            info = "请输入金额！";
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 10;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}