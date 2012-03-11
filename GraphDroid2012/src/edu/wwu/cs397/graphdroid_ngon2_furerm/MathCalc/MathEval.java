/**
 * 
 */
package edu.wwu.cs397.graphdroid_ngon2_furerm.MathCalc;

import java.util.List;

import org.matheclipse.core.eval.EvalUtilities;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.Parser;
import org.matheclipse.core.expression.F;

import android.util.Log;

/**
 * @author ngon2
 * Proxy class to eval 
 * Dont use new, use getInstant
 */
public class MathEval {
	private static MathEval ins = null;
	private EvalUtilities engine;
	private Parser p;
	
	public MathEval() 
	{
		engine = new EvalUtilities();
		F.initSymbols(null);
	}
	
	/**
	 * return the instance of the class. Recommended to use
	 * @return
	 */
	public static MathEval getInstance()
	{
		if (ins == null)
			ins = new MathEval();
		return ins;
	}
	
	/**
	 * Doing eval for simple expr, which mean no variable involved
	 * @param expr
	 * @return value
	 * @throws Exception 
	 */
	public String simpleEval(String expr) throws Exception
	{		
		try
		{
			IExpr r = engine.evaluate(expr);
			Log.v("simpleEval", "r.toString=" + r.toString() + "r.toScript=" + r.toScript());
			return r.toScript();			
		}
		catch (Exception e)
		{
			throw e;
		}		
	}
	
	/**
	 * takes in an expr with n variable and eval it
	 * it assume that number of variables name and value
	 * are equal 
	 * @param expr
	 * @param names: name of each variable
	 * @param values: value of each variable
	 * @return
	 */
	public String varibleEval(String expr, int count, List<String> names, List<Double> values)
		throws Exception
	{		
		for (int i = 0; i < count; i++)
		{
			try {
				engine.evaluate(names.get(i) + "=" + values.get(i));
			} catch (Exception e) {
				throw e;
			}					
		}
		try {
			IExpr r = engine.evaluate(expr);
			return r.toScript();
		} catch (Exception e) {

			throw e;
		}				
	}
}
