package com.embedsky.xmVideo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.embedsky.led.R;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.models.FunDevType;
import com.lib.funsdk.support.models.FunDevice;
import com.lib.funsdk.support.models.FunFileData;

import com.Utils.FunDeviceUtils;
import com.Utils.Utils;

import java.util.List;

public class DeviceLoginFragment extends Fragment{

	private static final String LOG_TAG = "devlogin";

	private FunDevice mFunDevice;
	private TextView tv_ip, tv_port, tv_logres;
	private Context context;
	private FunDeviceUtils fdu;
	private View fragmentview;
	private FragmentManager fgm;
	private FragmentTransaction fgt;
	private OnscreenPlayFragment onscreenplayfragment;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
		Log.d(LOG_TAG,"devlog onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		fragmentview = inflater.inflate(R.layout.devlog, container, false);
		Log.d(LOG_TAG,"devlog onCreateView");
		fdu = new FunDeviceUtils(context, mTaskHandler);
		tv_ip = (TextView) fragmentview.findViewById(R.id.devipval);
		tv_port = (TextView) fragmentview.findViewById(R.id.devportval);
		tv_logres = (TextView) fragmentview.findViewById(R.id.devlogres);

		//devLogin();

		return fragmentview;
	}

	@Override
	public void onResume(){
		Log.d(LOG_TAG,"login resume");
		devLogin();
		super.onResume();
	}

	private void devLogin(){
		String devIP = "10.11.62.234";
		int devPort = 34567;
		Log.d(LOG_TAG,"devlog settext");
		tv_ip.setText(devIP);
		tv_port.setText(String.valueOf(devPort));
		FunDevType devType = null;
		String devMac = null;
		String dev = devIP +":"+String.valueOf(devPort);
		mFunDevice = FunSupport.getInstance().buildTempDeivce(devType, devMac);
		mFunDevice.devType = FunDevType.EE_DEV_NORMAL_MONITOR;
		mFunDevice.devIp = devIP;
		mFunDevice.tcpPort = devPort;
		mFunDevice.devSn = dev;
		mFunDevice.loginName = "admin";
		mFunDevice.loginPsw = "";
		mFunDevice.CurrChannel = 0;
		Log.d(LOG_TAG,"device log data set");
		fdu.DeviceLogin(mFunDevice);

	}

	private Handler mTaskHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what){
				case FunDeviceUtils.MESSAGE_DEVICE_LOGINSUCCEED: {
					if(!fdu.saveDevice(mFunDevice)){
						Log.e(LOG_TAG,"saving FunDevice: "+mFunDevice.getDevIP()+"failed");
					}
					tv_logres.setText("device login success");
					Bundle bundle = new Bundle();
					bundle.putInt("deviceid", mFunDevice.getId());
					onscreenplayfragment = new OnscreenPlayFragment();
					onscreenplayfragment.setArguments(bundle);
					fgm = getFragmentManager();
					fgt = fgm.beginTransaction();
					fgt.replace(R.id.fragment_video, onscreenplayfragment);
					fgt.addToBackStack(null);
					fgt.commit();
				}
					break;
				case FunDeviceUtils.MESSAGE_DEVICE_LOGINFAILURED:
					Utils.showToast(context,"device login failed");
					break;
				case FunDeviceUtils.MESSAGE_RECORD_QUERYBYFILE: {
					List<FunFileData> files = (List<FunFileData>) msg.obj;
					for(int i = 0; i < files.size(); i++){
						FunFileData file = files.get(i);
						Log.d(LOG_TAG,"begin time: "+file.getBeginDateStr()+" "+file.getBeginTimeStr());
						Log.d(LOG_TAG,"end time: "+file.getEndDateStr()+" "+file.getEndTimeStr());
					}
				}
					break;
				default:
					break;
			}
		};
	};

	@Override
	public void onDestroy(){
		fdu.OnDestory();
		mTaskHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

}
