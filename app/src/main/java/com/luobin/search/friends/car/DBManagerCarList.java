package com.luobin.search.friends.car;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.luobin.model.CarBrands;
import com.luobin.model.CarFirstType;
import com.luobin.model.CarLastTypes;
import com.luobin.model.ViewAllCar;
import com.luobin.search.friends.car.listForSelectUsed.PinYinCompare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DBManagerCarList {
    private DBHelperCarList helper;
    private SQLiteDatabase db;
    private static final String NAME = "carmsg";

    public DBManagerCarList(Context context) {
        helper = new DBHelperCarList(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * 添加用户信息
     */
    public void add(List<ViewAllCar> userCars) {
        db.beginTransaction(); // 开始事务
        try {
            db.delete(NAME, null, null);
            for (ViewAllCar c : userCars) {

                db.execSQL("INSERT INTO " + NAME + " VALUES(?, ?, ?, ?, ?,?)",

                        new Object[]{c.getUserCarID(), c.getCarNumAll(), c.getCarFirstTypeName(), c.getCarLastTypeName(), c.getCarDefault(),
                                c.getFriendUserID()});
                Log.i("mFrined", c.getUserCarID() + "");
            }
            db.setTransactionSuccessful(); // 设置事务成功完成
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    /**
     * update person's age
     *
     * @param person
     */
    // public void updateAge(TrackIem person) {
    // ContentValues cv = new ContentValues();
    // cv.put("age", person.age);
    // db.update("person", cv, "name = ?", new String[]{person.name});
    // }

    /**
     * 删除用户数据库里的数据
     *
     * @param
     */
    public void deleteUserCar(long userCarId) {
        db.delete(NAME, "userCarId == ?", new String[]{String.valueOf(userCarId)});
    }

    /**
     * 查找用户信息的Cursor
     *
     * @return
     */
    public Cursor queryUserCarTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM " + NAME, null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        if (db.isOpen())
            db.close();
    }

    public List<ViewAllCar> getUserCar() {
        Cursor c = db.rawQuery("SELECT * FROM " + NAME, null);
        List<ViewAllCar> userCars = new ArrayList<ViewAllCar>();
        ViewAllCar userCar = null;
        try {
            while (c.moveToNext()) {
                userCar = new ViewAllCar();
                userCar.setCarNumAll(c.getString(c.getColumnIndex("carNumAll")));
                userCar.setCarFirstTypeName(c.getString(c.getColumnIndex("carFirstTypeName")));
                userCar.setCarLastTypeName(c.getString(c.getColumnIndex("carLastTypeName")));
                userCar.setUserCarID(c.getLong(c.getColumnIndex("userCarId")));
                userCar.setCarDefault(c.getInt(c.getColumnIndex("carDefault")));
                userCar.setFriendUserID(c.getLong(c.getColumnIndex("friend_user_id")));
                userCars.add(userCar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return userCars;
    }

    /**
     * @return ArrayList<CarBrands>
     * @Title:
     * @Description: 查询所有的汽车品牌
     * @createAuthor: XiongChangHui
     * @date:2016-6-14 下午5:51:01
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-14 下午5:51:01
     */
    public ArrayList<CarBrands> getCarBrandsList(boolean isHasCarBrand) {
        Cursor c = db.rawQuery("SELECT * FROM carBands", null);
        ArrayList<CarBrands> carBrands = new ArrayList<CarBrands>();
        CarBrands carBrand = null;
        try {
            while (c.moveToNext()) {
                carBrand = new CarBrands();
                carBrand.setCarBrandID(c.getInt(c.getColumnIndex("carBrandId")));
                carBrand.setCarBrandName(c.getString(c.getColumnIndex("carBrandName")));
                carBrand.setVersion(c.getInt(c.getColumnIndex("version")));
                carBrands.add(carBrand);
                if (isHasCarBrand && carBrands.size() > 0){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        if (!isHasCarBrand) {
            Collections.sort(carBrands, new Comparator<CarBrands>() {

                @Override
                public int compare(CarBrands c1, CarBrands c2) {
                    return PinYinCompare.compare(c1.getCarBrandName(), c2.getCarBrandName());
                }
            });
        }
        return carBrands;
    }

    /**
     * @Title:
     * @Description: 判断数据库里是否有数据
     * @createAuthor: XiongChangHui
     * @date:2016-6-14 下午5:51:01
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-14 下午5:51:01
     */
    public boolean getHasCarBrand() {
        boolean hasCarBrand = false;
        Cursor c = db.rawQuery("SELECT * FROM carBands", null);
        ArrayList<CarBrands> carBrands = new ArrayList<CarBrands>();
        CarBrands carBrand = null;
        try {
            while (c.moveToNext()) {
                carBrand = new CarBrands();
                carBrand.setCarBrandID(c.getInt(c.getColumnIndex("carBrandId")));
                carBrand.setCarBrandName(c.getString(c.getColumnIndex("carBrandName")));
                carBrand.setVersion(c.getInt(c.getColumnIndex("version")));
                carBrands.add(carBrand);
                if (carBrands.size() > 0){
                    hasCarBrand = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return hasCarBrand;
    }

    /**
     * @param carBrandId 品牌id
     * @return ArrayList<CarTypes>
     * @Title:
     * @Description: 查询品牌id为carBrandId的汽车型号
     * @createAuthor: XiongChangHui
     * @date:2016-6-14 下午6:01:02
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-14 下午6:01:02
     */
    public ArrayList<CarFirstType> getCarFristTypesList(int carBrandId) {
        Cursor c = db.rawQuery("SELECT * FROM carFirstTypes where carBrandId ='" + carBrandId + "'", null);
        ArrayList<CarFirstType> carTypes = new ArrayList<CarFirstType>();
        CarFirstType carType = null;
        try {
            while (c.moveToNext()) {
                carType = new CarFirstType();
                carType.setCarFirstTypeID(c.getInt(c.getColumnIndex("carFirstTypeId")));
                carType.setCarBrandID(c.getInt(c.getColumnIndex("carBrandId")));
                carType.setCarFirstTypeName(c.getString(c.getColumnIndex("carFirstTypeName")));
                carType.setVersion(c.getInt(c.getColumnIndex("version")));
                carTypes.add(carType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        Collections.sort(carTypes, new Comparator<CarFirstType>() {

            @Override
            public int compare(CarFirstType c1, CarFirstType c2) {
                return PinYinCompare.compare(c1.getCarFirstTypeName(), c2.getCarFirstTypeName());
            }
        });
        return carTypes;
    }

    /**
     * @param
     * @return
     * @Description:
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-17 下午6:05:53
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-17 下午6:05:53
     */
    public ArrayList<CarLastTypes> getCarLastTypesList(int carFirstTypeId) {
        Cursor c = db.rawQuery("SELECT * FROM carLastTypes where carFirstTypeId ='" + carFirstTypeId + "'", null);
        ArrayList<CarLastTypes> carTypes = new ArrayList<CarLastTypes>();
        CarLastTypes carType = null;
        try {
            while (c.moveToNext()) {
                carType = new CarLastTypes();
                carType.setCarFirstTypeID(c.getInt(c.getColumnIndex("carFirstTypeId")));
                carType.setCarLastTypeID(c.getInt(c.getColumnIndex("carLastTypeId")));
                carType.setCarLastTypeName(c.getString(c.getColumnIndex("carLastTypeName")));
                carType.setVersion(c.getInt(c.getColumnIndex("version")));
                carTypes.add(carType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        Collections.sort(carTypes, new Comparator<CarLastTypes>() {

            @Override
            public int compare(CarLastTypes c1, CarLastTypes c2) {
                return PinYinCompare.compare(c1.getCarLastTypeName(), c2.getCarLastTypeName());
            }
        });
        return carTypes;
    }

    /**
     * @return
     * @Description: 批量插入汽车品牌，使用db的事务
     * List<CarBrands> carBrands
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-14 下午6:21:22
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-14 下午6:21:22
     */
    public void insertCarBrands(List<CarBrands> carBrands) {
        if (!carBrands.isEmpty()) {
            db.beginTransaction();
            CarBrands carBrand;
            for (int i = 0, p = carBrands.size(); i < p; i++) {
                carBrand = carBrands.get(i);
                ContentValues values = new ContentValues();
                values.put("carBrandId", carBrand.getCarBrandID());
                values.put("carBrandName", carBrand.getCarBrandName());
                values.put("version", carBrand.getVersion());
                db.insert("carBands", "_id", values);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            //db.close();
            carBrand = null;
        }
    }

    /**
     * @return
     * @Description: 批量插入汽车品牌，使用db的事务
     * List<CarTypes> carTypes
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-14 下午6:21:22
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-14 下午6:21:22
     */
    public void insertCarFirstTypes(List<CarFirstType> carTypes) {
        if (!carTypes.isEmpty()) {
            db.beginTransaction();
            CarFirstType carType;
            for (int i = 0, p = carTypes.size(); i < p; i++) {
                carType = carTypes.get(i);
                ContentValues values = new ContentValues();
                values.put("carBrandId", carType.getCarBrandID());
                values.put("carFirstTypeId", carType.getCarFirstTypeID());
                values.put("carFirstTypeName", carType.getCarFirstTypeName());
                values.put("version", carType.getVersion());
                db.insert("carFirstTypes", "_id", values);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            //db.close();
            carType = null;
        }
    }

    /**
     * @param
     * @return
     * @Description:
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-17 下午6:09:32
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-17 下午6:09:32
     */
    public void insertCarLastTypes(List<CarLastTypes> carTypes) {
        if (!carTypes.isEmpty()) {
            db.beginTransaction();
            CarLastTypes carType;
            for (int i = 0, p = carTypes.size(); i < p; i++) {
                carType = carTypes.get(i);
                ContentValues values = new ContentValues();
                values.put("carLastTypeId", carType.getCarLastTypeID());
                values.put("carFirstTypeId", carType.getCarFirstTypeID());
                values.put("carLastTypeName", carType.getCarLastTypeName());
                values.put("version", carType.getVersion());
                db.insert("carLastTypes", "_id", values);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            //db.close();
            carType = null;
        }
    }

    /**
     * @param
     * @return int
     * @Description:
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-15 上午10:27:26
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-15 上午10:27:26
     */
    public int getMaxVersionFromCarLastTypes() {
        Cursor c = db.rawQuery("SELECT MAX(version) FROM carLastTypes", null);
        try {
            if (c != null && c.moveToNext()) {
                return c.getInt(c.getColumnIndex("MAX(version)"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return -1;
    }

    /**
     * @param
     * @return
     * @Description:
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-17 下午6:24:56
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-17 下午6:24:56
     */
    public int getMaxVersionFromCarFirstTypes() {
        Cursor c = db.rawQuery("SELECT MAX(version) FROM carFirstTypes", null);
        try {
            if (c != null && c.moveToNext()) {
                return c.getInt(c.getColumnIndex("MAX(version)"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return -1;

    }

    /**
     * @param
     * @return int
     * @Description:
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-15 上午10:36:55
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-15 上午10:36:55
     */
    public int getMaxVersionFromCarBands() {
        Cursor c = db.rawQuery("SELECT MAX(version) FROM carBands", null);
        try {
            if (c != null && c.moveToNext()) {
                return c.getInt(c.getColumnIndex("MAX(version)"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return -1;
    }

    /**
     * @param carBandText 汽车品牌名称
     * @return int
     * @Description:
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-16 下午3:23:28
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-16 下午3:23:28
     */
    public int getCarBandId(String carBandText) {
        Cursor c = db.rawQuery("SELECT * FROM carBands where carBrandName ='" + carBandText + "'", null);
        try {
            if (c.moveToNext()) {
                return c.getInt(c.getColumnIndex("carBrandId"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return -1;
    }
    public void modifyCarBrand(int carBrandId ,String carBrandName){
//        Log.d("ttt","修改车品牌 carBrandId:"+carBrandId+" carBrandName:"+carBrandName);
        ContentValues values = new ContentValues();
        values.put("carBrandId", carBrandId);
        values.put("carBrandName", carBrandName);
        db.update("carBands", values, "carBrandId == ?", new String[]{carBrandId+""});
    }

}