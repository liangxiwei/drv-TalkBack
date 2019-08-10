package com.luobin.ui.trajectory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.jrd48.chat.ToastR;
import com.luobin.dvr.R;
import com.luobin.ui.TalkBackSearch.TSConditionActivity;
import com.luobin.ui.trajectory.adapter.CloudTrajectoryAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO 轨迹记录
public class TrackRecordingActivity extends AppCompatActivity {

    @BindView(R.id.btnBack)
    Button btnBack;
    @BindView(R.id.tvStart)
    TextView tvStart;
    @BindView(R.id.tvEnd)
    TextView tvEnd;
    @BindView(R.id.tvTimeText)
    TextView tvTimeText;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tvDel)
    TextView tvDel;
    @BindView(R.id.tvShare)
    TextView tvShare;
    @BindView(R.id.tvMap)
    TextView tvMap;
    @BindView(R.id.tvNote)
    TextView tvNote;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.tvSure)
    TextView tvSure;
    @BindView(R.id.btn)
    RelativeLayout btn;
    @BindView(R.id.text)
    RelativeLayout text;

    private CloudTrajectoryAdapter adapter = null;

    ArrayList<String> datalist =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_recording);
        ButterKnife.bind(this);

        adapter = new CloudTrajectoryAdapter(this, datalist,0);
        list.setAdapter(adapter);

    }

    @OnClick({R.id.btnBack, R.id.tvStart, R.id.tvEnd, R.id.tvTime, R.id.tvDel, R.id.tvShare, R.id.tvMap, R.id.tvNote, R.id.list, R.id.tvSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                //TODO 返回
                break;
            case R.id.tvStart:
                //TODO 开始
                break;
            case R.id.tvEnd:
                //TODO 结束
                break;
            case R.id.tvTime:
                //TODO 时间间隔选择框
                selectTypeDialog();

                break;
            case R.id.tvDel:
                //TODO 删除
                break;
            case R.id.tvShare:
                //TODO 焚香
                break;
            case R.id.tvMap:
                //TODO 地图显示
                break;
            case R.id.tvNote:
                //TODO 备注
                break;
            case R.id.list:
                //TODO 确定
                break;
            case R.id.tvSure:
                //TODO 弹框
                break;
        }
    }
    String[] testData = {"15秒","30秒","60秒"};
    /**
     * 别的选择控件
     */
    OptionsPickerView pvOptions = null;
    private static ArrayList<String> options1Items = null;
    private static ArrayList<ArrayList<String>> options2Items = null;
    protected static ArrayList<ArrayList<ArrayList<String>>> options3Items = null;

    void selectTypeDialog() {
        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String one = "";
                one = options1Items.get(options1);
                tvTime.setText(one);
                //TODO  在这里走设置时间间隔
            }
        }).setLayoutRes(R.layout.dialog_select, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
                tvTitle.setText("设定时间间隔");

                Button btnSure = (Button) v.findViewById(R.id.btnSure);
                btnSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pvOptions.returnData();
                        pvOptions.dismiss();


                    }
                });

            }
        })
                .setDividerColor(getResources().getColor(R.color.dialog_color_line))
                //设置选中项文字颜色
                .setTextColorCenter(getResources().getColor(R.color.text_color_90white))
                .setContentTextSize(20)
                .build();

        options1Items = new ArrayList<>();

        for (String a : Arrays.asList(testData)) {
            options1Items.add(a);
        }
        //二级选择器*/
        pvOptions.setPicker(options1Items);

        pvOptions.show();
    }


}
