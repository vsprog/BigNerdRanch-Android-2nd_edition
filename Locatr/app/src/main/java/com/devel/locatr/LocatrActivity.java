package com.devel.locatr;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocatrActivity extends SingleFragmentActivity {

    private static final String TAG="LocatrActivity";

    private static final int REQUEST_ERROR = 0;
    private static final int MY_LOCATION_REQUEST=1;

    @Override
    protected Fragment createFragment() {
        return LocatrFragment.newInstancs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, REQUEST_ERROR,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // Leave if services aren't available
                            finish();
                }
            });
            errorDialog.show();
        }
        // Next, check on "dangerous" permissions - to wit, location data availability.  As of Android 6.0, we need to check
        // at runtime - not  during installation.
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST);
        }
    }

    // So - do we have permission to proceed(i.e. have locations information)?  If not, just quit.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission granted!");
                    break;
                } else {
                    Log.i(TAG, "Permission denied!");
                    // not permitted to do our job..
                    finish();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}