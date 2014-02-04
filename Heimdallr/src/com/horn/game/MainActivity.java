package com.horn.game;

import java.nio.ByteBuffer;

import com.horn.heimdallr.R;
import com.horn.heimdallr.TCPClient;
import com.horn.heimdallr.tcp;

import android.os.Bundle;
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
	private GPSService gpsService;
	private MapView mapView;
	private Context context;
	private String hints;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		hints = "brak wskazówek";
		
		tcp.tcpClient.setUser(MainActivity.this, context, mainOnMessageRecieved);
		//ustanawianie polaczenia z internetem
		/*
		tcpClient = null;
		setIsConnected(false);
		cnctTask = new connectTask();
        cnctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */
		
        //klawisz do wskazówek
		
		Button b_hints = (Button)findViewById(R.id.main_b_hints);
		b_hints.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, hints, Toast.LENGTH_LONG).show();
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
    				byte[] a = toByteArray(longitude);
    				byte[] b = toByteArray(latitude);
    				byte[] c = new byte[a.length+b.length+1];
    				c[0] = (byte)2;		//postawienie bomby
    				System.arraycopy(a, 0, c, 1, a.length);
    				System.arraycopy(b, 0, c, a.length+1, b.length);
                    tcp.tcpClient.sendMessage(c);
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
				
				byte[] a = toByteArray(longitude);
				byte[] b = toByteArray(latitude);
				byte[] c = new byte[a.length+b.length+1];
				c[0] = (byte)1;		//położenie
				System.arraycopy(a, 0, c, 1, a.length);
				System.arraycopy(b, 0, c, a.length+1, b.length);
                tcp.tcpClient.sendMessage(c);
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
		tcp.tcpClient.sendMessage(new byte[]{3});
		if(gpsService != null)
			gpsService.stopUsingGPS();
		super.onDestroy();
		
	}
	
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}

	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
	
	private TCPClient.onMessageRecieved mainOnMessageRecieved = new TCPClient.onMessageRecieved(){
		public void messageRecieved(final byte[] message, int len) {
			if(message[0] == (byte)201 && message[1] == (byte)2){
                double latitude = gpsService.getLatitude();
                double longitude = gpsService.getLongitude();
                mapView.addBomb(new Area(longitude, latitude, 20));
                mapView.updateMapPosition(latitude, longitude);
			} else if(message[0] == (byte)1){	//wszedłeś w bombę
				//przydałyby się jeszcze jakieś wibracje
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(context, "Wszedłeś w bombę.\nUCIEKAJ!", Toast.LENGTH_LONG).show();
					}
				});
			} else if(message[0] == (byte)2){	//umarłeś
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(context, "Nie żyjesz.", Toast.LENGTH_LONG).show();
					}
				});
				((Activity)context).finish();
			} else if(message[0] == (byte)3){	//bomba wybuchła
				byte[] longit = new byte[8];
				byte[] latit = new byte[8];
				System.arraycopy(message, 2, longit, 0, 8);
				System.arraycopy(message, 10, latit, 0, 8);
				mapView.removeBomb(new Area(toDouble(longit),toDouble(latit),0));
			} else if(message[0] == (byte)4){
				//powinna być minigra
			}
		}
	};

}
