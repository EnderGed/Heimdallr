package com.example.akcelerometr_zyroskop;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;

import java.util.*;
import java.io.*;

import com.androidplot.xy.*;




//po co mi cosinus k¹ta obrotu? 
//i jeszcze 2 ostatnie linijki usunê³am... i o co tam chodzi³o?



//ok 100Hz, zapis do pliku tekstowego zeby bylo opisane i przyspieszenie rzeczywiste - sprawdzic czestotliwosc probkowanie
//pomiar ¿yroskopem
public class MainActivity extends Activity{
	
	private AccEventListener AccEL;
	private GyrEventListener GyrEL;
	private SensorManager mSensorManager; 
	private Sensor mAccelerometer; 
	private Sensor mGyroscope;
	
	private ToggleButton run, run_zyr;
	private RadioButton zapis, zapis_zyr;
	private Button rys, rys_zyr;
	private XYPlot wykres;
	private Wykres wykresik, wykresik_zyr;
	private double tpocz, tpocz_zyr;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		zapis=(RadioButton)findViewById(R.id.zapisuj);
		run=(ToggleButton)findViewById(R.id.start);
		rys=(Button)findViewById(R.id.wykresik);
		
		zapis_zyr=(RadioButton)findViewById(R.id.zapisuj_zyr);
		run_zyr=(ToggleButton)findViewById(R.id.start_zyr);
		rys_zyr=(Button)findViewById(R.id.wykresik_zyr);
		
		wykres = (XYPlot) findViewById(R.id.chart);
		wykres.setVisibility(View.INVISIBLE);
		//dopliku = new Zapisywacz("acc.txt");
		//dopliku = new Zapisywacz("omega.txt");
		
		run.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AccEL.on = ((ToggleButton) v).isChecked();
			}
		});
		run_zyr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GyrEL.on = ((ToggleButton) v).isChecked();
			}
		});
		
		zapis.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AccEL.czyzapis=zapis.isChecked();
			}
		});
		zapis_zyr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GyrEL.czyzapis=zapis_zyr.isChecked();
			}
		});
		
		
		rys.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				wykresik = new Wykres(wykres,AccEL.czas,AccEL.przyspieszenie);
				//System.out.println(przyspieszenie.toString());
				//System.out.println(czas.toString());
				wykresik.rysuj();
				wykres.setVisibility(View.VISIBLE);
			}
		});
		
		rys_zyr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				wykresik_zyr = new Wykres(wykres,GyrEL.czas,GyrEL.omega);
				//System.out.println(przyspieszenie.toString());
				//System.out.println(czas.toString());
				wykresik_zyr.rysuj();
				wykres.setVisibility(View.VISIBLE);
			}
		});

		AccEL = new AccEventListener((TextView)findViewById(R.id.x_axis),(TextView)findViewById(R.id.y_axis),(TextView)findViewById(R.id.z_axis),"daneAcc.txt");
		GyrEL = new GyrEventListener((TextView)findViewById(R.id.x_axis_zyr),(TextView)findViewById(R.id.y_axis_zyr),(TextView)findViewById(R.id.z_axis_zyr),"daneGyr.txt");
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		//Trzeci argument registerListener to czas probkowania w mikrosekundach
		mSensorManager.registerListener(AccEL, mAccelerometer, 1000);
		mSensorManager.registerListener(GyrEL, mGyroscope, 1000);
	}
	
	
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(AccEL, mAccelerometer, 1000);
		mSensorManager.registerListener(GyrEL, mGyroscope, 1000);
		AccEL.resume();
		GyrEL.resume();
	}

	protected void onPause() {
		super.onPause();
		AccEL.pause();
		GyrEL.pause();
		mSensorManager.unregisterListener(AccEL);
		mSensorManager.unregisterListener(GyrEL);
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
