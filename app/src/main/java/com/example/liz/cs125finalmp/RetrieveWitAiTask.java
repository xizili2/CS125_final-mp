package com.example.liz.cs125finalmp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * This class is for creating a Curl HTTP Request, sending it to the Wit.ai API, and processing the response received.
 */
public class RetrieveWitAiTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "RetrieveWitAiTask"; //For logs

    protected String doInBackground(String... urls) {
        try {
            String url = "https://api.wit.ai/message"; //First part of request
            String key = "LS3XJFJTIQUM6IM4VSGBLM6SZ6YD2FOS"; //Client-side token copied form Wit.ai app

            String param1 = "20180430"; //Current version ot Wit.ai copied from website 1/5/2018
            String param2 = urls[0]; //Message to send to wit.ai
            String charset = "UTF-8";

            String query = String.format("v=%s&q=%s",
                    URLEncoder.encode(param1, charset),
                    URLEncoder.encode(param2, charset)); //Second part of request

            //Finish making the request and send it to the API
            URLConnection connection = new URL(url + "?" + query).openConnection();
            connection.setRequestProperty ("Authorization", "Bearer " + key);
            connection.setRequestProperty("Accept-Charset", charset);
            InputStream response = connection.getInputStream(); //Send the request, record the response

            //Decoding the response into a JSON string
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d(TAG, "Response from wit.ai: " + result.toString());
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error in sendToWitAi:\n" + e + "\n" + e.getStackTrace().toString());
        }
        return null;
    }

    protected void onPostExecute(String response) {
        MainActivity.witResponse = response;

        // TODO: check this.exception
        // TODO: do something with the feed
    }
}