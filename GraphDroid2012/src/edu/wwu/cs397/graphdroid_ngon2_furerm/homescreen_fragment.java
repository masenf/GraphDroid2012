package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.matheclipse.parser.client.ast.ASTNode;

import edu.wwu.cs397.graphdroid_ngon2_furerm.homescreen_history_adapter.display_type;
import edu.wwu.cs397.graphdroid_ngon2_furerm.homescreen_history_adapter.el_type;
import edu.wwu.cs397.graphdroid_ngon2_furerm.homescreen_history_adapter.history_element;
import edu.wwu.cs397.graphdroid_ngon2_furerm.MathCalc.MathEval;
import edu.wwu.cs397.graphdroid_ngon2_furerm.MathCalc.MathParser;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class homescreen_fragment extends EvalFragment 
		implements OnItemClickListener, OnItemLongClickListener {
	private static final String TAG = "homescreen_fragment";
	private static EditText ed = null;
	private static homescreen_history_adapter list_adapter = null;
	private static ListView list_view = null;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        //super.onCreateView(savedInstanceState);
		View main = inflater.inflate(R.layout.homescreen, container);
		ed = (EditText) main.findViewById(R.id.home_cmd_entry);

        return main;
	}
	 @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Populate list with our static array of titles.
        list_adapter = new homescreen_history_adapter(this.getActivity());
        setListAdapter(list_adapter);
        list_view = getListView();
        list_view.setOnItemClickListener(this);
        list_view.setOnItemLongClickListener(this);
        
        OnTouchListener otl = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
         };

		
        // hide the soft keyboard
		EditText ed = (EditText) getView().findViewById(R.id.home_cmd_entry);
		ed.setOnTouchListener(otl);
		InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ed.getWindowToken(),0);
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//imm.showSoftInput(ed, InputMethodManager.HIDE_NOT_ALWAYS);
    }
	 @Override
	public void eval() 
	{	
		String input_raw = ed.getText().toString();
		String input_sub = "";
		String output_raw = "";
		ASTNode input_parsed = null;
		ASTNode output_parsed = null;
		if (input_raw.isEmpty())
		{
			// if the user doesn't enter anything, eval the last entry again
			if (list_adapter.getCount() > 1)
			{
				input_raw = ((history_element) list_adapter.getItem(list_adapter.getCount() - 2)).raw;
			}
		}
		Log.v(TAG,"In eval() for " + input_raw);
		MathParser parser = MathParser.getInstance();
		MathEval eval = MathEval.getInstance();
		try
		{
			input_sub = specialSubstitutions(input_raw);
			input_parsed = parser.ParseToAST(input_raw);
			output_raw = eval.simpleEval(input_sub);
			output_parsed = parser.ParseToAST(output_raw);
		}
		catch (Exception ex)
		{
			//output_raw = "Invaild Expr: " + ex.getMessage() + " " + ex.toString();
			StackTraceElement[] st = ex.getStackTrace();
			Log.v(TAG,"STACK TRACE: " + ex.toString());
			for (int i=0;i<st.length;i++)
			{
				Log.v(TAG,st[i].getFileName() + ":" + st[i].getLineNumber() + 
						" in method " + st[i].getMethodName());
			}
			Log.v(TAG,"input_raw = " + input_raw);
			Log.v(TAG,"input_parsed = " + input_parsed);
			Log.v(TAG,"output_raw = " + output_raw);
			Log.v(TAG,"output_parsed = " + output_parsed);
		}
		
		// add the data to the adapter
		list_adapter.addItem(el_type.input, input_raw, input_parsed);
		int lastpos = list_adapter.addItem(el_type.output, output_raw, output_parsed);
		// clear the entry box
		ed.setText("");
		// scroll down
		list_view.setSelectionFromTop(lastpos - 1, 0);
		list_view.smoothScrollToPositionFromTop(lastpos, 0);
		//getListView().smoothScrollToPosition(lastpos);
	}

	private String specialSubstitutions(String input_raw) throws Exception {
		// do a history replace for Ans[] function
		Pattern p = Pattern.compile("Ans\\[([0-9]+)\\]");
		Matcher m = p.matcher(input_raw);
		String output = input_raw;
		String repl;
		int pos;
		while (m.find()) {
			try {
				pos = list_adapter.getCount() - 1 - Integer.parseInt(m.group(1));
			} catch (Exception ex) {
				 throw new Exception( "Syntax Error in " + m.group() );
			}
			if ((list_adapter.getCount() - 1) < pos)
				throw new Exception( "Error '" + pos + "' not in history" );
			else
				repl = ((history_element) list_adapter.getItem(pos)).raw;
			output = m.replaceFirst(repl);
			m = p.matcher(output);
		}
		Log.v(TAG, "After substitutions, the input is: " + output);
		return output;
	}
	public void onItemClick(AdapterView<?> lv, View target, int pos, long id) {
		// TODO Auto-generated method stub
		EditText ed = ((EditText) this.getView().findViewById(R.id.home_cmd_entry));
		int Start = ed.getSelectionStart();
		int End = ed.getSelectionEnd();
		String txt = ((history_element) lv.getAdapter().getItem(pos)).raw;
		ed.getText().replace(Math.min(Start,End), Math.max(Start,End), txt);
		//ed.setText(((history_element) lv.getAdapter().getItem(pos)).raw);
		ed.setSelection(ed.length());
	}
	public boolean onItemLongClick(AdapterView<?> lv, View target, int pos, long id) {
		history_element h = ((history_element) lv.getAdapter().getItem(pos));
		if (h.display == display_type.DISPLAY_PARSED)
			h.display = display_type.DISPLAY_RAW;
		else {
			if (h.parsed != null) {
				h.display = display_type.DISPLAY_PARSED;
			}
		}
		((homescreen_history_adapter) lv.getAdapter()).notifyDataSetChanged();
		return true;
	}
}
