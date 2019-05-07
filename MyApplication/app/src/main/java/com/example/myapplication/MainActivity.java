package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

    private static final String TAG = MainActivity.class.getSimpleName();
    static{
        ARSDK.loadSDKLibs();
    }

    private ListView listView;
    private Button connect_button;
    private List<ARDiscoveryDeviceService> deviceList;
    private String[] deviceNameList;

    private ARDiscoveryService ardiscoveryService;
    private boolean ardiscoveryServiceBound = false;
    private ServiceConnection ardiscoveryServiceConnection;
    public IBinder discoverServiceBinder;

    private BroadcastReceiver ardiscoveryServicesDevicesListUpdatedReceiver;
    private int droneSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBroadcastReceiver();
        initServiceConnection();

        listView = (ListView)findViewById(R.id.list_of_drones);
        connect_button = (Button)findViewById(R.id.Connect);

        deviceList = new ArrayList<ARDiscoveryDeviceService>();
        deviceNameList = new String[]{};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this,android.R.layout.simple_list_item_1,android.R.id.text1,deviceNameList);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setPressed(true);
                droneSelected = i;
                System.out.printf("The drone selected was %d", i);
            }
        });
        connect_button.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (droneSelected!=-1)
                        {
                            ARDiscoveryDeviceService service = deviceList.get(droneSelected);
                            Intent intent = new Intent(MainActivity.this,PilotingActivity.class);
                            intent.putExtra(PilotingActivity.EXTRA_DEVICE_SERVICE,service);
                            startActivity(intent);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (droneSelected!=-1)
                        {

                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });


    }
    private void initBroadcastReceiver()
    {
        ardiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
    }

    private void closeServices(){
        if(ardiscoveryServiceBound){
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    //ardiscoveryService;

                    getApplicationContext().unbindService(ardiscoveryServiceConnection);
                    ardiscoveryServiceBound = false;
                    discoverServiceBinder = null;
                    ardiscoveryService=null;
                }
            }).start();
        }
    }
    private void initServices(){
        if(discoverServiceBinder == null){
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i,ardiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        }
        else
        {
            ardiscoveryService = (( ARDiscoveryService.LocalBinder) discoverServiceBinder).getService();
            ardiscoveryServiceBound = true;

            ardiscoveryService.start();
        }
    }
    private void initServiceConnection()
    {
        ardiscoveryServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service){
                discoverServiceBinder = service;
                ardiscoveryService = ((ARDiscoveryService.LocalBinder)service).getService();
                ardiscoveryServiceBound = true;

                ardiscoveryService.start();
            }
            @Override
            public void onServiceDisconnected(ComponentName name){
                ardiscoveryServiceBound = false;
                ardiscoveryService=null;
            }
        };
    }


    @Override
    public void onResume(){
        super.onResume();
        //Log.d(TAG, "onResume...");

        onServicesDevicesListUpdated();
        registerReceivers();

        initServices();
    }
    private void registerReceivers(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(ardiscoveryServicesDevicesListUpdatedReceiver,new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    @Override
    public void onServicesDevicesListUpdated(){
        //Log.d(TAG,"onServicesDevicesListUpdated...");
        List<ARDiscoveryDeviceService> list;

        if(ardiscoveryService!= null) {
            list = ardiscoveryService.getDeviceServicesArray();

            deviceList = new ArrayList<ARDiscoveryDeviceService>();
            List<String> deviceNames = new ArrayList<String>();

            if (list != null) {
                for (ARDiscoveryDeviceService service : list) {
                    ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
                    if (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS.equals(product)) {
                        deviceList.add(service);
                        deviceNames.add(service.getName());
                    }
                }
            }

            deviceNameList = deviceNames.toArray(new String[deviceNames.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNameList);

            listView.setAdapter(adapter);
        }
    }


}
