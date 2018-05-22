package com.jo.activity.download;


import android.os.Handler;

public class DownloadContract {

    public interface IDownloadView{

        /**
         * 显示下载出错视图
         */
        void showErrorView();

        /**
         * 显示线程下载出错视图
         */
        void showThreadErrorView(String str);

        /**
         * 显示下载成功视图
         */
        void showDownloadFinish();

        /**
         * 设置进度条进度
         * @param index
         * @param load
         */
        void setProgress(int index,int load);

        /**
         * 设置进度条最大值
         * @param index
         * @param max
         */
        void setProgressMax(int index,int max);

        /**
         * 获取下载地址
         * @return
         */
        String getPath();

        /**
         * 获取线程数
         * @return
         */
        String getThreadNum();

        /**
         * 创建进度条
         */
        void createProgress(int num);

        /**
         * 显示输入参数为空
         * @param message
         */
        void showNullView(String message);

        /**
         * 设置按钮可否操作
         * @param enabled
         */
        void setButtonEnabled(boolean enabled);

    }

    public interface IDownloadPresenter{

        void download();

    }

    public interface IDownloadModel{

        void download(String path,int threadNum, Handler handler);

    }

}
