package com.example.jrd48.chat.sim;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by zhouyuhuan on 2018/3/12.
 */


public class AsyncFetchSimInfoTask extends AsyncTask<String, Integer, String> {
    public static final String TAG = "AsyncFetchSimInfoTask";
    private ISimStatusListener mListener;
    private Context mContext;

    public AsyncFetchSimInfoTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params != null && params.length > 0) {
            boolean isApiAdd = Boolean.parseBoolean(params[0]);
            String url = params[1];
            String body = params[2];
            try {
                if(isApiAdd){
                    return SimUrlBuilder.searchSimInfo(url,body);
                }else{
                    return SimUrlBuilder.getHttpResponse(url,"GET",null, 15000, 15000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mListener != null && !isCancelled()) {
            mListener.onSimResult(result, null);
        }
    }

    public void setListener(ISimStatusListener listener) {
        mListener = listener;
    }

}

