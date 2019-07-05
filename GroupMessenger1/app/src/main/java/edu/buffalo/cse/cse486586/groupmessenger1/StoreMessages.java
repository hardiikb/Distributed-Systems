package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class StoreMessages {
    private static final String TAG = "storage";//OnPTestClickListener.class.getName();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private final ContentResolver sContentResolver;
    private final Uri sUri;
    private int mCount;
    private ContentValues sContentValue;

    String sData;

    public StoreMessages(int count ,String msg, ContentResolver cr){
        mCount = count;
        sData = msg;
        sContentResolver = cr;
        sUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
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

}
