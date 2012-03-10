package edu.wwu.cs397.graphdroid_ngon2_furerm;

import edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe.FuncContainer;
import edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe.OnFinishEvalListener;

import java.util.ArrayList;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class TabletInterface extends Activity implements KeyboardContainer {
	private final Activity act = this;
	private static ArrayList<Integer> new_groups = new ArrayList<Integer>();
	private int num_groups = 0;
	private FuncContainer db;
	private EvalFragment flf = null;
	private EvalFragment hsf = null;
	private EvalFragment active_fragment = null;
	private KeyboardView keyboardView = null;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		db = FuncContainer.getIns(this);
		this.setContentView(R.layout.tablet_interface);
		hsf = (EvalFragment) this.getFragmentManager().findFragmentById(R.id.homescreen_fragment);
		active_fragment = hsf;
		createActionBar();
		
		// initialize the keypad
        keyboardView = (KeyboardView) findViewById(R.id.keyboardView);
        Keyboard keyboard = new Keyboard(this, R.xml.standard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(true);
        
        KeypadHandler handler = new KeypadHandler(this) {
        	public void eval()
        	{
        		active_fragment.eval();
        	}
        };
        keyboardView.setOnKeyListener(handler);
        keyboardView.setOnKeyboardActionListener(handler);
        KeypadHandler.activeEditText = (EditText) findViewById(R.id.home_cmd_entry);
        KeypadHandler.activeKeyboard = keyboardView;
        
        ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
		GraphCanvas cnv = (GraphCanvas)this.findViewById(R.id.cnv);
        cnv.setOnTouchListener(activitySwipeDetector);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        	    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        
        FuncContainer funcList = FuncContainer.getIns(this);
        funcList.setFinishEvalListener(new OnFinishEvalListener() {
			public void OnFinishEval(Object func, ArrayList<Double> domain) {
				// TODO Auto-generated method stub
				GraphCanvas cnv = (GraphCanvas)TabletInterface.this.findViewById(R.id.cnv);
				cnv.postInvalidate();
			}
        	
        });
	}
	public int getNextGroup() {
		if (new_groups.isEmpty())
		{
			return db.getLastGroup() + 1;
		} else {
			return new_groups.get(new_groups.size() - 1) + 1;
		}
	}
	public class GroupTabListener implements ActionBar.TabListener {
		private int grpid;
		public GroupTabListener(int grpid)
		{
			this.grpid = grpid;
		}
		public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// If we are showing the homescreen fragment:
			// display function_list_fragment, show graph screen
			if (flf == null) {
				flf = function_list_fragment.getInstance(grpid);
				act.findViewById(R.id.cnv).setVisibility(View.VISIBLE);
				act.findViewById(R.id.keyboardView).setVisibility(View.GONE);
				ft.hide(hsf);
				ft.add(R.id.fragment_container, flf, "function_list_fragment");
		        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		        active_fragment = flf;
		        FuncContainer funcList = FuncContainer.getIns(TabletInterface.this);
		        //bad
		        funcList.setScreenWidth(600);
		        
			} else {
				((function_list_fragment) flf).setGroup(grpid);
			}
		}
	}
	public class AddTabListener implements ActionBar.TabListener {
		private ActionBar ab;
		public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
		public AddTabListener(ActionBar ab) {
			this.ab = ab;
		}
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			int new_group = getNextGroup();
			new_groups.add(new_group);
			num_groups++;
		    Tab t = ab.newTab()
		            .setText("Group " + (num_groups - 1))
		            .setTabListener(new GroupTabListener(new_group));
		    ab.addTab(t, tab.getPosition(), true);
		    t.select();
		    ab.selectTab(t);
		}
	}
	public class HomeTabListener implements ActionBar.TabListener {
		public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (flf != null) {
				act.findViewById(R.id.cnv).setVisibility(View.GONE);
				act.findViewById(R.id.keyboardView).setVisibility(View.VISIBLE);
				ft.remove(flf);
				ft.show(hsf);
				flf = null;
				active_fragment = hsf;
			}
		}
	}
	public void createActionBar() {
	    
	    // setup action bar for tabs
	    final ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.removeAllTabs();
	    actionBar.setDisplayShowTitleEnabled(false);
	    
	    Tab tab = actionBar.newTab()
	    		.setText("Home")
	    		.setTabListener(new HomeTabListener());
	    actionBar.addTab(tab);
	    
	    final Integer[] grps = db.getAllGroups();
	    for (int i=0;i<grps.length;i++)
	    {
		    tab = actionBar.newTab()
		            .setText("Group " + i)
		            .setTabListener(new GroupTabListener(grps[i]));
		    actionBar.addTab(tab);
	    }
	    num_groups = grps.length;

	    tab = actionBar.newTab()
	    		.setText("Add group")
	    		.setTabListener(new AddTabListener(actionBar));
	    actionBar.addTab(tab);
	}
	public void showKeyboard() {
		keyboardView.setVisibility(View.VISIBLE);
	}
	public void hideKeyboard() {
		keyboardView.setVisibility(View.GONE);
	}

	/**
	 * Class for detect pitch zoom and scrolling	
	 * @author ngon2
	 *
	 */
	public class ActivitySwipeDetector implements View.OnTouchListener {
		private Activity activity;
		private int swipeMinDistance = 10;		

		private GestureDetector mGestureDetector;
		private ScaleGestureDetector mScaleDetector;
		
		
		public ActivitySwipeDetector(final Activity activity){
		    this.activity = activity;
		    			
		    mGestureDetector = new GestureDetector(
		    	new GestureDetector.SimpleOnGestureListener() {
		    		Pair<Double, Double> lastDomain;
				    Pair<Double, Double> lastRange;
				    
	    		@Override
	            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	    			
	    			float deltaX = e2.getX() - e1.getX();
	    			float deltaY = e2.getY() - e1.getY();
	    			
	    			if (Math.abs(deltaX) > 10)
	    			{
	    				
		                // i'm only scrolling along the X axis
		    			FuncContainer func = FuncContainer.getIns(activity);
		    			Pair<Double, Double> d = lastDomain;
		    			
		    			GraphCanvas cnv = (GraphCanvas)activity.findViewById(R.id.cnv);
		    			
		    			
		    			double valuePerDp = Math.abs(d.first - d.second) / (double)cnv.getWidth();
		    			double domainSwitch = valuePerDp * - deltaX;
		    			
		    			func.setDomain(new Pair<Double, Double>
		    							(d.first + domainSwitch, d.second + domainSwitch));
		    			
		    			cnv.postInvalidate();
		    			
	    			}
	    			if (Math.abs(deltaY) > 10)
	    			{
	    				FuncContainer func = FuncContainer.getIns(activity);
		    			Pair<Double, Double> r = lastRange;
		    			
		    			GraphCanvas cnv = (GraphCanvas)activity.findViewById(R.id.cnv);
		    			
		    			
		    			double valuePerDp = Math.abs(r.first - r.second) / (double)cnv.getHeight();
		    			double rangeSwitch = valuePerDp * deltaY;			    			
		    			
		    			func.setRange(new Pair<Double, Double>
		    							(r.first + rangeSwitch, r.second + rangeSwitch));
		    			
		    			cnv.postInvalidate();
	    			}	    				
	                return true;
	            }
	    		
	    		public boolean onDown(MotionEvent e) {
	    			FuncContainer func = FuncContainer.getIns(activity);
	    			lastDomain = func.getDomain();
	    			
	    			lastRange = func.getRange();
	                return true;
	            }
		    });

		    mScaleDetector = new ScaleGestureDetector(activity, new ScaleListener());
		}
		
		private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
			Pair<Double, Double> savedD;
			Pair<Double, Double> savedR;
			float mScaleFactorX = 1.f;
			float mScaleFactorY = 1.f;
			
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector)
			{
				FuncContainer func = FuncContainer.getIns(activity);
				mScaleFactorX = 1.f;
				mScaleFactorY = 1.f;
				savedD = func.getDomain();
				savedR = func.getRange();
				return true;
			}
			
		    @Override
		    public boolean onScale(ScaleGestureDetector detector) {
		    	
		    	
		        mScaleFactorX *= detector.getCurrentSpanX()/detector.getPreviousSpanX();
		        mScaleFactorY *= detector.getCurrentSpanY()/detector.getPreviousSpanY();
		        
		        
		        // Don't let the object get too small or too large.
		        mScaleFactorX = Math.max(0.1f, Math.min(mScaleFactorX, 5.0f));
		        mScaleFactorY = Math.max(0.1f, Math.min(mScaleFactorY, 5.0f));
		        
		        
		        
		        GraphCanvas cnv = (GraphCanvas)activity.findViewById(R.id.cnv);
				FuncContainer func = FuncContainer.getIns(activity);
				
				if (mScaleFactorX != 0)
				{
					Pair<Double, Double> newD = new Pair<Double, Double>
								(savedD.first * mScaleFactorX, savedD.second * mScaleFactorX);
					func.setDomain(newD);
				}
				
				if (mScaleFactorY != 0)
				{
					Pair<Double, Double> newR = new Pair<Double, Double>
								(savedR.first * mScaleFactorY, savedR.second * mScaleFactorY);
					func.setRange(newR);
				}
				
				cnv.postInvalidate();
		        return true;
		    }
		}

		public boolean onTouch(View arg0, MotionEvent event) {
			
			if (mScaleDetector.onTouchEvent(event))
			{
				if (mScaleDetector.isInProgress())
				{					
					return true;
				}
			}
			
            if (mGestureDetector.onTouchEvent(event)) {
                return true;
            }
            return false;
		}
	}

	public void removeSelectedGroup() {
		ActionBar ab = getActionBar();
		Tab sel = ab.getSelectedTab();
		int sel_i = ab.getSelectedNavigationIndex();
		ab.setSelectedNavigationItem(sel_i - 1);
		ab.removeTab(sel);
	}

}
