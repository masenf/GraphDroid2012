package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.HashMap;

import android.app.Fragment;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

public class KeypadFragment extends Fragment implements OnKeyboardActionListener, OnKeyListener {
	private static final String TAG = "KeypadFragment";
	private static HashMap<String, String> codeMap = null;
	private EditText activeEditText = null;
	private KeyboardView tabBar = null;
	private KeyboardView specialKeys = null;
	private KeyboardView numberKeys = null;
	private int activeLayout = R.xml.standard;
	private Context ctx;
	protected void eval() {
		// this method gets called when 'enter' is pressed
		if (getActivity() instanceof IKeyboardContainer)
		{
			((IKeyboardContainer) getActivity()).eval();
		}
	}
	public void setActiveEditText(EditText ed)
	{
		activeEditText = ed;
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// build the codes
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
	        
	        codeMap.put("200", "a");
	        codeMap.put("201", "b");
	        codeMap.put("202", "c");
	        codeMap.put("203", "d");
	        codeMap.put("204", "h");
	        codeMap.put("205", "i");
	        codeMap.put("206", "j");
	        codeMap.put("207", "k");
	        codeMap.put("208", "m");
	        codeMap.put("209", "n");
	        codeMap.put("210", "o");
	        codeMap.put("211", "p");
		}
	   
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		this.ctx = getActivity();
		
        Keyboard keyboardTB = new Keyboard(ctx, R.xml.tabs);
        tabBar.setKeyboard(keyboardTB);
        Keyboard keyboardSK = new Keyboard(ctx, R.xml.standard);
        specialKeys.setKeyboard(keyboardSK);
        Keyboard keyboardNK = new Keyboard(ctx, R.xml.numbers_operators);
        numberKeys.setKeyboard(keyboardNK);
        //keyboardView.setEnabled(true);
        //keyboardView.setPreviewEnabled(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.keypad_fragment, null);
		
		// initialize the keypad
		tabBar = (KeyboardView) view.findViewById(R.id.keyboard_tabBar);
		specialKeys = (KeyboardView) view.findViewById(R.id.keyboard_specialKeys);
		numberKeys = (KeyboardView) view.findViewById(R.id.keyboard_numberKeys);
        tabBar.setOnKeyListener(this);
        tabBar.setOnKeyboardActionListener(this);
        specialKeys.setOnKeyListener(this);
        specialKeys.setOnKeyboardActionListener(this);
        numberKeys.setOnKeyListener(this);
        numberKeys.setOnKeyboardActionListener(this);
        
        return view;
	}
	public void onViewCreated(View view, Bundle savedInstanceState) {
	}
	public boolean onKey(View v, int keycode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.v(TAG,"Inside onKey");
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
				specialKeys.setKeyboard(new Keyboard(this.ctx, R.xml.standard));
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
			changeSpecialKeys(R.xml.standard);
	        break;
		case -20:	// standard 2nd
			changeSpecialKeys(R.xml.standard_2nd);
	        break;
		case -21:	// trig
			changeSpecialKeys(R.xml.trig);
			break;
		case -22:	// calculus
			changeSpecialKeys(R.xml.calc);
			break;
		case -23:	// exp
			changeSpecialKeys(R.xml.exp);
			break;
		case -24:  // alpha
			changeSpecialKeys(R.xml.alpha);
			break;
		}
	}
	public void changeSpecialKeys(int keypad)
	{
		// TODO: add animation
		int newKeypad;
		if (keypad == activeLayout)
		{
			// change back to standard
			newKeypad = R.xml.standard;
		} else 
			newKeypad = keypad;
		specialKeys.setKeyboard(new Keyboard(ctx, newKeypad));
		activeLayout = newKeypad;
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