package com.example.datasender;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.EditText;

public class MainActivity extends Activity {
	private EditText editText;
	private TCPClient tcpClient;
	private connectTask cnctTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText = (EditText) findViewById(R.id.editText1);
		tcpClient = null;
		cnctTask = new connectTask();
        cnctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
        //teraz brzydki, tymczasowy kod
        byte[] message = new byte[1024];
        int len;
        for(int i=0; i<100; i++){
        	message[0] = 1;
        	message[1] = (byte)i;
        	message[2] = message[3] = message[4] = message[5] = message[6] = 0;
        	len = 7;
        	tcpClient.sendMessage(message, len);
        	
        }
        //koniec brzydkiego kodu
		
	}
	
	public class connectTask extends AsyncTask<String,String,TCPClient>{

		@Override
		protected TCPClient doInBackground(String... params) {
			tcpClient = new TCPClient(new TCPClient.onMessageRecieved() {
				
				@Override
				public void messageRecieved(byte[] message, int len) {
					message[len] = '\0';
					editText.setText(new String(message));
				}
			});
			
			tcpClient.start();
			
			return null;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		tcpClient.stop();
		cnctTask.cancel(true);
		cnctTask = null;
		super.onDestroy();
		
	}

}
