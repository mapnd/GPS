package nd.edu.mapresearch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Activity that displays the settings screen.
 */
public class SettingsActivity extends Activity {


    private CheckBox animalChkBox;
    private CheckBox roadChkBox;
    private CheckBox policeChkBox;
    private ArrayList<Float> availableOptions = new ArrayList<Float>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_settings);

        final SharedPreferences settings = getSharedPreferences("Choice", MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        availableOptions.add(1f);
        availableOptions.add(2f);
        availableOptions.add(5f);
        availableOptions.add(10f);

        float currentSearchDistance = 12f;
        if (!availableOptions.contains(currentSearchDistance)) {
            //availableOptions.add(currentSearchDistance);
        }
        Collections.sort(availableOptions);

        // The search distance choices
        RadioGroup searchDistanceRadioGroup = (RadioGroup) findViewById(R.id.searchdistance_radiogroup);

        for (int index = 0; index < availableOptions.size(); index++) {
            float searchDistance = availableOptions.get(index);

            RadioButton button = new RadioButton(this);
            button.setId(index);
            button.setText(getString(R.string.settings_distance_format, (int)searchDistance));
            searchDistanceRadioGroup.addView(button, index);

            if (currentSearchDistance == searchDistance) {
                searchDistanceRadioGroup.check(index);
            }
        }

        // Set up the selection handler to save the selection to the application
        searchDistanceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                editor.putFloat("search_distance",availableOptions.get(checkedId));
                editor.commit();
                //Application.setSearchDistance(availableOptions.get(checkedId));
            }
        });
        animalChkBox = (CheckBox) findViewById(R.id.reports_animals);
        animalChkBox.setChecked(settings.getBoolean("animalChkBox",false));
        animalChkBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                editor.putBoolean("animalChkBox", animalChkBox.isChecked());
//                Toast.makeText(getBaseContext(), "animalChkBox: " + animalChkBox.isChecked(), Toast.LENGTH_SHORT).show();
                editor.commit();
            }
        });
        roadChkBox = (CheckBox) findViewById(R.id.reports_road_obstacles);
        roadChkBox.setChecked(settings.getBoolean("roadChkBox",false));
        roadChkBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                editor.putBoolean("roadChkBox", roadChkBox.isChecked());
//                Toast.makeText(getBaseContext(), "roadChkBox: " + roadChkBox.isChecked(), Toast.LENGTH_SHORT).show();
                editor.commit();
            }
        });
        policeChkBox = (CheckBox) findViewById(R.id.reports_police);
        policeChkBox.setChecked(settings.getBoolean("policeChkBox",false));
        policeChkBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                editor.putBoolean("policeChkBox", policeChkBox.isChecked());
//                Toast.makeText(getBaseContext(), "policeChkBox: " + policeChkBox.isChecked(), Toast.LENGTH_SHORT).show();
                editor.commit();
            }
        });
    }
}

