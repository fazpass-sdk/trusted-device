package com.fazpass.trusted_device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class Contact {
    private Context context;
    private String name;
    private List<String> phoneNumber;
    private List<Contact> contacts;
    public Contact(Context context) {
        this.context = context;
        readContacts().subscribe(c-> this.contacts = c);
    }

    private Contact(String name, List<String> number){
        this.name = name;
        this.phoneNumber = number;
    }

    @SuppressLint("Range")
    private Observable<List<Contact>> readContacts(){
        return Observable.create(subscriber->{
            List<Contact> contacts = new ArrayList<>();
            boolean isPermit = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
            if(isPermit){
                ContentResolver cr =context.getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (cur.moveToNext()) {
                    List<String> phones = new ArrayList<>();
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phones.add(phoneNo);

                        }
                        pCur.close();
                    }
                    Contact contact = new Contact(name, phones);
                    contacts.add(contact);
                }
                cur.close();
            }else{
                List<String> phone = new ArrayList<>();
                phone.add("");
                Contact c = new Contact("", phone);
                contacts.add(c);
            }
            subscriber.onNext(contacts);
            subscriber.onComplete();
        });
    } 
    public String getName() {
        return name;
    }

    public List<String> getPhoneNumber() {
        return phoneNumber;
    }

    public List<Contact> getContacts() {
        return contacts;
    }
}
