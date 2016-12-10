package com.example.kingwen.dobot130.Services;

/**
 *
 * 蓝牙服务模块，用于和机械臂进行数据传输。
 *
 */

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.kingwen.dobot130.Activitities.MainActivity;
import com.example.kingwen.dobot130.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothService {
    private static final boolean D = true;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "MainActivity";
    private static final String TAG = "BluetoothService";
    private Context context;
    private AcceptThread mAcceptThread;
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final Handler mHandler;
    private int mState = 0;

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothService.this.mAdapter.listenUsingRfcommWithServiceRecord(BluetoothService.NAME, BluetoothService.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d(BluetoothService.TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            while (BluetoothService.this.mState != 3) {
                try {
                    BluetoothSocket socket = this.mmServerSocket.accept();
                    if (socket != null) {
                        synchronized (BluetoothService.this) {
                            switch (BluetoothService.this.mState) {
                                case 0:
                                case 3:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        Log.e(BluetoothService.TAG, "Could not close unwanted socket", e);
                                        break;
                                    }
                                case 1:
                                case 2:
                                    BluetoothService.this.connected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException e2) {
                    Log.e(BluetoothService.TAG, "accept() failed", e2);
                }
            }
            Log.i(BluetoothService.TAG, "=== END mAcceptThread ===");
            return;
        }

        public void cancel() {
            Log.d(BluetoothService.TAG, "cancel " + this);
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(BluetoothService.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "create() failed", e);
            }
            this.mmSocket = tmp;
        }

        public void run() {
            Log.i(BluetoothService.TAG, "=== BEGIN mConnectThread ===");
            setName("ConnectThread");
            BluetoothService.this.mAdapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                synchronized (BluetoothService.this) {
                    BluetoothService.this.mConnectThread = null;
                }
                BluetoothService.this.connected(this.mmSocket, this.mmDevice);
            } catch (IOException e) {
                BluetoothService.this.connectionFailed();
                try {
                    this.mmSocket.close();
                } catch (IOException e2) {
                    Log.e(BluetoothService.TAG, "unable to close() socket during connection failure", e2);
                }
                BluetoothService.this.start();
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    @SuppressLint({"SimpleDateFormat"})
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(BluetoothService.TAG, "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "temp sockets not created", e);
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            byte[] Recv_buf = new byte[1024];
            List<Byte> UARTBuffer = new ArrayList();
            UARTBuffer.clear();
            while (true) {
                try {
                    int bytes = this.mmInStream.read();
                    if (bytes == -1) {
                        UARTBuffer.clear();
                    } else if (bytes == 105 || UARTBuffer.size() != 0) {
                        UARTBuffer.add(Byte.valueOf((byte) bytes));
                        if (((Byte) UARTBuffer.get(UARTBuffer.size() - 1)).byteValue() == (byte) -106) {
                            if (UARTBuffer.size() == 15 || UARTBuffer.size() == 24) {
                                int i;
                                byte iCheck = (byte) 0;
                                for (i = 0; i < UARTBuffer.size() - 2; i++) {
                                    iCheck = (byte) (((Byte) UARTBuffer.get(i)).byteValue() + iCheck);
                                }
                                if (((Byte) UARTBuffer.get(UARTBuffer.size() - 2)).byteValue() == iCheck) {
                                    for (i = 0; i < UARTBuffer.size(); i++) {
                                        Recv_buf[i] = ((Byte) UARTBuffer.get(i)).byteValue();
                                    }
                                    BluetoothService.this.mHandler.obtainMessage(2, UARTBuffer.size(), -1, Recv_buf).sendToTarget();
                                    UARTBuffer.clear();
                                }
                            } else if (UARTBuffer.size() > 24) {
                                UARTBuffer.clear();
                            }
                        }
                    }
                } catch (IOException e) {
                    BluetoothService.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] message) {
            if (message == null) {
                Log.e(BluetoothService.TAG, "=== Send data is empty！ ===");
            }
            try {
                this.mmOutStream.write(message);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    public BluetoothService(Context context, Handler handler) {
        this.context = context;
        this.mHandler = handler;
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.mState + " -> " + state);
        this.mState = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(1);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        if (this.mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        setState(2);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        Message msg = this.mHandler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        setState(3);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        setState(0);
    }

    public void write(byte[] message) {
        synchronized (this) {
            if (this.mState != 3) {
                return;
            }
            ConnectedThread r = this.mConnectedThread;
            r.write(message);
        }
    }

    private void connectionFailed() {
        setState(1);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, this.context.getString(R.string.connected_failed));
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(1);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, this.context.getString(R.string.connection_break));
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

}