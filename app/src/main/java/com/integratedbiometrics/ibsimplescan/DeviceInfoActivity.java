
package com.integratedbiometrics.ibsimplescan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerCountState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerQualityState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.PlatenState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.SegmentPosition;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class DeviceInfoActivity extends Activity
        implements IBScanListener, IBScanDeviceListener, ActivityCompat.OnRequestPermissionsResultCallback {


    /* Android M Permission request variable */
    private static final int IB_PERMISSION_REQUEST_CODE = 1000;
    private String[] IB_REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private String[] IB_REQUIRED_PERMISSIONS_ANDROID13 = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
    };


    private Spinner m_cboUsbDevices;
    private TextView tvSerialNumber;
    private TextView tvNoDevice;
    private View layoutSerialNumber;
    private Button btnCopy;

    /*
     * A handle to the single instance of the IBScan class that will be the primary interface to
     * the library, for operations like getting the number of scanners (getDeviceCount()) and
     * opening scanners (openDeviceAsync()).
     */
    private IBScan m_ibScan;

    /*
     * A handle to the open IBScanDevice (if any) that will be the interface for getting data from
     * the open scanner, including capturing the image (beginCaptureImage(), cancelCaptureImage()),
     * and the type of image being captured.
     */
    private IBScanDevice m_ibScanDevice;


    protected int m_nSelectedDevIndex = -1;                ///< Index of selected device
    protected boolean m_bInitializing = false;                ///< Device initialization is in progress
    protected String m_strImageMessage = "";

    protected int m_nCurrentCaptureStep = -1;                    ///< Current capture step

    protected ArrayList<String> m_arrUsbDevices;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnCheckPermission();

        m_ibScan = IBScan.getInstance(this.getApplicationContext());
        m_ibScan.setScanListener(this);

        Resources r = Resources.getSystem();
        Configuration config = r.getConfiguration();

        setContentView(R.layout.activity_device_info);


        /* Initialize UI fields. */
        _InitUIFields();

        /*
         * Make sure there are no USB devices attached that are IB scanners for which permission has
         * not been granted.  For any that are found, request permission; we should receive a
         * callback when permission is granted or denied and then when IBScan recognizes that new
         * devices are connected, which will result in another refresh.
         */
        final UsbManager manager = (UsbManager) this.getApplicationContext().getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        final Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            final UsbDevice device = deviceIterator.next();
            final boolean isScanDevice = IBScan.isScanDevice(device);
            if (isScanDevice) {
                final boolean hasPermission = manager.hasPermission(device);
                if (!hasPermission) {
                    this.m_ibScan.requestPermission(device.getDeviceId());
                }
            }
        }

        OnMsg_UpdateDeviceList(false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);



        /* Initialize UI fields for new orientation. */
        _InitUIFields();

        OnMsg_UpdateDeviceList(true);

        /* Populate UI with data from old orientation. */

    }

    /*
     * Release driver resources.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < 10; i++) {
            try {
                _ReleaseDevice();
                break;
            } catch (IBScanException ibse) {
                if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        exitApp(this);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return null;
    }

    /*
     * For Check write permission for Save Images or ISO files
     */
    public void OnCheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    ActivityCompat.requestPermissions(this, IB_REQUIRED_PERMISSIONS_ANDROID13, IB_PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, IB_REQUIRED_PERMISSIONS_ANDROID13, IB_PERMISSION_REQUEST_CODE);
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, IB_REQUIRED_PERMISSIONS, IB_PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, IB_REQUIRED_PERMISSIONS, IB_PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check Request code is correct and Request counts(Camera, Storage) are match
        switch (requestCode) {
            case IB_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Warning")
                            .setMessage("Please Allow ALL permission on app setting")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Permission Not granted, App won't run due to permission Problem, So Exit app force.
                                    // Below
                                    finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            });

                    AlertDialog ErrDlg = builder.create();
                    ErrDlg.show();

                }
        }
    }


    /* *********************************************************************************************
     * PRIVATE METHODS
     ******************************************************************************************** */

    /*
     * Initialize UI fields for new orientation.
     */
    private void _InitUIFields() {
        m_cboUsbDevices = (Spinner) findViewById(R.id.spinUsbDevices);
        tvSerialNumber = (TextView) findViewById(R.id.tv_serial_number);
        btnCopy = (Button) findViewById(R.id.btn_copy_serial_number);
        layoutSerialNumber = (View) findViewById(R.id.layout_serial_number);
        tvNoDevice = (TextView) findViewById(R.id.tv_no_device_msg);
		btnCopy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(tvSerialNumber.getText().toString().isEmpty()){
					Toast.makeText(DeviceInfoActivity.this,"No device connected",Toast.LENGTH_SHORT).show();
				}else{
					ClipboardUtil.copyToClipboard(DeviceInfoActivity.this,tvSerialNumber.getText().toString());
				}
			}
		});
    }


    // Get IBScan.
    protected IBScan getIBScan() {
        return (this.m_ibScan);
    }

    // Get opened or null IBScanDevice.
    protected IBScanDevice getIBScanDevice() {
        return (this.m_ibScanDevice);
    }

    // Set IBScanDevice.
    protected void setIBScanDevice(IBScanDevice ibScanDevice) {
        m_ibScanDevice = ibScanDevice;
        if (ibScanDevice != null) {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    /*
     * Set status message text box.
     */
    protected void _SetStatusBarMessage(final String s) {

    }

    protected void _SetImageMessage(String s) {
        m_strImageMessage = s;
    }

    protected void _ReleaseDevice() throws IBScanException {
        if (getIBScanDevice() != null) {
            if (getIBScanDevice().isOpened() == true) {
                getIBScanDevice().close();
                setIBScanDevice(null);
            }
        }

        m_nCurrentCaptureStep = -1;
        m_bInitializing = false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Event-dispatch threads
    private void OnMsg_SetStatusBarMessage(final String s) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                _SetStatusBarMessage(s);
            }
        });
    }
    private void OnMsg_cboUsbDevice_Changed() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m_nSelectedDevIndex == m_cboUsbDevices.getSelectedItemPosition())
                    return;

                m_nSelectedDevIndex = m_cboUsbDevices.getSelectedItemPosition();
                if (getIBScanDevice() != null) {
                    try {
                        _ReleaseDevice();
                    } catch (IBScanException ibse) {
                        ibse.printStackTrace();
                    }
                }

            }
        });
    }

    private void OnMsg_UpdateDeviceList(final boolean bConfigurationChanged) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    boolean idle = (!m_bInitializing && (m_nCurrentCaptureStep == -1)) ||
                            (bConfigurationChanged);
                    //store currently selected device
                    String strSelectedText = "";
                    int selectedDev = m_cboUsbDevices.getSelectedItemPosition();
                    if (selectedDev > -1)
                        strSelectedText = m_cboUsbDevices.getSelectedItem().toString();

                    m_arrUsbDevices = new ArrayList<String>();

                    m_arrUsbDevices.add("- Please select -");

					tvSerialNumber.setText("");

                    // populate combo box
                    int devices = getIBScan().getDeviceCount();

                    selectedDev = 0;
                    for (int i = 0; i < devices; i++) {
                        IBScan.DeviceDesc devDesc = getIBScan().getDeviceDescription(i);
                        String strDevice;
                        //strDevice = devDesc.productName + "_v"+ devDesc.fwVersion + " (" + devDesc.serialNumber + ")";
                        strDevice = devDesc.productName + " (" + devDesc.serialNumber + ")";

						tvSerialNumber.setText(devDesc.serialNumber);
                        m_arrUsbDevices.add(strDevice);
                        if (strDevice == strSelectedText)
                            selectedDev = i + 1;
                    }
                    if(devices > 0){
                        tvNoDevice.setVisibility(View.GONE);
                        layoutSerialNumber.setVisibility(View.VISIBLE);
                    }else{
                        tvNoDevice.setVisibility(View.VISIBLE);
                        layoutSerialNumber.setVisibility(View.GONE);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeviceInfoActivity.this,
                            R.layout.spinner_text_layout, m_arrUsbDevices);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    m_cboUsbDevices.setAdapter(adapter);

                    if ((selectedDev == 0 && (m_cboUsbDevices.getCount() == 2)))
                        selectedDev = 1;

                    m_cboUsbDevices.setSelection(selectedDev);

                    if (idle) {
                        OnMsg_cboUsbDevice_Changed();
                    }

                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void OnMsg_DeviceCommunicationBreak() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                _SetStatusBarMessage("Device communication was broken");

                try {
                    _ReleaseDevice();

                    OnMsg_UpdateDeviceList(false);
                } catch (IBScanException ibse) {
                    if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                        OnMsg_DeviceCommunicationBreak();
                    }
                }
            }
        });
    }

    private static void exitApp(Activity ac) {
        ac.moveTaskToBack(true);
        ac.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    public void scanDeviceAttached(final int deviceId) {

        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        }
    }

    @Override
    public void scanDeviceDetached(final int deviceId) {

    }

    @Override
    public void scanDevicePermissionGranted(final int deviceId, final boolean granted) {

    }

    @Override
    public void scanDeviceCountChanged(final int deviceCount) {
        OnMsg_UpdateDeviceList(false);
    }

    @Override
    public void scanDeviceInitProgress(final int deviceIndex, final int progressValue) {
        OnMsg_SetStatusBarMessage("Initializing device..." + progressValue + "%");
    }

    @Override
    public void scanDeviceOpenComplete(final int deviceIndex, final IBScanDevice device,
                                       final IBScanException exception) {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanDeviceListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void deviceCommunicationBroken(final IBScanDevice device) {
        OnMsg_DeviceCommunicationBreak();
    }

    @Override
    public void deviceImagePreviewAvailable(final IBScanDevice device, final ImageData image) {

    }

    @Override
    public void deviceFingerCountChanged(final IBScanDevice device, final FingerCountState fingerState) {

    }

    @Override
    public void deviceFingerQualityChanged(final IBScanDevice device, final FingerQualityState[] fingerQualities) {

    }

    @Override
    public void deviceAcquisitionBegun(final IBScanDevice device, final ImageType imageType) {
        if (imageType.equals(ImageType.ROLL_SINGLE_FINGER)) {
            m_strImageMessage = "When done remove finger from sensor";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        }
    }

    @Override
    public void deviceAcquisitionCompleted(final IBScanDevice device, final ImageType imageType) {
        if (imageType.equals(ImageType.ROLL_SINGLE_FINGER)) {
        } else {
            _SetImageMessage("Remove fingers from sensor");
            _SetStatusBarMessage("Acquisition completed, postprocessing..");
        }
    }

    @Override
    public void deviceImageResultAvailable(final IBScanDevice device, final ImageData image,
                                           final ImageType imageType, final ImageData[] splitImageArray) {
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice device, IBScanException imageStatus,
                                                   final ImageData image, final ImageType imageType, final int detectedFingerCount,
                                                   final ImageData[] segmentImageArray, final SegmentPosition[] segmentPositionArray) {

    }

    @Override
    public void devicePlatenStateChanged(final IBScanDevice device, final PlatenState platenState) {

    }

    @Override
    public void deviceWarningReceived(final IBScanDevice device, final IBScanException warning) {
        _SetStatusBarMessage("Warning received " + warning.getType().toString());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice device, int pressedKeyButtons) {
    }
}
