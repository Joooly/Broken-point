package com.jo.activity.download;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;


public class DownloadPresenter implements DownloadContract.IDownloadPresenter{

    public static final int DOWNLOAD_ERROR = 1;
    public static final int THREAD_DOWNLOAD_ERROR = 2;
    public static final int DOWNLOAD_FINISH = 3;
    public static final int PROGRESS_LOAD=4;
    public static final int PROGRESS_MAX=5;

    private DownloadContract.IDownloadView iDownloadView;
    private DownloadContract.IDownloadModel iDownloadModel;

    public DownloadPresenter(DownloadContract.IDownloadView iDownloadView){

        this.iDownloadView=iDownloadView;
        this.iDownloadModel=new DownloadModel();

    }

    @Override
    public void download() {

        String path=iDownloadView.getPath();
        String num=iDownloadView.getThreadNum();

        if(TextUtils.isEmpty(path)){
            iDownloadView.showNullView("下载地址");
            return;
        }
        if(TextUtils.isEmpty(path)){
            iDownloadView.showNullView("线程数");
            return;
        }

        int threadNum=Integer.parseInt(num);

        iDownloadView.createProgress(threadNum);
        iDownloadView.setButtonEnabled(false);

        Handler handler=new Handler(){

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DOWNLOAD_ERROR:
                        iDownloadView.showErrorView();
                        iDownloadView.setButtonEnabled(true);
                        break;
                    case THREAD_DOWNLOAD_ERROR:
                        String str = (String) msg.obj;
                        iDownloadView.showThreadErrorView(str);
                        iDownloadView.setButtonEnabled(true);
                        break;
                    case DOWNLOAD_FINISH:
                        iDownloadView.showDownloadFinish();
                        iDownloadView.setButtonEnabled(true);
                        break;
                    case PROGRESS_LOAD:
                        iDownloadView.setProgress(msg.arg1,msg.arg2);
                        break;
                    case PROGRESS_MAX:
                        iDownloadView.setProgressMax(msg.arg1,msg.arg2);
                        break;
                }
            }

        };

        iDownloadModel.download(path,threadNum,handler);

    }


}
