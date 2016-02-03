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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private final String MNU_CONNECT = "Connect";
    private final String MNU_DISCONNECT = "Disconnect";
    private final String MNU_CONNECTING = "Connecting";
    private final String MNU_DISCONNECTING = "Disconnecting";

    private String speechValue;
    private BluetoothManagement bm;
    private TextView bt;
    private TextView measuredDistance;
    private Button lightLeft;
    private Button lightCenter;
    private Button lightRight;

    private SeekBar distanceSlider;


    private Map<String, String> commands = new HashMap<>();

    {
        commands.put("forward", "f");
        commands.put("backward", "b");
        commands.put("left", "l");
        commands.put("right", "r");
        commands.put("close", "c");
        commands.put("open", "o");
        commands.put("stop", "s");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerCommandForView(R.id.back, "b");
        registerCommandForView(R.id.forward, "f");
        registerCommandForView(R.id.left, "l");
        registerCommandForView(R.id.right, "r");
        registerCommandForView(R.id.pause, "s");

        registerCommandForView(R.id.close, "c");
        registerCommandForView(R.id.open, "o");
        registerCommandForView(R.id.pauseGripper, "s");
        registerCommandForView(R.id.pauseDistance, "x");
        registerCommandForView(R.id.pauseLight, "y");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        FloatingActionButton emergency = (FloatingActionButton) findViewById(R.id.emergency);
        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("s");
            }
        });

        bt = (TextView) findViewById(R.id.bt);
        lightLeft = (Button) findViewById(R.id.lightLeft);
        lightCenter = (Button) findViewById(R.id.lightCenter);
        lightRight = (Button) findViewById(R.id.lightRight);
        distanceSlider = (SeekBar) findViewById(R.id.distanceSlider);
        distanceSlider.setOnSeekBarChangeListener(new SeekBarListener());

        measuredDistance = (TextView) findViewById(R.id.measuredDistance);

        bm = new BluetoothManagement();
    }

    private int progress = 0;

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            MainActivity.this.progress = progress;
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int angle = (MainActivity.this.progress * 18 / 10 - 90);
            // avoid the edge cases since this will force the servo
            if (angle > 80) angle = 80;
            if (angle < -80) angle = -80;
            MainActivity.this.bm.writeCommand("h:" + angle);

        }
    }

    private void registerCommandForView(int viewId, String command) {
        View view = findViewById(viewId);
        view.setOnClickListener(new CommandSender(command));
    }

    private class CommandSender implements View.OnClickListener {
        private String command;

        public CommandSender(String command) {
            this.command = command;
        }

        @Override
        public void onClick(View v) {
            sendCommand(command);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void sendCommand(String argument) {
        bm.writeCommand(argument);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechValue = result.get(0);
                    System.out.println("Speech value: '" + speechValue + "'.");
                    String command = commands.get(speechValue);

                    if (command == null) {
                        Toast.makeText(this, "Unrecognized command.", Toast.LENGTH_SHORT);
                    } else {
                        sendCommand(command);
                    }

                    if (!"cancel".equalsIgnoreCase(speechValue)) promptSpeechInput();
                }
                break;
            }

        }
    }


    public void doConnect(final MenuItem menuItem) {
        CharSequence title = menuItem.getTitle();
        System.out.println("Title: " + title);
        if (MNU_CONNECT.equals(title)) {
            menuItem.setTitle(MNU_CONNECTING);
            AsyncTask connectingTask = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean result = bm.connect();
                    bm.setStringCallback(new StringCallback() {
                        @Override
                        public void doWithString(final String s) {
                            System.out.println("BT: " + s);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.dispatchBTCommandToUI(s);
                                }
                            });

                        }
                    });
                    return result;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if (aBoolean != null && aBoolean) {
                        Toast.makeText(MainActivity.this, "Connected to Sparki ;-)", Toast.LENGTH_SHORT);
                        menuItem.setTitle(MNU_DISCONNECT);
                    } else {
                        Toast.makeText(MainActivity.this, "Could not connect to Sparky.", Toast.LENGTH_SHORT);
                        menuItem.setTitle(MNU_CONNECT);
                    }
                }
            };

            connectingTask.execute(new Void[]{});
        } else if (MNU_DISCONNECT.equals(title)) {
            menuItem.setTitle(MNU_DISCONNECTING);
            AsyncTask disConnectingTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    bm.setStringCallback(null);
                    bm.disconnect();
                    return null;
                }

                @Override
                protected void onPostExecute(Void nothing) {
                    super.onPostExecute(nothing);

                    Toast.makeText(MainActivity.this, "Disconnected from Sparki. See you next time!", Toast.LENGTH_SHORT);
                    menuItem.setTitle(MNU_CONNECT);
                }
            };
            disConnectingTask.execute(new Void[]{});
        }

    }

    private void dispatchBTCommandToUI(String s) {
        String[] parts = s.split(":");
        String command = parts[0];
        switch (command) {
            case "ok":
                bt.setText("Bluetooth Says: " + s);
                break;
            case "light":
                updateLightUI(parts[1], Integer.parseInt(parts[2]));
                break;
            case "distance":
                measuredDistance.setText("Distance: " + Double.parseDouble(parts[1]));
                break;
            default:
                break;
        }
    }

    private void updateLightUI(String position, int value) {
        int grayValue = 255 * value / 1024;
        int tone = Color.rgb(grayValue, grayValue, grayValue);

        switch (position) {
            case "l":
                lightLeft.setBackgroundColor(tone);
                break;
            case "r":
                lightRight.setBackgroundColor(tone);
                break;
            case "c":
                lightCenter.setBackgroundColor(tone);
                break;
            default:
                break;
        }
    }
}