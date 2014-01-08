package com.example.akcelerometr_zyroskop;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class GyrEventListener implements SensorEventListener {
	
	private TextView tvX, tvY, tvZ;
	private double tpocz, t, gyrX, gyrY, gyrZ, gyrR, MS2S, eps;
	public boolean on, czyzapis;
	public boolean mInitialized;
	public ArrayList<Double> czas;
	public ArrayList<Double> omega;
	private String nazwaPliku;
	private Zapisywacz dopliku;
	
	public GyrEventListener(TextView tvX, TextView tvY, TextView tvZ, String nazwaPliku){
		this.tvX = tvX;
		this.tvY = tvY;
		this.tvZ = tvZ;
		on = czyzapis = mInitialized = false;
		MS2S = 1./1000.;
		eps = 0.0001;
		this.nazwaPliku = nazwaPliku;
		dopliku = new Zapisywacz(nazwaPliku);
		czas = new ArrayList<Double>();
		omega = new ArrayList<Double>();
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

			if (!mInitialized) {
				
				t = tpocz = System.currentTimeMillis();
				
				tvX.setText("0.0");
				tvY.setText("0.0");
				tvZ.setText("0.0");
			
				mInitialized = true;
			}
			else{
				double dt = Math.abs(event.timestamp-t)* MS2S;
				
				// Calculate the angular speed of the sample
				double om = Math.sqrt(x*x+y*y+z*z);
				
				// Normalize the rotation vector if it's big enough to get the axis
				// (that is, eps should represent your maximum allowable margin of error)
				if (om > eps) {
					x =	x/om;
					y =	y/om;
					z =	z/om;
				}
				double thetaOverTwo = om * dt / 2.0; //zmiana k¹ta tj. omega*zmiana czasu
				double sinThetaOverTwo = Math.sin(thetaOverTwo);
				double cosThetaOverTwo = Math.cos(thetaOverTwo);
				gyrX = sinThetaOverTwo * x;
				gyrY = sinThetaOverTwo * y;
				gyrZ = sinThetaOverTwo * z;
				gyrR = cosThetaOverTwo;
				// po co mi cosinus k¹ta obrotu? deltaRotationVector[3] = cosThetaOverTwo;
				
				tvX.setText(String.format("%.2f ", gyrX));
				tvY.setText(String.format("%.2f ", gyrY));
				tvZ.setText(String.format("%.2f ", gyrZ));
				
				t = event.timestamp;
				czas.add(t-tpocz);
				omega.add(om);
				
				if(czyzapis){
					dopliku.dopisz(String.format("%f %f %f %f %f",t-tpocz,gyrX,gyrY, gyrZ, gyrR));
				}
			}
		}
	}
	
	public void pause(){
		mInitialized = false;
		dopliku.koniec();
		dopliku = null;
		czas = null;
		omega = null;
	}
	
	public void resume(){
		czas = new ArrayList<Double>();
		omega = new ArrayList<Double>();
		dopliku = new Zapisywacz(nazwaPliku);
	}
	
}
