package com.example.pc.videorecordgl;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.Vector;


import static android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

/**
 * 音视频同步混合器
 * <p>
 * Created by willow.li on 2016/11/4.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class XMediaMuxer {

    static final int TRACK_VIDEO = 0;
    static final int TRACK_AUDIO = 1;

    private MediaMuxer mMediaMuxer;
    private Vector<MuxerData> mMuxerDatas;
    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    private volatile boolean isVideoAdd;
    private volatile boolean isAudioAdd;
    private volatile boolean isMuxerExit       = false;
    private          boolean isMediaMuxerStart = false;
    private          boolean isMediaDataFinish = false;

    private AudioEncoder audioThread;
    private VideoEncoder videoThread;
    private MediaFormat videoMediaFormat;
    private MediaFormat audioMediaFormat;
    private File mOutputFile;

    public XMediaMuxer(
            OutputConfig.VideoOutputConfig mVideoConfig,
            OutputConfig.AudioOutputConfig mAudioConfig,
            File outputFile) {
        try {
            mOutputFile = outputFile;
            mMuxerDatas = new Vector<>();
            mMediaMuxer = new MediaMuxer(mOutputFile.getAbsolutePath(), MUXER_OUTPUT_MPEG_4);
            audioThread = new AudioEncoder(mAudioConfig, this);
            videoThread = new VideoEncoder(mVideoConfig, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /*混合器流程控制================================================================================*/
    public void startMuxer() {
        videoThread.start();
        audioThread.start();
        isMuxerExit = false;
    }

    public void stopMuxer() {
        audioThread.exit();
        videoThread.exit();
        isMuxerExit = true;
        stopMediaMuxer();
    }


    /*混合器关键数据添加============================================================================*/
    /** 返回当前设定的WindowSurface录制层 */
    public Surface getInputSurface() {
        return videoThread.getInputSurface();
    }

    void addMuxerData(@TrackIndex int trackType, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (mMediaMuxer == null || !isMediaMuxerStart || isMuxerExit) return;
        try {
            switch (trackType) {
                case TRACK_AUDIO:
                    mMediaMuxer.writeSampleData(audioTrackIndex, byteBuf, bufferInfo);
                    break;
                case TRACK_VIDEO:
                    mMediaMuxer.writeSampleData(videoTrackIndex, byteBuf, bufferInfo);
                    break;
            }
            byteBuf.clear();
        } catch (IllegalStateException e) {
            LogUtils.w("".getClass(),trackType + "::\n" + e.toString());
        }
    }

    synchronized void addMediaTrack(@TrackIndex int trackType, MediaFormat mediaFormat) {
        if (mMediaMuxer == null) return;
        switch (trackType) {
            case TRACK_AUDIO:
                if (audioMediaFormat == null) {
                    audioMediaFormat = mediaFormat;
                    audioTrackIndex = mMediaMuxer.addTrack(mediaFormat);
                    isAudioAdd = true;
                }
                break;
            case TRACK_VIDEO:
                if (videoMediaFormat == null) {
                    videoMediaFormat = mediaFormat;
                    videoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
                    isVideoAdd = true;
                }
                break;
        }
        requestStart();
    }


    /*混合流程======================================================================================*/
    private void requestStart() {
        LogUtils.i("Muxer".getClass(), "Check should start");
        if (isAudioAdd && isVideoAdd) {
            LogUtils.i("Muxer".getClass(), "Muxer starting");
            mMediaMuxer.start();
            isMediaMuxerStart = true;
            LogUtils.i("Muxer".getClass(), "Muxer started");
        }
    }

    private void stopMediaMuxer() {
        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            isAudioAdd = false;
            isVideoAdd = false;
            isMediaMuxerStart = false;
            mMediaMuxer = null;
            videoMediaFormat = null;
            audioMediaFormat = null;
        }
        if (mMuxerDatas != null) {
            mMuxerDatas.clear();
            mMuxerDatas = null;
        }
        if (mOnFinishListener != null) {
            mOnFinishListener.onFinish(mOutputFile);
        }
    }


    /*类型封装======================================================================================*/
    @IntDef({TRACK_VIDEO, TRACK_AUDIO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TrackIndex {
    }

    /** 封装需要传输的数据类型 */
    public static class MuxerData {
        int                   trackIndex;
        ByteBuffer byteBuf;
        MediaCodec.BufferInfo bufferInfo;

        public MuxerData(@TrackIndex int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            this.byteBuf = byteBuf;
            this.bufferInfo = bufferInfo;
        }
    }


    /*对外暴露监听==================================================================================*/
    /** 最终结果返回监听 */
    private OnFinishListener mOnFinishListener;

    public interface OnFinishListener {
        void onFinish(File mOutputFile);
    }

    public void setOnFinishListener(OnFinishListener mOnFinishListener) {
        if (mOnFinishListener != null) {
            this.mOnFinishListener = mOnFinishListener;
        }
    }

}
