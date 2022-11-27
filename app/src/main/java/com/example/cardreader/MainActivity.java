package com.example.cardreader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback, NfcAdapter.OnTagRemovedListener {

    private NfcAdapter nfcAdapter;
    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            textView.setText("Read an NFC tag");
        } else {
            textView.setText("This phone is not NFC enabled.");
        }

//        nfcAdapter.enableReaderMode(this, tag -> {
//            Log.d("Read","NFC tag found"+tag);
//            Toast.makeText(this, "Tag found", Toast.LENGTH_LONG).show();
//        }, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Log.d("tag", "TAG IS NOT NULL"+tag);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            // Work around some buggy hardware that checks for cards too fast
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);
            nfcAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                             NfcAdapter.FLAG_READER_NFC_F |
                              NfcAdapter.FLAG_READER_NFC_V |
                                 NfcAdapter.FLAG_READER_NFC_BARCODE |
                                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK ,
                    options);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
        nfcAdapter.disableReaderMode(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTagDiscovered(Tag tag) {
        Log.d("Read","tag :"+tag.getId());
       // Ndef mndef = Ndef.get(tag);
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();
            //byte[] response = nfcA.transceive(selectApdu(SelectAID));
            byte[] response = isoDep.transceive(Utils.hexToByteArray("00A4040007F260060964053900"));
            String res = Utils.convertHexToStringValue(Utils.byteArrayToHex(response));
            Log.d("Read","response :"+res);

            textView.setText("Fonepay Response: "+res);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                isoDep.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public void onTagRemoved() {

    }
}