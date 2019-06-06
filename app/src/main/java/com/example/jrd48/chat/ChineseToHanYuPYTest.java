package com.example.jrd48.chat;

import com.example.jrd48.PolyphonePinYin;
import com.example.jrd48.chat.search.NameMatchPinYing;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汉字转拼音，能处理多音字
 *
 * @author feng bingbing
 */

public class ChineseToHanYuPYTest {

    private static Map<String, List<String>> pinyinMap = new HashMap<String, List<String>>();
    private static long count = 0;

    public ChineseToHanYuPYTest() {
        try {


//        String str = "中国银行长沙分行,重庆市市长长辈的长子正在举行第20届举重演讲,长途汽车";
//            String str = "市长长得高,长城,重庆举重，";
//        initPinyin("/assets/duoyinzi_dic.txt");
            pinyinMap.clear();
            pinyinMap = PolyphonePinYin.getPinyinMap();
//            String py = convertChineseToPinyin(str, false);
//            System.out.println(str + " = " + py);
//            Log.i("jim", "全拼 :" + str + " = " + py);
//
//            String py1 = convertChineseToPinyin(str, true);
//            System.out.println(str + " = " + py1);
//            Log.i("jim", "简拼：" + str + " = " + py1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将某个字符串的首字母 大写
     *
     * @param str
     * @return
     */
    public static String convertInitialToUpperCase(String str) {
        if (str == null) {
            return null;
        }

       /* StringBuffer sb = new StringBuffer();
        char[] arr = str.toCharArray();
        for(int i=0;i<arr.length;i++){
            char ch = arr[i];
            if(i==0){
                sb.append(String.valueOf(ch).toUpperCase());
            }else{
                sb.append(ch);
            }
        }

        return sb.toString();*/
        return str;
    }

    /**
     * 将某个字符串的首字母 大写
     *
     * @param str
     * @return
     */
    public static String convertToUpperCase(String str) {
        if (str == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            if (i == 0) {
                sb.append(String.valueOf(ch).toUpperCase());
            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    /**
     * 汉字转拼音 最大匹配优先
     *
     * @param chinese
     * @return
     */
    public static String convertChineseToPinyin(String chinese, boolean isHeadChar) {

        checkPinYinMapIsEmpty();

        StringBuffer pinyin = new StringBuffer();

        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        char[] arr = chinese.toCharArray();

        for (int i = 0; i < arr.length; i++) {

            char ch = arr[i];

            if (ch > 128) { // 非ASCII码
                // 取得当前汉字的所有全拼
                try {

                    String[] results = PinyinHelper.toHanyuPinyinStringArray(
                            ch, defaultFormat);

                    if (results == null || results.length <= 0) {  //非中文
                        pinyin.append(arr[i]);
//                        return "";
                    } else {

                        int len = results.length;

                        if (len == 1) { // 不是多音字

                            //                          pinyin.append(results[0]);
                            String py = results[0];
                            if (py.contains("u:")) {  //过滤 u:
                                py = py.replace("u:", "v");
//                                System.out.println("filter u:" + py);
                            }
                            if (isHeadChar) {
                                pinyin.append(convertInitialToUpperCase(py).charAt(0));
                            } else {
                                pinyin.append(convertInitialToUpperCase(py));
                            }
                        } else if (results[0].equals(results[1])) {    //非多音字 有多个音，取第一个

                            //                          pinyin.append(results[0]);
                            if (isHeadChar) {
                                pinyin.append(convertInitialToUpperCase(results[0]).charAt(0));
                            } else {
                                pinyin.append(convertInitialToUpperCase(results[0]));
                            }
                        } else { // 多音字

//                            System.out.println("多音字：" + ch);

                            int length = chinese.length();

                            boolean flag = false;

                            String s = null;

                            List<String> keyList = null;

                            for (int x = 0; x < len; x++) {

                                String py = results[x];

                                if (py.contains("u:")) {  //过滤 u:
                                    py = py.replace("u:", "v");
//                                    System.out.println("filter u:" + py);
                                }

                                keyList = pinyinMap.get(py);

                                if (i + 3 <= length) {   //后向匹配2个汉字  大西洋
                                    s = chinese.substring(i, i + 3);
                                    if (keyList != null && (keyList.contains(s))) {
                                        //                                  if (value != null && value.contains(s)) {

//                                        System.out.println("last 2 > " + py);
                                        //                                      pinyin.append(results[x]);
                                        if (isHeadChar) {
                                            pinyin.append(convertInitialToUpperCase(py).charAt(0));
                                        } else {
                                            pinyin.append(convertInitialToUpperCase(py));
                                        }
                                        flag = true;
                                        break;
                                    }
                                }

                                if (i + 2 <= length) {   //后向匹配 1个汉字  大西
                                    s = chinese.substring(i, i + 2);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("last 1 > " + py);
                                        //                                      pinyin.append(results[x]);
                                        if (isHeadChar) {
                                            pinyin.append(convertInitialToUpperCase(py).charAt(0));
                                        } else {
                                            pinyin.append(convertInitialToUpperCase(py));
                                        }
                                        flag = true;
                                        break;
                                    }
                                }

                                if ((i - 2 >= 0) && (i + 1 <= length)) {  // 前向匹配2个汉字 龙固大
                                    s = chinese.substring(i - 2, i + 1);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("before 2 < " + py);
                                        //                                      pinyin.append(results[x]);
                                        if (isHeadChar) {
                                            pinyin.append(convertInitialToUpperCase(py).charAt(0));
                                        } else {
                                            pinyin.append(convertInitialToUpperCase(py));
                                        }
                                        flag = true;
                                        break;
                                    }
                                }

                                if ((i - 1 >= 0) && (i + 1 <= length)) {  // 前向匹配1个汉字   固大
                                    s = chinese.substring(i - 1, i + 1);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("before 1 < " + py);
                                        //                                      pinyin.append(results[x]);
                                        if (isHeadChar) {
                                            pinyin.append(convertInitialToUpperCase(py).charAt(0));
                                        } else {
                                            pinyin.append(convertInitialToUpperCase(py));
                                        }
                                        flag = true;
                                        break;
                                    }
                                }

                                if ((i - 1 >= 0) && (i + 2 <= length)) {  //前向1个，后向1个      固大西
                                    s = chinese.substring(i - 1, i + 2);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("before last 1 <> " + py);
                                        //                                      pinyin.append(results[x]);
                                        if (isHeadChar) {
                                            pinyin.append(convertInitialToUpperCase(py).charAt(0));
                                        } else {
                                            pinyin.append(convertInitialToUpperCase(py));
                                        }
                                        flag = true;
                                        break;
                                    }
                                }
                            }

                            if (!flag) {    //都没有找到，匹配默认的 读音  大

                                s = String.valueOf(ch);

                                for (int x = 0; x < len; x++) {

                                    String py = results[x];

                                    if (py.contains("u:")) {  //过滤 u:
                                        py = py.replace("u:", "v");
//                                        System.out.println("filter u:");
                                    }

                                    keyList = pinyinMap.get(py);

                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("default = " + py);
                                        //                                      pinyin.append(results[x]);  //如果不需要拼音首字母大写 ，直接返回即可
                                        if (isHeadChar) {
                                            pinyin.append(convertInitialToUpperCase(py).charAt(0));
                                        } else {
                                            pinyin.append(convertInitialToUpperCase(py));
                                        }//拼音首字母 大写
                                        break;
                                    }
                                }
                            }
                        }
                    }

                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyin.append(arr[i]);
            }
        }
        return pinyin.toString();
    }

    private static void checkPinYinMapIsEmpty() {
        if (pinyinMap == null || pinyinMap.size() <= 0) {
            PolyphonePinYin.initPinyin();
            pinyinMap = PolyphonePinYin.getPinyinMap();
        }
    }

    /**
     * 初始化 所有的多音字词组
     *
     * @param fileName
     */
    public static void initPinyin(String fileName) {
        // 读取多音字的全部拼音表;
        InputStream file = PinyinHelper.class.getResourceAsStream(fileName);

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

    public static NameMatchPinYing getNameMatchPinYing(String chinese) {
        checkPinYinMapIsEmpty();
        NameMatchPinYing mNameMatchPinYing = new NameMatchPinYing();
        List<String> pingStr = new ArrayList<String>();
        List<String> nameStr = new ArrayList<String>();
        StringBuffer pinyin = new StringBuffer();

        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        char[] arr = chinese.toCharArray();

        for (int i = 0; i < arr.length; i++) {

            char ch = arr[i];
            nameStr.add(ch + "");
            if (ch > 128) { // 非ASCII码
                // 取得当前汉字的所有全拼
                try {

                    String[] results = PinyinHelper.toHanyuPinyinStringArray(
                            ch, defaultFormat);

                    if (results == null || results.length <= 0) {  //非中文
                        pinyin.append(arr[i]);
                        pingStr.add(arr[i] + "");
//                        return "";
                    } else {

                        int len = results.length;

                        if (len == 1) { // 不是多音字

                            //                          pinyin.append(results[0]);
                            String py = results[0];
                            if (py.contains("u:")) {  //过滤 u:
                                py = py.replace("u:", "v");
//                                System.out.println("filter u:" + py);
                            }
                            pinyin.append(convertInitialToUpperCase(py));
                            pingStr.add(py);
                        } else if (results[0].equals(results[1])) {    //非多音字 有多个音，取第一个

                            //                          pinyin.append(results[0]);
                            pinyin.append(convertInitialToUpperCase(results[0]));
                            pingStr.add(results[0]);
                        } else { // 多音字

//                            System.out.println("多音字：" + ch);

                            int length = chinese.length();

                            boolean flag = false;

                            String s = null;

                            List<String> keyList = null;

                            for (int x = 0; x < len; x++) {

                                String py = results[x];

                                if (py.contains("u:")) {  //过滤 u:
                                    py = py.replace("u:", "v");
//                                    System.out.println("filter u:" + py);
                                }

                                keyList = pinyinMap.get(py);

                                if (i + 3 <= length) {   //后向匹配2个汉字  大西洋
                                    s = chinese.substring(i, i + 3);
                                    if (keyList != null && (keyList.contains(s))) {
                                        //                                  if (value != null && value.contains(s)) {

//                                        System.out.println("last 2 > " + py);
                                        //                                      pinyin.append(results[x]);
                                        pinyin.append(convertInitialToUpperCase(py));
                                        pingStr.add(py);
                                        flag = true;
                                        break;
                                    }
                                }

                                if (i + 2 <= length) {   //后向匹配 1个汉字  大西
                                    s = chinese.substring(i, i + 2);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("last 1 > " + py);
                                        //                                      pinyin.append(results[x]);
                                        pinyin.append(convertInitialToUpperCase(py));
                                        pingStr.add(py);
                                        flag = true;
                                        break;
                                    }
                                }

                                if ((i - 2 >= 0) && (i + 1 <= length)) {  // 前向匹配2个汉字 龙固大
                                    s = chinese.substring(i - 2, i + 1);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("before 2 < " + py);
                                        //                                      pinyin.append(results[x]);
                                        pinyin.append(convertInitialToUpperCase(py));
                                        pingStr.add(py);
                                        flag = true;
                                        break;
                                    }
                                }

                                if ((i - 1 >= 0) && (i + 1 <= length)) {  // 前向匹配1个汉字   固大
                                    s = chinese.substring(i - 1, i + 1);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("before 1 < " + py);
                                        //                                      pinyin.append(results[x]);
                                        pinyin.append(convertInitialToUpperCase(py));
                                        pingStr.add(py);
                                        flag = true;
                                        break;
                                    }
                                }

                                if ((i - 1 >= 0) && (i + 2 <= length)) {  //前向1个，后向1个      固大西
                                    s = chinese.substring(i - 1, i + 2);
                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("before last 1 <> " + py);
                                        //                                      pinyin.append(results[x]);
                                        pinyin.append(convertInitialToUpperCase(py));
                                        pingStr.add(py);
                                        flag = true;
                                        break;
                                    }
                                }
                            }

                            if (!flag) {    //都没有找到，匹配默认的 读音  大

                                s = String.valueOf(ch);

                                for (int x = 0; x < len; x++) {

                                    String py = results[x];

                                    if (py.contains("u:")) {  //过滤 u:
                                        py = py.replace("u:", "v");
//                                        System.out.println("filter u:");
                                    }

                                    keyList = pinyinMap.get(py);

                                    if (keyList != null && (keyList.contains(s))) {

//                                        System.out.println("default = " + py);
                                        //                                      pinyin.append(results[x]);  //如果不需要拼音首字母大写 ，直接返回即可
                                        pinyin.append(convertInitialToUpperCase(py));//拼音首字母 大写
                                        pingStr.add(py);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyin.append(arr[i]);
                pingStr.add(arr[i] + "");
            }
        }
        mNameMatchPinYing.setName(nameStr);
        mNameMatchPinYing.setPinyinlist(pingStr);
        mNameMatchPinYing.setPinyin(pinyin.toString());
        return mNameMatchPinYing;
    }
}
