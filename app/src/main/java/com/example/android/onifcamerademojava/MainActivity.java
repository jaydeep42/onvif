package com.example.android.onifcamerademojava;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rvirin.onvif.onvifcamera.OnvifDevice;
import com.rvirin.onvif.onvifcamera.OnvifListener;
import com.rvirin.onvif.onvifcamera.OnvifRequest;
import com.rvirin.onvif.onvifcamera.OnvifResponse;

import static com.rvirin.onvif.onvifcamera.OnvifDeviceKt.currentDevice;

/**
 * Created by vardan on 5/31/18.
 * Onvif camera login page.
 */

public class MainActivity extends AppCompatActivity implements OnvifListener {
    private static final String TAG = "MainActivity";
    private Toast toast = null;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_WRITE_PERM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int rw = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rw != PackageManager.PERMISSION_GRANTED) {
            requestExternalStoragePermission();
        }
    }

    @Override
    public void requestPerformed(OnvifResponse onvifResponse) {
        Log.d("INFO", onvifResponse.getParsingUIMessage());

        cancelToast();

        if (!onvifResponse.getSuccess()) {
            Log.e("Error", "request failed: " + onvifResponse.getRequest().getType() +
                    "\n Response: " + onvifResponse.getError());
            toast = Toast.makeText(this, "⛔️ Request failed: ${response.request.type}", Toast.LENGTH_SHORT);
            if (toast != null) {
                toast.show();
            }
        }
        // if GetServices have been completed, we request the device information
        else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetServices) {
            currentDevice.getDeviceInformation();
        }
        // if GetDeviceInformation have been completed, we request the profiles
        else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetDeviceInformation) {

            TextView textView = findViewById(R.id.explanationTextView);
            textView.setText(onvifResponse.getParsingUIMessage());
            toast = Toast.makeText(this, "Device information retrieved 👍", Toast.LENGTH_SHORT);
            showToast();

            currentDevice.getProfiles();

        }
        // if GetProfiles have been completed, we request the Stream URI
        else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetProfiles) {
            int profilesCount = currentDevice.getMediaProfiles().size();
            toast = Toast.makeText(this, profilesCount + " profiles retrieved 😎", Toast.LENGTH_SHORT);
            showToast();

            currentDevice.getStreamURI();

        }
        // if GetStreamURI have been completed, we're ready to play the video
        else if (onvifResponse.getRequest().getType() == OnvifRequest.Type.GetStreamURI) {

            Button button = findViewById(R.id.button);
            button.setText(getString(R.string.Play));

            toast = Toast.makeText(this, "Stream URI retrieved,\nready for the movie 🍿", Toast.LENGTH_SHORT);
            showToast();
        }

    }

    public void buttonClicked(View view) {

        // If we were able to retrieve information from the camera, and if we have a rtsp uri,
        // We open StreamActivity and pass the rtsp URI
        if (currentDevice.isConnected()) {
            String uri = currentDevice.getRtspURI();
            if (uri == null) {
                Toast.makeText(this, "RTSP URI haven't been retrieved", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(view.getContext(), StreamActivity.class);
            intent.putExtra("url", currentDevice.getRtspURI());
            startActivityForResult(intent, 0);
        } else {

            // get the information type by the user to create the Onvif device
            String ipAddress = getTextFromEditText(R.id.ipAddress);
            String login = getTextFromEditText(R.id.login);
            String password = getTextFromEditText(R.id.password);

            if (!TextUtils.isEmpty(ipAddress) &&
                    !TextUtils.isEmpty(login) &&
                    !TextUtils.isEmpty(password)) {

                // Create ONVIF device with user inputs and retrieve camera informations
                currentDevice = new OnvifDevice(ipAddress, login, password);
                OnvifListener onvifListener = this;
                currentDevice.setListener(onvifListener);
                currentDevice.getServices();

            } else {
                cancelToast();
                toast = Toast.makeText(this,
                        "Please enter an IP Address login and password",
                        Toast.LENGTH_SHORT);
                showToast();
            }
        }
    }

    private String getTextFromEditText(int id) {
        return ((EditText) findViewById(id)).getText().toString();
    }

    private void showToast() {
        if (toast != null) {
            toast.show();
        }
    }

    private void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    /**
     * Handles the requesting of the external storage write permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestExternalStoragePermission() {
        Log.w(TAG, "External storage write permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_WRITE_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_WRITE_PERM);
            }
        };

        Snackbar.make(findViewById(android.R.id.content), R.string.permission_extrenal,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }
}
