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
 * This class contains the information regarding the connected NSP.
 */
public class NSPInfo implements Parcelable {
	
	/** {@hide} */
	public int structureSize = 0;
	/** {@hide} */
	public String nspName    = new String();
	/** {@hide} */
	public int nspId         = 0;
	/** {@hide} */
	public byte rssi         = 0;
	/** {@hide} */
	public byte cinr         = 0;
	/** {@hide} */
	public int networkType   = 0;
	
	/**
	 * This is the default constructor for the Connected NSP Information.
	 */
	public NSPInfo() { }
	
	/**
	 * Constructor with fields.
	 */
	public NSPInfo(String nspName, int nspId, 
			byte rssi, byte cinr, int networkType) {
		super();
		this.nspName = nspName;
		this.nspId = nspId;
		this.rssi = rssi;
		this.cinr = cinr;
		this.networkType = networkType;
	}
	
	/**
	 * @return the nspName
	 */
	public String getNspName() {
		return nspName;
	}

	/**
	 * @param nspName the nspName to set
	 */
	public void setNspName(String nspName) {
		this.nspName = nspName;
	}

	/**
	 * @return the nspId
	 */
	public int getNspId() {
		return nspId;
	}

	/**
	 * @param nspId the nspId to set
	 */
	public void setNspId(int nspId) {
		this.nspId = nspId;
	}

	/**
	 * @return the rssi
	 */
	public byte getRssi() {
		return rssi;
	}
	
	/**
	 * @return the rssi in dBm
	 */
	public int getRssiInDBm() {
		int val = (0xff & rssi) - 123;
		return val;
	}

	/**
	 * @param rssi the rssi to set
	 */
	public void setRssi(byte rssi) {
		this.rssi = rssi;
	}

	/**
	 * @return the cinr
	 */
	public byte getCinr() {
		return cinr;
	}
	
	/**
	 * @return the cinr in dB
	 */
	public int getCinrInDB() {
		int val = (0xff & cinr) - 10;
		return val;
	}

	/**
	 * @param cinr the cinr to set
	 */
	public void setCinr(byte cinr) {
		this.cinr = cinr;
	}

	/**
	 * @return the networkType
	 */
	public int getNetworkType() {
		return networkType;
	}

	/**
	 * @param networkType the networkType to set
	 */
	public void setNetworkType(int networkType) {
		this.networkType = networkType;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure size: ");sb.append(structureSize);
  		sb.append(",  Name: ");sb.append(nspName);
  		sb.append(",  NSP Id: ");sb.append(nspId);
  		sb.append(",  RSSI: ");sb.append(rssi);
  		sb.append(",  CINR: ");sb.append(cinr);
  		sb.append(",  Network Type: ");sb.append(networkType);
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
        dest.writeString(nspName);
        dest.writeInt(nspId);
        dest.writeByte(rssi);
        dest.writeByte(cinr);
        dest.writeInt(networkType);

    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<NSPInfo> CREATOR =
        new Creator<NSPInfo>() {
            public NSPInfo createFromParcel(Parcel in) {
                NSPInfo info = new NSPInfo();
                info.structureSize = in.readInt();
                info.nspName = in.readString();
                info.nspId = in.readInt();
                info.rssi = in.readByte();
                info.cinr = in.readByte();
                info.networkType = in.readInt();
                return info;
            }

            public NSPInfo[] newArray(int size) {
                return new NSPInfo[size];
            }
        };
}
