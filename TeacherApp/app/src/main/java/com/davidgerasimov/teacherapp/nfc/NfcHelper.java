package com.davidgerasimov.teacherapp.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import java.nio.charset.StandardCharsets;

public class NfcHelper {

    private NfcAdapter nfcAdapter;
    private Activity activity;

    public interface NfcReadCallback {
        void onNfcRead(String studentId);
        void onNfcError(String error);
    }

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
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, null);
    }

    public void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public String readStudentIdFromIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            // Try reading NDEF message first
            android.os.Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMessages != null && rawMessages.length > 0) {
                NdefMessage message = (NdefMessage) rawMessages[0];
                NdefRecord record = message.getRecords()[0];
                byte[] payload = record.getPayload();
                // Skip language code bytes for TEXT record
                int langCodeLength = payload[0] & 0x3F;
                return new String(payload, langCodeLength + 1,
                        payload.length - langCodeLength - 1, StandardCharsets.UTF_8);
            }

            // Try reading raw tag ID as fallback
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                byte[] tagId = tag.getId();
                StringBuilder sb = new StringBuilder();
                for (byte b : tagId) {
                    sb.append(String.format("%02X", b));
                }
                return sb.toString();
            }
        }
        return null;
    }
}