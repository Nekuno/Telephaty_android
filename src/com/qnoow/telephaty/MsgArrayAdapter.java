package com.qnoow.telephaty;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;



public class MsgArrayAdapter  extends ArrayAdapter<Msg> {
	private final Context context;
	private final List<Msg> data;

	public MsgArrayAdapter(Context context, List<Msg> data) {
		super(context, R.layout.message_item_a, data);
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView;
		TextView message;
		TextView time ;
		TextView mac;
		
		if(data.get(position).getMac().equals("me")){
			rowView = inflater.inflate(R.layout.message_item_b,
				parent, false);
			message = (TextView) rowView.findViewById(R.id.singleMessageb);
			
			time = (TextView) rowView.findViewById(R.id.horab);
			
			mac = (TextView) rowView.findViewById(R.id.MACb);
		}
		else{
			rowView = inflater.inflate(R.layout.message_item_a,
					parent, false);
			message = (TextView) rowView.findViewById(R.id.singleMessagea);
			
			time = (TextView) rowView.findViewById(R.id.horaa);
			
			mac = (TextView) rowView.findViewById(R.id.MACa);
			
			LinearLayout layout = (LinearLayout) rowView.findViewById(R.id.singleMessageContainera);
			layout.setGravity(android.view.Gravity.RIGHT);
		}
		
	
		message.setText(data.get(position).getMessage());
		time.setText(data.get(position).getTime().toGMTString());
		mac.setText(data.get(position).getMac());
		
		

		return rowView;
	}
	
	
	
	
}