package nd.edu.mapresearch;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import java.util.Locale;
import android.widget.Toast;

/**
 * Created by victoriajohnston1 on 24/10/2015.
 */
public class Speech {

    private TextToSpeech TTS; //TTS object
    private int MY_DATA_CHECK_CODE = 0; //status check code
    private String text;
    private Context context;

    // constructor
    public Speech(Context c) {
        context = c;
        TTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    TTS.setLanguage(Locale.UK);
                }
            }
        });
        /*context = c;
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        ((Activity) context).startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);*/
    }

    //speak the user text
    public void speak(String speech) {
        //speak straight away
        TTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    //shut down TTS
    public void shutDown(){
        if(TTS !=null){
            TTS.stop();
            TTS.shutdown();
        }
    }

 /*   //act on result of TTS data check
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                TTS = new TextToSpeech(context, this);
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                context.startActivity(installTTSIntent);
            }
        }
    }

    //setup TTS
    public void onInit(int initStatus) {
        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(TTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                TTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            //Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }*/
}