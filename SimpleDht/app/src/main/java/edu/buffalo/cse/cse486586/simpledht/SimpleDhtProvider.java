package edu.buffalo.cse.cse486586.simpledht;

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
import java.util.concurrent.TimeUnit;

import android.telephony.TelephonyManager;



public class SimpleDhtProvider extends ContentProvider {
    static final String TAG = "provider"; //GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    static final int SERVER_PORT = 10000;
    String myPort;
    private String[] ports = new String[]{REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
    private String[] refer = new String[]{"177ccecaec32c54b82d5aaafc18a2dadb753e3b1","208f7f72b198dadd244e61801abe1ec3a4857bc9","33d6357cfaaf0f72991b0ecd8c56da066613c089","abf0fd8db03e5ecb199a9b82929e9db79b909643","c25ddd596aa7c81fa12378fa725f706d54325d12"};
    private ArrayList<Socket> clients = new ArrayList<Socket>();
    private ArrayList<ArrayList<String>> hashes = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> lfiles = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> gfiles = new ArrayList<ArrayList<String>>();
    //private Integer index = 0;

    String[] allHashes = new String[5];
    ArrayList<String> hashesList= new ArrayList<String>();
    ArrayList<String> portsList = new ArrayList<String>();
    String[] allPorts = new String[]{"5562", "5556", "5554","5558", "5560"};
    String passMsg;
    String pred_hash;
    String succ_hash;
    String pred_port;
    String succ_port;



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

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
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index))*2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF( "delete" + ":" + selection + ":" + queryhash );


            }catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

        }


        if(selection.equals("@")){
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
        String keyHash = "";
        String key = (String) values.get("key");
        String value = (String) values.get("value");
        Log.e(TAG, hashesList.toString());
        try {
            if (hashesList.size() == 0 || hashesList.size() == 1) {

                keyHash = genHash(key);
                FileOutputStream outputStream;
                ArrayList<String> temp = new ArrayList<String>();
                temp.add(key);
                temp.add(keyHash);
                lfiles.add(temp);
                //gfiles.add(temp);
                outputStream = getContext().openFileOutput(keyHash, Context.MODE_PRIVATE);
                outputStream.write(value.getBytes());
                outputStream.close();
                //Log.e(TAG, "insert complted");

                return uri;
            }
            keyHash = genHash(key);
            while(keyHash.compareTo(hashesList.get(index)) > 0){
                if(index == hashesList.size()-1){
                    index = 0;
                    break;
                }
                index += 1;
            }
            Log.e(TAG ,"send oper");
            Log.e(TAG, portsList.get(index));
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index))*2);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF( "insert" + ":" + key + ":" + value + ":" + keyHash);

        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (Exception e){
            Log.e(TAG, "canr scoket");
        }

        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(getContext().TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        String[] columns = new String[]{"key", "value"};
        MatrixCursor mc = new MatrixCursor(columns);


        if(hashesList.size() == 0 && !(selection.equals("*") || selection.equals("@"))) {
            try {

                // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                FileInputStream fis = getContext().openFileInput(genHash(selection));
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                //////
                //Log.e("insert", sb.toString());
                mc.addRow(new Object[]{selection, sb.toString()});
            } catch (FileNotFoundException e) {
                Log.e("insert", "filenotfound");
            } catch (IOException e) {
                Log.e("insert", "IOexception");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return mc;

        }else if(hashesList.size() == 0 && (selection.equals("*") || selection.equals("@"))){

            Log.e(TAG, "being queries");
            Log.e(TAG, String.valueOf(lfiles.size()));
            for(int i=0; i<lfiles.size();i++){
                try {
                    //Log.e(TAG, lfiles.get(i));
                    //mc = new MatrixCursor(columns);
                    // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                    FileInputStream fis = getContext().openFileInput(lfiles.get(i).get(1));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    //////
                    //Log.e("insert", sb.toString());
                    mc.addRow(new Object[]{lfiles.get(i).get(0), sb.toString()});
                    //mc.moveToNext();
                } catch (FileNotFoundException e) {
                    Log.e("insert", "filenotfound");
                } catch (IOException e) {
                    Log.e("insert", "IOexception");
                }
                //lfiles.remove(i);

            }
            mc.moveToFirst();
            /*
            while (mc.moveToNext()) {
                Log.e(TAG, "hello");
                Log.e(TAG, "hello " + mc.getString(mc.getColumnIndex("key")));
                Log.e(TAG, "hello " +  mc.getString(mc.getColumnIndex("value")));

            }*/
            return mc;
        }else if(hashesList.size() == 1 && !(selection.equals("*") || selection.equals("@"))){
            try {

                Log.e(TAG, hashesList.toString() + " " + selection);
                // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                FileInputStream fis = getContext().openFileInput(genHash(selection));
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                //////
                //Log.e("insert", sb.toString());
                mc.addRow(new Object[]{selection, sb.toString()});

            } catch (FileNotFoundException e) {
                Log.e("insert", "filenotfound");
            } catch (IOException e) {
                Log.e("insert", "IOexception");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return mc;
        }else if(hashesList.size() == 1 && (selection.equals("*") || selection.equals("@"))){
            Log.e(TAG, "being queries");
            Log.e(TAG, String.valueOf(lfiles.size()));
            for(int i=0; i<lfiles.size();i++){
                try {
                    //Log.e(TAG, lfiles.get(i));
                    //mc = new MatrixCursor(columns);
                    // Reference : "https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app"
                    Log.e(TAG, hashesList.toString() + " " + selection);
                    FileInputStream fis = getContext().openFileInput(lfiles.get(i).get(1));
                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    //////
                    //Log.e("insert", sb.toString());
                    mc.addRow(new Object[]{lfiles.get(i).get(0), sb.toString()});
                    //mc.moveToNext();
                } catch (FileNotFoundException e) {
                    Log.e("insert", "filenotfound");
                } catch (IOException e) {
                    Log.e("insert", "IOexception");
                }
                //lfiles.remove(i);

            }
            mc.moveToFirst();
            /*
            while (mc.moveToNext()) {
                Log.e(TAG, "hello");
                Log.e(TAG, "hello " + mc.getString(mc.getColumnIndex("key")));
                Log.e(TAG, "hello " +  mc.getString(mc.getColumnIndex("value")));

            }*/
            return mc;
        }
        else if(hashesList.size() > 1  && !(selection.equals("*") || selection.equals("@"))){

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
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(index))*2);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF( "query" + ":" + selection + ":" + queryhash );
                //Log.e(TAG, "response received " + in.readUTF());
                String response = in.readUTF();
                String filename = response.split(":")[1];
                String value = response.split(":")[2];
                Log.e(TAG, filename);
                Log.e(TAG, value);
                mc.addRow(new Object[]{filename, value});
                return mc;

            }catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(hashesList.size() > 1 && selection.equals("@")){
            Log.e(TAG, "multiple queries");
            Log.e(TAG, gfiles.toString());
            for(int i=0; i<gfiles.size();i++){
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
            mc.moveToFirst();
            return mc;
        }else if(hashesList.size() > 1 && selection.equals("*")){
            try {
                for (int i = 0; i < portsList.size(); i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(portsList.get(i)) * 2);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    out.writeUTF("allquery" );
                    String response = in.readUTF();
                    if(!(response.equals("none"))){
                        String[] files = response.split(",")[0].split(":");
                        String[] values = response.split(",")[1].split(":");

                        for(int j = 0; j<files.length; j++){
                            mc.addRow(new Object[]{files[j], values[j]});
                        }
                    }


                    Log.e(TAG, response);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return mc;
        }

        return mc;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
            Log.e(TAG, "server doinbackground");
            ServerSocket serverSocket = sockets[0];

            try{
                while(true) {
                    Log.e(TAG,String.valueOf(clients.size()));
                    Socket server = serverSocket.accept();
                    clients.add(server);
                    //Log.e(TAG, server + "");
                    String hash = "";
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    String incoming = in.readUTF();
                    Log.e(TAG, incoming);
                    if(incoming.split(",")[0].equals("hashport")){
                        allPorts = incoming.split(",")[1].split(":");
                        allHashes = incoming.split(",")[2].split(":");

                        portsList = new ArrayList<String>(Arrays.asList(allPorts));
                        hashesList = new ArrayList<String>(Arrays.asList(allHashes));
                        Log.e(TAG, hashesList.toString());
                        Log.e(TAG, portsList.toString());
                    }

                    if(incoming.split(":")[0].equals("start")){
                        Log.e(TAG, "start");
                        String port = String.valueOf(Integer.parseInt(incoming.split(":")[1])/2);
                        hash = genHash(port);
                        ArrayList<String> port_hash = new ArrayList<String>();
                        port_hash.add(port);
                        port_hash.add(hash);

                        hashes.add(port_hash);

                        for (int i = 0; i < hashes.size() - 1; i++) {
                            // Find the minimum element in unsorted array
                            int min_idx = i;
                            for (int j = i + 1; j < hashes.size(); j++)
                                if (hashes.get(j).get(1).compareTo(hashes.get(min_idx).get(1)) < 0)
                                    min_idx = j;

                            // Swap the found minimum element with the first
                            // element
                            ArrayList<String> temp = hashes.get(min_idx);
                            hashes.set(min_idx, hashes.get(i));
                            hashes.set(i, temp);

                        }


                        for(int i=0; i<ports.length; i++){
                            try {
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(ports[i]));

                                String hash_ = "";
                                String port_ = "";
                                for(int j=0; j<hashes.size();j++){
                                    if(j == hashes.size()-1){
                                        port_ = port_ + hashes.get(j).get(0);
                                        hash_ = hash_ + hashes.get(j).get(1);
                                    }else {
                                        port_ = port_ + hashes.get(j).get(0) + ":";
                                        hash_ = hash_ + hashes.get(j).get(1) + ":";
                                    }
                                }
                                DataOutputStream outsoc = new DataOutputStream(socket.getOutputStream());
                                //DataInputStream in = new DataInputStream(socket.getInputStream());
                                outsoc.writeUTF("hashport" + "," + port_ + "," + hash_);
                                //String data = in.readUTF();

                                socket.close();
                            }catch (UnknownHostException e) {
                                Log.e(TAG, "ClientTask UnknownHostException");
                            }catch (IOException e) {
                                Log.e(TAG, "ClientTask socket IOException");
                            }
                        }


                    }

                    if(incoming.split(":")[0].equals("insert")){
                        Log.e(TAG, "insert");
                        Log.e(TAG, incoming);

                        String filename = incoming.split(":")[1];
                        String filedata = incoming.split(":")[2];
                        String filehash = incoming.split(":")[3];

                        ArrayList<String> gtemp = new ArrayList<String>();
                        gtemp.add(filename);
                        gtemp.add(filehash);
                        gfiles.add(gtemp);
                        FileOutputStream outputStream;
                        try {
                            outputStream = getContext().openFileOutput(filehash, Context.MODE_PRIVATE);
                            outputStream.write(filedata.getBytes());
                            outputStream.close();
                            Log.v("insert", "insert complted");

                        } catch (Exception e) {
                            Log.e("error", "File write failed");
                        }
                    }

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


                            //lfiles.remove(i);

                        }
                        //Log.e(TAG, files + ":::::::::::::" + values);
                        if(files.length() == 0){
                            out.writeUTF("none");
                        }else {
                            out.writeUTF(files + "," + values);
                        }
                    }

                    if(incoming.equals("deleteall")){
                        File[] files = getContext().getFilesDir().listFiles();
                        if(files != null) {
                            for (File file : files) {
                                file.delete();
                            }
                        }
                    }

                    if(incoming.split(":")[0].equals("delete")){
                        String filehash = incoming.split(":")[2];

                        File dir = getContext().getFilesDir();
                        File file = new File(dir, filehash);
                        file.delete();
                    }
                        /*
                        String filename = incoming.split(":")[1];
                        String filedata = incoming.split(":")[2];
                        String filehash = incoming.split(":")[3];

                        ArrayList<String> gtemp = new ArrayList<String>();
                        gtemp.add(filename);
                        gtemp.add(filehash);
                        gfiles.add(gtemp);
                        FileOutputStream outputStream;
                        try {
                            outputStream = getContext().openFileOutput(filehash, Context.MODE_PRIVATE);
                            outputStream.write(filedata.getBytes());
                            outputStream.close();
                            Log.v("insert", "insert complted");

                        } catch (Exception e) {
                            Log.e("error", "File write failed");
                        }*/


                    //Log.e(TAG, "client got connected");




                }
            }catch(IOException e){
                e.printStackTrace();
            }catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }

            return null;
        }

    }
    /*
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {


            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt("11108"));

                String msgToSend = "start" + ":" + msgs[0];
                String myHash = genHash(String.valueOf(Integer.parseInt(msgs[0])/2));
                Log.e(TAG,msgToSend);
                //Log.e(TAG, msgs[0] + " " + myHash);
                //Log.e(TAG, String.valueOf(msgs[0] +" "+ msgs[1] + "~"));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF(msgToSend);
                String data = in.readUTF();

                socket.close();
            }catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            }catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

            return null;
        }
    }*/
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {


            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt("11108"));

                String msgToSend = "start" + ":" + msgs[0];
                String myHash = genHash(String.valueOf(Integer.parseInt(msgs[0])/2));

                //Log.e(TAG, msgs[0] + " " + myHash);
                //Log.e(TAG, String.valueOf(msgs[0] +" "+ msgs[1] + "~"));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF(msgToSend);
                String data = in.readUTF();
                Log.e(TAG,data);

                socket.close();
            }catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            }catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }

            return null;
        }
    }
}
