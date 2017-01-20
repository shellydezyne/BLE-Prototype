package name.domain.com.blemuseumprot1;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dezyne 2 on 1/19/2017.
 */


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BackgroundScan extends Service {

    public final Handler handler = new Handler();
    private BluetoothLeScanner scanner;
    boolean mScanning;
    double tem=50.00;
    String temprec;
    static String minor="0000";
    Artifact has,temp;
    ArrayList<Artifact> items;
    String[] descp;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        items = new ArrayList<Artifact>();
        descp=getResources().getStringArray(R.array.Artifacts);
        scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();


       // mScanning = false;
       // scanner.stopScan(scanCallback);

        Log.i("Service","Service started");
        Sync sync = new Sync(call,10*1000);
    }


    final private Runnable call = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {
            //This is where my sync code will be, but for testing purposes I only have a Log statement
            Log.v("test","this will run every minute");

            mScanning = true;
            scanner.startScan(scanCallback);

            handler.postDelayed(call,10*1000);
        }
    };
    public class Sync {
        Runnable task;

        public Sync(Runnable task, long time) {
            this.task = task;
            handler.removeCallbacks(task);
            handler.postDelayed(task, time);
        }
    }


    private ScanCallback scanCallback=new ScanCallback(){

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord mScanRecord=result.getScanRecord();
            String record;

            record = bytesToHexString(mScanRecord.getBytes());
            Log.i("Got this:", record);
            int rssi=result.getRssi();
            Log.i("TX:",record.substring(58,60));
            int txpower=Integer.parseInt(record.substring(58,60),16)-256;
            Log.i("TXPOWER:",txpower+"");
            double distance=getDistance(rssi,txpower);
            Log.i("Distance:",distance+"");


            if (tem>distance)
            {
                if (minor.equals(record.substring(54,58)))
                {
                        Log.i("Serice","same minor");


                }
                else
                {
                    Log.i("UUID",record.substring(18, 50));
                    Log.i("MAJOR",record.substring(50, 54));
                    Log.i("MINOR",record.substring(54, 58));
                    minor = record.substring(54,58);

                    has=hashFunction(record.substring(18, 50), record.substring(50, 54), record.substring(54, 58));

                    if(!items.contains(has))
                        items.add(has);
                        Log.i("temp", String.valueOf(items));
                    displayText();
                }
            }


            tem =distance;
            Log.i("Service",String.valueOf(tem));
            Log.i("temp",String.valueOf(tem));
            Log.i("SERVICEONSCANRESULT",Thread.currentThread().getName());

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"NO Devices",Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    private void displayText() {

        temp = items.get(items.size()-1);

        if (Integer.parseInt(minor)==1000)
        {
            temp.descrip=descp[0];
        }
        else if (Integer.parseInt(minor)==1001)
        {
            temp.descrip=descp[1];
        }
        else if (Integer.parseInt(minor)==1002)
        {
            temp.descrip=descp[2];
        }
        else if (Integer.parseInt(minor)==1003)
        {
            temp.descrip=descp[3];
        }

       /* if (frag!= null) {
            frag.settext(String.valueOf(temp.descrip));
        }
        else {
            Log.i("MainActivity",String.valueOf(frag));
        }*/

        sendMessage();

    }

    private void sendMessage() {
        Intent intent = new Intent("my-event");
        // add data
        intent.putExtra("message", temp.descrip);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public static String bytesToHexString(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            buffer.append(String.format("%02x", bytes[i]));
        }
        return buffer.toString();
    }

    double getDistance(int rssi, int txPower) {

        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    public Artifact hashFunction(String uid,String maj,String min) {
        //Create hashing code
        Log.i("UUID",uid);
        Log.i("MAJOR",maj);
        Log.i("MINOR",min);
        Artifact obj=new Artifact(uid,maj,min);
        List<Integer> images = new ArrayList<>();           //initialised images
        //This is static right now , fixed values are being added


        return obj;
    }



}
