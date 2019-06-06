package com.example.jrd48.service.parser;

import java.util.ArrayList;

/**
 * Created by quhuabo on 2016/9/7 0007.
 */

public interface ParserListener {
    //void onGotPackage(final byte[] packData);
    void onGotPackage(final ArrayList<Item> packItems);
}
