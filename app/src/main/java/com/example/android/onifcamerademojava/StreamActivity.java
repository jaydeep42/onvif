package com.example.android.onifcamerademojava;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.onifcamerademojava.patch.SafeFaceDetector;
import com.example.android.onifcamerademojava.util.FaceUtil;
import com.example.android.onifcamerademojava.util.TextUtil;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.pedro.vlc.VlcListener;
import com.pedro.vlc.VlcVideoLibrary;

/**
 * Created by vardan on 5/17/18.
 * Performs streaming and face detection
 */

public class StreamActivity extends AppCompatActivity implements VlcListener, View.OnClickListener, TextureView.SurfaceTextureListener {
    private static final String TAG = "StreamActivity";
    private static final int waitTimeInMilliseconds = 200;
    private static String whereToSave;
    private VlcVideoLibrary vlcVideoLibrary = null;
    private TextureView textureView;
    private TextView textViewDetectedFaces;
    private TextView textViewDetectedTexts;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        // Keep screen on while streaming.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Directory where images to be saved
        whereToSave = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/faces/";

        textureView = findViewById(R.id.textureView);
        textViewDetectedFaces = findViewById(R.id.textViewDetectedFaces);
        textViewDetectedTexts = findViewById(R.id.textViewDetectedTexts);
        textureView.setSurfaceTextureListener(this);

        url = getIntent().getStringExtra("url");

        Button captureFaces = findViewById(R.id.capture_faces);
        captureFaces.setOnClickListener(this);

    }


    @Override
    public void onComplete() {
        Toast.makeText(this, "Video loading...", Toast.LENGTH_LONG).show();
        runDetection();
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error loading video...", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onClick(View view) {
        capture();
        Toast.makeText(this, R.string.faces_saved, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        vlcVideoLibrary = new VlcVideoLibrary(this, this, textureView);
        vlcVideoLibrary.play(url);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    /**
     * Continuously detects faces while streaming is live by waitTimeInMilliseconds frequency.
     * You can change waitTimeInMilliseconds of your own to get your desirable speed
     * of updating detected people number message.
     */
    private void runDetection() {
        new Thread(new Runnable() {
            public void run() {
                while (vlcVideoLibrary.isPlaying()) {
                    detect(textureView.getBitmap(), false);
                    try {
                        Thread.sleep(waitTimeInMilliseconds);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Captures image and detected faces.
     */
    private void capture() {
        new Thread(new Runnable() {
            public void run() {
                if (vlcVideoLibrary.isPlaying()) {
                    detect(textureView.getBitmap(), true);
                }
            }
        }).start();
    }


    /**
     * Detects faces and updates people number.
     *
     * @param bitmap image for face detection
     * @param save   if true will save the detected faces and original image in the given path
     */
    public void detect(Bitmap bitmap, boolean save) {
        // A new face detector is created for detecting the face.
        // You can enable landmarks detection.
        //
        // Setting "tracking enabled" to false is recommended for detection with unrelated
        // individual images (as opposed to video or a series of consecutively captured still
        // images).  For detection on unrelated individual images, this will give a more accurate
        // result.  For detection on consecutive images (e.g., live video), tracking gives a more
        // accurate (and faster) result.
        //

        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.NO_LANDMARKS)
                .build();

        // A text recognizer is created to find text
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        // Per google mobile vision api:
        // This is a temporary workaround for a bug in the face detector with respect to operating
        // on very small images.  This will be fixed in a future release.  But in the near term, use
        // of the SafeFaceDetector class will patch the issue.
        SafeFaceDetector safeDetector = new SafeFaceDetector(detector);
        checkIfReady(safeDetector);

        // Create a frame from the bitmap and run face and text detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = safeDetector.detect(frame);
        SparseArray<TextBlock> texts = textRecognizer.detect(frame);

        // Updating people number and detected texts
        updateDetectedText(TextUtil.concatenate(texts));
        updatePeopleNumber(faces.size());

        if (save) {
            FaceUtil.save(faces, bitmap, whereToSave);
        }

        // Although detector may be used multiple times for different images, it should be released
        // when it is no longer needed in order to free native resources.
        safeDetector.release();
        textRecognizer.release();
    }

    private void updatePeopleNumber(final int number) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDetectedFaces.setText(getString(R.string.people_number, number));
            }
        });
    }

    private void updateDetectedText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDetectedTexts.setText(getString(R.string.detected_texts, text));
            }
        });
    }

    private void checkIfReady(SafeFaceDetector safeDetector) {
        if (!safeDetector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }
    }

}
