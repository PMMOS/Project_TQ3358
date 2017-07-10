/*瑙嗛鎾斁绐楀彛*/
package com.lib.funsdk.support.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import com.android.gl2jni.GL2JNIView;
import com.basic.G;
import com.interfaces.mFunVideoViewOnTouchCallBack;
import com.lib.EUIMSG;
import com.lib.FunSDK;
import com.lib.IFunSDKResult;
import com.lib.MsgContent;
import com.lib.funsdk.support.FunError;
import com.lib.funsdk.support.FunLog;
import com.lib.funsdk.support.FunPath;
import com.lib.funsdk.support.models.FunStreamType;
import com.lib.funsdk.support.utils.MyUtils;
import com.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.lib.sdk.struct.H264_DVR_FINDINFO;
import com.video.opengl.GLSurfaceView20;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Message;
//import android.print.PrintDocumentAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class FunVideoView extends LinearLayout implements IFunSDKResult {

	private mFunVideoViewOnTouchCallBack mOnTouchCallBack;

	private final String TAG = "FunVideoView";

	private final int STAT_STOPPED = 0;
	private final int STAT_PLAYING = 1;
	private final int STAT_PAUSED = 2;

	private int mPlayStat = STAT_STOPPED;
	private FunStreamType mStreamType = FunStreamType.STREAM_SECONDARY;
	private String mVideoUrl = null;
	private H264_DVR_FILE_DATA mVideoFile = null;
	private String mDeviceSn = null;
	private int mPlayerHandler = 0;
	private int mUserID = -1;
	private GLSurfaceView mSufaceView = null;
	private boolean mInited = false;
	public boolean bRecord = false;
	private String mFilePath;

	private int mPlayStartPos = 0;
	private int mPlayEndPos = 0;
	private int mPlayPosition = 0;
	private boolean mIsPrepared = false;
	private int mChannel = 0;

	private OnPreparedListener mPreparedListener = null;
	private OnCompletionListener mCompletionListener = null;
	private OnErrorListener mErrorListener = null;
	private OnInfoListener mInfoListener = null;

	private float FistXLocation;
	private float FistYlocation;
	private boolean Istrigger;
	private final int LENTH = 1;
	private long time;

	// 鏄惁宸茬粡璋冪敤杩囧簳灞傛帴鍙ｆ挱鏀句簡
	private boolean mIsPlaying = false;

	// 鏄惁浣跨敤楸肩溂鏁堟灉
	private boolean mIsFishEyeEnable = false;

	public FunVideoView(Context context) {
		super(context);
		init();
	}

	public FunVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FunVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setOnPreparedListener(OnPreparedListener listener) {
		mPreparedListener = listener;
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		mCompletionListener = listener;
	}

	public void setOnErrorListener(OnErrorListener listener) {
		mErrorListener = listener;
	}

	public void setOnInfoListener(OnInfoListener listener) {
		mInfoListener = listener;
	}

	public void setFishEye(boolean enable) {
		mIsFishEyeEnable = true;
	}

	private void init() {
		if (!isInEditMode()) {
			if (mUserID == -1) {
				mUserID = FunSDK.RegUser(this);
			}

			mIsPlaying = false;
		}
	}

	private void initSurfaceView() {
		if (null == mSufaceView) {
			// if ( mIsFishEyeEnable ) {
			// mSufaceView = new GL2JNIView(getContext());
			// } else {
			mSufaceView = new GLSurfaceView20(getContext());
			mSufaceView.setLongClickable(true);
			// }
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			this.addView(mSufaceView, lp);
		}
	}

	private int getUserId() {
		return mUserID;
	}

	private void release() {
		stopPlayback();

		if (-1 != mUserID) {
			FunSDK.UnRegUser(mUserID);
			mUserID = -1;
		}

		mSufaceView = null;
	}

	/**
	 * 璁剧疆鎾斁鍦板潃
	 * 
	 * @param path
	 */
	public void setVideoPath(String path) {
		mVideoUrl = path;
		mPlayStat = STAT_PLAYING;
		openVideo();
	}

	/**
	 * 閫氳繃璁惧鐨処P(濡傛灉鏄疉P杩炴帴璁惧)鎴栬�呰澶囩殑搴忓垪鍙稴N(濡傛灉鏄簰鑱旂綉杩炴帴璁惧)鎾斁璁惧瑙嗛
	 * 
	 * @param devSn
	 *            璁惧鐨処P(AP杩炴帴鏃�)鎴栬�呰澶嘢N(浜掕仈缃戣繛鎺ユ椂)
	 */
	public void setRealDevice(String devSn, int channel) {
		System.err.println("in funvideoview setRealDevice");
		String playUrl = null;
		mChannel = channel;
		if (MyUtils.isIp(devSn)) {
			// 濡傛灉浼犲叆鐨処P鍦板潃,闇�瑕佹坊鍔犵鍙�
			playUrl = "real://" + devSn + ":34567";
		} else {
			playUrl = "real://" + devSn;
		}

		mDeviceSn = devSn;
		System.err.println("in funvideoview setRealDevice playurl = " + playUrl);
		setVideoPath(playUrl);
	}

	/**
	 * 鎾斁褰曞儚(鍙湁璧峰鏃堕棿)
	 * 
	 * @param devSn
	 * @param absTime
	 */
	public void playRecordByTime(String devSn, int absTime) {
		String playUrl = "time://" + Integer.toString(absTime);
		mDeviceSn = devSn;
		setVideoPath(playUrl);
	}

	/**
	 * 鎾斁褰曞儚(璧峰鏃堕棿-缁撴潫鏃堕棿)
	 * 
	 * @param devSn
	 * @param fromTime
	 * @param toTime
	 */
	public void playRecordByTime(String devSn, int fromTime, int toTime) {
		String playUrl = "time://" + Integer.toString(fromTime) + "-" + Integer.toString(toTime);
		mDeviceSn = devSn;
		setVideoPath(playUrl);
	}

	public void playRecordByFile(String devSn, H264_DVR_FILE_DATA file) {

		String playUrl = "file://";
		mDeviceSn = devSn;
		mVideoFile = file;

		setVideoPath(playUrl);
	}

	public void seek(int absTime) {
		if (mInited && mPlayerHandler != 0) {
			FunSDK.MediaSeekToTime(mPlayerHandler, 0, absTime, 0);
		}
	}

	public void seekbyfile(int absTime) {
		if (mInited && mPlayerHandler != 0) {
			FunSDK.MediaSeekToPos(mPlayerHandler, absTime, 0);
		}
	}

	public void pause() {
		if (isPlaying()) {
			FunSDK.MediaPause(mPlayerHandler, 1, 0);
		}

		mPlayStat = STAT_PAUSED;
	}

	public void resume() {
		if (mInited && mPlayerHandler != 0) {
			FunSDK.MediaPause(mPlayerHandler, 0, 0);
		}

		mPlayStat = STAT_PLAYING;
	}

	/**
	 * 鍋滄瑙嗛鎾斁
	 */
	public void stopPlayback() {
		if (mPlayerHandler != 0) {
			FunSDK.MediaStop(mPlayerHandler);
			mPlayerHandler = 0;
		}
		mDeviceSn = null;
		mVideoUrl = null;
		mIsPlaying = false;
	}

	/**
	 * 瑙嗛鎾斁鏄惁鏆傚仠鐘舵��
	 * 
	 * @return
	 */
	public boolean isPaused() {
		return (mPlayStat == STAT_PAUSED);
	}

	/**
	 * 瑙嗛鏄惁姝ｅ湪鎾斁
	 * 
	 * @return
	 */
	public boolean isPlaying() {
		return (mPlayStat == STAT_PLAYING && mInited && mPlayerHandler != 0);
	}

	/**
	 * 璁剧疆涓荤爜娴�/瀛愮爜娴�
	 * 
	 * @param streamType
	 */
	public void setStreamType(FunStreamType streamType) {
		mStreamType = streamType;
	}

	public FunStreamType getStreamType() {
		return mStreamType;
	}

	/**
	 * 鑾峰彇褰撳墠鎾斁杩涘害,鍗曚綅绉�
	 * 
	 * @return
	 */
	public int getPosition() {
		return mPlayPosition;
	}

	/**
	 * 鑾峰彇鎾斁璧峰鐐圭殑鏃ユ湡/鏃堕棿,鍗曚綅绉�
	 * 
	 * @return
	 */
	public int getStartTime() {
		return mPlayStartPos;
	}

	/**
	 * 鑾峰彇鎾斁缁撴潫鐐圭殑鏃ユ湡/鏃堕棿,鍗曚綅绉�
	 * 
	 * @return
	 */
	public int getEndTime() {
		return mPlayEndPos;
	}

	private String getPlayPath(String url) {
		if (url.contains("://")) {
			return url.substring(mVideoUrl.indexOf("://") + 3);
		}
		return url;
	}

	private void openVideo() {
		if (!mInited || null == mVideoUrl || mPlayStat != STAT_PLAYING || null == mSufaceView) {
			return;
		}

		mIsPrepared = false;
		mPlayPosition = 0;

		String playPath = getPlayPath(mVideoUrl);
		if (mVideoUrl.startsWith("real://")) {
			if (!mIsPlaying) {
				// 鎾斁瀹炴椂瑙嗛
				mPlayerHandler = FunSDK.MediaRealPlay(getUserId(), playPath, mChannel, mStreamType.getTypeId(),
						mSufaceView, 0);
			}
			mIsPlaying = true;
		} else if (mVideoUrl.startsWith("time://")) {
			System.err.println("in startsWith(time)");
			if (!mIsPlaying) {
				// 鎾斁褰曞儚
				int fromTime = -1;
				int toTime = -1;
				if (playPath.contains("-")) {
					String[] tmStrs = playPath.split("-");
					fromTime = Integer.parseInt(tmStrs[0]);
					toTime = Integer.parseInt(tmStrs[1]);
				} else {
					fromTime = Integer.parseInt(playPath);
				}

				Date fromDate = new Date((long) fromTime * 1000);
				PrintDate(fromDate);
				H264_DVR_FINDINFO fileInfo = new H264_DVR_FINDINFO();

				fileInfo.st_2_startTime.st_0_dwYear = fromDate.getYear() + 1900;
				fileInfo.st_2_startTime.st_1_dwMonth = fromDate.getMonth() + 1;
				fileInfo.st_2_startTime.st_2_dwDay = fromDate.getDate();
				fileInfo.st_2_startTime.st_3_dwHour = fromDate.getHours();
				fileInfo.st_2_startTime.st_4_dwMinute = fromDate.getMinutes();
				fileInfo.st_2_startTime.st_5_dwSecond = fromDate.getSeconds();
				if (toTime > 0 && toTime > fromTime) {
					Date toDate = new Date((long) toTime * 1000);
					PrintDate(toDate);
					fileInfo.st_3_endTime.st_0_dwYear = toDate.getYear() + 1900;
					fileInfo.st_3_endTime.st_1_dwMonth = toDate.getMonth() + 1;
					fileInfo.st_3_endTime.st_2_dwDay = toDate.getDate();
					fileInfo.st_3_endTime.st_3_dwHour = toDate.getHours();
					fileInfo.st_3_endTime.st_4_dwMinute = toDate.getMinutes();
					fileInfo.st_3_endTime.st_5_dwSecond = toDate.getSeconds();
				} else {
					fileInfo.st_3_endTime.st_0_dwYear = fromDate.getYear() + 1900;
					fileInfo.st_3_endTime.st_1_dwMonth = fromDate.getMonth() + 1;
					fileInfo.st_3_endTime.st_2_dwDay = fromDate.getDate();
					fileInfo.st_3_endTime.st_3_dwHour = 23;
					fileInfo.st_3_endTime.st_4_dwMinute = 59;
					fileInfo.st_3_endTime.st_5_dwSecond = 59;
				}
				fileInfo.st_6_StreamType = mStreamType.getTypeId();

				mPlayerHandler = FunSDK.MediaNetRecordPlayByTime(getUserId(), mDeviceSn, G.ObjToBytes(fileInfo),
						mSufaceView, 0);
			}
			mIsPlaying = true;
		} else if (mVideoUrl.startsWith("file://")) {
			if (!mIsPlaying) {
				mPlayerHandler = FunSDK.MediaNetRecordPlay(getUserId(), mDeviceSn, G.ObjToBytes(mVideoFile),
						mSufaceView, 0);
			}
			mIsPlaying = true;
		}
	}

	private void PrintDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.err.println(sdf.format(date));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed || mInited == false) {

			initSurfaceView();

			mInited = true;

			if (mPlayStat == STAT_PLAYING && null != mVideoUrl) {
				openVideo();
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDetachedFromWindow() {
		this.release();
		super.onDetachedFromWindow();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		System.out.println("TTTT-----FunVideo");
		return super.onTouchEvent(event);
	}

	public void setmOnTouchCallBack(mFunVideoViewOnTouchCallBack mOnTouchCallBack) {
		this.mOnTouchCallBack = mOnTouchCallBack;
	}

	// here to intercept evention by some condition, to show or hide the button
	// bar
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mOnTouchCallBack != null) {
			mOnTouchCallBack.monTouch(ev);
		}
		int deltaX = 0;
		int deltaY = 0;
		long deltime = 0;
		int count = ev.getPointerCount();
		long times = ev.getEventTime();
		final float x = ev.getX();
		final float y = ev.getY();

		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			deltaX = (int) (FistXLocation - x);
			deltaY = (int) (FistYlocation - y);
			deltime = times - time;
			System.out.println("TTTT---->>" + deltime);
			if (count == 1) {
				if (deltime < 100) {
					if (Math.abs(deltaY) < LENTH && Math.abs(deltaX) < LENTH) {
						Istrigger = true;
						return true;
					}
				}
			}
			return super.onInterceptTouchEvent(ev);
		case MotionEvent.ACTION_DOWN:
			FistXLocation = x;
			FistYlocation = y;
			time = ev.getDownTime();
			if (getScaleY() < -400) {
				System.out.println(getScaleY());
			}
			// requestDisallowInterceptTouchEvent(false);
			return super.onInterceptTouchEvent(ev);

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (Istrigger) {

				Istrigger = false;
				return super.onInterceptTouchEvent(ev);
			}
			break;
		// deltaX = (int)(FistXLocation - x);
		// deltaY = (int)(FistYlocation - y);
		// deltime = times - time;
		// System.out.println("TTT---->>" + deltime);
		// if (count == 1) {
		// if (deltime < 100) {
		// if (Math.abs(deltaY) < LENTH
		// && Math.abs(deltaX) < LENTH) {
		// Istrigger = true;
		// return true;
		// }
		// }
		// }
		// break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent event) {
	// if ( null != mSufaceView ) {
	// return mSufaceView.dispatchTouchEvent(event);
	// }
	// return super.dispatchTouchEvent(event);
	// }

	public void setMediaSound(boolean bSound) {
		FunSDK.MediaSetSound(mPlayerHandler, bSound ? 100 : 0, 0);
	}

	/**
	 * 瑙嗛鎴浘
	 * 
	 * @param path
	 */
	public String captureImage(String path) {
		if (0 != mPlayerHandler) {
			if (null == path) {
				// 鑷姩浜х敓涓�涓矾寰�
				path = FunPath.getCapturePath();
			}

			int result = FunSDK.MediaSnapImage(mPlayerHandler, path, 0);
			if (result == 0) {
				return path;
			}
			return null;
		}

		return null;
	}

	/**
	 * 褰曞埗瑙嗛鍒版寚瀹氭枃浠�
	 * 
	 * @param path
	 */
	public void startRecordVideo(String path) {
		if (0 != mPlayerHandler) {

			if (!bRecord) {
				if (null == path) {
					path = FunPath.getRecordPath();
				}
				mFilePath = path;
				bRecord = true;
				FunSDK.MediaStartRecord(mPlayerHandler, mFilePath, 0);
			}
		}
	}

	public void stopRecordVideo() {
		if (0 != mPlayerHandler) {
			if (bRecord) {
				bRecord = false;
				FunSDK.MediaStopRecord(mPlayerHandler, 0);
			}

		}
	}

	public String getFilePath() {
		return mFilePath;
	}

	private int parsePlayPosition(String str) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			return (int) (sdf.parse(str).getTime() / 1000);
		} catch (Exception e) {

		}
		return 0;
	}

	private int parsePlayBeginTime(String str) {
		try {
			if (str.contains("=")) {
				str = str.substring(str.indexOf("=") + 1);
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH);
			return (int) (sdf.parse(str).getTime() / 1000);
		} catch (Exception e) {

		}
		return 0;
	}

	@Override
	public int OnFunSDKResult(Message msg, MsgContent msgContent) {
		FunLog.d(TAG, "msg.what : " + msg.what);
		FunLog.d(TAG, "msg.arg1 : " + msg.arg1 + " [" + FunError.getErrorStr(msg.arg1) + "]");
		FunLog.d(TAG, "msg.arg2 : " + msg.arg2);
		if (null != msgContent) {
			FunLog.d(TAG, "msgContent.sender : " + msgContent.sender);
			FunLog.d(TAG, "msgContent.seq : " + msgContent.seq);
			FunLog.d(TAG, "msgContent.str : " + msgContent.str);
			FunLog.d(TAG, "msgContent.arg3 : " + msgContent.arg3);
			FunLog.d(TAG, "msgContent.pData : " + msgContent.pData);
		}

		switch (msg.what) {
		case EUIMSG.START_PLAY: {
			FunLog.i(TAG, "EUIMSG.START_PLAY");
			if (msg.arg1 == FunError.EE_OK) {
				// 鎾斁鎴愬姛
				if (null != msgContent.str) {
					String[] infos = msgContent.str.split(";");

					if (infos.length > 2) {
						mPlayStartPos = parsePlayBeginTime(infos[1]);
						mPlayEndPos = parsePlayBeginTime(infos[2]);
					}

					if (msgContent.arg3 == 3) {
						// DSS妯″紡鎾斁
					} else {
						// 鏅�氭ā寮�
					}
				}
			} else {
				// 鎾斁澶辫触
				if (null != mErrorListener) {
					mErrorListener.onError(null, MediaPlayer.MEDIA_ERROR_UNKNOWN, msg.arg1);
				}
			}

		}
			break;
		case EUIMSG.SEEK_TO_TIME: {
			FunLog.i(TAG, "EUIMSG.SEEK_TO_TIME");
		}
			break;
		case EUIMSG.SEEK_TO_POS: {
			FunLog.i(TAG, "EUIMSG.SEEK_TO_POS");
		}
			break;
		case EUIMSG.ON_PLAY_INFO: {
			FunLog.i(TAG, "EUIMSG.ON_PLAY_INFO");
			if (null != msgContent.str) {
				String[] infos = msgContent.str.split(";");

				if (infos.length > 0) {
					// 鏇存柊鎾斁杩涘害
					mPlayPosition = parsePlayPosition(infos[0]);
					mInfoListener.onInfo(null, MediaPlayer.MEDIA_INFO_METADATA_UPDATE, mPlayPosition);
				}
			}
		}
			break;
		case EUIMSG.ON_PLAY_END: {
			FunLog.i(TAG, "EUIMSG.ON_PLAY_END");
			if (null != mCompletionListener) {
				mCompletionListener.onCompletion(null);
			}
		}
			break;
		case EUIMSG.ON_PLAY_BUFFER_BEGIN: {
			FunLog.i(TAG, "EUIMSG.ON_PLAY_BUFFER_BEGIN");
			if (null != mInfoListener) {
				mInfoListener.onInfo(null, MediaPlayer.MEDIA_INFO_BUFFERING_START, mChannel);
			}
		}
			break;
		case EUIMSG.ON_PLAY_BUFFER_END: {
			FunLog.i(TAG, "EUIMSG.ON_PLAY_BUFFER_END");
			if (null != mInfoListener) {
				mInfoListener.onInfo(null, MediaPlayer.MEDIA_INFO_BUFFERING_END, mChannel);
			}

			if (!mIsPrepared) {
				mIsPrepared = true;
				if (null != mPreparedListener) {
					mPreparedListener.onPrepared(null);
				}
			}
		}
			break;

		// case 5524: // YUV CallBack, FunSDK.MediaRealPlay()鏃禫iew浼爊ull灏变細鍥炲埌YUV鏁版嵁
		// {
		// FunLog.i(TAG, "__frame_count = " + __frame_count);
		// if ( null != msgContent.pData && __frame_count ++ == 100 ) {
		// try {
		// String path = FunPath.getCapturePath() + ".yuv";
		// FunLog.i(TAG, "write yuv file : " + path);
		// File file = new File(path);
		// if ( !file.exists() ) {
		// file.createNewFile();
		// }
		//
		// FileOutputStream fos = new FileOutputStream(file);
		// fos.write(msgContent.pData);
		// fos.flush();
		// fos.close();
		// file = null;
		// } catch (Exception e) {
		// e.printStackTrace();
		// FunLog.e(TAG, "write yuv file error");
		// }
		// }
		// }
		// break;
		}

		return 0;
	}

	// static int __frame_count = 0;
}
