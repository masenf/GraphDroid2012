package edu.wwu.cs397.graphdroid_ngon2_furerm.MathCalc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import org.matheclipse.core.convert.AST2Expr;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.Parser;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FloatNode;
import org.matheclipse.parser.client.ast.FractionNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.ast.IntegerNode;
import org.matheclipse.parser.client.ast.StringNode;


public class MathParser 
{
	private static MathParser ins = null;
	
	private MathParser() { }
	private List<String> ASTNodeResult = new ArrayList<String>();
	
	public static MathParser getInstance() 
	{
		if (ins == null)
			ins = new MathParser();
		return ins;
	}
	
	public IExpr Parse(String expr)
	{
		Parser p = new Parser();
		ASTNode root = p.parse(expr);
		AST2Expr converter = new AST2Expr(null, null);
		IExpr result = converter.convert(root);	
		return result;
	}
	
	public ASTNode ParseToAST(String expr)
	{
		Parser p = new Parser();
		return p.parse(expr);
	}
	
	/*
	 * Travel across the node and return the node
	 * 
	 */
	public List<String> travel(ASTNode node)
	{
		ASTNodeResult = new ArrayList<String>();
		node_travel(node,"");
		return ASTNodeResult;
	}
	
	private void node_travel(ASTNode node, String indent)
	{
		if (node == null)
		{
			return;			
		}
		else if (node instanceof FunctionNode)
		{
			FunctionNode func = (FunctionNode) node;
			ASTNodeResult.add(indent+func.get(0).toString()); // this is the head of the function, like Times[,] Cos[,], Sin[,]....
			for (int i = 1; i < func.size();i++) // now travel thru these function's argu
			{
				ASTNode child = func.get(i);
				node_travel(child, indent+"      ");				
			}		
		}
		else if (node instanceof FractionNode)
		{
			FractionNode frac = (FractionNode) node;
			ASTNodeResult.add(indent+"Rational");
			// fetch the components
			node_travel(frac.getNumerator(),indent+"      ");
			node_travel(frac.getDenominator(), indent+"      ");
		}
		else
		{
			ASTNodeResult.add(indent+node.toString());
		}
		
	
	}
}
