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
 * This class contains the information regarding the link status.
 */
public class LinkStatusInfo implements Parcelable {
	
	/** {@hide} */
	public int structureSize   = 0;
	/** {@hide} */
	public int centerFrequency = 0;
	/** {@hide} */
	public byte rssi   		   = 0;
	/** {@hide} */
	public byte cinr 		   = 0;
	/** {@hide} */
	public byte txPwr 		   = 0;
	/** {@hide} */
	public byte[] bsid		   = new byte[6];
	
	
	/**
	 * This is the default constructor for the Link Status Information C struct.
	 */
	public LinkStatusInfo() { }
	
	/** Constructor with fields */
	public LinkStatusInfo(int centerFrequency, byte rssi, byte cinr,
			byte txPwr, byte[] bsid) {
		super();
		this.centerFrequency = centerFrequency;
		this.rssi = rssi;
		this.cinr = cinr;
		this.txPwr = txPwr;
		this.bsid = bsid;
	}

	/**
	 * @return the centerFrequency
	 */
	public int getCenterFrequency() {
		return centerFrequency;
	}

	/**
	 * @param centerFrequency the centerFrequency to set
	 */
	public void setCenterFrequency(int centerFrequency) {
		this.centerFrequency = centerFrequency;
	}

	/**
	 * @return the rssi
	 */
	public byte getRssi() {
		return rssi;
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
	 * @param cinr the cinr to set
	 */
	public void setCinr(byte cinr) {
		this.cinr = cinr;
	}

	/**
	 * @return the txPwr
	 */
	public byte getTxPwr() {
		return txPwr;
	}

	/**
	 * @param txPwr the txPwr to set
	 */
	public void setTxPwr(byte txPwr) {
		this.txPwr = txPwr;
	}

	/**
	 * @return the bsid
	 */
	public byte[] getBsid() {
		return bsid;
	}

	/**
	 * @param bsid the bsid to set
	 */
	public void setBsid(byte[] bsid) {
		this.bsid = bsid;
	}

	/**
	 * Mean RSSI measured by device. As according to IEEE 802.16 standard, values
	 * are ranging from 0x00 to 0x53, where -123dBm is encoded as 0x00 and -40dBm
	 * encoded as 0x53 with 1dB increments.
	 * 
	 * @return int - derived value from rssi
	 */
	public int getRssiInDBm() {
		int val = (0xff & rssi) - 123;
		return val;
	}
	
	/**
	 * Mean RSSI measured by device. As according to IEEE 802.16 standard, values
	 * are ranging from 0x00 to 0x3F, where -10dB is encoded as 0x00 and 53dB encoded
	 * as 0x3F with 1dB increments.
	 * 
	 * @return int - derived value from cinr
	 */
	public int getCinrInDB() {
		int val = (0xff & cinr) - 10;
		return val;
	}

	/**
	 * Average transmit power for the last burst transmitted by the device. Based on
	 * IEEE802.16 standard, the values are ranging from 0xxx to 0xFF, where -84dBm is
	 * encoded as 0x00  and 43.5dBm is encoded as 0xFF with 0.5dB increment.
	 * 
	 * @return int - derived value from txPwr
	 */
	public int getTxPwrInDBm() {
		int val = ((0xff & txPwr)/2) - 84;
		return val;
	}
	
	/**
	 * Determine the Base Station Id display value from the byte[], for example
	 * "00:00:01:04:4f:6e".
	 * 
	 * @return java.lang.String - derived value from the bsid byte[]
	 */
	public String getBsIdString() {
		String formattedBsid = "";
		for (int i=0;i<bsid.length;i++) {
			if (formattedBsid.equals(""))
				formattedBsid = leftZeroFill(Integer.toHexString(0xff & bsid[i]), 2);
			else
				formattedBsid = formattedBsid + ":" + leftZeroFill(Integer.toHexString(0xff & bsid[i]), 2);
		}
		
		return formattedBsid.toUpperCase();
	}
	
	/**
	 * Left zero fill Base Station Identifiers.
	 * 
	 * @param twoDigitNumber java.lang.String
	 * @param numToFill int - How many digits there should be displayed. 
	 * @return java.lang.String - For example "01"
	 */
	private String leftZeroFill(String twoDigitNumber, int numToFill) {
		String value = twoDigitNumber;
		for (int i=0;i<(numToFill - twoDigitNumber.length()); i++) {
			value = "0" + value;
		}	
		
		return value;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: "+this.getClass().getName());
  		sb.append("[  Structure size: ");sb.append(structureSize);
  		sb.append(",  Center Freq: ");sb.append(centerFrequency);
  		sb.append(",  RSSI: ");sb.append(rssi);
  		sb.append(",  CINR: ");sb.append(cinr);
  		sb.append(",  Tx Power: ");sb.append(txPwr);
  		sb.append(",  Bs Id: ");sb.append(bsid);
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
        dest.writeInt(centerFrequency);        
        dest.writeByte(rssi);
        dest.writeByte(cinr);
        dest.writeByte(txPwr);
        dest.writeByteArray(bsid);

    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<LinkStatusInfo> CREATOR =
        new Creator<LinkStatusInfo>() {
            public LinkStatusInfo createFromParcel(Parcel in) {
                LinkStatusInfo info = new LinkStatusInfo();
                info.structureSize = in.readInt();
                info.centerFrequency = in.readInt();
                info.rssi = in.readByte();
                info.cinr = in.readByte();
                info.txPwr = in.readByte();
                in.readByteArray(info.bsid);
                return info;
            }

            public LinkStatusInfo[] newArray(int size) {
                return new LinkStatusInfo[size];
            }
        };
}
