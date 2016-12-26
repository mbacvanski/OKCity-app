package com.okcity.okcity.recording;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.List;

public class RecorderRecognizer {

    private SpeechRecognizer sr;
    private static final String TAG = "RecorderRecognizer";
    private SpeechListener speechListener;

    public RecorderRecognizer(Context context, SpeechListener speechListener) {
        this.speechListener = speechListener;
        sr = SpeechRecognizer.createSpeechRecognizer(context);
        sr.setRecognitionListener(new SpeechRecognitionListener());
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.d(TAG, "error " + error);
        }

        public void onResults(Bundle results) {
            Log.d(TAG, "onResults " + results);
            List data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            speechListener.onResults(data);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    void startRecordingIntention() {
        Log.i(TAG, "start recording intention");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.okcity.okcity");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        sr.startListening(intent);
    }

    void destroyEverything() {
        sr.destroy();
    }
}
