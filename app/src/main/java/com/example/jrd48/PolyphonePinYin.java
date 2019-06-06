package com.example.jrd48;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/20.
 */

public class PolyphonePinYin {
    private static String path = "/assets/duoyinzi_dic.txt";
    private static Map<String, List<String>> pinyinMap = new HashMap<String, List<String>>();

    public static Map<String, List<String>> getPinyinMap() {
        return pinyinMap;
    }

    /**
     * 初始化 所有的多音字词组
     */
    public static void initPinyin() {
        // 读取多音字的全部拼音表;
        InputStream file = PinyinHelper.class.getResourceAsStream(path);
        pinyinMap.clear();
        BufferedReader br = new BufferedReader(new InputStreamReader(file));

        String s = null;
        try {
            while ((s = br.readLine()) != null) {

                if (s != null) {
                    String[] arr = s.split("#");
                    String pinyin = arr[0];
                    String chinese = arr[1];

                    if (chinese != null) {
                        String[] strs = chinese.split(" ");
                        List<String> list = Arrays.asList(strs);
                        pinyinMap.put(pinyin, list);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
