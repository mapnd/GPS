package nd.edu.mapresearch;

import java.util.ArrayList;
import java.util.Locale;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by victoriajohnston1 on 15/11/2015.
 * Used http://www.androidhive.info/2014/07/android-speech-to-text-tutorial/
 */
public class InputSpeech {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Context context;
    private Activity activity;

    public InputSpeech(Activity a,Context c){
        context = c;
        activity = a;
    }

    // showing google speech input dialog
    public void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.speech_prompt));
        try {
            activity.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(context, context.getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void decodeCommand(String command){

    }
}
