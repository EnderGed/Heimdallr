package com.minigry.mojagra1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;

public class Game {
	
	//fields represent body, empty, wall, item
	public final String fields = "- #*";
	public final int tileSize = 10;
	
	private int points;
	private int level;
	
	private double speed;
	private int growth;
	private char[][] board;
	private int hx, hy, tx, ty; //position of head and tail
	private int dx,dy;			//current direction of slithering
	private int toGrow;			//segments left to grow
	
	private Resources res;
	private int width,height;
	private BitmapFactory.Options opt;
	private Bitmap head,snake,item,fence;
	private Paint text,basic = new Paint();
	
	private Random rand = new Random();
	
	
	
	public Game(Context c, int width, int height) {
		this.res = c.getResources();
		this.width = (int)(width/tileSize);
		this.height = (int)(height/tileSize);
		this.vibr = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		text = new Paint();
		text.setColor(Color.RED); text.setTextSize(20);
		opt = new BitmapFactory.Options();
		opt.inDensity = 1; opt.inTargetDensity = 1;
		
		snake = BitmapFactory.decodeResource(res,R.drawable.body,opt);
		item = BitmapFactory.decodeResource(res,R.drawable.item,opt);
		head = BitmapFactory.decodeResource(res,R.drawable.head,opt);
		fence = BitmapFactory.decodeResource(res,R.drawable.fence,opt);
		
		points = 0;
		level = 0;
		toGrow = 0;
		
		board = new char[this.width][this.height];
		for (int i=0; i<this.width; i++) {
			for (int j=0; j<this.height; j++) {
				board[i][j]=' ';
			}
		}
		for(int i=0;i<15;i++)board[i][0]='-';
		tx=ty=hy=0;hx=14;dx=1;dy=0;
		initBoard();
		
		//temporary
		speed = 10f;
		growth = 8;
	}
	
	//turn left(true) or right
	public void turn(boolean left) {
		
		if (dx==0) {
			if (left) dx=dy;
			else dx=(-1)*dy;
			dy=0;
		}
		else {
			if (left) dy=dx;
			else dy=(-1)*dx;
			dx=0;
		}
	}
	
	public void turn(int dirx, int diry) {
		
		if (dx==0 && diry*dy==-1) return;
		if (dy==0 && dirx*dx==-1) return;
		dx=dirx; dy=diry;
	}
	

	public boolean update(long timeDiff) {
		
		int distanceSlithered = (int)(speed * ((float)timeDiff/1000));
		
		for (int i=0; i<distanceSlithered; i++) {
			
			if (toGrow > 0)
				toGrow -= 1;
			else {
				board[tx][ty] = ' ';
			
				if (board[(tx+1)%width][ty]=='-') tx += 1;
				else if (board[tx][(ty+1)%height]=='-') ty += 1;
				else if (board[mod(tx-1,width)][ty]=='-') tx -= 1;
				else if (board[tx][mod(ty-1,height)]=='-') ty -= 1;
			}
			
			hx += dx; hy += dy;
			tx = mod(tx,width); hx = mod(hx,width);
			ty = mod(ty,height); hy = mod(hy,height);
			
			//check if it ate something!!!
			if (!processNextField(hx, hy))
				return false;
			
			processRandomEvents();
			
		}
		return true;
	}
	
	public void draw(Canvas c) {

		c.drawColor(Color.BLACK);
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (i == hx && j == hy) {
					c.drawBitmap(head, i*tileSize, j*tileSize, basic);
					continue;
				}
				if (board[i][j]=='-') {
					c.drawBitmap(snake, i*tileSize, j*tileSize, basic);
					continue;
				}
				if (board[i][j]=='*') {
					c.drawBitmap(item, i*tileSize, j*tileSize, basic);
					continue;
				}
				if (board[i][j]=='#') {
					c.drawBitmap(fence, i*tileSize, j*tileSize, basic);
					continue;
				}
			}
		}
		c.drawText("points: "+Integer.toString(points), width*tileSize-100, 15, text);
	}
	
	private void initBoard() {
		
		if(board == null) return;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(res.openRawResource(R.raw.board1), "UTF8"));
			String s = br.readLine(); s = br.readLine();
			dx = Integer.parseInt(s.substring(0, 1));
			dy = Integer.parseInt(s.substring(2, 3));
			s = br.readLine();
			int j,i=0;
			char c;
			for (i=0; s != null && i<height; i++, s=br.readLine()) {
				j=0;
				for (j=0; j<width && j<s.length(); j++) {
					c = s.charAt(j);
					if (c=='H') {
						hx=j; hy=i;
						board[j][i] = '-';
					}
					else if (c=='T') {
						tx=j; ty=i;
						board[j][i] = '-';
					}
					else
						board[j][i] = c;
				}
				for (; j<width; j++)
					board[j][i] = ' ';
			}
			for (; i<height; i++)
				for (j=0; j<width; j++)
					board[j][i] = ' ';
			
		} catch(IOException e) {
			System.err.println("file not found");
		}
	}
	
	private boolean processNextField(int x, int y) {
		if (board[x][y] == '-') {
			return false;
		}
		if (board[x][y] == '#') {
			return false;
		}
		if (board[x][y] == '*') {
			scorePoint();
			board[x][y] = '-';
			return true;
		}
		board[x][y] = '-';
		return true;
	}
	
	
	private Vibrator vibr;
	/* utrudnienia, np.
	 * - wąż porusza się tak szybko jak osoba(accelerometer)
	 * - pomieszanie planszy
	 * - pole magnetyczne przyciąga węża/murek
	 */
	private void processRandomEvents() {
		
		if (rand.nextFloat() < 0.01) vibr.vibrate(5000);
		
		if (rand.nextFloat() < 0.01) speed += 0.2;
		
		/*if (rand.nextFloat() < 0.05) {
			int tmp = hx; hx = tx; tx = tmp;
			tmp = hy; hy = ty; ty = tmp;
			//
		}*/
		
		
	}
	
	private void scorePoint() {
		
		points += 1;
		toGrow = growth;
		
		int i = rand.nextInt(width * height);
		while (board[i%width][i%height] != ' ')
			i = (i+1)%(width*height);
		board[i%width][i%height] = '*';
	}
	
	private int mod(int n, int m) {
		return (n < 0) ? (m - (Math.abs(n)%m) )%m:(n%m);
	}
	
	public void setSpeed(double s) {speed = s;}
}
