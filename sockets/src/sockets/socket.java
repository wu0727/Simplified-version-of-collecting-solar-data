package sockets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
public class socket extends Thread{
    private Socket s_socket;
    static Connection conn=null;
    public socket(Socket c)
    {
        this.s_socket = c;
    }
    public static String DateDemo() {//宣告現在時間
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat (" yyyy/MM/dd HH:mm:ss");
        return(ft.format(dNow));
    }
    public static void insetnewSolar(String windowposition, String solarV, String solarEC, String temperature, String illumination) {//SQL與法
    	String insert= "insert into SolarData (WindowID,time,SolarV,SolarEC,Temperature,Illumination) values (?,?,?,?,?,?)";
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(insert);
            pstmt.setString(1,windowposition);
			pstmt.setString(2,DateDemo());
            pstmt.setString(3,solarV);
            pstmt.setString(4,solarEC);
            pstmt.setString(5,temperature);
            pstmt.setString(6,illumination);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
        @Override
    public void run()
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String datasource="jdbc:mysql://120.105.161.89/rrs?user=rrs&password=rrs";//透過JDBC連上資料庫
            conn=DriverManager.getConnection(datasource); //
            Statement st=conn.createStatement();

            BufferedReader in = new BufferedReader(new InputStreamReader(s_socket.getInputStream()));
            String [] clientdata = new String[5];
            System.out.print("start");
            while (true) {
                String str = in.readLine();
                //System.out.println(str);
                System.out.flush();
                clientdata = str.split(":");
                System.out.println(clientdata[0]+"時間："+DateDemo()+"電壓:"+clientdata[1]+"電流:"+clientdata[2]+"，溫度："+clientdata[3]
				+"，光照度："+clientdata[4]);
				insetnewSolar(clientdata[0],clientdata[1],clientdata[2],clientdata[3],clientdata[4]);   		 	
            if(str.equals("end")) break;
            }
            s_socket.close();
        } catch (Exception d) {
        	
        	}
        }
    public static void main(String[] argv)throws IOException{//主程式
        ServerSocket server = new ServerSocket(2020);
        while(true){
            System.out.print("wait...");
            socket sercode = new socket(server.accept());
            sercode.start();
        }
    }
}
