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

import android.net.wimax.structs.NSPInfo;
import android.net.wimax.types.ConnectionProgressStatus;
import android.net.wimax.types.DeviceStatus;
import android.net.wimax.types.DeviceStatusChangeReason;
import android.net.wimax.types.PackageUpdateStatus;
import android.net.wimax.types.ProvisioningOperationStatus;

/**
 * This interface contains the callback methods from the subscribed
 * callbacks as defined in the Common API.
 */
public interface CallbackListener
{
   public void callbackDeviceStatusChange (DeviceStatus deviceStatus, DeviceStatusChangeReason reason, ConnectionProgressStatus connectionProgressStatus);
   public void callbackDeviceInsertRemove (boolean cardPresent);
   public void callbackControlPowerManagement (int status);
   public void callbackConnectToNetwork (int status);
   public void callbackDisconnectToNetwork (int status);
   public void callbackNetworkSearchWideScan (NSPInfo[] nspInfo);
   public void callbackProvisioningOperation (ProvisioningOperationStatus provisioningOperation, int contactType);
   public void callbackPackageUpdate (PackageUpdateStatus packageUpdate);
}
