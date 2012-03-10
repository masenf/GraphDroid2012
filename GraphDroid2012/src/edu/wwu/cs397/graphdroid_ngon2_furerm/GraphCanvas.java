package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.ArrayList;

import edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe.FuncContainer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * The graph canvas, in charge to draw the graph
 * I mostly have no idea what i'm doing
 *
 * @author ngon2
 *
 */

public class GraphCanvas extends SurfaceView implements SurfaceHolder.Callback{
	public static final int COORD_NUMBER = 12;
	private CanvasThread canvasThread;
	
	
	private int graphType = 0; // 0: Cartesian, 1: Polar, 2: ... (fill in later)
	private boolean showGrid = true; 
	private boolean showProjection = false;
	private boolean showCoordNumber = true;
	private boolean showCoord = true;
	
	public GraphCanvas(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public GraphCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		this.getHolder().addCallback(this);
		this.canvasThread = new CanvasThread(getHolder());
		this.setFocusable(true);
		
	}
		
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    	
    }
 
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
    	this.canvasThread = new CanvasThread(getHolder());
    	canvasThread.setRunning(true);
    	canvasThread.start();
    }
		
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		boolean retry = true;
		canvasThread.setRunning(false);
		while(retry) {
			try {
				canvasThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Convert the (x, y) point to the actual point in the screen
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getScreenCoord(double x, double y)
	{
		FuncContainer funcList = FuncContainer.getIns(this.getContext());
		double d0 = funcList.getDomain().first;
		double d1 = funcList.getDomain().second;
		
		double r0 = funcList.getRange().first;
		double r1 = funcList.getRange().second;
		
		Point r = new Point();
		r.x = (int) Math.round(Math.abs(x - d0)/Math.abs(d1-d0) * getWidth());
		r.y = (int) Math.round(Math.abs(r1 - y)/Math.abs(r1-r0) * getHeight());
		return r;
	}
	
	/**
	 * Translate back the coord from screen to (x,fx)
	 * @param x
	 * @param y
	 * @return
	 */
	public Pair<Double, Double> getMathCoord(double sx, double sy)
	{
		FuncContainer funcList = FuncContainer.getIns(this.getContext());
		double d0 = funcList.getDomain().first;
		double d1 = funcList.getDomain().second;
		
		double r0 = funcList.getRange().first;
		double r1 = funcList.getRange().second;
		
		double x = sx * Math.abs(d1 - d0) / getWidth() + d0;
		double y = r1 - sy * Math.abs(r1- r0) / getHeight(); 
		
		Pair<Double, Double> r = new Pair<Double, Double>(x, y);		
		return r;
	}
	
	/**
	 * draw the background of the canvas
	 * supposed to be called only in onDraw
	 */
	private void drawBg(Canvas canvas)
	{
		Paint bgpaint = new Paint();
		bgpaint.setColor(Color.WHITE);		
		Rect bgrect = new Rect();
		bgrect.left = 0;
		bgrect.top = 0;
		bgrect.right = getWidth();
		bgrect.bottom = getHeight();
		canvas.drawRect(bgrect, bgpaint);
	}
	
	/**
	 * draw the Cartesian Coordinate
	 * supposed to be called only by onDraw
	 * @param canvas
	 */
	private void drawCartesianCoord(Canvas canvas)
	{
		FuncContainer funcList = FuncContainer.getIns(this.getContext());
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		
		Paint pgrid = new Paint();
		pgrid.setColor(Color.LTGRAY);
		
		double d0 = funcList.getDomain().first;
		double d1 = funcList.getDomain().second;
		
		double r0 = funcList.getRange().first;
		double r1 = funcList.getRange().second;
			
		Point orgin = new Point();
		
		// draw the x = 0 first
		
		if (d0 * d1 < 0)
		{
			orgin.x = getScreenCoord(0, 0).x;		
		}
		else
		{
			orgin.x = 50;
		}
		
		if (r0 * r1 < 0)
		{
			orgin.y = getScreenCoord(0, 0).y;
		}
		else
			orgin.y = 550;
		
		if (showCoord)
		{
			p.setStrokeWidth(3);
			p.setColor(Color.GRAY);
			canvas.drawLine(orgin.x, 0, orgin.x, getHeight(), p); // x = 0
			canvas.drawLine(0, orgin.y, getWidth(), orgin.y, p); // y = 0
		}
				
		p.setStrokeWidth(0);
		p.setColor(Color.DKGRAY);
		
		// this is drawing the x = 0 line
		double segy = Math.abs(r1 - r0) / (double) COORD_NUMBER;
		double orginCoordy = getMathCoord(orgin.x, orgin.y).second;
		
		for (double y = orginCoordy; y < r1; y += segy)
		{
			String t = String.format("%.2f", y);
			float textw = p.measureText(t) + 5;
			int ay = getScreenCoord(0, y).y;
		
			if (showCoord)			
				canvas.drawLine(orgin.x - 4, ay, orgin.x + 4, ay, p);
			
			if (showCoordNumber)
				canvas.drawText(t, orgin.x - textw, (float)ay - 5, p);

			if (showGrid)
				if (ay != orgin.y)
					canvas.drawLine(0, ay, getWidth(), ay, pgrid);
		}
		
		for (double y = orginCoordy - segy; y > r0; y -= segy)
		{
			String t = String.format("%.2f", y);
			float textw = p.measureText(t) + 5;
			int ay = getScreenCoord(0, y).y;
			
			if (showCoord)		
				canvas.drawLine(orgin.x - 4, ay, orgin.x + 4, ay, p);
			
			if (showCoordNumber)
				canvas.drawText(t, orgin.x - textw, (float)ay - 5, p);
			
			if (showGrid)
				if (ay != orgin.y)
					canvas.drawLine(0, ay, getWidth(), ay, pgrid);
		}
					
		// draw y = 0 line
		double segx = Math.abs(d1 - d0) / (double) COORD_NUMBER;
		double orginCoordx = getMathCoord(orgin.x, orgin.y).first;
		
		for (double x = orginCoordx; x < d1; x += segx)
		{
			String t = String.format("%.2f", x);
			int ax = getScreenCoord(x, 0).x;
			
			if (showCoord)
				canvas.drawLine(ax, orgin.y - 4, ax, orgin.y + 4, p);
			
			if (showCoordNumber)
				canvas.drawText(t, ax + 4, orgin.y + 20, p);
			
			if (showGrid)
				if (ax != orgin.x)
					canvas.drawLine(ax, 0, ax, getHeight(), pgrid);
		}
		
		for (double x = orginCoordx - segx; x > d0 ; x -= segx)
		{
			String t = String.format("%.2f", x);
			int ax = getScreenCoord(x, 0).x;
			
			if (showCoord)
				canvas.drawLine(ax, orgin.y - 4, ax, orgin.y + 4, p);
			
			if (showCoordNumber)
				canvas.drawText(t, ax + 4, orgin.y + 20, p);
			
			if (showGrid)
				if (ax != orgin.x)
					canvas.drawLine(ax, 0, ax, getHeight(), pgrid);
		}
				
	}
	
	Paint funcP = new Paint();		
	
	private void drawFunc(Canvas canvas, long id)
	{
		FuncContainer funcList = FuncContainer.getIns(this.getContext());
		
		double d0 = funcList.getDomain().first;
		double d1 = funcList.getDomain().second;
		
		double r0 = funcList.getRange().first;
		double r1 = funcList.getRange().second;
		
		double step = funcList.getStep();
		for (double x = d0; x < d1 - step; x+=step)
		{
			try
			{								//
				double y = funcList.getValue(id, x);
				double ye = funcList.getValue(id, x + step);
				Point spoint = getScreenCoord(x, y);
				Point epoint = getScreenCoord(x + step, ye);
				if (y >= r0 && y <= r1 && ye >= r0 && ye <= r1)
				{
					//Log.v("draw", String.format("Drawing value (%f, %f)", x, y));
					canvas.drawLine(spoint.x, spoint.y, epoint.x, epoint.y, funcP);
				}
			}
			catch (Exception ex)
			{
				Log.e("graphcanvas", ex.getMessage());
			}
		}

	}
	
	private void drawFunctions(Canvas canvas)
	{
		FuncContainer funcList = FuncContainer.getIns(this.getContext());
		ArrayList<Long> fs = funcList.getVisibleFunc();
		for (long id : fs)
		{
			funcP.setColor(funcList.getColor(id));
			Log.v("draw", String.format("Line color:%d", funcList.getColor(id)));
			funcP.setStrokeWidth(funcList.getLineWidth(id));
//			funcP.setColor(Color.BLACK);
//			funcP.setStrokeWidth(2);
			drawFunc(canvas, id);
			
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub

		// first draw the background
		drawBg(canvas);
		if (graphType == 0)
			drawCartesianCoord(canvas);
		
		drawFunctions(canvas);
	}

	
	private class CanvasThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private boolean isRun = false;
		
		public CanvasThread(SurfaceHolder holder) {
			this.surfaceHolder = holder;
		}
		
		public void setRunning(boolean run) {
			this.isRun = run;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Canvas c;
			
			while(isRun) {
				c = null;
				try {
					c = this.surfaceHolder.lockCanvas(null);
					synchronized(this.surfaceHolder) {
						GraphCanvas.this.onDraw(c);
					}
				} finally {
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
	/**
	 * @return the graphType: 0: Cartesian, 1: Polar, 2: ... (fill in later)
	 */
	public int getGraphType() {
		return graphType;
	}

	/**
	 * @param graphType 0: Cartesian, 1: Polar, 2: ... (fill in later)
	 */
	public void setGraphType(int graphType) {
		this.graphType = graphType;
	}

	/**
	 * @return the showGrid
	 */
	public boolean isShowGrid() {
		return showGrid;
	}

	/**
	 * @param showGrid the showGrid to set
	 */
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	/**
	 * @return the showProjection
	 */
	public boolean isShowProjection() {
		return showProjection;
	}

	/**
	 * @param showProjection the showProjection to set
	 */
	public void setShowProjection(boolean showProjection) {
		this.showProjection = showProjection;
	}

	/**
	 * @return the showCoordNumber
	 */
	public boolean isShowCoordNumber() {
		return showCoordNumber;
	}

	/**
	 * @param showCoordNumber the showCoordNumber to set
	 */
	public void setShowCoordNumber(boolean showCoordNumber) {
		this.showCoordNumber = showCoordNumber;
	}

	/**
	 * @return the showCoord
	 */
	public boolean isShowCoord() {
		return showCoord;
	}

	/**
	 * @param showCoord the showCoord to set
	 */
	public void setShowCoord(boolean showCoord) {
		this.showCoord = showCoord;
	}
}
