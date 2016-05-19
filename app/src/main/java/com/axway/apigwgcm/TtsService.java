package com.axway.apigwgcm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;

import com.axway.apigwgcm.util.StringUtil;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by su on 12/10/2014.
 */
public class TtsService extends BaseIntentService implements TextToSpeech.OnInitListener {

    private static final String TAG = TtsService.class.getSimpleName();

    public static final String ACTION_SPEAK = "speak";

    public static final int MSG_START_SPEAKING = 101;
    public static final int MSG_STOP_SPEAKING = 102;

    private static final float RATE_SLOWEST = 0.1f;
    private static final float RATE_SLOW = 0.5f;
    private static final float RATE_NORMAL = 1.0f;
    private static final float RATE_FAST = 1.5f;
    private static final float RATE_FASTEST = 2.0f;

    private static final float PITCH_HIGHEST = 2.0f;
    private static final float PITCH_HIGH = 1.5f;
    private static final float PITCH_NORMAL = 1.0f;
    private static final float PITCH_LOW = 0.5f;
    private static final float PITCH_LOWEST = 0.1f;

    private CountDownLatch latch;
    private boolean ttsInit;

    public TtsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d(TAG, "onHandleIntent");
        super.onHandleIntent(intent);
        String action = intent.getAction();
        if (ACTION_SPEAK.equals(action)) {
            speak(intent);
        }
    }

    private void speak(final Intent intent) {
        String s = intent.getStringExtra("params");
        String txt = null;
        float rate = RATE_NORMAL;
        float pitch = PITCH_NORMAL;
        ttsInit = false;
        if (!TextUtils.isEmpty(s)) {
            String[] pairs = s.split("&");
            for (String p: pairs) {
                String[] kv = p.split("=");
                if (kv.length == 2) {
                    String k = kv[0];
                    String v = kv[1];
                    if ("text".equals(k))
                        txt = v;
                    else if ("rate".equals(k))
                        rate = decodeRate(v);
                    else if ("pitch".equals(k))
                        pitch = decodePitch(v);
                }
            }
        }
        if (TextUtils.isEmpty(txt)) {
            Log.d(TAG, "Nothing to say");
            return;
        }
        latch = new CountDownLatch(1);
        Log.d(TAG, "initializing TTS service");
        TextToSpeech tts = new TextToSpeech(this, this);
        awaitLatch();
        latch = null;
        if (ttsInit) {
            tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setOnUtteranceProgressListener(getProgListener());
            latch = new CountDownLatch(1);
            String utid = Integer.toString(txt.hashCode());
            Log.d(TAG, StringUtil.format("speaking: %s, %.2f, %.2f, %s", txt, pitch, rate, utid));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Bundle args = new Bundle();
                args.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utid);
                tts.speak(txt, TextToSpeech.QUEUE_ADD, args, txt);
            } else {
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utid);
                tts.speak(txt, TextToSpeech.QUEUE_ADD, params);
            }
            awaitLatch();
        }
        Log.d(TAG, "shutting down TTS service");
        tts.shutdown();
    }

    private float decodeRate(String in) {
        if ("slowest".equals(in))
            return RATE_SLOWEST;
        if ("slow".equals(in))
            return RATE_SLOW;
        if ("fast".equals(in))
            return RATE_FAST;
        if ("fastest".equals(in))
            return RATE_FASTEST;
        return RATE_NORMAL;
    }

    private float decodePitch(String in) {
        if ("highest".equals(in))
            return PITCH_HIGHEST;
        if ("high".equals(in))
            return PITCH_HIGH;
        if ("low".equals(in))
            return PITCH_LOW;
        if ("lowest".equals(in))
            return PITCH_LOWEST;
        return PITCH_NORMAL;
    }

    private void awaitLatch() {
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            Log.d(TAG, "interrupted while awaiting latch");
        }
    }

    private UtteranceProgressListener getProgListener() {
        return new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d(TAG, String.format("Utterance.onStart %s", utteranceId));
            }
            @Override
            public void onDone(String utteranceId) {
                Log.d(TAG, String.format("Utterance.onDone %s", utteranceId));
                if (latch != null)
                    latch.countDown();
            }
            @Override
            public void onError(String utteranceId) {
                Log.d(TAG, String.format("Utterance.onError %s", utteranceId));
                if (latch != null)
                    latch.countDown();
            }
        };
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsInit = true;
            Log.i(TAG, "tts initialized");
        }
        else {
            ttsInit = false;
            Log.e(TAG, "tts initialization failed");
        }
        if (latch != null)
            latch.countDown();
    }
}
