package com.horn.heimdallr;

import java.util.ArrayList;
import java.util.List;

import com.horn.game.MainActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Lobby extends Activity {
	private TextView tv_gameCode;
	private TextView tv_team1Name;
	private TextView tv_team2Name;
	private ListView lv_team1playerList;
	private ListView lv_team2playerList;
	private List<String> team1List;
	private List<String> team2List;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		context = this;
		
		//inicjowanie list drużyn
		team1List = new ArrayList<String>();
		team2List = new ArrayList<String>();
		
		//podłączenie się do TCPClienta
		tcp.tcpClient.setUser(Lobby.this,context, lobbyOnMessageRecieved);
		
		//ustawianie działania klawiszy
		Button b_changeTeam = (Button)findViewById(R.id.lobby_change_team_button);
		Button b_exitLobby = (Button)findViewById(R.id.lobby_exit);
		Button b_startGame = (Button)findViewById(R.id.lobby_start);
		b_changeTeam.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tcp.tcpClient.sendMessage(new byte[]{(byte)104});
			}
		});
		b_exitLobby.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tcp.tcpClient.sendMessage(new byte[]{(byte)105});
				endActivity((byte)0);
			}
		});
		b_startGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tcp.tcpClient.sendMessage(new byte[]{(byte)103});
			}
		});
		
		//ustawianie pól tekstowych
		tv_gameCode = (TextView)findViewById(R.id.lobby_code_textView);
		tv_team1Name = (TextView)findViewById(R.id.lobby_team1_textView);
		tv_team2Name = (TextView)findViewById(R.id.lobby_team2_textView);
		
		lv_team1playerList = (ListView)findViewById(R.id.lobby_listView1);
		lv_team2playerList = (ListView)findViewById(R.id.lobby_listView2);
		
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,team1List);
		lv_team1playerList.setAdapter(adapter1);
		
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,team2List);
		lv_team2playerList.setAdapter(adapter2);
		
	}
	
	private TCPClient.onMessageRecieved lobbyOnMessageRecieved = new TCPClient.onMessageRecieved(){
		public void messageRecieved(final byte[] message, final int len) {
			//Log.d("Connection","recieved: "+(message[0]<0?(message[0]+256):message[0]));
			if(message[0] == (byte)110){		//przesłanie nazw drużyn
				final String[] strs = new String(message).split("\0");
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						tv_team1Name.setText(strs[1]);
						tv_team2Name.setText(strs[2]);
					}
				});
			} else if(message[0] == (byte)102){			//gracz dołączył do drużyny
				final String[] strs = new String(message).split("\0");
				Log.d("Lobby","102 "+message[2]+" "+strs[2]);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Log.d("Lobby","onUIThread 102 "+(byte)strs[1].charAt(0));
						if((byte)strs[1].charAt(0) == (byte)1){
							team1List.add(strs[2]);
							team1List.add("pp1");
							lv_team1playerList.invalidateViews();
						}
						else{
							team2List.add(strs[2]);
							team2List.add("pp2");
							lv_team2playerList.invalidateViews();
						}
					}
				});
			} else if(message[0] == (byte)103){		//stworzono rozgrywkę o ID
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv_gameCode.setText("kod rozgrywki to: "+((Integer)(int)(message[2]<0?message[2]+256:message[2])).toString());
					}
				});
			} else if(message[0] == (byte)104){		//gracz zmienił drużynę
				final String[] strs = new String(message).split("\0");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d("Lobby","onUIThread 104 "+(byte)strs[1].charAt(0));
						if((byte)strs[1].charAt(0) == (byte)1){
							team1List.add(strs[2]);
							if(!team2List.remove(strs[2]))
								Log.e("Lobby","can't remove "+strs[2]);
						}
						else{
							team2List.add(strs[2]);
							if(!team1List.remove(strs[2]))
								Log.e("Lobby","can't remove "+strs[2]);
						}
						lv_team1playerList.invalidateViews();
						lv_team2playerList.invalidateViews();
					}
				});
			} else if(message[0] == (byte)109){		//lista graczy
				
				final String[] strs = new String(message).split("\0");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						for(int i=1;i+1<strs.length;i+=2){
							Log.d("Lobby","onUIThread 109 "+strs[i]+ " "+(byte)(strs[i+1].charAt(0)));
							if(strs[i+1].charAt(0) == 1){
								team1List.add(strs[i]);
							}
							else{
								team2List.add(strs[i]);
							}
						}
						lv_team1playerList.invalidateViews();
						lv_team2playerList.invalidateViews();
					}
				});
			} else if(message[0] == (byte)111){			//gracz opuścił lobby
				final String[] strs = new String(message).split("\0");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						team1List.remove(strs[1]);
						team2List.remove(strs[1]);
						lv_team1playerList.invalidateViews();
						lv_team2playerList.invalidateViews();
					}
				});
			} else if(message[0] == (byte)112){			//twórca wyszedł z lobby, koniec rozgrywki
				Toast.makeText(context, "Twórca wyszedł z lobby\nrozgrywka zakończona.", Toast.LENGTH_SHORT).show();
				endActivity((byte)0);
			} else if(message[0] == (byte)10){
				Intent mainAct = new Intent(Lobby.this,MainActivity.class);
				startActivityForResult(mainAct,3);
			} else if(message[0] == (byte)203){
				endActivity((byte)1);
			} else{
				Log.e("Connection","recieved: "+(message[0]<0?(message[0]+256):message[0]));
			}
		}
	};
	
	private void endActivity(byte reason){
		if(reason == 0){	//normal return to menu
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
		} else if(reason == 1){		//serwer nie pamięta gracza, wyjść do logowania
			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", "new_connection");
			setResult(RESULT_OK, returnIntent);
		}
		((Activity)context).finish();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 3) {
			if(resultCode == RESULT_OK){
				String result = data.getStringExtra("result");
				Log.d("Menu", "lobby return with: " + result);
				if(result.equals("new_connection")){		//serwer nie pamięta połączenia - cofnąć się do logowania
					endActivity((byte)1);
				} else{
					Log.e("Menu","omg, what's happening here!?");
				}
		    }
		    if (resultCode == RESULT_CANCELED) {    
		        tcp.tcpClient.setUser(Lobby.this, context, lobbyOnMessageRecieved);
		    }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lobby, menu);
		return true;
	}
}
