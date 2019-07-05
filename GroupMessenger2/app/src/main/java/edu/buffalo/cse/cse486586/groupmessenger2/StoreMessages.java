package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.HashMap;

public class StoreMessages {
    private static final String TAG = "storage";//OnPTestClickListener.class.getName();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private final ContentResolver sContentResolver;
    private final Uri sUri;
    private int mCount;
    private ContentValues sContentValue;
    private Object md;
    String sData;

    public StoreMessages(int count , String msg,ContentResolver cr){
        sData = msg;
        mCount = count;
        //sData = msg;
        sContentResolver = cr;
        sUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        sContentValue = buildContentValue();

    }

    private Uri buildUri(String scheme, String authority) {
        Log.e(TAG, "BuidUri");
        Log.e(TAG, String.valueOf(mCount) + "" );
        Log.e(TAG, sData + "");
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        Log.e("Debugg","BuildUri called");
        return uriBuilder.build();
    }

    private ContentValues buildContentValue() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_FIELD, String.valueOf(mCount));
        cv.put(VALUE_FIELD, sData);

        sContentResolver.insert(sUri,cv);
        return cv;


        ///////// read files from internal storage ///////////


    }

    /*
                    String sender = String.valueOf(passMsg.keySet().toArray()[0]);
                    String msg = passMsg.get(sender)[0];
                    //String receiver = passMsg.get(sender)[1];


                    Log.e(TAG, sender);
                    Log.e(TAG, msg);
                    Log.e(TAG, receiver);

                    Log.e(TAG, map.get(sender).get(receiver) + " hello");
                    keyValue.put(receiver,100);
                    map.put(sender,keyValue);
                    Log.e(TAG, map.get(sender).get(receiver) + " hello");
                    if(keyValue.get("11108") == null){
                        Log.e(TAG, "true");
                    }else{
                        Log.e(TAG,"false");
                    }

                    Log.e(TAG, sender.equals(receiver) + "");

                    for(int i=0; i<5; i++) {
                        String receiver = tempPorts[i];
                        if (sender.equals(receiver)) {
                            Log.e(TAG, "if condition");
                            count = map.get(sender).get(receiver);
                            new StoreMessages(count, msg, getContentResolver());
                            keyValue.put(sender, count + 1);
                            map.put(sender, keyValue);
                            Log.e(TAG, map.get(sender).get(sender) + "");
                        } else {


                            if (map.get(sender).get(sender) == map.get(receiver).get(sender) + 1) {
                                Log.e(TAG, "if(else)");
                                count = map.get(receiver).get(sender);
                                new StoreMessages(count, msg, getContentResolver());
                                keyValue.put(sender, count + 1);
                                map.put(receiver, keyValue);

                            } else if (map.get(sender).get(sender) > map.get(receiver).get(sender) + 1) {
                                Log.e(TAG, "else if");
                                count = map.get(sender).get(sender);
                                buffered.put(count, msg);

                                for (Map.Entry<Integer, String> entry : buffered.entrySet()) {
                                    Integer key = entry.getKey();
                                    String value = entry.getValue();
                                    if (key.intValue() == map.get(receiver).get(sender).intValue() + 1) {
                                        new StoreMessages(key, value, getContentResolver());
                                        buffered.remove(key);
                                    }
                                    // ...
                                }

                            }
                        }
                        Log.e(TAG, msg + " has counter " + count + " on avd " + receiver);
                    }
                    Log.e(TAG, map.get("11108").toString());
                    Log.e(TAG, map.get("11112").toString());
                    Log.e(TAG, map.get("11116").toString());
                    Log.e(TAG, map.get("11120").toString());
                    Log.e(TAG, map.get("11124").toString());
                    */
    //new  StoreMessages(count, passMsg,getContentResolver());
    //count += 1;

}
