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
    private int updateTouchVectorInterval = 50;

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

    Runnable runningUpdateTouchVector = new Runnable() {
        @Override
        public void run() {
            try {

            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                updateTouchVectorHandler.postDelayed(
                        runningUpdateTouchVector, updateTouchVectorInterval);
            }
        }
    };

    void startRotation() {
        runningUpdateTouchVector.run();
    }

    void stopRotation() {
        updateTouchVectorHandler.removeCallbacks(runningUpdateTouchVector);
    }

}
