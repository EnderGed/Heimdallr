package com.example.datasender;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

public class GPSService extends Service implements LocationListener {
	
	public boolean isConnected;
	private final Context mContext;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	
	Location location;
	double latitude;
	double longitude;
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;		//w metrach
	private static final long MIN_TIME_BW_UPDATES = 1000 * 10;		//10sekund

	protected LocationManager locationManager;
	
	private onLocationChangedInt olci;
	
	public GPSService(Context context, onLocationChangedInt olci){
		this.mContext = context;
		this.olci = olci;
		getLocation();
	}

	public Location getLocation(){
		try{
			locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
			//pobieranie statusu GPS
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			//getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if(!isGPSEnabled && !isNetworkEnabled){
				//szkoda, nie ma providera
			} else{
				this.canGetLocation = true;
				//pobieranie lokacji z GPS
				if(isGPSEnabled){
					if(location == null){
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS","GPSEnabled");
						if(locationManager != null){
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if(location != null){
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
				//pobieranie lokacji z internetu
				if(isNetworkEnabled){
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network","got location updates");
					if(locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return location;
	}
	
	@Override
    public void onLocationChanged(Location location) {
		getLocation();
		if(olci != null)
			olci.locationChanged(latitude, longitude);
    }
 
    @Override
    public void onProviderDisabled(String provider) {
    }
 
    @Override
    public void onProviderEnabled(String provider) {
    }
 
    @Override
    public void onStatusChanged(String provider, int status, android.os.Bundle extras) {
    }
 
    @Override
    public android.os.IBinder onBind(Intent arg0) {
        return null;
    }
    
    public double getLatitude(){
    	if(location != null)
    		latitude = location.getLatitude();
    	return latitude;
    }
    
    public double getLongitude(){
    	if(location != null)
    		longitude = location.getLongitude();
    	return longitude;
    }
    
    //aby poprosic uzytkownika o wlaczenie GPS
    public boolean canGetLocation(){
    	return this.canGetLocation;
    }
    
    public void showSettingsAlert(){
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
    	alertDialog.setTitle("ustawienia GPS");
    	alertDialog.setMessage("GPS nie jest włączony. Czy chcesz przejść do ustawien?");
    	alertDialog.setPositiveButton("Ustawienia", new DialogInterface.OnClickListener(){
			
			@Override
    		public void onClick(DialogInterface dialog, int which) {
    			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    			mContext.startActivity(intent);
    		}
    	});
    	alertDialog.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	
    	alertDialog.show();
    }
    
    public void stopUsingGPS(){
    	if(locationManager != null)
    		locationManager.removeUpdates(GPSService.this);
    }
    
    public interface onLocationChangedInt{
    	public void locationChanged(double latitude, double longitude);
    }
}