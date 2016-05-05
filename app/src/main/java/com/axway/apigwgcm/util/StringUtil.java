package com.axway.apigwgcm.util;

import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by su on 12/7/2014.
 */
public class StringUtil {

    public static String capFirst(String input) {
        if (TextUtils.isEmpty(input))
            return input;
        String first = input.substring(0, 1);
        String rest = input.substring(1);
        return first.toUpperCase() + rest;
    }

    public static int strToIntDef(String input) {
        return strToIntDef(input, 0);
    }

    public static int strToIntDef(String input, int defVal) {
        if (TextUtils.isEmpty(input))
            return defVal;
        int rv = defVal;
        try {
            rv = Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            rv = defVal;
        }

        return rv;
    }

    public static String format(String msg, Object... args) {
        return String.format(Locale.getDefault(), msg, args);
    }

    public static long strToLongDef(String input) {
        return strToLongDef(input, 0);
    }

    public static long strToLongDef(String input, long defVal) {
        if (TextUtils.isEmpty(input))
            return defVal;
        long rv = defVal;
        try {
            rv = Long.parseLong(input);
        }
        catch (NumberFormatException e) {
            rv = defVal;
        }
        return rv;
    }

    public static String pluralize(String input) {
        if (TextUtils.isEmpty(input))
            return input;
        if (input.endsWith("s"))
            input = input + "e";
        String base = input;
        if (input.endsWith("y")) {
            base = input.substring(0, input.length()-2);
            input = base + "ie";
        }
        return input + "s";
    }

    /*
        private void takePhoto() {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                if (camera == null) {
                    String cameraId = null;
                    // Search for the front facing camera
                    CameraCharacteristics props;
                    try {
                        String[] ids = cameraMgr.getCameraIdList();
                        for (int i = 0; i < ids.length; i++) {
                            props = cameraMgr.getCameraCharacteristics(ids[i]);
                            int facing = props.get(CameraCharacteristics.LENS_FACING);
                            if (facing == CameraMetadata.LENS_FACING_BACK) {
                                Log.d(TAG, "Camera found");
                                cameraId = ids[i];
                                break;
                            }
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    if (cameraId == null)
                        return;
                    camera = cameraMgr.openCamera(cameraId, this, null);
                }
    //            camera.takePicture(null, null, this);
            }
        }

        public void onPictureTaken(byte[] data, CameraDevice camera) {
            Log.d(TAG, "photo taken");
            File f = getFilesDir();
            File out = new File(f, "test.jpg");
            try {
                out.createNewFile();
                FileOutputStream stream = new FileOutputStream(out);
                stream.write(data);
                stream.flush();
                stream.close();
                Log.d(TAG, "photo saved to " + out.getAbsolutePath());
            }
            catch(IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onOpened(CameraDevice camera) {
            try {
                camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            catch (CameraAccessException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    */
}
