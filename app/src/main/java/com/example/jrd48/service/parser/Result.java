package com.example.jrd48.service.parser;

import org.apache.commons.lang3.ArrayUtils;

class Result
{
    private byte[] recognized = null;

    private ResultStage succeeded = ResultStage.cached;

    public Result(Result src){
        this.succeeded = src.succeeded;
        this.recognized = ArrayUtils.clone(src.recognized);
    }

    public Result(){

    }

    private Result(byte[] recognized,  ResultStage succeeded) {
        this.recognized = recognized;
        this.succeeded = succeeded;
    }

    /**
     * 是否解析成功
     * @return
     */
    public boolean isParseOk() {
        return succeeded==ResultStage.Ok;
    } 

    /** 获取已经接受的内容 */
    public byte[] get_recognized() {
        return recognized; 
    } 

    public int get_recogized_len() {
        return recognized == null ? 0 : recognized.length;
    }



    /** 加入缓存 */
    public void addBuffer(byte[] buff, int len){
        recognized =  ArrayUtils.addAll(recognized, ArrayUtils.subarray(buff, 0, len));
    }

    public ResultStage getStage(){
        return succeeded;
    }
    public void setStage(ResultStage val) {
        succeeded = val;
    }

    public void reset(){
        recognized = null;
        succeeded = ResultStage.cached;
    };
}