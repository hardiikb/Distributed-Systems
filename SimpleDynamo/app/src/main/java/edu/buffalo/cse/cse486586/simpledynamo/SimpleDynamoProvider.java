package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.*;
import android.os.AsyncTask;
import java.io.IOException;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import android.database.MatrixCursor;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.telephony.TelephonyManager;
import android.view.ScaleGestureDetector;

public class SimpleDynamoProvider extends ContentProvider {
    static final String TAG = "provider"; //GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    static final int SERVER_PORT = 10000;
    String myPort;
    Uri uri;
    boolean flag = false;
    Integer failedcount = 0;
    Integer cc = 0;
    String store = "";
    String deadPort = "";


    private ArrayList<String> ports = new ArrayList<String>(Arrays.asList(REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4));
    private String[] portsarray = new String[]{REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
    private ArrayList<ArrayList<String>> hashes = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> gfiles = new ArrayList<ArrayList<String>>();
    ArrayList<String> hashesList= new ArrayList<String>();
    ArrayList<String> portsList = new ArrayList<String>();
    //HashMap storage[] = new HashMap[5];
    HashMap<String, ArrayList<ArrayList<String>>> storage = new HashMap<String, ArrayList<ArrayList<String>>>();
    String dead = "";
    ArrayList<ArrayList<String>> fi = new ArrayList<ArrayList<String>>();
    ReentrantLock lock = new ReentrantLock();
    Object obj = new Object();




    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        Log.e("delete", "delete called");
        File dir = getContext().getFilesDir();
        Log.e(TAG, dir.toString());

        if(!(selection.equals("*") || selection.equals("@"))){

            Integer index = 0;
            try {
                String queryhash = genHash(selection);
                while(queryhash.compareTo(hashesList.get(index)) > 0){
                    if(index == hashesList.size()-1){
                        index = 0;
                        break;
                    }
                    index += 1;
                }
                int count = 0;
                String coord = portsList.get(index);
                while(count<3) {
                    index = index % portsList.size();
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index)) * 2);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    if(count == 0) {
                        out.writeUTF("delete" + ":" + selection + ":" + "aaaa" + queryhash);
                    }else{
                        out.writeUTF("delete" + ":" + selection + ":" + coord + queryhash);
                    }
                    index += 1;
                    count += 1;
                }


            }catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

        }


        if(selection.equals("@")){
            gfiles = new ArrayList<ArrayList<String>>();
            File[] files = getContext().getFilesDir().listFiles();
            if(files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }

        if(selection.equals("*")) {
            try {
                for (int i = 0; i < portsList.size(); i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(i)) * 2);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    out.writeUTF("deleteall");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub

        Integer index = 0;
        Integer counter = 0;
        String key = (String) values.get("key");
        String value = (String) values.get("value");
        String keyHash = "";


        try {

            keyHash = genHash(key);
            while(keyHash.compareTo(hashesList.get(index)) > 0){
                if(index == hashesList.size()-1){
                    index = 0;
                    break;
                }
                index += 1;
            }
            int count = 0;
            String coord = portsList.get(index);
            while(count<3) {
                index = index % portsList.size();
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index)) * 2);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    /*
                    Object[] storagePorts = storage.keySet().toArray();
                    for(int i = 0; i<storagePorts.length;i++){

                        String port = String.valueOf(storagePorts[i]);
                        ArrayList<ArrayList<String>> filesArray = storage.get(port);
                        for(int j = 0 ; j< filesArray.size(); j++){
                            ArrayList<String> files = filesArray.get(j);


                            out.writeUTF("insert" + ":" + files.get(0) + ":" + files.get(1) + ":" + files.get(2) + ":" + String.valueOf(storagePorts[i]));

                        }
                    }*/


                    if(count == 0) {
                        out.writeUTF("insert" + ":" + key + ":" + value + ":" + "aaaa" + keyHash + ":" + portsList.get(index));
                    }else{
                        out.writeUTF("insert" + ":" + key + ":" + value + ":" + coord + keyHash + ":" + portsList.get(index));
                    }
                    Log.e(TAG, in.readUTF());

                }catch (IOException e){

                    Log.e("dead","insert" + ":" + key + ":" + value + ":" + keyHash + ":" + portsList.get(index));
                    /*
                    for(int i=0; i<portsarray.length; i++){
                        try {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsarray[i]));
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            out.writeUTF("storage" + "#" + store + "#" + portsList.get(index));
                        }catch (IOException et){
                            et.printStackTrace();
                        }

                    }*/

                    /*
                    store = store + key + ":" + value + ":" + keyHash + ":" + portsList.get(index) + "-";
                    try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get((index+1) % portsList.size())) * 2);
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF("storage" + "#" + store + "#" + portsList.get(index));
                    }catch (IOException et){
                        et.printStackTrace();
                    }*/

                }
                index += 1;
                count += 1;
            }

        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        return null;
    }
    /*
    public void sendStorage(HashMap<String, ArrayList<ArrayList<String>>> storage, int index){
        ArrayList<ArrayList<String>> filesArray = storage.get(portsList.get(index));
        Log.e("storage", String.valueOf(storage.containsKey(portsList.get(index))));
        for(int i=0; i<filesArray.size();i++){
            ArrayList<String> files = filesArray.get(i);
            Log.e("fuck", "insert" + ":" + files.get(0) + ":" + files.get(1) + ":" + files.get(2) + ":" );
            try {
                Socket soc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index)) * 2);
                DataOutputStream outsoc = new DataOutputStream(soc.getOutputStream());
                DataInputStream insoc = new DataInputStream(soc.getInputStream());
                outsoc.writeUTF("storage" + ":" + "hello");
                Log.e(TAG, insoc.readUTF());
            }catch (IOException e){
                e.printStackTrace();
            }
            //Log.e("storage", String.valueOf(files.size()));
        }

    }*/

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        Log.e("restart", "hello there");
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority("edu.buffalo.cse.cse486586.simpledynamo.provider");
        uriBuilder.scheme("content");
        uri = uriBuilder.build();

        portsList = new ArrayList<String>(Arrays.asList("5562","5556","5554","5558","5560"));
        hashesList = new ArrayList<String>(Arrays.asList("177ccecaec32c54b82d5aaafc18a2dadb753e3b1", "208f7f72b198dadd244e61801abe1ec3a4857bc9", "33d6357cfaaf0f72991b0ecd8c56da066613c089","abf0fd8db03e5ecb199a9b82929e9db79b909643","c25ddd596aa7c81fa12378fa725f706d54325d12"));

        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(getContext().TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        File[] files = getContext().getFilesDir().listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        //Log.e(TAG, myPort);
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");

        }
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stu
        String[] columns = new String[]{"key", "value"};
        MatrixCursor mc = new MatrixCursor(columns);
        Log.e("size", String.valueOf(gfiles.size()));

        if (!(selection.equals("*") || selection.equals("@"))) {
            String excep = "";
            Integer index = 0;
            try {
                String queryhash = genHash(selection);
                excep = queryhash;
                while (queryhash.compareTo(hashesList.get(index)) > 0) {
                    if (index == hashesList.size() - 1) {
                        index = 0;
                        break;
                    }
                    index += 1;
                }

                String coord = portsList.get(index);
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index)) * 2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF("query" + ":" + selection + ":" + "aaaa" + queryhash);
                //Log.e(TAG, "response received " + in.readUTF());
                String response = in.readUTF();
                String filename = response.split(":")[1];
                String value = response.split(":")[2];
                Log.e(TAG, filename);
                Log.e(TAG, value);
                mc.addRow(new Object[]{filename, value});
                return mc;

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {

                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get((index+1) % portsList.size())) * 2);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    out.writeUTF("query" + ":" + selection + ":" + portsList.get(index) + excep);
                    String response = in.readUTF();
                    String filename = response.split(":")[1];
                    String value = response.split(":")[2];
                    Log.e(TAG, filename);
                    Log.e(TAG, value);
                    mc.addRow(new Object[]{filename, value});
                    return mc;

                }catch (IOException et){
                    et.printStackTrace();
                }
                Log.e("fuckoff", "exception");

            }
        } else if (selection.equals("@")) {

            File[] files = getContext().getFilesDir().listFiles();
            //if (files != null) {
            Log.e(TAG, "multiple queries");
            Log.e(TAG, gfiles.toString());

            for (int i = 0; i < gfiles.size(); i++) {
                try {
                    //Log.e(TAG, lfiles.get(i));
                    //mc = new MatrixCursor(columns);
                    // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                    FileInputStream fis = getContext().openFileInput(gfiles.get(i).get(1));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    //////
                    Log.e("insert", sb.toString());
                    mc.addRow(new Object[]{gfiles.get(i).get(0), sb.toString()});
                    //mc.moveToNext();
                } catch (FileNotFoundException e) {
                    Log.e("insert", "filenotfound");
                } catch (IOException e) {
                    Log.e("insert", "IOexception");
                }
                //lfiles.remove(i);

            }

            //}
            mc.moveToFirst();
            return mc;

        } else if (selection.equals("*")) {

            for (int i = 0; i < portsList.size(); i++) {
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(i)) * 2);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    out.writeUTF("allquery");
                    String response = in.readUTF();
                    if (!(response.equals("none"))) {
                        String[] files = response.split(",")[0].split(":");
                        String[] values = response.split(",")[1].split(":");

                        for (int j = 0; j < files.length; j++) {
                            mc.addRow(new Object[]{files[j], values[j]});
                        }
                    }
                }catch (IOException e){
                    Log.e("fuckoff", "star query");
                }
            }

            return mc;
        }

        return null;
    }
    /*
    public void inserted(String filename, String filedata,String filehash, String port){

        ArrayList<String> gtemp = new ArrayList<String>();
        gtemp.add(filename);
        gtemp.add(filehash);
        gfiles.add(gtemp);

        FileOutputStream outputStream;
        try {
            outputStream = getContext().openFileOutput(filehash, Context.MODE_PRIVATE);
            outputStream.write(filedata.getBytes());
            outputStream.close();

        } catch (Exception e) {
            Log.e("error", "File write failed");
        }

        for(int i=0; i<portsList.size(); i++){
            if(!(deadPort.equals(portsList.get(i)))){
                try {

                    // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                    FileInputStream fis = getContext().openFileInput(portsList.get(i));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    String storedStr = sb.toString();
                    String[] storedArr = storedStr.split("-");

                    for(int j=0; j<storedArr.length;j++){
                        String[] data = storedArr[j].split(":");
                        try {
                            Socket outsoc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(data[3]) * 2);
                            DataOutputStream soc = new DataOutputStream(outsoc.getOutputStream());
                            soc.writeUTF("insert" + ":" + data[0] + ":" + data[1] + ":" + data[2] + ":" + data[3]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    File dir = getContext().getFilesDir();
                    File file = new File(dir, portsList.get(i));
                    file.delete();


                    Log.e("bbb", sb.toString());
                    //out.writeUTF("query" + ":" + input + ":" + sb.toString());
                    //////
                    //Log.e("insert", sb.toString());


                } catch (FileNotFoundException e) {
                    Log.e("insert", "filenotfound");
                } catch (IOException e) {
                    Log.e("insert", "IOexception");
                }

            }
        }
    }*/

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub

        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            Log.e(TAG, "server in background");
            ServerSocket serverSocket = sockets[0];

            try{
                while (true){
                    Socket server = serverSocket.accept();
                    Log.e(TAG,"client connected");
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    String incoming = in.readUTF();
                    //Log.e(TAG, incoming)
                    /*
                    if(incoming.split("#")[0].equals("storage")){
                        String port = incoming.split("#")[2];
                        String data = incoming.split("#")[1];
                        Log.e("dead", incoming );
                        FileOutputStream outputStream;
                        try {
                            outputStream = getContext().openFileOutput(port, Context.MODE_PRIVATE);
                            outputStream.write(data.getBytes());
                            outputStream.close();

                        } catch (Exception e) {
                            Log.e("error", "File write failed");
                        }
                    }*/
                    if(incoming.split(":")[0].equals("reload")) {
                        Log.e("hello", incoming);


                    }

                    if(incoming.split(":")[0].equals("back")){
                        String reqPort = incoming.split(":")[1];
                        String thisPort = incoming.split(":")[2];
                        for(int i=0; i<gfiles.size(); i++){
                            if(gfiles.get(i).get(1).substring(0,4).equals("aaaa")){
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqPort) * 2);
                                DataOutputStream outsoc = new DataOutputStream(socket.getOutputStream());
                                DataInputStream insoc = new DataInputStream(socket.getInputStream());
                                String changed = gfiles.get(i).get(1).replaceFirst("aaaa", thisPort);
                                outsoc.writeUTF("insert" + ":" + gfiles.get(i).get(0) + ":" + gfiles.get(i).get(2)  + ":" + changed + ":" + reqPort);
                                Log.e(TAG, insoc.readUTF());
                            }
                        }
                    }
                    if(incoming.split(":")[0].equals("insert")) {

                        Log.e("insert", incoming);
                        String filename = incoming.split(":")[1];
                        String filedata = incoming.split(":")[2];
                        String filehash = incoming.split(":")[3];
                        String portd = incoming.split(":")[4];

                        out.writeUTF("inserted");
                        //inserted(filename,filedata,filehash,portd);

                        ArrayList<String> gtemp = new ArrayList<String>();
                        gtemp.add(filename);
                        gtemp.add(filehash);
                        gtemp.add(filedata);

                        for(int i=0;i<gfiles.size(); i++){
                            if(gfiles.get(i).get(0).equals(filename)){
                                gfiles.remove(gfiles.get(i));
                                gfiles.add(gtemp);
                                break;
                            }
                        }
                        gfiles.add(gtemp);

                        FileOutputStream outputStream;
                        try {
                            outputStream = getContext().openFileOutput(filehash, Context.MODE_PRIVATE);
                            outputStream.write(filedata.getBytes());
                            outputStream.close();

                        } catch (Exception e) {
                            Log.e("error", "File write failed");
                        }

                    }


                    if(incoming.split(":")[0].equals("alive")) {
                        String fileString = "";
                        String valueString = "";

                        String reqPort = incoming.split(":")[1];
                        Log.e("alive", "msg req from " + incoming.split(":")[1]);
                        File[] files = getContext().getFilesDir().listFiles();

                        for(int i=0; i<gfiles.size(); i++){
                            if(gfiles.get(i).get(1).substring(0,4).equals(reqPort)){
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqPort) * 2);
                                DataOutputStream outsoc = new DataOutputStream(socket.getOutputStream());
                                DataInputStream insoc = new DataInputStream(socket.getInputStream());
                                String changed = gfiles.get(i).get(1).replaceFirst(reqPort, "aaaa");
                                outsoc.writeUTF("insert" + ":" + gfiles.get(i).get(0) + ":" + gfiles.get(i).get(2)  + ":" + changed + ":" + reqPort);
                                Log.e(TAG, insoc.readUTF());
                            }
                        }



                        /*
                        try {


                            if (files != null) {
                                for (File file : files) {
                                    FileInputStream fin = null;

                                    String filename = file.getName();

                                    if (filename.substring(0, 4).equals(reqPort)) {
                                        fin = getContext().openFileInput(filename);
                                        BufferedReader bufferedReader = new BufferedReader(
                                                new InputStreamReader(fin));
                                        String readValue = bufferedReader.readLine();
                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqPort) * 2);
                                        DataOutputStream outsoc = new DataOutputStream(socket.getOutputStream());
                                        outsoc.writeUTF("reload" + ":" + filename + ":" + readValue);
                                        //fileString = fileString + filename + ":";
                                        //valueString = valueString + readValue + ":";

                                    }


                                    ///Log.e("queryallllll", valFile + " " + readValue);

                                }

                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }*/
                    }
                        /*
                        for(int i=0; i<portsList.size(); i++){
                            if(!(deadPort.equals(portsList.get(i)))){
                                try {

                                    // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                                    FileInputStream fis = getContext().openFileInput(portsList.get(i));
                                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                                    BufferedReader bufferedReader = new BufferedReader(isr);
                                    StringBuilder sb = new StringBuilder();
                                    String line;
                                    while ((line = bufferedReader.readLine()) != null) {
                                        sb.append(line);
                                    }

                                    String storedStr = sb.toString();
                                    String[] storedArr = storedStr.split("-");

                                    for(int j=0; j<storedArr.length;j++){
                                        String[] data = storedArr[j].split(":");
                                        try {
                                            Socket outsoc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(data[3]) * 2);
                                            DataOutputStream soc = new DataOutputStream(outsoc.getOutputStream());
                                            soc.writeUTF("insert" + ":" + data[0] + ":" + data[1] + ":" + data[2] + ":" + data[3]);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    File dir = getContext().getFilesDir();
                                    File file = new File(dir, portsList.get(i));
                                    file.delete();


                                    Log.e("bbb", sb.toString());
                                    //out.writeUTF("query" + ":" + input + ":" + sb.toString());
                                    //////
                                    //Log.e("insert", sb.toString());


                                } catch (FileNotFoundException e) {
                                    Log.e("insert", "filenotfound");
                                } catch (IOException e) {
                                    Log.e("insert", "IOexception");
                                }

                            }
                        }*/


                    /*
                    if(incoming.split("#")[0].equals("storage")){
                        Log.e("aaa", incoming);
                        String filedata = incoming.split("#")[1];
                        String port = incoming.split("#")[2];
                        Log.e("aaa", port);
                        FileOutputStream outputStream;
                        try {
                            outputStream = getContext().openFileOutput(port, Context.MODE_PRIVATE);
                            outputStream.write(filedata.getBytes());
                            outputStream.close();

                        } catch (Exception e) {
                            Log.e("error", "File write failed");
                        }

                    }*/

                    /*
                    if(incoming.split(":")[0].equals("storage")){
                        Log.e("storage", incoming);
                        cc += 1;
                        Log.e("zzz", String.valueOf(cc));
                        String filename = incoming.split(":")[1];
                        String filedata = incoming.split(":")[2];
                        String filehash = incoming.split(":")[3];
                        String port = incoming.split(":")[4];
                        dead = port;

                        Integer index = portsList.indexOf(port);

                        storage[index].put(filename, new String[]{filedata,filehash});
                        /*
                        ArrayList<String> temp = new ArrayList<String>(Arrays.asList(filename,filedata,filehash));
                        ArrayList<ArrayList<String>> existing = storage.get(port);
                        existing.add(temp);
                        storage.put(port, existing);


                        Log.e("failedsize", String.valueOf(storage[index].size()));


                    }*/

                    if(incoming.split(":")[0].equals("query")){
                        Log.e(TAG, incoming.split(":")[1]);
                        String input = incoming.split(":")[1];
                        String inputhash = incoming.split(":")[2];

                        try {

                            // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                            FileInputStream fis = getContext().openFileInput(inputhash);
                            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                            BufferedReader bufferedReader = new BufferedReader(isr);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                sb.append(line);
                            }

                            out.writeUTF("query" + ":" + input + ":" + sb.toString());
                            //////
                            //Log.e("insert", sb.toString());
                            Log.e("query" , "file " + input);
                            Log.e("query", "hash " + sb.toString());

                        } catch (FileNotFoundException e) {
                            Log.e("insert", "filenotfound");
                        } catch (IOException e) {
                            Log.e("insert", "IOexception");
                        }

                    }

                    if(incoming.equals("allquery")) {
                        //Log.e(TAG, "gfiles size " + String.valueOf(gfiles.size()));
                        String files = "";
                        String values = "";
                        for (int i = 0; i < gfiles.size(); i++) {
                            try {
                                //Log.e(TAG, lfiles.get(i));
                                //mc = new MatrixCursor(columns);
                                // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                                FileInputStream fis = getContext().openFileInput(gfiles.get(i).get(1));
                                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                                BufferedReader bufferedReader = new BufferedReader(isr);
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    sb.append(line);
                                }
                                if(i == gfiles.size()-1){
                                    files = files + gfiles.get(i).get(0);
                                    values = values + sb.toString();
                                }else {
                                    files = files + gfiles.get(i).get(0) + ":";
                                    values = values + sb.toString() + ":";
                                }

                                Log.e("allquery", gfiles.get(i).get(0) + " " + sb.toString());
                                //mc.addRow(new Object[]{gfiles.get(i).get(0), sb.toString()});
                                //mc.moveToNext();
                            } catch (FileNotFoundException e) {
                                Log.e("insert", "filenotfound");
                            } catch (IOException e) {
                                Log.e("insert", "IOexception");
                            }

                        }
                        //Log.e(TAG, files + ":::::::::::::" + values);
                        if(files.length() == 0){
                            out.writeUTF("none");
                        }else {
                            out.writeUTF(files + "," + values);
                        }
                    }

                    if(incoming.equals("deleteall")){
                        gfiles = new ArrayList<ArrayList<String>>();
                        File[] files = getContext().getFilesDir().listFiles();
                        if(files != null) {
                            for (File file : files) {
                                file.delete();
                            }
                        }
                    }

                    if(incoming.split(":")[0].equals("delete")){
                        gfiles = new ArrayList<ArrayList<String>>();
                        String filehash = incoming.split(":")[2];

                        File dir = getContext().getFilesDir();
                        File file = new File(dir, filehash);
                        file.delete();
                    }

                }
            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.e("dhdj", "hejje");
            Log.e("alive", msgs[0] + " is alive");
            String port = String.valueOf(Integer.parseInt(msgs[0])/2);
            Integer index = portsList.indexOf(port);
            Log.e("ppp", port);
            gfiles = new ArrayList<ArrayList<String>>();
            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(port) * 2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("deleteall");
            }catch (IOException e){
                e.printStackTrace();
            }

            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get((index + 1) % portsList.size())) * 2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF("alive" + ":" + port);
            } catch (IOException et) {
                et.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get((index + 4) % portsList.size())) * 2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("back" + ":" + port + ":" + portsList.get((index + 4) % portsList.size()));
            }catch (IOException e){
                e.printStackTrace();
            }

            try{
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get((index + 3) % portsList.size())) * 2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("back" + ":" + port + ":" + portsList.get((index + 3) % portsList.size()));
            }catch (IOException e){
                e.printStackTrace();
            }

            //gfiles = new ArrayList<ArrayList<String>>();



            /*
            for (int i = 0; i < portsarray.length; i++) {

                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsarray[i]));
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("alive" + ":" + Integer.parseInt(msgs[0]) / 2);
                } catch (IOException et) {
                    et.printStackTrace();
                }
            }


            /*
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt("11108"));

                String msgToSend = "start" + ":" + msgs[0];
                String myHash = genHash(String.valueOf(Integer.parseInt(msgs[0])/2));

                //Log.e(TAG, msgs[0] + " " + myHash);
                //Log.e(TAG, String.valueOf(msgs[0] +" "+ msgs[1] + "~"));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(msgToSend);

                socket.close();
            }catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            }catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }*/
            return  null;
        }
    }



}

