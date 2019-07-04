package com.frd.hjc.videoplay1;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by HJC on 2019/7/3.
 */

public class MyCodec {
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private String MIME;
    private static final String LogString = "MediaCoder";

    private boolean eosReceived;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean readVideoFile(String url ,Surface surface){
        Surface surface1 = surface;
        System.out.println("read video start: " + surface1.hashCode());
        eosReceived = false;
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(url);
            for (int i = 0; i < extractor.getTrackCount(); i++){
                MediaFormat format = extractor.getTrackFormat(i);
                MIME = format.getString(MediaFormat.KEY_MIME);
                if(MIME.startsWith("video/")){
                    extractor.selectTrack(i);
                    decoder = MediaCodec.createDecoderByType(MIME);
                    decoder.configure(format, surface1, null, 0);
                    decoder.start();
                    System.out.println("dispatch decoder success: "  + surface1.hashCode());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void playVideo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startMs;
                System.out.println("start play video");
                ByteBuffer[] inputBuffers = decoder.getInputBuffers();
                ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                while(!Thread.interrupted()){
                    startMs = System.currentTimeMillis();
                    if(!eosReceived){
                        int inIndex = decoder.dequeueInputBuffer(10000);
                        if (inIndex > 0){
                            ByteBuffer buffer = inputBuffers[inIndex];
                            int sampleSize = extractor.readSampleData(buffer, 0);
                            if (sampleSize < 0){
                                Log.e(LogString, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                                decoder.queueInputBuffer(inIndex, 0, 0,
                                        0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                eosReceived = true;
                            }else {
                                //                       System.out.println("start read video");
                                decoder.queueInputBuffer(inIndex, 0, sampleSize,
                                        extractor.getSampleTime(), 0);
                                extractor.advance();
                            }
                        }
                    }

                    int outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000);
                    switch(outIndex){
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            outputBuffers = decoder.getOutputBuffers();
                            break;
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            break;
                        default:
//                    System.out.println("start draw video");
//                            System.out.println("一帧解码已经开始");
                            ByteBuffer buffer = outputBuffers[outIndex];
                            decoder.releaseOutputBuffer(outIndex, true);
//                            while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
//                                try {
//                                    Thread.sleep((long) (41.7 - (System.currentTimeMillis() - startMs)));
//                                    System.out.println("时间戳1： " + bufferInfo.presentationTimeUs);
//                                    System.out.println("时间戳2： " + (System.currentTimeMillis() - startMs));
//
//                                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 ){
//                                        System.out.println("这是 I 帧");
//                                    }
////                                    System.out.println("一帧解码已经完成");
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }

                            // frame rate control , about 24 frame/s
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 ){
                                long displayTime = System.currentTimeMillis();
                                long frameTime = startMs - displayTime;
                                if (frameTime <= 41.7){
                                    try {
                                        System.out.println("一帧解码已经完成");
                                        Thread.sleep((long) (41.7 -frameTime));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;

                    }

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                        break;
                    }
                }
                decoder.stop();
                decoder.release();
                extractor.release();
            }
        }).start();
    }

    public void close(){
        eosReceived = true;
    }
}
