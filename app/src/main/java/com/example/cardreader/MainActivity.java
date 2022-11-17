package com.example.cardreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter;
    private TextView textView;
    private PendingIntent pendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Log.d("apdu","data: "+selectApdu(SelectAID));
        byte[] bytes = Base64.getDecoder().decode("00A4040007A0000002471001");
        byte[] bytes1 = Utils.hexToByteArray("F2600609640539");
        Log.d("apdu","base decode: "+bytes);
        Log.d("apdu","base decode 1: "+bytes1);
        Log.d("apdu","decode: "+Base64.getEncoder().encodeToString(bytes));
        Log.d("apdu","decode 1: "+Base64.getEncoder().encodeToString(bytes1));
        Log.d("apdu","data 1: "+Base64.getEncoder().encodeToString(selectApdu(SelectAID)));
        Log.d("apdu","data 2: "+Utils.byteArrayToHex(selectApdu(SelectAID)));
        Log.d("apdu","data 3: "+Utils.byteArrayToHex(bytes1));
        if (nfcAdapter != null) {
            textView.setText("Read an NFC tag");
        } else {
            textView.setText("This phone is not NFC enabled.");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        Log.d("Action",":"+action);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

//        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        Log.d("Tag","new intent tag: "+tag);
//        onTagDiscovered(tag);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            // nfcAdapter.enableForegroundDispatch(this, pendingIntent, mIntentFilters, mNFCTechLists);
            // Work around some buggy hardware that checks for cards too fast
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);
            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A |
                NfcAdapter.FLAG_READER_NFC_B |
                NfcAdapter.FLAG_READER_NFC_F |
                NfcAdapter.FLAG_READER_NFC_V |
                NfcAdapter.FLAG_READER_NFC_BARCODE |
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK |
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
          //  nfcAdapter.disableForegroundDispatch(this);
        nfcAdapter.disableReaderMode(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTagDiscovered(Tag tag) {
        Log.d("response ","nfc tag: "+tag);
        IsoDep nfcA = IsoDep.get(tag);
        try {
            nfcA.connect();
            byte[] response = nfcA.transceive(Utils.hexToByteArray("00A4040007F2600609640539"));
            Log.d("response","byte nfca: "+response);
            Log.d("response","byte 1 nfca: "+Base64.getDecoder().decode("00A4040007A0000002471001"));
            Log.d("response","hex nfca: "+ Base64.getEncoder().encodeToString(response));
            Log.d("response","hex nfca: "+ new String(response, StandardCharsets.UTF_8));
            Log.d("response","result nfca: "+ Utils.byteArrayToHex(response));
            textView.setText("NFC Response: "+Utils.byteArrayToHex(response));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                nfcA.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        IsoDep isoDep = IsoDep.get(tag);
//        Log.d("response","isoDep: "+isoDep);
//        if (isoDep != null) {
//            try {
//                isoDep.connect();
//                byte[] result = isoDep.transceive(Utils.hexToByteArray("00A4040007F260060964053900"));
//                Log.d("response","byte: "+result);
//                Log.d("response","byte 1: "+Base64.getDecoder().decode("00A4040007F260060964053900"));
//                Log.d("response","hex: "+ Base64.getEncoder().encodeToString(result));
//                Log.d("response","result: "+ Utils.byteArrayToHex(result));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            finally {
//                try {
//                    isoDep.close();
//                } catch (Exception ignored) {}
//            }
//        }

    }

    public static byte[] SelectAID = new byte[]{
            (byte) 0xF2, (byte) 0x60, (byte) 0x06, (byte) 0x09,
            (byte) 0x64, (byte) 0x05, (byte) 0x39};

    private byte[] selectApdu(byte[] aid) {
        byte[] commandApdu = new byte[6 + aid.length];
        commandApdu[0] = (byte)0x00;  // CLA
        commandApdu[1] = (byte)0xA4;  // INS
        commandApdu[2] = (byte)0x04;  // P1
        commandApdu[3] = (byte)0x00;  // P2
        commandApdu[4] = (byte)(aid.length & 0x0FF);       // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.length);
       // commandApdu[commandApdu.length - 1] = (byte)0x00;  // Le
        return commandApdu;
    }
}