package com.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Utils.SharedPreferencesNames.LocalCaptureHistoryItems;
import com.Utils.SharedPreferencesNames.LocalRecordHistoryItems;
import com.Utils.SharedPreferencesNames.SPNames;
import com.Utils.SharedPreferencesNames.WarnInfoItems;
import com.items.LocalCapture;
import com.items.LocalRecord;
import com.items.WarnInfo;

//import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.view.View;
import android.widget.Toast;

//@SuppressLint("SimpleDateFormat")
public class Utils {
	public static void showToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int resID) {
		String str = context.getResources().getString(resID);
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	public static String StampToDate(Long stamp) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(stamp);
		return df.format(date);
	}

	public static String StampToDate(String stampstr) {
		Long stamp = Long.parseLong(stampstr);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(stamp);
		return df.format(date);
	}

	public static String getCurrenttime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}

	public static String getCurrentdate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(new Date());
	}

	public static String getFilename() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return df.format(new Date());
	}
	
	public static String[] getFilenameAndCurrenttime(){
		Date date = new Date();
		String[] ret = new String[2];
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		ret[0] = df.format(date);
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ret[1] = df.format(date);
		return ret;
	}
	
	public static boolean savebitmap(Bitmap bm,String dirpath,String bmname, int compressrate)
	{
		File dirfile = new File(dirpath);
		if(!dirfile.exists())
			dirfile.mkdirs();
		String path = dirpath + bmname;
		File bmFile = new File(path);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(bmFile));
			bm.compress(Bitmap.CompressFormat.PNG, compressrate, bos);
			bos.flush();
			bos.close();
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("in savebitmap" + e.getMessage());
			return false;
		} catch (IOException e) {
			System.err.println("in savebitmap" + e.getMessage());
			return false;
		}
	}
	
	public static boolean getVideoThumbAndSave(String videopath, int width, int height, String savepath, String savename){
		Bitmap bitmap = getVideoThumb(videopath, width, height);
		if(bitmap == null)
			return false;
		savebitmap(bitmap, savepath, savename, 100);
		return true;
	}

	public static Bitmap getVideoThumb(String path, int width, int height) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(path);
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException e) {
			System.err.println("In getVideoThumb, IllegalArgumentException");
			return null;
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException e2) {
			}
		}
		return ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	}
	
	public static boolean getImageThumbAndSave(String imagePath, int width, int height, String savepath, String savename){
		Bitmap bitmap = getImageThumb(imagePath, width, height);
		if(bitmap == null)
			return false;
		savebitmap(bitmap, savepath, savename, 100);
		return true;
	}

	public static Bitmap getImageThumb(String imagePath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 关于inJustDecodeBounds的作用将在下文叙述
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
		int h = options.outHeight;// 获取图片高度
		int w = options.outWidth;// 获取图片宽度
		int scaleWidth = w / width; // 计算宽度缩放比
		int scaleHeight = h / height; // 计算高度缩放比
		int scale = 1;// 初始缩放比
		if (scaleWidth < scaleHeight) {// 选择合适的缩放比
			scale = scaleWidth;
		} else {
			scale = scaleHeight;
		}
		if (scale <= 0) {// 判断缩放比是否符合条件
		}
		options.inSampleSize = scale;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把inJustDecodeBounds 设为 false
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	public static boolean saveRecordHistory(LocalRecord localRecord, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SPNames.LocalRecordHistory.getValue(),
				Context.MODE_PRIVATE);
		String jsonstr = sharedPreferences.getString(LocalRecordHistoryItems.LocalHistory.getValue(), null);
		try {
			JSONArray jsonArray = null;
			if (jsonstr == null)
				jsonArray = new JSONArray();
			else
				jsonArray = new JSONArray(jsonstr);
			jsonArray.put(localRecord.getJson());
			sharedPreferences.edit().putString(LocalRecordHistoryItems.LocalHistory.getValue(), jsonArray.toString()).commit();
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
	
	public static List<LocalRecord> loadRecordHistory(Context context){
		return loadRecordHistory(context, null);
	}
	
	public static List<LocalRecord> loadRecordHistory(Context context, List<LocalRecord> ret){
		if(ret == null)
			ret = new ArrayList<LocalRecord>();
		ret.clear();
		SharedPreferences sharedPreferences = context.getSharedPreferences(SPNames.LocalRecordHistory.getValue(),
				Context.MODE_PRIVATE);
		String jsonstr = sharedPreferences.getString(LocalRecordHistoryItems.LocalHistory.getValue(), null);
		try {
			JSONArray jsonArray = null;
			if (jsonstr == null)
				return ret;
			else{
				jsonArray = new JSONArray(jsonstr);
				for(int i = 0; i < jsonArray.length(); i++){
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					LocalRecord localRecord = new LocalRecord();
					if(localRecord.fromJson(jsonObject))
						ret.add(localRecord);
				}
				return ret;
			}
		} catch (JSONException e) {
			return ret;
		}
	}
	public static boolean saveCaptureHistory(LocalCapture localCapture, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SPNames.LocalCaptureHistory.getValue(),
				Context.MODE_PRIVATE);
		String jsonstr = sharedPreferences.getString(LocalCaptureHistoryItems.LocalCapture.getValue(), null);

		try {
			JSONArray jsonArray = null;
			if (jsonstr == null)
				jsonArray = new JSONArray();
			else
				jsonArray = new JSONArray(jsonstr);
			jsonArray.put(localCapture.getJson());
			sharedPreferences.edit().putString(LocalCaptureHistoryItems.LocalCapture.getValue(), jsonArray.toString()).commit();
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
	public static List<LocalCapture> loadCaptureHistory(Context context, List<LocalCapture> ret){
		if(ret == null)
			ret = new ArrayList<LocalCapture>();
		ret.clear();
		SharedPreferences sharedPreferences = context.getSharedPreferences(SPNames.LocalCaptureHistory.getValue(),
				Context.MODE_PRIVATE);
		String jsonstr = sharedPreferences.getString(LocalCaptureHistoryItems.LocalCapture.getValue(), null);
		try {
			JSONArray jsonArray = null;
			if (jsonstr == null)
				return ret;
			else{
				jsonArray = new JSONArray(jsonstr);
				for(int i = 0; i < jsonArray.length(); i++){
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					LocalCapture localCapture = new LocalCapture();
					if(localCapture.fromJson(jsonObject))
						ret.add(localCapture);
				}
				return ret;
			}
		} catch (JSONException e) {
			return ret;
		}
	}
	public static boolean saveWarnHistory(WarnInfo warnInfo, Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SPNames.WarnInfo.getValue(),
				Context.MODE_PRIVATE);
		String jsonstr = sharedPreferences.getString(WarnInfoItems.WarnInfo.getValue(), null);
		System.err.println("in saveWarnHistory: " + warnInfo.toString());
		try {
			JSONArray jsonArray = null;
			if (jsonstr == null)
				jsonArray = new JSONArray();
			else
				jsonArray = new JSONArray(jsonstr);
			jsonArray.put(warnInfo.getJson());
			sharedPreferences.edit().putString(WarnInfoItems.WarnInfo.getValue(), jsonArray.toString()).commit();
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
	public static List<WarnInfo> loadWarnHistory(Context context, List<WarnInfo> ret){
		if(ret == null)
			ret = new ArrayList<WarnInfo>();
		ret.clear();
		SharedPreferences sharedPreferences = context.getSharedPreferences(SPNames.WarnInfo.getValue(),
				Context.MODE_PRIVATE);
		String jsonstr = sharedPreferences.getString(WarnInfoItems.WarnInfo.getValue(), null);
		try {
			JSONArray jsonArray = null;
			if (jsonstr == null)
				return ret;
			else{
				jsonArray = new JSONArray(jsonstr);
				for(int i = 0; i < jsonArray.length(); i++){
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					WarnInfo warnInfo = new WarnInfo();
					if(warnInfo.fromJson(jsonObject))
						ret.add(warnInfo);
				}
				return ret;
			}
		} catch (JSONException e) {
			return ret;
		}
	}
	public static boolean saveCurrentImage(View view, int width, int height, String path)  
	{  
	    Bitmap temBitmap = Bitmap.createBitmap( width, height, Config.ARGB_8888 );    
	    view.setDrawingCacheEnabled(true);
	    view.buildDrawingCache();
	    //从缓存中获取当前屏幕的图片
	    temBitmap = view.getDrawingCache();
	 
	    //输出到sd卡
	    File file = new File(path);  
        try {
            if (!file.exists()) {  
                file.createNewFile();  
            } 
            FileOutputStream foStream = new FileOutputStream(file);
            temBitmap.compress(Bitmap.CompressFormat.PNG, 100, foStream);
            foStream.flush();  
            foStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
	}
}
