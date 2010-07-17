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

package android.net.wimax;

import android.net.wimax.structs.ConnectedNspInfo;
import android.net.wimax.structs.ConnectionStatistics;
import android.net.wimax.structs.ContactInfo;
import android.net.wimax.structs.DeviceInfo;
import android.net.wimax.structs.HardwareDeviceId;
import android.net.wimax.structs.LinkStatusInfo;
import android.net.wimax.structs.NSPInfo;
import android.net.wimax.structs.PackageInfo;

/**
 * Interface that allows controlling and querying Wimax connectivity.
 *
 * {@hide}
 */
interface IWimaxManager
{

	List<HardwareDeviceId> getDeviceList();

	List<NSPInfo> getNetworkList();

	boolean performWideScan();

	boolean connect(String nspName);

	boolean disconnect();

	int getWimaxState();

	ConnectedNspInfo getConnectedNSP();

	ConnectionStatistics getConnectionStatsitics();

	LinkStatusInfo getLinkStatus();

	DeviceInfo getDeviceInformation();

	PackageInfo getPackageInformation();

	boolean setPackageUpdateState(int state);

	List<ContactInfo> getContactInformation();

	boolean isRoamingEnabled();

	boolean setRoamingEnabled(boolean enable);

	int getWimaxStatus();

	boolean setWimaxEnabled(boolean enable);

	boolean acquireWimaxLock(IBinder lock, String tag);

    boolean releaseWimaxLock(IBinder lock);
}