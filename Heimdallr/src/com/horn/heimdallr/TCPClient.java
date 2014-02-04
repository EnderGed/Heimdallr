package com.horn.heimdallr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TCPClient{
	public static final int PORT = 4747;
	public static final String IP = "192.168.43.44";		//"89.171.250.203";
	private static final int MSGMAXLENGTH = 100;
	//private OnMessageReceived messageListener = null;
	private boolean isRunning = false;
	private InputStream in = null;
	private OutputStream out = null;
	private onMessageRecieved omr = null;
	private Context context;
	private Activity activity;
	
	public TCPClient(){
	}
	
	public void setUser(Activity act, Context ctx, onMessageRecieved onMRecieved){
		Log.d("TCPClient", "setUser: "+ctx.getClass().getCanonicalName());
		activity = act;
		context = ctx;
		omr = onMRecieved;
	}
	
	
	//uwaga! trzeba najpierw wywołać setUser
	public void connect(){
		Log.d("TCPClient","connect called");
		new connectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void start(){
		Log.d("TCPClient", "starting...");
		byte[] msg = new byte[MSGMAXLENGTH];
		int bytesRead = 0;
		try{
			InetAddress serverAddr = InetAddress.getByName(IP);
			Log.e("connecting","before obtaining socket");
			Socket socket = new Socket(serverAddr, PORT);
			Log.e("connecting","socket obtained");
			isRunning = true;
			try{
				in = socket.getInputStream();
				out = socket.getOutputStream();
				while(isRunning){
					bytesRead = in.read(msg);
					if(bytesRead != -1){
						Log.d("TCPClient", "recieved: "+(msg[0]<0?msg[0]+256:msg[0]));
						omr.messageRecieved(msg, bytesRead);
					}
				}
				in.close();
				out.close();
			} catch(Exception e){
				onConnectionLost();
				Log.e("TCPClient","Error", e);
			} finally{
				isRunning = false;
				socket.close();
			}
		} catch(Exception e){
			onConnectionLost();
			isRunning = false;
			Log.e("TCPClient","Error", e);
		}
	}
	
	public void stop(){
		isRunning = false;
	}
	
	public void sendMessage(byte[] message){
		//message[len] = -1;			//adding EOF at the end
		if(isRunning){
			try {
				out.write(message,0,message.length);
				Log.d("TCPClient","sending: "+(message[0]<0?message[0]+256:message[0]));
			} catch (IOException e) {
				Log.e("TCPClient","Sending error");
			}
		} else{
			Toast.makeText(context, "czekam na polaczenie z serwerem", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void sendMessage(byte[] message, int len){
		//message[len] = -1;			//adding EOF at the end
		if(isRunning){
			try {
				out.write(message,0,len);
				Log.d("TCPClient","sending: "+(message[0]<0?message[0]+256:message[0]));
			} catch (IOException e) {
				Log.e("TCPClient","Sending error");
			}
		} else{
			Toast.makeText(context, "czekam na polaczenie z serwerem", Toast.LENGTH_SHORT).show();
		}
	}

	public interface onMessageRecieved{
		public void messageRecieved(byte[] message, int len);
	}
	
	private void onConnectionLost(){
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				alertDialogBuilder.setTitle("Połączenie zerwane.");
				alertDialogBuilder.setMessage("Upewnij się, że urządzenie ma dostęp do internetu.");
				alertDialogBuilder.setNeutralButton("połącz", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						connect();
					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				
				/*try{
					LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.connection_lost, viewGroup);	//(ViewGroup)findViewById(R.id.popup_element));
					connectionLostWindow = new PopupWindow(layout,350,350,true);
					connectionLostWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
					
					Button btnClosePopup = (Button)layout.findViewById(R.id.btn_close_popup);
					btnClosePopup.setOnClickListener(recconect_button_click_listener);
				} catch(Exception e){
					Log.e("Popup",e.getMessage());
				}*/
			}
		});
	}
	/*
	private View.OnClickListener recconect_button_click_listener = new View.OnClickListener(){
		public void onClick(View v){
			connect();
			connectionLostWindow.dismiss();
		}
	};*/
	
public class connectTask extends AsyncTask<String,String,TCPClient>{
		
		@Override
		protected TCPClient doInBackground(String... params) {
			Log.d("Connection", "connecting...");
			start();
			return null;
		}
	}
}
