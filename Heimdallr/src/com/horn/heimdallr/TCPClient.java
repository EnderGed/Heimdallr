package com.horn.heimdallr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class TCPClient{
	public static final int PORT = 5657;
	public static final String IP = "192.168.1.82";
	private static final int MSGMAXLENGTH = 100;
	//private OnMessageReceived messageListener = null;
	private boolean isRunning = false;
	private InputStream in = null;
	private OutputStream out = null;
	private onMessageRecieved omr = null;
	private onConnectionLost ocl = null;
	
	public TCPClient(onMessageRecieved omr, onConnectionLost ocl){
		this.omr = omr;
		this.ocl = ocl;
	}
	
	public void start(){
		isRunning = true;
		byte[] msg = new byte[MSGMAXLENGTH];
		int bytesRead = 0;
		try{
			InetAddress serverAddr = InetAddress.getByName(IP);
			Log.e("connecting","before obtaining socket");
			Socket socket = new Socket(serverAddr, PORT);
			Log.e("connecting","socket obtained");
			try{
				in = socket.getInputStream();
				out = socket.getOutputStream();
				while(isRunning){
					bytesRead = in.read(msg);
					if(bytesRead != -1)
						omr.messageRecieved(msg, bytesRead);
				}
				in.close();
				out.close();
			} catch(Exception e){
				ocl.connectionLost();
				Log.e("TCPClient","Error", e);
			} finally{
				socket.close();
			}
		} catch(Exception e){
			ocl.connectionLost();
			Log.e("TCPClient","Error", e);
		}
	}
	
	public void stop(){
		isRunning = false;
	}
	
	public void sendMessage(byte[] message){
		//message[len] = -1;			//adding EOF at the end
		try {
			out.write(message,0,message.length);
		} catch (IOException e) {
			Log.e("TCPClient","Sending error");
		}
	}
	
	public void sendMessage(byte[] message, int len){
		//message[len] = -1;			//adding EOF at the end
		try {
			out.write(message,0,len);
		} catch (IOException e) {
			Log.e("TCPClient","Sending error");
		}
	}
	
	public void setOnMessageRecieved(onMessageRecieved omr){
		this.omr = omr;
	}

	public interface onMessageRecieved{
		public void messageRecieved(byte[] message, int len);
	}
	
	public interface onConnectionLost{
		public void connectionLost();
	}
}
