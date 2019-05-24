package com.revolut.kataykin.revolutcurrency.op;

import android.os.Handler;
import android.os.Message;

import com.revolut.kataykin.revolutcurrency.view.DoEvent;

public class HandlerOp {

    public static class MyHandler extends Handler {

        private DoEvent onEvent;

        public MyHandler(DoEvent onEvent) {
            this.onEvent = onEvent;
        }


        @Override
        public void handleMessage(Message msg) {
            if (onEvent != null)
                onEvent.run();
        }
    }
}
