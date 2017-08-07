package com.embedsky.httpUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import android.util.Log;


public class httpUtils {
    private static final String LOG_TAG = "Http";
	
	private static final int CONNECT_TIMEOUT = 2*1000;  
    private static final int READ_TIMEOUT = 5*1000;
        
    private static String encoderUTF(String in)
    {
        try {
            return URLEncoder.encode(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    private static String Map2param(Map<String, String> params) throws UnsupportedEncodingException
    {
        StringBuilder param = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        while(iterator.hasNext())
        {
            Entry<String, String> entry = iterator.next();
            param.append(encoderUTF(entry.getKey())+"="+encoderUTF(entry.getValue())+"&");
        }
        param.deleteCharAt(param.length()-1);
        return param.toString();
    }
    
	//callback
	public interface HttpCallBackListener{
		//success
		public void onFinish(String result);
		
		//fail
		public void onError(Exception e);
	}
	
	/**
     * 异步的Get请求
     *
     * @param urlStr 请求url地址
     * @param listener 请求结果回调
     */
    public static void doGetAsyn(final String urlStr, final HashMap<String,String> params, final HttpCallBackListener listener) {
        new Thread() {
            public void run() {
                try {
                    String result = doGet(urlStr,params);
                    if (listener != null) {
                        listener.onFinish(result);
                    }
                } catch (Exception e) {
                    if (listener != null){
                        listener.onError(e);
                    }
                }

            };
        }.start();
    }
    
    /**
     * 异步的Post请求
     * @param urlStr
     * @param params
     * @param listener
     */
    public static void doPostAsyn(final String urlStr, final HashMap<String,String> params, final HttpCallBackListener listener) {
        new Thread() {
            public void run() {
                try {
                    String result = doPost(urlStr, params);
                    if (listener != null) {
                        listener.onFinish(result);
                    }
                } catch (Exception e) {
                    if (listener != null){
                        listener.onError(e);
                    }
                }

            };
        }.start();

    }
    
    /**
     * Get请求，获得返回数据
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String doGet(String url,HashMap<String,String> params) throws UnsupportedEncodingException {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + Map2param(params);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "No-Alive");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
//            connection.setRequestProperty("user-agent",
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + ": " + map.get(key));
            }
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }
        } catch (Exception e) {
            System.out.println("Error in Get" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        //return URLDecoder.decode(result, "UTF-8");
        return result;

    }
    
    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param params
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * @throws UnsupportedEncodingException 
     * @throws Exception
     */
    public static String doPost(String url, HashMap<String,String> params) throws UnsupportedEncodingException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            System.out.println(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
//            conn.setRequestProperty("user-agent",
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(Map2param(params));
            out.flush();
            Map<String, List<String>> map = conn.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + ": " + map.get(key));
            }
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }
        } catch (Exception e) {
            System.out.println("Error in Post"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return URLDecoder.decode(result, "UTF-8");
    }

}
