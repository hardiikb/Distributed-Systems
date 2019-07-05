package edu.buffalo.cse.cse486586.groupmessenger2;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;

import android.telephony.TelephonyManager;
import android.content.Context;

import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.*;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = "hardik"; //GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    static final int SERVER_PORT = 10000;
    private int count = 0;
    private int counter = 0;
    private int msgCount = 0;
    private String failed = "";
    private HashMap<String,String> seqNum = new HashMap<String,String>();
    private HashMap<String,ArrayList<Double>> msgToProposals = new HashMap<String, ArrayList<Double>>();
    private HashMap<String,ArrayList<Double>> four = new HashMap<String, ArrayList<Double>>();
    private HashMap<String,ArrayList<Double>> five = new HashMap<String, ArrayList<Double>>();
    private TreeMap<Double,String[]> seqMsg = new TreeMap<Double, String[]>();
    private TreeMap<Double,String[]> copy;
    //private PriorityQueue<Double> global = new PriorityQueue<Double>();
    private ArrayList<String> tempPorts = new ArrayList<String>(Arrays.asList(REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4));
    private ArrayList<String> alive = new ArrayList<String>(Arrays.asList(REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4));
    String[] passMsg;
    private Boolean flag = true;
    private String tag1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Log.e("Debugg", "OnCreate Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        seqNum.put(REMOTE_PORT0,".1");
        seqNum.put(REMOTE_PORT1,".2");
        seqNum.put(REMOTE_PORT2,".3");
        seqNum.put(REMOTE_PORT3,".4");
        seqNum.put(REMOTE_PORT4,".5");
///////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);

        /*
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    String msg = editText.getText().toString() + "\n";
                    //Log.e(TAG,msg + "");
                    new StoreMessages(count, msg, getContentResolver());
                    count += 1;
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                    return true;
                }
                return false;
            }
        });
        */
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                //Log.e(TAG,msg + "");
                //new StoreMessages(count, msg, getContentResolver());
                //count += 1;
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                editText.setText("");
            }
        });

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            //Log.e(TAG, "server doinbackground");
            ServerSocket serverSocket = sockets[0];

            try{
                while(true) {

                    Socket server = serverSocket.accept();

                    //Log.e(TAG, "server is listening");
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    passMsg = in.readUTF().split(":");

                    if(passMsg[passMsg.length-1].equals("multicast")) {
                        msgCount += 1;
                        //Log.e(TAG, "Message counter " + msgCount);
                        count += 1;
                        String msg = passMsg[0];
                        String sender = passMsg[1];
                        String receiver = passMsg[2];

                        String tag = passMsg[3];
                        String suffix = count + seqNum.get(receiver);


                        String reply = suffix + ":" + msg + ":" + receiver;
                        String[] queueData = new String[]{msg, sender, receiver, "undel"};
                        seqMsg.put(Double.parseDouble(suffix), queueData);

                        //Log.e(TAG, "before the update");
                        //Log.e(TAG, seqMsg.toString());
                        out.writeUTF(reply);
                    }
                    else{
                        String finalMsg = passMsg[0];
                        Double finalAgreed = Double.parseDouble(passMsg[1]);

                        if(finalAgreed.intValue() > count){
                            count = finalAgreed.intValue();
                        }
                        //Log.e(TAG, "FINAL: " + finalMsg + "," + " PROPOSED NO: " + finalAgreed);
                        tag1 = passMsg[2];

                        Log.e(TAG, tag1);

                        if(!tag1.equals("last")){
                            Log.e(TAG, "FAILED NODE " + failed );
                            for(int i=0;i<seqMsg.size();i++){
                                Object[] suffixkeys = seqMsg.keySet().toArray();
                                Object[] keyDetail = seqMsg.values().toArray();
                                if( ((String[])keyDetail[i])[1].equals(failed) || ((String[])keyDetail[i])[2].equals(failed) ){
                                    Log.e(TAG,"HELLO " + ((String[])keyDetail[i])[1].equals(failed));
                                    //Log.e(TAG, "matched at " + i);
                                    String[] updated = new String[]{seqMsg.get(suffixkeys[i])[0], seqMsg.get(suffixkeys[i])[1],seqMsg.get(suffixkeys[i])[2],"del" };
                                    //seqMsg.remove(suffixkeys[i]);
                                    seqMsg.put(100.00, updated);
                                    //global.add(finalAgreed);

                                }
                            }
                        }

                        for(int i=0;i<seqMsg.size();i++){
                            Object[] suffixkeys = seqMsg.keySet().toArray();
                            Object[] keyDetail = seqMsg.values().toArray();
                            if( ((String[])keyDetail[i])[0].equals(finalMsg) ){
                                //Log.e(TAG, "matched at " + i);
                                String[] updated = new String[]{seqMsg.get(suffixkeys[i])[0], seqMsg.get(suffixkeys[i])[1],seqMsg.get(suffixkeys[i])[2],"del" };
                                seqMsg.remove(suffixkeys[i]);
                                seqMsg.put(finalAgreed, updated);
                                //global.add(finalAgreed);

                                break;
                            }
                        }

                    }
                    Log.e(TAG,(count + seqNum.get(passMsg[2])) + " " + "MESSAGE: " + passMsg[0] + " " + passMsg[1] + "~");
                    Iterator it = seqMsg.entrySet().iterator();
                    TreeMap.Entry pair = (TreeMap.Entry)it.next();

                    if(!failed.equals("last")){
                        Iterator copyit = seqMsg.entrySet().iterator();
                        TreeMap.Entry copypair = (TreeMap.Entry)copyit.next();
                        while(((Object[]) copypair.getValue())[3].equals("undel") && ((Object[]) copypair.getValue())[1].equals(failed)){


                            copyit.remove();

                            //Log.e(TAG,"after removal");
                            //Log.e(TAG, pair.toString() + "nehehe");

                            if(!seqMsg.isEmpty()) {
                                copyit = seqMsg.entrySet().iterator();
                                copypair = (TreeMap.Entry) copyit.next();
                                //Log.e(TAG, "next pair is " + pair.getKey());
                            }
                        }

                    }

                    while(!seqMsg.isEmpty() && ((Object[]) pair.getValue())[3].equals("del")){
                        //Log.e(TAG,pair.getKey() + " will be removed");
                        while(((Object[]) pair.getValue())[1].equals(failed) && ((Object[]) pair.getValue())[3].equals("undel")){
                            it.remove();


                        }
                        new StoreMessages(counter, String.valueOf(((Object[]) pair.getValue())[0]), getContentResolver());
                        //Log.e(TAG, "counter is " + String.valueOf(counter));
                        counter += 1;
                        it.remove();

                        //Log.e(TAG,"after removal");
                        //Log.e(TAG, pair.toString() + "nehehe");

                        if(!seqMsg.isEmpty()) {
                            it = seqMsg.entrySet().iterator();
                            pair = (TreeMap.Entry) it.next();
                            //Log.e(TAG, "next pair is " + pair.getKey());
                        }
                    }
                    Log.e(TAG, "QUEUE" + String.valueOf(seqMsg.isEmpty()));

                    Object[] suffixkeys = seqMsg.keySet().toArray();
                    Object[] keyDetail = seqMsg.values().toArray();

                    for(int i=0;i<seqMsg.size();i++){
                        Log.e(TAG, "QUEUE: " + String.valueOf(suffixkeys[i]) + " " + seqMsg.get(suffixkeys[i])[1] + " " + seqMsg.get(suffixkeys[i])[2] + " " + seqMsg.get(suffixkeys[i])[3]);
                    }


                    //Log.e(TAG, "MESSAGE count:" + msgCount);



                }

            }catch(IOException e){
                e.printStackTrace();
            }

            publishProgress(passMsg);
            return null;
        }

        protected void onProgressUpdate(String...strings) {
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {


            //Log.e(TAG,"client doinbackgroud");

            ArrayList<Double> proposals = new ArrayList<Double>();


            for(int i=0 ; i<tempPorts.size() ; i++){

                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(tempPorts.get(i)));

                    String msgToSend = msgs[0] + ":" + msgs[1] + ":" + tempPorts.get(i) + ":" + "multicast";
                    //Log.e(TAG, String.valueOf(msgs[0] +" "+ msgs[1] + "~"));
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());


                    out.writeUTF(msgToSend);
                    String data = in.readUTF();
                    String[] incoming = data.split(":");

                    proposals.add(Double.parseDouble(incoming[0]));
                    msgToProposals.put(incoming[1], proposals);
                    socket.close();
                }catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                }catch (IOException e) {
                    Log.e(TAG, "Node " + tempPorts.get(i) + " failed");
                    if(tempPorts.size() == 5){
                        failed = tempPorts.get(i);
                        alive.remove(failed);
                    }
                    flag = false;
                    //Log.e(TAG, "ClientTask socket IOException");
                }
            }

            tempPorts = new ArrayList<String>(alive);
            Object[] keys = msgToProposals.keySet().toArray();
            //Log.e(TAG, String.valueOf(tempPorts.size()));
            //Log.e(TAG, String.valueOf(alive.size()));
            //Log.e(TAG, proposals.toString());

            for(int i=0;i<keys.length;i++) {

                String msg = String.valueOf(keys[i]);

                if(flag) {
                    if (msgToProposals.get(msg).size() == 5) {
                        Double agreed = Collections.max(msgToProposals.get(msg));

                        try {
                            for (int j = 0; j < tempPorts.size(); j++) {
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(tempPorts.get(j)));

                                String lastReply = msg + ":" + agreed + ":" + "last" + ":" + failed;
                                DataOutputStream last = new DataOutputStream(socket.getOutputStream());
                                last.writeUTF(lastReply);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "ClientTask socket IOException");
                        }
                        msgToProposals.remove(msg);

                    }
                }
                else {
                    if (msgToProposals.get(msg).size() == 4) {
                        Double agreed = Collections.max(msgToProposals.get(msg));

                        try {
                            for (int j = 0; j < tempPorts.size(); j++) {
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(tempPorts.get(j)));
                                Log.e(TAG, "four " + failed);
                                String lastReply = msg + ":" + agreed + ":" + failed;
                                DataOutputStream last = new DataOutputStream(socket.getOutputStream());
                                last.writeUTF(lastReply);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "ClientTask socket IOException");
                        }
                        msgToProposals.remove(msg);
                    }
                }
            }
            Log.e(TAG, String.valueOf(msgToProposals.isEmpty()));
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

}
