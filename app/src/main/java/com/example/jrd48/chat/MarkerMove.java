package com.example.jrd48.chat;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class MarkerMove {

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 10;
    private static final double DISTANCE = 0.05;

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {
        double interception = point.latitude - slope * point.longitude;
        return interception;
    }


    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;
    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper(final LatLng startPoint, final LatLng endPoint, final Marker mMoveMarker){
        new Thread() {

            public void run() {
//                while (true) {
//                    LatLng startPoint = new LatLng(29.613918, 106.50361);
//                    LatLng endPoint = new LatLng(29.613918, 106.50361);

//                    LatLng startPoint = new LatLng(29.613918, 106.50361);
//                    LatLng endPoint = new LatLng(29.613918, 106.50361);
                    mMoveMarker.setPosition(startPoint);
                    //mMoveMarker.setRotateAngle((float) getAngle(startPoint, endPoint));
                    double slope = getSlope(startPoint, endPoint);
                    //是不是正向的标示（向上设为正向）
                    boolean isReverse = (startPoint.latitude > endPoint.latitude);
                    double intercept = getInterception(slope, startPoint);

                    double xMoveDistance = isReverse ? getXMoveDistance(slope) : -1 * getXMoveDistance(slope);
                    for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse); j = j - xMoveDistance) {
                        LatLng latLng = null;
                        if (slope != Double.MAX_VALUE) {
                            latLng = new LatLng(j, (j - intercept) / slope);
                        } else {
                            latLng = new LatLng(j, startPoint.longitude);
                        }
                        mMoveMarker.setPosition(latLng);
                        try {
                            Thread.sleep(TIME_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
//            }
        }.start();
    }
}
