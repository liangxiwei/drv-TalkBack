package com.luobin.model;

/**
 * 返回数据的基类，主要有两个属性，一个就是status，
 * 一个就是errorMsg
 *
 * @author hugh
 */
public class ResultStatus {

    //状态码，1是true；其他是false
    private int status = 0;

    //错误信息
    private String errorMsg = "";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "ResultStatus [status=" + status + ", errorMsg=" + errorMsg + "]";
    }


}
