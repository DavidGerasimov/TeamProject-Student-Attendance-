package com.davidgerasimov.teacherapp.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NfcHelper {

    private NfcAdapter nfcAdapter;
    private Activity activity;

    // Must match the AID in apduservice.xml
    private static final byte[] SELECT_APDU = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00,
            (byte) 0x05,
            (byte) 0xF2, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22,
            (byte) 0x00
    };

    private static final byte[] GET_DATA_APDU = {
            (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    public NfcHelper(Activity activity) {
        this.activity = activity;
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
    }

    public boolean isNfcAvailable() {
        return nfcAdapter != null;
    }

    public boolean isNfcEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public void enableForegroundDispatch() {
        Intent intent = new Intent(activity, activity.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity, 0, intent, PendingIntent.FLAG_MUTABLE);

        IntentFilter[] filters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        };

        String[][] techLists = new String[][]{
                new String[]{IsoDep.class.getName()}
        };

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent,
                filters, techLists);
    }

    public void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public String readStudentIdFromIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                IsoDep isoDep = IsoDep.get(tag);
                if (isoDep != null) {
                    try {
                        isoDep.connect();
                        isoDep.setTimeout(5000);

                        // Step 1: Select our app by AID
                        byte[] selectResponse = isoDep.transceive(SELECT_APDU);
                        android.util.Log.d("NFC_DEBUG", "Select response: " +
                                bytesToHex(selectResponse));

                        // Check if selection was successful (SW = 90 00)
                        if (selectResponse != null && selectResponse.length >= 2) {
                            byte sw1 = selectResponse[selectResponse.length - 2];
                            byte sw2 = selectResponse[selectResponse.length - 1];

                            if (sw1 == (byte) 0x90 && sw2 == (byte) 0x00) {
                                // Step 2: Request the student data
                                byte[] dataResponse = isoDep.transceive(GET_DATA_APDU);
                                android.util.Log.d("NFC_DEBUG", "Data response: " +
                                        bytesToHex(dataResponse));

                                if (dataResponse != null && dataResponse.length > 2) {
                                    // Remove the last 2 status bytes (90 00)
                                    byte[] payload = Arrays.copyOf(dataResponse,
                                            dataResponse.length - 2);
                                    return new String(payload, StandardCharsets.UTF_8);
                                }
                            }
                        }
                        isoDep.close();
                    } catch (Exception e) {
                        android.util.Log.e("NFC_DEBUG", "Error: " + e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}