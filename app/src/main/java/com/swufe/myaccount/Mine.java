package com.swufe.myaccount;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Mine extends AppCompatActivity implements Runnable{
    private static final String TAG = "Mine";
    TextView oname,newName,newPsd,exportData,deleteData,feedback;
    ImageView imgHp,imgCa,imgMe,imgExit;
    Intent config;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    int uid;
    String uname;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);

        initView();
    }
    public void initView(){
        oname = (TextView)findViewById(R.id.meText_name);
        newName = (TextView)findViewById(R.id.meText_newname);
        newPsd = (TextView)findViewById(R.id.meText_newpsd);
        exportData = (TextView)findViewById(R.id.meText_export);
        deleteData = (TextView)findViewById(R.id.meText_delete);
        feedback = (TextView)findViewById(R.id.meText_feedback);
        imgHp = (ImageView)findViewById(R.id.imgHp_mine);
        imgCa = (ImageView)findViewById(R.id.imgCa_mine);
        imgMe = (ImageView)findViewById(R.id.imgMe_mine);
        imgExit = (ImageView)findViewById(R.id.imgExit_mine);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);
        uname = sp.getString("user_name","");
        oname.setText(uname);
    }

    public void op_me(View v){
        switch (v.getId()){
            case R.id.imgExit_mine:
                sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                editor = sp.edit();
                editor.remove("user_id");
                editor.remove("user_name");
                editor.apply();
                sp = getSharedPreferences("token", Activity.MODE_PRIVATE);
                editor = sp.edit();
                editor.putString("token","");
                editor.apply();
                Log.i("Exit-btn","已将token改为空!");
                Toast.makeText(getApplicationContext(),"已退出!",Toast.LENGTH_SHORT).show();
                config = new Intent(this,Login.class);
                startActivity(config);
                break;
            case R.id.meText_newname:
                config = new Intent(this,ChangeName.class);
                startActivity(config);
                break;
            case R.id.meText_newpsd:
                config = new Intent(this,ChangePsd.class);
                startActivity(config);
                break;
            case R.id.meText_export:
                config = new Intent(this,ExportData.class);
                startActivity(config);
                break;
            case R.id.meText_delete:
                //AlertDialog对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(" 提示")
                        .setMessage(" 请确认是否删除所有数据 ")
                        .setPositiveButton(" 是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onClick:  对话框事件处理");
                                Thread t = new Thread(Mine.this);
                                t.start();
                                handler = new Handler(){
                                    @Override
                                    public void handleMessage(Message msg){
                                        if(msg.what == 3){
                                            String sstr = (String)msg.obj;
                                            Toast.makeText(getApplicationContext(),sstr,Toast.LENGTH_SHORT).show();
                                        }
                                        super.handleMessage(msg);
                                    }
                                };
                            }
                        }).setNegativeButton(" 否", null);
                builder.create().show();
                break;
            case R.id.meText_feedback:
                config = new Intent(this,Feedback.class);
                startActivity(config);
                break;
            case R.id.imgHp_mine:
                config = new Intent(this,HomePage.class);
                startActivity(config);
                break;
            case R.id.imgCa_mine:
                config = new Intent(this,ChartAnalysis.class);
                startActivity(config);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        String info = "noInfo";
        try {
            PreparedStatement ps = DBOpenHelper.getConn().prepareStatement("select * from records where uid=? and rflag=?");
            ps.setInt(1, uid);
            ps.setInt(2,1);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ps = DBOpenHelper.getConn().prepareStatement("update records set rflag=? where uid=?");
                ps.setInt(1,0);
                ps.setInt(2,uid);
                ps.executeUpdate();
                Log.i("Mine-run","已将标志改为0！");
                info = "已删除全部数据!";
                rs.close();
                ps.close();
            } else {
                info = "无数据!";
                Log.i("Mine-run","无数据！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Message msg = new Message();
        msg.obj = info;
        msg.what = 3;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}