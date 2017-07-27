package com.example.s01144.esp8266_transmit_by_httprequest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.speech.tts.Voice;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String PREF_IP = "PREF_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    private Button buttonPin11,buttonPin12,buttonPin13;
    private EditText editTextIPAddress, editTextPortNumber;

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        buttonPin11 = (Button) findViewById(R.id.buttonPin11);
        buttonPin12 = (Button) findViewById(R.id.buttonPin12);
        buttonPin13 = (Button) findViewById(R.id.buttonPin13);

        buttonPin11.setOnClickListener(this);
        buttonPin12.setOnClickListener(this);
        buttonPin13.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String parameterValue = "";
        String ipAddress = editTextIPAddress.getText().toString().trim();
        String portNumber = editTextPortNumber.getText().toString().trim();

        editor.putString(PREF_IP, ipAddress);
        editor.putString(PREF_PORT, portNumber);

        switch (view.getId()){
            case R.id.buttonPin11:
                parameterValue = "11";
                break;
            case R.id.buttonPin12:
                parameterValue = "12";
                break;
            case R.id.buttonPin13:
                parameterValue = "13";
                break;
            default:
                break;
        }
        // execute HTTP request
        if (ipAddress.length() > 0 && portNumber.length() > 0){
            new HttpRequestAsyncTask(view.getContext(), parameterValue, ipAddress, portNumber, "pin").execute();
        }
    }

    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void>{

        // declare variables needed
        private String requestReply,ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;
        /**
                 * Description: The asyncTask class constructor. Assigns the values used in its other methods.
                 * @param context the application context, needed to create the dialog
                 * @param parameterValue the pin number to toggle
                 * @param ipAddress the ip address to send the request to
                 * @param portNumber the port number of the ip address
                 */
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber){
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context).setTitle("HTTP Response From IP Address:").setCancelable(true).create();
            
        }
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}


