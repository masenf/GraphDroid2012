package edu.wwu.cs397.graphdroid_ngon2_furerm;

import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe.FuncContainer;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class function_list_fragment extends EvalFragment
	implements OnItemClickListener, OnItemLongClickListener {
		private static final String TAG = "function_list_fragment";
		private FuncContainer db;
		private int active_group = 0;
		private EditText last_view;
		private long last_id;
		
	    private class myViewBinder implements ViewBinder {
	        
	        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	            /* Callback from the CursorAdapter
	             * check if view is the image view and then update the graphic
	             */
	        	Log.v(TAG,"Viewbinder -- setViewValue(" + cursor.getColumnName(columnIndex) + ") = " + cursor.getString(columnIndex));
	        	final int col_id = cursor.getColumnIndexOrThrow(FuncContainer.KEY_FUNCID);
	        	final Cursor c = cursor;
	        	final int id = c.getInt(col_id);
	        	Button rm_btn = (Button) ((RelativeLayout) view.getParent()).findViewById(R.id.func_list_item_remove);
	        	rm_btn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						db.remove(id);
						refreshList();
					}
				});
	        	switch(view.getId()) {
	        	case R.id.func_list_item_edittext:
	                OnTouchListener otl = new OnTouchListener() {
	        			public boolean onTouch(View v, MotionEvent event) {
	        				KeyboardContainer act = (KeyboardContainer) getActivity();
	        				EditText ed = (EditText) v;
	        				KeypadHandler.activeEditText = ed;
	        				setLastView(ed, id);
	        				act.showKeyboard();
	        			    int inType = ed.getInputType(); // backup the input type
	        			    ed.setInputType(InputType.TYPE_NULL); // disable soft input
	        			    ed.onTouchEvent(event); // call native handler
	        			    ed.setInputType(inType); // restore input type
	        				return true;
	        			}
	                 };
	                // hide the soft keyboard
	        		EditText ed = (EditText) view;
	        		ed.setOnTouchListener(otl);
	        		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	        		imm.hideSoftInputFromWindow(ed.getWindowToken(),0);
	        		return false;
	        	case R.id.func_list_item_enabled:
	        		CheckBox cb = (CheckBox) view;
	        		boolean visible = (cursor.getInt(columnIndex) > 0) ? true : false;
	        		cb.setChecked(visible);
	        		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							db.setVisibility(id, isChecked);
						}
	        		});
	        		break;
	        	case R.id.func_list_item_color:
	        		final int initialColor = c.getInt(columnIndex);
	        		final View c_button = view;
	        		view.setBackgroundColor(initialColor);
	        		view.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							// TODO Auto-generated method stub
							AmbilWarnaDialog dialog = new AmbilWarnaDialog(getActivity(), initialColor, new OnAmbilWarnaListener() {
							        public void onOk(AmbilWarnaDialog dialog, int color) {
							                c_button.setBackgroundColor(color);
							                db.setColor(id, color);
							        }
							                
							        public void onCancel(AmbilWarnaDialog dialog) {
							                // cancel was selected by the user
							        }
							});
							dialog.show();
						}
	        		});
	        		break;
	        	case R.id.func_list_item_linetype:
	        		break;
	        	case R.id.func_list_item_linewt:
	        		break;
	        	default:
	        		return false;
	        	}
	            return true;
	        }
	    } 
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
	        //super.onCreateView(savedInstanceState);
			Log.v(TAG,"onCreateView");
			View main = inflater.inflate(R.layout.function_list, null);
			
			Button btn_add_func = (Button) main.findViewById(R.id.btn_add_function);
			Button btn_rm_grp = (Button) main.findViewById(R.id.btn_remove_group);
			btn_add_func.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					
					db.add("", Color.BLACK, 1, 0, 0, active_group, true);
					refreshList();
				}
			});
			btn_rm_grp.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					db.removeGroup(active_group);
					((TabletInterface) getActivity()).removeSelectedGroup();
				}
				
			});


	        return main;
		}
		 @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
		}
		 @Override
		public void onResume() {
			getListView().setOnItemClickListener(this);
			getListView().setOnItemLongClickListener(this);
			
			db = FuncContainer.getIns(this.getActivity());
			// establish the cursor
			String[] from = new String[]{FuncContainer.STR_FUNC,
										 FuncContainer.BOOL_VISIBLE,
										 FuncContainer.INT_COLOR,
										 FuncContainer.INT_LINETYPE,
										 FuncContainer.INT_LINEWT};
			int[] to = new int[]{R.id.func_list_item_edittext,
								 R.id.func_list_item_enabled,
								 R.id.func_list_item_color,
								 R.id.func_list_item_linetype,
								 R.id.func_list_item_linewt};
			SimpleCursorAdapter items = 
			        new SimpleCursorAdapter(getActivity(), 
			        		R.layout.function_list_item, 
			        		null, 
			        		from, to);
			items.setViewBinder(new myViewBinder());
			setListAdapter(items);
			getListView().setOnItemClickListener(this);
			Log.v(TAG, "Called onResume, active_group for this fragment = " + active_group);
		    refreshList();
	        super.onResume();       // call through required!
		 }
		public void refreshList() {
			SimpleCursorAdapter a = (SimpleCursorAdapter) getListAdapter();
			if (a != null) {
				a.changeCursor(db.getGroup(active_group)); 
				db.setActive_group(active_group);
				a.notifyDataSetChanged();
				Log.v(TAG,"Refreshing list view. Items = " + a.getCount());
			} else {
				Log.v(TAG,"Refresh requested before fragment active...");
			}
	    }
		public void eval() 
		{	
			String expr = last_view.getText().toString();
			db.setExpr(last_id, expr);
			KeyboardContainer act = (KeyboardContainer) getActivity();
			act.hideKeyboard();
			db.setActive_group(active_group);
		}
		public void onItemClick(AdapterView<?> lv, View target, int pos, long id) {

		}
		public boolean onItemLongClick(AdapterView<?> lv, View target, int pos, long id) {
			return false;
		}
		public void setGroup(int grpid) {			
			active_group = grpid;
			
			refreshList();
		}
		public void setLastView(EditText ed, int id) {
			if (last_view != null) {
				last_view.setActivated(false);
			}
			Log.v(TAG,"Set last view");
			last_view = ed;
			last_id = id;
			last_view.setActivated(true);
		}
		public static function_list_fragment getInstance(int grpid)
		{
			function_list_fragment flf = new function_list_fragment();
			flf.setGroup(grpid);
			return flf;
		}
		
	}

