package com.example.kingwen.dobot130.Constants;

import java.security.PublicKey;

/**
 * 用于存放常量
 */
public class Constant {

    /**
     * 机械臂动作，包括前后左右动作，在Mainactivity中使用
     */
    public static String GESTURE_DOWN="down,one";
    public static String GESTURE_UP="up ,one";
    public static String GESTURE_LEFT="left,one";
    public static String GESTURE_RIGHT="right,one";
    public static String GESTURE_BACK="back,one";
    public static String GESTURE_GOAHEAD="go ahead,one";
    public static String GESTURE_CATCH="catch";
    public static String GESTURE_RELEASE="release";


    /**
     * 摇杆控制
     */

    public static final int BUTTON_PRESSED=11;
    public static final int BUTTON_RELEASED=12;
    public static final int JOYSTICK_UPDATE=13;


}
