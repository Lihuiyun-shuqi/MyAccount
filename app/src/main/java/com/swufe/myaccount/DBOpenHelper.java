package com.swufe.myaccount;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBOpenHelper {
    private static String diver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://10.64.124.9:3306/myaccount?characterEncoding=utf-8";//处理乱码问题
    private static String username = "root";//用户名
    private static String password = "root";//密码

    //连接数据库
    public static Connection getConn(){
        Connection conn = null;
        try {
            Class.forName(diver);
            conn = (Connection)DriverManager.getConnection(url,username,password);//获取连接
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
