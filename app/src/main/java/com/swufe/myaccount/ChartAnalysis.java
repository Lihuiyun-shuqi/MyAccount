package com.swufe.myaccount;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class ChartAnalysis extends AppCompatActivity implements Runnable{
    private static final String TAG = "ChartAnalysis";
    PieChart pieChart;
    ImageView imgHp,imgCa,imgMe;
    SharedPreferences sp;
    int uid;
    Intent config;
    Handler handler;
    ArrayList<PieEntry> pieEntries;
    PieDataSet pieDataSet;
    ArrayList<Integer> colors;
    PieData pieData;
    double all_money = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_analysis);

        initView();
        bindPie();
    }

    public void initView(){
        pieChart = (PieChart)findViewById(R.id.mPieChart);
        pieChart.setUsePercentValues(true);//设置为显示百分比
        pieChart.setDrawSliceText(false);//设置隐藏饼图上文字，只显示百分比
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(8, 5, 8, 5);//设置饼状图距离上下左右的偏移量

        imgHp = (ImageView)findViewById(R.id.imgHp_chartanalysis);
        imgCa = (ImageView)findViewById(R.id.imgCa_chartanalysis);
        imgMe = (ImageView)findViewById(R.id.imgMe_chartanalysis);
        sp = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sp.getInt("user_id",0);
    }

    private void bindPie(){
        pieChart.setDrawCenterText(true);//设置可以绘制中间的文字
        pieChart.setCenterTextColor(Color.parseColor("#6A6969"));//中间的文本颜色
        pieChart.setCenterTextSize(20);//设置中间文本文字的大小
        pieChart.setCenterText("总支出\n" + all_money + "元"); //设置圆环中间的文字
        pieChart.setDragDecelerationFrictionCoef(0.95f);//设置摩擦系数（值越小摩擦系数越大）
        pieChart.setDrawHoleEnabled(true);//true为环形图，false为饼图
        pieChart.setHoleColor(Color.WHITE);//环形图中间的圆的绘制颜色
        pieChart.setHoleRadius(40f);//环形图中间的圆的半径大小
        pieChart.setTransparentCircleColor(Color.WHITE);//设置圆环的颜色
        pieChart.setTransparentCircleAlpha(100);//设置圆环的透明度[0,255]
        pieChart.setTransparentCircleRadius(50f);//设置半透明圆环的半径值
        pieChart.setRotationEnabled(true);//设置饼状图是否可以旋转(默认为true)
        pieChart.setRotationAngle(20);//设置饼状图旋转的角度
        pieChart.setHighlightPerTapEnabled(true);//点击是否放大
        //设置图例
        Legend legend = pieChart.getLegend();//设置比例图
        legend.setEnabled(true);//是否显示图例
        legend.setMaxSizePercent(100);
        legend.setTextSize(20);
        //设置图例位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);//图例相对于图表纵向的位置
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);//图例相对于图表横向的位置
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);//图例显示的方向
        legend.setDrawInside(false);
        legend.setXEntrySpace(3f);
        legend.setYEntrySpace(3f);//设置图例之间Y轴方向上的空白间距值
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);

        pieChart.setDrawEntryLabels(false);//饼图上只显示百分比，不显示文字
        //开启子线程，获取数据库数据
        pieEntries = new ArrayList<PieEntry>();
        Thread t = new Thread(this);
        t.start();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 8){
                    String sstr = (String)msg.obj;
                    Log.i("Register-handler",sstr);
                    Toast.makeText(ChartAnalysis.this,sstr,Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };

        pieChart.animateXY(2500,2500);//设置X,Y轴上的绘制动画
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //点击获取当前块数据
                PieEntry pieEntry = (PieEntry) e;
                Log.i(TAG, "-->label: " + pieEntry.getLabel() + "-->value: " + pieEntry.getValue());
                pieChart.setCenterText(pieEntry.getLabel() + "\n" + pieEntry.getValue() + "元");
            }
            @Override
            public void onNothingSelected() {
                pieChart.setCenterText("总支出\n" + all_money + "元");
            }
        } );
    }

    public void img_op_ca(View imgView){
        switch (imgView.getId()){
            case R.id.imgHp_chartanalysis:
                config = new Intent(this,HomePage.class);
                startActivity(config);
                break;
            case R.id.imgMe_chartanalysis:
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
        int cid;
        String detail_name = "";
        double detail_money;
        try {
            PreparedStatement ps1 = DBOpenHelper.getConn().prepareStatement("select cid,SUM(money) from records where uid=? and rflag=? group by cid");
            ps1.setInt(1,uid);
            ps1.setInt(2,1);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                cid = rs1.getInt(1);
                Log.i("run-cid", String.valueOf(cid));
                PreparedStatement ps2 = DBOpenHelper.getConn().prepareStatement("select cname from category where cid=?");
                ps2.setInt(1,cid);
                ResultSet rs2 = ps2.executeQuery();
                if(rs2.next()){
                    detail_name = rs2.getString(1);
                    Log.i("run-typename",detail_name);
                }
                detail_money = rs1.getDouble(2);
                Log.i("run-money",String.valueOf(detail_money));
                if(detail_money != 0){
                    pieEntries.add(new PieEntry((float) detail_money, detail_name));
                }
                all_money += detail_money;
                rs2.close();
                ps2.close();
            }
            rs1.close();
            ps1.close();

            info = "读取数据成功!";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        pieChart.setCenterText("总支出\n" + all_money + "元");
        Log.i("pieEntries", String.valueOf(pieEntries.size()));
        pieDataSet = new PieDataSet(pieEntries, "");
        //饼图颜色
        colors = new ArrayList<Integer>();
        for(int k = 0; k < pieEntries.size(); k++){
            StringBuilder mBuilder = new StringBuilder();
            mBuilder.delete(0, mBuilder.length());//使用前清空内容
            String haxString;
            for(int i = 0; i < 3; i++){
                haxString = Integer.toHexString(new Random().nextInt(0xFF));
                if(haxString.length() == 1){
                    haxString = "0" + haxString;
                }
                mBuilder.append(haxString);
            }
            colors.add(Color.parseColor("#" + mBuilder.toString()));
        }
        Log.i("colors", String.valueOf(colors.size()));
        pieDataSet.setColors(colors);
        pieDataSet.setSliceSpace(8f);//设置饼块之间的间隔
        pieDataSet.setSelectionShift(15f);//设置饼块选中时偏离饼图中心的距离
        pieData = new PieData();
        pieData.setDataSet(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(15f);
        pieData.setValueTextColor(Color.BLUE);
        pieChart.setData(pieData);
        pieChart.highlightValues(null);

        Message msg = new Message();
        msg.obj = info;
        msg.what = 8;
        handler.sendMessageAtFrontOfQueue(msg);
    }
}