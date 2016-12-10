package com.example.kingwen.dobot130.widgets;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.kingwen.dobot130.Activitities.MainActivity;
import com.example.kingwen.dobot130.Constants.Constant;
import com.zeemote.zc.Controller;
import com.zeemote.zc.event.BatteryEvent;
import com.zeemote.zc.event.ButtonEvent;
import com.zeemote.zc.event.ControllerEvent;
import com.zeemote.zc.event.DisconnectEvent;
import com.zeemote.zc.event.IButtonListener;
import com.zeemote.zc.event.IJoystickListener;
import com.zeemote.zc.event.IStatusListener;
import com.zeemote.zc.event.JoystickEvent;
import com.zeemote.zc.ui.android.ControllerAndroidUi;


/**
 *摇杆控制类，用于控制我们的摇杆控制类
 */
public class Zeemote implements IStatusListener,IJoystickListener,IButtonListener {

    //TAG
    private static final String TAG="zeemote";
    //controller 对象
    private Controller mZeemoteController;
    //上下文
    private Context mContext;

    //用于信号的传输
    private static Handler mhandler=null;

    private  ControllerAndroidUi controllerAndroidUi;
    //构造方法

    //用来保存初始位置，然后确定摇杆的控制位置
    private int x=0,y=0;


    public Zeemote(Context context){
        mContext=context;

        /**
         * 注意这个地方原本是controller1 但是被废弃了，暂时改为handshake_not_ready
         */
        mZeemoteController=new Controller(1);
        mZeemoteController.addButtonListener(this);
        mZeemoteController.addJoystickListener(this);
        mZeemoteController.addStatusListener(this);

    }
    public Zeemote(Context context, Handler handler){
        mContext=context;

        mhandler=handler;
        /**
         * 注意这个地方原本是controller1 但是被废弃了，暂时改为handshake_not_ready
         */
        mZeemoteController=new Controller(1);
        mZeemoteController.addButtonListener(this);
        mZeemoteController.addJoystickListener(this);
        mZeemoteController.addStatusListener(this);

        

        if(mZeemoteController.isConnected()){
            Toast.makeText(mContext,"mZeemoteController connected",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(mContext,"mZeemoteController not connected",Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 连接
     */
    public void connect(){
        //创建连接对象
        controllerAndroidUi=new ControllerAndroidUi(mContext,mZeemoteController);
        //开始操作
        controllerAndroidUi.startConnectionProcess();

        Log.e(TAG,"连接成功");
    }


    /**
     * 按钮事件
     * @param buttonEvent
     */
    @Override
    public void buttonPressed(ButtonEvent buttonEvent) {

        Log.e(TAG,"buttonpressed");

        int id_button=buttonEvent.getButtonID();
        String label=buttonEvent.getController().getConfiguration().getButtonLabel(id_button);

        int gameAction=buttonEvent.getButtonGameAction();

        Log.e(TAG, "button id = " + id_button + "label =" + label + "gameAction" + gameAction);

        Message message=new Message();
        message.what= Constant.BUTTON_PRESSED;
        message.arg1=buttonEvent.getButtonID();
        mhandler.sendMessage(message);
    }

    @Override
    public void buttonReleased(ButtonEvent buttonEvent) {

        String buttonName=buttonEvent.getController().getConfiguration().getButtonLabel(buttonEvent.getButtonID());

        Log.e(TAG, "buttonreleased" + "buttonName= " + buttonName);

    }

    /**
     * 摇杆事件
     * @param joystickEvent
     */
    @Override
    public void joystickMoved(JoystickEvent joystickEvent) {


        int xmax=joystickEvent.getMaxX();
        int xmin=joystickEvent.getMinX();
        int ymin=joystickEvent.getMaxY();
        int ymax=joystickEvent.getMaxY();


        Log.e(TAG,"joystickmoved"+"xmax ="+xmax+"xmin= "+xmin+" ymax="+ymin+"ymin="+ymin);

        int xcurrent=joystickEvent.getX();
        int ycurrent=joystickEvent.getMaxY();

        int idjoy=joystickEvent.getJoystickID();

        Log.e(TAG, "xCurrent="+xcurrent+"yCurrent="+ycurrent+"idJoy"+idjoy);

        Message message=new Message();
        message.what=Constant.JOYSTICK_UPDATE;
        message.arg1=xcurrent;
        message.arg2=ycurrent;


        /**
         * 通过判断当前位置和之前的位置进行比较判断，然后进行相应的移动
         */
        
        if(xcurrent>x){
            //向左
            MainActivity.sendMessage(Constant.GESTURE_LEFT);
            x=xcurrent;
        }else if(xcurrent<x){
            //向右
            MainActivity.sendMessage(Constant.GESTURE_RIGHT);
            x=xcurrent;
        }

        if(ycurrent>y){
            //向上
            MainActivity.sendMessage(Constant.GESTURE_UP);
            y=ycurrent;
        }else if(ycurrent<y){
            //向下
            MainActivity.sendMessage(Constant.GESTURE_DOWN);
            y=ycurrent;
        }

    }

    @Override
    public void connected(ControllerEvent controllerEvent) {
        Log.e(TAG, "connected");
        com.zeemote.zc.Configuration configuration=controllerEvent.getController().getConfiguration();
        Log.e(TAG,"Connected to controller");
        Log.e(TAG,"Num Buttons ="+configuration.getButtonCount());
        Log.e(TAG,"Num Joysticks="+configuration.getJoystickCount());
    }

    @Override
    public void disconnected(DisconnectEvent disconnectEvent) {
        Log.e(TAG, "disconnect");
        Log.d(TAG, "Disconnected from controller: "
                + (disconnectEvent.isUnexpected() ? "unexpected" : "expected"));
        if (mZeemoteController != null) {
            Log.d(TAG, "Removing Zee listeners.");
            mZeemoteController.removeStatusListener(this);
            mZeemoteController.removeJoystickListener(this);
            mZeemoteController.removeButtonListener(this);
        }
    }
    @Override
    public void batteryUpdate(BatteryEvent batteryEvent) {
        Log.e(TAG,"batteryUpdate");

        int id=batteryEvent.getController().getId();

        int max=batteryEvent.getMaximumLevel();

        int min=batteryEvent.getMinimumLevel();

        int warn=batteryEvent.getWarningLevel();

        int cur=batteryEvent.getCurrentLevel();

        int pctLeft = (int) (((float) (cur - min) / (float) (max - min)) * 100);

        Log.e("batteryUpdate","id:"+id+"max:"+max+"min:"+min+"warn:"+warn+"current:"+cur+"pctleft:"+pctLeft);

        //当前电量小于警告值，则toast提示
        if(cur<warn){

       }

    }

}
