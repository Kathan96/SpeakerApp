package com.example.admin.speakerapp;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

//http://android-er.blogspot.in/2014/02/android-sercerclient-example-server.html
public class ServerActivity extends ActionBarActivity {

    TextView info,infoip,msg;
    String message="";
    ServerSocket serverSocket;
    MainActivity mainActivity=new MainActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        info=(TextView)findViewById(R.id.info);
        infoip=(TextView)findViewById(R.id.infoip);
        msg=(TextView)findViewById(R.id.msg);

        String IP=getIpAddress();
        if(IP.compareTo("0.0.0.0")==0){
            infoip.setText("Connect to Internet and try again.");
        }
        else{
            infoip.setText("IP Address: "+IP);
        }

        SocketServerThread socketServerThread=new SocketServerThread();
        socketServerThread.run();
    }

    //SocketServerThread class basically looks for responses from the client side.
    public class SocketServerThread extends Thread{
        int SocketServerPORT = 8080;
        int count = 0;
        @Override
        public void run(){
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                Toast.makeText(getApplicationContext(),"TOAST",Toast.LENGTH_LONG).show();
                //MainActivity.activity.runOnUiThread(new Runnable() {
                ServerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public final void run() {
                        Toast.makeText(getApplicationContext(),"TOAST",Toast.LENGTH_LONG).show();
                        info.setText("Server Port: "
                                + serverSocket.getLocalPort());
                    }
                });

             while (true){
                 Socket socket=serverSocket.accept();
                 count++;
                 message += "#" + count + " from " + socket.getInetAddress()
                         + ":" + socket.getPort() + "\n";

                 ServerActivity.this.runOnUiThread(new Runnable() {

                     @Override
                     public void run() {
                         msg.setText(message);
                     }
                 });
                 SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                         socket, count);
                 socketServerReplyThread.start();

             }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    //SocketServerReplyThread class sends messages to the client.
    public class SocketServerReplyThread extends Thread{
        private Socket hostThreadSocket;
        int cnt;
        SocketServerReplyThread(Socket socket,int c){
            hostThreadSocket = socket;
            cnt = c;
        }
        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Connected to Server. You are number: " + cnt;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                message += "Connected to Client: "+cnt;

                ServerActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msg.setText(message);
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            ServerActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msg.setText(message);
                }
            });
        }

    }

    private String getIpAddress() {
        int ip;
        String ipAddress;
        try {
            WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            ip = wifiInfo.getIpAddress();
            ipAddress = Formatter.formatIpAddress(ip);
            //Toast.makeText(getApplicationContext(),"Getting Ip Address!",Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
           // ip += "Something Wrong! " + e.toString() + "\n";
           ipAddress="ERROR";
        }

        return ipAddress;
    }

    @Override
    //Must close socket before destroying activity.
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
