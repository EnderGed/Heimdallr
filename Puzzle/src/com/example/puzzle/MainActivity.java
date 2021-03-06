package com.example.puzzle;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;


/** 
 * Main Activity for loading game - puzzle
 * 
 * @author Aleksandra Mielcarek 
 */  
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Panel(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}