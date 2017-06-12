/**
 * Copyright 2016-2017 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.by_syk.netupdown.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.by_syk.lib.storage.SP;

import static android.content.ContentValues.TAG;

/**
 * Created by By_syk on 2016-11-08.
 */

public class FloatTextView extends TextView {
    private float lastX = 0;
    private float lastY = 0;

    private float viewStartX = 0;
    private float viewStartY = 0;

    private float offsetY = 0;

    private long lastTapTime = 0;
    private int tapTimes = 0;

    private boolean isMoving = false,isPin=false;
    private SP sp;
    private SharedPreferences sharedPreferences;
    private static final int TIME_LONG_PRESS = 1200;

    private Runnable doubleTapRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMoving && onMoveListener != null) {
                onMoveListener.onDoubleTap();
            }
        }
    };

    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMoving && onMoveListener != null) {
                onMoveListener.onLongPress();
            }
        }
    };

    private OnMoveListener onMoveListener = null;

    public interface OnMoveListener {
        void onMove(int x, int y);
        void onDoubleTap();
        void onTripleTap();
        void onLongPress();
    }

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }

    public FloatTextView(Context context) {
        this(context, null);
        sharedPreferences=context.getSharedPreferences("com.by_syk.netupdown_preferences",Context.MODE_PRIVATE);

    }

    public FloatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedPreferences=context.getSharedPreferences("com.by_syk.netupdown_preferences",Context.MODE_PRIVATE);

    }

    public void setOffsetY(float offset) {
        offsetY = offset;
    }

    /*方法postDelayed的作用是延迟多少毫秒后开始运行，而removeCallbacks方法是删除指定的Runnable对象，使线程对象停止运行。*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastX = event.getRawX();
                lastY = event.getRawY();
                viewStartX = event.getX();
                viewStartY = event.getY();

                isMoving = false;
                long time = System.currentTimeMillis();
                if (time - lastTapTime < 600) {
                    ++tapTimes;
                    if (tapTimes == 2) {
                        postDelayed(doubleTapRunnable, 600);
                    } else if (tapTimes == 3) {
                        removeCallbacks(doubleTapRunnable);
                        if (onMoveListener != null) {
                            onMoveListener.onTripleTap();
                        }
                    }
                } else {
                    lastTapTime = time;
                    tapTimes = 1;
                    postDelayed(longPressRunnable, TIME_LONG_PRESS);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getRawX();
                float y = event.getRawY();
                isPin=sharedPreferences.getBoolean("pin",false);
                //Log.i(TAG, "onTouchEvent: "+isPin);
                if (Math.abs(x - lastX) > 1 || Math.abs(y - lastY) > 1) {
                    isMoving = true;
                    if (onMoveListener != null&&!isPin) {
                        onMoveListener.onMove((int) (x - viewStartX), (int) (y - viewStartY - offsetY));
                    }
                }
                lastX = x;
                lastY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
                removeCallbacks(longPressRunnable);
        }
        return true;
    }
}
