package io.github.alexlondon07.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity" ;
    private TwitterLoginButton twitterLoginButton;
    private LoginButton loginButtonFacebook;
    private CallbackManager callbackManager;
    private BottomSheetDialog bottomSheetDialog;
    private Button buttonDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        twitterLoginButton = findViewById(R.id.login_twitter);
        loginButtonFacebook = findViewById(R.id.login_facebook);
        buttonDialog = findViewById(R.id.button_dialog);

        buttonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });

        
        showCustomDialog();
        loginTwitter();
        loginFacebook();
    }

    private void showCustomDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottomsheet, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);
    }

    private void loginFacebook() {
        loginButtonFacebook.setReadPermissions("public_profile", "email");
        loginButtonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback(){

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        try{
                            String name = object.getString("name");
                            if(name != null){
                                Toast.makeText(MainActivity.this, "Bienvenido: " + name + " a la aplicación", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Nombre del perfil " + name);
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });

                Bundle bundle = new Bundle();
                bundle.putString("fields", "name, email, birthday, picture.type(large)");
                graphRequest.setParameters(bundle);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, R.string.Cancelado, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loginTwitter() {
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();

                TwitterAuthToken authToken = twitterSession.getAuthToken();
                    Call<User> userResult = TwitterCore.getInstance().getApiClient(twitterSession).getAccountService().verifyCredentials(true, true, true);

                    userResult.enqueue(new Callback<User>() {
                        @Override
                        public void success(Result<User> result) {
                            String description = result.data.description;
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode,resultCode,data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
