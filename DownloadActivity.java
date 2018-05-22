package com.jo.activity.download;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jo.swipelist.R;

import java.util.ArrayList;
import java.util.List;


public class DownloadActivity extends Activity implements DownloadContract.IDownloadView{

    private EditText et_path;
    private EditText et_number;
    private LinearLayout ll_pb;
    private Button button;
    private List<ProgressBar> pbs;

    private DownloadContract.IDownloadPresenter iDownloadPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        et_number = (EditText) findViewById(R.id.et_number);
        et_path = (EditText) findViewById(R.id.et_path);
        ll_pb = (LinearLayout) findViewById(R.id.ll_pb);
        button= (Button) findViewById(R.id.bt);

        iDownloadPresenter=new DownloadPresenter(this);
    }

    public void click(View view) {
        iDownloadPresenter.download();
    }

    @Override
    public void showErrorView() {
        Toast.makeText(getApplicationContext(), "初始化下载失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showThreadErrorView(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDownloadFinish() {
        Toast.makeText(getApplicationContext(), "下载完毕", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setProgress(int index, int load) {
        pbs.get(index).setProgress(load);
    }

    @Override
    public void setProgressMax(int index, int max) {
        pbs.get(index).setMax(max);
    }

    @Override
    public String getPath() {
        return et_path.getText().toString().trim();
    }

    @Override
    public String getThreadNum() {
        return et_number.getText().toString().trim();
    }

    @Override
    public void createProgress(int num) {
        pbs = new ArrayList<>();
        ll_pb.removeAllViews();//移除所有的视图进度条
        for (int i = 0; i < num; i++) {
            ProgressBar pb = (ProgressBar) View.inflate(getApplicationContext(), R.layout.pb_item, null);
            pb.setPadding(5, 5, 5, 5);
            ll_pb.addView(pb, LinearLayout.LayoutParams.MATCH_PARENT, 100);//vie试图int width 宽度int height高度
            pbs.add(pb);//集合里面添加进度条的引用
        }
    }

    @Override
    public void showNullView(String message) {
        Toast.makeText(this, "请输入"+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setButtonEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }
}
