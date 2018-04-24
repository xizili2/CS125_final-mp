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
    ImageView recBtn, stopBtn, playBtn; //This is an image view that serves as the record & pause, stop, and play buttons for recording voice notes
    TextView lblStatus, outputBox; //The label for the submission-ready indicator, and output textview
    Button editBtn, processBtn, copyBtn; //Edits text submission, processes, and copies output to clipboard
    Button seeTextBtn, saveVoiceBtn; //Sees transcript of voice note and saves the voice note to the phone


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the view objects here to the proper views in the xml
        toggle = (ToggleButton) findViewById(R.id.toggle_input);
        submissionStatus = (ImageView) findViewById(R.id.submission_status);
        lblStatus = (TextView) findViewById(R.id.submission_status_label);
        outputBox = (TextView) findViewById(R.id.output_view);
        editBtn = (Button) findViewById(R.id.edit_btn);
        processBtn = (Button) findViewById(R.id.process_btn);
        copyBtn = (Button) findViewById(R.id.copy_output_btn);
        recBtn = (ImageView) findViewById(R.id.rec_pause_button);
        stopBtn = (ImageView) findViewById(R.id.stop_button);
        playBtn = (ImageView) findViewById(R.id.play_button);
        seeTextBtn = (Button) findViewById(R.id.see_transcript_button);
        saveVoiceBtn = (Button) findViewById(R.id.save_voice_note_button);

        //Sets the image tags for the imageviews so that their current image can be identified
        recBtn.setTag(R.drawable.record_icon);
        stopBtn.setTag(R.drawable.stop_grey_icon);
        playBtn.setTag(R.drawable.play_grey_icon);

        //Sets the views to listen for clicks
        toggle.setOnClickListener(this);
        outputBox.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        recBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        seeTextBtn.setOnClickListener(this);
        saveVoiceBtn.setOnClickListener(this);
    }

    /**
     * The onClick method that acts whenever a listener view is clicked
     * @param v the view that was clicked
     */
    public void onClick(View v) {
        Log.d(TAG, "onClick procced");
        int id = v.getId(); //ID of the view that was clicked
        if (id == toggle.getId()) {
            //Toggle button was clicked
            toggleClick();
        }
        else if (id == processBtn.getId()) {
            //The process submission button was clicked
            outputBox.setText("Process Button has been clicked!!!!!" + (int)(Math.random() * 10));
        }
        else if (id == editBtn.getId()) {
            //The Edit Text Submission button was clicked
            Log.d(TAG, "Edit Button was clicked");
        }
        else if (id == recBtn.getId()) {
            //The record / pause button was clicked
            recordOrPauseClick();
        }
        else if (id == stopBtn.getId()) {
            //The stop button was clicked
            stopClick();
        }
        else if (id == playBtn.getId()) {
            //The play button was clicked
            Log.d(TAG, "Play button was clicked");
        }
        else if (id == seeTextBtn.getId()) {
            //Transcript button was clicked
            Log.d(TAG, "The button for seeing the transcript was clicked");
        }
        else if (id == saveVoiceBtn.getId()) {
            Log.d(TAG, "The button for saving the voice recoding was clicked");
        }
    }

    /**
     * Switches the mode of the app from text to voice input and vice versa when
     * the toggle button is clicked
     */
    public void toggleClick() {
        if (toggle.isChecked()) {
            //Toggle button is now on, set to text
            Log.d(TAG, "Toggle checked");
            //Make text mode views visible
            editBtn.setVisibility(View.VISIBLE);

            //Make voice mode views invisible
            stopClick();
            recBtn.setVisibility(View.INVISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);
            playBtn.setVisibility(View.INVISIBLE);
            seeTextBtn.setVisibility(View.INVISIBLE);
            saveVoiceBtn.setVisibility(View.INVISIBLE);

            submissionStatus.setVisibility(View.INVISIBLE);
            lblStatus.setVisibility(View.INVISIBLE);
        } else {
            Log.d(TAG, "Toggle not checked");
            //Make voice mode views visible
            recBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.VISIBLE);
            seeTextBtn.setVisibility(View.VISIBLE);
            saveVoiceBtn.setVisibility(View.VISIBLE);

            //Make text mode views invisible
            editBtn.setVisibility(View.INVISIBLE);

            submissionStatus.setVisibility(View.VISIBLE);
            lblStatus.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If no recording is started, it starts recording.
     * If a recording is going, it pauses.
     * If a recording is paused, it resumes.
     */
    public void recordOrPauseClick() {
        int tagId = (Integer)(recBtn.getTag());
        if (tagId == R.drawable.pause_icon) {
            //The image is currently a pause icon
            recBtn.setTag(R.drawable.step_blue_icon);
            recBtn.setImageResource(R.drawable.step_blue_icon);
        }
        else {
            //The image is currently either a step icon or a record icon
            if (tagId == R.drawable.record_icon) {
                stopBtn.setTag(R.drawable.stop_icon);
                stopBtn.setImageResource(R.drawable.stop_icon);
            }
            recBtn.setTag(R.drawable.pause_icon);
            recBtn.setImageResource(R.drawable.pause_icon);
        }
    }

    /**
     * If there is already a recording, stop the recording and become greyed out.
     * Otherwise, does nothing.
     */
    public void stopClick() {
        int tagId = (Integer)(stopBtn.getTag());
        if (tagId == R.drawable.stop_icon) {
            //The image is currently an active stop icon
            stopBtn.setTag(R.drawable.stop_grey_icon);
            stopBtn.setImageResource(R.drawable.stop_grey_icon);
            recBtn.setTag(R.drawable.record_icon);
            recBtn.setImageResource(R.drawable.record_icon);
            playBtn.setTag(R.drawable.play_icon);
            playBtn.setImageResource(R.drawable.play_icon);
        }
    }
}
