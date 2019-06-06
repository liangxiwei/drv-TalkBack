package com.example.jrd48.chat.sim;

/**
 * Created by zhouyuhuan on 2018/3/13.
 */

public class SimValue {
    public static String SIM_SERVER_URL = "http://redtea.easyiot.ai:8765";
    public static String API_ORDER_ITEM_BY_CID = "/api/agent/orderItemsByCid";
    public static String API_FLOW_BY_ICCID = "/api/agent/flowByIccid";
    public static String ACCESS_KEY = "d4f679991f2a42bf8448e5d0ddd6db44";
    public static String SECRET_KEY = "bd813fd6291446f2beda0e88a3a8284d";
    public static String VERSION = "v0.1";
    public final static int TYPE_OrderItemsByCid = 7;
    public final static int TYPE_FlowByIccid = 8;
    public final static int TYPE_SearchServer = 1;
    public static String TRANID = "tranId";
    public static String RETURN_CODE = "returnCode";
    public static String DATA = "data";
    public static String ORDER_ITEM_LIST = "orderItemList";
    public static String ID = "id";
    public static String USING_STATUS = "usingStatus";
    public static String CID = "cid";
    public static String PID = "pid";
    public static String COUNT = "count";
    public static String ACTIVE_START_TIME = "activeStartTime";
    public static String ACTIVE_END_TIME = "activeEndTime";
    public static String NOT_ACTIVE_LOST_TIME = "notActiveLostTime";
    public static String PERIOD_TOTAL ="periodTotal";
    public static String PERIOD_TYPE = "periodType";
    public static String FLOW_TOTAL = "flowTotal";
    public static String SUBSCRIBE_TYPE = "subscribeType";
    public static String TOTAL_BYTES_CNT = "totalBytesCnt";
    public static String UPDATE_TIME = "updateTime";
    public static String SIM_SERVER_URL_LUOBIN = "http://irobbing.com/luobin-new/getFlow";
    public static String KEY_ICCID = "iccid";
    public static String SELF_TOTAL_BYTES_CNT = "totalBytesCnt";//已经使用
    public static String SELF_FLOW_TOTAL = "flowTotal";//总共流量
    public static String SELF_FLOW_SURPLUS = "flowSurplus";//剩余流量

}
