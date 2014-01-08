package com.example.akcelerometr_zyroskop;


import java.util.ArrayList;
import com.androidplot.ui.Formatter;
import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import com.androidplot.Plot;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;

import android.graphics.Color;

import com.androidplot.xy.*;
import com.androidplot.series.*;



public class Wykres {
	
	public ArrayList<Double> x;
	public ArrayList<Double> y;
	public XYPlot graph;
	
	Wykres (XYPlot wykres, ArrayList<Double> a, ArrayList<Double> b){
		this.graph=wykres;
		this.x=a;
		this.y=b;
	}
	
	public void rysuj(){
	
		
		//tworzymy serie
		XYSeries zaleznosc = new SimpleXYSeries(x,y,"Przyspieszenie");
		
		
		
		graph.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        graph.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
        graph.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));


        graph.getBorderPaint().setStrokeWidth(1);

    
		//kolor osi X
		graph.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
		//kolor osi Y
		graph.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

		//usuwam legendê
		graph.getLayoutManager().remove(graph.getLegendWidget());
    
		//odsuniecie napisow od wykresu
		graph.getGraphWidget().setPadding(0, 0, 8, 0);
    
		//Margines od brzegow zewnetrznych
		graph.setPlotMargins(10, 10, 10, 10);
		graph.setPlotPadding(10, 10, 10, 10);
 
		
        graph.setMarkupEnabled(false);
		//typ obramowanie calej formy
		graph.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
		//kolor powyzszej
		graph.getBorderPaint().setColor(Color.WHITE);

		//antyaliassing obramowania
		graph.getBorderPaint().setAntiAlias(false);

    
		// setup our line fill paint to be a slightly transparent gradient:
		       
		Paint lineFill = new Paint();
		lineFill.setAlpha(0);

    	//kolor kolejno linii, punktu i wype³nienia
    	LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0,0,0), Color.rgb(0,0,0), null);
    	formatter.setFillPaint(lineFill);
    	graph.getGraphWidget().setPaddingRight(2);

    	//dodajemy nasza serie + format linii
    	graph.addSeries(zaleznosc, formatter);
    
    	//okresla skok/krok skali domeny, w tym przypadku nastepuje co rok
    	//graph.setDomainStep(XYStepMode.SUBDIVIDE, x.length);

    	// opisy osi
    	graph.setDomainLabel("t");      
    	graph.setRangeLabel("a");
  
    	//precyzja zakresu     
    	//graph.setRangeValueFormat(new DecimalFormat("0"));
    	//graph.setDomainValueFormat(new DecimalFormat("0"));

    	//wlacz a przekonasz sie ;)
    	//graph.disableAllMarkup();
    	graph.redraw();
	}
}
