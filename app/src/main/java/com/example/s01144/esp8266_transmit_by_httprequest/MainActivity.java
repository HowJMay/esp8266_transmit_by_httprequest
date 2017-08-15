package com.example.s01144.esp8266_transmit_by_httprequest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.speech.tts.Voice;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    public final static String PREF_IP = "192.168.4.1";
    public final static String PREF_PORT = "80";
    private Button buttonHIGH, buttonMEDIUM, buttonLOW, buttonAUTO, buttonSend;
    private EditText editTextIPAddress, editTextPortNumber;
    private SeekBar seekBar;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        buttonHIGH = (Button) findViewById(R.id.high);
        buttonMEDIUM = (Button) findViewById(R.id.medium);
        buttonLOW = (Button) findViewById(R.id.low);
        buttonAUTO = (Button) findViewById(R.id.auto);
        buttonSend = (Button) findViewById(R.id.send);


        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(seekBarOnSeekBarChange);

        // assign text inputs
        editTextIPAddress = (EditText)findViewById(R.id.editTextIPAddress);
        editTextIPAddress.setText("192.168.4.1");
        editTextPortNumber = (EditText)findViewById(R.id.editTextPortNumber);
        editTextPortNumber.setText("80");

        buttonHIGH.setOnClickListener(this);
        buttonMEDIUM.setOnClickListener(this);
        buttonLOW.setOnClickListener(this);

        // get the IP address and port number from the last time the user used the app,
        // put an empty string "" is this is the first time.
        editTextIPAddress.setText(sharedPreferences.getString(PREF_IP,""));
        editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT,""));
    }

    @Override
    public void onClick(View view) {
        String parameterValue = "";
        String ipAddress = editTextIPAddress.getText().toString().trim();
        String portNumber = editTextPortNumber.getText().toString().trim();

        editor.putString(PREF_IP, ipAddress);
        editor.putString(PREF_PORT, portNumber);

        int speed = 0;
        switch (view.getId()){
            case R.id.high:
                parameterValue = "high";
                speed = 100;
                break;
            case R.id.medium:
                parameterValue = "medium";
                speed = 50;
                break;
            case R.id.low:
                parameterValue = "low";
                speed = 10;
                break;
            case R.id.auto:
                parameterValue = "auto";
                speed = 80;
                break;
            case R.id.send:
                parameterValue = "send";
                speed = seekBar.getProgress();
                break;
            default:
                break;
        }
        // execute HTTP request
        if (ipAddress.length() > 0 && portNumber.length() > 0){
            new HttpRequestAsyncTask(view.getContext(), parameterValue, ipAddress, portNumber, "pin").execute();
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarOnSeekBarChange = new SeekBar.OnSeekBarChangeListener()
    {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            //停止拖曳時觸發事件
            Toast.makeText(MainActivity.this, Integer.toString(seekBar.getProgress()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            //開始拖曳時觸發事件
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            //拖曳途中觸發事件，回傳參數 progress 告知目前拖曳數值

        }
    };


    /**
         * Description: Send an HTTP Get request to a specified ip address and port.
         * Also send a parameter "parameterName" with the value of "parameterValue".
         * @param parameterValue the pin number to toggle
         * @param ipAddress the ip address to send the request to
         * @param portNumber the port number of the ip address
         * @param parameterName
         * @return The ip address' reply text, or an ERROR message is it fails to receive one
         */
    public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName){
        String serverResponse = "ERROR";

        try {

            HttpClient httpClient = new DefaultHttpClient(); // create an HTTP client
            // define the URL e.g. http://myIpaddress:myport/?pin=13 (to toggle pin 13 for example)
            URI website = new URI("http://" + ipAddress + ":" + portNumber + "/?" + parameterName + "=" + parameterValue);
            HttpGet getRequest = new HttpGet();// create an HTTP GET object
            getRequest.setURI(website);// set the URL of the GET request
            HttpResponse response = httpClient.execute(getRequest); // execute the request
            // get the ip address server's reply
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            serverResponse = bufferedReader.readLine();
            //Close connection
            content.close();

        } catch (IOException e){
            serverResponse = e.getMessage();
        } catch (URISyntaxException e){
            serverResponse = e.getMessage();
        } catch (Exception e){
            serverResponse = e.getMessage();
        }

        return serverResponse;
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
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter){
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context).setTitle("HTTP Response From IP Address:").setCancelable(true).create();
            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;
        }
        /**
                 * Name: doInBackground
                 * Description: Sends the request to the ip address
                 * @param voids
                 * @return
                 */
        @Override
        protected Void doInBackground(Void... voids) {
            alertDialog.setMessage("Data sent, waiting for reply from server...");
            if (!alertDialog.isShowing()){
                alertDialog.show();
            }
            requestReply = sendRequest(parameterValue, ipAddress, portNumber, parameter);

            return null;
        }

        /**
                 * Name: onPostExecute
                 * Description: This function is executed after the HTTP request returns from the ip address.
                 * The function sets the dialog's message with the reply text from the server and display the dialog
                 * if it's not displayed already (in case it was closed by accident);
                 * @param aVoid void parameter
                 */
        @Override
        protected void onPostExecute(Void aVoid) {
            alertDialog.setMessage(requestReply);
            if (!alertDialog.isShowing()){
                alertDialog.show();;
            }
        }
        /**
                 * Name: onPreExecute
                 * Description: This function is executed before the HTTP request is sent to ip address.
                 * The function will set the dialog's message and display the dialog.
                 */
        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Sending data to server, please wait...");
            if (!alertDialog.isShowing()){
                alertDialog.show();
            }
        }
    }
}


