/**
****************************************************************************************************
* @file
*       IBScan.java
*
* @brief
*       Android Java wrapper for IBScanUltimate library
*
* @author
*       Integrated Biometrics, LLC
*
* @copyright
*       Copyright (c) Integrated Biometrics, 2009-2023 \n
*       http://www.integratedbiometrics.com
*
* @page page_API_Revision_History_IBScan IBScan.java
* @section section_IBScan IBScan.java
* @li @par  2020/09/21
* 						Added method setCustomerKey(), getErrorString()
*
* @li @par  2018/11/19
*						Added method getRequiredSDKVersion()
*
* @li @par  2017/11/08
*						Added method isWritableDirectory() to check whether a directory is writable.
*
* @li @par  2015/04/09
*						Added method updateUsbPermission() to update USB permission on rooted device.\n
*                 		Added method unloadLibrary() to release the library manaually.
*
* @li @par  2013/04/06
*						Added method to enable or disable trace log.\n
*                 		Modified various variable declarations with "final".\n
*						Added method setContext() to set the context.
*
* @li @par  2013/03/01
*						First version.
****************************************************************************************************
*/

package com.integratedbiometrics.ibscanultimate;

import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import org.libusb.LibUsbManager;
 
/**
****************************************************************************************************
* @defgroup group_API_Main_Class_IBScan Class - IBScan
* @{
****************************************************************************************************
*/
/**
* @brief    Principal class for managing IB scanners.
* 			<p>
* 			The single instance of this class may be gotten with getInstance().  The application \n
* 			will typically register a IBScanListener to receive notifications for events such as \n
* 			device count change and device communication failure.  Device instances should be obtained by \n
* 			either the blocking openDevice() method or non-blocking openDeviceAsync()\n
* 			method. The activity accessing IB scanners must set the context for operations with setContext().
*/
public class IBScan
{
    /**
    ********************************************************************************************
    * PUBLIC INNER CLASSES
    ********************************************************************************************
    */

    /**
	****************************************************************************************************
	* @defgroup group_API_Nested_Class_SdkVersion Class - IBScan.SdkVersion
	* @brief    Container to hold version information.
	* @{
	****************************************************************************************************
	*/
    public static final class SdkVersion
    {
        /**
        * @brief    Product version string.
        */
        public final String product;

        /**
        * @brief    File version string.
        */
        public final String file;

        /**
        * @brief    Instantiate version & initialize version information.
        */
        protected SdkVersion(final String product, final String file)
        {
            this.product = product;
            this.file = file;
        }

        @Override
        public String toString()
        {
            final String s = "Product: " + this.product + "\n" + 
                             "File: "    + this.file    + "\n";
            return (s);
        }
    }
    /**
	****************************************************************************************************
	* @}
	****************************************************************************************************
	*/

    /**
	****************************************************************************************************
	* @defgroup group_API_Nested_Class_DeviceDesc Class - IBScan.DeviceDesc
	* @brief    Basic device description structure.
	* @{
	****************************************************************************************************
	*/
    public static final class DeviceDesc
    {
        /** @brief    Device serial number. */
        public final String serialNumber;

        /** @brief    Device product name. */
        public final String productName;

        /** @brief    Device interface type (USB, Firewire). */
        public final String interfaceType;

        /** @brief    Device firmware version. */
        public final String fwVersion;

        /** @brief    Device revision. */
        public final String devRevision;

        /** @brief    Indicates whether device is opened. */
        public final boolean isOpened;

        /** @brief    Indicates whether device is locked. */
        public final boolean isLocked;

        /** @brief    Customer string to display. */
        public final String customerString;
        
        /**
         * ID of the device.  This is the ID that Android assigns to the device, obtained 
         * through the <code>UsbDevice</code> <code>getDeviceId()</code> method.
         */
        /**
        * @brief	ID of the device.  This is the ID that Android assigns to the device, obtained\n
        *			through the UsbDevice getDeviceId() method.
        */
        public final int deviceId;
        
        /** @brief    Instantiate device description & initialize device description information. */
        protected DeviceDesc(final String serialNumber, final String productName,
            final String interfaceType, final String fwVersion,
            final String devRevision, final boolean isOpened,
            final int deviceId, final boolean isLocked, final String customerString)
        {
            this.serialNumber  = serialNumber;
            this.productName   = productName;
            this.interfaceType = interfaceType;
            this.fwVersion     = fwVersion;
            this.devRevision   = devRevision;
            this.isOpened      = isOpened;
            this.deviceId      = deviceId;
            this.isLocked      = isLocked;
            this.customerString= customerString;
        }

        @Override
        public String toString()
        {
            final String s = "Serial Number: "    + this.serialNumber  + "\n" + 
                             "Product Name: "     + this.productName   + "\n" + 
            		         "Interface Type: "   + this.interfaceType + "\n" + 
                             "Firmware Version: " + this.fwVersion     + "\n" + 
            		         "Device Revision: "  + this.devRevision   + "\n" +
                             "Device Opened: "    + this.isOpened      + "\n" +
                             "Device Locked: "    + this.isLocked      + "\n" +
                             "Customer String: "  + this.customerString + "\n"+
            		         "Device ID: "        + this.deviceId      + "\n";
            return (s);
        }
    }
    /**
	****************************************************************************************************
	* @}
	****************************************************************************************************
	*/

    /* *********************************************************************************************
     * (OBJECT) PUBLIC INTERFACE
     ******************************************************************************************** */

    /**
    * @brief    Enables or disable trace log in native library.  The trace log is enabled by default.
    *
    * @param 	on  true to enable trace log; false to 
    *           	disable trace log
    * @throws	IBScanException
    */
    public void enableTraceLog(final boolean on) throws IBScanException
    {
        final NativeError error = new NativeError();
        enableTraceLogNative(on, error);
        handleError(error); /* throws exceptin if necessary */
    }

    /**
    * @brief    - Android Only\n
    *			Update the permission to allow the approach to attached USB bus by libusb library.\n
    * 			It is required rooting
    * 
    * @throws 	IBScanException
    */
    public void updateUsbPermission()
    {
        updateUsbPermissionNative();
    }

    /**
    * @brief    Obtains product and software version information.
    * 
    * @return 	[SdkVersion] SDK Version
    * @throws 	IBScanException
    */
    public SdkVersion getSdkVersion() throws IBScanException
    {
        final NativeError error   = new NativeError();
        final SdkVersion  version = getSdkVersionNative(error);
        handleError(error); /* throws exception if necessary */

        return (version);
    }
    
    /**
    * @brief	Retrieve count of connected IB USB scanner devices. Only the attached devices to which the\n
    *			caller has been granted permission will be counted.
    * 
    * @return	[int] count of IB USB scanner devices
    * @throws	IBScanException
    */
    public int getDeviceCount() throws IBScanException
    {
        final NativeError error = new NativeError();
        final int count = getDeviceCountNative(error);
        handleError(error); /* throws exception if necessary */

        return (count);
    }

    /**
    * @brief	- Android Only\n
    *			Request permission to access the device.  Success or failure will be returned to the user \n
    * 			through the registered IBScanListener's scanDevicePermissionGranted()\n
    * 			callback.  If permission has not already been granted to the device, a dialog may be shown \n
    * 			to the user.
    * 
    * @param 	deviceId	ID of the device.  This is the ID that Android assigns to the device,
     *                  	obtained through the UsbDevice getDeviceId() method.
    * @throws	IBScanException
    */
    public void requestPermission(final int deviceId)
    {
    	/* Request permission with the USB manager. */
    	final UsbDevice device = findDevice(deviceId);
    	if (device != null)
    	{  	
	    	/* Create intent and request permission with the USB manager. */
	    	final UsbManager    manager          = (UsbManager)this.m_context.getSystemService(Context.USB_SERVICE);
	    	final Intent        intent           = new Intent(ACTION_USB_PERMISSION);
            final PendingIntent permissionIntent;
			
            if (Build.VERSION.SDK_INT >= 23) {
                // Create a PendingIntent using FLAG_IMMUTABLE.
                permissionIntent = PendingIntent.getBroadcast(this.m_context, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                // Existing code that creates a PendingIntent.
                permissionIntent = PendingIntent.getBroadcast(this.m_context, 0, intent, 0);
            }

	    	manager.requestPermission(device, permissionIntent);
    	}
    }
    
    /**
    * @brief	- Android Only\n
    *			Determines whether the caller has permission to access the device.
    * 
    * @param 	deviceId	ID of the device.  This is the ID that Android assigns to the device,
    *                  		obtained through the UsbDevice getDeviceId() method.
    * @return	[boolean] true if caller has permission; false otherwise.
    * @throws	IBScanException
    */
    public boolean hasPermission(final int deviceId)
    {
    	boolean hasPermission = false;
    	
    	final UsbDevice device = findDevice(deviceId);
    	if (device != null)
    	{  	
	    	final UsbManager manager = (UsbManager)this.m_context.getSystemService(Context.USB_SERVICE);	    	
	    	hasPermission = manager.hasPermission(device);
    	}
    	
    	return (hasPermission);
    }
        
    /**
     * Retrieve detailed device information about particular scanner by logical index.
     * 
     * @param deviceIndex  zero-based index of the device
     * @return             a description of the device
     * @throws             IBScanException
     */
    /**
    * @breif	Retrieve detailed device information about particular scanner by logical index.
    * 
    * @param 	deviceIndex  zero-based index of the device
    * @return   [DeviceDesc] a description of the device
    * @throws	IBScanException
    */
    public DeviceDesc getDeviceDescription(final int deviceIndex)
        throws IBScanException
    {
        final NativeError error = new NativeError();
        final DeviceDesc  desc  = getDeviceDescNative(deviceIndex, error);
        handleError(error); /* throws exception if necessary */

        return (desc);
    }

    /**
    * @breif	Initialize device, given a particular by device index.  This function blocks until an error
    * 			occurs or initialization completes; meanwhile any registered IBScanListener will 
    * 			receive scanDeviceInitProgress() callbacks to track the initialization progress.
    * 			Either a device object will be returned to the application or an exception will be thrown.
    * 
    * @param	deviceIndex  zero-based index of the device
    * @return	[IBScanDevice] device object, if initialization succeeds; null otherwise
    * @throws	IBScanException
    */
    public IBScanDevice openDevice(final int deviceIndex) throws IBScanException
    {
    	final NativeError  error      = new NativeError();
        final IBScanDevice scanDevice = openDeviceNative(deviceIndex, error);
        handleError(error); /* throws exception if necessary */

        return (scanDevice);
    }

    /**
    * @brief	See also openDevice(int).
    * 
    * @param	deviceIndex        zero-based index of the device
    * @param	uniformityMaskPath uniformity mask path
    * @return	[IBScanDevice] device object, if initialization succeeds; null 
    *           otherwise
    * @throws	IBScanException
    */
    public IBScanDevice openDevice(final int deviceIndex,
        final String uniformityMaskPath) throws IBScanException
    {
        if (uniformityMaskPath == null)
        {
        	logPrintWarning(getMethodName() + ": receive null uniformityMaskPath");
        	throw (new IllegalArgumentException());
        }
        
    	final NativeError  error  = new NativeError();
        final IBScanDevice device = openDeviceExNative(deviceIndex, uniformityMaskPath, error);
        handleError(error); /* throws exception if necessary */

        return (device);
    }

    /**
    * @brief	Initialize device asynchronously, given a particular by device index.  This function returns
    * 			immediately.  Any registered IBScanListener will receive scanDeviceInitProgress() 
    * 			callbacks to track the initialization progress.  When an error occurs or initialization 
    * 			completes, scanDeviceOpenComplete() will be invoked with either a device object 
    * 			or a description of the error that occurred.
    * 
    * @param	deviceIndex zero-based index of the device
    * @throws	IBScanException
    */
    public void openDeviceAsync(final int deviceIndex) throws IBScanException
    {
    	final NativeError error = new NativeError();
        openDeviceAsyncNative(deviceIndex, error);
        handleError(error); /* throws exception if necessary */
    }

    /**
    * @brief	See also openDeviceAsync(int)
    * 
    * @param	deviceIndex        zero-based index of the device
    * @param	uniformityMaskPath uniformity mask path
    * @throws	IBScanException
    */
    public void openDeviceAsync(final int deviceIndex,
        final String uniformityMaskPath) throws IBScanException
    {
        if (uniformityMaskPath == null)
        {
        	logPrintWarning(getMethodName() + ": receive null uniformityMaskPath");
        	throw (new IllegalArgumentException());
        }
        
    	final NativeError error = new NativeError();
        openDeviceAsyncExNative(deviceIndex, uniformityMaskPath, error);
        handleError(error); /* throws exception if necessary */
    }

    /**
    * @brief	Get initialization progress.
    * 
    * @param	deviceIndex  zero-based index of the device
    * @return	[int] initialization progress between 0 and 100. A value of 100 indicates that
    *           	that initialization is complete.
    * @throws	IBScanException
    */
    public int getInitProgress(final int deviceIndex) throws IBScanException
    {
    	final NativeError error    = new NativeError();
        final int         progress = getInitProgressNative(deviceIndex, error);
        handleError(error); /* throws exception if necessary */

        return (progress);
    }

    /**
    * @brief	The library is unmapped from the address space manually, and the library is no longer valid
    * 			So APIs will not be worked correctly after calling 
    * 			Some platform SDKs (Windows Mobile, Android)
    * 			can be needed to call IBSU_UnloadLibrary() before shutting down the application.
    * 
    * @throws	IBScanException
    */
    public void unloadLibrary() throws IBScanException
    {
    	final NativeError error    = new NativeError();
        unloadLibraryNative(error);
        handleError(error); /* throws exception if necessary */
    }

    /**
    * @brief	Get minimum SDK version required for running.
    *
    * @param	deviceIndex  zero-based index of the device
    * @return	[String] Minimum SDK Version to be returned
    * @throws	IBScanException
    */
    public String getRequiredSDKVersion(final int deviceIndex) throws IBScanException
    {
        final NativeError error = new NativeError();
        final String minSDKVersion = getRequiredSDKVersionNative(deviceIndex, error);
        handleError(error); /* throws exceptin if necessary */
        
        return (minSDKVersion);
    }
    
    /**
    * @brief Enumeration of Hash Type
    */
    public static enum HashType
    {
    	/** Hash type : SHA256 */
        SHA256(0),

        /** Hash type : Reserved */
        Reserved(1);

        /** Native value for enumeration. */
        private final int code;

    	HashType(int code)
        {
            this.code = code;
        }

        /** Find Java object from native value. */
        protected static HashType fromCode(int code)
        {
            for (HashType t : HashType.values())
            {
                if (t.code == code)
                {
                    return (t);
                }
            }
            return (null);
        }

        /** Get native value for Java object. */
        public int toCode()
        {
            return (this.code);
        }
    }

    /**
    * @brief	Set customerkey for running of the locked device.
    * 			This is must performed on the locked device before open the device
    *
    * @param  	deviceIndex  zero-based index of the device
    * @param  	hashType     type of Hash
    * @param  	customerKey  customer key to match lock info written in the locked device
    * @throws   IBScanException
    */
    public void setCustomerKey(final int deviceIndex,
        final HashType hashType,
        final String customerKey) throws IBScanException
    {
        final NativeError error = new NativeError();
        setCustomerKeyNative(deviceIndex, hashType.toCode(), customerKey, error);
        handleError(error); /* throws exceptin if necessary */
    }
	
    /**
    * @brief	Returns a string description of the error code.
    *
    * @param	errorCode    error code
    * @throws	IBScanException
    */
    public String getErrorString(final int errorCode) throws IBScanException
    {
        final NativeError error = new NativeError();
        final String ErrorString = getErrorStringNative(errorCode, error);
        handleError(error); /* throws exceptin if necessary */
        return (ErrorString);
    }

    /**
    * @brief	Set listener for scan events.
    * 
    * @param	listener  listener for scan events
    */
    public void setScanListener(final IBScanListener listener)
    {
        this.m_listener = listener;
    }
    
    /**
    * @brief	- Android Only\n
    *			Determine whether device is a scan device.  This just checks whether the vendor and product\n
    * 			IDs match recognized devices.
    *
    * @param	device	device to investigate
    * @return	[boolean] true if device is an IB scan device; false otherwise
    */
    public static boolean isScanDevice(final UsbDevice device)
    {
	    boolean isScanDevice = false;
	    int     vendorId     = device.getVendorId();
	    if (vendorId == VID_IB || vendorId == VID_DERMALOG)
	    {
	        int[] productIds = { PID_CURVE, PID_WATSON, PID_WATSON_REV1, PID_SHERLOCK, PID_SHERLOCK_AUO, 
	        		PID_WATSON_MINI, PID_WATSON_MINI_REV1, PID_COLUMBO, PID_COLUMBO_REV1, PID_KOJAK, PID_KOJAK_REV1, 
	        		PID_HOLMES, PID_FIVE0, PID_FIVE0_REV1, PID_FIVE0_DERMALOG, PID_DANNO, PID_KOJAK_LOCK, 
					PID_KOJAK_DERMALOG, PID_COLUMBO_MINI, PID_MANNIX};
	    	int   productId  = device.getProductId();
	    	for (int productIdTemp : productIds)
	    	{
	    		if (productIdTemp == productId)
	    		{
	    			isScanDevice = true;
	    			break;
	    		}
	    	}
	    }
    	
	    return (isScanDevice);
    }
    
    /**
    * @brief	- Android Only\n
    *			Set the context for this IBScan.  This fuction must be called by an activity for scanners\n
    *			to be recognized and accessible.  When one activity transfers control of the IB scanners\n
    *			to another, this function should be called with a null argument to release\n
    *			the reference to the context and unregister it as a USB receiver.
    *
    * @param	context	the context for the reciever and USB accesses.  If null, \n
    *                 	the existing context will still be unregistered as a receiver and the \n
    *                 	reference to it will be cleared.
    */
    public void setContext(final Context context)
    {
        /* Clear current context. */
        if (this.m_context != null)
        {
            this.m_context.unregisterReceiver(this.m_usbReceiver);
            this.m_context = null;
            
            LibUsbManager.setContext(null);
        }
        
        /* Set new context. */
        if (context != null)
        {
            this.m_context = context;
            
            LibUsbManager.setContext(this.m_context);
          
            /* Register broadcast receiver to receive USB events. */
            final IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_PERMISSION);
            this.m_context.registerReceiver(this.m_usbReceiver, filter);
        }
    }

    /* *********************************************************************************************
     * (CLASS) PUBLIC INTERFACE
     ******************************************************************************************** */

    /**
    * @brief	Get single instance of class.
    *
    * @param	- Android Only\n
    *			context	the context for the reciever and USB accesses.
    * @return	[IBScan] single instance of IBScan.
    */
    public static IBScan getInstance(final Context context)
    {
        if (IBScan.m_instance == null)
        {
            IBScan.m_instance = new IBScan();
        }
        
        IBScan.m_instance.setContext(context);

        return (IBScan.m_instance);
    }

    /* *********************************************************************************************
     * PROTECTED INNER CLASSES
     ******************************************************************************************** */

    /** @brief	Container for native error value. */
    protected static final class NativeError
    {
        public int code = 0;
    }

    /* *********************************************************************************************
     * PRIVATE INTERFACE
     ******************************************************************************************** */

    /** @brief	Protected default constructor to prevent external instantiation. */
    private IBScan()
    {
        initNative();
    }
       
    /** @brief	Intent action when USB device access permission is granted or denied. */
    private static final String ACTION_USB_PERMISSION = "ibscan.USB_PERMISSION";

    /** @brief	Vendor ID and product IDs for IB scanners. */
    private static final int VID_IB                 = 0x113F;
	private static final int VID_DERMALOG           = 0x1FBA; // Added Dermalog Five-O / For TF10 model
	
    private static final int PID_CURVE              = 0x1004;
    private static final int PID_WATSON             = 0x1005;
    private static final int PID_WATSON_REV1        = 0x1006;
    private static final int PID_SHERLOCK           = 0x1010;
    private static final int PID_SHERLOCK_AUO       = 0x1011;
    private static final int PID_WATSON_MINI        = 0x1020;
    private static final int PID_WATSON_MINI_REV1   = 0x1021;
    private static final int PID_COLUMBO            = 0x1100;
    private static final int PID_COLUMBO_REV1       = 0x1101;
    private static final int PID_HOLMES             = 0x1200;
    private static final int PID_KOJAK              = 0x1300;
    private static final int PID_KOJAK_REV1         = 0x1301;
	private static final int PID_KOJAK_DERMALOG     = 0x0036; // For Dermalog Kojak support
    private static final int PID_FIVE0              = 0x1500;
    private static final int PID_FIVE0_REV1         = 0x1501;
	private static final int PID_FIVE0_DERMALOG     = 0x0034; // For Dermalog Five-0 support
	private static final int PID_DANNO			    = 0x1600;
    private static final int PID_KOJAK_LOCK         = 0x1A00;
    private static final int PID_COLUMBO_MINI       = 0x7100;
    private static final int PID_MANNIX   	        = 0x1D00;

    /**
    * @brief	Find device for a certain device ID.
    *
    * @param	deviceId	device ID to search for
    * @return	[UsbDevice]	device, if found; null otherwise
    */
    private UsbDevice findDevice(final int deviceId)
    {
    	UsbDevice device = null;
    	
        if (this.m_context != null)
        {
            final UsbManager                 manager    = (UsbManager)this.m_context.getSystemService(Context.USB_SERVICE);
            final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

            /* Iterate through list to find device. */
            final Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext())
            {
                final UsbDevice deviceTemp   = deviceIterator.next();
                final int       deviceIdTemp = deviceTemp.getDeviceId();
		    
                if (deviceIdTemp == deviceId)
                {
                    device = deviceTemp;
                    break;
                }
            }
        }

    	return (device);
    }
    
    /**
    * @brief	Handle	error from native method.  If an error is returned, the appropriate exception will be thrown.
    *
    * @param	error	error set in native code
    * @return	[UsbDevice]	device, if found; null otherwise
    * @throws	IBScanException
    */
    private static void handleError(final NativeError error) throws IBScanException
    {
        if (error.code != 0)
        {
            IBScanException.Type type;

            type = IBScanException.Type.fromCode(error.code);
            if (type == null)
            {
            	logPrintError(getMethodName() + ": unrecognized error code(" + error.code + ") returned from native code");
            	type = IBScanException.Type.COMMAND_FAILED;
            }
            throw (new IBScanException(type));
        }
    }

    /**
    * @brief	Log warning to System.out.
    *
    * @param	ln	log string for warning
    */
    private static void logPrintWarning(final String ln)
    {
    	Log.w("IBScan", ln);
    }
    
    /**
    * @brief	Log Error to System.out.
    *
    * @param	ln	log string for error
    */
    private static void logPrintError(final String ln)
    {
    	Log.e("IBScan", ln);
    }
    
    /** @brief The stack index at which a caller method's name will be found. */
    private static int METHOD_STACK_INDEX;

	/**
    * @brief	Get name of method caller.
    *
    * @return	[String]	method name
    */
    private static String getMethodName() 
    {
    	StackTraceElement[] stackTrace;
    	String              name;
    	
    	stackTrace = Thread.currentThread().getStackTrace();
    	/* Sanity check the index, though it should always be within bounds. */
    	if (stackTrace.length > METHOD_STACK_INDEX)
    	{
    		name = stackTrace[METHOD_STACK_INDEX].getMethodName();
    	}
    	else
    	{
    		name = "?";
    	}
        return (name);
    }
        
    /** @brief	Scan listener. */
    private IBScanListener m_listener = null;

    /** @brief	Scan context. */
    private Context m_context = null;
    
    /** @brief	Singleton scan object. */
    private static IBScan m_instance = null;
    
    /** @brief	Broadcast receiver to listen for USB device events. */
    private final BroadcastReceiver m_usbReceiver = new BroadcastReceiver()
    {
    	@Override
    	public void onReceive(final Context context, final Intent intent)
    	{
    		final String action = intent.getAction();
    		
    		/* Receive event about device attachment. */
    		if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
    		{
    			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);    		
    			if (device != null)
    			{
    				final boolean isScanDevice = isScanDevice(device);
    				if (isScanDevice)
    				{
    					callbackScanDeviceAttached(device.getDeviceId());
    				}
    			}
    		}
    		
    		/* Receive event about device detachment. */
    		else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
    		{
    			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
       			if (device != null)
    			{
       				final boolean isScanDevice = isScanDevice(device);
    				if (isScanDevice)
    				{
    					callbackScanDeviceDetached(device.getDeviceId());
    				}
    			}
    		}
    		
    		/* Receive event about access permission granted or denied. */
    		else if (action.equals(ACTION_USB_PERMISSION))
    		{    			
    			/* Sanity check.  This should always be true. */
    			final boolean exists = intent.hasExtra(UsbManager.EXTRA_PERMISSION_GRANTED);
    			if (exists)
    			{
    				/* Alert user about status. */
					final boolean   granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
					final UsbDevice device  = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (device != null)
					{
	    				boolean isScanDevice = isScanDevice(device);
	    				if (isScanDevice)
	    				{
	    					callbackScanDevicePermissionGranted(device.getDeviceId(), granted);
	    				}
					}
    			}
    		}
    	}    	
    };

    /* *********************************************************************************************
     * INTERFACE METHODS: IBScanListener INTERMEDIATES
     ******************************************************************************************** */

    /** @brief	Callback for scan device attach. */
    private static void callbackScanDeviceAttached(final int deviceId)
    {
    	final IBScan ibScan = IBScan.m_instance;

		if (ibScan != null)
		{
	        if (ibScan.m_listener != null)
	        {
	            ibScan.m_listener.scanDeviceAttached(deviceId);
	        }
		}
    }

    /** @brief	Callback for scan device detach. */
    private static void callbackScanDeviceDetached(final int deviceId)
    {
    	final IBScan ibScan = IBScan.m_instance;

		if (ibScan != null)
		{
	        if (ibScan.m_listener != null)
	        {
	            ibScan.m_listener.scanDeviceDetached(deviceId);
	        }
		}
    }
    
    /** @brief	Callback for device access granted or denied. */
    private static void callbackScanDevicePermissionGranted(final int deviceIndex, final boolean granted)
    {
    	final IBScan ibScan = IBScan.m_instance;

 		if (ibScan != null)
		{
	        if (ibScan.m_listener != null)
	        {
	            ibScan.m_listener.scanDevicePermissionGranted(deviceIndex, granted);
	        }
		}
    }
    
    /** @brief	Callback for device initialization progress.  Called from native code. */
    private static void callbackScanDeviceInitProgress(final int deviceIndex, final int progressValue)
    {
    	final IBScan ibScan = IBScan.m_instance;

 		if (ibScan != null)
		{
	        if (ibScan.m_listener != null)
	        {
	            ibScan.m_listener.scanDeviceInitProgress(deviceIndex, progressValue);
	        }
		}
    }

    /** @brief	Callback for scan device count change.  Called from native code. */
    private static void callbackScanDeviceCountChanged(final int deviceCount)
    {
        final IBScan ibScan = IBScan.m_instance;

		if (ibScan != null)
		{
	        if (ibScan.m_listener != null)
	        {
	            ibScan.m_listener.scanDeviceCountChanged(deviceCount);
	        }
		}
    }

    /** @brief	Callback for device open completion.  Called from native code. */
    private static void callbackScanDeviceOpenComplete(final int deviceIndex, final IBScanDevice device,
    		final int exceptionCode)
    {
    	final IBScan ibScan = IBScan.m_instance;

 		if (ibScan != null)
		{
	        if (ibScan.m_listener != null)
	        {
	            IBScanException.Type type = IBScanException.Type.fromCode(exceptionCode);
	            ibScan.m_listener.scanDeviceOpenComplete(deviceIndex, device, new IBScanException(type));
	        }
		}
    }
    /* *********************************************************************************************
     * NATIVE METHODS
     ******************************************************************************************** */

    /** @brief	Native method for constructor. */
    private native void initNative();

    /** @brief	Native method for enableTraceLog(). */
    private native void enableTraceLogNative(boolean on, NativeError error);

    /** @brief	Native method for updateUsbPermissionNative(). */
    private native void updateUsbPermissionNative();

    /** @brief	Native method for getSDKVersion(). */
    private native SdkVersion getSdkVersionNative(NativeError error);

    /** @brief	Native method for getDeviceCount(). */
    private native int getDeviceCountNative(NativeError error);

    /** @brief	Native method for getDeviceDesc(). */
    private native DeviceDesc getDeviceDescNative(int deviceIndex, NativeError error);

    /** @brief	Native method for openDevice(int). */
    private native IBScanDevice openDeviceNative(int deviceIndex, NativeError error);

    /** @brief	Native method for openDevice(int, String). */
    private native IBScanDevice openDeviceExNative(int deviceIndex, String uniformityMaskPath,
    		NativeError error);

    /** @brief	Native method for openDeviceAsync(int). */
    private native void openDeviceAsyncNative(int deviceIndex, NativeError error);

    /** @brief	Native method for openDeviceAsync(int, String). */
    private native void openDeviceAsyncExNative(int deviceIndex, String uniformityMaskPath,
    		NativeError error);

    /** @brief	Native method for getInitProgress(). */
    private native int getInitProgressNative(int deviceIndex, NativeError error);

    /** @brief	Native method for unloadLibrary(). */
    private native int unloadLibraryNative(NativeError error);
    
    /** @brief	Native method for getRequiredSDKVersion(). */
    private native String getRequiredSDKVersionNative(int deviceIndex, NativeError error);
    
    /** @brief	Native method for setCustomerKey(). */
    private native void setCustomerKeyNative(int deviceIndex, int hashType, String customerKey, NativeError error);
	
	/** @brief	Native method for getErrorString(). */
    private native String getErrorStringNative(int errorCode, NativeError error);

    /* *********************************************************************************************
     * STATIC BLOCKS
     ******************************************************************************************** */
    
    /** @brief	Helper block to get method name for debug messages. */
    static 
    {
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) 
        {
            i++;
            if (ste.getClassName().equals(IBScan.class.getName())) 
            {
                break;
            }
        }
        METHOD_STACK_INDEX = i;
    }
    
    /** @brief	Load native library. */
    static
    {
        System.loadLibrary("usb");
        if (System.getProperty("PPI_BUILD") != null) {
            System.loadLibrary("DeviceParallel");
        }
        System.loadLibrary("IBScanUltimate");
        System.loadLibrary("ibscanultimatejni");
    }
}
/**
****************************************************************************************************
* @}
****************************************************************************************************
*/