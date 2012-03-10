package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.HashMap;

import edu.wwu.cs397.graphdroid_ngon2_furerm.homescreen_history_adapter.history_element;


import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class KeypadHandler implements OnKeyboardActionListener, OnKeyListener {
	private static final String TAG = "KeypadHandler";
	private static HashMap<String, String> codeMap = null;
	public static EditText activeEditText = null;
	public static KeyboardView activeKeyboard = null;
	public static int activeLayout = R.xml.standard;
	private Context ctx;
	protected void eval() {
		// this method gets called when 'enter' is pressed
	}
	private void backspace() {
		if(activeEditText.getText().toString().length() > 0)
		{
			int curSelectionStart = activeEditText.getSelectionStart();
			int curSelectionLength = activeEditText.getSelectionEnd() - curSelectionStart;
			String curText = activeEditText.getText().toString();
			Log.v(TAG,"curSelectionStart = " + Integer.toString(curSelectionStart));
			if (curSelectionLength > 0)
			{
				activeEditText.getText().replace(Math.min(curSelectionStart,activeEditText.getSelectionEnd()), Math.max(curSelectionStart,activeEditText.getSelectionEnd()), "");
				activeEditText.setSelection(curSelectionStart);
				//newText = curText.substring(0,curSelectionStart) + curText.substring(activeEditText.getSelectionEnd(), curText.length());
			} else if (curSelectionStart > 0) {
				String newText = curText.substring(0,curSelectionStart - 1);
				if (curSelectionStart < curText.length()) 
					newText = newText + curText.substring(curSelectionStart, curText.length());
				activeEditText.setText(newText);
				activeEditText.setSelection(curSelectionStart - 1);
			}
		}
	}
	public KeypadHandler(Context ctx) {
		this.ctx = ctx;
		if (codeMap == null) {
			Log.v(TAG,"Building internal code map...");
			codeMap = new HashMap<String, String>();
			   
	    	codeMap.put("50","&lt;");
	        codeMap.put("51","&gt;");
	   
	   
	        codeMap.put("54","x");
	        codeMap.put("55","y");
	        codeMap.put("56","z");
	        codeMap.put("57",";");
	        codeMap.put("58","-&gt;");
	   
	   
	        codeMap.put("59","=");
	        codeMap.put("60","[");
	        codeMap.put("61","]");
	        codeMap.put("62",",");
	        codeMap.put("63","^");
	   
	        codeMap.put("64","7");
	        codeMap.put("65","8");
	        codeMap.put("66","9");
	        codeMap.put("67","/");
	   
	   
	        codeMap.put("68","4");
	        codeMap.put("69","5");
	        codeMap.put("70","6");
	        codeMap.put("71","*");
	   
	   
	        codeMap.put("72","1");
	        codeMap.put("73","2");
	        codeMap.put("74","3");
	        codeMap.put("75","-");
	   
	   
	        codeMap.put("76","0");
	        codeMap.put("77",".");
	        codeMap.put("78","-");
	        codeMap.put("79","+");
	        
	        codeMap.put("80","{");
	        codeMap.put("81","}");
	        codeMap.put("82","(");
	        codeMap.put("83",")");
	        
	        codeMap.put("100", "Sin[");
	        codeMap.put("101", "Cos[");
	        codeMap.put("102", "Tan[");
	        codeMap.put("103", "ArcSin[");
	        codeMap.put("104", "ArcCos[");
	        codeMap.put("105", "ArcTan[");
	        codeMap.put("106", "Pi");
	        codeMap.put("107", "Theta");
	        codeMap.put("108", "Degree");
	        
	        codeMap.put("120", "Integrate[");
	        codeMap.put("121", "NIntegrate[");
	        codeMap.put("122", "D[");
	        codeMap.put("123", "Sum[");
	        codeMap.put("124", "Product[");
	        
	        codeMap.put("140", "EE");
	        codeMap.put("141", "^2");
	        codeMap.put("142", "^3");
	        codeMap.put("143", "^4");
	        codeMap.put("144", "Log[");
	        codeMap.put("145", "Log[");
	        codeMap.put("146", "E");
	        codeMap.put("147", "Sqrt[");
		}
	   
	}
	
	public boolean onKey(View v, int keycode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onKey(int keycode, int[] other_codes) {
		// TODO Auto-generated method stub
		if (codeMap != null && activeEditText != null)
		{
			String c = codeMap.get(String.valueOf(keycode));
			int[] ops = new int[] {63,71,75,78,79,141,142,143};
			Log.v(TAG, "Got keypress, keycode="+Integer.toString(keycode)+" value="+c);
			if (activeLayout != R.xml.standard) 		// reset the keyboard back to standard
			{
				activeKeyboard.setKeyboard(new Keyboard(this.ctx, R.xml.standard));
			}
			
			// auto append history to command entry when the user strikes
			// an operator key
			for (int i=0;i<ops.length;i++)
			{
				if ((ops[i] == keycode) && (activeEditText.getText().length() == 0)) {
					c = "Ans[0]" + c;
				}
			}
			if(!(c == null)) {
				int Start = activeEditText.getSelectionStart();
				int End = activeEditText.getSelectionEnd();
				activeEditText.getText().replace(Math.min(Start,End), Math.max(Start,End), c);
			} else{
				specialKeys(keycode);
			}
		}
	}
	private void specialKeys(int keycode)
	{
		switch(keycode)
		{
		case -5:
			backspace();
			break;
		case -6:	// move cursor one to left
			if (activeEditText.getSelectionStart() > 0)
				activeEditText.setSelection(activeEditText.getSelectionStart() - 1);
			break;
		case -7:	// move cursor one to right
			if (activeEditText.getSelectionEnd() < activeEditText.getText().toString().length())
				activeEditText.setSelection(activeEditText.getSelectionEnd() + 1);
			break;
        case -10:	// clear
        	activeEditText.setText("");
        	break;
		case -11:	// eval button
			eval();
			break;
		case -19:	// keypad back to standard
	        activeKeyboard.setKeyboard(new Keyboard(this.ctx, R.xml.standard));
	        activeLayout = R.xml.standard;
	        break;
		case -20:	// standard 2nd
	        activeKeyboard.setKeyboard(new Keyboard(this.ctx, R.xml.standard_2nd));
	        activeLayout = R.xml.standard_2nd;
	        break;
		case -21:	// trig
			activeKeyboard.setKeyboard(new Keyboard(this.ctx, R.xml.trig));
			activeLayout = R.xml.trig;
			break;
		case -22:	// calculus
			activeKeyboard.setKeyboard(new Keyboard(this.ctx, R.xml.calc));
			activeLayout = R.xml.calc;
			break;
		case -23:	// exp
			activeKeyboard.setKeyboard(new Keyboard(this.ctx, R.xml.exp));
			activeLayout = R.xml.exp;
			break;
		}
	}
	public void onPress(int arg0) {
		// TODO Auto-generated method stub

	}

	public void onRelease(int arg0) {
		// TODO Auto-generated method stub

	}

	public void onText(CharSequence arg0) {
		// TODO Auto-generated method stub

	}

	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	public void swipeUp() {
		// TODO Auto-generated method stub

	}

}
