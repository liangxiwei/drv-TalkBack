package com.example.jrd48.chat.sim;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.IccidGetOrSetProcesser;
import com.luobin.dvr.R;
import com.luobin.utils.JsonTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimInfoService extends Service {
    private String TAG = "SimInfoService";
    private Context mContext;
    int ASYNC_TASK_TIMEOUT = 5 * 1000;
    private long TIME_SEARCH_DELAY = 2 * 60 * 1000L;
    private static final int MSG_SHOW_VIEW = 2000;
    private static final int MSG_DISMISS_VIEW = 2001;
    private long mFlowTotal = 0;
    private long mTotalBytesCnt = 0;
    private long mFlowSurplus = -1;
    private final int LIMIT_FLOW = 300;
    private View mView;
    private TextView btnQuit;
    private WindowManager wm;
    private static WindowManager.LayoutParams params;
    public boolean isSearchSimTotal = false;
    public boolean isUpload = true;
    public int uploadTryTimes = 1;
    private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
    private boolean isUseApiAdd = false;
    ISimStatusListener mSimStatusListener = new ISimStatusListener() {
        @Override
        public void onSimResult(String result, String error) {

            Log.d(TAG, "onSimResult == " + result);
            if (result != null && result.length() > 0) {
                if (isUseApiAdd) {
                    String tranId = parseGetId(result);
                    if (tranId != null) {
                        int type = hashMap.get(tranId);
                        Log.d(TAG, "type == " + type);
                        switch (type) {
                            case SimValue.TYPE_OrderItemsByCid:
//                            result = "{\n" +
//                                    "    \"tranId\": \"2017080349454\",\n" +
//                                    "    \"returnCode\": 0,\n" +
//                                    "    \"data\": {\n" +
//                                    "        \"orderItemList\": [\n" +
//                                    "            {\n" +
//                                    "                \"id\": \"201708034945\",\n" +
//                                    "                \"usingStatus\": 1,\n" +
//                                    "                \"cid\": \"TSN201201022\",\n" +
//                                    "                \"pid\": \"2303434224324\",\n" +
//                                    "                \"count\": 2,\n" +
//                                    "                \"activeStartTime\": \"2017-08-12 12:00:00\",\n" +
//                                    "                \"activeEndTime\": \"2017-08-16 12:00:00\",\n" +
//                                    "                \"notActiveLostTime\": \"2017-08-16 12:00:00\",\n" +
//                                    "                \"periodTotal\": 4,\n" +
//                                    "                \"periodType\": 1,\n" +
//                                    "                \"flowTotal\": 102410240,\n" +
//                                    "                \"subscribeType\": 1\n" +
//                                    "            },\n" +
//                                    "            {\n" +
//                                    "                \"id\": \"201708034946\",\n" +
//                                    "                \"usingStatus\": 1,\n" +
//                                    "                \"cid\": \"TSN201201022\",\n" +
//                                    "                \"pid\": \"2303434224324\",\n" +
//                                    "                \"count\": 2,\n" +
//                                    "                \"activeStartTime\": \"2017-08-12 12:00:00\",\n" +
//                                    "                \"activeEndTime\": \"2017-08-16 12:00:00\",\n" +
//                                    "                \"notActiveLostTime\": \"2017-08-16 12:00:00\",\n" +
//                                    "                \"periodTotal\": 4,\n" +
//                                    "                \"periodType\": 1,\n" +
//                                    "                \"flowTotal\": 102410240,\n" +
//                                    "                \"subscribeType\": 1\n" +
//                                    "            }\n" +
//                                    "        ]\n" +
//                                    "    }\n" +
//                                    "}";
                                hashMap.remove(tranId);
                                isSearchSimTotal = false;
                                List<SimOrderItem> resultList = parseOrderItemsJSONResult(result);
                                if (resultList != null && resultList.size() > 0) {
                                    mFlowTotal = 0;
                                    for (SimOrderItem item : resultList) {
                                        mFlowTotal += item.getFlowTotal();
                                    }
                                    Log.d(TAG, "mFlowTotal == " + mFlowTotal);
                                    searchSimFlowByIccid();
                                }
                                break;
                            case SimValue.TYPE_FlowByIccid:
//                            result = "{\n" +
//                                    "    \"tranId\": \"2017080349454\",\n" +
//                                    "    \"returnCode\": 0,\n" +
//                                    "    \"data\": {\n" +
//                                    "        \"totalBytesCnt\": 1024102400,\n" +
//                                    "        \"updateTime\": \"2017-08-01 12:00:00\"\n" +
//                                    "    }\n" +
//                                    "}";

                                hashMap.remove(tranId);
                                mTotalBytesCnt = parseFlowByIccidJSONResult(result);
                                Log.d(TAG, "mTotalBytesCnt == " + mTotalBytesCnt);
                                if (mFlowTotal != 0 && mTotalBytesCnt != 0 && (mFlowTotal - mTotalBytesCnt) < LIMIT_FLOW * 1024 * 1024L) {
                                    mUIHandler.sendEmptyMessage(MSG_SHOW_VIEW);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    boolean parse = parseServerDataJSONResult(result);
                    Log.d(TAG, "mFlowTotal == " + mFlowTotal+",mTotalBytesCnt == " + mTotalBytesCnt+",mFlowSurplus == " + mFlowSurplus);
                    if (parse){
                        if(mFlowSurplus < LIMIT_FLOW * 1024 * 1024L) {
                            mUIHandler.sendEmptyMessage(MSG_SHOW_VIEW);
                            mUIHandler.postDelayed(mSearchRunnable, TIME_SEARCH_DELAY);
                        }else{
                            if(mUIHandler!=null){
                                mUIHandler.removeMessages(MSG_SHOW_VIEW);
                                mUIHandler.removeMessages(MSG_DISMISS_VIEW);
                                mUIHandler.removeCallbacks(mSearchRunnable);
                            }
                            mUIHandler.sendEmptyMessage(MSG_DISMISS_VIEW);
                        }
                    }else{
                        mUIHandler.postDelayed(mSearchRunnable, TIME_SEARCH_DELAY);
                    }
                }
            } else {
                mUIHandler.postDelayed(mSearchRunnable, TIME_SEARCH_DELAY);
            }
        }
    };
    private Runnable mSearchRunnable = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "postDelayed");
            isSearchSimTotal = true;
            searchSimOrderItemsByCid();
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mContext = this;
        registerReceiver();
        findViews();
        mUIHandler.postDelayed(mSearchRunnable, TIME_SEARCH_DELAY);
    }

    private void searchSimOrderItemsByCid() {
        if (!detectStatus(mContext)) {
            Log.w(TAG, "detectStatus == false");
            return;
        }
        String iccid = ConnUtil.getSimIccid(mContext);
        if (iccid == null) {
            Log.w(TAG, "iccid == null");
            return;
        }
        //send iccid
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String tokenLogin = preferences.getString("token", "");
        if (tokenLogin.length() > 1) {
            isUpload = true;
            setICCID(iccid);//send iccid after logined
        }
        //iccid = "8986031749203118555";
        AsyncFetchSimInfoTask fetchSimInfoTask = new AsyncFetchSimInfoTask(SimInfoService.this);
        Log.w(TAG, "isCancelled = " + fetchSimInfoTask.isCancelled());
        fetchSimInfoTask.setListener(mSimStatusListener);
        Log.d(TAG, "iccid = " + iccid);
        if (isUseApiAdd) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            Log.d(TAG, "Order uuid=" + uuid);
            hashMap.put(uuid, SimValue.TYPE_OrderItemsByCid);
            String body = SimUrlBuilder.makeJsonOrderItemsByCid(iccid, uuid);
            startSimInfoTask(fetchSimInfoTask, true/*isApiAdd*/, SimValue.SIM_SERVER_URL + SimValue.API_ORDER_ITEM_BY_CID, body);
        } else {
            //hashMap.put(uuid, SimValue.TYPE_SearchServer);
            if(iccid.length() >= 19){
                iccid= iccid.substring(0,19);
                Log.d(TAG, "sub iccid = " + iccid);
            }
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put(SimValue.KEY_ICCID, iccid);
            String url = SimUrlBuilder.buildGetUrl(SimValue.SIM_SERVER_URL_LUOBIN, paramMap);
            startSimInfoTask(fetchSimInfoTask, false, url, null);
        }
    }

    /*
    * 上传iccid
    * */
    private void setICCID(final String iccid) {
        ProtoMessage.MsgIccid.Builder builder = ProtoMessage.MsgIccid.newBuilder();
        builder.setIccid(iccid);
        builder.setEditCode(ProtoMessage.EditCode.ecUpdate_VALUE);
        MyService.start(mContext, ProtoMessage.Cmd.cmdIccidGetOrSet.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(IccidGetOrSetProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, new TimeoutBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                //ToastR.setToast(mContext, "连接超时");
                Log.e(TAG, "上传iccid 连接超时 uploadTryTimes=" + uploadTryTimes);
                if (isUpload && uploadTryTimes > 0) {
                    uploadTryTimes--;
                    setICCID(iccid);
                }
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.d(TAG, "上传iccid 成功 ");
                } else {
                    Log.e(TAG, "上传iccid 失败 错误吗：" + code);
                }
            }
        });

    }

    private void searchSimFlowByIccid() {
        String iccid = ConnUtil.getSimIccid(mContext);
        if (iccid == null) {
            Log.w(TAG, "iccid == null");
            return;
        }
        AsyncFetchSimInfoTask fetchSimInfoTask = new AsyncFetchSimInfoTask(SimInfoService.this);
        fetchSimInfoTask.setListener(mSimStatusListener);
        Log.d(TAG, "iccid = " + iccid);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Log.d(TAG, "Flow uuid=" + uuid);
        hashMap.put(uuid, SimValue.TYPE_FlowByIccid);
        String body = SimUrlBuilder.makeJsonFlowByIccid(iccid, uuid);
        startSimInfoTask(fetchSimInfoTask, true/*isApiAdd*/, SimValue.SIM_SERVER_URL + SimValue.API_FLOW_BY_ICCID, body);
    }

    private void startSimInfoTask(AsyncFetchSimInfoTask fetchSimInfoTask, boolean isApiAdd, String url, String body) {
        try {
            fetchSimInfoTask.execute(String.valueOf(isApiAdd), url, body);//.get(ASYNC_TASK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            //fetchSimInfoTask.cancel(true);
        }
    }

    private Handler mUIHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SHOW_VIEW:
                    if (!Build.PRODUCT.contains("LB1728")) {
                        createFloatView();
                    }
                    break;
                case MSG_DISMISS_VIEW:
                    isSearchSimTotal = false;
                    dismiss(mView);
                default:
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "!--->onStartCommand: intent " + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver();
        isSearchSimTotal = false;
        isUpload = false;
        dismiss(mView);
        if(mUIHandler!=null){
            mUIHandler.removeMessages(MSG_SHOW_VIEW);
            mUIHandler.removeMessages(MSG_DISMISS_VIEW);
            mUIHandler.removeCallbacks(mSearchRunnable);
        }
    }

    /**
     * 创建悬浮窗
     */
    @SuppressWarnings("deprecation")
    public void createFloatView() {

        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (mView != null && mView.isShown()) {
            return;
        }

        params = new WindowManager.LayoutParams();

        if (params.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBA_8888;
        // resetWindowParamsFlags();
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        /* | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE */

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 0;

        wm.addView(mView, params);//
        if(btnQuit != null){
            Log.d(TAG, "btnQuit--requestFocusFromTouch");
            btnQuit.post(new Runnable() {
                @Override
                public void run() {
                    btnQuit.requestFocusFromTouch();
                }
            });
        }
    }

    private void findViews() {
        mView = View.inflate(mContext, R.layout.pop_setting_help, null);
        TextView content = (TextView) mView.findViewById(R.id.tv_pop_content);
        content.setText(getResources().getString(R.string.pop_title_sim_status_content, LIMIT_FLOW));
        btnQuit = (TextView) mView.findViewById(R.id.tv_i_know);
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnQuit--onQuit--click");
                if(mUIHandler != null){
                    mUIHandler.removeMessages(MSG_SHOW_VIEW);
                    mUIHandler.removeMessages(MSG_DISMISS_VIEW);
                    mUIHandler.removeCallbacks(mSearchRunnable);
                }
                mUIHandler.sendEmptyMessage(MSG_DISMISS_VIEW);
            }
        });
    }

    public void dismiss(View view) {
        if (view == null || !view.isShown()) {
            return;
        }
        Log.d(TAG, "dismiss");
        wm.removeViewImmediate(view);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SIM_STATE_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "!--->mReceiver--onReceive:intent " + intent);
            String action = intent.getAction();
            if (ACTION_SIM_STATE_CHANGED.equals(action)
                    || ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                searchSimOrderItemsByCid();
            }
        }
    };

    private boolean detectStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        int state = tm.getSimState();
        Log.d(TAG, "state = " + state);
        boolean isConnected = ConnUtil.isConnected(mContext);
        Log.d(TAG, "isConnected = " + isConnected + ",isSearchSimTotal=" + isSearchSimTotal);
        if (state == TelephonyManager.SIM_STATE_READY && isConnected && isSearchSimTotal) {
            return true;
        }
        return false;
    }

    private String parseGetId(String result) {
        try {
            JSONObject obj = JsonTool.parseToJSONObject(result);
            String tranId = obj.getString(SimValue.TRANID);
            return tranId;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * "tranId": "2017080349454",
     * "returnCode": 0,
     * "data": {
     * "orderItemList": [
     * {
     * "id": "201708034945",
     * "usingStatus": 1,
     * "cid": "TSN201201022",
     * "pid": "2303434224324",
     * "count": 2,
     * "activeStartTime": "2017-08-12 12:00:00",
     * "activeEndTime": "2017-08-16 12:00:00",
     * "notActiveLostTime": "2017-08-16 12:00:00",
     * "periodTotal": 4,
     * "periodType": 1,
     * "mFlowTotal": 102410240,
     * "subscribeType": 1
     * },
     * {
     * "id": "201708034946",
     * "usingStatus": 1,
     * "cid": "TSN201201022",
     * "pid": "2303434224324",
     * "count": 2,
     * "activeStartTime": "2017-08-12 12:00:00",
     * "activeEndTime": "2017-08-16 12:00:00",
     * "notActiveLostTime": "2017-08-16 12:00:00",
     * "periodTotal": 4,
     * "periodType": 1,
     * "mFlowTotal": 102410240,
     * "subscribeType": 1
     * }
     * ]
     * }
     */
    private List<SimOrderItem> parseOrderItemsJSONResult(String result) {
        try {
            JSONObject obj = JsonTool.parseToJSONObject(result);
            if (0 == obj.getInt(SimValue.RETURN_CODE)) {
                JSONObject data = JsonTool.getJSONObject(obj, SimValue.DATA);
                if (data == null) {
                    Log.e(TAG, "data == null");
                    return null;
                }
                JSONArray orderItemList = JsonTool.getJsonArray(data, SimValue.ORDER_ITEM_LIST);
                List<SimOrderItem> resultList = new ArrayList<SimOrderItem>();
                for (int i = 0; i < orderItemList.length(); i++) {
                    SimOrderItem simSearchResult = new SimOrderItem();
                    JSONObject simObj = JsonTool.getJSONObject(orderItemList, i);
                    simSearchResult.setId(JsonTool.getJsonValue(simObj, SimValue.ID));
                    simSearchResult.setUsingStatus(JsonTool.getJsonValue(simObj, SimValue.USING_STATUS, 1));
                    simSearchResult.setCid(JsonTool.getJsonValue(simObj, SimValue.CID));
                    simSearchResult.setPid(JsonTool.getJsonValue(simObj, SimValue.PID));
                    simSearchResult.setCount(JsonTool.getJsonValue(simObj, SimValue.COUNT, 1));
                    simSearchResult.setActiveStartTime(JsonTool.getJsonValue(simObj, SimValue.ACTIVE_START_TIME));
                    simSearchResult.setActiveEndTime(JsonTool.getJsonValue(simObj, SimValue.ACTIVE_END_TIME));
                    simSearchResult.setNotActiveLostTime(JsonTool.getJsonValue(simObj, SimValue.NOT_ACTIVE_LOST_TIME));
                    simSearchResult.setPeriodTotal(JsonTool.getJsonValue(simObj, SimValue.PERIOD_TOTAL, 4));
                    simSearchResult.setPeriodType(JsonTool.getJsonValue(simObj, SimValue.PERIOD_TYPE, 1));
                    simSearchResult.setFlowTotal(JsonTool.getJsonValue(simObj, SimValue.FLOW_TOTAL, 1));
                    simSearchResult.setSubscribeType(JsonTool.getJsonValue(simObj, SimValue.SUBSCRIBE_TYPE, 1));

                    resultList.add(simSearchResult);
                }
                if (resultList.size() > 0) {
                    return resultList;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {
     * "tranId": "2017080349454",
     * "returnCode": 0,
     * "data": {
     * "totalBytesCnt": 1024102400,
     * "updateTime": "2017-08-01 12:00:00"
     * }
     * }
     */
    private long parseFlowByIccidJSONResult(String result) {
        try {
            JSONObject obj = JsonTool.parseToJSONObject(result);
            if (0 == obj.getInt(SimValue.RETURN_CODE)) {
                JSONObject data = JsonTool.getJSONObject(obj, SimValue.DATA);
                if (data == null) {
                    Log.e(TAG, "data == null");
                    return 0;
                }
                long totalBytesCnt = JsonTool.getJsonValue(data, SimValue.TOTAL_BYTES_CNT, 0);
                String updateTime = JsonTool.getJsonValue(data, SimValue.UPDATE_TIME);
                return totalBytesCnt;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * {
     * "errorMessage":"查询第三方成功",
     * "returnCode":"0",
     * "data":{
     * "totalBytesCnt":0, //适用流量
     * "flowTotal":0,     //总流量
     * "flowSurplus":0    //剩余流量
     * }
     * }
     */
    private boolean parseServerDataJSONResult(String result) {
        try {
            JSONObject obj = JsonTool.parseToJSONObject(result);
            if (0 == obj.getInt(SimValue.RETURN_CODE)) {
                JSONObject data = JsonTool.getJSONObject(obj, SimValue.DATA);
                if (data == null) {
                    Log.e(TAG, "data == null");
                    return false;
                }
                mTotalBytesCnt = JsonTool.getJsonValue(data, SimValue.SELF_TOTAL_BYTES_CNT, 0);
                mFlowTotal = JsonTool.getJsonValue(data, SimValue.SELF_FLOW_TOTAL, 0);
                mFlowSurplus = JsonTool.getJsonValue(data, SimValue.SELF_FLOW_SURPLUS, 0);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}