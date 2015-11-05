package nd.edu.mapresearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by JoaoGuilherme on 5/27/2015.
 * Activity that makes the user login
 */
public class LoginActivity extends Activity {
    // View elements
    private EditText username; //username edit text
    private EditText password; //password edit text
    private Button signin; //Sign in button
    private Button register; //register button


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Basic setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        Parse.initialize(getBaseContext(), "lqmx2GmYTOn8of5IM0LrrZ8bYT0ehDvzHTSdGLGA", "Uk4Leh4EpoN0i04lg7fU5yUW7O6UL94RhTdVWfED");

        username = (EditText) findViewById(R.id.registerDialogEditUsername);
        password = (EditText) findViewById(R.id.registerDialogEditPassword);
        signin = (Button) findViewById(R.id.loginButtonSignin);
        register = (Button) findViewById(R.id.loginButtonRegister);

        signin.setOnClickListener(SignInButtonHandler);
        register.setOnClickListener(RegisterButtonHandler);


        // We will check in SharedPreferences if a user was previosly logged in
        final SharedPreferences settings = getSharedPreferences("Choice", MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();
        // Getting previous username and hashed password
        final String previousUsername = settings.getString(Utils.EDITOR_PREVIOUS_USERNAME, "");
        final String previousPasswordHash = settings.getString(Utils.EDITOR_PREVIOUS_PASSWORD_HASH, "");
        final String previousUserID = settings.getString(Utils.EDITOR_PREVIOUS_USER_ID, "");

        if (!previousUserID.equals("")) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Utils.USER_DATA);
            query.getInBackground(previousUserID, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject user, ParseException e) {
                    if (e == null) {
                        if (previousUsername.equals(user.getString(Utils.USER_DATA_USERNAME)) && previousPasswordHash.equals(user.getString(Utils.USER_DATA_PASSWORD_HASH))) {
                            finish();
                            startActivity(createIntentWithUserData(user));
                        }
                    } else {
                        Log.d("LoginActivity", "Error in query: " + e);
                    }
                }
            });
        }

    }

    private final View.OnClickListener SignInButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //DO the Singin!
            final String userString = username.getText().toString();
            final String pass = password.getText().toString();
            Log.d("LoginAcitivity", "Sign In clicked!");
            //get data from server

            if (userString.replaceAll("\\s", "").equals("") || pass.replaceAll("\\s", "").equals("")) {
                // We want to make sure there is anything written in both edit texts
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                return;

            }
            Log.d("LoginAcitivity", "Starting query!");
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Utils.USER_DATA);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        Log.d("LoginAcitivity", "Query done!");
                        if (objects.size() == 0) { //no user on DB
                            Toast.makeText(LoginActivity.this, "Username not found!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        for (ParseObject user : objects) {
                            if (userString.equals(user.getString(Utils.USER_DATA_USERNAME))) { // if we find the username in the DB
                                Log.d("LoginAcitivity", "Found a match!");
                                if (BCrypt.checkpw(pass, user.getString(Utils.USER_DATA_PASSWORD_HASH))) { // check if password matches hash
                                    //Login!
                                    saveInfo(userString, user.getString(Utils.USER_DATA_PASSWORD_HASH), user.getObjectId());
                                    finish();
                                    startActivity(createIntentWithUserData(user));
                                } else {
                                    Toast.makeText(LoginActivity.this, "Wrong password!", Toast.LENGTH_LONG).show();
                                }
                                return;
                            }
                        }
                        // If we got here we did not find the username in the DB
                        Toast.makeText(LoginActivity.this, "Username not found!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "objectRetrievalFailed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    };

    private final View.OnClickListener RegisterButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Make dialog appear
            // Lets set up the register dialog
            LayoutInflater factory = LayoutInflater.from(LoginActivity.this);
            final View registerDialogView = factory.inflate(R.layout.register_dialog, null);

            final AlertDialog registerDialog = new AlertDialog.Builder(LoginActivity.this).create();
            registerDialog.setView(registerDialogView);
            Button confirmButton = (Button)registerDialogView.findViewById(R.id.registerDialogButtonConfirm);
            Button cancelButton = (Button)registerDialogView.findViewById(R.id.registerDialogButtonCancel);

            // This is the handler for the confirm button on dialog
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Get username, password and confirmation from user
                    final String userInput = ((EditText) registerDialogView.findViewById(R.id.registerDialogEditUsername)).getText().toString();
                    final String pass = ((EditText) registerDialogView.findViewById(R.id.registerDialogEditPassword)).getText().toString();
                    final String confirmPass = ((EditText) registerDialogView.findViewById(R.id.registerDialogEditConfirmPassword)).getText().toString();
                    // Check to see if any of them is blank
                    if (userInput.replaceAll("\\s", "").equals("") || pass.replaceAll("\\s", "").equals("") || confirmPass.replaceAll("\\s", "").equals("")) {
                        // We want to make sure there is anything written in both edit texts
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                        return;
                    }
                    // check to see if passwords match
                    if (!pass.equals(confirmPass)) {
                        Toast.makeText(LoginActivity.this, "Passwords do not match!", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        // We have to check if the username is already taken!
                        Log.d("LoginAcitivity", "Doing query!");
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(Utils.USER_DATA);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    Log.d("LoginAcitivity", "Query done!");
                                    if (objects.size() != 0) {
                                        for (ParseObject userObject : objects) {
                                            if (userInput.equals(userObject.getString(Utils.USER_DATA_USERNAME))) {
                                                Toast.makeText(getBaseContext(), "Username already being used!", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                    }
                                    // If not taken, insert into Parse database
                                    Log.d("LoginAcitivity", "Inserting into database!");
                                    final ParseObject newUser = new ParseObject("UserData");
                                    newUser.put(Utils.USER_DATA_USERNAME, userInput);
                                    // We have to hash the password to make it safer to store it
                                    final String hashed = BCrypt.hashpw(pass, BCrypt.gensalt());
                                    newUser.put(Utils.USER_DATA_PASSWORD_HASH, hashed);
                                    Log.d("LoginAcitivity", "Storing: " + userInput + " and " + hashed);
                                    newUser.saveInBackground(new SaveCallback() {
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                // Saved successfully.
                                                Log.d("LoginAcitivity", "Inserted into database!");
                                                Log.d("LoginAcitivity", "ID: "+ newUser.getObjectId());
                                                //Logged In!
                                                saveInfo(userInput, hashed, newUser.getObjectId());
                                                finish();
                                                startActivity(createIntentWithUserData(newUser));
                                            } else {
                                                // The save failed.
                                                Log.d("HHHHHHH", "Error updating user data: " + e);
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(getBaseContext(), "objectRetrievalFailed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    registerDialog.dismiss();
                }
            });
            // handler for the cancel button
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // just close the dialog
                    registerDialog.dismiss();
                }
            });
            registerDialog.show();
        }
    };

    // Method to easily create an intent to MainActivity passing on user data of the user logged in
    private Intent createIntentWithUserData(ParseObject user) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra(Utils.USER_DATA_USERNAME, user.getString(Utils.USER_DATA_USERNAME));
        intent.putExtra(Utils.USER_DATA_USER_ID, user.getObjectId());
        return intent;
    }


    // Method to save user information in the shared preferences
    public void saveInfo(String username, String passwordHash, String userID) {
        final SharedPreferences settings = getSharedPreferences("Choice", MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        editor.putString(Utils.EDITOR_PREVIOUS_USERNAME, username);
        editor.putString(Utils.EDITOR_PREVIOUS_PASSWORD_HASH, passwordHash);
        editor.putString(Utils.EDITOR_PREVIOUS_USER_ID, userID);

        editor.commit();
    }
}
