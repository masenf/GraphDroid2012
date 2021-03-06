package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.ast.ASTNode;

import edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe.*;;

public class graph_page extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_page);
        
        GraphCanvas cnv = (GraphCanvas)this.findViewById(R.id.cnv);    
        Log.w("sw", String.format("%d", cnv.getWidth()));
        
        
        Button btn_draw = (Button)this.findViewById(R.id.btn_draw);
        btn_draw.setOnClickListener(new View.OnClickListener() {				
			public void onClick(View v) {
				handleDrawBtnClick();			
			}
		});
        
        //Button moveDomain = (Button)this.findViewById(R.id.btn_moveDomain);
        //moveDomain.setOnClickListener(new View.OnClickListener() {				
		//	public void onClick(View v) {
		//		
		//	}
		//});
        
        
        ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);

        cnv.setOnTouchListener(activitySwipeDetector);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        	    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
	}
	
	private void handleDrawBtnClick()
	{
		GraphCanvas cnv = (GraphCanvas)this.findViewById(R.id.cnv);
		FuncContainer funcList = FuncContainer.getIns(this);
		
		funcList.setScreenWidth(cnv.getWidth());
		funcList.add("x+2", Color.BLUE,	1, -1, 0, 0, true);
		funcList.add("x^2+2", Color.RED,	1, -1, 0, 0, true);
		funcList.add("Sin[x^2]+2", Color.GREEN,	1, -1, 0, 0, true);
		//funcList.add("Integrate[Sqrt[1+x^2],x]+2", Color.BLACK,	1, -1, 0, 0, true);
	}	
	
	private void handleFinishEval()
	{
		
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
	    			Log.d("touch", String.format("Sw = %d, ", func.getScreenWidth()));
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
		        
		        Log.v("touch", String.format("X = %f, Y = %f", mScaleFactorX, mScaleFactorY));
		        
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

}
