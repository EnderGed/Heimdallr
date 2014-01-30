package com.horn.game;

import com.horn.heimdallr.TCPClient;
import com.horn.heimdallr.tcp;

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
	private PopupWindow connectionLostWindow;
	private GPSService gpsService;
	private MapView mapView;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		
		tcp.tcpClient.setUser(MainActivity.this, context, mainOnMessageRecieved);
		//ustanawianie polaczenia z internetem
		/*
		tcpClient = null;
		setIsConnected(false);
		cnctTask = new connectTask();
        cnctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */
		
        
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
		/*
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
		});*/
		
		//ustawianie mapView
		mapView = (MapView)findViewById(R.id.mapView1);
		mapView.setDestination(new Area(16.975, 51.037, 50));
		mapView.invalidate();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		Log.e("onDestroy","killing application");
		if(gpsService != null)
			gpsService.stopUsingGPS();
		super.onDestroy();
		
	}
	
	private TCPClient.onMessageRecieved mainOnMessageRecieved = new TCPClient.onMessageRecieved(){
		public void messageRecieved(final byte[] message, int len) {
			
		}
	};

}
