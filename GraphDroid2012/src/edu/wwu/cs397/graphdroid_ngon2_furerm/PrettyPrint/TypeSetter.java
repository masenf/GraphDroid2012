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
import android.graphics.Path;
import android.graphics.RectF;
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
		p.setStyle(Paint.Style.STROKE);

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
		float curve_height = o_textSize * 0.3f;
		float curve_width = curve_height;
		float bar_height = o_textSize * 0.8f;
		float bar_width = bar_height/6;
		RectF oval_rect = new RectF();
		
		// Draw the integral symbol
		p.setStrokeWidth(3);
		ypos += curve_height/3;
		oval_rect.set(xpos, ypos-curve_height, xpos+curve_width, ypos);
		c.drawArc(oval_rect, 0, 160, false, p);
		xpos += curve_width;
		ypos -= curve_height/2;
		c.drawLine(xpos, ypos, xpos+bar_width, ypos-bar_height, p);
		xpos += bar_width;
		ypos -= (bar_height + curve_height / 2);
		oval_rect.set(xpos, ypos, xpos+curve_width, ypos+curve_height);
		c.drawArc(oval_rect, 180, 160, false, p);
		xpos += curve_width;
		ypos = o_ypos;
		xpos += 2.0f;
		p.setStrokeWidth(0);

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
		Path rad = new Path();
		
		// draw the head of the radical sign
		rad.moveTo(xpos, ypos - rad_height / 3);
		rad.lineTo(xpos + rad_head_width / 6, ypos - rad_height / 3);
		xpos += rad_head_width / 6;
		rad.lineTo(xpos + rad_head_width / 6, ypos);
		xpos += rad_head_width / 6;
		rad.lineTo(o_xpos + rad_head_width, ypos - rad_height);
		xpos = o_xpos + rad_head_width;
		
		// typeset the expression
		typeset(node.get(1));
		
		// draw the top bar
		rad.lineTo(xpos,  ypos-rad_height);
		p.setStrokeWidth(3);
		c.drawPath(rad, p);
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
		float dot_y = p.getTextSize() / 3;
		float margin = dot_y / 3;
		dot_y = ypos - dot_y;
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
			{
				p.setStrokeWidth(3);
				c.drawPoint(xpos+margin, dot_y, p);
				xpos += 2*margin;
				p.setStrokeWidth(0);
			}
				//typeText("á");
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
	private void typeAssign(FunctionNode node)
	{
		float dot_y = p.getTextSize() / 3;
		float margin = dot_y / 3;
		float arrow_body = 2 * p.getTextSize() / 3;
		float arrow_head = dot_y / 2;
		dot_y = ypos - dot_y;
		typeset(node.get(2));
		
		xpos += margin;
		Path arrow = new Path();
		arrow.moveTo(xpos, dot_y);
		arrow.lineTo(xpos+arrow_body, dot_y);
		arrow.moveTo(xpos+arrow_body-arrow_head, dot_y - arrow_head);
		arrow.lineTo(xpos+arrow_body, dot_y);
		arrow.lineTo(xpos+arrow_body-arrow_head, dot_y + arrow_head);
		p.setStrokeWidth(3);
		c.drawPath(arrow, p);
		p.setStrokeWidth(0);
		xpos += arrow_body + margin;
		typeset(node.get(1));
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
		else if (funcName.equals("Set"))
			typeAssign(node);
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
