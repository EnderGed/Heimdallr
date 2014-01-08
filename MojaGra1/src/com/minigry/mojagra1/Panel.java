package com.minigry.mojagra1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class Panel extends SurfaceView implements Callback, SensorEventListener {

	private Game game;
	private Context c;
	private SensorManager sm;
	
	private double vx,vy,vz;
	private double delay = 500;
	private double alpha = 0.8;
	
	
	public Panel(Context context) {
		super(context);
		
		c = context;
		setFocusable(true);
		getHolder().addCallback(this);
		
		sm = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		sm.registerListener(this,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),500);
		vx = vy = vz = 0;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		
		game = new Game(c,getWidth(),getHeight());
		new Loop(getHolder(),game).start();
		

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
	double a = 1., b = 0.;
	@Override
	public void onSensorChanged(SensorEvent evt) {
		
		
		gravity[0] = alpha * gravity[0] + (1 - alpha) * evt.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * evt.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * evt.values[2];

        accx = evt.values[0] - gravity[0];
        accy = evt.values[1] - gravity[1];
        accz = evt.values[2] - gravity[2];
        
        vx += accx * delay;
        vy += accy * delay;
        vz += accz * delay;

        game.setSpeed(a * Math.sqrt(vx*vx + vy*vy + vz*vz) + b);
	}

}
