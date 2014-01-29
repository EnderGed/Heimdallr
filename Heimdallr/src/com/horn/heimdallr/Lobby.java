package com.horn.heimdallr;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Lobby extends Activity {
	private TCPClient tcpClient;
	private TextView tv_gameCode;
	private TextView tv_team1Name;
	private TextView tv_team2Name;
	private ListView lv_team1playerList;
	private ListView lv_team2playerList;
	private boolean currentTeam;
	private List<String> team1List;
	private List<String> team2List;
	private String playerName;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		context = this;
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		playerName = (String)extras.get("player");
		
		//inicjowanie list drużyn
		team1List = new ArrayList<String>();
		team2List = new ArrayList<String>();
		
		//podłączenie się do TCPClienta
		tcp.tcpClient.setUser(Lobby.this,getApplicationContext(), lobbyOnMessageRecieved);
		
		//ustawianie działania klawiszy
		Button b_changeTeam = (Button)findViewById(R.id.lobby_change_team_button);
		Button b_exitLobby = (Button)findViewById(R.id.lobby_exit);
		Button b_startGame = (Button)findViewById(R.id.lobby_start);
		b_changeTeam.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentTeam = !currentTeam;
				tcp.tcpClient.sendMessage(new byte[]{(byte)104,(byte)(currentTeam == true?1:0)});
				if(currentTeam == true){
					team1List.remove(playerName);
					team2List.add(playerName);
				} else{
					team1List.add(playerName);
					team2List.remove(playerName);
				}
			}
		});
		b_exitLobby.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Activity)context).finish();
				
			}
		});
		b_startGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//ustawianie pól tekstowych
		tv_gameCode = (TextView)findViewById(R.id.lobby_code_textView);
		tv_team1Name = (TextView)findViewById(R.id.lobby_team1_textView);
		tv_team2Name = (TextView)findViewById(R.id.lobby_team2_textView);
		
		lv_team1playerList = (ListView)findViewById(R.id.lobby_listView1);
		lv_team2playerList = (ListView)findViewById(R.id.lobby_listView2);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lobby, menu);
		return true;
	}
	
	private TCPClient.onMessageRecieved lobbyOnMessageRecieved = new TCPClient.onMessageRecieved(){
		public void messageRecieved(byte[] message, int len) {
			Log.d("Connection","recieved: "+(message[0]<0?(message[0]+256):message[0]));
			if(message[0] == (byte)201){
				tcpClient.sendMessage(new byte[]{110,112,113});
				
			}
		}
	};
}
