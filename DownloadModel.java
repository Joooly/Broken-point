package com.jo.activity.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.jo.activity.download.DownloadPresenter.DOWNLOAD_ERROR;
import static com.jo.activity.download.DownloadPresenter.DOWNLOAD_FINISH;
import static com.jo.activity.download.DownloadPresenter.PROGRESS_LOAD;
import static com.jo.activity.download.DownloadPresenter.PROGRESS_MAX;
import static com.jo.activity.download.DownloadPresenter.THREAD_DOWNLOAD_ERROR;


public class DownloadModel implements DownloadContract.IDownloadModel {

    private int threadCount = 4;
    private int livingThread = 0;

    private String path;
    private Handler handler;

    @Override
    public void download(final String path, int threadNum, final Handler handler) {
        threadCount = threadNum;
        this.path = path;
        this.handler = handler;
        new Thread() {
            public void run() {
                RandomAccessFile raf = null;
                HttpURLConnection conn = null;

                try {

                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int length = conn.getContentLength(); //服务器文件的长度

                    raf = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getFileName(path), "rw");
                    raf.setLength(length);
                    Log.i("DownloadModel", "文件的总长度:" + length);

                    int blockSize = length / threadCount;//计算每个线程下载的位置
                    Log.i("DownloadModel", "每一个块的平均长度:" + blockSize);
                    livingThread = threadCount;

                    for (int i = 0; i < threadCount; i++) {
                        int startIndex = i * blockSize;
                        int endIndex = (i + 1) * blockSize - 1;
                        if ((i+1) == threadCount) {
                            endIndex = length-1;
                        }

                        Message msg = Message.obtain();
                        msg.what = PROGRESS_MAX;
                        msg.arg1 = i;
                        msg.arg2 = endIndex - startIndex;
                        handler.sendMessage(msg);

                        new Thread(new DownLoadTask(i, startIndex, endIndex)).start();

                    }

                } catch (Exception e) {
                    Message msg = Message.obtain();
                    msg.what = DOWNLOAD_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                } finally {

                    if (null != raf) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    if (null != conn) {
                        conn.disconnect();
                    }
                }
            }
        }.start();

    }

    /**
     * 获取保存的路径
     *
     * @param path
     * @return
     */
    private static String getFileName(String path) {
        int start = path.lastIndexOf("/") + 1;
        return path.substring(start);
    }

    private class DownLoadTask implements Runnable {
        private int id;
        private int startIndex;
        private int endIndex;

        public DownLoadTask(int id, int startIndex, int endIndex) {
            super();
            this.id = id;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public void run() {
            InputStream is = null;
            RandomAccessFile raf = null;
            HttpURLConnection conn = null;
            FileInputStream fis = null;
            BufferedReader br = null;
            try {
                int downloadCount = 0;

                URL url = new URL(path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                //在下载之前检查是否已经下载大小的记录文件
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getFileName(path) + id + ".txt");
                if (f.exists() && f.length() > 0) {// 查看存储在本地是否有记录下载进度的文本，有的话读取进度
                    fis = new FileInputStream(f);
                    br = new BufferedReader(new InputStreamReader(fis));
                    downloadCount = Integer.parseInt(br.readLine());//已经下载的大小
                    startIndex += downloadCount;
                }

                conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);// 注意要与服务端配合，这是传给服务端的参数

                Log.i("DownloadModel", "返回的请求代码："+conn.getResponseCode());
                Log.i("DownloadModel", "线程id:" + id + "下载的位置:" + startIndex + "-" + endIndex);

                raf = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getFileName(path), "rwd");
                raf.seek(startIndex);// 指定线程下载存放文件的开始位置

                int total = 0;// 记录当前线程下载的大小

                is = conn.getInputStream();
                int len;
                byte[] buffer = new byte[1024*10];
                while ((len = is.read(buffer)) != -1) {

                    raf.write(buffer, 0, len);
                    total += len;

                    // 记录下载的进度
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getFileName(path) + id + ".txt");
                    RandomAccessFile positionRaf = new RandomAccessFile(file, "rwd");
                    positionRaf.write(String.valueOf(total).getBytes());
                    positionRaf.close();

                    Message msg = Message.obtain();
                    msg.what = PROGRESS_LOAD;
                    msg.arg1 = id;
                    msg.arg2 = total + downloadCount;
                    handler.sendMessage(msg);
                }

                Log.i("DownloadModel", "线程:" + id + "下载完毕...");

            } catch (Exception e) {

                Message msg = Message.obtain();
                msg.what = THREAD_DOWNLOAD_ERROR;
                msg.obj = "线程:" + id + "下载失败.";
                handler.sendMessage(msg);

                e.printStackTrace();
            } finally {

                synchronized (DownloadModel.this) {
                    livingThread--;

                    if (livingThread <= 0) {
                        Log.i("DownloadModel", "全部下载完毕");

                        Message msg = Message.obtain();
                        msg.what = DOWNLOAD_FINISH;
                        handler.sendMessage(msg);

                        // 将记录进度的文件删除
                        for (int i = 0; i < threadCount; i++) {
                            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getFileName(path) + i + ".txt");
                            f.delete();
                        }
                    }
                }

                if (null != conn) {
                    conn.disconnect();
                }

                if (null != fis) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != br) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                if (null != raf) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != is) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

}
