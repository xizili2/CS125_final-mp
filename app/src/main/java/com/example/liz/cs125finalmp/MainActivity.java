package com.example.liz.cs125finalmp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final int EDITTEXT_MIN_LINES = 6; //Setting for the edittext box
    public final int EDITTEXT_MAX_LINES = 20; //Setting for the edittext box

    public static String witResponse; //The response from Wit.ai

    private static final String TAG = "MyActivity"; //For logs
    private boolean isTxtSubmitted = false; //Whether or not a txtSubmission has been submitted yet or not
    private boolean isVoiceSubmitted = false; //Whether or not a txtSubmission has been submitted yet or not

    ToggleButton toggle; //This will toggle between voice and text mode
    ImageView submissionStatus; //This is the image used to indicate a submission is ready
    ImageView recBtn, stopBtn, playBtn; //This is an image view that serves as the record & pause, stop, and play buttons for recording voice notes
    TextView lblStatus, outputBox; //The label for the submission-ready indicator, and output textview
    Button editBtn, processBtn, copyBtn; //Edits text submission, processes, and copies output to clipboard
    Button seeTextBtn, saveVoiceBtn; //Sees transcript of voice note and saves the voice note to the phone

    AlertDialog dialog; //The dialog that appears when recording a textual submission
    String txtSubmission = null; //Current submission
    EditText einput; //Edittext for the dialog

    //The following are necessary for recording and saving a WAV file
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Instantiate an AlertDialog.Builder with its constructor
        //AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppTheme));
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);
        einput = new EditText(MainActivity.this); //Initializes the edittext declared earlier

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.edittext_dialog_message)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    //The Save button is clicked
                    public void onClick(DialogInterface dialog, int which) {
                        txtSubmission = einput.getText().toString();
                        if (txtSubmission != null && !txtSubmission.equals("")) {
                            isTxtSubmitted = true;
                            submissionStatus.setVisibility(View.VISIBLE);
                            lblStatus.setVisibility(View.VISIBLE);
                        } else {
                            isTxtSubmitted = false;
                            submissionStatus.setVisibility(View.INVISIBLE);
                            lblStatus.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .setNeutralButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
                    //The Copy button is clicked
                    public void onClick(DialogInterface dialog, int which) {
                        //Copies the current edittext text to user's clipboard
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("submission currently typed", einput.getText());
                        clipboard.setPrimaryClip(clip);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    //The cancel button is clicked
                    public void onClick(DialogInterface dialog, int which) {
                        einput.setText(txtSubmission);
                    }
                });
        dialog = builder.create(); // Get the AlertDialog from create()
        //einput.setLayoutParams(lp);
        einput.setHint(R.string.type_here_hint);
        einput.setScrollContainer(true);
        einput.setMinLines(EDITTEXT_MIN_LINES);
        einput.setMaxLines(EDITTEXT_MAX_LINES);
        einput.setFocusable(true);
        dialog.setView(einput);

        //This is for initializing all the views and setting their onClicks(), etc.
        setViewsUp();
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
            if (isTxtSubmitted) {
                new RetrieveWitAiTask().execute(txtSubmission);
                outputBox.setText(R.string.make_happy_link);
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.toast_pls_submit,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == editBtn.getId()) {
            //The Edit Text Submission button was clicked
            Log.d(TAG, "Edit Button was clicked");
            dialog.show();
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
     * Does the busywork of onCreate() for initializing and setting up all the view objects
     */
    public void setViewsUp() {
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

        outputBox.setMovementMethod(LinkMovementMethod.getInstance()); //This makes links clickable
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
            if (isTxtSubmitted) { //Checks submission status of text mode
                submissionStatus.setVisibility(View.VISIBLE);
                lblStatus.setVisibility(View.VISIBLE);
            } else {
                submissionStatus.setVisibility(View.INVISIBLE);
                lblStatus.setVisibility(View.INVISIBLE);
            }

            //Make voice mode views invisible
            stopClick();
            recBtn.setVisibility(View.INVISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);
            playBtn.setVisibility(View.INVISIBLE);
            seeTextBtn.setVisibility(View.INVISIBLE);
            saveVoiceBtn.setVisibility(View.INVISIBLE);
        } else {
            Log.d(TAG, "Toggle not checked");
            //Make voice mode views visible
            recBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.VISIBLE);
            seeTextBtn.setVisibility(View.VISIBLE);
            saveVoiceBtn.setVisibility(View.VISIBLE);
            if (isVoiceSubmitted) { //Checks submission status of voice mode
                submissionStatus.setVisibility(View.VISIBLE);
                lblStatus.setVisibility(View.VISIBLE);
            } else {
                submissionStatus.setVisibility(View.INVISIBLE);
                lblStatus.setVisibility(View.INVISIBLE);
            }

            //Make text mode views invisible
            editBtn.setVisibility(View.INVISIBLE);
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

    /**
     * Determines if an emotion is identified int he response form Wit.ai
     * @param json the response
     * @param emotion the emotion to identify
     * @return true if the emotion is found, false otherwise
     */
    public boolean isPresentEmotion(String json, String emotion) {
        return false;
    }

    /**
     * Makes the name and path of a WAV file to save. Checks ahead of time to see if the file directory already exists.
     * @return the new name of the file
     */
    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    /**
     * Makes the temporary name and path of a WAV file to save. Checks ahead of time to see if the file directory already exists.
     * @return the temporary name of the file
     */
    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    /**
     * Begins the recording.
     */
    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    /**
     * Idk, this seems important
     */
    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (Exception e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the recording.
     */
    private void stopRecording(){
        if(null != recorder){
            isRecording = false;

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(),getFilename());
        deleteTempFile();
    }

    /**
     * Deletes the temporary file.
     */
    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    /**
     * Idk, probably copying the WAV file? Can't hurt to keep in here.
     * @param inFilename probably the name of the file to be copied
     * @param outFilename probably the name of the copy
     */
    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.d(TAG, "File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Whewee. I'm not getting into this too deep if I don't have to.
     * @param out idk
     * @param totalAudioLen idk
     * @param totalDataLen seems technical
     * @param longSampleRate 2 technical 4 me
     * @param channels this I sort of understand form lab
     * @param byteRate this sounds familiar
     * @throws Exception if something goes wrong I guess
     */
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws Exception {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
