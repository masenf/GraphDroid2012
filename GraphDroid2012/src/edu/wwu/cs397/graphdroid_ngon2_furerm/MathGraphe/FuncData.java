package edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe;

import java.util.ArrayList;
import org.matheclipse.core.convert.AST2Expr;
import org.matheclipse.core.eval.EvalUtilities;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.Parser;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.eval.DoubleEvaluator;
import org.matheclipse.parser.client.eval.DoubleVariable;
import org.matheclipse.parser.client.eval.IDoubleValue;

import android.app.Service;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

/*
 * This class represent the function object
 * that stored the pair of <x,f(x)>
 * There is no definition of zooming in here
 * if you want to zoom, simply give it a smaller domain to draw
 * smaller domain/same viewportWidth = more precise graph
 * 
 * Version 1: No caching yet!
 */
public class FuncData {
	private static final String TAG = "FuncData";
	public static final int ZOOM_RATIO = 2;
	
	private String expr = "";
	private ASTNode parsedExpr = null;
	private OnFinishEvalListener finishEvalListener = null;
	private Thread evalThread = null;
	private Thread cacheThread = null;
	
	/*
	 * Internal cache for this function
	 */
	private FuncValueCache cache;	
	
	public int NumberOfInstance = 0; // how many instances of this function is in graph
	public long funcid = 0;
	public int axesType;
	public int lineWeight;
	public int lineType;
	public boolean valid = true;
	public int color; // color of this function
	public int group = 0; // 0: no group
	public boolean visible = true;
		
	public FuncData(String expr, int viewportWidth, int color, 
						int lineWeight, int lineType, int group, boolean visible, double step)
	{		
		this.expr = expr;
		this.color = color;
		this.lineType = lineType;
		this.lineWeight = lineWeight;
		this.group = group;
		this.visible = visible;
		
		try {
			if (expr != "")
			{
				Parser p = new Parser();				
				parsedExpr = p.parse(expr);
				
			}
		} catch (Exception ex) {
			Log.e(TAG,"Error parsing expression" + ex.toString());
			valid = false;
		}		
		cache = new FuncValueCache(viewportWidth, step);
	}
	
	public void setStep(double step)
	{
		cache.step = step;
	}
	
	public FuncData(Cursor row, int viewportWidth, double step) {
		
		Log.v(TAG,"Creating FuncData from a cursor row");
		try {
			int funcid_col = row.getColumnIndexOrThrow(FuncContainer.KEY_FUNCID);
			int func_col  = row.getColumnIndexOrThrow(FuncContainer.STR_FUNC);
			int group_col  = row.getColumnIndexOrThrow(FuncContainer.INT_GRPID);
			int axes_type_col = row.getColumnIndexOrThrow(FuncContainer.INT_AXES);
			int line_weight_col = row.getColumnIndexOrThrow(FuncContainer.INT_LINEWT);
			int line_type_col = row.getColumnIndexOrThrow(FuncContainer.INT_LINETYPE);
			int color_col = row.getColumnIndexOrThrow(FuncContainer.INT_COLOR);
			int visible_col = row.getColumnIndexOrThrow(FuncContainer.BOOL_VISIBLE);
			//int valid_col = row.getColumnIndexOrThrow(FuncContainer.BOOL_VALID);			

			row.moveToNext();
			funcid = row.getInt(funcid_col);
			expr = row.getString(func_col);
			group = row.getInt(group_col);
			axesType = row.getInt(axes_type_col);
			lineWeight = row.getInt(line_weight_col);
			lineType = row.getInt(line_type_col);
			color = row.getInt(color_col);
			visible = (row.getInt(visible_col) != 0) ? true : false;
		} catch (IllegalArgumentException ex) {
			Log.e(TAG,"Error resolving column indexes...This should never happen");
		}
		try {
			if (expr != "") {
				Parser p = new Parser();				
				parsedExpr = p.parse(expr);
				
			} else {
				valid = false;
			}
		} catch (Exception ex) {
			Log.e(TAG,"Error parsing expression " + ex.toString());
			valid = false;
		}
		cache = new FuncValueCache(viewportWidth, step);
	}
	
	public String getExpr()
	{		
		return expr;
	}
	
	public void parseExpr(String newExpr)
	{
		Log.v("fd", "func data get new expr:" + newExpr +"|");	
		this.expr = newExpr;
		Parser p = new Parser();				
		parsedExpr = p.parse(newExpr);		
	}
	
	public void setOnFinishEvalListener(OnFinishEvalListener l)
	{
		finishEvalListener = l;
	}
	
	/**
	 * you should calculate the value before querying it
	 * otherwise will just throw and exception
	 * @param x
	 * @return
	 */
	public Double fx(Double x) throws Exception
	{
		if (cache.containsKey(x))
			return cache.get(x);
		else
			throw new Exception(String.format("%f is not calculated yet", x));		
	}
	
	/**
	 * return the range of value that are already calculated, then start a new thread to 
	 * calculate the rest
	 * @param start
	 * @param end
	 * @return calculated range
	 */
	public void Eval(double start, double end)
	{	
		if (this.expr != "")
		{
			
		//ArrayList<Double> already_in_cache = cache.getCoverage(new Pair<Double, Double>(start, end));
		
//		if (evalThread != null)
//			if (evalThread.isAlive())
//				evalThread.interrupt();
//		
//		evalThread = new Thread(new EvalWorker(domain, this, finishEvalListener));		
//		evalThread.start();
		
		//try the approch without threading
			ArrayList<Double> domain = 
					cache.getNotCoverage(new Pair<Double, Double>(start, end));
			DoubleEvaluator engine = new DoubleEvaluator();
			for (double d : domain)
			{				
				try
				{
					IDoubleValue vd = new DoubleVariable(d);
					engine.defineVariable("x", vd);
					double result = engine.evaluateNode(parsedExpr);
					cache.put(d, result);			
					 
				}
				catch (Exception ex)
				{ 
					Log.e("fd", ex.getMessage());
				}						
			}
		}
	}
	
	private class EvalWorker implements Runnable {
		public ArrayList<Double> domain = null;
		private DoubleEvaluator engine = new DoubleEvaluator();
		private FuncData callerIns;
		private OnFinishEvalListener cb;
		
		public EvalWorker(ArrayList<Double> domain, FuncData callerIns, OnFinishEvalListener callback)
		{
			super();
			this.domain = domain;
			this.callerIns = callerIns;
			cb = callback;
		}
		
		public void run() {
			for (double d : domain)
			{
				IDoubleValue vd = new DoubleVariable(d);
				engine.defineVariable("x", vd);
				try
				{
					double result = engine.evaluate(expr);
					cache.put(d, result);					
				}
				catch (Exception ex)
				{ 
					// do nothing for now
				}						
			}			
			
			if (cb != null)
				cb.OnFinishEval(callerIns, domain);
		}		
	}
}
