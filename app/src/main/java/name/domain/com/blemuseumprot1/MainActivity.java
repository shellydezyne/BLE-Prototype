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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;
import java.util.StringTokenizer;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private boolean mScanning;
    //Menu menu;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    TextToSpeech tts;
    int current;
    TypedArray imgs;
    String[] descp;
    //Fragment fragment;
    TextView tv;
    ImageView img;
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
        tts = new TextToSpeech(this,this);
        askPerm();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        setupBluetoothAdapter();
        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        current=-1;
        descp=getResources().getStringArray(R.array.Artifacts);
        imgs=getResources().obtainTypedArray(R.array.Arti_imgs);
        tv=(TextView)findViewById(R.id.textview);
        img=(ImageView)findViewById(R.id.imageView);
        //fragment=getSupportFragmentManager().findFragmentById(R.id.fragment);
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
            menu.findItem(R.id.scanning).setActionView(null);
            }
            else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.scanning).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.menu_scan:
                startLeScan(true);
                break;
            case R.id.menu_stop:
                startLeScan(false);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
    }

    private ScanCallback scanCallback=new ScanCallback(){

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord mScanRecord=result.getScanRecord();
            String record;
            record = bytesToHexString(mScanRecord.getBytes());
            Log.i("Got this:", record);
            int has=hashFunction(record.substring(18, 50), record.substring(50, 54), record.substring(54, 58));
            Log.i("ONSCANRESULT",Thread.currentThread().getName());
            displayFragment(has);

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                      Toast.makeText(getApplicationContext(),"NO Devices",Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    public void displayFragment(int hashcode) {
        if(current!=hashcode)
        {
            if(tts.isSpeaking())
                tts.stop();
            current =hashcode;
            tv.setText(descp[hashcode]);
            img.setImageResource(imgs.getResourceId(hashcode,-1));
            speakOut(descp[hashcode]);
        }
    }
    public int hashFunction(String uid,String maj,String min) {
        //Create hashing code
        Log.i("UUID",uid);
        Log.i("MAJOR",maj);
        Log.i("MINOR",min);
        return 0;
    }

    @NonNull
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            buffer.append(String.format("%02x", bytes[i]));
        }
        return buffer.toString();
    }

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
                    {
                        //tts.setOnUtteranceCompletedListener(listener);
                        //btnSpeak.setEnabled(true);
                        //speakOutWelcomeMessage();
                    }
                    tts.setPitch((float) 1.5);
                    tts.setSpeechRate((float) 0.75);
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

        //StringTokenizer sent=new StringTokenizer(text,".");
         Bundle temp = null;
         int i = 0;
        //while(sent.hasMoreTokens()) {
         //new Thread(new Runnable() {
             //@Override
             //public void run() {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                     Log.v("TextToSpeech","Using UtteranceProgressListener");
                     tts.speak(text, TextToSpeech.QUEUE_FLUSH,temp,"ID="+ i++);
                 }

          //   }
        // }).start();
            /*else
            {
                tts.speak(sent.nextToken(),TextToSpeech.QUEUE_FLUSH,null);
            }*/
            //while(tts.isSpeaking());
        }
 }

