package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.List;

import org.matheclipse.parser.client.ast.ASTNode;

import edu.wwu.cs397.graphdroid_ngon2_furerm.MathCalc.MathParser;
//import edu.wwu.cs397.graphdroid_ngon2_furerm.PrettyPrint.PrettyPrint;
import edu.wwu.cs397.graphdroid_ngon2_furerm.PrettyPrint.TypeSetter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class keypad_test extends Activity {

	private static final String TAG = "keypad_test";
	@Override
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.keypad_test);
	        KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboardView);
	        Keyboard keyboard = new Keyboard(this, R.xml.standard);
	        keyboardView.setKeyboard(keyboard);
	        keyboardView.setEnabled(true);
	        keyboardView.setPreviewEnabled(true);
	        KeypadHandler handler = new KeypadHandler(this) {
	        	public void eval()
	        	{
	        		expr_eval();
	        	}
	        };
	        keyboardView.setOnKeyListener(handler);
	        keyboardView.setOnKeyboardActionListener(handler);
	        KeypadHandler.activeEditText = (EditText) findViewById(R.id.keypad_test_edit);
	        KeypadHandler.activeKeyboard = keyboardView;
	}
	public void expr_eval()
	{
		EditText ed = (EditText) findViewById(R.id.keypad_test_edit);
		MathParser parser = MathParser.getInstance();
		String start_expr = ed.getText().toString();
		ASTNode node = parser.ParseToAST(start_expr);
		ImageView iv = ((ImageView) findViewById(R.id.keypad_iv));
		if (iv != null)
		{
			Log.v(TAG,"Instantiating a TypeSetter width=" + iv.getWidth() + " height=" + iv.getHeight());
			Bitmap output = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
			
			TypeSetter t = new TypeSetter(node,output);
			iv.setImageBitmap(output);
		} else {
			Log.v(TAG,"Could not find the linear layout...RIP");
		}
	}

}
