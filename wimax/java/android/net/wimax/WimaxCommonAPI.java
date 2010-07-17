/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.net.wimax;

import android.net.wimax.structs.*;

/**
 * @hide
 */
public class WimaxCommonAPI {

	public native static boolean loadLibrary();
	public native static boolean unloadLibrary();

    public interface DeviceStatusChangeCB {	void callback(DeviceId deviceId, int deviceStatus, int statusReason, int connectionProgressInfo); }
    public interface DeviceInsertRemoveCB {	void callback(DeviceId deviceId, boolean cardPresent); }
    public interface ControlPowerManagementCB {	void callback(DeviceId deviceId, int status); }
    public interface ConnectToNetworkCB { void callback(DeviceId deviceId, int status); }
    public interface DisconnectToNetworkCB { void callback(DeviceId deviceId, int status); }
    public interface NetworkSearchWideScanCB { void callback(DeviceId deviceId, NSPInfo[] nspInfo, int listSize);	}
    public interface ProvisioningOperationCB { void callback(DeviceId deviceId, int provisoningOperation, int contactType);	}
    public interface PackageUpdateCB { void callback(DeviceId deviceId, int packageUpdate); }

	public native static int WiMaxAPIOpen(DeviceId deviceId /*[In, Out]*/);
	public native static int GetListDevice(DeviceId deviceId /*[In]*/, HardwareDeviceId[] pHwDeviceIdList /*[In, Out]*/, int[] pHwDeviceIdListSize /*[In, Out]*/);
	public native static int WiMaxDeviceOpen(DeviceId deviceId /*[In]*/);
	public native static int WiMaxDeviceClose(DeviceId deviceId /*[In]*/);
	public native static int WiMaxAPIClose(DeviceId deviceId /*[In]*/);
	public native static int CmdControlPowerManagement(DeviceId deviceId /*[In]*/, int powerState /*[In]*/);
	public native static int CmdResetWimaxDevice(DeviceId deviceId /*[In]*/);
	public native static int CmdResetToFactorySettings(DeviceId deviceId /*[In]*/);
	public native static int GetErrorString(DeviceId deviceId /*[In]*/, int errorCode /*[In]*/, String[] buffer /*[Out]*/, int[] pLength /*[In, Out]*/);
	public native static int SetServiceProviderUnLock(DeviceId deviceId /*[In]*/, String lockCode /*[In]*/);
	public native static int GetServiceProviderLockStatus(DeviceId deviceId /*[In]*/, int[] pLockStatus /*[Out]*/, String[] NSPName /*[Out]*/);
	public native static int GetNetworkList(DeviceId deviceId /*[In]*/, NSPInfo[] pNSPInfo /*[Out]*/, int[] pArrayLength /*[In, Out]*/);
	public native static int CmdConnectToNetwork(DeviceId deviceId /*[In]*/, String nspName /*[In]*/, int profileId /*[In]*/, String password /*[In]*/);
	public native static int CmdDisconnectFromNetwork(DeviceId deviceId /*[In]*/);
	public native static int CmdNetworkSearchWideScan(DeviceId deviceId /*[In]*/);
	public native static int GetIPInterfaceIndex(DeviceId deviceId /*[In]*/, InterfaceInfo pInterfaceInfo /*[Out]*/);
	public native static int GetSelectProfileList (DeviceId deviceId /*[In]*/, ProfileInfo[] pProfileList /*[In]*/, int[] pListSize /*[In, Out]*/);
	public native static int GetLinkStatus (DeviceId deviceId /*[In]*/, LinkStatusInfo pLinkStatus /*[Out]*/);
	public native static int GetDeviceInformation (DeviceId deviceId /*[In]*/, DeviceInfo pDeviceInfo /*[Out]*/);
	public native static int GetDeviceStatus (DeviceId deviceId /*[In]*/, int[] pDeviceStatus /*[Out]*/, int[] pConnectionProgressInfo /*[Out]*/);
	public native static int GetConnectedNSP (DeviceId deviceId /*[In]*/, ConnectedNspInfo pConnectedNSP /*[Out]*/);

	public native static int SetRoamingMode (DeviceId deviceId /*[In]*/, boolean roamingMode /*[In]*/);
	public native static int GetRoamingMode (DeviceId deviceId /*[In]*/, boolean[] pRoamingMode /*[Out]*/);
	public native static int GetStatistics (DeviceId deviceId /*[In]*/, ConnectionStatistics pStatistics /*[Out]*/);
	public native static int GetProvisioningStatus(DeviceId deviceId /*[In]*/, String nspName /*[In]*/, boolean[] pProvisoningStatus /*[Out]*/);
	public native static int GetContactInformation(DeviceId deviceId /*[In]*/, String nspName /*[In]*/, ContactInfo[] pContactInfo /*[Out]*/, int[] pSizeOfContactList /*[In, Out]*/);
	public native static int GetPackageInformation(DeviceId deviceId /*[In]*/, PackageInfo pPackageInfo /*[Out]*/);
	public native static int SetPackageUpdateState(DeviceId deviceId /*[In]*/, int packageUpdateState /*[In]*/);

	public native static int SubscribeDeviceStatusChange (DeviceId deviceId /*[In]*/, DeviceStatusChangeCB pCallbackFnc /*[In]*/);
	public native static int SubscribeDeviceInsertRemove (DeviceId deviceId /*[In]*/, DeviceInsertRemoveCB pCallbackFnc /*[In]*/);
	public native static int SubscribeControlPowerManagement (DeviceId deviceId /*[In]*/, ControlPowerManagementCB pCallbackFnc /*[In]*/);
	public native static int SubscribeConnectToNetwork (DeviceId deviceId /*[In]*/, ConnectToNetworkCB pCallbackFnc /*[In]*/);
	public native static int SubscribeDisconnectToNetwork (DeviceId deviceId /*[In]*/, DisconnectToNetworkCB pCallbackFnc /*[In]*/);
	public native static int SubscribeNetworkSearchWideScan (DeviceId deviceId /*[In]*/, NetworkSearchWideScanCB pCallbackFnc /*[In]*/);
	public native static int SubscribeProvisioningOperation (DeviceId deviceId /*[In]*/, ProvisioningOperationCB pCallbackFnc /*[In]*/);
	public native static int SubscribePackageUpdate (DeviceId deviceId /*[In]*/, PackageUpdateCB pCallbackFnc /*[In]*/);

	public native static int UnsubscribeDeviceStatusChange (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribeDeviceInsertRemove (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribeControlPowerManagement (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribeConnectToNetwork (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribeDisconnectToNetwork (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribeNetworkSearchWideScan (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribeProvisioningOperation (DeviceId deviceId /*[In]*/);
	public native static int UnsubscribePackageUpdate (DeviceId deviceId /*[In]*/);

}
