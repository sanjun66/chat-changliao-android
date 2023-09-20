package com.legend.common.utils

import android.text.TextUtils
import android.util.Log
import com.legend.base.utils.FileUtils
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader

object ImFileDownloadUtil {
    private const val TAG = "ImFileDownloadUtil"
    private val downloadFileMap = HashMap<String, FileDownloadListener>()

    fun addDownLoad(fileName: String , listener: FileDownloadListener) {
        downloadFileMap[fileName] = listener
    }

    fun removeDownload(fileName: String) {
        downloadFileMap.remove(fileName)
    }

    fun pauseDownload() {
        for (entry in downloadFileMap.entries) {
            FileDownloader.getImpl().pause(entry.value)
        }
        downloadFileMap.clear()
    }

    fun pauseAllDownload() {
        FileDownloader.getImpl().pauseAll()
    }

    fun downloadWithNoProgress(url: String?, path: String, listener: SimpleDownloadListener?) {
        if (TextUtils.isEmpty(url)) return
        val downloadListener = object : FileDownloadListener() {
            override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                Log.i(TAG, "-- download -- pending")

            }

            override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                Log.i(TAG, "-- download -- progress")
            }

            override fun completed(task: BaseDownloadTask?) {
                Log.i(TAG, "-- download -- complete")
                listener?.let {
                    it.onCompleted()
                    removeDownload(FileUtils.getFileName(path))
                }
            }

            override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                Log.i(TAG, "-- download -- paused")
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                Log.i(TAG, "-- download -- fail")
                listener?.onFail()
            }

            override fun warn(task: BaseDownloadTask?) {
                Log.i(TAG, "-- download -- warn")
            }

        }

        addDownLoad(FileUtils.getFileName(path), downloadListener);
        FileDownloader.getImpl().create(url)
            .setPath(path)
            .setListener(downloadListener).start()
    }


    fun downloadWithProgress(url: String?, path: String, listener: DownloadListener?) {
        if (TextUtils.isEmpty(url)) return
        val downloadListener = object : FileDownloadListener() {
            override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                Log.i(TAG, "-- download -- pending")
                listener?.onStart()
            }

            override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                Log.i(TAG, "-- download -- progress")
                listener?.onProgress(soFarBytes / totalBytes * 100)
            }

            override fun completed(task: BaseDownloadTask?) {
                Log.i(TAG, "-- download -- complete")
                listener?.let {
                    it.onCompleted()
                    removeDownload(FileUtils.getFileName(path))
                }
            }

            override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                Log.i(TAG, "-- download -- paused")
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                Log.i(TAG, "-- download -- fail")
                listener?.onFail()
            }

            override fun warn(task: BaseDownloadTask?) {
                Log.i(TAG, "-- download -- warn")
            }

        }

        addDownLoad(FileUtils.getFileName(path), downloadListener);
        FileDownloader.getImpl().create(url)
            .setPath(path)
            .setListener(downloadListener).start()
    }



    interface DownloadListener : SimpleDownloadListener {
        fun onStart()
        fun onProgress(progress: Int)
    }

    interface SimpleDownloadListener {
        fun onCompleted()
        fun onFail()
    }
}