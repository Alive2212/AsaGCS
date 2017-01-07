package com.gcs.asa.asagcs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gcs.asa.asagcs.database.DatabaseHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import layout.HomeFragment;
import layout.LeftFlightSettingFragment;
import layout.LeftMainMenuFragment;
import layout.LeftMonitorFragment;
import layout.SettingMenuFragment;

public class MainActivity extends AppCompatActivity {

    String MAIN_TAG = "AsaGCS";
    String ACTIVITY_TAG = "MainActivity";
    EditText serverIpAddressEditText, serverTcpPortEditText, udpListenPortEditText, passwordEditText;

    private ReadTelemetry TelemetryAsyncTask;

    //TODO ! FLAG MUST BE 'FALSE' FOR PLAY STORE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    boolean DEBUG=true;

    //Application Settings
    public static final String SERVER_IP_ADDRESS = "server_ip_address_text";
    public static final String SERVER_PORT_ADDRESS = "server_port_number_text";
    public static final String LOCAL_PORT_ADDRESS = "local_port_number_text";
    public static final String MIN_AIRSPEED = "minimum_air_speed";
    public static final String USE_GPS = "use_gps_checkbox";
    public static final String Control_Pass = "app_password";
    public static final String BLOCK_C_TIMEOUT = "block_change_timeout";
    public static final String DISABLE_SCREEN_DIM = "disable_screen_dim";

    public Telemetry AC_DATA;                       //Class to hold&proces AC Telemetry Data
    boolean AcLocked = false;
    TextView DialogTextWpName;
    Button DialogButtonConfirm;
    Button Alt_DialogButtonConfirm;
    Button Alt_ResToDlAlt;
    Button Alt_ResToDesAlt;
    EditText DialogWpAltitude;
    TextView DialogTextWpLat;
    TextView DialogTextWpLon;
    Button DialogAltUp;
    Button DialogAltDown;
    boolean ShowOnlySelected = true;
    String AppPassword;
    //AP_STATUS
    TextView TextViewApMode;
    TextView TextViewGpsMode;
    TextView TextViewStateFilterMode;
    TextView TextViewFlightTime;
    TextView TextViewBattery;
    TextView TextViewSpeed;
    TextView TextViewAirspeed;
    //AC Blocks
    ArrayList<BlockModel> BlList = new ArrayList<BlockModel>();
    BlockListAdapter mBlListAdapter;
    ListView BlListView;
    SharedPreferences AppSettings;                  //App Settings Data
    Float MapZoomLevel = 14.0f;
    //Ac Names
    ArrayList<Model> AcList = new ArrayList<Model>();
    AcListAdapter mAcListAdapter;
    ListView AcListView;
    boolean TcpSettingsChanged;
    boolean UdpSettingsChanged;
    int DialogAcId;
    int DialogWpId;
    //UI components (needs to be bound onCreate
    private GoogleMap mMap;
    private TextView MapAlt;
    private TextView MapThrottle;
    private ImageView Pfd;

    //_______________ my Code______________//
    private Button toggleButton_createPlane;
    DatabaseHelper databaseHelper;


    private Button Button_ConnectToServer;
    private ToggleButton ChangeVisibleAcButon;
    private ToggleButton LockToAcSwitch;
    private DrawerLayout mDrawerLayout;
    //Dialog components
    private Dialog WpDialog;
    private Dialog AltDialog;
    //Position descriptions >> in future this needs to be an array or struct
//  private LatLng AC_Pos = new LatLng (43.563958, 1.481391);//default position
    private LatLng AC_Pos = new LatLng(35.699857, 51.338063);//Tehran Position (Azadi Tower)
    private String SendStringBuf;
    private boolean AppStarted = false;             //App started indicator
    private CharSequence mTitle;
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Background task to read and write telemery msgs
    private boolean isTaskRunning;

    private boolean DisableScreenDim;

    private CountDownTimer BL_CountDown;
    private int BL_CountDownTimerValue;
    private int JumpToBlock;
    private int BL_CountDownTimerDuration;

    private ArrayList<Model> generateDataAc() {
        AcList = new ArrayList<Model>();
        return AcList;
    }

    private ArrayList<BlockModel> generateDataBl() {
        BlList = new ArrayList<BlockModel>();
        return BlList;
    }

    private Thread mTCPthread;

    private NumberPicker mNumberPickerThus,mNumberPickerHuns, mNumberPickerTens,mNumberPickerOnes;















    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Initial Fragment
        setFragment(R.id.left_fragment,new LeftMainMenuFragment());
        setFragment(R.id.right_fragment,new HomeFragment());

//        Initial Setting
        serverIpAddressEditText = (EditText) findViewById(R.id.serverIpAddressEditText);
        serverTcpPortEditText = (EditText) findViewById(R.id.serverTcpPortEditText);
        udpListenPortEditText = (EditText) findViewById(R.id.udpListenPortEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

//        Initial AC_DATA
        setup_telemetry_class();
        TelemetryAsyncTask = new ReadTelemetry();
        TelemetryAsyncTask.execute();

//        Show Logo and Title in Action Bar
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    public void onConnectButtonClick(View view){
        //TODO Connect Options
    }

    public void onHomeButtonClick(View view){
        setFragment(R.id.right_fragment,new HomeFragment());
    }

    public void onMonitorButtonClick(View view){
        //TODO don't reload google map
        setFragment(R.id.right_fragment,new ViewFragment());
        setFragment(R.id.left_view_fragment,new LeftMonitorFragment());
    }

    public void onFlightSettingButtonClick(View view){
        //TODO we have an error when map view not loaded
        setFragment(R.id.left_view_fragment,new LeftFlightSettingFragment());
    }

    public void onMissionPlaningButtonClick(View view){
        //TODO create Planing
    }

    public void onSettingButtonClick(View view){
        //TODO we have an error when map view not loaded
        setFragment(R.id.left_view_fragment,new SettingMenuFragment());
    }

    public void onHelpButtonClick(View view){
        //TODO Create Help Note
    }

    private void setFragment(int fragmentID, Fragment fragment){
//        define fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
//        define fragment transaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        replace new fragment into fragment place
        fragmentTransaction.replace(fragmentID,fragment);
//        execute changes into fragment
        fragmentTransaction.commit();
    }

    /**
     * Setup TCP and UDP connections of Telemetry class
     */
    //TODO Give Variable Input and initialze it
    private void setup_telemetry_class(){
        //Create Telemetry Class
        AC_DATA= new Telemetry();

        //Read & Setup Telemetry Class
/*        AC_DATA.ServerIp = serverIpAddressEditText.equals(null) ?
                "192.168.1.7" : serverIpAddressEditText.getText().toString();
        AC_DATA.ServerTcpPort = serverTcpPortEditText.equals(null) ?
                5010 : Integer.valueOf(serverTcpPortEditText.getText().toString());
        AC_DATA.UdpListenPort = udpListenPortEditText.equals(null) ?
                5005 : Integer.valueOf(udpListenPortEditText.getText().toString());*/
        AC_DATA.ServerIp = "192.168.1.7";
        AC_DATA.ServerTcpPort = 5010;
        AC_DATA.UdpListenPort = 5005;
        //TODO Air Speed Changeable
        AC_DATA.AirSpeedMinSetting = 10;
        AC_DATA.DEBUG=DEBUG;

        AC_DATA.GraphicsScaleFactor = getResources().getDisplayMetrics().density;
        AC_DATA.prepare_class();

        AC_DATA.setup_udp();
    }

    /**
     * background thread to read & write comm strings. Only changed UI items should be refreshed for smoother UI
     * Check telemetry class for whole UI change flags
     */
    class ReadTelemetry extends AsyncTask<String,String,String>{
        String CLASS_TAG = "ReadTelemetry";

        @Override
        protected void onPreExecute() {
            String METHOD_TAG = "onPreExecute";
            super.onPreExecute();
            isTaskRunning = true;
            if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                    "Done");
        }

        @Override
        protected String doInBackground(String... strings) {
            String METHOD_TAG = "doInBackground";
            mTCPthread = new Thread(new ClientThread());
            mTCPthread.start();

            while (isTaskRunning){
                //TODO if setting changed
                //TODO if UDP setting changed
                if (null != AC_DATA.SendToTcp){
                    AC_DATA.mTcpClient.sendMessage(AC_DATA.SendToTcp);
                    AC_DATA.SendToTcp = null;
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "Send waiting messages");
                }

                AC_DATA.read_udp_data();
                if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                        "UDP Data Read");

                if(AC_DATA.ViewChanged){
                    publishProgress("ee");
                    AC_DATA.ViewChanged = false;
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "View Changed");
                }
            }
            if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                    "Done");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String METHOD_TAG = "onProgressUpdate";
            super.onProgressUpdate(values);
            try{
                if(AC_DATA.SelAcInd <0){
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "does't exist any aircraft");
                    return;
                }
                if(AC_DATA.AircraftData[AC_DATA.SelAcInd].ApStatusChanged){
                    //TODO show selected aircraft data
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "we have new data ;)");
                    AC_DATA.AircraftData[AC_DATA.SelAcInd].ApStatusChanged = false;
                }
                if(AC_DATA.AirspeedWarning){
                    //TODO Play warning sound and show warning message
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "Air Speed Warning");
                }
                if(AC_DATA.BlockChanged){
                    //Block changed for selected aircraft
                    //TODO Block Changed
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "BlockChanged");
                    AC_DATA.BlockChanged=false;
                }
                if(AC_DATA.NewAcAdded){
                    //New AC Added
                    //TODO New AC Added
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "BlockChanged");
                    AC_DATA.NewAcAdded=false;
                }
                if(AC_DATA.BatteryChanged){
                    //Selected AC Battery Changed
                    //TODO Change Selected AC Battery View
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "BatteryChanged");
                    AC_DATA.BatteryChanged=false;
                }

                //TODO Create refresh_markers() Methode

                if(AC_DATA.AircraftData[AC_DATA.SelAcInd].EngineStatusChanged){
                    //TODO Change Map Throttle
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "EngineStatusChanged");
                    AC_DATA.AircraftData[AC_DATA.SelAcInd].EngineStatusChanged = false;
                }
                if(AC_DATA.AircraftData[AC_DATA.SelAcInd].Altitude_Changed){
                    //TODO Change Image of Primary Flight
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "EngineStatusChanged");
                    AC_DATA.AircraftData[AC_DATA.SelAcInd].Altitude_Changed = false;
                }
                if (AC_DATA.AircraftData[AC_DATA.SelAcInd].AirspeedEnabled && AC_DATA.AircraftData[AC_DATA.SelAcInd].AirspeedChanged) {
                    //Airspeed Enabled
                    //TODO Show Air Speed Changed
                    if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                            "Air Speed Changed");
                }

                //TODO Change Connect Button Text

            }catch (Exception e){
                if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,
                        e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    //when run any message from TCP Server run this method
    class ClientThread implements Runnable{
        String CLASS_TAG = "ClientThread";
        @Override
        public void run() {
            String METHOD_TAG = "run";
            if(DEBUG) Log.d(MAIN_TAG + " " + ACTIVITY_TAG + " " + CLASS_TAG + " " + METHOD_TAG,"Done");
            AC_DATA.mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    AC_DATA.parse_tcp_string(message);
                }
            });
            AC_DATA.mTcpClient.SERVERIP = AC_DATA.ServerIp;
            AC_DATA.mTcpClient.SERVERPORT = AC_DATA.ServerTcpPort;
            AC_DATA.mTcpClient.run();
        }
    }

}
