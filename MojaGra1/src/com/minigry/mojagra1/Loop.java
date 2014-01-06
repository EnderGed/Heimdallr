package com.minigry.mojagra1;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class Loop extends Thread {

	private static int sleepTime = 60; 
	
	private SurfaceHolder holder;
	private Game game;
	
	public Loop(SurfaceHolder holder, Game game) {
		super();
		this.holder = holder;
		this.game = game;
	}
	
	@Override
	public void run() {
		
		Canvas canvas = null;
		long time = System.currentTimeMillis();
		long dif = 0;
		
		boolean gameRunning = true;
		while (gameRunning) {
			
			try {
				sleep(sleepTime);
				
				canvas = holder.lockCanvas();
				if (canvas == null) continue;
				
				synchronized(holder) {
					dif = System.currentTimeMillis();
					gameRunning = game.update(dif-time);
					time = dif;
					game.draw(canvas);
				}
				
			} catch(InterruptedException e) {
				//shutdown
			} finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
		}
		//END GAME???
	}
}
