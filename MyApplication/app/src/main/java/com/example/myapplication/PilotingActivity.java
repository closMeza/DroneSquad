package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;

import java.io.ByteArrayInputStream;

public class PilotingActivity extends Activity implements ARDeviceControllerListener, ARDeviceControllerStreamListener{
    private static String TAG = PilotingActivity.class.getSimpleName();
    public static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";

    public ARDeviceController deviceController;
    public ARDiscoveryDeviceService service;
    public ARDiscoveryDevice device;

    //The interface that controls the turning
    private SeekBar turnbar;

    //The Button the control the forward movement
    private Button forwardBt;
    //The button that controls the bacwards movement
    private Button backBt;
    //The button that performs the high jump
    private Button jumHightBt;
    //The Button that performs the long jump.
    private Button jumLongBt;

    //The red text that will represent the battery levels
    private TextView batteryLabel;

    //Alerts like "disconnecting" or "connecting"
    private AlertDialog alertDialog;

    //The video stream.
    private ImageView imgView;

    //When you first boot up this "activity" this function is called/
    //Think of it like a contructor for an activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //So save the last activity you performed. In this case it would be
        //The drone selection screen.
        super.onCreate(savedInstanceState);

        //parse the XML file for the layout of this activity.
        //Essentially draw the ui
        setContentView(R.layout.activity_piloting);

        //This is a bunch of initializations for the buttons and ui stuff.
        initIHM();
        //Initialize the imgview video frame
        imgView = (ImageView) findViewById(R.id.imageView);
        //Get the intent that this was called with
        Intent intent = getIntent();
        //Get that extra parameter that was intended to be passed from the previous activity.

        //In this case that would be the drone object that has all the info we need to
        //connect to the drone
        //The service object holds all of this.
        service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

        //create the device
        //This has to do with connecting the wifi broadcasted by the drone to this application.
        try
        {
            device = new ARDiscoveryDevice();

            ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

            device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
        }
        catch (ARDiscoveryException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getError());
        }

        //Once the wifi device has been set up
        //Link the wifi signal to the deviceController object that will handle the controls.
        //SO in order
        //1.inputs from the app call member functions from the deviceController object
        //2.deviceController functions will then use the "device" object and it's member functions
        //to send the signal over the wifi
        //3. The drone on the ground will move and respond based on the signals it received..
        if (device != null)
        {
            try
            {
                //create the deviceController
                deviceController = new ARDeviceController (device);
                deviceController.addListener (this);
                deviceController.addStreamListener(this);
            }
            catch (ARControllerException e)
            {
                e.printStackTrace();
            }
        }
    }

    //This member function will link objects from this class, to the drawn ui elements on the application
    //This will include getting inputs from buttons.
    //Initializing the battery levels.
    //and preparing the seekbar for the turning.
    private void initIHM ()
    {
        jumHightBt = (Button) findViewById(R.id.jumHightBt);
        jumHightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        jumLongBt = (Button) findViewById(R.id.jumLongBt);
        jumLongBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_LONG);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        turnbar = (SeekBar) findViewById(R.id.seekBar3);
        turnbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int turnradius = (i-50)*2;
                deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) turnradius);
                deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                seekBar.setProgress(50,true);
                return;
            }
        });
        //The Forward Button. Duh
        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    //If you are pressing the button DOWN
                    // Then this even triggers
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null){
                            //Set the speed to 50 granted this should be modified when we implement the
                            //variable speed control.
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;
                    //IF you release the button.
                    // Then the action is up
                    case MotionEvent.ACTION_UP:
                        //This adds that little shadow over the button indicating you pushed the button.
                        v.setPressed(false);
                        //If you are connected to a device
                        if (deviceController != null)
                        {
                            //Set the speed to 0
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) -50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null)
                        {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        //Link this textview object called battery label to the textview object in the activity_piloting.xml
        batteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //start the deviceController
        //If it aint null that means it is connected to something.
        if (deviceController != null)
        {
            //This is a popup window that will cover a majority of the screen
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            //Let the user know that we are connection
            alertDialogBuilder.setTitle("Connecting ...");


            // create alert dialog
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            //This isnt really an error. THis will get a state of the program
            ARCONTROLLER_ERROR_ENUM error = deviceController.start();

            //If it is NOT ok, then we close this activity and go to the previous activity.
            //For unexpected disconnects.
            if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)
            {
                finish();
            }
        }
    }
    //This is called whenever we hit the back button, home button, or anything that interrupts the execution of this program.
    //calls, other events that override activities.
    private void stopDeviceController()
    {
        if (deviceController != null)
        {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Disconnecting ...");

            // show it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    ARCONTROLLER_ERROR_ENUM error = deviceController.stop();

                    if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                        finish();
                    }
                }
            });
        }
    }

    @Override
    //Whenever you hit the home button.
    protected void onStop()
    {
        if (deviceController != null)
        {
            deviceController.stop();
        }

        super.onStop();
    }

    @Override
    //Whenever you hit the back button
    public void onBackPressed()
    {
        stopDeviceController();
    }

    //Function to update the battery.
    public void onUpdateBattery(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryLabel.setText(String.format("%d%%", percent));
            }
        });
    }


    @Override
    public void onStateChanged (ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error)
    {
        Log.i(TAG, "onStateChanged ... newState:" + newState + " error: " + error);

        switch (newState)
        {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                //The deviceController is started
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_RUNNING .....");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                    }
                });
                deviceController.getFeatureJumpingSumo().sendMediaStreamingVideoEnable((byte)1);
                break;

            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                //The deviceController is stoped
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_STOPPED ....." );

                deviceController.dispose();
                deviceController = null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                        finish();
                    }
                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error)
    {
        // Nothing to do
        //This has no functionality in our application.
    }


    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary)
    {
        if (elementDictionary != null)
        {
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED)
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null)
                {
                    Integer batValue = (Integer) args.get("arcontroller_dictionary_key_common_commonstate_batterystatechanged_percent");

                    onUpdateBattery(batValue);
                }
            }
        }
        else
        {
            Log.e(TAG, "elementDictionary is null");
        }
    }

    //This doesnt do anything in our program, but we need to provide a definition or else this will not compile
    // so this is just a stub
    //Just gives the controller the ok so it doenst crash.
    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec)
    {
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    //This basically takes the data from the camera in as a bitmap(and array of bits).
    // Then imageview is used to basically provide the "Frame"
    // in which the video is shown. FrameDisplay will be the object that will actually play
    //the images in a manner that will produce a live video feed.
    //ERROR_ENUM is the return type. But in reality it doesnt return an error.
    // It return the "ok" to let the program know that the controller is still good.
    //Otherwise this is a function of the parrot libraries that activates everytime the application
    //receives one "frame" of the video. SO this happens a lot of times per second.
    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame)
    {
        if (!frame.isIFrame())
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR_STREAM;

        byte[] data = frame.getByteData();
        ByteArrayInputStream ins = new ByteArrayInputStream(data);
        Bitmap bmp = BitmapFactory.decodeStream(ins);

        FrameDisplay fDisplay = new FrameDisplay(imgView, bmp);
        fDisplay.execute();

        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }


    //This is for debug purposes. However, if the video drops frames, (which is does very often) it will
    //log the dropped frames.
    @Override
    public void onFrameTimeout(ARDeviceController deviceController)
    {
        Log.i(TAG, "onFrameTimeout ..... ");
    }

}



