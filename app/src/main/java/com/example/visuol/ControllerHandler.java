package com.example.visuol;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.util.Log;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A class which handles all statuses and methods regarding the controller in MainActivity
 */
public class ControllerHandler {
    private static final String TAG = "ControllerHandler";

    private static final float MINIMUM_SWIPE_SPEED = 1.0f;
    private PointF touchPosPrevious = new PointF(0.5f, 0.5f);
    private PointF touchPos = new PointF(0.5f, 0.5f);
    private PointF touchVector = new PointF(0.0f, 0.0f);
    private Handler updateTouchVectorHandler;
    /** The time between each swipe-speed update, in milliseconds. */
    private int UPDATE_TOUCH_VECTOR_INTERVAL = 50;
    private boolean isRunningUpdateTouchvector = false;

    private Controller controller;
    private ControllerManager controllerManager;
    private MainActivity mainActivity;

    public Controller getController() {
        return controller;
    }

    public ControllerManager getControllerManager() {
        return controllerManager;
    }

    public ControllerHandler(Context context, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        updateTouchVectorHandler = new Handler();
        controllerManager = new ControllerManager(context, new ControllerManager.EventListener() {
            @Override
            public void onApiStatusChanged(int i) {
                return;
            }

            @Override
            public void onRecentered() {
                return;
            }
        });
        controller = controllerManager.getController();
        controllerManager.start();
        controller.setEventListener(new Controller.EventListener() {
            @Override
            public void onUpdate() {
                onControllerUpdate();
            }
        });
    }

    boolean controllerClickButtonClicked = false;
    public void onControllerUpdate() {
        controller.update();
        if (controller.clickButtonState) {
            if (!controllerClickButtonClicked) {
                Log.i(TAG, "onControllerClickButton called in MainActivity");
                mainActivity.onControllerClickButton();
                controllerClickButtonClicked = true;
            }
        } else {
            controllerClickButtonClicked = false;
        }
        setUpdatingTouchVector(controller.isTouching);

        if (touchVector.length() > MINIMUM_SWIPE_SPEED) {
            Log.i(TAG, "onControllerSwipeX");
            mainActivity.onControllerSwipe(touchVector);
        }
    }

    public void setUpdatingTouchVector(boolean isTouching) {
        if (isTouching && !isRunningUpdateTouchvector) {
            startUpdatingTouchVector();
        }
        if (!isTouching && isRunningUpdateTouchvector) {
            stopUpdatingTouchVector();
        }
    }

    Runnable updateTouchVector = new Runnable() {
        @Override
        public void run() {
            try {
                touchVector.x = (touchPos.x - touchPosPrevious.x) / UPDATE_TOUCH_VECTOR_INTERVAL;
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                updateTouchVectorHandler.postDelayed(
                        updateTouchVector, UPDATE_TOUCH_VECTOR_INTERVAL);
            }
        }
    };

    void startUpdatingTouchVector() {
        updateTouchVector.run();
        isRunningUpdateTouchvector = true;
    }

    void stopUpdatingTouchVector() {
        updateTouchVectorHandler.removeCallbacks(updateTouchVector);
        isRunningUpdateTouchvector = false;
    }

}
