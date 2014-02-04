package com.horn.heimdallr;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private PopupWindow tempPopupWindow;
	private EditText et_usrname;
	private EditText et_pswrd;
	private String playerName;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		context = this;
		
		//ustanawianie polaczenia z internetem
		tcp.tcpClient = new TCPClient();
		tcp.tcpClient.setUser(LoginActivity.this,context, loginOnMessageRecieved);
		tcp.tcpClient.connect();
		
		et_usrname = (EditText)findViewById(R.id.editText_login);
		et_pswrd = (EditText)findViewById(R.id.editText_password);
		
		//klawisz zaloguj
		Button b_loggin = (Button)findViewById(R.id.button_login);
		b_loggin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//if(isConnected)
				playerName = et_usrname.getText().toString();
				byte[] a = (playerName+'\0').getBytes();
				byte[] b = (et_pswrd.getText().toString()+'\0').getBytes();
				byte[] c = new byte[a.length+b.length+1];
				c[0] = (byte)201;	//komunikat o logowaniu
				System.arraycopy(a, 0, c, 1, a.length);
				System.arraycopy(b, 0, c, a.length+1, b.length);
				tcp.tcpClient.sendMessage(c);
				
				//else
				//	Log.e("Login","Not connected");
				
			}
		});
		
		//klawisz rejestruj
		Button b_register = (Button)findViewById(R.id.button_register);
		b_register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				registerNewPlayer();
			}
		});
		
	}


	

	private TCPClient.onMessageRecieved loginOnMessageRecieved = new TCPClient.onMessageRecieved(){
		public void messageRecieved(byte[] message, int len) {
			Log.d("Connection","recieved: "+(message[0]<0?(message[0]+256):message[0]));
			if(message[0] == (byte)200){
				//nieprawidlowy login lub haslo
				wrongLoginOrPassword();
			} else if(message[0] == (byte)201 && message[2] == (byte)201){
				//zalogowano poprawnie, przejdz do menu
				Intent menuAct = new Intent(LoginActivity.this,MenuActivity.class);
				startActivityForResult(menuAct,1);
			} else if(message[0] == (byte)203){
				//połączono z serwerem, który nie wie kim jesteś
				//tak trzymać!
			} else if(message[0] == (byte)222){
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(context, "jesteś już zalogowany", Toast.LENGTH_SHORT).show();
					}
				});
				Intent menuAct = new Intent(LoginActivity.this,MenuActivity.class);
				startActivityForResult(menuAct,1);
			} else{
				Log.e("Login",""+ (message[2]<0?message[2]+256:message[2]));
			}
		}
	};
	
	private void registerNewPlayer(){
		//musi byc uruchamiane na watku UI
		try{
			LayoutInflater inflater = (LayoutInflater)LoginActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.register_layout, (ViewGroup)findViewById(R.id.popup_register));
			tempPopupWindow = new PopupWindow(layout,350,350,true);
			tempPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
			
			Button btnRegister = (Button)layout.findViewById(R.id.button_register);
			btnRegister.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText et_login = (EditText)layout.findViewById(R.id.editText_login_register);
					EditText et_email = (EditText)layout.findViewById(R.id.editText_email_register);
					byte[] a = (et_login.getText().toString()+'\0').getBytes();
					byte[] b = (et_email.getText().toString()+'\0').getBytes();
					byte[] c = new byte[a.length + b.length + 1];
					c[0] = (byte)203;
					System.arraycopy(a, 0, c, 1, a.length);
					System.arraycopy(b, 0, c, a.length+1,b.length);
					Log.d("Register",et_login.getText()+" "+et_email.getText());
					tcp.tcpClient.sendMessage(c);
					tempPopupWindow.dismiss();
					Toast.makeText(getApplicationContext(), "Email został wysłany", Toast.LENGTH_LONG).show();
				}
			});
		} catch(Exception e){
			Log.e("Popup","register",e);
		}
	}
	
	private void wrongLoginOrPassword(){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				try{
					LayoutInflater inflater = (LayoutInflater)LoginActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.wrong_login, (ViewGroup)findViewById(R.id.popup_wrong_login));
					tempPopupWindow = new PopupWindow(layout,350,600,true);
					tempPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
					
					Button btnClosePopup = (Button)layout.findViewById(R.id.btn_close_popup_wrong_login);
					btnClosePopup.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							et_pswrd.setText("");
							tempPopupWindow.dismiss();
						}
					});
					
					Button btnNewPass = (Button)layout.findViewById(R.id.btn_new_password);
					btnNewPass.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							LayoutInflater inflater = (LayoutInflater)LoginActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View layout = inflater.inflate(R.layout.wrong_login, (ViewGroup)findViewById(R.id.popup_wrong_login));
							EditText et_mail = (EditText)layout.findViewById(R.id.editText1_wrong_login);
							byte a[] = (et_mail.getText().toString()+'\0').getBytes();
							byte b[] = new byte[a.length+1];
							b[0] = (byte)205;	//nowe haslo
							System.arraycopy(a, 0, b, 1, a.length);
							tcp.tcpClient.sendMessage(b);
							tempPopupWindow.dismiss();
							Toast.makeText(getApplicationContext(), "Na podany adres wysłano nowe hasło.", Toast.LENGTH_LONG).show();
						}
					});
				} catch(Exception e){
					Log.e("Popup",e.getMessage());
				}
			}
		});
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				Log.e("Login", "menu return with: " + data.getStringExtra("result"));          
		    }
		    if (resultCode == RESULT_CANCELED) {    
		        tcp.tcpClient.setUser(LoginActivity.this, context, loginOnMessageRecieved);
		    }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		Log.d("Login","shutting down");
		if(tcp.tcpClient != null){
			tcp.tcpClient.stop();
			tcp.tcpClient = null;
		}
		super.onDestroy();
	}
}
