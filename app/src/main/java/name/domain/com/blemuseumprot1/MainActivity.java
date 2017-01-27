package name.domain.com.blemuseumprot1;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,TabLayout.OnTabSelectedListener{

    private boolean mScanning;
    //Menu menu;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private Handler mHandler;
    HorizontalScrollView horizontalscroll;
    //This is our viewPager
    private ViewPager viewPager;
    String temprec,minor,message;
    double tem=50.00;
    Artifact has,temp;
    private static final long SCAN_PERIOD = 10000;
    ArrayList<Artifact> items;
    TextToSpeech tts;
    int current;
    TypedArray imgs;
    String[] descp;
    String actmessage;
     Tab_1 frag;
    Tab_2 frag2;
    Pager adapter;
    ImageView img;


    // Bluetooth is checked through this

    public void setupBluetoothAdapter()
    {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                scanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }
    }


    public void askPerm()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            PermissionUtils.requestPermission((AppCompatActivity) this,1, Manifest.permission.ACCESS_COARSE_LOCATION,true);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            PermissionUtils.requestPermission((AppCompatActivity) this,1, Manifest.permission.ACCESS_FINE_LOCATION,true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        items = new ArrayList<Artifact>();            //initialised items
        tts = new TextToSpeech(this,this);
        askPerm();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        //Krish start
        horizontalscroll = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Text"));
        tabLayout.addTab(tabLayout.newTab().setText("Music"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);

        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        tabLayout.setOnTabSelectedListener((TabLayout.OnTabSelectedListener) this);
        //Krish end

        stopService(new Intent(MainActivity.this,BackgroundScan.class));

        setupBluetoothAdapter();
        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        current=-1;

        descp=getResources().getStringArray(R.array.Artifacts);
        imgs=getResources().obtainTypedArray(R.array.Arti_imgs);


        img = (ImageView)findViewById(R.id.imageView1);


    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        BluetoothState(false);

                        //setButtonText("Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //findViewById(R.id.menu_scan).setClickable(false);
                        //setButtonText("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        BluetoothState(true);
                        //setButtonText("Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
 ;                       //findViewById(R.id.menu_scan).setClickable();
                        //setButtonText("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    public void BluetoothState(boolean state) {
        if (state) {
            findViewById(R.id.menu_scan).setClickable(true);
            setupBluetoothAdapter();
        } else {
            findViewById(R.id.menu_scan).setClickable(false);
            Toast.makeText(this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                    // this device?
                    final BluetoothManager bluetoothManager =
                            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        scanner = mBluetoothAdapter.getBluetoothLeScanner();
                    }
                } else {
                    // User declined to enable Bluetooth, exit the app.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
            if(tts!=null&&current!=-1)
            {
                if(tts.isSpeaking()) {
                    menu.findItem(R.id.stop_speech).setVisible(true);
                    menu.findItem(R.id.speak_out).setVisible(false);
                }
                else{
                    menu.findItem(R.id.speak_out).setVisible(true);
                    menu.findItem(R.id.stop_speech).setVisible(false);
                }

            }
            else
            {
                menu.findItem(R.id.speak_out).setVisible(false);
                menu.findItem(R.id.stop_speech).setVisible(false);
            }
            if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
           // menu.findItem(R.id.scanning).setActionView(null);
            }
            else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
          //  menu.findItem(R.id.scanning).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    protected void onPause() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        stopService(new Intent(MainActivity.this,BackgroundScan.class));
        super.onPause();
        tts.stop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,new IntentFilter("my-event"));
        //startService(new Intent(MainActivity.this,BackgroundScan.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        tts.stop();
        stopService(new Intent(MainActivity.this,BackgroundScan.class));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.menu_scan:
                if(items!=null)
                {
                   items.clear();
                }
            //startLeScan(true);
                startService(new Intent(this,BackgroundScan.class));
                break;
            case R.id.menu_stop:
                //startLeScan(false);
                break;
            case R.id.speak_out:
                speakOut(descp[current]);
                break;
            case R.id.stop_speech:
                tts.stop();
                invalidateOptionsMenu();
                break;

        }
        return true;
        //  return super.onOptionsItemSelected(item);
    }

  /*  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan(boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scanner.stopScan(scanCallback);
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            scanner.startScan(scanCallback);
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            scanner.stopScan(scanCallback);
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();


    }*/



    @Override
    public void onInit(final int status) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    Log.i("ONINIT",Thread.currentThread().getName());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {

                        tts.speak("Welcome to the BLE Museum App", TextToSpeech.QUEUE_FLUSH, null);

                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {
                                Log.i("Start:", s);
                                invalidateOptionsMenu();
                            }

                            @Override
                            public void onDone(String s) {
                                Log.i("Done:", s);
                                invalidateOptionsMenu();
                            }
                            @Override
                            public void onError(String s) {
                                Log.i("Error:", s);
                            }
                        });

                    }
                    else
                    {                    }

                    tts.setPitch((float) 1.0);
                    tts.setSpeechRate((float) 0.9);
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                }
            }
        }).start();
    }
    private void speakOut(final String text) {


         Bundle temp = null;
         int i = 0;
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                     Log.v("TextToSpeech","Using UtteranceProgressListener");
                     tts.speak(text, TextToSpeech.QUEUE_FLUSH,temp,"ID="+ i++);
                 }
        }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle extras = intent.getExtras();
             minor = extras.getString("minor");
             message = extras.getString("message");
            Log.d("receiver", "Got message: " + message);
            Log.d("receiver", "Got minor: " + minor);

            frag = (Tab_1)adapter.getRegisteredFragment(0);
           // frag2 = (Tab_2)adapter.getRegisteredFragment(1);

            if (Integer.parseInt(minor)==1000)
            {
                img.setImageResource(R.drawable.image_1);
            }
            else if (Integer.parseInt(minor)==1001)
            {
                img.setImageResource(R.drawable.image_2);
               // frag2.setTrack(R.raw.song);
            }
            else if (Integer.parseInt(minor)==1002)
            {
                img.setImageResource(R.drawable.image_3);
            }
            else if (Integer.parseInt(minor)==1003)
            {
                img.setImageResource(R.drawable.image_4);
            }
            else if (Integer.parseInt(minor)==0000)
            {
                img.setImageResource(R.drawable.nothing);
            }
            frag.settext(message);
            tts.stop();
            speakOut(message);
        }
    };
    ;

 }

