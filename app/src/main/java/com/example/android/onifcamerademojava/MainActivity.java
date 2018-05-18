package com.example.android.onifcamerademojava;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.pedro.vlc.VlcListener;
import com.pedro.vlc.VlcVideoLibrary;
import com.rvirin.onvif.onvifcamera.MediaProfile;
import com.rvirin.onvif.onvifcamera.OnvifDevice;
import com.rvirin.onvif.onvifcamera.OnvifListener;
import com.rvirin.onvif.onvifcamera.OnvifRequest;
import com.rvirin.onvif.onvifcamera.OnvifResponse;
import com.rvirin.onvif.onvifcamera.*;

import java.util.List;

/**
 * Created by vardan on 5/17/18.
 */

public class MainActivity extends AppCompatActivity implements OnvifListener, VlcListener {
    private GraphicOverlay mGraphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            Log.d("Error", "No internet access");
        }

//        OnvifDeviceKt.currentDevice = new OnvifDevice("193.159.244.134", "service", "Xbks8tr8vT");
        OnvifDeviceKt.currentDevice = new OnvifDevice("193.159.244.132", "service", "Xbks8tr8vT");
        OnvifListener onvifListener = this;
        OnvifDeviceKt.currentDevice.setListener(onvifListener);
        OnvifDeviceKt.currentDevice.getServices();


        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

    }


    @Override
    public void requestPerformed(OnvifResponse onvifResponse) {
        Log.d("INFO", onvifResponse.getParsingUIMessage());

        if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetDeviceInformation){
            OnvifDeviceKt.currentDevice.getProfiles();
        } else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetProfiles){
            List<MediaProfile> mediaProfile = OnvifDeviceKt.currentDevice.getMediaProfiles();
            if (mediaProfile.size() != 0) {
                OnvifDeviceKt.currentDevice.getStreamURI(mediaProfile.get(0));
            }
        } else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetStreamURI) {
            String uri = OnvifDeviceKt.currentDevice.getRtspURI();
            SurfaceView surfaceView = findViewById(R.id.surfaceView);
            VlcVideoLibrary vlcVideoLibrary = new VlcVideoLibrary(this, this, surfaceView);
            vlcVideoLibrary.play(uri);
        } else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetServices) {
            OnvifDeviceKt.currentDevice.getDeviceInformation();
        }

    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "Video loading...", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error loading video...", Toast.LENGTH_LONG).show();
    }


    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}
