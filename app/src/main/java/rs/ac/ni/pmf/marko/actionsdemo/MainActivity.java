package rs.ac.ni.pmf.marko.actionsdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_CONTACT = 1;
    private static final int PERMISSION_REQUEST = 2;

    private Uri selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setAlarm(View view) {

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                .putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm from app")
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            Toast.makeText(this, "Alarm in 1 minute!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No activity found for action", Toast.LENGTH_SHORT).show();
        }
    }

    public void pickConatact(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK)
                .setType(ContactsContract.Contacts.CONTENT_TYPE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PICK_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_CONTACT:
                if (resultCode == RESULT_OK && data != null) {
                    selectedContact = data.getData();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showContact(View view) {
        if (selectedContact == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, selectedContact);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void contactDetails(View view) {
        if (selectedContact == null) {
            return;
        }

        if(checkReadContactsPermission()) {
            showContactDetails();
        }
    }

    private void showContactDetails() {
        String name = getContactName(selectedContact);
        List<String> phones = getContactPhones(selectedContact);

        StringBuilder sb = new StringBuilder();

        for (String phone : phones) {
            sb.append(phone);
            sb.append(" ");
        }

        Toast.makeText(this, "Name: " + name + ", Phones: " + sb.toString(), Toast.LENGTH_SHORT).show();
    }

    private String getContactName(Uri contactUri) {
        String contactName = null;

        Cursor contactsCursor = getContentResolver().query(contactUri, new String[]{
                ContactsContract.Contacts.DISPLAY_NAME
        }, null, null, null);

        if (contactsCursor.moveToFirst()) {
            contactName = contactsCursor
                    .getString(contactsCursor
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        contactsCursor.close();

        return contactName;
    }

    private List<String> getContactPhones(Uri contactUri) {
        List<String> result = new ArrayList<>();

        Cursor contactIdCursor = getContentResolver().query(contactUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null);

        String contactId = null;

        if (contactIdCursor.moveToFirst()) {
            contactId = contactIdCursor.getString(contactIdCursor.getColumnIndex(ContactsContract.Contacts._ID));
        }

        contactIdCursor.close();

        if (contactId == null) {
            Log.e("CONTACTS", "Contact ID not found");
            return Collections.emptyList();
        }

        String phoneQuery = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId;

        Cursor phoneCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                phoneQuery,
                null,
                null);

        while (phoneCursor.moveToNext()) {
            String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            result.add(phone);
        }

        return result;
    }

    private boolean checkReadContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContactDetails();
                } else {
                    Toast.makeText(this, "Sorry, you don't have permissiong to read contacts", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
