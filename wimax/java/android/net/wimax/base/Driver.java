/**
 * Copyright (c) 2009, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.net.wimax.base;

import android.net.wimax.structs.*;
import android.net.wimax.types.*;

/**
 * This object contains information to the location of the common API
 * library. This object also contains all the attribute information
 * of the network connection and what subscribed API calls have been
 * called so that they don't get reissued.
 */
public class Driver
{
	private Connection connection         = null;
	private boolean cardConnected         = false;
	private boolean initialized           = false;
	private boolean[] apiCalled           = { false, false, false, false, false, false, false, false, false, false };
	private DeviceId deviceId             = null;
	private HardwareDeviceId[] list   	  = null;
	private int pHwDeviceIdListSize       = 0 ;
	private DeviceStatus pDeviceStatus    = null;
	private ConnectionProgressStatus pConnectionProgressInfo   = null;
	private ProfileInfo[] pProfileList    = null;
	private int pListSize                 = 0;
	private NSPInfo[] pNSPInfo            = null;
	private ConnectionStatistics connectionStatistics = null;
	private InterfaceInfo interfaceInfo   = null;
	private LinkStatusInfo pLinkStatus    = null;
	private ConnectedNspInfo pConnNspInfo = null;
	private DeviceInfo pDeviceInfo 		  = null;
	private PackageInfo pPackageInfo 	  = null;
	private boolean provisioningStatus 	  = false;
	private ContactInfo[] contactInfo 	  = null;

	/**
	 * Constructor for the Driver that contains all the informtion from
	 * the various calls made to the Common API.
	 */
	public Driver() {

	}

	/**
	 * Getter returns the hardware list.
	 *
	 * @return android.net.wimax.structs.HardwareDeviceId[]
	 */
	public HardwareDeviceId[] getList()
	{
		return list;
	}

	/**
	 * Getter return the Device Id list size.
	 *
	 * @return int[]
	 */
	public int getListSize()
	{
		return pHwDeviceIdListSize;
	}

	/**
	 * Getter returns the device status.
	 *
	 * @return DeviceStatus - the device status
	 * @see android.net.wimax.types.DeviceStatus
	 */
	public DeviceStatus getDeviceStatus()
	{
		return pDeviceStatus;
	}

	/**
	 * Getter returns the connection progress info.
	 *
	 * @return ConnectionProgressStatus - the connection progress status
	 * @see android.net.wimax.types.ConnectionProgressStatus
	 */
	public ConnectionProgressStatus getConnectionProgressInfo()
	{
		return pConnectionProgressInfo;
	}

	/**
	 * Getter returns the connection.
	 *
	 * @return android.net.wimax.Connection
	 */
	public Connection getConnection()
	{
		if (connection == null)
		{
			connection = new Connection(this);
		}

		return connection;
	}

	/**
	 * Getter returns the device id information.
	 *
	 * @return android.net.wimax.structs.DeviceId
	 */
	public DeviceId getDeviceId()
	{
		if (deviceId == null)
			deviceId = new DeviceId();

		return deviceId;
	}

	/**
	 * Getter returns the profile list size.
	 *
	 * @return int[]
	 */
	public int getProfileListSize()
	{
		return pListSize;
	}

	/**
	 * Getter returns the profile list.
	 *
	 * @return android.net.wimax.structs.ProfileInfo[]
	 */
	public ProfileInfo[] getProfileList()
	{
		return pProfileList;
	}

	/**
	 * Getter returns the NSP information.
	 *
	 * @return android.net.wimax.structs.NSPInfo[]
	 */
	public NSPInfo[] getNSPInfo()
	{
		return pNSPInfo;
	}

	/**
	 * This method returns the nsp info for the specified nsp
	 * @param nspName - nsp name
	 * @return NSPInfo - info for the specified nsp
	 */
	public NSPInfo getNSPInfo(String nspName) {
		NSPInfo nspInfo = null;

		if (pNSPInfo != null && pNSPInfo.length > 0) {
			for(int i=0; i<pNSPInfo.length; i++) {
				String nsp = pNSPInfo[i].getNspName();
				if(nsp.equalsIgnoreCase(nspName)){
					nspInfo = pNSPInfo[i];
					break;
				}
			}
		}

		return nspInfo;
	}

	/**
	 * Getter returns the connection statistics.
	 *
	 * @return android.net.wimax.structs.ConnectionStatistics[]
	 */
	public ConnectionStatistics getConnectionStatistics()
	{
		return connectionStatistics;
	}

	/**
	 * Getter returns the interface information.
	 *
	 * @return android.net.wimax.structs.InterfaceInfo
	 */
	public InterfaceInfo getInterfaceInfo()
	{
		return interfaceInfo;
	}

	/**
	 * Getter returns the link status information.
	 *
	 * @return android.net.wimax.structs.LinkStatusInfo
	 */
	public LinkStatusInfo getLinkStatusInfo()
	{
		return pLinkStatus;
	}

	/**
	 * Getter returns the Connected NSP informtion.
	 *
	 * @return android.net.wimax.structs.ConnectedNspInfo[]
	 */
	public ConnectedNspInfo getConnectedNspInfo()
	{
		return pConnNspInfo;
	}

	/**
	 * Getter returns the device informtion.
	 *
	 * @return android.net.wimax.structs.DeviceInfo
	 */
	public DeviceInfo getDeviceInfo()
	{
		return pDeviceInfo;
	}

	/**
	 * Getter returns the package informtion.
	 *
	 * @return android.net.wimax.structs.PackageInfo
	 */
	public PackageInfo getPackageInfo()
	{
		return pPackageInfo;
	}

	/**
	 * Get the provisioning status.
	 *
	 * @return boolean - the provisioningStatus
	 */
	public boolean getProvisioningStatus() {
		return provisioningStatus;
	}

	/**
	 * Get the contact information.
	 *
	 * @return android.net.wimax.structs.ContactInfo
	 */
	public ContactInfo[] getContactInfo() {
		return contactInfo;
	}

	/**
	 * Getter returns true if there is a WiMax card connected to the machine.
	 *
	 * @return boolean
	 */
	public boolean isCardConnected()
	{
		return cardConnected;
	}

	/**
	 * Set the is card connected value.
	 *
	 * @param cardConnected boolean
	 */
	public void isCardConnected(boolean cardConnected)
	{
		this.cardConnected = cardConnected;
		if (!cardConnected)
			isInitialized(cardConnected);
	}

	/**
	 * Is the card initialized?
	 *
	 * @return boolean - true is the card is initialized
	 */
	public boolean isInitialized()
	{
		return initialized;
	}

	/**
	 * Set whether or not the card is initialized.
	 *
	 * @param initialized boolean
	 */
	public void isInitialized(boolean initialized)
	{
		this.initialized = initialized;
	}

	/**
	 * Is the Common API opened?
	 *
	 * @return boolean
	 */
	public boolean isApiOpened()
	{
		return apiCalled[0];
	}

	/**
	 * Set the is Common API opened flag.
	 *
	 * @param value boolean
	 */
	public void isApiOpened(boolean value)
	{
		this.apiCalled[0] = value;
	}

	/**
	 * Is the device opened?
	 *
	 * @return boolean
	 */
	public boolean isDeviceOpened()
	{
		return apiCalled[1];
	}

	/**
	 * Set the is device opened flag.
	 *
	 * @param value boolean
	 */
	public void isDeviceOpened(boolean value)
	{
		this.apiCalled[1] = value;
	}

	/**
	 * Have we subscribed to the connect to network callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeConnectToNetwork()
	{
		return apiCalled[2];
	}

	/**
	 * Set the subscribed connect to network flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeConnectToNetwork(boolean value)
	{
		this.apiCalled[2] = value;
	}

	/**
	 * Have we subscribed to the disconnect to network callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeDisconnectToNetwork()
	{
		return apiCalled[3];
	}

	/**
	 * Set the subscribed disconnect to network flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeDisconnectToNetwork(boolean value)
	{
		this.apiCalled[3] = value;
	}

	/**
	 * Have we subscribed to the control power management callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeControlPowerManagement()
	{
		return apiCalled[4];
	}

	/**
	 * Set the subscribed to control power management flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeControlPowerManagement(boolean value)
	{
		this.apiCalled[4] = value;
	}

	/**
	 * Have we subscribed to the network search wide scan?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeNetworkSearchWideScan()
	{
		return apiCalled[5];
	}

	/**
	 * Set the subscribed network search wide scan flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeNetworkSearchWideScan(boolean value)
	{
		this.apiCalled[5] = value;
	}

	/**
	 * Have we subscribed to the package update callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribePackageUpdate()
	{
		return apiCalled[6];
	}

	/**
	 * Set the subscribe package update flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribePackageUpdate(boolean value)
	{
		this.apiCalled[6] = value;
	}

	/**
	 * Have we subscribed to the provisioning operation callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeProvisioningOperation()
	{
		return apiCalled[7];
	}

	/**
	 * Set the subscribe provisioning update flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeProvisioningOperation(boolean value)
	{
		this.apiCalled[7] = value;
	}

	/**
	 * Have we subscribed to the device status change callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeDeviceStatusChange()
	{
		return apiCalled[8];
	}

	/**
	 * Set the subscribe device status change flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeDeviceStatusChange(boolean value)
	{
		this.apiCalled[8] = value;
	}

	/**
	 * Have we subscribed to the device insert/remove callback?
	 *
	 * @return boolean
	 */
	public boolean isSubscribeDeviceInsertRemove()
	{
		return this.apiCalled[9];
	}

	/**
	 * Set the subscribe device insert/remove flag.
	 *
	 * @param value boolean
	 */
	public void isSubscribeDeviceInsertRemove(boolean value)
	{
		this.apiCalled[9] = value;
	}

	/**
	 * Do we have a device in the device list?
	 *
	 * @return boolean
	 */
	public boolean hasDevice()
	{
		return (getListSize() > 0);
	}

	/**
	 * Set the hardware device list from the common API call.
	 *
	 * @param list android.net.wimax.structs.HardwareDeviceList[]
	 */
	public void setList(HardwareDeviceId[] list)
	{
		this.list = list;
	}

	/**
	 * Set the device id list size.
	 *
	 * @param listSize int[]
	 */
	public void setListSize(int listSize)
	{
		this.pHwDeviceIdListSize = listSize;
	}

	/**
	 * Set the device status from the Common API call.
	 *
	 * @param deviceStatus DeviceStatus
	 */
	public void setDeviceStatus(DeviceStatus deviceStatus)
	{
		this.pDeviceStatus = deviceStatus;
	}

	/**
	 * Set the connection progress information from the Common API call.
	 *
	 * @param progressInfo ConnectionProgressStatus
	 */
	public void setConnectionProgressInfo(ConnectionProgressStatus progressInfo)
	{
		this.pConnectionProgressInfo = progressInfo;
	}

	/**
	 * Set the device id from the common API call.
	 *
	 * @param deviceId android.net.wimax.structs.DeviceId
	 */
	public void setDeviceId(DeviceId deviceId)
	{
		this.deviceId = deviceId;
	}

	/**
	 * Set the profile list size from the common API call.
	 *
	 * @param pListSize int[]
	 */
	public void setProfileListSize(int pListSize)
	{
		this.pListSize = pListSize;
	}

	/**
	 * Set the profile list from the common api call.
	 *
	 * @param pProfileList android.net.wimax.structs.ProfileInfo[]
	 */
	public void setProfileList(ProfileInfo[] pProfileList)
	{
		this.pProfileList = pProfileList;
	}

	/**
	 * Set the NSP info list from the common API call.
	 *
	 * @param nspInfoArray android.net.wimax.structs.NSPInfo[]
	 */
	public void setNSPInfo(NSPInfo[] nspInfoArray)
	{
		this.pNSPInfo = nspInfoArray;
	}

	/**
	 * Set the connection statistics from the common API call.
	 *
	 * @param connectionStatistics android.net.wimax.structs.ConnectionStatistics[]
	 */
	public void setConnectionStatistics(ConnectionStatistics connectionStatistics)
	{
		this.connectionStatistics = connectionStatistics;
	}

	/**
	 * Set the interface info from the common API call.
	 *
	 * @param interfaceInfo android.net.wimax.structs.InterfaceInfo
	 */
	public void setInterfaceInfo(InterfaceInfo interfaceInfo)
	{
		this.interfaceInfo = interfaceInfo;
	}

	/**
	 * Set the link status information from the common API call.
	 *
	 * @param pLinkStatus android.net.wimax.structs.LinkStatusInfo
	 */
	public void setLinkStatusInfo(LinkStatusInfo pLinkStatus)
	{
		this.pLinkStatus = pLinkStatus;
	}

	/**
	 * Set the connected NSP information from the commmon API call.
	 *
	 * @param pConnectedNspInfo android.net.wimax.structs.ConnectedNspInfo[]
	 */
	public void setConnectedNspInfo(ConnectedNspInfo pConnectedNspInfo)
	{
		this.pConnNspInfo = pConnectedNspInfo;
	}

	/**
	 * Set the device information from the commmon API call.
	 *
	 * @param pDeviceInfo android.net.wimax.structs.DeviceInfo
	 */
	public void setDeviceInfo(DeviceInfo pDeviceInfo)
	{
		this.pDeviceInfo = pDeviceInfo;
	}

	/**
	 * Set the package information from the commmon API call.
	 *
	 * @param pPackageInfo android.net.wimax.structs.PackageInfo
	 */
	public void setPackageInfo(PackageInfo pPackageInfo)
	{
		this.pPackageInfo = pPackageInfo;
	}

	/**
	 * Set the provisioning status.
	 * @param provisioningStatus boolean - the provisioningStatus to set
	 */
	public void setProvisioningStatus(boolean provisioningStatus) {
		this.provisioningStatus = provisioningStatus;
	}

	/**
	 * Set the contact information from the commmon API call.
	 *
	 * @param contactInfo android.net.wimax.structs.ContactInfo
	 */
	public void setContactInfo(ContactInfo[] contactInfo)
	{
		this.contactInfo = contactInfo;
	}

	/**
	 * Reset all the API's that have been called back to false.
	 *
	 */
	public void reset()
	{
		for (int i=0;i<apiCalled.length;i++)
			apiCalled[i] = false;
		initialized = false;
	}
}
