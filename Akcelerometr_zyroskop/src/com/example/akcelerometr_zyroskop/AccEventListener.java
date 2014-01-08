package com.example.akcelerometr_zyroskop;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class AccEventListener implements SensorEventListener {
	
	private TextView tvX, tvY, tvZ;
	private double tpocz, gravX, gravY, gravZ, accX, accY, accZ, MS2S, alpha;
	public boolean on, czyzapis;
	public boolean mInitialized;
	public ArrayList<Double> czas;
	public ArrayList<Double> przyspieszenie;
	private String nazwaPliku;
	private Zapisywacz dopliku;
	
	public AccEventListener(TextView tvX, TextView tvY, TextView tvZ, String nazwaPliku){
		this.tvX = tvX;
		this.tvY = tvY;
		this.tvZ = tvZ;
		on = czyzapis = mInitialized = false;
		MS2S = 1./1000.;
		this.nazwaPliku = nazwaPliku;
		dopliku = new Zapisywacz(nazwaPliku);
		czas = new ArrayList<Double>();
		przyspieszenie = new ArrayList<Double>();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];
		
		if(on){
			if(!mInitialized){
				tpocz=System.currentTimeMillis();
				/*
				alpha = 1.;
				gravX = 0.;
				gravY = 0.;
				gravZ = 0.;
				/*/
				alpha = 0.8;
				gravX = x;
				gravY = y;
				gravZ = z; 
				//*/

				tvX.setText("0.0");
				tvY.setText("0.0");
				tvZ.setText("0.0");
			
				mInitialized = true;
			} else{


				double t = Math.abs(event.timestamp-tpocz)* MS2S;
				gravX = alpha * gravX + (1 - alpha) * x;
				gravY = alpha * gravY + (1 - alpha) * y;
				gravZ = alpha * gravZ + (1 - alpha) * z;
				
				accX = x - gravX;
				accY = y - gravY;
				accZ = z - gravZ;
				
				double a = Math.sqrt(accX*accX+accY*accY+accZ*accZ);
				czas.add(t);
				przyspieszenie.add(a);

				tvX.setText(String.format("%.2f ", accX));
				tvY.setText(String.format("%.2f ", accY));
				tvZ.setText(String.format("%.2f ", accZ));
				
				if(czyzapis){
					dopliku.dopisz(String.format("%f %f %f %f",t, accX, accY, accZ));
				}
				
			}
		}
	}
	
	public void pause(){
		mInitialized = false;
		dopliku.koniec();
		dopliku = null;
		czas = null;
		przyspieszenie = null;
	}
	
	public void resume(){
		czas = new ArrayList<Double>();
		przyspieszenie = new ArrayList<Double>();
		dopliku = new Zapisywacz(nazwaPliku);
	}
	
}
