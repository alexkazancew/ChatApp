package chatapplication.alexkazancew.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    public static final String USER_NAME = "user_name";

    AppCompatButton mLoginButton;
    AppCompatEditText mUserNameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mLoginButton = (AppCompatButton) findViewById(R.id.login_button);
        mUserNameEditText = (AppCompatEditText) findViewById(R.id.login_input_editText);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);

                String userName = mUserNameEditText.getText().toString();

                if(userName.isEmpty())
                    mUserNameEditText.setError(getString(R.string.not_empty));
                else
                {
                    intent.putExtra(USER_NAME, userName );
                    startActivity(intent);


                }





            }
        });


    }
}
