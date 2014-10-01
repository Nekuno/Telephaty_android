package com.qnoow.telephaty;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class MsgArrayAdapter  extends ArrayAdapter<Msg> {
	private final Context context;
	private final List<Msg> data;

	public MsgArrayAdapter(Context context, List<Msg> data) {
		super(context, R.layout.message_item, data);
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.message_item,
				parent, false);
		
		TextView message = (TextView) rowView.findViewById(R.id.singleMessage);
		
		TextView time = (TextView) rowView.findViewById(R.id.hora);
		
		TextView mac = (TextView) rowView.findViewById(R.id.MAC);
		
		message.setText(data.get(position).getMessage());
		time.setText(data.get(position).getTime().toGMTString());
		mac.setText(data.get(position).getMac());

		return rowView;
	}
}