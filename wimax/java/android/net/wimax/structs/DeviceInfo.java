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
 * This class contains the information about the Device.
 */
public class DeviceInfo implements Parcelable {
	
	/** {@hide} */
	public int structureSize           = 0;
	/** {@hide} */
	public DeviceVersion hwVersion     = new DeviceVersion();
	/** {@hide} */
	public DeviceVersion swVersion     = new DeviceVersion();
	/** {@hide} */
	public DeviceVersion rfVersion     = new DeviceVersion();
	/** {@hide} */
	public DeviceVersion asicVersion   = new DeviceVersion();
	/** {@hide} */
	public byte[] macAddress           = new byte[6];
	/** {@hide} */
	public String vendorName           = new String();
	/** {@hide} */
	public boolean vendorSpecificInfoIncl = false;
	/** {@hide} */
	public String vendorSpecificInfo   = new String();
	
	/**
	 * This is the default constructor for the Device Information.
	 */
	public DeviceInfo() { }

	/** Constructor with fields. */
	public DeviceInfo(DeviceVersion hwVersion, DeviceVersion swVersion,
			DeviceVersion rfVersion, DeviceVersion asicVersion,
			byte[] macAddress, String vendorName,
			boolean vendorSpecificInfoIncl, String vendorSpecificInfo) {
		super();
		this.hwVersion = hwVersion;
		this.swVersion = swVersion;
		this.rfVersion = rfVersion;
		this.asicVersion = asicVersion;
		this.macAddress = macAddress;
		this.vendorName = vendorName;
		this.vendorSpecificInfoIncl = vendorSpecificInfoIncl;
		this.vendorSpecificInfo = vendorSpecificInfo;
	}	
	
	/**
	 * @return the hwVersion
	 */
	public DeviceVersion getHwVersion() {
		return hwVersion;
	}

	/**
	 * @param hwVersion the hwVersion to set
	 */
	public void setHwVersion(DeviceVersion hwVersion) {
		this.hwVersion = hwVersion;
	}

	/**
	 * @return the swVersion
	 */
	public DeviceVersion getSwVersion() {
		return swVersion;
	}

	/**
	 * @param swVersion the swVersion to set
	 */
	public void setSwVersion(DeviceVersion swVersion) {
		this.swVersion = swVersion;
	}

	/**
	 * @return the rfVersion
	 */
	public DeviceVersion getRfVersion() {
		return rfVersion;
	}

	/**
	 * @param rfVersion the rfVersion to set
	 */
	public void setRfVersion(DeviceVersion rfVersion) {
		this.rfVersion = rfVersion;
	}

	/**
	 * @return the asicVersion
	 */
	public DeviceVersion getAsicVersion() {
		return asicVersion;
	}

	/**
	 * @param asicVersion the asicVersion to set
	 */
	public void setAsicVersion(DeviceVersion asicVersion) {
		this.asicVersion = asicVersion;
	}

	/**
	 * @return the macAddress
	 */
	public byte[] getMacAddress() {
		return macAddress;
	}
	
	/**
	 * Determine the MAC Address String display value from the byte[], for example
	 * "22:23:98:90:22:70".
	 * 
	 * @return java.lang.String - derived value from the macAddress byte[]
	 */
	public String getMacAddressString()
	{
		String macAddrStr = "";
		for(int i=0; i<macAddress.length; i++) {
			String macByte = Integer.toHexString(0xff & macAddress[i]);
			macAddrStr += (macByte.length()==1 ?"0"+macByte :macByte) + ((i==macAddress.length-1) ?"" :":");
		}
		
		return macAddrStr.toUpperCase();
	}

	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(byte[] macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the vendorName
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**
	 * @param vendorName the vendorName to set
	 */
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	/**
	 * @return the vendorSpecificInfoIncl
	 */
	public boolean isVendorSpecificInfoIncl() {
		return vendorSpecificInfoIncl;
	}

	/**
	 * @param vendorSpecificInfoIncl the vendorSpecificInfoIncl to set
	 */
	public void setVendorSpecificInfoIncl(boolean vendorSpecificInfoIncl) {
		this.vendorSpecificInfoIncl = vendorSpecificInfoIncl;
	}

	/**
	 * @return the vendorSpecificInfo
	 */
	public String getVendorSpecificInfo() {
		return vendorSpecificInfo;
	}

	/**
	 * @param vendorSpecificInfo the vendorSpecificInfo to set
	 */
	public void setVendorSpecificInfo(String vendorSpecificInfo) {
		this.vendorSpecificInfo = vendorSpecificInfo;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
		sb.append("[  Structure size =");sb.append(structureSize).append("\n");
  		sb.append(",  Hardware Version: [ ");sb.append(hwVersion);sb.append(" ]").append("\n");
  		sb.append(",  Software Version: [ ");sb.append(swVersion);sb.append(" ]").append("\n");
  		sb.append(",  RF Version: [ ");sb.append(rfVersion);sb.append(" ]").append("\n");
  		sb.append(",  Asic Version: [ ");sb.append(asicVersion);sb.append(" ]").append("\n");
  		sb.append(",  MAC Address: ");sb.append(macAddress);
  		sb.append(",  Vendor Name: ");sb.append(vendorName);
  		sb.append(",  Vendor Specific Incl: ");sb.append(vendorSpecificInfoIncl);
  		sb.append(",  Vendor Specific: ");sb.append(vendorSpecificInfo);
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
    	dest.writeParcelable(hwVersion, 0);
    	dest.writeParcelable(swVersion, 0);
    	dest.writeParcelable(rfVersion, 0);
    	dest.writeParcelable(asicVersion, 0);
        dest.writeByteArray(macAddress);
        dest.writeString(vendorName);
        dest.writeInt(vendorSpecificInfoIncl ?1 :0);
        dest.writeString(vendorSpecificInfo);

    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<DeviceInfo> CREATOR =
        new Creator<DeviceInfo>() {
            public DeviceInfo createFromParcel(Parcel in) {
                DeviceInfo info = new DeviceInfo();
                info.structureSize = in.readInt();
                info.hwVersion = in.readParcelable(null);
                info.swVersion = in.readParcelable(null);
                info.rfVersion = in.readParcelable(null);
                info.asicVersion = in.readParcelable(null);
                in.readByteArray(info.macAddress);
                info.vendorName = in.readString();
                info.vendorSpecificInfoIncl = (in.readInt() == 1) ?true :false;
                info.vendorSpecificInfo = in.readString();                
                return info;
            }

            public DeviceInfo[] newArray(int size) {
                return new DeviceInfo[size];
            }
        };
}
