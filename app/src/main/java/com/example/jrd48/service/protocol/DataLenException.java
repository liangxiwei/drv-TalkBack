package com.example.jrd48.service.protocol;

/**
 * Created by Administrator on 2016/11/19 0019.
 */

public class DataLenException extends RuntimeException {
    DataLenException(String tip, int needLen, int gotLen){
        super("DataLen exception["+tip+"], need bytes["+needLen+"], but got bytes["+gotLen+"]");
    }
}
