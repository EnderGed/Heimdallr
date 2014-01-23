package com.example.puzzle;


import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class Panel {
	
	private Activity c;
	private final int number = 4;
    public ImageView[] parts= new ImageView[number*number];
    public Button button;
	private int[] tab = new int[number*number];
    private int[] tab2 = new int[number*number];
	private int act = number*number-1;
	
	public Panel(Activity c) {
		this.c = c;
		start_of_game();
	}
	
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
    
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    
    public void swap(int i, int l) {
    	s(i,l);
    	act = i;
    	tab[l] = 1;
    	int t=tab2[i];
    	tab2[i] = tab2[l];
    	tab2[l] = t;
    	tab=new int[number*number];
    	//do poprawki
    	try {
    		tab[i+1]=1;
    	} catch (ArrayIndexOutOfBoundsException e) {}
    	try {
    		tab[i-1]=1;
    	} catch (ArrayIndexOutOfBoundsException e) {}
    	try {
    		tab[i-number]=1;
    	} catch (ArrayIndexOutOfBoundsException e) {}
    	try {
    		tab[i+number]=1;
    	} catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    private boolean end() {
    	for (int i=0;i<number*number; i++) {
    		if (tab2[i] != i) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public void koniec(int i) {
    	if(tab[i]==1) {
        	swap(i,act);
    	}
    	if (i==number*number-1) {
    		if(end()) {
    			System.out.println("end of game");
    			//do something
    		}
    	}
    }
    
    private void shufle() {
		Random r = new Random();
		int a;
    	for (int i=0; i<number*number-1; i++) {
    		a=r.nextInt(8-0);
    		int temp = tab2[a];
    		tab2[a] = tab2[i];
    		tab2[i] = temp;
    	}
    	for (int i=0;i<number*number-1;i++) {
    		s(i, tab2[i]);
    	}
    }
    
    private void s(int i, int j) {
    	Drawable myDrawable = parts[i].getDrawable();
    	parts[i].setImageDrawable(parts[j].getDrawable());
    	parts[j].setImageDrawable(myDrawable);
    }
    
	private void start_of_game() {
		Display display = c.getWindowManager().getDefaultDisplay();
    	Point size = new Point();

    	for (int i=0; i<number*number;i++) {
    		tab2[i] = i;
    	}
    	display.getSize(size);
    	int targetWidth = size.x;
    	int targetHeight = size.x;
    	int szerokosc = targetWidth/number;
    	int wysokosc = targetHeight/number;
         //dobra to potem jakos ladniej napisze
         parts[0] = (ImageView)c.findViewById(R.id.image1);
         parts[1] = (ImageView)c.findViewById(R.id.image2);
         parts[2] = (ImageView)c.findViewById(R.id.image3);
         parts[3] = (ImageView)c.findViewById(R.id.image4);
         parts[4] = (ImageView)c.findViewById(R.id.image5);
         parts[5] = (ImageView)c.findViewById(R.id.image6);
         parts[6] = (ImageView)c.findViewById(R.id.image7);
         parts[7] = (ImageView)c.findViewById(R.id.image8);
         parts[8] = (ImageView)c.findViewById(R.id.image9);
         parts[9] = (ImageView)c.findViewById(R.id.image10);
         parts[10] = (ImageView)c.findViewById(R.id.image11);
         parts[11] = (ImageView)c.findViewById(R.id.image12);
         parts[12] = (ImageView)c.findViewById(R.id.image13);
         parts[13] = (ImageView)c.findViewById(R.id.image14);
         parts[14] = (ImageView)c.findViewById(R.id.image15);
         parts[15] = (ImageView)c.findViewById(R.id.image16);
        /* button = (Button) c.findViewById(R.id.button);
         button.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                   // Perform action on click
              	 //ok tutaj bedzie jeszcze wyswietlanie obrazka - jesli nie trzeba, to nie bede robic
               }
           });*/
         Bitmap targetBitmap = decodeSampledBitmapFromResource(c.getResources(), R.drawable.obraz, targetWidth, targetHeight);
         for (int i=0;i<number;i++) {
        	 for (int j=0;j<number;j++) {
        		 Matrix matrix = new Matrix();
                 matrix.postScale(1f, 1f);
                 if(i!=(number-1) || j!=(number-1)) {
                	 parts[i*number+j].setImageBitmap(Bitmap.createBitmap(targetBitmap, j*szerokosc, i*wysokosc, szerokosc, wysokosc, matrix, true));
                 }
        	 }
         }
         tab[number*number-2] = 1;
         tab[number*(number-1)-1] = 1;
         for (int i=0;i<number*number;i++) {
        	 parts[i].setOnClickListener(new View.OnClickListener() {
    			    @Override
    			    public void onClick(View v) {
    			    	for (int i=0;i<number*number;i++) { 
			    			if(parts[i]==v)
			    				koniec(i);
    			    	}
    			    }
    			});
        	 }

         
        shufle();
 
    }
}
	