package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class HomePage extends AppCompatActivity implements Runnable, AdapterView.OnItemLongClickListener{
    private static final String TAG = "HomePage";
    TextView allOut;
    double allMoney;
    ListView listView;
    detailListAdapter adapter;
    ImageView imgHp,imgCa,imgMe;
    SharedPreferences sp;
    int uid;
    Intent config;
    Handler handler;
    ArrayList<HashMap<String,String>> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initView();

        Thread t = new Thread(this);
        t.start();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 9){
                    dataList = (ArrayList<HashMap<String,String>>)msg.obj;
                    adapter = new detailListAdapter(HomePage.this,
                            R.layout.detail_item_list,
                            dataList);
                    listView.setAdapter(adapter);
                    listView.setEmptyView(findViewById(R.id.listnodata));
                    listView.setOnItemLongClickListener(HomePage.this);
                    adapter.notifyDataSetChanged();
                }
                super.handleMessage(msg);
            }
        };
        Log.i(TAG,"显示数据成功!");
    }

    public void initView(){
        allOut = (TextView)findViewById(R.id.hpAllOut);
        listView = (ListView)findViewById(R.id.hp_detaillist);
        imgHp = (ImageView)findViewById(R.id.imgHp_homepage);
        imgCa = (ImageView)findViewById(R.id.imgCa_homepage);
        imgMe = (ImageView)findViewById(R.id.imgMe_homepage);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);
    }

    public void img_op_hp(View imgView){
        switch (imgView.getId()){
            case R.id.imgHp_homepage:
                config = new Intent(this,AddAccount.class);
                startActivity(config);
                break;
            case R.id.imgCa_homepage:
                config = new Intent(this,ChartAnalysis.class);
                startActivity(config);
                break;
            case R.id.imgMe_homepage:
                config = new Intent(this,Mine.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        int cid;
        String type_name = "";
        String dateStr = "";
        String remark = "";
        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
        try {
            PreparedStatement ps1 = DBOpenHelper.getConn().prepareStatement("select * from records where uid=? and rflag=?");
            ps1.setInt(1,uid);
            ps1.setInt(2,1);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                cid = rs1.getInt("cid");
                PreparedStatement ps2 = DBOpenHelper.getConn().prepareStatement("select cname from category where cid=?");
                ps2.setInt(1,cid);
                ResultSet rs2 = ps2.executeQuery();
                if(rs2.next()){
                    type_name = rs2.getString(1);
                }
                dateStr = String.valueOf(rs1.getDate("rtime"));
                remark = rs1.getString("remarks");
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("itemNumber", String.valueOf(rs1.getInt("rid")));
                map.put("itemType",type_name);
                map.put("itemDate",dateStr);
                map.put("itemRemark",remark);
                map.put("itemMoney",String.valueOf(rs1.getDouble("money")));
                list.add(map);
                allMoney += rs1.getDouble("money");
                rs2.close();
                ps2.close();
            }
            rs1.close();
            ps1.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        DecimalFormat df = new DecimalFormat("0.00");
        allOut.setText("总支出:  " + String.valueOf(df.format(allMoney)) + "(元)");
        Message msg = new Message();
        msg.obj = list;
        msg.what = 9;
        handler.sendMessageAtFrontOfQueue(msg);
        Log.i("HomePage-run","读取数据成功!");
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //长按处理
        final int pp = position;

        Object itemAtPosition = listView.getItemAtPosition(position);//获取ListView中点击的数据
        HashMap<String,String> map = (HashMap<String, String>) itemAtPosition;
        final String ItemNumber = map.get("itemNumber");

        //AlertDialog对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" 提示")
                .setMessage(" 请确认是否删除该条记录 ")
                .setPositiveButton(" 是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("HomePage-onItemClick", "onItemLongClick: 长按对话框事件处理");
                        //删除数据项
                        dataList.remove(pp);
                        //更新适配器
                        adapter.notifyDataSetChanged();
                        Log.i("HomePage-onItemClick", "onItemLongClick: 删除的当前数据流水号为：ItemNumber= " + ItemNumber);
                        new Thread(){
                            public void run(){
                                try {
                                    PreparedStatement ps = DBOpenHelper.getConn().prepareStatement("update records set rflag=? where rid=?");
                                    ps.setInt(1,0);
                                    ps.setInt(2,Integer.parseInt(ItemNumber));
                                    ps.executeUpdate();
                                    Log.i("HomePage-onItemClick","onItemLongClick: 数据库更新成功，将flag改为0!");
                                    ps.close();
                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }
                            }
                        }.start();
                    }
                }).setNegativeButton(" 否", null);
        builder.create().show();
        return true;
    }
}