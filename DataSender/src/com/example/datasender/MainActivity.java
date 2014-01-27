package com.example.datasender;

import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TCPClient tcpClient;
	private PopupWindow connectionLostWindow;
	private connectTask cnctTask;
	private boolean isConnected;
	private GPSService gpsService;
	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//ustanawianie polaczenia z internetem
		/*
		tcpClient = null;
		setIsConnected(false);
		cnctTask = new connectTask();
        cnctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */
		
        //klawisz do wysylania
        Button send = (Button)findViewById(R.id.button_send);
        send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(tcpClient != null)
					tcpClient.sendMessage(new byte[]{4,1}, 1);
				else
					Log.e("connection","tcpClient not active");
			}
		});
        
        //klawisz do stawiania bomb
        ImageButton placeBomb = (ImageButton)findViewById(R.id.bBomb);
        placeBomb.setOnClickListener(new View.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// sprawdzanie, czy GPS jest wlaczony    
                if(gpsService.canGetLocation()){
                     
                    double latitude = gpsService.getLatitude();
                    double longitude = gpsService.getLongitude();
                    
                    mapView.addBomb(new Area(longitude, latitude, 20));
                    mapView.updateMapPosition(latitude, longitude);
                    
                }else{
                    // jesli nie dziala, to popros uzytkownika, zeby wlaczyl GPS
                    gpsService.showSettingsAlert();
                }
        	}
        });
        
        Log.d("main", "setting GPS");
        
        //ustawianie GPS
		gpsService = new GPSService(MainActivity.this, new GPSService.onLocationChangedInt() {
			
			@Override
			public void locationChanged(double latitude, double longitude) {
				//Toast.makeText(getApplicationContext(), "Twoje położenie to: \nSzer: " + latitude + "\nDł: " + longitude, Toast.LENGTH_LONG).show();
				mapView.updateMapPosition(latitude, longitude);
				
			}
		});
		
		//klawisz - pokaz lokacje
		Button loc = (Button)findViewById(R.id.b_location);
		loc.setOnClickListener(new View.OnClickListener(){
			@Override
            public void onClick(View view) {

                // sprawdzanie, czy GPS jest wlaczony    
                if(gpsService.canGetLocation()){
                     
                    double latitude = gpsService.getLatitude();
                    double longitude = gpsService.getLongitude();
                    
                    mapView.updateMapPosition(latitude, longitude);
                    
                    //Toast.makeText(getApplicationContext(), "Twoje położenie to: \nSzer: " + latitude + "\nDł: " + longitude, Toast.LENGTH_LONG).show();    
                }else{
                    // jesli nie dziala, to popros uzytkownika, zeby wlaczyl GPS
                    gpsService.showSettingsAlert();
                }
                 
            }
		});
		
		//ustawianie mapView
		mapView = (MapView)findViewById(R.id.mapView1);
		mapView.setDestination(new Area(16.975, 51.037, 50));
		mapView.invalidate();
	}
	
	public class connectTask extends AsyncTask<String,String,TCPClient>{
		
		@Override
		protected TCPClient doInBackground(String... params) {
			tcpClient = new TCPClient(
					//when a message comes
					new TCPClient.onMessageRecieved() {
				
				public void messageRecieved(byte[] message, int len) {
					message[len] = '\0';
					System.out.println(new String(message));
				}
			},	//when connection is lost
					new TCPClient.onConnectionLost() {
				
				@Override
				public void connectionLost() {
					runOnUiThread(new Runnable() {
						@Override
						public void run(){
							try{
								LayoutInflater inflater = (LayoutInflater)MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								View layout = inflater.inflate(R.layout.connection_lost, (ViewGroup)findViewById(R.id.popup_element));
								connectionLostWindow = new PopupWindow(layout,350,350,true);
								connectionLostWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
								
								Button btnClosePopup = (Button)layout.findViewById(R.id.btn_close_popup);
								btnClosePopup.setOnClickListener(recconect_button_click_listener);
								setIsConnected(false);
							} catch(Exception e){
								Log.e("Popup",e.getMessage());
							}
						}
					});
				}
			});
			
			tcpClient.start();
			setIsConnected(true);
			return null;
		}
	}
	
	private View.OnClickListener recconect_button_click_listener = new View.OnClickListener(){
		public void onClick(View v){
			tcpClient = null;
			cnctTask = new connectTask();
			cnctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			connectionLostWindow.dismiss();
		}
	};
	
	public boolean getIsConnected(){
		return isConnected;
	}
	
	private void setIsConnected(boolean b){
		isConnected = b;
		if(gpsService != null)
			gpsService.isConnected = b;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		if(tcpClient != null)
			tcpClient.stop();
		Log.e("onDestroy","killing application");
		if(gpsService != null)
			gpsService.stopUsingGPS();
		super.onDestroy();
		
	}

}
