package com.example.liz.cs125finalmp;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MyActivity";

    ToggleButton toggle; //This will toggle between voice and text mode
    ImageView submissionStatus; //This is the image used to indicate a submission is ready
    TextView lblStatus, outputBox; //The label for the submission-ready indicator, and output textview
    Button editBtn, processBtn, copyBtn; //Edits text submission, processes, and copies output to clipboard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the view objects here to the proper views in the xml
        toggle = (ToggleButton) findViewById(R.id.toggle_input);
        submissionStatus = (ImageView) findViewById(R.id.submission_status);
        lblStatus = (TextView) findViewById(R.id.type_here);

        //Sets the views to listen for clicks
        toggle.setOnClickListener(this);
    }

    public void onClick(View v) {
        Log.d(TAG, "onClick procced");
        if (v.getId() == toggle.getId()) {
            //Toggle button was clicked
            if (toggle.isChecked()) {
                //Toggle button is now on
                Log.d(TAG, "Toggle checked");
                submissionStatus.setVisibility(View.INVISIBLE);
                lblStatus.setVisibility(View.INVISIBLE);
            } else {
                Log.d(TAG, "Toggle not checked");
                submissionStatus.setVisibility(View.VISIBLE);
                lblStatus.setVisibility(View.VISIBLE);
            }
        }
    }
}
