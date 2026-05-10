package com.davidgerasimov.studentapp.nfc;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HceService extends HostApduService {

    private static final String TAG = "HceService";

    // AID for our app - must match what teacher app looks for
    private static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";

    // Commands
    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
    private static final byte[] UNKNOWN_CMD_SW = {(byte) 0x00, (byte) 0x00};
    private static final byte[] SELECT_APDU_HEADER = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00
    };
    private static final byte[] GET_DATA_APDU_HEADER = {
            (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0x00
    };

    private static String studentPayload = "";

    public static void setStudentPayload(String payload) {
        studentPayload = payload;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (commandApdu == null) return UNKNOWN_CMD_SW;

        Log.d(TAG, "Received APDU: " + bytesToHex(commandApdu));

        if (isSelectAid(commandApdu)) {
            Log.d(TAG, "SELECT AID command received");
            return SELECT_OK_SW;
        }

        if (isGetData(commandApdu)) {
            Log.d(TAG, "GET DATA command received, sending payload: " + studentPayload);
            byte[] payloadBytes = studentPayload.getBytes(StandardCharsets.UTF_8);
            byte[] response = new byte[payloadBytes.length + SELECT_OK_SW.length];
            System.arraycopy(payloadBytes, 0, response, 0, payloadBytes.length);
            System.arraycopy(SELECT_OK_SW, 0, response, payloadBytes.length, SELECT_OK_SW.length);
            return response;
        }

        return UNKNOWN_CMD_SW;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
    }

    private boolean isSelectAid(byte[] apdu) {
        if (apdu.length < SELECT_APDU_HEADER.length) return false;
        return Arrays.equals(
                Arrays.copyOf(apdu, SELECT_APDU_HEADER.length),
                SELECT_APDU_HEADER);
    }

    private boolean isGetData(byte[] apdu) {
        if (apdu.length < GET_DATA_APDU_HEADER.length) return false;
        return Arrays.equals(
                Arrays.copyOf(apdu, GET_DATA_APDU_HEADER.length),
                GET_DATA_APDU_HEADER);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}