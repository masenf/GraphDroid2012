package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.List;
import java.util.Vector;

import org.matheclipse.parser.client.ast.ASTNode;

import edu.wwu.cs397.graphdroid_ngon2_furerm.PrettyPrint.TypeSetter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class homescreen_history_adapter extends BaseAdapter implements android.widget.Adapter {
	public enum el_type {input, output}
	public enum display_type {DISPLAY_RAW, DISPLAY_PARSED}
	public class history_element {
		public el_type type = null;
		public display_type display = display_type.DISPLAY_RAW;
		public String raw = null;
		public ASTNode parsed = null;
	}
	
	private static final String TAG = "homescreen_history_adapter";

	private static List<history_element> data;
	private Context ctx;
	private LayoutInflater inflater;

	public homescreen_history_adapter(Context context) {
		if (data == null)
		{
			data = new Vector<history_element>();
		}
		this.ctx = context;
		this.inflater = LayoutInflater.from(context);
		Log.v(TAG, "Instantiating history adapter");		
	}
	
	public int getCount() {
		Log.v(TAG, "Got count: " + data.size());
		return data.size();
	}

	public Object getItem(int pos) {
		Log.v(TAG, "Fetched item at position " + pos);
		return data.get(pos);
	}

	public long getItemId(int pos) {
		return pos;
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		Log.v(TAG, "Fetching view for postition " + pos);
		
		history_element el = data.get(pos);
		
		if (convertView == null) {	// we're not recycling
			Log.v(TAG, "Generating a fresh view");
			convertView = inflater.inflate(R.layout.homescreen_history_item, null);
		}
		if (el.type == el_type.input)
			((LinearLayout) convertView).setGravity(Gravity.LEFT);
		else
			((LinearLayout) convertView).setGravity(Gravity.RIGHT);
		
		TextView tv = (TextView) convertView.findViewById(R.id.history_txt);
		ImageView iv = (ImageView) convertView.findViewById(R.id.history_typeset);
		if (el.display == display_type.DISPLAY_RAW)
		{
			tv.setText(el.raw);
			tv.setVisibility(View.VISIBLE);
			iv.setVisibility(View.GONE);
		} else {
			Log.v(TAG,"Creating bitmap of size " + parent.getWidth() + "," + 50);
			Bitmap typeset = Bitmap.createBitmap(parent.getWidth(), 50, Config.ARGB_8888);
			TypeSetter t = new TypeSetter(el.parsed, typeset);
			Bitmap b = t.getBitmap();
			Log.v(TAG,"Received bitmap of size " + b.getWidth() + "," + b.getHeight());
			iv.setImageBitmap(t.getBitmap());
			iv.setVisibility(View.VISIBLE);
			tv.setVisibility(View.GONE);
		}
		return convertView;
	}
	public int addItem(el_type type, String raw, ASTNode parsed)
	{
		history_element h = new history_element();
		h.type = type;
		h.raw = raw;
		h.parsed = parsed;
		if (h.parsed != null)
			h.display = display_type.DISPLAY_PARSED;
		data.add(h);
		Log.v(TAG,"Added item " + raw);
		this.notifyDataSetChanged();
		return data.size() - 1;
	}
	

}
