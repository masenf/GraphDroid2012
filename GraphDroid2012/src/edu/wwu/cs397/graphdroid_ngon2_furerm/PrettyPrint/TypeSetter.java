package edu.wwu.cs397.graphdroid_ngon2_furerm.PrettyPrint;

import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FractionNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.ast.IntegerNode;
import org.matheclipse.parser.client.ast.StringNode;
import org.matheclipse.parser.client.ast.SymbolNode;
import org.matheclipse.parser.client.ast.NumberNode;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

public class TypeSetter {
	// the typesetter class takes care of performing the typesetting of 
	// an ASTnode. It maintains internal state among class methods to 
	// reduce the calling parameters required otherwise
	
	private static final String TAG = "TypeSetter";
	
	private static final String BRACKET_LEFT = "[";
	private static final String BRACKET_RIGHT = "]";

	private Bitmap b;
	private Canvas c;
	private float xpos = 0;			// the current xpos of the output
	private float ypos = 0;			// the current ypos of the output
	
	private Paint p;
	
	public TypeSetter(ASTNode node, Bitmap b)
	{
		this.b = b;
		this.c = new Canvas(this.b);
		this.p = new Paint();
		c.drawARGB(0,255,255,255);

		ypos = 5 * (float)b.getHeight() / 6;
		p.setTextSize(2*(float)b.getHeight()/3);
		// p.setTextSkewX((float) -0.25);
		p.setColor(Color.rgb(240, 240, 240));
		p.setAntiAlias(true);

		Log.v(TAG,"Beginning Typesetting...");
		typeset(node);
		Log.v(TAG,"Finished Typesetting...Trimming bitmap");
		this.b = Bitmap.createBitmap(this.b, 0, 0, (int)xpos, b.getHeight());
	}
	public Bitmap getBitmap() {
		return b;
	}

	private void typeset(ASTNode node) {
		// private int typeset(...)
		// draw the typeset version of <node>
		if (node == null)
		{
			return;			
		}
		else if (node instanceof FunctionNode)
		{
			FunctionNode func = (FunctionNode) node;
			typeFunction(func);

		}
		else if (node instanceof FractionNode)
		{
			FractionNode frac = (FractionNode) node;
			typeFraction(frac.getNumerator(), frac.getDenominator());
		}
		else
		{
			typeText(node);
		}
	}

	// these functions generate views for a given node type
	private void typeIntegrate(FunctionNode node)
	{
		float o_textSize = p.getTextSize();
		float o_ypos = ypos;
		p.setTextSize(o_textSize*(float)1.2);
		ypos -= 3;
		typeText("º");
		ypos = o_ypos;
		p.setTextSize(o_textSize);
		FunctionNode func = new FunctionNode(new SymbolNode("Times"));
		func.add(node.get(1));
		func.add(new StringNode("d" + node.get(2).toString()));
		typeset(func);
	}
	private void typePower(ASTNode base, ASTNode exponent)
	{
		Log.v(TAG,"Generating exponent view");
		float o_textSize = p.getTextSize();
		float o_ypos = ypos;
		
		if (base instanceof FunctionNode)
		{
			typeText("(");
			typeset(base);
			typeText(")");
		} else 
			typeset(base);
		if (!(exponent.equals(IntegerNode.C1)))
		{
			ypos -= (o_textSize / 2);
			p.setTextSize(o_textSize / 2);		// half the original text size
			typeset(exponent);
			ypos = o_ypos;
			p.setTextSize(o_textSize);
		}
	}
	private void typeSqrt(FunctionNode node)
	{
		// determine the dimensions of the radical sign
		float o_xpos = xpos;
		float rad_height = (float) 1.1 * p.getTextSize();
		float rad_head_width = rad_height/2;
		p.setStrokeWidth(3);
		
		// draw the head of the radical sign
		c.drawLine(xpos, ypos - rad_height / 3, xpos + rad_head_width / 6, ypos - rad_height / 3, p);
		xpos += rad_head_width / 6;
		c.drawLine(xpos, ypos - rad_height / 3, xpos+rad_head_width / 6, ypos, p);
		xpos += rad_head_width / 6;
		c.drawLine(xpos, ypos, o_xpos + rad_head_width, ypos-rad_height, p);
		xpos = o_xpos + rad_head_width;
		o_xpos = xpos;
		
		// typeset the expression
		typeset(node.get(1));
		
		// draw the top bar
		c.drawLine(xpos, ypos-rad_height, o_xpos, ypos-rad_height, p);
		
		p.setStrokeWidth(0);
	}
	private void typePlus(FunctionNode node)
	{
		int i = 1;
		while(i < node.size())
		{
			ASTNode child1 = node.get(i);
			if (i > 1)
				typeText("+");
			typeset(child1);
			if (i + 1 < node.size())
			{
				ASTNode child2_node = node.get(i+1);
				if (child2_node instanceof FunctionNode) {
					FunctionNode child2_func = (FunctionNode) child2_node;
					if (child2_func.get(0).toString() == "Times")
					{
						if (child2_func.get(1) instanceof IntegerNode)
						{
							if (((IntegerNode) child2_func.get(1)).getIntValue() < 0)
							{
								IntegerNode new_val = new IntegerNode(new Integer(((IntegerNode) child2_func.get(1)).getIntValue() * (-1)).toString());
								child2_func = new FunctionNode(new SymbolNode("Times"));
								child2_func.add(new_val);
								for (int j=2;j<((FunctionNode) child2_node).size();j++)
									child2_func.add(((FunctionNode) child2_node).get(j));
								typeText("-");
								typeset(child2_func);
								i+=2;
								continue;
							}
						}
					}
				}

			}
			i++;
		}		
	}
	private void typeTimes(FunctionNode node)
	{
		int i = 1;
		boolean printed = false;
		while(i < node.size())
		{
			ASTNode child1 = node.get(i);
			Log.v(TAG,"child1 nodetype is " + child1.getClass());
			if (child1 instanceof IntegerNode)
			{
				Log.v(TAG,"child1 value is " + Integer.toString(((IntegerNode) child1).getIntValue()).toString());
				Log.v(TAG,"also known as " + child1.toString());
				if (child1.equals(IntegerNode.C1))
				{
					i++;
					continue;
				}
			}
			if (printed)
				typeText("á");
			if (i + 1 < node.size())
			{
				ASTNode child2_node = node.get(i+1);
				if (child2_node instanceof FunctionNode) {
					FunctionNode child2_func = (FunctionNode) child2_node;
					if (child2_func.get(0).toString() == "Power")
					{
						if (child2_func.get(2) instanceof IntegerNode)
						{
							if (((IntegerNode) child2_func.get(2)).getIntValue() < 0)
							{
								IntegerNode new_exp = new IntegerNode(new Integer(((IntegerNode) child2_func.get(2)).getIntValue() * (-1)).toString());
								child2_node = new FunctionNode(new SymbolNode("Power"),child2_func.get(1),new_exp);
								typeFraction(child1,child2_node);
								i += 2;
								continue;
							}
						}
					}
				} else if (child2_node instanceof FractionNode) {
					FractionNode child2_frac = (FractionNode) child2_node;
					typeFraction(new FunctionNode(new SymbolNode("Times"),child1,child2_frac.getNumerator()), child2_frac.getDenominator());
					i += 2;
					continue;
				}
			}
			typeset(child1);
			printed = true;
			i++;
		}
	}
	private void typeFraction(ASTNode num, ASTNode den)
	{
		Log.v(TAG,"Generating fraction view");
		float o_textSize = p.getTextSize();
		float o_ypos = ypos;
		float o_xpos = xpos;
		float width = 0;
		float num_width;
		float den_width;
		Paint o_p = p;
		Paint alpha_fill = new Paint(p);
		alpha_fill.setARGB(0, 0, 0, 0);
		alpha_fill.setTextSize(o_textSize / 2);
		
		// measurement phase
		p = alpha_fill;
		// print the denominator
		typeset(den);
		den_width = xpos - o_xpos;
		xpos = o_xpos;			// reset the position
		ypos -= (p.getTextSize() + 5);
		// print the numerator
		typeset(num);
		num_width = xpos - o_xpos;
		if (den_width > num_width)
		{
			width = den_width;
		} else {
			width = num_width;
		}
		
		// draw phase
		p = o_p;									// reset the paint
		p.setTextSize(o_textSize / 2);				// half the original text size
		// print the denominator
		xpos = o_xpos + (width - den_width) / 2;
		ypos = o_ypos;
		typeset(den);
		
		// print the numerator
		xpos = o_xpos + (width - num_width) / 2;	// reset the position
		ypos = o_ypos - (p.getTextSize() + 5);
		typeset(num);
		
		// draw the line
		ypos += 5;
		c.drawLine(o_xpos, ypos, o_xpos + width, ypos, p);
		
		// restore the original state
		xpos = o_xpos + width;
		ypos = o_ypos;
		p.setTextSize(o_textSize);
	}
	private void typeFunction(FunctionNode node)
	{

		// specialized functions
		String funcName = node.get(0).toString();
		Log.v(TAG,"Generating function view for \"" + funcName + "\"");
		if (funcName.equals("Power"))
			typePower(node.get(1), node.get(2));
		else if (funcName.equals("Times"))
			typeTimes(node);
		else if (funcName.equals("Plus"))
			typePlus(node);
		else if (funcName.equals("Sqrt"))
			typeSqrt(node);
		else if (funcName.equals("Integrate"))
			typeIntegrate(node);
		else
		{
			Log.v(TAG,"Specialized function not recognized...\"" + funcName + "\"");
			// for generic functions 
			typeText(node.get(0));
			typeText(BRACKET_LEFT);
			for (int i = 1; i < node.size();i++)
			{
				ASTNode child = node.get(i);
				if (i > 1)
					typeText(",");
				typeset(child);
			}
			typeText(BRACKET_RIGHT);
		}
	}
	private void typeText(ASTNode node)
	{
		typeText(node.toString());
	}
	private void typeText(String s)
	{
		Log.v(TAG,"Generating text view for " + s);
		float width;

		width = p.measureText(s);
		c.drawText(s, xpos, ypos, p);
		xpos += (float) width;		// update the internal pointer
	}
}
