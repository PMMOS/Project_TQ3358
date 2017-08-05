package com.embedsky.xmVideo;

import java.util.ArrayList;
import java.util.List;

import com.embedsky.led.R;

import com.Utils.Utils;
import com.adapters.PreviewsGridViewAdapter;
import com.interfaces.mCallBackOutAdapter;
import com.items.VideoForChoose;
import com.lib.funsdk.support.FunError;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.models.FunDevice;
import com.lib.funsdk.support.widget.FunVideoView;
import com.lib.sdk.struct.H264_DVR_FILE_DATA;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

public class MulscreenPlayFragment extends Fragment {

	//private TextView tv_title;
	private GridView gv_screens;
	//private ImageButton ib_rtn;
	
	private Context context;
	private View fragmentview;
	private List<TextView> textvlist = new ArrayList<TextView>();
	private List<FunVideoView> funvideovlist = new ArrayList<FunVideoView>();
	private PreviewsGridViewAdapter adapter;
	
	private int positionforonresult, playtype;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		fragmentview = inflater.inflate(R.layout.mulscreen, container, false);
		initview();

		return fragmentview;
	}

	@Override
	public void onStart(){
		super.onStart();
		playtype = getArguments().getInt("playtype",0);
		switch (playtype) {
			case 1:{
				int deviceid = getArguments().getInt("deviceid", 0);
				FunDevice funDevice = FunSupport.getInstance().findDeviceById(deviceid);
				if(funDevice == null)
					onDestroy();
				adapter = new PreviewsGridViewAdapter(4, context);
				gv_screens.setAdapter(adapter);
				gv_screens.post(new gvpostRunnable(mHandler));
			}
				break;
			// case 2:{
			// 	int screennumber = getArguments().getInt("screennumber", 0);
			// 	if(screennumber <= 0)
			// 		onDestory();
			// 	adapter = new PreviewsGridViewAdapter(screennumber, this);
			// 	gv_screens.setAdapter(adapter);
			// 	gv_screens.post(new gvpostRunnable(mHandler));
			// }
			// 	break;
			default:
				onDestroy();
				break;
		}
	}

	private class gvpostRunnable implements Runnable {
		private Handler handler;
		public gvpostRunnable(Handler handler) {
			this.handler = handler;
		}
		@Override
		public void run() {
			handler.sendEmptyMessage(1);
		}
		
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 1:{
				if(playtype == 1){
					for(int i = 0; i < 4; i++){
						View view = gv_screens.findViewWithTag(i);
						if(view != null){
							FunVideoView funVideoView = (FunVideoView) view.findViewById(R.id.funVideoView);
							TextView tv_videostate = (TextView) view.findViewById(R.id.tv_VideoState);
							funVideoView.setOnErrorListener(onErrorListener);
							funVideoView.setOnInfoListener(onInfoListener);
							tv_videostate.setText(R.string.media_player_opening);
							tv_videostate.setVisibility(View.VISIBLE);
							funvideovlist.add(i, funVideoView); 
							textvlist.add(i, tv_videostate);
							int deviceid = getArguments().getInt("deviceid", 0);
							FunDevice funDevice = FunSupport.getInstance().findDeviceById(deviceid);
							if (funDevice.isRemote) {
								funVideoView.setRealDevice(funDevice.getDevSn(), i);
							} else {
								String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
								funVideoView.setRealDevice(deviceIp, i);
							}
						}else {
							onDestroy();
						}
					}
				 }//else if(playtype == 2){
				// 	adapter.setMcallBack(new mCallBackOutAdapter() {
				// 		@Override
				// 		public void OnActivonUp(int position) {
				// 			System.err.println("OnActivonUp");
				// 			positionforonresult = position;
				// 			Intent intent = new Intent(context, MediaChoose.class);
				// 			startActivityForResult(intent, 1);
				// 		}
				// 	});
				// 	gv_screens.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				// 		@Override
				// 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 			System.err.println("onItemClick");
				// 			positionforonresult = position;
				// 			Intent intent = new Intent(context, MediaChoose.class);
				// 			startActivityForResult(intent, 1);
				// 		}
				// 	});
				// }
			}
				break;
			case 2:
				break;
			}
		};
	};

	private void initview() {
		//tv_title = (TextView) fragmentview.findViewById(R.id.tv_title);
		//ib_rtn = (ImageButton) fragmentview.findViewById(R.id.ib_rtn);
		gv_screens = (GridView) fragmentview.findViewById(R.id.gv_mulscreen);
		//tv_title.setText("多通道预览");
		// ib_rtn.setOnClickListener(new View.OnClickListener() {
		// 	@Override
		// 	public void onClick(View arg0) {
		// 		onDestroy();
		// 	}
		// });
	}

	private MediaPlayer.OnErrorListener onErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Utils.showToast(context, context.getResources().getString(R.string.media_play_error) 
					+ " : " 
					+ FunError.getErrorStr(extra));
			return true;
		}
	};

	private MediaPlayer.OnInfoListener onInfoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
//				textvlist.get(extra).setText(R.string.media_player_buffering);
//				textvlist.get(extra).setVisibility(View.VISIBLE);
				textvlist.get(positionforonresult).setText(R.string.media_player_buffering);
				textvlist.get(positionforonresult).setVisibility(View.VISIBLE);
			} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
//				textvlist.get(extra).setVisibility(View.GONE);
				textvlist.get(positionforonresult).setVisibility(View.GONE);
			}
			return true;
		}
	};

}
