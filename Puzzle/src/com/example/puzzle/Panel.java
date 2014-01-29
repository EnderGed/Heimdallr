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
import android.os.SystemClock;
import android.widget.TextView;
import android.os.Handler;
@SuppressLint("NewApi")
public class Panel {
	
	private Activity c;
	/** 
	 * Number of pictures in one row
	 */
	private final int number = 4;
	/** 
	 * Pictures (puzzle elements)
	 */
    public ImageView[] parts= new ImageView[number*number];
    public Button button1, button2;
    private TextView timerValue;
	private int[] tab = new int[number*number];
    private int[] tab2 = new int[number*number];
	private int act = number*number-1;
	private long startTime = 0L;
	private Handler customHandler = new Handler();
	long timeInSeconds = 0L;
	long updatedTime = 0L;
	long timeLimit = 300000;
	
	
	public Panel(Activity c) {
		this.c = c;
		start_of_game();
	}
	
	/** 
	 * Method to calculate size of picture
	 * 
	 * @param reqWidth - width that picture should have
	 * @param reqHeight - height that picture should have
	 * @return calculated size 
	 */  
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
   }
    
	/** 
	 * Method that resize picture
	 * 
	 * @param res - resurses
	 * @param resId - id of picture to be resized
	 * @param reqWidth - width we want to have
	 * @param reqHeight - height we want to have
	 * @return resized image
	 */  
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
    
    /** 
	 * Method is checking if puzzle has been solved 
	 * @return true - the end of game
	 * 			false - otherwise
	 */  
    private boolean check_if_end() {
    	for (int i=0;i<number*number; i++) {
    		if (tab2[i] != i) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /** 
   	 * Method which starts method swap and check_if_end
	 * @param i - index of picture in parts
   	 */ 
    public void action_on_image_click(int i) {
    	if(tab[i]==1) {
        	swap(i,act);
	    	if (i==number*number-1) {
	    		if(check_if_end()) {
	    			System.out.println("end of game");
	    			//do something
	    		}
	    	}
    	}
    }
    
    /** 
   	 * Method which shufle elements of parts and starts timer
   	 */ 
    private void shufle() {
		Random r = new Random();
		int a;
		int[] tab3 = new int[number*number];
		for (int i=0; i< number*number; i++) {
			tab3[i] = i;
		}
    	for (int i=0; i<number*number; i++) {
    		a=r.nextInt((number*number-1)-0);
    		if(i!=act && a!=act) {
	    		tab3[i] = a;
    		}
    	}
    	for (int i=0; i<number*number;i++) {
    		if(i<tab3[i]) {
    			int temp = tab2[i];
    			tab2[i] = tab2[tab3[i]];
    			tab2[tab3[i]] = temp;
    			s(i, tab3[i]);
    		}
    	}
    	startTime = SystemClock.uptimeMillis();
    	customHandler.postDelayed(updateTimerThread, 0);
    }
    
    private void s(int i, int j) {
    	Drawable myDrawable = parts[i].getDrawable();
    	parts[i].setImageDrawable(parts[j].getDrawable());
    	parts[j].setImageDrawable(myDrawable);
    }
    
    
    /** 
   	 * Method which is call out at the beggining
   	 * It loads buttons, other view elements
	 * @param i - index of picture in parts
   	 */ 
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
         timerValue = (TextView)c.findViewById(R.id.timerValue);
         button1 = (Button) c.findViewById(R.id.button);
         button2 = (Button) c.findViewById(R.id.button2);
         button1.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                   // Perform action on click
            	   System.out.println("koniec - poddaje sie");
            	   // co z tym zrobic?
               }
           });button2.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                   // Perform action on click
            	   shufle();
               }
           });
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
			    				action_on_image_click(i);
    			    	}
    			    }
    			});
        	 }

         
        shufle();
 
    }
	
	/** 
   	 * Method which update timer
   	 */ 
	private Runnable updateTimerThread = new Runnable() {
		public void run() { 
			timeInSeconds = SystemClock.uptimeMillis() - startTime; 
		    updatedTime = timeLimit - timeInSeconds;
			if(updatedTime <= 0) {
				System.out.println("koniec - jeszcze raz");
				shufle();
				return;
			}
		    int secs = (int) (updatedTime / 1000);
		    int mins = secs / 60;
		    secs = secs % 60;
	        timerValue.setText("" + mins + ":" + String.format("%02d", secs));
	        customHandler.postDelayed(this, 0);
		}	 
	};
}
	