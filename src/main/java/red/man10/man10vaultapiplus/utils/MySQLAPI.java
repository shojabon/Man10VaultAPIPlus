package red.man10.man10vaultapiplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by sho-pc on 2017/05/21.
 */
public class MySQLAPI {

    public Boolean debugMode = false;
    private JavaPlugin plugin;
    private String HOST = null;
    private String DB = null;
    private String USER = null;
    private String PASS = null;
    private String PORT = null;
    private boolean connected = false;
    private Statement st = null;
    private Connection con = null;
    private String conName;
    private MySQLFunc MySQL;

    ////////////////////////////////
    //      コンストラクタ
    ////////////////////////////////
    public String getConfirmation(){
        return "%QltpzRbj$4AFjRSRqAblzrbdiDqAblzs4t$sbRpQ5bqFlbbsVFe3eIbxfbmbIsA";
    }

    public List<String> getMySqlSetting(){
        List<String> list = new ArrayList<>();
        list.add(HOST);
        list.add(DB);
        list.add(USER);
        list.add(PASS);
        list.add(PORT);
        return list;
    }

    public MySQLAPI(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.conName = name;
        this.connected = false;
        loadConfig();

        this.connected = Connect(HOST, DB, USER, PASS, PORT);

    }

    public MySQLAPI(String conName,String host,String user,String pass,String port,String db){
        this.conName = conName;
        this.connected = false;
        HOST = host;
        USER = user;
        PASS = pass;
        PORT = port;
        DB = db;
        this.connected = Connect(host,db,user,pass,port);

    }

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////
    public void loadConfig() {
        plugin.reloadConfig();
        HOST = plugin.getConfig().getString("mysql.host");
        USER = plugin.getConfig().getString("mysql.user");
        PASS = plugin.getConfig().getString("mysql.pass");
        PORT = plugin.getConfig().getString("mysql.port");
        DB = plugin.getConfig().getString("mysql.db");
    }

    public boolean connectable(){
        return this.connected;
    }

    ////////////////////////////////
    //       接続
    ////////////////////////////////
    public Boolean Connect(String host, String db, String user, String pass,String port) {
        this.HOST = host;
        this.DB = db;
        this.USER = user;
        this.PASS = pass;
        this.MySQL = new MySQLFunc(host, db, user, pass,port);
        this.con = this.MySQL.open();
        if(this.con == null){
            return false;
        }
        try {
            this.st = this.con.createStatement();
            this.connected = true;
        } catch (SQLException var6) {
            this.connected = false;
        }
        //this.MySQL.close(this.con);
        try {
            this.con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Boolean.valueOf(this.connected);
    }

    ////////////////////////////////
    //     行数を数える
    ////////////////////////////////
    public String getDB(){
        return DB;
    }
    public String getUSER(){
        return USER;
    }
    public int countRows(String table) {
        int count = 0;
        ResultSet set = this.query(String.format("SELECT * FROM %s", new Object[]{table}));

        try {
            while (set.next()) {
                ++count;
            }
        } catch (SQLException var5) {
        }

        return count;
    }

    ////////////////////////////////
    //     レコード数
    ////////////////////////////////
    public int count(String table) {
        int count = 0;
        ResultSet set = this.query(String.format("SELECT count(*) from %s", table));

        try {
            count = set.getInt("count(*)");

        } catch (SQLException var5) {
            return -1;
        }

        return count;
    }
    ////////////////////////////////
    //      実行
    ////////////////////////////////

    public int executeGetId(String query){
        int key = -1;
        try {
            MySQL = new MySQLFunc(HOST, DB, USER, PASS,PORT);
            con = MySQL.open();
            //open();
            PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.executeUpdate();
            pstmt.setQueryTimeout(10);
            ResultSet keys = pstmt.getGeneratedKeys();
            keys.next();
            key = keys.getInt(1);
            keys.close();
            pstmt.close();
            //close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    public boolean execute(String query) {
        MySQLFunc mysql = new MySQLFunc(this.HOST, this.DB, this.USER, this.PASS,this.PORT);
        Connection con = mysql.open();
        if(con == null){
            Bukkit.getLogger().info("failed to open MYSQL");
            return false;
        }
        boolean ret = true;
        try {
            Statement st = con.createStatement();
            st.execute(query);
            st.close();
            st = null;
            con.close();
            con = null;
        } catch (SQLException var3) {
            ret = false;
        }
        return ret;
    }

    public void executeThread(String query){
        Runnable r = () -> execute(query);
        Thread t = new Thread(r);
        t.start();
    }



    ////////////////////////////////
    //      クエリ
    //////////////////////////////
    //

    public ResultSet query(String query) {
        ResultSet rs = null;
        MySQL = new MySQLFunc(HOST, DB, USER, PASS, PORT);
        con = MySQL.open();
        if (debugMode) {
        }
        try {
            st = con.createStatement();
            st.setQueryTimeout(10);
            rs = st.executeQuery(query);
        } catch (SQLException var4) {
        }
        return rs;
    }

    public void close(){

        try {
            if(this.st != null){
                this.st.close();
                this.st = null;
            }
            if(this.con != null){
                this.con.close();
                this.con = null;
            }
        } catch (SQLException var4) {
        }

    }


    public String currentTimeNoBracket(){
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
        return sdf.format(date);
    }

    public int convertBooleanToMysql(Boolean b){
        if(b){
            return 1;
        }
        return 0;
    }

    public boolean convertMysqlToBoolean(int i){
        if(i == 1){
            return true;
        }
        return false;
    }
}