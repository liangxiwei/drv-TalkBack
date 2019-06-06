package com.luobin.search.friends.car;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.jrd48.chat.ActivityCollector;

import com.luobin.dvr.R;
import com.luobin.search.friends.car.fragment.SelectCarBrandsAndTypesFragment;
import com.luobin.search.friends.car.fragment.SelectFragmentCallbackI;
import com.luobin.utils.ButtonUtils;


public class SelectTypeActivity extends FragmentActivity {

    private TextView titleName;
    /**
     * 1代表需要选择汽车品牌和型号，2代表只需要选择汽车型号
     */
    private int requestCode;
    /**
     * requestBand 0：表示获取品牌和型号  1： 表示只获取品牌
     */
    private int requestBand;
    private Integer fatherCode;
    /**
     * 1代表选择汽车品牌，2代表选择first型号，3代表选择Last型号
     */
    private int currentSelectStep = 0;
    /**
     * 品牌选择Fragment
     */
    private SelectCarBrandsAndTypesFragment bandsFragment;
    /**
     * 型号选择Fragment
     */
    private SelectCarBrandsAndTypesFragment firstTypeFragment;
    /**
     * 型号选择Fragment
     */
    private SelectCarBrandsAndTypesFragment lastTypeFragment;
    /**
     * 记录用户选择的品牌名称
     */
    private String brandName;
    /**
     * 记录用户选择的型号名称
     */
    private String firstTypeName;

    private Dialog mDialog;

    /**
     * 用户接收及处理Fragment中的List的OnItemSelected时间
     */
    private SelectFragmentCallbackI selectFragmentCallbackI = new SelectFragmentCallbackI() {

        @Override
        public void ItemSelectedCallback(String name, int code) {
            switch (currentSelectStep) {
                case 1:
                    brandName = name;
                    currentSelectStep += 1;
                    initFirstTypeFragment(currentSelectStep, code);
                    if (requestBand == 0) {
                        replaceFragment(firstTypeFragment);
                        setTitleByCurrentCode(currentSelectStep);
                    } else {
                        setDateForResultOK("",code);
                    }
                    break;
                case 2:
                    currentSelectStep += 1;
                    firstTypeName = name;
                    setDateForResultOK(name, code);
                    /*initLastTypeFragment(currentSelectStep, code);
                    replaceFragment(lastTypeFragment);
                    setTitleByCurrentCode(currentSelectStep);*/
                    break;
                case 3:
                    setDateForResultOK(name, code);
                    break;
                default:
                    break;
            }

        }

        private void setDateForResultOK(String name, int code) {
            Intent intent = new Intent();
            if (requestBand == 0) {
                if (requestCode == 1) {
                    intent.putExtra("carBandText", brandName);
                }
                intent.putExtra("carTypeText", firstTypeName);
                intent.putExtra("carTypeId", code);
            } else {
                intent.putExtra("carBandText", brandName);
                intent.putExtra("requestBand",1);
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        ActivityCollector.addAct(this);
        initDataFromIntentCode();
        setContentView(R.layout.select_type_activity_container);
        initView();
        initFragmentByRequestCode(requestCode);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        cleanData(bandsFragment);
//        cleanData(firstTypeFragment);
//        cleanData(lastTypeFragment);
//        System.gc();
//        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                ButtonUtils.changeLeftOrRight(true);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                ButtonUtils.changeLeftOrRight(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        ActivityCollector.removeAct(this);
        super.onDestroy();
    }

    private void initView() {
        titleName = (TextView) findViewById(R.id.title_name);
        Button addCarType = (Button) findViewById(R.id.add_carType);
        addCarType.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog = getReportCarTypeAlertDialog(SelectTypeActivity.this, mDialog, getString(R.string.report_car_type), new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String carType = ((EditText) mDialog.getWindow().findViewById(R.id.car_type)).getText().toString();
                        if (null != carType && !TextUtils.isEmpty(carType.trim())) {
                            reportCarTypeNet(carType);
                            hideInput();
                            if (null != mDialog && mDialog.isShowing()) {
                                mDialog.dismiss();
                                mDialog = null;
                            }
                        } else {
                            Toast.makeText(SelectTypeActivity.this, "请按要求填写", Toast.LENGTH_LONG).show();
                        }

                    }
                }, new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (null != mDialog && mDialog.isShowing()) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                        hideInput();
                    }
                }, true);

            }
        });
    }

    /**
     * @param
     * @return
     * @Description: 初始化数据
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-21 上午11:01:57
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-21 上午11:01:57
     */
    private void initDataFromIntentCode() {
        Intent intent = getIntent();
        requestBand = intent.getIntExtra("requestBand", 0);
        requestCode = intent.getIntExtra("requestCode", 1);
        currentSelectStep = requestCode;
        int intFather = intent.getIntExtra("fatherCode", -1);
        fatherCode = intFather == -1 ? null : intFather;
    }

    /**
     * @param
     * @return
     * @Description: 根据请求码初始化第一个Fragment
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-21 上午11:02:43
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-21 上午11:02:43
     */
    private void initFragmentByRequestCode(int code) {
        switch (code) {
            case 1:
                initBrandsFragment();
                replaceFragment(bandsFragment);
                break;
            case 2:
                initFirstTypeFragment(code, fatherCode);
                replaceFragment(firstTypeFragment);
                break;
            default:
                break;
        }
        setTitleByCurrentCode(currentSelectStep);
    }

    private void initFirstTypeFragment(int currentCode, int fatherCode) {
        if (firstTypeFragment == null) {
            firstTypeFragment = new SelectCarBrandsAndTypesFragment(currentCode, fatherCode);
            firstTypeFragment.setSelectFragmentCallbackI(selectFragmentCallbackI);
        } else
            firstTypeFragment.setFatherCode(fatherCode);

    }

    private void initBrandsFragment() {
        if (bandsFragment == null) {
            bandsFragment = new SelectCarBrandsAndTypesFragment(requestCode, fatherCode);
            bandsFragment.setSelectFragmentCallbackI(selectFragmentCallbackI);
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.type_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void initLastTypeFragment(int currentSelectStep2, int fatherCode) {
        if (null == lastTypeFragment) {
            lastTypeFragment = new SelectCarBrandsAndTypesFragment(currentSelectStep2, fatherCode);
            lastTypeFragment.setSelectFragmentCallbackI(selectFragmentCallbackI);
        } else {
            lastTypeFragment.setFatherCode(fatherCode);
        }

    }

    public void back(View v) {
        if (isLastestFragment()) {
            finish();
        }
    }

    private boolean isLastestFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            cleanData(bandsFragment);
            cleanData(firstTypeFragment);
            cleanData(lastTypeFragment);
            System.gc();
            return true;
        } else {
            currentSelectStep -= 1;
            getSupportFragmentManager().popBackStack();
            setTitleByCurrentCode(currentSelectStep);
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (isLastestFragment()) {
            finish();
        }
    }

    /**
     * @param
     * @return
     * @Description: activity 退出时清除数据，释放缓存
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-21 上午11:15:32
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-21 上午11:15:32
     */
    private void cleanData(SelectCarBrandsAndTypesFragment fragment) {
        if (null != fragment) {
            fragment.clearDate();
            fragment = null;
        }
    }

    private void setTitleByCurrentCode(int code) {
        if (code == 1) {
            titleName.setText(getString(R.string.select_car_brand));
        } else {
            titleName.setText(getString(R.string.select_car_type));
        }
    }

    public Dialog getReportCarTypeAlertDialog(Context mContext, Dialog dialog, String title, OnClickListener sureOnClick,
                                              OnClickListener cancelOnClick, Boolean cancelable) {
        if (null == dialog)
            dialog = new Dialog(mContext);
        dialog.setCancelable(cancelable);
        try {
            dialog.show();
        } catch (Exception ex) {
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.getWindow().setContentView(R.layout.report_car_type);
        if (!TextUtils.isEmpty(title)) {
            ((TextView) dialog.getWindow().findViewById(R.id.title)).setText(title);
        } else {
            dialog.getWindow().findViewById(R.id.title).setVisibility(View.GONE);
        }
        if (null != sureOnClick) {
            dialog.getWindow().findViewById(R.id.report).setOnClickListener(sureOnClick);
        }
        if (null != cancelOnClick) {
            dialog.getWindow().findViewById(R.id.cancel).setOnClickListener(cancelOnClick);
        }
        return dialog;
    }

    private void hideInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

    }


    private void reportCarTypeNet(String carType) {
//		SubmitCarTypeParam submitCarTypeParam=new SubmitCarTypeParam();
//		submitCarTypeParam.setCarType(carType);
//		submitCarTypeParam.setToken(GlobalData.getInstance().getToken());
        /*JSONObject json=new JSONObject();
        try {
			json.put("carType", carType);
			json.put("token", GlobalData.getInstance().getToken());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		new MyAsynHttpClient(this).postJson2(GlobalData.getInstance().getUrlSubmitCarType(),json,
				new MyAsynPostListener(this) {

					@Override
					public void onGotSuccess(JSONObject response) {
						Toast.makeText(SelectTypeActivity.this, "提交成功，非常感谢您的反馈，我们将尽快处理，在未处理之前您可以在已有品牌中选择一个替代的型号！",Toast.LENGTH_LONG).show();
					}

					@Override
					public void onGotFailed(JSONObject response) {
						int errorCode=response.optInt("status",-1);
						if(errorCode==ErrorCode.CARTYPE_DOUBLE_SUBMIT){
							Toast.makeText(SelectTypeActivity.this, "提示：已提交，等待处理中！ " ,Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(SelectTypeActivity.this, "错误提示：提交错误 " + response.optString("errorMsg"),Toast.LENGTH_LONG).show();
						}
					}
				});*/
    }
}
