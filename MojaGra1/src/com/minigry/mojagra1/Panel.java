package com.minigry.mojagra1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class Panel extends SurfaceView implements Callback, SensorEventListener, LocationListener {

	private Game game;
	private Context c;
	private SensorManager sm;
	//private LocationManager lm;
	
	private double vx,vy,vz;
	private int delay = 1;
	private double alpha = 0.8;
	
	
	public Panel(Context context) {
		super(context);
		
		c = context;
		setFocusable(true);
		getHolder().addCallback(this);
		
		sm = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		//lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		
		game = new Game(c,getWidth(),getHeight());
		new Loop(getHolder(),game).start();
		vx = vy = vz = 0;
		sm.registerListener(this,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),delay);
		//System.err.println(lm.isProviderEnabled(lm.GPS_PROVIDER));
		//lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		synchronized(getHolder()) {
		
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				if (ev.getY() < getHeight()/3) game.turn(0,-1);
				else if (ev.getY() > 2*getHeight()/3) game.turn(0,1);
				else if (ev.getX() < getWidth()/2) game.turn(-1,0);
				else if (ev.getX() > getWidth()/2) game.turn(1,0);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	
	double[] gravity = new double[3];
	double accx,accy,accz;
	double a = 20, b = 0;
	long time = 0, del;
	@Override
	public void onSensorChanged(SensorEvent evt) {
		
		if (time == 0) {
			time = evt.timestamp;
			vx=vy=vz=0;
			return;
		}
		alpha = 0.4;
		
		gravity[0] = alpha * gravity[0] + (1 - alpha) * evt.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * evt.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * evt.values[2];

        accx = evt.values[0] - gravity[0];
        accy = evt.values[1] - gravity[1];
        accz = evt.values[2] - gravity[2];
        
        del = evt.timestamp - time;
        time = evt.timestamp;
        vx = accx;// * del / 1000000000;
        vy = accy;// * del / 1000000000;
        vz = accz;// * del / 1000000000;
        
        /*vx += accx * del / 1000000000;
        vy += accy * del / 1000000000;
        vz += accz * del / 1000000000;
		*/
        //System.err.print(vx); System.err.print(vy); System.err.println(vz);
        //if (game.rand.nextFloat() < 0.01) vx=vy=vz=0;
        game.setSpeed(a * Math.sqrt(vx*vx + vy*vy + vz*vz) + b);
	}

	@Override
	public void onLocationChanged(Location update) {
		
		//game.setSpeed(update.getSpeed());
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

}
