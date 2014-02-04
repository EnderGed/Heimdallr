package com.horn.heimdallr;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MenuActivity extends Activity {

	Context context;
	EditText team1name;
	EditText team2name;
	EditText gameId;
	EditText oldPswrd;
	EditText newPswrd1;
	EditText newPswrd2;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		context = this;
		
		tcp.tcpClient.setUser(MenuActivity.this, context, menuOnMessageRecieved);
		
		//inicjalizowanie edittextów i buttonów
		team1name = (EditText)findViewById(R.id.menu_et_team1);
		team2name = (EditText)findViewById(R.id.menu_et_team2);
		gameId = (EditText)findViewById(R.id.menu_et_gameId);
		oldPswrd = (EditText)findViewById(R.id.menu_et_old_password);
		newPswrd1 = (EditText)findViewById(R.id.menu_et_new_password1);
		newPswrd2 = (EditText)findViewById(R.id.menu_et_new_password2);
		
		Button b_createGame = (Button)findViewById(R.id.menu_b_create_game);
		Button b_joinGame = (Button)findViewById(R.id.menu_b_join_game);
		Button b_changePswrd = (Button)findViewById(R.id.menu_b_change_password);
		Button b_logout = (Button)findViewById(R.id.menu_b_logout);
		
		b_createGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				byte[] a = (team1name.getText().toString()+'\0').getBytes();
				byte[] b = (team2name.getText().toString()+'\0').getBytes();
				byte[] c = new byte[a.length + b.length + 1];
				c[0] = (byte)101;
				System.arraycopy(a, 0, c, 1, a.length);
				System.arraycopy(b, 0, c, a.length+1, b.length);
				tcp.tcpClient.sendMessage(c);
			}
		});
		
		b_joinGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = Integer.parseInt(gameId.getText().toString());
				tcp.tcpClient.sendMessage(new byte[]{(byte)102,(byte)id});
				
			}
		});
		
		b_changePswrd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String npsw1 = newPswrd1.getText().toString();
				String npsw2 = newPswrd2.getText().toString();
				if(npsw1.equals(npsw2)){
					byte[] a = (oldPswrd.getText().toString()+'\0').getBytes();
					byte[] b = (npsw1+'\0').getBytes();
					byte[] c = new byte[a.length + b.length + 1];
					c[0] = (byte)204;
					System.arraycopy(a, 0, c, 1, a.length);
					System.arraycopy(b, 0, c, a.length+1, b.length);
					tcp.tcpClient.sendMessage(c);
					
				} else{
					Toast.makeText(context, "nowe hasła się nie zgadzają", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		b_logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tcp.tcpClient.sendMessage(new byte[]{(byte)202});
				((Activity)context).finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	private TCPClient.onMessageRecieved menuOnMessageRecieved = new TCPClient.onMessageRecieved(){
		public void messageRecieved(final byte[] message, int len) {
			if(message[0] == (byte)200 && message[2] == (byte)102){
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(context, "gra o podanym id nie istnieje", Toast.LENGTH_SHORT).show();						
					}
				});
			} else if(message[0] == (byte)200 && message[2] == (byte)204){
				runOnUiThread(new Runnable(){
					public void run(){
						Toast.makeText(context, "stare hasło się nie zgadza", Toast.LENGTH_SHORT).show();
					}
				});
			} else if(message[0] == (byte)201 && (message[2] == (byte)102 || message[2] == (byte)101)){
				Intent lobby = new Intent(MenuActivity.this,Lobby.class);
				startActivity(lobby);
				//po zakonczeniu activity, podłącz się do tcpClienta
				tcp.tcpClient.setUser(MenuActivity.this, context, menuOnMessageRecieved);
			} else if(message[0] == (byte)202){
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(context, "zmień hasło!", Toast.LENGTH_LONG).show();
						
					}
				});
			} else{
				Log.e("Menu",""+ message[1] + " "+message[2]);
			}
		}
	};

}
