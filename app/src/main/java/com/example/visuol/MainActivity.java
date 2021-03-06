package com.example.visuol;

import android.content.Intent;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.microedition.khronos.egl.EGLConfig;

/**
 * Group Project by Pieter and Jiaming.
 *
 * User will be able to see three dimensional objects used in Calculus III class and switch them by
 * clicking the panels.
 */
public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private static final String TAG = "HelloVrActivity";

    /** The total number of objects in the app. */
    private static final int TARGET_MESH_COUNT = 6;

    private int rotationInterval = 50;
    private Handler rotationHandler;
    private boolean targetPositionUpdated = false;

    /**
     * Establish the connection between the Controller and all actions that can be done with it.
     */
    private ControllerHandler controllerHandler;

    /**
     * The location of the user's last touch on the controller touchpad, from 0 to 1 in both
     * x and y directions. Valid region is a circle centered around [0.5, 0.5] with a radius
     * of 0.5.
     */

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 10.0f;

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] FORWARD_VEC = {0.0f, 0.0f, -1.0f, 1.f};

    /** Minimum distance away from the user the object could be generated. Unused. */
    private static final float MIN_TARGET_DISTANCE = 3.0f;
    /** Maximum distance away from the user the object could be generated. Unused. */
    private static final float MAX_TARGET_DISTANCE = 3.5f;

    private static final String OBJECT_SOUND_FILE = "audio/HelloVR_Loop.ogg";
    private static final String SUCCESS_SOUND_FILE = "audio/HelloVR_Activation.ogg";

    private static final float FLOOR_HEIGHT = -2.0f;

    private static final float ANGLE_LIMIT_OBJECT = 0.4f;
    private static final float ANGLE_LIMIT_CEILING = 0.5f;

    // The maximum yaw and pitch of the target object, in degrees. After hiding the target, its
    // yaw will be within [-MAX_YAW, MAX_YAW] and pitch will be within [-MAX_PITCH, MAX_PITCH].
    private static final float MAX_YAW = 100.0f;
    private static final float MAX_PITCH = 25.0f;

    private static final String[] OBJECT_VERTEX_SHADER_CODE =
            new String[] {
                    "uniform mat4 u_MVP;",
                    "attribute vec4 a_Position;",
                    "attribute vec2 a_UV;",
                    "varying vec2 v_UV;",
                    "",
                    "void main() {",
                    "  v_UV = a_UV;",
                    "  gl_Position = u_MVP * a_Position;",
                    "}",
            };
    private static final String[] OBJECT_FRAGMENT_SHADER_CODE =
            new String[] {
                    "precision mediump float;",
                    "varying vec2 v_UV;",
                    "uniform sampler2D u_Texture;",
                    "",
                    "void main() {",
                    "  // The y coordinate of this sample's textures is reversed compared to",
                    "  // what OpenGL expects, so we invert the y coordinate.",
                    "  gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));",
                    "}",
            };

    private int objectProgram;

    private int objectPositionParam;
    private int objectUvParam;
    private int objectModelViewProjectionParam;

    private float targetDistance = MAX_TARGET_DISTANCE;

    private TexturedMesh room;
    private Texture roomTex;

    /** The list which contains the textured mesh for each of the objects */
    private ArrayList<TexturedMesh> targetObjectMeshes;

    /**
     * The list which contains the texture (colors) for
     * each of the objects when it is not selected.
     */
    private ArrayList<Texture> targetObjectNotSelectedTextures;

    /**
     * The list which contains the texture (colors) for
     * each of the objects when it is selected.
     */
    private ArrayList<Texture> targetObjectSelectedTextures;

    /**
     * The int which refers to the index in each object-related list corresponding to the current
     * object which has been chosen. This index will identify which texture and textured mesh to use
     * in the each ArrayList.
     */
    private int curTargetObject;

    private Random random;

    private float[] targetPosition;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;

    private float[] modelTarget;
    private float[] modelRoom;

    private float[] ceilingTarget;

    private float[] tempPosition;
    private float[] headRotation;

    private GvrAudioEngine gvrAudioEngine;
    private volatile int sourceId = GvrAudioEngine.INVALID_ID;
    private volatile int successSourceId = GvrAudioEngine.INVALID_ID;

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        // Target object first appears directly in front of user.
        targetPosition = new float[] {0.0f, 0.0f, -MIN_TARGET_DISTANCE};
        tempPosition = new float[4];
        headRotation = new float[4];
        modelTarget = new float[16];
        modelRoom = new float[16];
        headView = new float[16];
        controllerHandler = new ControllerHandler(this,this);
        rotationHandler = new Handler();

        // Define ceiling location
        ceilingTarget = new float[16];
        Matrix.setIdentityM(ceilingTarget, 0);
        Matrix.translateM(ceilingTarget, 0, 0, 5, 0);

        startRotation();
        // Initialize 3D audio engine.
        gvrAudioEngine = new GvrAudioEngine(this,
                GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);

        random = new Random();
    }

    public void initializeGvrView() {
        setContentView(R.layout.common_ui);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of
        // supporting Daydream controller input for basic interactions using the existing Cardboard
        // trigger API. We want to fix this so we can use more Daydream Controller functionality.

        /*
        gvrView.enableCardboardTriggerEmulation();
        */

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        setGvrView(gvrView);
    }

    @Override
    public void onPause() {
        gvrAudioEngine.pause();
        controllerHandler.getControllerManager().stop();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        gvrAudioEngine.resume();
        controllerHandler.getControllerManager().start();
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE);

        objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
        objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

        Util.checkGlError("Object program params");

        Matrix.setIdentityM(modelRoom, 0);
        Matrix.translateM(modelRoom, 0, 0, FLOOR_HEIGHT, 0);

        // Avoid any delays during start-up due to decoding of sound files.

        // WE DON'T WANT ANY AUDIO RIGHT NOW
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        // Start spatial audio playback of OBJECT_SOUND_FILE at the model position. The
//                        // returned sourceId handle is stored and allows for repositioning the sound object
//                        // whenever the target position changes.
//                        gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
//                        sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
//                        gvrAudioEngine.setSoundObjectPosition(
//                                sourceId, targetPosition[0], targetPosition[1], targetPosition[2]);
//                        gvrAudioEngine.playSound(sourceId, true /* looped playback */);
//                        // Preload an unspatialized sound to be played on a successful trigger on the
//                        // target.
//                        gvrAudioEngine.preloadSoundFile(SUCCESS_SOUND_FILE);
//                    }
//                })
//                .start();

        updateTargetPosition();

        Util.checkGlError("onSurfaceCreated");

        try {
            room = new TexturedMesh(this, "CubeRoom.obj", objectPositionParam, objectUvParam);
            roomTex = new Texture(this, "CubeRoom_BakedDiffuse.png");
            targetObjectMeshes = new ArrayList<>();
            targetObjectNotSelectedTextures = new ArrayList<>();
            targetObjectSelectedTextures = new ArrayList<>();
            targetObjectMeshes.add(
                    new TexturedMesh(this, "QuadSphere.obj", objectPositionParam, objectUvParam));
            targetObjectNotSelectedTextures.add(new Texture(this, "QuadSphere_Blue_BakedDiffuse.png"));
            //The textures are the same for the QuadSphere because we want consistency with other objects for now
            targetObjectSelectedTextures.add(new Texture(this, "QuadSphere_Blue_BakedDiffuse.png"));
            targetObjectMeshes.add(
                    new TexturedMesh(this, "elliptic_paraboloid.obj", objectPositionParam, objectUvParam));
            targetObjectNotSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectMeshes.add(
                    new TexturedMesh(this, "hyperbolic_paraboloid.obj", objectPositionParam, objectUvParam));
            targetObjectNotSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectMeshes.add(
                    new TexturedMesh(this, "inverse_elliptic_paraboloid.obj", objectPositionParam, objectUvParam));
            targetObjectNotSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectMeshes.add(
                    new TexturedMesh(this, "sinusoidal_sheet.obj", objectPositionParam, objectUvParam));
            targetObjectNotSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectMeshes.add(
                    new TexturedMesh(this, "trig_exp.obj", objectPositionParam, objectUvParam));
            targetObjectNotSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
            targetObjectSelectedTextures.add(new Texture(this, "colorfulGradient.jpg"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to initialize objects", e);
        }
        curTargetObject = random.nextInt(TARGET_MESH_COUNT);
    }

    /** Updates the target object position. */
    private void updateTargetPosition() {
        Matrix.setIdentityM(modelTarget, 0);
        Matrix.translateM(modelTarget, 0, targetPosition[0], targetPosition[1], targetPosition[2]);
        Matrix.rotateM(modelTarget, 0, yawDegreeCount, 0, 1, 0);

        targetPositionUpdated = true;
        // Update the sound location to match it with the new target position.
        if (sourceId != GvrAudioEngine.INVALID_ID) {
            gvrAudioEngine.setSoundObjectPosition(
                    sourceId, targetPosition[0], targetPosition[1], targetPosition[2]);
        }
        Util.checkGlError("updateTargetPosition");
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);

        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);
        gvrAudioEngine.setHeadRotation(
                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
        // Regular update call to GVR audio engine.
        gvrAudioEngine.update();

        Util.checkGlError("onNewFrame");
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // The clear color doesn't matter here because it's completely obscured by
        // the room. However, the color buffer is still cleared because it may
        // improve performance.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        // Build the ModelView and ModelViewProjection matrices. TEST
        // for calculating the position of the target object.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        Matrix.multiplyMM(modelView, 0, view, 0, modelTarget, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawTarget();

        // Set modelView for the room, so it's drawn in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, modelRoom, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawRoom();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    /** Draw the target object. */
    public void drawTarget() {
        GLES20.glUseProgram(objectProgram);
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
        if (isLookingAt(modelTarget, ANGLE_LIMIT_OBJECT)) {
            targetObjectSelectedTextures.get(curTargetObject).bind();
        } else {
            targetObjectNotSelectedTextures.get(curTargetObject).bind();
        }
        targetObjectMeshes.get(curTargetObject).draw();
        Util.checkGlError("drawTarget");
    }

    /** Draw the room. */
    public void drawRoom() {
        GLES20.glUseProgram(objectProgram);
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
        roomTex.bind();
        room.draw();
        Util.checkGlError("drawRoom");
    }

    /*
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        if (isLookingAt(modelTarget, ANGLE_LIMIT_OBJECT)) {
            successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
            gvrAudioEngine.playSound(successSourceId, false );
            curTargetObject = (curTargetObject + 1) % TARGET_MESH_COUNT;
            yawDegreeCount = 0;
        }

        if (isLookingAt(ceilingTarget, ANGLE_LIMIT_CEILING)) {
            //For now just do the same thing as if you were looking at the object
            Intent newIntent = new Intent(MainActivity.this, HomeActivity.class);
            MainActivity.this.startActivity(newIntent);
        }
    }
    */

    // COMMENTED BECAUSE WE DON'T WANT TO MOVE OBJECT. This put the object in a new location.
    // We could try to use this to make the rotation.
  /*
  private void hideTarget() {
    float[] rotationMatrix = new float[16];
    float[] posVec = new float[4];

    // Matrix.setRotateM takes the angle in degrees, but Math.tan takes the angle in radians, so
    // yaw is in degrees and pitch is in radians.
    float yawDegrees = (random.nextFloat() - 0.5f) * 2.0f * MAX_YAW;
    float pitchRadians = (float) Math.toRadians((random.nextFloat() - 0.5f) * 2.0f * MAX_PITCH);

    Matrix.setRotateM(rotationMatrix, 0, yawDegrees, 0.0f, 1.0f, 0.0f);
    targetDistance =
        random.nextFloat() * (MAX_TARGET_DISTANCE - MIN_TARGET_DISTANCE) + MIN_TARGET_DISTANCE;
    targetPosition = new float[] {0.0f, 0.0f, -targetDistance};
    Matrix.setIdentityM(modelTarget, 0);
    Matrix.translateM(modelTarget, 0, targetPosition[0], targetPosition[1], targetPosition[2]);
    Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelTarget, 12);

    targetPosition[0] = posVec[0];
    targetPosition[1] = (float) Math.tan(pitchRadians) * targetDistance;
    targetPosition[2] = posVec[2];

    updateTargetPosition();
    curTargetObject = random.nextInt(TARGET_MESH_COUNT);
  }
  */

    /**
     * Check if user is looking at the target by comparing and multiplying vectors
     *
     * @return true if the user is looking at the target.
     */
    private boolean isLookingAt(float[] target, float angleLimit) {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, target, 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);
        float angle = Util.angleBetweenVectors(tempPosition, FORWARD_VEC);
        return angle < angleLimit;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRotation();
        controllerHandler.getControllerManager().stop();
    }

    public void onControllerClickButton() {
        Log.i(TAG, "onControllerClickButton run");
        if (isLookingAt(modelTarget, ANGLE_LIMIT_OBJECT)) {
            successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
            gvrAudioEngine.playSound(successSourceId, false );
            curTargetObject = (curTargetObject + 1) % TARGET_MESH_COUNT;
            yawDegreeCount = 0;
            targetPosition[1] = 0.0f;
        }

        if (isLookingAt(ceilingTarget, ANGLE_LIMIT_CEILING)) {
            //For now just do the same thing as if you were looking at the object
            Intent newIntent = new Intent(MainActivity.this, HomeActivity.class);
            MainActivity.this.startActivity(newIntent);
        }
    }

    /**
     * Run when a user swipe across the touchpad of the controller with a specific velocity
     * @param velocity The velocity of the swipe, in (% diameter moved) / millisecond
     */
    public void onControllerSwipe(PointF velocity) {
        Log.i(TAG, "onControllerSwipe run: velocity vector = " +
                velocity.x + ", " + velocity.y);
        yawDegreeCount += velocity.x * 500;
        targetPosition[1] -= velocity.y * 20;
        if (targetPosition[1] > 2) {
            targetPosition[1] = 2;
        }
        if (targetPosition[1] < -1) {
            targetPosition[1] = -1;
        }
    }

    /** How much the object is rotated by about the vertical axis. */
    private float yawDegreeCount = 0;
    Runnable rotationStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (targetPositionUpdated) {
                    yawDegreeCount += 0.4;
                    updateTargetPosition();
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                rotationHandler.postDelayed(rotationStatusChecker, rotationInterval);
            }
        }
    };

    void startRotation() {
        rotationStatusChecker.run();
    }

    void stopRotation() {
        rotationHandler.removeCallbacks(rotationStatusChecker);
    }
}
