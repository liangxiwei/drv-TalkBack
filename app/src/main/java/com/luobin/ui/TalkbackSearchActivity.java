package com.luobin.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.luobin.dvr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author wangjunjie
 */
public class TalkbackSearchActivity extends BaseDialogActivity {

//    /**
//     * 输入
//     */
//    @BindView(R.id.edContent)
//    EditText edContent;
//
//    /**
//     * 搜索文本删除
//     */
//    @BindView(R.id.imgSearchDel)
//    ImageView imgSearchDel;
//
//    /**
//     * 搜索
//     */
//    @BindView(R.id.btnSearch)
//    Button btnSearch;
//
//
//    @BindView(R.id.rlEdit)
//    RelativeLayout rlEdit;
//
//    /**
//     * 按条件搜索人
//     */
//    @BindView(R.id.btnSearchPerson)
//    Button btnSearchPerson;
//
//    /**
//     * 搜索车
//     */
//    @BindView(R.id.btnSearchCar)
//    Button btnSearchCar;
//
//    /**
//     * 关闭
//     */
//    @BindView(R.id.imgDel)
//    ImageButton imgDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talkback_search);
        ButterKnife.bind(this);

    }

//    @OnClick({R.id.imgSearchDel, R.id.btnSearch, R.id.btnSearchPerson, R.id.btnSearchCar, R.id.imgDel})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.imgSearchDel:
//                edContent.setText("");
//
//                break;
//            case R.id.btnSearch:
//
//
//                break;
//            case R.id.btnSearchPerson:
//
//                break;
//            case R.id.btnSearchCar:
//
//                break;
//            case R.id.imgDel:
//
//                break;
//            default:
//                break;
//        }
//    }

}
