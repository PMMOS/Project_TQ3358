package com.adapters;

import com.embedsky.led.R;
import com.interfaces.mCallBackOutAdapter;
import com.interfaces.mFunVideoViewOnTouchCallBack;
import com.lib.funsdk.support.widget.FunVideoView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PreviewsGridViewAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private int channels;
	private mCallBackOutAdapter mcallBack;
	
	public PreviewsGridViewAdapter(int channels,
			Context context) {
		// TODO Auto-generated constructor stub
		this.channels = channels;
		this.inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setMcallBack(mCallBackOutAdapter mcallBack) {
		this.mcallBack = mcallBack;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return channels;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return channels;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView == null)
			convertView = inflater.inflate(R.layout.layout_preview_item, null);
		convertView.setTag(position);
		final int thisposition = position;
		FunVideoView funVideoView = (FunVideoView) convertView.findViewById(R.id.funVideoView);
		funVideoView.setmOnTouchCallBack(new mFunVideoViewOnTouchCallBack() {
			@Override
			public void monTouch(MotionEvent ev) {
				if(ev.getAction() == MotionEvent.ACTION_UP)
					mcallBack.OnActivonUp(thisposition);
			}
		});
		return convertView;
	}
}
