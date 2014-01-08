package com.example.akcelerometr_zyroskop;


import java.io.*;

import android.os.Environment;


public class Zapisywacz {
	FileOutputStream fos;
    Zapisywacz(String nazwaPliku){
    	File file = new File(Environment.getExternalStorageDirectory(), nazwaPliku);
    	try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void dopisz(String linia){
        linia += "\n";
        try {
			fos.write(linia.getBytes());
	        fos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void koniec(){
    	try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
