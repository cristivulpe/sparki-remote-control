/*
Copyright 2016 cristian.vulpe@gmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.swedesboro_woolwich.remotecontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Class responsible for the BlueTooth management and communication.
 */
public class BluetoothManagement {
    // Magic string for the Sparki's name (defaults to ArcBotics)
    private static final String SPARKI = "ArcBotics";
    private BluetoothSocket socket;
    private OutputStream os;
    private InputStream is;
    private StringCallback stringCallback;

    public StringCallback getStringCallback() {
        return stringCallback;
    }

    public void setStringCallback(StringCallback stringCallback) {
        this.stringCallback = stringCallback;
    }

    /**
     * Connect via bluetooth to Sparki.
     *
     * @return <code>true<code/> if the connection was successful, <code>false</code> otherwise.
     */
    public boolean connect() {
        boolean result = false;
        if (socket == null) {
            BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
            if (bluetooth != null && bluetooth.isEnabled()) {
                String mydeviceaddress = bluetooth.getAddress();
                String mydevicename = bluetooth.getName();
                int state = bluetooth.getState();
                String status = "'" + mydevicename + "': " + mydeviceaddress + "; state: " + state;
                System.out.println("Status: " + status);
                bluetooth.cancelDiscovery();
                Set<BluetoothDevice> devices = bluetooth.getBondedDevices();

                // iterate through all the devices till one finds 'ArcBotics'
                for (BluetoothDevice device : devices) {
                    System.out.println(device.getName() + ":" + device.getAddress());
                    if (SPARKI.equals(device.getName())) {
                        try {
                            // accordingly to this reported issue, a fallback procedure should be in place: http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                            try {
                                // connect to the serial channel (details here: http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html)
                                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                socket.connect();
                            } catch (Exception e) {
                                System.err.println("Exception connecting to bluetooth. Attempting fallback procedure.");
                                e.printStackTrace();
                                socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                                socket.connect();
                            }

                            System.out.println("Connected");
                            this.os = socket.getOutputStream();
                            this.is = socket.getInputStream();

                            LineReader lr = new LineReader();
                            Thread t = new Thread(lr);
                            t.start();
                            result = true;
                        } catch (Exception e) {
                            System.err.println("Exception caught!");
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Send a string command via BT interface. The method will automatically append a new line feed character.
     *
     * @param command - the command to be send
     */
    public void writeCommand(String command) {
        if (os != null) {
            try {
                String commandToWrite = command + "\n";
                os.write(commandToWrite.getBytes());
                os.flush();
                System.out.println("TBT: '" + command + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A class that will read lines coming from the bluetooth interface and send them to the string callblack for processing.
     */
    private class LineReader implements Runnable {

        public void run() {
            if (is != null && stringCallback != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        stringCallback.doWithString(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Close the bluetooth connections (streams and socket).
     */
    public void disconnect() {
        if (is != null) try {
            is.close();
            is = null;
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (os != null) try {
            os.close();
            os = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null) try {
            socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
