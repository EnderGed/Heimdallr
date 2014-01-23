package com.example.datasender;

//same as Area, but doesn't have latitude and longitude
public class AreaMod {
	public int radius;	//w metrach
	public int relativeRadiusHeight;	//srednica w pikselach
	public int relativeRadiusWidth;		//srednica w pikselach
	public int relativeX;	//lewy, gorny rog obszaru, wzgledem lewego, gornego rogu mapy, w pixelach
	public int relativeY;	//lewy, gorny rog obszaru, wzgledem lewego, gornego rogu mapy, w pixelach
	
	public AreaMod(int rad, int relRadH, int relRadW, int x, int y){
		radius = rad;
		relativeRadiusHeight = relRadH;
		relativeRadiusWidth = relRadW;
		relativeX = x - relativeRadiusWidth/2;
		relativeY = y - relativeRadiusHeight/2;
	}
}
