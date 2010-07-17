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

package android.net.wimax.structs;

import android.os.Parcelable;
import android.os.Parcel;

/**
* This class contains the information for device identification.
*
* @hide
*/
public class DeviceId implements Parcelable {

	/** {@hide} */
	public int structureSize   = 0;
	/** {@hide} */
	public int sdkHandle       = 0;
	/** {@hide} */
	public int privilege 	   = 0;
	/** {@hide} */
	public byte deviceIndex    = 0x0000;
	/** {@hide} */
	public int apiVersion      = 0;
	/** {@hide} */
	public boolean devicePresenceStatus = false;

	/**
	 * The default constructor for the Device Id.
	 */
	public DeviceId() {	}

	/** Constructor with fields. */
	public DeviceId(int sdkHandle, int privilege, byte deviceIndex,
			int apiVersion, boolean devicePresenceStatus) {
		super();
		this.sdkHandle = sdkHandle;
		this.privilege = privilege;
		this.deviceIndex = deviceIndex;
		this.apiVersion = apiVersion;
		this.devicePresenceStatus = devicePresenceStatus;
	}

	/**
	 * Return the sdk handle.
	 * @return the sdkHandle
	 */
	public int getSdkHandle() {
		return sdkHandle;
	}

	/**
	 * @param sdkHandle the sdkHandle to set
	 */
	public void setSdkHandle(int sdkHandle) {
		this.sdkHandle = sdkHandle;
	}

	/**
	 * Return the read/write privilege
	 * @return the privilege
	 */
	public int getPrivilege() {
		return privilege;
	}

	/**
	 * @param privilege the privilege to set
	 */
	public void setPrivilege(int privilege) {
		this.privilege = privilege;
	}

	/**
	 * Return the device Index
	 * @return the deviceIndex
	 */
	public byte getDeviceIndex() {
		return deviceIndex;
	}

	/**
	 * @param deviceIndex the deviceIndex to set
	 */
	public void setDeviceIndex(byte deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	/**
	 * Return the api version
	 * @return the apiVersion
	 */
	public int getApiVersion() {
		return apiVersion;
	}

	/**
	 * Return the api version in string format
	 * @return the apiVersion
	 */
	public String getApiVersionString() {
		return convertToVersionNumber(apiVersion);
	}

	/**
	 * @param apiVersion the apiVersion to set
	 */
	public void setApiVersion(int apiVersion) {
		this.apiVersion = apiVersion;
	}

	/**
	 * Return the device presence status
	 * @return the devicePresenceStatus
	 */
	public boolean isDevicePresenceStatus() {
		return devicePresenceStatus;
	}

	/**
	 * @param devicePresenceStatus the devicePresenceStatus to set
	 */
	public void setDevicePresenceStatus(boolean devicePresenceStatus) {
		this.devicePresenceStatus = devicePresenceStatus;
	}

	private String convertToVersionNumber(int apiVersion) {
		byte[] buf = new byte[4];
		buf[0]=(byte)((apiVersion & 0xff000000)>>>24);
		buf[1]=(byte)((apiVersion & 0x00ff0000)>>>16);
		buf[2]=(byte)((apiVersion & 0x0000ff00)>>>8);
		buf[3]=(byte)((apiVersion & 0x000000ff));

		int length = buf.length;
        StringBuffer sb = new StringBuffer(12);

        for(int i=0;i<length;i++) {
     		sb.append(buf[i]);
     		if ((i+1) != length) sb.append(".");
        }

      return sb.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: "+this.getClass().getName());
		sb.append("[  Structure Size: ").append(structureSize);
		sb.append(",  SDK Handle: ").append(sdkHandle);
		sb.append(",  Privilege: ").append(privilege);
		sb.append(",  Device Index: ").append(deviceIndex);
  		sb.append(",  Version: ").append(apiVersion);
  		sb.append(",  Device Present: ").append(devicePresenceStatus);
  		sb.append("  ]");

  		return sb.toString();
	}

    /** Implement the Parcelable interface {@hide} */
    public int describeContents() {
        return 0;
    }

    /** Implement the Parcelable interface {@hide} */
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(structureSize);
    	dest.writeInt(sdkHandle);
    	dest.writeInt(privilege);
    	dest.writeByte(deviceIndex);
    	dest.writeInt(apiVersion);
    	dest.writeInt(devicePresenceStatus ?1 :0);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<DeviceId> CREATOR =
        new Creator<DeviceId>() {
            public DeviceId createFromParcel(Parcel in) {
                DeviceId info = new DeviceId();
                info.structureSize = in.readInt();
                info.sdkHandle = in.readInt();
                info.privilege = in.readInt();
                info.deviceIndex = in.readByte();
                info.apiVersion = in.readInt();
                info.devicePresenceStatus = (in.readInt() == 1) ?true :false;
                return info;
            }

            public DeviceId[] newArray(int size) {
                return new DeviceId[size];
            }
        };
}
