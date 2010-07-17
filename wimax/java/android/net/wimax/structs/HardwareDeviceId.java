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
 * This class contains the information for hardware device id.
 */
public class HardwareDeviceId implements Parcelable {

	/** {@hide} */
	public int structureSize   = 0;
	/** {@hide} */
	public byte deviceIndex    = 0;
	/** {@hide} */
	public String deviceName   = new String();
	/** {@hide} */
	public int deviceType 	   = 0;

	/**
	 * This is the default constructor for the Hardware Device Id.
	 */
	public HardwareDeviceId() { }

	/** Constructor with fields */
	public HardwareDeviceId(byte deviceIndex, String deviceName, int deviceType) {
		super();
		this.deviceIndex = deviceIndex;
		this.deviceName = deviceName;
		this.deviceType = deviceType;
	}

	/**
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
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @return the deviceType
     */
    public int getDeviceType() {
        return deviceType;
    }

    /**
     * @param deviceType the deviceType to set
     */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    @Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: "+this.getClass().getName());
  		sb.append("[  Structure Size: ");sb.append(structureSize);
  		sb.append(",  Device Index: ");sb.append(deviceIndex);
  		sb.append(",  Device Name: ");sb.append(deviceName);
  		sb.append(",  Device Type: ");sb.append(deviceType);
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
    	dest.writeByte(deviceIndex);
        dest.writeString(deviceName);
        dest.writeInt(deviceType);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<HardwareDeviceId> CREATOR =
        new Creator<HardwareDeviceId>() {
            public HardwareDeviceId createFromParcel(Parcel in) {
                HardwareDeviceId info = new HardwareDeviceId();
                info.structureSize = in.readInt();
                info.deviceIndex = in.readByte();
                info.deviceName = in.readString();
                info.deviceType = in.readInt();
                return info;
            }

            public HardwareDeviceId[] newArray(int size) {
                return new HardwareDeviceId[size];
            }
        };
}
