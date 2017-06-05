package com.embedsky.httpUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class httpUtils {
	
	//require time
	private static final int TIMEOUT_IN_MILLIONS = 5000;
	
	//token
	public static final String HTTP_HEADER_PARAM = "token";
	
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
     * @param urlStr
     * @return
     * @throws Exception
     */
    public static String doGet(String urlStr,HashMap<String,String> params) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            StringBuilder sb = null;
            if (params != null) {
                //将HashMap<String,String> params转化成name1=value1&name2=value2 的形式
                sb = toRequestString(params);
                }
            // 将url整合成携带参数的url：http://192.168.1.35:8080/MyWeb/MyServlet?name=changj&age=10
            url = new URL(String.format(urlStr+"?%s",sb));
            // 打开和URL之间的连接
            conn = (HttpURLConnection) url.openConnection();
            //设置读取超时、连接超时
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            //设置请求方式
            conn.setRequestMethod("GET");
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("content-Type","application/json;charset=UTF-8");
            conn.setRequestProperty(HTTP_HEADER_PARAM,"1234");
            if (conn.getResponseCode() == 200) {
                //读取服务器返回的数据流
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len ;
                byte[] buf = new byte[128];
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return URLDecoder.decode(baos.toString(), "UTF-8");
            } else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
            }
            conn.disconnect();
        }

        return null ;

    }
    
    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param urlStr
     *            发送请求的 URL
     * @param params
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * @throws UnsupportedEncodingException 
     * @throws Exception
     */
    public static String doPost(String urlStr, HashMap<String,String> params) throws UnsupportedEncodingException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL url = new URL(urlStr);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置请求方式
            conn.setRequestMethod("POST");
            //设置读取超时、连接超时
            conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty(HTTP_HEADER_PARAM,"1234");
            conn.setUseCaches(false);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            if (params != null) {
                //将HashMap<String,String> params转化成name1=value1&name2=value2 的形式
                StringBuilder sb = toRequestString(params);
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(sb);
                // flush输出流的缓冲
                out.flush();
              }
            // 定义BufferedReader输入流来读取URL的响应
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            	in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                  }
            }else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }
         } catch (Exception e) {
            e.printStackTrace();
          }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
		return URLDecoder.decode(result, "UTF-8");
		
    }
    
    /**
     * 将map类型转换成name1=value1&name2=value2 的形式
     * @param map
     * @return
     */
    private static StringBuilder toRequestString(HashMap<String,String> map){
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,String> entry = (Map.Entry<String,String>)it.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(val);
            sb.append("&");
        }
        //sb.deleteCharAt(sb.length()-1);
        return sb;
    }

}
