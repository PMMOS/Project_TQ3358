package com.embedsky.xmVideo;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.embedsky.led.R;
import com.Utils.SharedPreferencesNames.SPNames;
import com.Utils.SharedPreferencesNames.UserInfoItems;
import com.Utils.Utils;
import com.interfaces.mFunVideoViewOnTouchCallBack;
import com.interfaces.mLocalCaptureCallBack;
import com.interfaces.mPictureCallBack;
import com.items.LocalCapture;
import com.items.LocalRecord;
import com.items.WarnInfo;
import com.items.mH264File;
import com.items.mH264Files;
import com.lib.funsdk.support.FunError;
import com.lib.funsdk.support.FunLog;
import com.lib.funsdk.support.FunPath;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.models.FunDevice;
import com.lib.funsdk.support.models.FunStreamType;
import com.lib.funsdk.support.widget.FunVideoView;
import com.lib.sdk.struct.H264_DVR_FILE_DATA;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OnscreenPlayFragment extends Fragment implements OnClickListener, OnErrorListener,
		OnInfoListener, mFunVideoViewOnTouchCallBack, OnCompletionListener, mLocalCaptureCallBack {

	private static final String LOG_TAG = "lock";

	private final int MESSAGE_PLAY_MEDIA = 0x100;
	private final int MESSAGE_AUTO_HIDE_CONTROL_BAR = 0x102;
	private final int MESSAGE_TOAST_SCREENSHOT_PREVIEW = 0x103;
	private final int MESSAGE_TOAST_SCREENSHOT_SAVE = 0x104;
	private final int MESSAGE_OPEN_VOICE = 0x105;
	private final int MESSAGE_HIDE_CONTROL = 0x106;
	private final int MESSAGE_MCALLBACK_WARNSTART = 0x107;
	private final int MESSAGE_MCALLBACK_WARN = 0x108;
	private final int MESSAGE_MCALLBACK_WARNEND = 0x109;

	private mPictureCallBack mpictureupload;

	private int type;
	private FunVideoView mFunVideoView;
	//type == 0
	private FunDevice mFunDevice = null;
	//type == 1
	private H264_DVR_FILE_DATA file = null;
	//type == 3
	private List<H264_DVR_FILE_DATA> files = null;
	private int startseek, endseek;
	private int playingindex = 0;
	private String devSn = null;
	private boolean isseek = false;
	//type == 2 unuse
	private Integer starttime = null, endtime = null;
	
	private Context context;
	private View fragmentview;
	private ImageButton ib_playstop, ib_reload, ib_capture, ib_record, ib_preview, ib_warn, ib_rtn;
	private TextView tv_videostate;
	private RelativeLayout layout_warning, layout_recording, layout_suspension;
	private LinearLayout layout_control1, layout_control2;
	private TextView tv_mainsource, tv_subsource, tv_selectsource;
	private TextView tv_channel1, tv_channel2, tv_channel3, tv_channel4, tv_selectchannel;
	// private TextView tv_channel5, tv_channel6, tv_channel7, tv_channel8;
	private boolean isChannelShowing = false, isSourceShowing = false, isControlShowing = false;
	private boolean isChannelAnimating = false, isSourceAnimating = false;
	private boolean isPlaying = false, hasES;
	public int warnstatus = 0;
	private String localpath;
	private LocalCapture localCapture;
	private LocalRecord localRecord;
	private WarnInfo[] warnInfo = new WarnInfo[4];
	private String[] localRecordtime, localCapturetime, warnInfotime;

	private int channel = 0;
	private FragmentManager fgm;
	private FragmentTransaction fgt;
	private MulscreenPlayFragment mulscreenplayfragment;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
		SharedPreferences userinfodata = getActivity().getSharedPreferences(SPNames.UserInfo.getValue(), Context.MODE_PRIVATE);
		hasES = userinfodata.getBoolean(UserInfoItems.hasES.getValue(), false);
		if(hasES){
			localpath = userinfodata.getString(UserInfoItems.localPath.getValue(), null);
			if(localpath == null)
				hasES = false;
		}
		Log.d(LOG_TAG, "Onescreen play onCreate");
	}

	@Override 
	public void onAttach(Activity activity){
		try{
			mpictureupload = (mPictureCallBack) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString()+"must implements mPictureCallBack");
		}
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		fragmentview = inflater.inflate(R.layout.onescreen, container, false);
		Log.d(LOG_TAG, "Onescreen onCreateView");
		initview();
		return fragmentview;
	}

	@Override
	public void onStart(){
		// data get
		super.onStart();
		type = getArguments().getInt("type", 0);
		if(type == 0){
			int devId = getArguments().getInt("deviceid", 0);
			mFunDevice = FunSupport.getInstance().findDeviceById(devId);
			if (mFunDevice == null)
				onDestroy();
			else
				playRealMedia();
		}else if(type == 1){
			devSn = getArguments().getString("devsn");
			file = ((mH264File) getArguments().getSerializable("filedata")).getFile();
			playRecordMedia();
		}else if(type == 2){
			devSn = getArguments().getString("devsn");
			String stime = getArguments().getString("starttime");
			String etime = getArguments().getString("endtime");
			starttime = timeparse(stime);
			endtime = timeparse(etime);
			if(starttime == null || endtime == null){
				System.err.println("starttime or endtime error");
				onDestroy();
			}else
				playRecordMediaByTime();
		}else if(type == 3){
			startseek = getArguments().getInt("startseek", 0);
			endseek = getArguments().getInt("endseek", 0);
			System.err.println("in OneScreenActivity type == 3 "+startseek+","+endseek);
			devSn = getArguments().getString("devsn");
			files = ((mH264Files)getArguments().getSerializable("filedatas")).getFiles();
			file = files.get(playingindex++);
			playRecordMedia();
		}

	}


	private void initview() {
		mFunVideoView = (FunVideoView) fragmentview.findViewById(R.id.funVideoView);
		ib_playstop = (ImageButton) fragmentview.findViewById(R.id.ib_playstop);
		ib_reload = (ImageButton) fragmentview.findViewById(R.id.ib_reload);
		ib_capture = (ImageButton) fragmentview.findViewById(R.id.ib_capture);
		ib_record = (ImageButton) fragmentview.findViewById(R.id.ib_record);
		ib_preview = (ImageButton) fragmentview.findViewById(R.id.ib_preview);
		ib_warn = (ImageButton) fragmentview.findViewById(R.id.ib_warn);
		ib_rtn = (ImageButton) fragmentview.findViewById(R.id.ib_rtn);
		tv_videostate = (TextView) fragmentview.findViewById(R.id.tv_VideoState);
		layout_recording = (RelativeLayout) fragmentview.findViewById(R.id.layout_Recording);
		layout_warning = (RelativeLayout) fragmentview.findViewById(R.id.layout_Warning);
		layout_suspension = (RelativeLayout) fragmentview.findViewById(R.id.layout_suspension);
		layout_control1 = (LinearLayout) fragmentview.findViewById(R.id.layout_control1);
		layout_control2 = (LinearLayout) fragmentview.findViewById(R.id.layout_control2);
		tv_mainsource = (TextView) fragmentview.findViewById(R.id.tv_mainsource);
		tv_subsource = (TextView) fragmentview.findViewById(R.id.tv_subsource);
		tv_selectsource = (TextView) fragmentview.findViewById(R.id.tv_selectsource);
		tv_channel1 = (TextView) fragmentview.findViewById(R.id.tv_channel1);
		tv_channel2 = (TextView) fragmentview.findViewById(R.id.tv_channel2);
		tv_channel3 = (TextView) fragmentview.findViewById(R.id.tv_channel3);
		tv_channel4 = (TextView) fragmentview.findViewById(R.id.tv_channel4);
		// tv_channel5 = (TextView) fragmentview.findViewById(R.id.tv_channel5);
		// tv_channel6 = (TextView) fragmentview.findViewById(R.id.tv_channel6);
		// tv_channel7 = (TextView) fragmentview.findViewById(R.id.tv_channel7);
		// tv_channel8 = (TextView) fragmentview.findViewById(R.id.tv_channel8);
		tv_selectchannel = (TextView) fragmentview.findViewById(R.id.tv_selectchannel);

		mFunVideoView.setOnErrorListener(this);
		mFunVideoView.setOnInfoListener(this);
		mFunVideoView.setOnCompletionListener(this);
		mFunVideoView.setmOnTouchCallBack(this);
		ib_playstop.setOnClickListener(this);
		ib_reload.setOnClickListener(this);
		ib_capture.setOnClickListener(this);
		ib_record.setOnClickListener(this);
		ib_preview.setOnClickListener(this);
		ib_warn.setOnClickListener(this);
		ib_rtn.setOnClickListener(this);
		tv_mainsource.setOnClickListener(this);
		tv_subsource.setOnClickListener(this);
		tv_selectsource.setOnClickListener(this);
		tv_channel1.setOnClickListener(this);
		tv_channel2.setOnClickListener(this);
		tv_channel3.setOnClickListener(this);
		tv_channel4.setOnClickListener(this);
		// tv_channel5.setOnClickListener(this);
		// tv_channel6.setOnClickListener(this);
		// tv_channel7.setOnClickListener(this);
		// tv_channel8.setOnClickListener(this);
		tv_channel1.setTag(1001);
		tv_channel2.setTag(1002);
		tv_channel3.setTag(1003);
		tv_channel4.setTag(1004);
		// tv_channel5.setTag(1005);
		// tv_channel6.setTag(1006);
		// tv_channel7.setTag(1007);
		// tv_channel8.setTag(1008);
		tv_selectchannel.setOnClickListener(this);
		setPlaystop(false);
		showControl();
	}

	@Override
	public void onResume() {
		if(type == 0){
			if(mFunDevice != null)
				playRealMedia();
		}
		else if(type == 1 || type == 3){
			if(file != null && devSn != null)
				mFunVideoView.resume();
		}
		else if(type == 2){
			if(devSn != null && starttime != null && endtime != null)
				mFunVideoView.resume();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if(type == 0)
			stopMedia();
		else
			mFunVideoView.pause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		stopMedia();
		if (null != mHandler) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		super.onDestroy();
	}

	private void stopMedia() {
		if (null != mFunVideoView) {
			mFunVideoView.stopPlayback();
			mFunVideoView.stopRecordVideo();
		}
	}
	
	private Integer timeparse(String timestr){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = sdf.parse(timestr);
			long timelong = date.getTime();
			return (int) (timelong / 1000);
		} catch (ParseException e) {
			return null;
		}
	}
	
	private void playRecordMediaByTime() {
		System.err.println("in playRecordMediaByTime");
		mFunVideoView.stopPlayback();
		tv_videostate.setText(R.string.media_player_opening);
		tv_videostate.setVisibility(View.VISIBLE);
		mFunVideoView.playRecordByTime(devSn, starttime, endtime);
		setPlaystop(true);
		mFunVideoView.setMediaSound(true);
		layout_suspension.setVisibility(View.INVISIBLE);
		ib_capture.setVisibility(View.INVISIBLE);
		ib_record.setVisibility(View.INVISIBLE);
		ib_preview.setVisibility(View.INVISIBLE);
		ib_warn.setVisibility(View.INVISIBLE);
	}
	
	private void playRecordMedia() {
		mFunVideoView.stopPlayback();
		tv_videostate.setText(R.string.media_player_opening);
		tv_videostate.setVisibility(View.VISIBLE);
		mFunVideoView.playRecordByFile(devSn, file);
		setPlaystop(true);
		mFunVideoView.setMediaSound(true);
		layout_suspension.setVisibility(View.INVISIBLE);
		ib_capture.setVisibility(View.INVISIBLE);
		ib_record.setVisibility(View.INVISIBLE);
		ib_preview.setVisibility(View.INVISIBLE);
		ib_warn.setVisibility(View.INVISIBLE);
	}

	private void playRealMedia() {
		mFunVideoView.stopPlayback();
		tv_videostate.setText(R.string.media_player_opening);
		tv_videostate.setVisibility(View.VISIBLE);
		if (mFunDevice.isRemote) {
			mFunVideoView.setRealDevice(mFunDevice.getDevSn(), mFunDevice.CurrChannel);
		} else {
			String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
			mFunVideoView.setRealDevice(deviceIp, mFunDevice.CurrChannel);
		}
		setPlaystop(true);
		mFunVideoView.setMediaSound(true);
		
		if (FunStreamType.STREAM_SECONDARY == mFunVideoView.getStreamType()) {
			tv_selectsource.setText("subsource");
			tv_subsource.setTextColor(context.getResources().getColor(R.color.blue));
			tv_mainsource.setTextColor(context.getResources().getColor(R.color.white));
		} else {
			tv_selectsource.setText("mainsource");
			tv_subsource.setTextColor(context.getResources().getColor(R.color.white));
			tv_mainsource.setTextColor(context.getResources().getColor(R.color.blue));
		}
		tv_selectchannel.setText("ch"+(mFunDevice.CurrChannel+1));
		for(int i = 1; i <= 4; i++){
			TextView tv_now = (TextView) layout_suspension.findViewWithTag(1000+i);
			if(mFunDevice.CurrChannel+1 == i)
				tv_now.setTextColor(context.getResources().getColor(R.color.blue));
			else
				tv_now.setTextColor(context.getResources().getColor(R.color.white));
		}
	}

	private void setPlaystop(boolean isPlaying) {
		this.isPlaying = isPlaying;
		if (isPlaying)
			ib_playstop.setImageDrawable(context.getResources().getDrawable(R.drawable.stop));
		else
			ib_playstop.setImageDrawable(context.getResources().getDrawable(R.drawable.play));
	}

	private void showSource() {
		if (!isSourceAnimating) {
			isSourceAnimating = true;
			isSourceShowing = true;
			tv_selectsource.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			int width = tv_selectsource.getMeasuredWidth();
			ObjectAnimator firstAnimator = ObjectAnimator.ofFloat(tv_mainsource, "translationX", 0, -(width + 40) * 2);
			ObjectAnimator secondAnimator = ObjectAnimator.ofFloat(tv_subsource, "translationX", 0, -(width + 40) * 1);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.setDuration(500);
			animatorSet.setInterpolator(new OvershootInterpolator());
			animatorSet.playTogether(firstAnimator, secondAnimator);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					isSourceAnimating = false;
				}
			});
			animatorSet.start();
		}
	}

	private void hideSource() {
		if (!isSourceAnimating) {
			isSourceAnimating = true;
			isSourceShowing = false;
			ObjectAnimator firstAnimator = ObjectAnimator.ofFloat(tv_mainsource, "translationX",
					tv_mainsource.getTranslationX(), 0);
			ObjectAnimator secondAnimator = ObjectAnimator.ofFloat(tv_subsource, "translationX",
					tv_subsource.getTranslationX(), 0);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.setDuration(500);
			animatorSet.setInterpolator(new OvershootInterpolator());
			animatorSet.playTogether(firstAnimator, secondAnimator);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					isSourceAnimating = false;
				}
			});
			animatorSet.start();
		}
	}

	private void showChannel() {
		if (!isChannelAnimating) {
			isChannelAnimating = true;
			isChannelShowing = true;
			tv_selectchannel.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			int width = tv_selectchannel.getMeasuredWidth();
			ObjectAnimator Animator1 = ObjectAnimator.ofFloat(tv_channel1, "translationX", 0, -(width + 40) * 4);
			ObjectAnimator Animator2 = ObjectAnimator.ofFloat(tv_channel2, "translationX", 0, -(width + 40) * 3);
			ObjectAnimator Animator3 = ObjectAnimator.ofFloat(tv_channel3, "translationX", 0, -(width + 40) * 2);
			ObjectAnimator Animator4 = ObjectAnimator.ofFloat(tv_channel4, "translationX", 0, -(width + 40) * 1);
			// ObjectAnimator Animator5 = ObjectAnimator.ofFloat(tv_channel5, "translationX", 0, -(width + 40) * 4);
			// ObjectAnimator Animator6 = ObjectAnimator.ofFloat(tv_channel6, "translationX", 0, -(width + 40) * 3);
			// ObjectAnimator Animator7 = ObjectAnimator.ofFloat(tv_channel7, "translationX", 0, -(width + 40) * 2);
			// ObjectAnimator Animator8 = ObjectAnimator.ofFloat(tv_channel8, "translationX", 0, -(width + 40) * 1);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.setDuration(500);
			animatorSet.setInterpolator(new OvershootInterpolator());
			animatorSet.playTogether(Animator1, Animator2, Animator3, Animator4);
			// , Animator5, Animator6, Animator7,
			// 		Animator8);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					isChannelAnimating = false;
				}
			});
			animatorSet.start();
		}
	}

	private void hideChannel() {
		if (!isChannelAnimating) {
			isChannelAnimating = true;
			isChannelShowing = false;
			ObjectAnimator Animator1 = ObjectAnimator.ofFloat(tv_channel1, "translationX",
					tv_channel1.getTranslationX(), 0);
			ObjectAnimator Animator2 = ObjectAnimator.ofFloat(tv_channel2, "translationX",
					tv_channel2.getTranslationX(), 0);
			ObjectAnimator Animator3 = ObjectAnimator.ofFloat(tv_channel3, "translationX",
					tv_channel3.getTranslationX(), 0);
			ObjectAnimator Animator4 = ObjectAnimator.ofFloat(tv_channel4, "translationX",
					tv_channel4.getTranslationX(), 0);
			// ObjectAnimator Animator5 = ObjectAnimator.ofFloat(tv_channel5, "translationX",
			// 		tv_channel5.getTranslationX(), 0);
			// ObjectAnimator Animator6 = ObjectAnimator.ofFloat(tv_channel6, "translationX",
			// 		tv_channel6.getTranslationX(), 0);
			// ObjectAnimator Animator7 = ObjectAnimator.ofFloat(tv_channel7, "translationX",
			// 		tv_channel7.getTranslationX(), 0);
			// ObjectAnimator Animator8 = ObjectAnimator.ofFloat(tv_channel8, "translationX",
			// 		tv_channel8.getTranslationX(), 0);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.setDuration(500);
			animatorSet.setInterpolator(new OvershootInterpolator());
			animatorSet.playTogether(Animator1, Animator2, Animator3, Animator4);
			// , Animator5, Animator6, Animator7,
			// 		Animator8);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					isChannelAnimating = false;
				}
			});
			animatorSet.start();
		}
	}

	private void showControl() {
		if (!isControlShowing) {
			isControlShowing = true;
			ObjectAnimator Animator1 = ObjectAnimator.ofFloat(layout_control1, "translationY",
					layout_control1.getTranslationX(), 0);
			ObjectAnimator Animator2 = ObjectAnimator.ofFloat(layout_control2, "translationY",
					layout_control2.getTranslationX(), 0);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.setDuration(2000);
			animatorSet.setInterpolator(new OvershootInterpolator());
			animatorSet.playTogether(Animator1, Animator2);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
				}
			});
			animatorSet.start();
			ControlBarHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, 8000);
		}
	}

	private void hideControl() {
		if (isControlShowing) {
			isControlShowing = false;
			layout_control1.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			int height1 = layout_control1.getMeasuredHeight();
			layout_control2.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			int height2 = layout_control1.getMeasuredHeight();
			ObjectAnimator Animator1 = ObjectAnimator.ofFloat(layout_control1, "translationY", 0, height1);
			ObjectAnimator Animator2 = ObjectAnimator.ofFloat(layout_control2, "translationY", 0, -height2);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.setDuration(2000);
			animatorSet.setInterpolator(new OvershootInterpolator());
			animatorSet.playTogether(Animator1, Animator2);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
				}
			});
			animatorSet.start();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ib_rtn: {
			stopMedia();
			onDestroy();
		}
			break;
		case R.id.tv_selectsource: {
			if (isSourceShowing)
				hideSource();
			else
				showSource();
		}
			break;
		case R.id.tv_mainsource: {
			tv_selectsource.setText("mainsource");
			hideSource();
			if (FunStreamType.STREAM_MAIN != mFunVideoView.getStreamType()) {
				mFunVideoView.setStreamType(FunStreamType.STREAM_MAIN);
			}
			playRealMedia();
		}
			break;
		case R.id.tv_subsource: {
			tv_selectsource.setText("subsource");
			hideSource();
			if (FunStreamType.STREAM_SECONDARY != mFunVideoView.getStreamType()) {
				mFunVideoView.setStreamType(FunStreamType.STREAM_SECONDARY);
			}
			playRealMedia();
		}
			break;
		case R.id.tv_selectchannel: {
			if (isChannelShowing)
				hideChannel();
			else
				showChannel();
		}
			break;
		case R.id.tv_channel1: {
			tv_selectchannel.setText("ch1");
			hideChannel();
			mFunDevice.CurrChannel = 0;
			playRealMedia();
		}
			break;
		case R.id.tv_channel2: {
			tv_selectchannel.setText("ch2");
			hideChannel();
			mFunDevice.CurrChannel = 1;
			playRealMedia();
		}
			break;
		case R.id.tv_channel3: {
			tv_selectchannel.setText("ch3");
			hideChannel();
			mFunDevice.CurrChannel = 2;
			playRealMedia();
		}
			break;
		case R.id.tv_channel4: {
			tv_selectchannel.setText("ch4");
			hideChannel();
			mFunDevice.CurrChannel = 3;
			playRealMedia();
		}
			break;
		// case R.id.tv_channel5: {
		// 	tv_selectchannel.setText("5通道");
		// 	hideChannel();
		// 	mFunDevice.CurrChannel = 4;
		// 	playRealMedia();
		// }
		// 	break;
		// case R.id.tv_channel6: {
		// 	tv_selectchannel.setText("6通道");
		// 	hideChannel();
		// 	mFunDevice.CurrChannel = 5;
		// 	playRealMedia();
		// }
		// 	break;
		// case R.id.tv_channel7: {
		// 	tv_selectchannel.setText("7通道");
		// 	hideChannel();
		// 	mFunDevice.CurrChannel = 6;
		// 	playRealMedia();
		// }
		// 	break;
		// case R.id.tv_channel8: {
		// 	tv_selectchannel.setText("8通道");
		// 	hideChannel();
		// 	mFunDevice.CurrChannel = 7;
		// 	playRealMedia();
		// }
		// 	break;
		case R.id.ib_playstop: {
			if (isPlaying) {
				setPlaystop(false);
				if(type == 0)
					stopMedia();
				else
					mFunVideoView.pause();
			} else {
				setPlaystop(true);
				if(type == 0){
					if(mFunDevice != null)
						playRealMedia();
				}
				else if(type == 1 || type == 3){
					if(file != null && devSn != null)
						mFunVideoView.resume();
				}
				else if(type == 2){
					if(devSn != null && starttime != null && endtime == null)
						mFunVideoView.resume();
				}
			}
		}
			break;
		case R.id.ib_reload: {
			if(type == 0){
				if(mFunDevice != null)
					playRealMedia();
			}
			else if(type == 1) {
				if(file != null && devSn != null)
					playRecordMedia();
			}
			else if(type == 3){
				if(file != null && devSn != null){
					isseek = false;
					playRecordMedia();
				}
			}
			else if(type == 2) {
				if(devSn != null && starttime != null && endtime == null)
					playRecordMediaByTime();
			}
		}
			break;
		case R.id.ib_capture: {
			tryToCapture();
		}
			break;
		case R.id.ib_record: {
			tryToRecord();
		}
			break;
		case R.id.ib_preview: {
			stopMedia();
			//Intent intent = new Intent(OneScreenMPlayer.this, MulScreenMPlayer.class);
			//intent.putExtra("playtype", 1);
			System.err.println("deviceidddd = " + mFunDevice.getId());
			//intent.putExtra("deviceid", mFunDevice.getId());
			//startActivity(intent);
			Bundle bundle = new Bundle();
			bundle.putInt("playtype",1);
			bundle.putInt("deviceid", mFunDevice.getId());
			mulscreenplayfragment = new MulscreenPlayFragment();
			mulscreenplayfragment.setArguments(bundle);
			fgm = getFragmentManager();
			fgt = fgm.beginTransaction();
			fgt.replace(R.id.fragment_video, mulscreenplayfragment);
			fgt.addToBackStack(null);
			fgt.commit();
		}
			break;
		case R.id.ib_warn: {
			tryToWarn();
		}
			break;
		default:
			break;
		}
	}

	@Override
	public void setCapturePath(int warnStatus){
		mFunDevice.CurrChannel = 0;
		channel = 0;
		playRealMedia();
		warnstatus = warnStatus;
		Message message = new Message();
		message.what = MESSAGE_MCALLBACK_WARN;
		message.obj = warnStatus;
		mcallbackhandler.sendMessageDelayed(message,300);

		//tryToCapture();
	}

	 private void tryToWarn(int chan, int warnStatus){
	 	mFunDevice.CurrChannel = chan;
	 	playRealMedia();
	 	warnstatus = warnStatus;
	 	Message message = new Message();
		message.what = MESSAGE_MCALLBACK_WARN;
	 	mcallbackhandler.sendMessageDelayed(message,300);
	 }

	private void tryToWarn(){
		//截图，并保存信息，在报警界面读取信息，展示截图，播放云端录像
		if (!mFunVideoView.isPlaying()) {
			Utils.showToast(context, R.string.media_capture_failure_need_playing);
			return;
		}
		if(warnstatus == 0){
			Utils.showToast(context, "开始报警");
			layout_warning.setVisibility(View.VISIBLE);
			warnstatus = 1;
			warnInfotime = Utils.getFilenameAndCurrenttime();
			warnInfo[mFunDevice.CurrChannel] = new WarnInfo();
			warnInfo[mFunDevice.CurrChannel].setStarttime(warnInfotime[1]);
			warnInfo[mFunDevice.CurrChannel].setDeviceid(mFunDevice.getId());
			warnInfo[mFunDevice.CurrChannel].setChannel(mFunDevice.CurrChannel);
			warnInfo[mFunDevice.CurrChannel].setDevicename(mFunDevice.devIp + ":" + mFunDevice.tcpPort + "/" + mFunDevice.CurrChannel);
			//截图
			warnInfotime = Utils.getFilenameAndCurrenttime();
			String savepath = localpath + "WarnInfo" + File.separator + mFunDevice.getDevIP() + "-" + mFunDevice.CurrChannel + File.separator;
			String savename = warnInfotime[0] + ".jpg";
			File dirfile = new File(savepath);
			if(!dirfile.exists()){
				if(dirfile.mkdirs()){
					Log.d(LOG_TAG,savepath);
				}else{
					Log.d(LOG_TAG,"mkdirs failed");
				}
			}
	
			final String path = mFunVideoView.captureImage(savepath+savename);
			//final String path = captureImage(savepath, savename);
			if (!TextUtils.isEmpty(path)) {
				Log.d(LOG_TAG, path);
				Message message = new Message();
				message.what = MESSAGE_TOAST_SCREENSHOT_SAVE;
				message.obj = path;
				mHandler.sendMessageDelayed(message, 100);
			}
		}else if(warnstatus == 2) {
			Utils.showToast(context, "报警结束");
			layout_warning.setVisibility(View.INVISIBLE);
			Message msg = new Message();
			msg.what = MESSAGE_MCALLBACK_WARNEND;
			mHandler.sendMessageDelayed(msg, 50);
			//warnstatus = 0;
		}
	}

	private void tryToRecord() {
		if (!mFunVideoView.isPlaying() || mFunVideoView.isPaused()) {
			Utils.showToast(context, R.string.media_record_failure_need_playing);
			return;
		}
		if (mFunVideoView.bRecord) {
			mFunVideoView.stopRecordVideo();
			String VideoThumbSavePath = localpath + "LocalRecord" + File.separator + mFunDevice.getDevIP() + "-" + mFunDevice.CurrChannel + File.separator;
			String VideoThumbSaveName = localRecordtime[0] + ".thumb";
			String videopath = localRecord.getVideopath();
			localRecord.setEndtime(Utils.getCurrenttime());
			localRecord.setThumbpath(VideoThumbSavePath+VideoThumbSaveName);
			if(Utils.getVideoThumbAndSave(videopath, 500, 500, VideoThumbSavePath, VideoThumbSaveName)){
				Utils.saveRecordHistory(localRecord, context);
				layout_recording.setVisibility(View.INVISIBLE);
				toastRecordSucess(mFunVideoView.getFilePath());				
			}
			else {
				Utils.showToast(context, "保存录像失败");
			}
		} else {
			localRecordtime = Utils.getFilenameAndCurrenttime();
			String savepath = localpath + "LocalRecord" + File.separator + mFunDevice.getDevIP() + "-" + mFunDevice.CurrChannel + File.separator + localRecordtime[0] + ".mp4";
			localRecord = new LocalRecord();
			localRecord.setChannel(mFunDevice.CurrChannel);
			localRecord.setDevid(mFunDevice.getId());
			localRecord.setDevip(mFunDevice.devIp);
			localRecord.setStarttime(localRecordtime[1]);
			localRecord.setVideopath(savepath);
			mFunVideoView.startRecordVideo(savepath);
			layout_recording.setVisibility(View.VISIBLE);
			Utils.showToast(context, R.string.media_record_start);
		}
	}

	private void tryToCapture() {
		if (!mFunVideoView.isPlaying()) {
			Utils.showToast(context, R.string.media_capture_failure_need_playing);
			return;
		}
	
		localCapturetime = Utils.getFilenameAndCurrenttime();
		String savepath = localpath + "LocalCapture" + File.separator + mFunDevice.getDevIP() + "-" + mFunDevice.CurrChannel + File.separator;
		String savename = localCapturetime[0] + ".jpg";
		File dirfile = new File(savepath);
		if(!dirfile.exists())
			dirfile.mkdirs();
		final String path = mFunVideoView.captureImage(savepath+savename);
		if (!TextUtils.isEmpty(path)) {
			localCapture = new LocalCapture();
			localCapture.setDevid(mFunDevice.getId());
			localCapture.setDevip(mFunDevice.devIp);
			localCapture.setChannel(mFunDevice.CurrChannel);
			localCapture.setPath(savepath+savename);
			localCapture.setTime(localCapturetime[1]);
			Message message = new Message();
			message.what = MESSAGE_TOAST_SCREENSHOT_PREVIEW;
			message.obj = path;
			mHandler.sendMessageDelayed(message, 100);
		}
	}

	private Handler mcallbackhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what){
				case MESSAGE_MCALLBACK_WARN: tryToWarn();
				break;
				default:
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_PLAY_MEDIA: {
				playRealMedia();
			}
				break;
			case MESSAGE_AUTO_HIDE_CONTROL_BAR: {
				// hideVideoControlBar();
			}
				break;
			case MESSAGE_TOAST_SCREENSHOT_PREVIEW: {
				String path = (String) msg.obj;
				toastScreenShotPreview(path);
			}
				break;
			case MESSAGE_TOAST_SCREENSHOT_SAVE: {
				String path = (String) msg.obj;
				String ThumbSavePath = localpath + "WarnInfo" + File.separator + mFunDevice.getDevIP() + "-" + mFunDevice.CurrChannel + File.separator;
				String ThumbSaveName = warnInfotime[0] + ".thumb";
				//toastScreenShotPreview(path);
				if(Utils.getImageThumbAndSave(path, 500, 500, ThumbSavePath, ThumbSaveName)){
					warnInfo[mFunDevice.CurrChannel].setSavepath(path);
					warnInfo[mFunDevice.CurrChannel].setThumbpath(ThumbSavePath+ThumbSaveName);
					channel += 1;
					Log.d(LOG_TAG, "save success");
					mpictureupload.uploadPicture(ThumbSavePath+ThumbSaveName);
					if(channel < 3){
						tryToWarn(channel, 0);
					}else{
						channel = 0;
					}					
				}
				else {
					Utils.showToast(context, "保存截图失败");
					if(channel < 3){
					 	tryToWarn(channel, 0);
					 }else{
					  	channel = 0;
					 }		
				}
				//warnstatus = 2;
			}
				break;
			case MESSAGE_MCALLBACK_WARNEND: {
				warnInfo[mFunDevice.CurrChannel].setEndtime(Utils.getCurrenttime());
			    Utils.saveWarnHistory(warnInfo[mFunDevice.CurrChannel], context);
			    channel += 1;
			    if(channel < 3){
					tryToWarn(channel, 2);
				}else{
					channel = 0;
				}			
			}
				break;
			case MESSAGE_OPEN_VOICE: {
				mFunVideoView.setMediaSound(true);
			}
				break;
			case MESSAGE_HIDE_CONTROL: {
				if (isControlShowing)
					hideControl();
			}
				break;
			}
		}
	};

	private Handler ControlBarHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_HIDE_CONTROL: {
				if (isControlShowing)
					hideControl();
			}
				break;
			}
		}
	};

	private void toastScreenShotPreview(final String path) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.screenshot_preview, null, false);
		ImageView iv = (ImageView) view.findViewById(R.id.iv_screenshot_preview);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		iv.setImageBitmap(bitmap);
		new AlertDialog.Builder(getActivity()).setTitle(R.string.device_socket_capture_preview).setView(view)
				.setPositiveButton(R.string.device_socket_capture_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String ThumbSavePath = localpath + "LocalCapture" + File.separator + mFunDevice.getDevIP() + "-" + mFunDevice.CurrChannel + File.separator;
						String ThumbSaveName = localCapturetime[0] + ".thumb";
						if(Utils.getImageThumbAndSave(path, 500, 500, ThumbSavePath, ThumbSaveName)){
							localCapture.setThumbpath(ThumbSavePath+ThumbSaveName);
							Utils.saveCaptureHistory(localCapture, context);
							Utils.showToast(context, R.string.device_socket_capture_save_success);							
						}else
							Utils.showToast(context, "保存截图失败");
					}
				}).setNegativeButton(R.string.device_socket_capture_delete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FunPath.deleteFile(path);
						Utils.showToast(context, R.string.device_socket_capture_delete_success);
					}
				}).show();
	}

	private void toastRecordSucess(final String path) {
		new AlertDialog.Builder(getActivity()).setTitle(R.string.device_sport_camera_record_success)
				.setMessage(getString(R.string.media_record_stop) + path)
				.setPositiveButton(R.string.device_sport_camera_record_success_open,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent("android.intent.action.VIEW");
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								String type = "video/*";
								Uri uri = Uri.fromFile(new File(path));
								intent.setDataAndType(uri, type);
								startActivity(intent);
								FunLog.e("test", "------------startActivity------" + uri.toString());
							}
						})
				.setNegativeButton(R.string.device_sport_camera_record_success_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
				.show();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Utils.showToast(context, context.getResources().getString(R.string.media_play_error) + " : " + FunError.getErrorStr(extra));

		if (FunError.EE_TPS_NOT_SUP_MAIN == extra || FunError.EE_DSS_NOT_SUP_MAIN == extra) {
			// 不支持高清码流,设置为标清码流重新播放
			if (null != mFunVideoView) {
				mFunVideoView.setStreamType(FunStreamType.STREAM_SECONDARY);
				playRealMedia();
			}
		}
		return true;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
			tv_videostate.setText(R.string.media_player_buffering);
			tv_videostate.setVisibility(View.VISIBLE);
		} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
			tv_videostate.setVisibility(View.GONE);
			if(type == 3 && !isseek){
				isseek = true;
				mFunVideoView.seek(startseek);
				System.err.println("in onInfo, seek");
			}
		} else if (what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE){
			if(type == 3)
				if(playingindex == files.size()){
					if(endseek <= extra){
						Utils.showToast(context, "播放录像完成");
						stopMedia();
						onDestroy();
					}
				}
		}
		return true;
	}

	@Override
	public void monTouch(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			ControlBarHandler.removeCallbacksAndMessages(null);
			if (!isControlShowing) {
				showControl();
			} else {
				hideControl();
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		if(type == 1){
			onDestroy();
		}else if(type == 3){
			if(playingindex == files.size()){
				onDestroy();
				Utils.showToast(context, "播放录像完成");
			}
			else{
				file = files.get(playingindex);
				playRecordMedia();
			}
		}
	}


}