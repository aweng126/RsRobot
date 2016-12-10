package com.example.kingwen.dobot130.widgets;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.kingwen.dobot130.R;

/**
 * Created by kingwen on 2016/8/24.
 */
public class Dialog extends android.app.Dialog {

    //定义回调事件
    public interface  OnMyDialogListener{

        public void back(String address);
    }

    private EditText et_address;

    private Button btn_confirm;

    private Button btn_cancel;

    private String dialogname;


    private OnMyDialogListener onMyDialogListener;


    public Dialog(Context context, String name, OnMyDialogListener MyDialogListener) {
        super(context);
        dialogname=name;
        onMyDialogListener=MyDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_layout);

        setTitle(dialogname);

        initViews();

        initListeners();

    }

    private void initListeners() {
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.this.dismiss();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=et_address.getText().toString().trim();
                if(!"".equals(s)){
                    onMyDialogListener.back(s);
                }
                Log.d("回调back","新输入的地址为"+s);

                Dialog.this.dismiss();
            }
        });
    }


        private void initViews() {
        et_address= (EditText) findViewById(R.id.et_address_dialog);
        btn_cancel= (Button) findViewById(R.id.btn_cancel_dialog);
        btn_confirm= (Button) findViewById(R.id.btn_confirm_dialog);
    }
}
