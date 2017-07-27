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


