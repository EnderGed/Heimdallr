package com.horn.game;

import com.horn.heimdallr.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class MapView extends SurfaceView{
	private final Bitmap map;
	private final int mapHeight;
	private final int mapWidth;
	private final double minLongitude;
	private final double gpsWidth;
	private final double maxLatitude;
	private final double gpsHeight;
	private final float mapHeightInMeters;
	private final float mapWidthInMeters;
	private int viewHalfHeight;	//polowa wysokosci
	private int viewHalfWidth;	//polowa szerokosci
	private int x;
	private int y;
	private Paint positionPaint;
	private Paint bombPaint;
	private Paint destinationPaint;
	private Area destination;
	private java.util.Map<Pair<Double, Double>,AreaMod> bombs;
	private RectF rectangle;	//pomocniczy przy rysowaniu
/*
    public MapView(Context context) {
        this(context, null);
    }*/

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        android.util.Log.e("MapView","created");
        map = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        this.setWillNotDraw(false);
        mapHeight = map.getHeight();
        mapWidth = map.getWidth();
        minLongitude = 16.969702;
        gpsWidth = 16.977545 - minLongitude;
        maxLatitude = 51.038884;
        gpsHeight = maxLatitude - 51.034526;
        mapHeightInMeters = distFrom(maxLatitude - gpsHeight,minLongitude,maxLatitude,minLongitude) * 1000;
        mapWidthInMeters = distFrom(maxLatitude,minLongitude,maxLatitude,minLongitude+gpsWidth) * 1000;
        x = 0;
        y = 0;
        positionPaint = new Paint();
        bombPaint = new Paint();
        destinationPaint = new Paint();
        bombPaint.setARGB(125, 215, 0, 0);
        destinationPaint.setARGB(200, 0, 215, 0);
        bombs = new java.util.HashMap<Pair<Double,Double>,AreaMod>();
        rectangle = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	android.util.Log.e("MapView","onDraw");
        android.util.Log.d("MapView","Map size [m]: "+mapWidthInMeters+" "+mapHeightInMeters);
        Canvas g = canvas;
        //rysowanie mapy
        if (map != null) {
            g.drawBitmap(map, -x, -y, null);
        }
        //rysowanie pozycji
        g.drawCircle(viewHalfWidth, viewHalfHeight, 20f, positionPaint);
        //rysowanie celu
        if (destination != null){
        	rectangle.top = destination.relativeY-y;
        	rectangle.bottom = destination.relativeY-y+destination.relativeRadiusHeight;
        	rectangle.left = destination.relativeX-x;
        	rectangle.right = destination.relativeX-x+destination.relativeRadiusWidth;
        	//jezeli cel jest poza mapa
        	if((rectangle.top<10 || rectangle.bottom>viewHalfHeight*2-10) &&
        			(rectangle.left > viewHalfWidth*2-10 || rectangle.right < 10)){
        		//to na rysuj linie laczaca ciebie z celem
	        	g.drawLine(viewHalfWidth, viewHalfHeight,
	        			destination.relativeX-x+destination.relativeRadiusWidth/2,
	        			destination.relativeY-y+destination.relativeRadiusHeight/2,
	        			destinationPaint);
        	} else{
        		//narysuj cel
            	g.drawOval(rectangle, destinationPaint);
        	}
        }
        for(AreaMod bomb : bombs.values()){
        	rectangle.top = bomb.relativeY-y;
        	rectangle.bottom = bomb.relativeY-y+bomb.relativeRadiusHeight;
        	rectangle.left = bomb.relativeX-x;
        	rectangle.right = bomb.relativeX-x+bomb.relativeRadiusWidth;
        	//jezeli znajdzie sie w ogole w obszarze, to narysuj
        	if(rectangle.bottom>0 && rectangle.top<viewHalfHeight*2 &&
        			rectangle.right < viewHalfWidth*2 && rectangle.left > 0)
        		g.drawOval(rectangle, bombPaint);
        	android.util.Log.d("Bomb","Placed on "+ bomb.relativeX+" "+bomb.relativeY+" "+bomb.relativeRadiusHeight);
        	android.util.Log.d("Bomb","Drawing on "+rectangle.top+ " "+rectangle.bottom+" "+rectangle.left+" "+rectangle.right);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        viewHalfHeight = heightSize/2;
        viewHalfWidth = widthSize/2;

        setMeasuredDimension(widthSize, heightSize);
    }
    
    public void updateMapPosition(double latitude, double longitude){
    	//wyznacenie srodka, procentowo na mapie
    	android.util.Log.d("mapView",latitude+" "+longitude);
    	latitude = maxLatitude - latitude;
    	longitude = longitude - minLongitude;
    	android.util.Log.d("mapView",latitude+" "+longitude);
    	android.util.Log.d("mapView",gpsWidth+" "+gpsHeight);
    	
    	//wyznaczenie srodkowego piksela
    	int midX = (int)(longitude*mapWidth/gpsWidth);
    	int midY = (int)(latitude*mapHeight/gpsHeight);
    	android.util.Log.d("mapView",midX+" "+midY);
    	
    	//wyznaczenie x, y - lewego, gornego rogu
    	x = midX - viewHalfWidth;
    	y = midY - viewHalfHeight;
    	
    	this.invalidate();
    }
    
    void setDestination(Area newDest){
    	destination = newDest;
    	if(map != null){
    		destination.relativeRadiusHeight = (int)(destination.radius*mapHeight/mapHeightInMeters*2);
    		destination.relativeRadiusWidth = (int)(destination.radius*mapWidth/mapWidthInMeters*2);
    		destination.relativeX = (int) ((destination.longitude - minLongitude)*mapWidth/gpsWidth - destination.relativeRadiusWidth);
    		destination.relativeY = (int) ((maxLatitude - destination.latitude)*mapHeight/gpsHeight - destination.relativeRadiusHeight);
    	}
    }
    
    void addBomb(Area bomb){
    	Pair<Double,Double> p = new Pair<Double,Double>(bomb.longitude,bomb.latitude);
    	AreaMod am = new AreaMod(bomb.radius,
    			(int)(bomb.radius*mapHeight/mapHeightInMeters)*2,
    			(int)(bomb.radius*mapWidth/mapWidthInMeters)*2,
    			(int)((bomb.longitude - minLongitude)*mapWidth/gpsWidth),
    			(int)((maxLatitude - bomb.latitude)*mapHeight/gpsHeight));
    	bombs.put(p, am);
    }
    
    void removeBomb(Area bomb){
    	Pair<Double,Double> p = new Pair<Double,Double>(bomb.longitude,bomb.latitude);
    	bombs.remove(p);
    }
    
    private float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6378.137;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return (float) dist;
       }
}
