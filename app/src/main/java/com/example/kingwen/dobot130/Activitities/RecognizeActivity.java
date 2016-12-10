package com.example.kingwen.dobot130.Activitities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.kingwen.dobot130.Constants.Constant;
import com.example.kingwen.dobot130.R;
import com.example.kingwen.dobot130.Utils.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *本类用于语音控制模块，通过讯飞语音识别模块进行机械人模块的控制
 *
 * 数据通过mainActivity中的bluetoothservice对象来进行传输
 *
 */
public class RecognizeActivity extends AppCompatActivity {
    //TAG
    private static String TAG=MainActivity.class.getSimpleName();

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private Toast mToast;

    //点击开始录音按钮
    private ImageButton btn_start_record;

    /**
     * 语音识别结果
     */
    private  String recognizeResult="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_recognize);

        initView();

        initListener();

    }

    private void initListener() {

        btn_start_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

               int action= event.getAction();

                Log.e("action",action+"");
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        Log.e("motionEvent","action down");

                        mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                        mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
                        mIatDialog.setListener(recognizerDialogListener);
                        mIatDialog.show();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e("motionEvent","Action up");
                        break;
                }
                return false;
            }


        });
    }

    /**
     * 听写UI监听器
     */
    public RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {

        public void onResult(RecognizerResult results, boolean isLast) {


            //显示结果
            recognizeResult=getRrcongnizedResult(results);

            String str = JsonParser.parseIatResult(results.getResultString()).replace(".", "\r\n").replace("。", "\r\n").replace("!", "\r\n").replace("?", "\r\n");


            //Log.e("这里显示识别结果", str);

            /**
             * 向我们的UI界面的更新 发送数据ManagerResult(recognizeResult)
             */

            ManagerResult(str);

            recognizeResult="";
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
            Log.e("recognizeListener", "onError");
        }

    };


    /**
     * 通过识别结果，然后对数据进行传输控制
     * @param mMessage
     */
    private  void ManagerResult(String mMessage) {

        if(mMessage.contains("上")||mMessage.contains("up")){
            MainActivity.sendMessage(Constant.GESTURE_UP);
        }

        if(mMessage.contains("下")||mMessage.contains("down")){
            MainActivity.sendMessage(Constant.GESTURE_DOWN);
        }

        if(mMessage.contains("左")||mMessage.contains("left")){
            MainActivity.sendMessage(Constant.GESTURE_LEFT);
        }

        if(mMessage.contains("右")||mMessage.contains("right")){
            MainActivity.sendMessage(Constant.GESTURE_RIGHT);
        }

        if(mMessage.contains("前")||mMessage.contains("forward")||mMessage.contains("go ahead")){
            MainActivity.sendMessage(Constant.GESTURE_GOAHEAD);
        }

        if(mMessage.contains("后")||mMessage.contains("back")){
            MainActivity.sendMessage(Constant.GESTURE_BACK);
        }

        if(mMessage.contains("抓")||mMessage.contains("catch")){
            MainActivity.sendMessage(Constant.GESTURE_CATCH);
        }

        if(mMessage.contains("放")||mMessage.contains("release")){
            MainActivity.sendMessage(Constant.GESTURE_RELEASE);
        }

        Log.e("ManagerResult","sendMessage");

    }

    private void initView() {

        //点击开始识别的按钮
        btn_start_record= (ImageButton) findViewById(R.id.start_record_Mainactivity);

        //初始化识别对象，可以根据回调消息自定义界面
        mIat=SpeechRecognizer.createRecognizer(RecognizeActivity.this,null);

        setparam();

        //先将我们存储结果的hash表清空
        mIatResults.clear();

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(RecognizeActivity.this, null);

        mToast=Toast.makeText(this, "", Toast.LENGTH_SHORT);


    }

    private void setparam() {
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");



        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "2000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

      /*  // 设置音频保存路径，保存音频格式仅为pcm，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/iflytek/wavaudio.pcm");
*/
        // 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
        mIat.setParameter(SpeechConstant.ASR_DWA, "0");

    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }


    private String getRrcongnizedResult(RecognizerResult results) {

        String result=null;

        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        result=resultBuffer.toString();

        showTip(resultBuffer.toString());

        return  result;

    }
}
