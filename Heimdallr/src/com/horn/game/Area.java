package com.horn.game;

public class Area {
	public double longitude;
	public double latitude;
	public int radius;	//w metrach
	public int relativeRadiusHeight = 0;	//srednica w pikselach
	public int relativeRadiusWidth = 0;		//srednica w pikselach
	public int relativeX = 0;	//lewy, gorny rog obszaru, wzgledem lewego, gornego rogu mapy, w pixelach
	public int relativeY = 0;	//lewy, gorny rog obszaru, wzgledem lewego, gornego rogu mapy, w pixelach
	
	public Area(double longi, double lat, int rad){
		longitude = longi;
		latitude = lat;
		radius = rad;
	}
}
