package com.luobin.search.friends.car;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.crash.MyApplication;
import com.luobin.model.CarBrands;
import com.luobin.model.CarFirstType;
import com.luobin.model.CarLastTypes;
import com.luobin.model.CarUpdateResult;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * @Description:用于检查汽车品牌及型号最近更新和同步到本地数据库
 * @Company: robot
 * @createAuthor: XiongChangHui
 * @createDate:2016-6-15 上午10:17:55
 * @lastUpdateAuthor:XiongChangHui
 * @lasetUpdateDate:2016-6-15 上午10:17:55
 */
public class CheckAndUpdateCarTypeThread extends Thread {
    private MyApplication myApplication;
    //private MyAsynHttpClient client;

    public CheckAndUpdateCarTypeThread(MyApplication myApplication) {
        this.myApplication = myApplication;
        //	client = new MyAsynHttpClient(myApplication);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        SharedPreferencesUtils.put(myApplication, "type_code", 1);
//		GlobalData.getInstance().setCheckingCarTypeCode(1);
        DBManagerCarList carListDBM = new DBManagerCarList(myApplication);
        int bandsVersion = carListDBM.getMaxVersionFromCarBands();
        int carFirstTypeVersion = carListDBM.getMaxVersionFromCarFirstTypes();
        int carLastTypeVersion = carListDBM.getMaxVersionFromCarLastTypes();
        carListDBM.closeDB();
        carListDBM = null;
        Log.i("jim", " bandsVersion:" + bandsVersion + " carFirstTypeVersion:" + carFirstTypeVersion + " carLastTypeVersion:" + carLastTypeVersion);
        if (doUpdateBandsAndTypes(bandsVersion, carFirstTypeVersion, carLastTypeVersion)) {
            Log.d("--------------数据缓存耗间:", "" + (System.currentTimeMillis() - startTime));
            SharedPreferencesUtils.put(myApplication, "type_code", 2);
//			GlobalData.getInstance().setCheckingCarTypeCode(2);
        }
        Intent intent = new Intent();
        intent.setAction("CheckAndUpdateCarTypeComplete");
        myApplication.sendBroadcast(intent);
    }

    private boolean doUpdateBandsAndTypes(int bandsVersion, int carFirstTypeVersion, int carLastTypeVersion) {

        ArrayList<CarUpdateResult> data = new ArrayList<CarUpdateResult>();
        try {
            String json = convertString(myApplication.getAssets().open("carBrand.json"), "utf-8");
            data.addAll(JSON.parseArray(json, CarUpdateResult.class));
            DBManagerCarList carListDBM = new DBManagerCarList(myApplication);

//            CarUpdateResult resultCarBandsAndTypes = gson.fromJson(json, CarUpdateResult.class);
            ArrayList<CarBrands> carBrandsList = data.get(0).getCarBrands();
            ArrayList<CarFirstType> carFirstTypes = data.get(0).getCarFirstTypes();
            ArrayList<CarLastTypes> carLastTypes = data.get(0).getCarLastTypes();
            if (carBrandsList != null && !carBrandsList.isEmpty()) {
                carListDBM.insertCarBrands(carBrandsList);
            }
            if (carFirstTypes != null && !carFirstTypes.isEmpty()) {
                carListDBM.insertCarFirstTypes(carFirstTypes);
            }
            if (carLastTypes != null && !carLastTypes.isEmpty()) {
                carListDBM.insertCarLastTypes(carLastTypes);
            }
            carListDBM.closeDB();
//            DBManagerCarList carListDBM1 = new DBManagerCarList(myApplication);
//            ArrayList<CarBrands> date = carListDBM1.getCarBrandsList();
//            carListDBM1.closeDB();
//            if (date != null) {
//                Log.i("jim", "date size:" + data.size());
//            }
//            Log.i("jim", "date:" + (date == null));
//            resultCarBandsAndTypes = null;
            carBrandsList = null;
            carLastTypes = null;
            carFirstTypes = null;
//            data.clear();
            System.gc();

        } catch (Exception e) {
            e.printStackTrace();
        }

//		JSONObject params = new JSONObject();
//		try {
//			System.out.println("token: "+GlobalData.getInstance().getToken());
//			params.put("token", GlobalData.getInstance().getToken());
//			params.put("brandVersion", bandsVersion);
//			params.put("carFirstTypeVersion", carFirstTypeVersion);
//			params.put("carLastTypeVersion", carLastTypeVersion);
//			HttpRequestss http=new HttpRequestss();
//			http=postJson(http, myApplication.getString(R.string.url_checkAndUpdateTypes), params.toString());
//			if (http.mRespondCode == HttpURLConnection.HTTP_OK) {
//				byte[] res = ConvertStreamToByteArray(http.mInStream);
//					if (res == null || res.length < 1) {
//						Log.e("CheckAndUpdateCarTypeThread ", "http.mInStream 为空");
//					}else{
//					    String fileName = getPhotoFileName();
//					    saveFile(new String(res),fileName);
//
//						DBManagerCarList carListDBM=new DBManagerCarList(myApplication);
//						Gson gson=new Gson();
//						Log.i("jim", new String(res));
//						CarUpdateResult resultCarBandsAndTypes=gson.fromJson(new String(res), CarUpdateResult.class);
//						gson=null;
//						ArrayList<CarBrands> carBrandsList=  resultCarBandsAndTypes.getCarBrands();
//						ArrayList<CarFirstType> carFirstTypes=  resultCarBandsAndTypes.getCarFirstTypes();
//						ArrayList<CarLastTypes> carLastTypes=resultCarBandsAndTypes.getCarLastTypes();
//						if(carBrandsList!=null&&!carBrandsList.isEmpty()){
//							carListDBM.insertCarBrands(carBrandsList);
//						}
//						if(carFirstTypes!=null&&!carFirstTypes.isEmpty()){
//							carListDBM.insertCarFirstTypes(carFirstTypes);
//						}
//						if(carLastTypes!=null&&!carLastTypes.isEmpty()){
//							carListDBM.insertCarLastTypes(carLastTypes);
//						}
//						carListDBM.closeDB();
//						resultCarBandsAndTypes=null;
//						carBrandsList=null;
//						carLastTypes=null;
//						carFirstTypes=null;
//						System.gc();
//					}
//
//			} else {
//				GlobalData.getInstance().setCheckingCarTypeCode(0);
//				return false;
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//			GlobalData.getInstance().setCheckingCarTypeCode(0);
//			return false;
//		}
        return true;
    }

    public static String convertString(InputStream is, String charset) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            Log.e("convertString", e.toString());
        }
        return sb.toString();
    }
    /*class HttpRequestss{
        *//**
     * HTTP响应代码.
     *//*
        public int mRespondCode;

		*//**
     * HTTP请求返回 InputStream.
     *//*
		public InputStream mInStream;
		public String mErrorMsg = "";

		public boolean mErrorFlag;
	}
	public  HttpRequestss postJson(HttpRequestss http , String url, String params) {
		int count = 0;// 重连次数
		Log.e("统一资源定位器：", url);
		
		while (count < 2) {
			try {
				// 包含域名或是IP
				HttpURLConnection conn = null;
				URL urls = null;

				try {
					urls = new URL(url);
					conn = (HttpURLConnection) urls.openConnection();
				} catch (Exception e) {
					Log.e("HttpRequest error :" , e.toString());
				}
				Log.e("HttpRequest post OK CONNECTION: " , conn.toString());
				if (null == conn) {
					Log.e("HttpRequest post: ","conn is NULL.");
					return http;
				}
				// 设置是否向connection输出，因为这个是post请求，参数要放在HTTP正文内，因此需要设为true
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setConnectTimeout(20000);
				conn.setReadTimeout(20000);
				conn.setRequestMethod("POST");
				// Post 请求不能使用缓存
				conn.setUseCaches(false);
				conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
				if (null != params) {
					OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
					out.write(params);
					out.flush();
					out.close();
				}

				http.mRespondCode = conn.getResponseCode();
				Log.e("HttpRequest post mRespondCode: " , ""+http.mRespondCode);
				http.mErrorFlag = false;
				http.mInStream = conn.getInputStream();
				Log.e("HttpRequest post InputStream: " , http.mInStream.toString());
				break;
			} catch (Exception e) {
				e.printStackTrace();
				http.mErrorFlag = true;
				http.mErrorMsg = e.toString();
				Log.e("HttpRequest post error, msg ", e.toString());
				if (count == 2) {
					return http;
				}
				// 开始重连
				if (http.mRespondCode == 0 || http.mRespondCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT
						|| http.mRespondCode == HttpURLConnection.HTTP_NOT_FOUND) {
					count++;
				} else {
					count = 2;
				}
			}
		}
		return http;
	}*/

    /**
     * 使用当前系统时间作为上传图片的名称
     *
     * @return 存储的根路径+图片名称
     */
    public static String getPhotoFileName() {  
       /* File file = new File(getPhoneRootPath(context) + FILES_NAME);
        // 判断文件是否已经存在，不存在则创建  
        if (!file.exists()) {  
            file.mkdirs();  
        }  
        // 设置图片文件名称  
        SimpleDateFormat format = new SimpleDateFormat(TIME_STYLE, Locale.getDefault());
//        Date date = new Date(System.currentTimeMillis());
//        String time = format.format(date);
        String timeStamp = format.format(new Date());
        String photoName = "/" + timeStamp + IMAGE_TYPE;
        return file + photoName;  */
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM + "/luobin_1");
            if (!path.exists()) {
                path.mkdirs();
            }
            SimpleDateFormat x = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timeStamp = x.format(new Date());
            String imageFileName = "JPEG_" + timeStamp;
            Log.i("jim", "path: " + path.getAbsolutePath());
            //.getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
            //创建临时文件,文件前缀不能少于三个字符,后缀如果为空默认未".tmp"
            File imagePath = File.createTempFile(
                    imageFileName,  /* 前缀 */
                    ".txt",         /* 后缀 */
                    path      /* 文件夹 */
            );
            return imagePath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 　　* 保存文件
     * 　　* @param toSaveString
     * 　　* @param filePath
     */

    private void saveFile(String str, String path) {

        try {
            FileOutputStream out = new FileOutputStream(path);
            out.write(str.getBytes());
            out.flush();
            out.close();
            Log.i("jim", "已经保存  地址：" + path);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
    
 /*   
  　　public static void saveFile(String toSaveString, String filePath)
  　　{
  　　try
  　　{
  　　File saveFile = new File(filePath);
  　　if (!saveFile.exists())
  　　{
  　　File dir = new File(saveFile.getParent());
  　　dir.mkdirs();
  　　saveFile.createNewFile();
     }

  　　FileOutputStream outStream = new FileOutputStream(saveFile);
  　　outStream.write(toSaveString.getBytes());
  　　outStream.close();
  　　} catch (FileNotFoundException e)
  　　{
  　　e.printStackTrace();
  　　} catch (IOException e)
  　　{
  　　e.printStackTrace();
  　　}
   }
    */


    public byte[] ConvertStreamToByteArray(InputStream inputStream) {
        // ByteArrayOutputStream相当于内存输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        // 将输入流转移到内存输出流中
        try {
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            // 将内存流转换为字符串
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
