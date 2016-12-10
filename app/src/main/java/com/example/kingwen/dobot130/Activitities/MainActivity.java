package com.example.kingwen.dobot130.Activitities;

import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.SyncStateContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kingwen.dobot130.Services.BluetoothService;
import com.example.kingwen.dobot130.Constants.Constant;
import com.example.kingwen.dobot130.R;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * 主类。
 */
public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static byte[] BluetoothDataFrame = null;
    private static byte[] BluetoothDataFrameEnd = null;
    private static byte[] BluetoothDataFrameHead = null;
    public static final String DEVICE_NAME = "device_name";

    public static final String TAG= "MainActivity";
    public static final String TOAST = "toast";
    private static String VoiceMessage = "";
    private TextView TitleView;
    private ActionBar actionBar;
    private BluetoothAdapter bluetoothAdapter = null;
    private  static BluetoothService chatService = null;
    private Button btn_up;
    private Button btn_down;
    private Button btn_left;
    private Button btn_right;
    private Button btn_goahead;
    private Button btn_back;
    private Button btn_release;
    private Button btn_catch;
    private String mConnectedDeviceName = null;


    public  final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 0:
                        case 1:
                            MainActivity.this.TitleView.setText(R.string.title_not_connected);
                            return;
                        case 2:
                            MainActivity.this.TitleView.setText(R.string.title_connecting);
                            return;
                        case 3:
                            MainActivity.this.TitleView.setText(R.string.title_connected_to);
                            MainActivity.this.TitleView.append(MainActivity.this.mConnectedDeviceName);
                            MainActivity.this.chatService.write(MainActivity.BluetoothDataFrameHead);

                            Log.e("send message","sendmessage");

                            return;
                    }
                case 4:
                    MainActivity.this.mConnectedDeviceName = msg.getData().getString(MainActivity.DEVICE_NAME);
                    MainActivity.this.ToastToShowText(new StringBuilder(String.valueOf(MainActivity.this.getResources().getString(R.string.connected_to))).append(' ').append(MainActivity.this.mConnectedDeviceName).toString());
                    return;
                case 5:
                    MainActivity.this.ToastToShowText(msg.getData().getString(MainActivity.TOAST));
                    return;
                case 6:
                    MainActivity.this.ToastToShowText("你没有连接到一个设备");
                    return;

                case  Constant.BUTTON_PRESSED:
                    Log.e(TAG,"button pressed");

                    break;
                case Constant.BUTTON_RELEASED:

                    Log.e(TAG,"button released");
                    break;
                case Constant.JOYSTICK_UPDATE:

                    Log.e(TAG,"button update");
                    break;

                default:
                    return;
            }
        }
    };

    private static Toast toast = null;
    private Vibrator vibrator = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initListeners();

        DataFrameInit();
    }

    private void initListeners() {

        /**
         * ontouchlistener事件
         *
         */
        btn_down.setOnTouchListener(this);
        btn_down.setLongClickable(true);

        btn_up.setOnTouchListener(this);
        btn_up.setLongClickable(true);

        btn_left.setOnTouchListener(this);
        btn_left.setLongClickable(true);

        btn_right.setOnTouchListener(this);
        btn_right.setLongClickable(true);

        btn_goahead.setOnTouchListener(this);
        btn_goahead.setLongClickable(true);

        btn_back.setOnTouchListener(this);
        btn_back.setLongClickable(true);

        btn_catch.setOnTouchListener(this);
        btn_catch.setLongClickable(true);

        btn_release.setOnTouchListener(this);
        btn_release.setLongClickable(true);

    }

    private void initViews() {
        /**
         * 组件的绑定
         */
        btn_down= (Button) findViewById(R.id.btn_down);
        btn_up= (Button) findViewById(R.id.btn_up);
        btn_left= (Button) findViewById(R.id.btn_left);
        btn_right= (Button) findViewById(R.id.btn_right);

        btn_goahead= (Button) findViewById(R.id.btn_goahead);
        btn_back= (Button) findViewById(R.id.btn_back);
        btn_release= (Button) findViewById(R.id.btn_release);
        btn_catch= (Button) findViewById(R.id.btn_catch);

        this.actionBar = getSupportActionBar();
        //手机振动
        this.vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        this.actionBar.setDisplayShowCustomEnabled(true);
        this.actionBar.setDisplayShowTitleEnabled(false);
        this.actionBar.setCustomView(R.layout.custom_title);
        this.TitleView = (TextView) this.actionBar.getCustomView().findViewById(R.id.titleView);
        this.TitleView.setText(R.string.title_not_connected);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.bluetoothAdapter == null) {
            ToastToShowText((int) R.string.not_supported);
            finish();
            return;
        }
    }

    public void onStart() {
        super.onStart();
        if (!this.bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
        } else if (this.chatService == null) {
            this.chatService = new BluetoothService(getApplicationContext(), this.mHandler);
        }
    }

    public synchronized void onResume() {
        super.onResume();
        if (this.chatService != null && this.chatService.getState() == 0) {
            this.chatService.start();
        }
    }

    public synchronized void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.chatService != null) {
            this.chatService.stop();
        }
    }

    public void DataFrameInit() {
        int index;
        BluetoothDataFrameHead = new byte[42];
        BluetoothDataFrameEnd = new byte[42];
        BluetoothDataFrame = new byte[42];
        BluetoothDataFrameHead[0] = (byte) -75;
        BluetoothDataFrameHead[1] = (byte) 0;
        BluetoothDataFrameHead[2] = (byte) 0;
        BluetoothDataFrameHead[3] = (byte) 17;
        BluetoothDataFrameHead[4] = (byte) 17;
        BluetoothDataFrameHead[5] = (byte) 34;
        BluetoothDataFrameHead[6] = (byte) 34;
        BluetoothDataFrameHead[7] = (byte) 51;
        BluetoothDataFrameHead[8] = (byte) 51;
        for (index = 9; index < BluetoothDataFrameHead.length - 1; index++) {
            BluetoothDataFrameHead[index] = (byte) 0;
        }
        BluetoothDataFrameHead[41] = (byte) 91;
        BluetoothDataFrameEnd[0] = (byte) -75;
        BluetoothDataFrameEnd[1] = (byte) 68;
        BluetoothDataFrameEnd[2] = (byte) 68;
        BluetoothDataFrameEnd[3] = (byte) 85;
        BluetoothDataFrameEnd[4] = (byte) 85;
        BluetoothDataFrameEnd[5] = (byte) 102;
        BluetoothDataFrameEnd[6] = (byte) 102;
        BluetoothDataFrameEnd[7] = (byte) 119;
        BluetoothDataFrameEnd[8] = (byte) 119;
        for (index = 9; index < BluetoothDataFrameEnd.length - 1; index++) {
            BluetoothDataFrameEnd[index] = (byte) 0;
        }
        BluetoothDataFrameEnd[41] = (byte) 91;
        BluetoothDataFrame[0] = (byte) -75;
        BluetoothDataFrame[1] = (byte) 68;
        BluetoothDataFrame[2] = (byte) 68;
        BluetoothDataFrame[3] = (byte) 85;
        BluetoothDataFrame[4] = (byte) 85;
        BluetoothDataFrame[5] = (byte) 102;
        BluetoothDataFrame[6] = (byte) 102;
        BluetoothDataFrame[7] = (byte) 119;
        BluetoothDataFrame[8] = (byte) 119;
        for (index = 9; index < BluetoothDataFrame.length - 1; index++) {
            BluetoothDataFrame[index] = (byte) 0;
        }
        BluetoothDataFrame[41] = (byte) 91;
    }

    public static String readFile(Context mContext, String file, String code) {
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            int len = in.available();
            byte[] buf = new byte[len];
            in.read(buf, 0, len);
            return new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }


    public  static void sendMessage(String message) {
        if (MainActivity.chatService.getState() != 3) {

            Log.e("sendMeeeage","蓝牙未连接");

            /**
             * 设置弹出框，需要解决掉
             */

        } else if (message.length() > 0) {
            byte[] send = null;
            try {
                send =message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            for (int index = 1; index < BluetoothDataFrame.length - 1; index++) {
                if (index < send.length + 1) {
                    BluetoothDataFrame[index] = send[index - 1];
                } else {
                    BluetoothDataFrame[index] = (byte) 0;
                }
            }
            MainActivity.chatService.write(BluetoothDataFrame);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == -1) {
                    this.chatService.connect(this.bluetoothAdapter.getRemoteDevice(data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)));
                    return;
                }
                return;
            case 2:
                if (resultCode == -1) {
                    this.chatService = new BluetoothService(getApplicationContext(), this.mHandler);
                    return;
                }
                ToastToShowText((int) R.string.bt_not_enabled_leaving);
                finish();
                return;
            default:
                return;
        }
    }

    private void ensureDiscoverable() {
        if (this.bluetoothAdapter.getScanMode() != 23) {
            Intent discoverableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            discoverableIntent.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", 300);
            startActivity(discoverableIntent);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getRepeatCount() == 0) {
            Builder localBuilder = new Builder(this);
            localBuilder.setIcon(R.drawable.logo).setTitle(R.string.tips).setMessage(R.string.exit_confirm);
            localBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    if (MainActivity.this.chatService.getState() == 3) {
                        MainActivity.this.chatService.write(MainActivity.BluetoothDataFrameEnd);
                    }
                    MainActivity.this.finish();
                }
            });
            localBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.cancel();
                }
            }).create();
            localBuilder.show();
        } else if (keyCode == 82) {
            return false;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //setIconEnable(menu, true);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                startActivityForResult(new Intent(this, DeviceListActivity.class), 1);
                return true;

            case R.id.speechcontrol:
                Intent intent1=new Intent(MainActivity.this,RecognizeActivity.class);
                startActivity(intent1);
                return true;

            case R.id.discoverable:
                ensureDiscoverable();
                return true;

            case R.id.vrwatch:
                Intent intent2=new Intent(MainActivity.this,VrWatchActivity.class);
                startActivity(intent2);
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /**
     * 在菜单处显示图标
     */
  /*  public void setIconEnable(Menu menu, boolean enable) {
        try {
            Method m = Class.forName("com.android.internal.view.menu.MenuBuilder").getDeclaredMethod("setOptionalIconsVisible",
                    new Class[]{Boolean.TYPE});
            m.setAccessible(true);
            m.invoke(menu, new Object[]{Boolean.valueOf(enable)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public  void ToastToShowText(int showdata) {
        if (MainActivity.toast != null) {
            MainActivity.toast.setText(showdata);
            MainActivity.toast.setDuration(Toast.LENGTH_SHORT);
            MainActivity.toast.show();
            return;
        }
        MainActivity.toast = Toast.makeText(MainActivity.this, showdata, Toast.LENGTH_SHORT);
        MainActivity.toast.show();
    }

    public void ToastToShowText(String ShowText) {
        if (this.toast != null) {
            this.toast.setText(ShowText);
            this.toast.setDuration(Toast.LENGTH_SHORT);
            this.toast.show();
            return;
        }
        this.toast = Toast.makeText(this, ShowText, Toast.LENGTH_SHORT);
        this.toast.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch(v.getId()){

            case R.id.btn_down:
                Log.e(TAG,"btn_down");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_DOWN);
                break;
            case R.id.btn_left:
                Log.e(TAG,"btn_left");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_LEFT);
                break;
            case R.id.btn_right:
                Log.e(TAG,"btn_right");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_RIGHT);
                break;
            case R.id.btn_up:
                Log.e(TAG,"btn_up");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_UP);
                break;
            case R.id.btn_goahead:
                Log.e(TAG, "btn_goahead");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_GOAHEAD);

                break;
            case R.id.btn_back:
                Log.e(TAG,"btn_back");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_BACK);
                break;
            case R.id.btn_release:
                Log.e(TAG,"btn_release");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_RELEASE);
                break;
            case R.id.btn_catch:
                Log.e(TAG,"btn_catch");
                vibrator.vibrate(50);
                sendMessage(Constant.GESTURE_CATCH);
                break;

        }
        return false;
    }






}
