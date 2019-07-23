package com.example.kiera.morsecodercommunicator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.lang.String;


public class MainActivity extends AppCompatActivity {
    TextView mTvBluetoothStatus;
    TextView mTvReceiveData;
    TextView mTvSendData;
    Button mBtnBluetoothOn;
    Button mBtnBluetoothOff;
    Button mBtnConnect;
    Button mBtnSendData;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Morse Code Table
    public static HashMap<String, String> morseCode;
    static {
        morseCode = new HashMap<>();
        morseCode.put("A", "-");
        morseCode.put("B", "=...");
        morseCode.put("C", "-.-");
        morseCode.put("D", "-..");
        morseCode.put("E", ".");
        morseCode.put("F", "..-.");
        morseCode.put("G", "--.");
        morseCode.put("H", "....");
        morseCode.put("I", "..");
        morseCode.put("J", ".---");
        morseCode.put("K", "-.-");
        morseCode.put("L", "-..");
        morseCode.put("M", "--");
        morseCode.put("N", "-.");
        morseCode.put("O", "---");
        morseCode.put("P", ".--.");
        morseCode.put("Q", "--.-");
        morseCode.put("R", ".-.");
        morseCode.put("S", "...");
        morseCode.put("T", "-");
        morseCode.put("U", "..-");
        morseCode.put("V", "...-");
        morseCode.put("W", ".--");
        morseCode.put("X", "-..-");
        morseCode.put("Y", "-.--");
        morseCode.put("Z", "--..");
        morseCode.put("1", "----");
        morseCode.put("2", "..---");
        morseCode.put("3", "...--");
        morseCode.put("4", "....-");
        morseCode.put("5", ".....");
        morseCode.put("6", "-....");
        morseCode.put("7", "--...");
        morseCode.put("8", "---..");
        morseCode.put("9", "----.");
        morseCode.put("0", "-----");
        morseCode.put(",", "--..--");
        morseCode.put(".", ".-.-.-");
        morseCode.put("?", "..--..");
        morseCode.put("/", "-..-.");
        morseCode.put("-", "....-");
        morseCode.put("(", "-.--.");
        morseCode.put(")", "-.--.-");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvBluetoothStatus = (TextView)findViewById(R.id.tvBluetoothStatus);
        mTvReceiveData = (TextView)findViewById(R.id.tvReceiveData);
        mTvSendData =  (EditText) findViewById(R.id.tvSendData);
        mBtnBluetoothOn = (Button)findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = (Button)findViewById(R.id.btnBluetoothOff);
        mBtnConnect = (Button)findViewById(R.id.btnConnect);
        mBtnSendData = (Button)findViewById(R.id.btnSendData);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });
        mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });
        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });
        mBtnSendData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String writeMessage = null;
                String encryptedMessage = null;
                if(mThreadConnectedBluetooth != null) {
                    // mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                    // TODO : encode the mTvSendData.getText().toString()
                    writeMessage = mTvSendData.getText().toString();
//                    encryptedMessage = encryptMessage(writeMessage);
//                    mThreadConnectedBluetooth.write(encryptedMessage);
                    mThreadConnectedBluetooth.write(writeMessage);
                    mTvSendData.setText("");
                }
            }
        });
        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    String decryptedMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8"); // received message
                        // TODO : decode the readMessage(morse code)
//                        decryptedMessage = decryptMessage(readMessage);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                     mTvReceiveData.setText(readMessage);
//                    mTvReceiveData.setText(decryptedMessage);
                }
            }
        };
    }

    /* encrypt morse code */
    // text -> morse code
    private String encryptMessage(String plainText) {
        String enText = null;
        char tmp;

        for (int i = 0; i < plainText.length(); i++) {
            tmp = plainText.charAt(i);
            enText.concat(morseCode.get(tmp));
            enText.concat(" ");
        }

        return enText;
    }

    /* decrypt morse code */
    // morse code -> text
    private String decryptMessage(String morseCodeText) {
        String deText = null;
        String tmp = null;
        String tmp2 = null;
        String[] words = null;
        int i;

        words = morseCodeText.split(" ");

        i = 0;
        while(words[i] != null) {
            tmp = words[i];
            tmp2 = getKey(morseCode, tmp);
            deText.concat(tmp2);
            i++;
        }

        return deText;
    }

    private String getKey(HashMap<String, String> morseCode, String mCode) {
        for (String plainText : morseCode.keySet()) {
            if (plainText.equals(mCode)) {
                return plainText;
            }
        }
        return "?";
    }


    /* bluetooth on */
    void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't support BLUETOOTH", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Already BLUETOOTH is activate", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("ACTIVATE");
            }
            else {
                Toast.makeText(getApplicationContext(), "BLUETOOTH is not activate", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    /* bluetooth off */
    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "BLUETOOTH become deactivate", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("DEACTIVATE");
        }
        else {
            Toast.makeText(getApplicationContext(), "Already BLUETOOTH is deactivate", Toast.LENGTH_SHORT).show();
        }
    }

    /* bluetooth activation check */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "BLUETOOTH ON", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("ON");
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "CANCELE", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("OFF");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* list paired devices */
    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Device Select");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "No Device paired", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "BLUETOOTH is deactivate", Toast.LENGTH_SHORT).show();
        }
    }

    /* bluetooth connect to selected device */
    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error on BLUETOOTH connection", Toast.LENGTH_LONG).show();
        }
    }

    /* connection thread */
    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error on Socket connection", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error on sending data", Toast.LENGTH_LONG).show();
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error on freeing socket", Toast.LENGTH_LONG).show();
            }
        }
    }
}
