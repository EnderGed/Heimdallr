package com.example.datasender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class TCPClient {
	public static final int PORT = 5657;
	public static final String IP = "192.168.1.82";	//localhost dla maszyny wirtualnej
	private static final int MSGMAXLENGTH = 40;
	//private OnMessageReceived messageListener = null;
	private boolean isRunning = false;
	private InputStream in = null;
	private OutputStream out = null;
	private onMessageRecieved omr = null;
	
	public TCPClient(onMessageRecieved omr){
		this.omr = omr;
	}
	
	public void start(){
		isRunning = true;
		byte[] msg = new byte[MSGMAXLENGTH];
		int bytesRead = 0;
		try{
			InetAddress serverAddr = InetAddress.getByName(IP);
			Socket socket = new Socket(serverAddr, PORT);
			try{
				in = socket.getInputStream();
				out = socket.getOutputStream();
				while(isRunning){
					bytesRead = in.read(msg);
					if(bytesRead != -1)
						omr.messageRecieved(msg, bytesRead);
				}
			} catch(Exception e){
				Log.e("TCPClient","Error", e);
			} finally{
				socket.close();
			}
		} catch(Exception e){
			Log.e("TCPClient","Error", e);
		}
	}
	
	public void stop(){
		isRunning = false;
	}
	
	public void sendMessage(byte[] message, int len){
		message[len] = -1;			//adding EOF at the end
		try {
			out.write(message,0,len+1);
		} catch (IOException e) {
			Log.e("TCPClient","Sending error");
		}
	}

	public interface onMessageRecieved{
		public void messageRecieved(byte[] message, int len);
	}
}
