package edu.wwu.cs397.graphdroid_ngon2_furerm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.matheclipse.core.eval.EvalUtilities;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.form.output.StringBufferWriter;
import org.matheclipse.core.interfaces.IExpr;

public class main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        F.initSymbols(null);
        EvalUtilities util = new EvalUtilities();

        IExpr result;
        try {
	        StringBufferWriter buf = new StringBufferWriter();
	        String input = "Expand[(AX^2+BX)^2]";
	        result = util.evaluate(input);
	        OutputFormFactory.get().convert(buf, result);
	        String output = buf.toString();
	        ((TextView) this.findViewById(R.id.main_text)).setText("Expanded form for " + input + " is " + output);
        }
	    catch (final Exception e) { 
	        ((TextView) this.findViewById(R.id.main_text)).setText("");
	    } finally { }
        
        Button btn_showGraph = (Button)this.findViewById(R.id.btn_showGraph);
        btn_showGraph.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent_graph = new Intent(main.this, graph_page.class);
				startActivity(intent_graph);
			}
		});
        Button btn_showKeypadTest = (Button)this.findViewById(R.id.btn_showKeypadTest);
        btn_showKeypadTest.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(main.this, TabletInterface.class);
				startActivity(i);
			}
		});

    }
}