package edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

/**
 * This is a container that manages all of the functions
 * including their details (color, linetype...)
 * @author ngon2
 *
 */
public class FuncContainer {
	private static final String TAG = "FuncContainer";
	
	private HashMap<Long, FuncData> map;
	private ArrayList<Integer> idList;
	private int count = 0;
	private int screenWidth = 1;	
	private static FuncContainer ins = null;
	private Pair<Double, Double> domain;
	private Pair<Double, Double> range;
	private OnFinishEvalListener finishEvalListener = null;
	private double step;
	private int active_group = 0;

	// functions table fields
	public static final String KEY_FUNCID = "_id";
	public static final String STR_FUNC = "function";

	public static final String INT_GRPID = "group_id";
	public static final String INT_AXES = "axes_type";
	public static final String INT_LINEWT = "line_weight";
	public static final String INT_LINETYPE = "line_type";
	public static final String INT_COLOR = "color";
	public static final String BOOL_VISIBLE = "visible";
	public static final String BOOL_VALID = "valid";

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_FUNCTIONS = "functions";
	
	public class NonExistantFunction extends Exception {
		public NonExistantFunction(String string) {
			super(string);
		}

		private static final long serialVersionUID = 1464119239212134599L;
	}
	
	// this class helps us open/create a new database
	private static class DatabaseHelper extends SQLiteOpenHelper {
		// this will generate the table structure
		private static final String SQL_DB_CREATE =
				"CREATE TABLE functions ( " +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"function TEXT, " + 
				"group_id INTEGER, " + 
				"axes_type INTEGER NOT NULL DEFAULT 0, " +
				"line_weight INTEGER NOT NULL DEFAULT 1, " +
				"line_type INTEGER NOT NULL DEFAULT -1, " +  // all 1's
				"color INTEGER NOT NULL DEFAULT 0, " +
				"visible INTEGER NOT NULL DEFAULT 0, " +
				"valid INTEGER NOT NULL DEFAULT 0)";
		
		DatabaseHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
			Log.v(TAG,"Instantiating new database helper");
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "Creating new database");
			db.execSQL(SQL_DB_CREATE);			
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
			Log.w(TAG, "Upgrading database from ver " + oldVer +
					" --> " + newVer + ". Data will be destroyed.");
			db.execSQL("DROP TABLE IF EXISTS functions");
			onCreate(db);
		}
	}
	public void close() {
		dbHelper.close();
	}	

	/**
	 * if you know that the instance is already there
	 * then screen width can be ignore
	 * @param screenWidth
	 * @return
	 */
	public static FuncContainer getIns(Context ctx)
	{		
		if (ins == null) {
			Log.v(TAG,"Creating new instance of FuncContainer");
			ins = new FuncContainer();
			ins.dbHelper = new DatabaseHelper(ctx);
			ins.db = ins.dbHelper.getWritableDatabase();
		}
		return ins;
	}
		
	private FuncContainer() {
		idList = new ArrayList<Integer>();
		map = new HashMap<Long, FuncData>();
		this.screenWidth = 1;
		domain = new Pair<Double, Double>((double)-10, (double)10);
		range = new Pair<Double, Double>((double)-10, (double)10);
		setStep();
	}
	
	/**
	 * Add an function to the system
	 * @param expr
	 * @param color
	 * @param lineType
	 * @param group
	 * @param visible
	 * @return the assigned id of that function
	 * @throws NonExistantFunction 
	 */
	public long add(String expr, int color, int lineWeight, int lineType, 
			int axesType, int group, boolean visible) 
	{
		
		ContentValues v = new ContentValues();
		v.put(STR_FUNC, expr);
		v.put(INT_GRPID, group);
		v.put(INT_AXES, axesType);
		v.put(INT_LINEWT, lineWeight);
		v.put(INT_LINETYPE, lineType);
		v.put(INT_COLOR, color);
		v.put(BOOL_VISIBLE, visible);
		
		long id =db.insert(TABLE_FUNCTIONS, null, v);

		Log.v("funcc", String.format("added function: %s at %d", expr, id));
		
		FuncData f = new FuncData(expr, getScreenWidth(), color,
						lineWeight, lineType, group, visible, getStep());
		f.funcid = id;
		f.setOnFinishEvalListener(new OnFinishEvalListener() {
			public void OnFinishEval(Object func, ArrayList<Double> domain) {
				if (finishEvalListener != null)
					finishEvalListener.OnFinishEval(func, domain);
			}
		});
		map.put(id, f);
		
		startCalc();
		return id;	
	}
	/**
	 * 
	 * @param id
	 * @return funcdata representing the function or throw exception
	 * @throws Exception 
	 */
	public FuncData get(long id) throws NonExistantFunction
	{
		String[] columns = {STR_FUNC, INT_GRPID, INT_AXES, INT_LINEWT, INT_LINETYPE, INT_COLOR, BOOL_VISIBLE};
		Cursor c;
		c = db.query(TABLE_FUNCTIONS, columns, KEY_FUNCID + " = " + id, null, null, null,null);
		if (c.getCount() > 0)
		{
			FuncData func = new FuncData(c,screenWidth,step);
			func.setOnFinishEvalListener(new OnFinishEvalListener() {
				public void OnFinishEval(Object func, ArrayList<Double> domain) {
					if (finishEvalListener != null)
						finishEvalListener.OnFinishEval(func, domain);
				}
			});
			map.put(func.funcid, func);
			startCalc();
			return func;
		} else {
			throw new NonExistantFunction("Error, function id does not exist");
		}
	}
	public Integer[] getAllGroups()
	{
		ArrayList<Integer> grps = new ArrayList<Integer>();
		Integer[] ret = new Integer[] {};
		String[] columns = {INT_GRPID};
		Cursor c;
		c = db.query(true, TABLE_FUNCTIONS, columns, null, null, null, null, INT_GRPID, null);
		while (c.moveToNext())
		{
			grps.add(c.getInt(0));
		}
		return grps.toArray(ret);
	}
	public Integer getLastGroup()
	{
		String[] columns = {INT_GRPID};
		Cursor c;
		c = db.rawQuery("SELECT MAX(" + INT_GRPID + ") FROM " + TABLE_FUNCTIONS, null);
		if (c.getCount() > 0) {
			c.moveToNext();
			Log.v(TAG, "Num columns: " + c.getColumnCount());
			return c.getInt(0);
		} else 
			return -1;
	}
	public Cursor getGroup(int grpid)
	{
        String[] columns = {KEY_FUNCID,
        					STR_FUNC, 
							INT_LINEWT, 
							INT_LINETYPE, 
							INT_COLOR, 
							BOOL_VISIBLE};
        Cursor c;
        c = db.query(TABLE_FUNCTIONS, columns, 
        		INT_GRPID + " = " + grpid, 
        		null, null, null,null);
        Log.v(TAG,"fetched group cursor: " + c.getCount() + " items.");

        while (c.getPosition() > 0) {
			FuncData func = new FuncData(c,screenWidth,step);
			if (func.getExpr() != "") {
				func.setOnFinishEvalListener(new OnFinishEvalListener() {
					public void OnFinishEval(Object func, ArrayList<Double> domain) {
						if (finishEvalListener != null)
							finishEvalListener.OnFinishEval(func, domain);
					}
				});
				map.put(func.funcid, func);
				startCalc();
			}
			Log.v(TAG,"Cursor Position: " + c.getPosition());
        }
        return c;
	}
	public void remove(long id)
	{
		Log.v(TAG, "Removing function " + id);
		db.delete(TABLE_FUNCTIONS, KEY_FUNCID + " = " + id, null);
		map.remove(id);
	}
	public void removeGroup(int grpid)
	{
		Log.v(TAG,"Removing group " + grpid);
		db.delete(TABLE_FUNCTIONS, INT_GRPID + " = " + grpid, null);
	}
	
	/**
	 * step is for drawing
	 * 
	 */
	private void setStep()
	{
		double sw = (double)screenWidth;
		step = Math.abs(domain.first - domain.second) / sw;
		
	}
	
	/**
	 * can only see it, not modify it
	 * @return
	 */
	public double getStep()
	{
		return step;
	}
	
	private void startCalc()
	{
		ArrayList<Long> vfs = getVisibleFunc();
		for (long fid : vfs)
		{
			FuncData func = map.get(fid);
			if (func != null)
			{
				Log.v("funcc", String.format("Found function %s at: %d" , 
						func.getExpr(), fid));
				func.Eval(domain.first, domain.second);
			}
			
		}		
	}	
	
	/**
	 * return the value at x of the function
	 * @param funcId the id of the function
	 * @param x
	 * @return
	 */
	public double getValue(long funcId, double x) throws Exception
	{
		try
		{
			return map.get(funcId).fx(x);
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
	
	/**
	 * Get those functions that are visible in the screen
	 * @return an array list of those function id
	 */
	public ArrayList<Long> getVisibleFunc()
	{
		ArrayList<Long> r = new ArrayList<Long>();
		Cursor c = db.query(TABLE_FUNCTIONS, new String[] {KEY_FUNCID}, 
				BOOL_VISIBLE + " > " + 0 + " AND " + INT_GRPID + " = " + this.active_group, 
				null, null, null, null);
		while(c.moveToNext()) {
			
			long id = c.getLong(c.getColumnIndexOrThrow(KEY_FUNCID));
			if (map.containsKey(id))
			{
				Log.i(TAG, String.format("Found one %d", id));
				r.add(id);
			}
		}
		
		return r;
	}
	
	/**
	 * set the function to visible or not
	 * @param id id of the function
	 * @param visibility
	 */
	public void setVisibility(int id, boolean visibility)
	{
		ContentValues v = new ContentValues();
		int vis = visibility ? 1 : 0;
		v.put(BOOL_VISIBLE, vis);
		db.update(TABLE_FUNCTIONS, v, KEY_FUNCID + " = " + id, null);
	}
	
	/**
	 * set the function's color 
	 * @param id id of the function
	 * @param color
	 */
	public void setColor(long id, int color)
	{
		ContentValues v = new ContentValues();
		v.put(INT_COLOR, color);
		db.update(TABLE_FUNCTIONS, v, KEY_FUNCID + " = " + id, null);
	}
	
	public int getColor(long id)
	{
		int color = Color.BLACK;
		ContentValues v = new ContentValues();
		Cursor c = db.query(TABLE_FUNCTIONS, new String[] {INT_COLOR}, 
				String.format("%s=%d", KEY_FUNCID, id), 
				null, null, null, null);
		while (c.moveToNext())
			color = c.getInt(c.getColumnIndexOrThrow(INT_COLOR));		
		return color;
	}
	
	public int getLineWidth(long id)
	{
		int w = 1;
		ContentValues v = new ContentValues();
		Cursor c = db.query(TABLE_FUNCTIONS, new String[] {INT_LINEWT}, 
				String.format("%s=%d", KEY_FUNCID, id), 
				null, null, null, null);
		while (c.moveToNext())
			w = c.getInt(c.getColumnIndexOrThrow(INT_LINEWT));		
		return w;
	}
	
	public int getLineType(long id)
	{
		int t = -1;
		ContentValues v = new ContentValues();
		Cursor c = db.query(TABLE_FUNCTIONS, new String[] {INT_LINETYPE}, 
				String.format("%s=%d", KEY_FUNCID, id), 
				null, null, null, null);
		while (c.moveToNext())
			t = c.getInt(c.getColumnIndexOrThrow(INT_LINETYPE));		
		return t;
	}
	
	/**
	 * set the function's expression 
	 * @param id id of the function
	 * @param expr
	 */
	public void setExpr(long id, String expr)
	{
		Log.v("funcc", String.format("New expr: %s for id=%d" , 
				expr, id));
		ContentValues v = new ContentValues();
		if (map.containsKey(id))
		{
			map.get(id).parseExpr(expr);
			Log.v("funcc", String.format("New expr 1: %s for id=%d" , 
					expr, id));
			this.startCalc();
		}
		v.put(STR_FUNC, expr);
		db.update(TABLE_FUNCTIONS, v, KEY_FUNCID + " = " + id, null);
		
	}	
	
	/**
	 * this is for screen slide and zoom
	 * @return the current domain of the screen
	 * 
	 */
	public Pair<Double, Double> getDomain() {		
		return domain;
	}

	/**
	 * this is for screen slide and zoom
	 * @param domain set the domain of the screen
	 */
	public void setDomain(Pair<Double, Double> domain) {
		this.domain = domain;
		startCalc();
		setStep();
	}
	
	/**
	 * this is for screen slide and zoom
	 * @return the current domain of the screen
	 * 
	 */
	public Pair<Double, Double> getRange() {
		return range;
	}

	/**
	 * this is for screen slide and zoom
	 * @param domain set the domain of the screen
	 */
	public void setRange(Pair<Double, Double> range) {
		this.range = range;
	}
	
	/**
	 * @return the finishEvalListener
	 */
	public OnFinishEvalListener getFinishEvalListener() {
		return finishEvalListener;
	}

	/**
	 * @param finishEvalListener the finishEvalListener to set
	 */
	public void setFinishEvalListener(OnFinishEvalListener finishEvalListener) {
		this.finishEvalListener = finishEvalListener;
	}
	
	/**
	 * @return the screenWidth
	 */
	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * @param screenWidth the screenWidth to set
	 */
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
		setStep();
	}
	
	/**
	 * @return the active_group
	 */
	public int getActive_group() {
		return active_group;
	}

	/**
	 * @param active_group the active_group to set
	 */
	public void setActive_group(int active_group) {
		this.active_group = active_group;
		
	}
}
