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
 * This class contains the information regarding the Connection statistics.
 */
public class ConnectionStatistics implements Parcelable {
	
	/** {@hide} */
	public int structureSize   = 0;
	/** {@hide} */
	public long totalRxBytes    = 0;
	/** {@hide} */
	public long totalTxBytes    = 0;
	/** {@hide} */
	public long totalRxPackets = 0;
	/** {@hide} */
	public long totalTxPackets = 0;
	
	/**
	 * Default constructor for Connection Statistics.
	 */
	public ConnectionStatistics() { }

	/**
	 * Constructor with fields.
	 */
	public ConnectionStatistics(long totalRxBytes, long totalTxBytes,
			long totalRxPackets, long totalTxPackets) {
		super();
		this.totalRxBytes = totalRxBytes;
		this.totalTxBytes = totalTxBytes;
		this.totalRxPackets = totalRxPackets;
		this.totalTxPackets = totalTxPackets;
	}

	/**
	 * Return the number of total bytes received.
	 * @return the totalRxBytes
	 */
	public long getTotalRxBytes() {
		return totalRxBytes;
	}

	/**
	 * @param totalRxBytes the totalRxBytes to set
	 */
	public void setTotalRxBytes(long totalRxBytes) {
		this.totalRxBytes = totalRxBytes;
	}

	/**
	 * Return the total bytes transmitted.
	 * @return the totalTxBytes
	 */
	public long getTotalTxBytes() {
		return totalTxBytes;
	}

	/**
	 * @param totalTxBytes the totalTxBytes to set
	 */
	public void setTotalTxBytes(long totalTxBytes) {
		this.totalTxBytes = totalTxBytes;
	}
	
	/**
	 * Return the total packets received.
	 * @return the totalRxPackets
	 */
	public long getTotalRxPackets() {
		return totalRxPackets;
	}

	/**
	 * @param totalRxPackets the totalRxPackets to set
	 */
	public void setTotalRxPackets(long totalRxPackets) {
		this.totalRxPackets = totalRxPackets;
	}
	
	/**
	 * Return the total transmitted packets.
	 * @return the totalTxPackets
	 */
	public long getTotalTxPackets() {
		return totalTxPackets;
	}

	/**
	 * @param totalTxPackets the totalTxPackets to set
	 */
	public void setTotalTxPackets(long totalTxPackets) {
		this.totalTxPackets = totalTxPackets;
	}

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		sb.append("Class: ");sb.append(this.getClass().getName());
  		sb.append("[  Structure size: ");sb.append(structureSize);
  		sb.append(",  Total Rx Bytess: ");sb.append(totalRxBytes);
  		sb.append(",  Total Tx Bytess: ");sb.append(totalTxBytes);
  		sb.append(",  Total Rx Packets: ");sb.append(totalRxPackets);
  		sb.append(",  Total Tx Packets: ");sb.append(totalTxPackets);
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
    	dest.writeLong(totalRxBytes);
    	dest.writeLong(totalTxBytes);
    	dest.writeLong(totalRxPackets);
    	dest.writeLong(totalTxPackets);
    }

    /** Implement the Parcelable interface {@hide} */
    public static final Creator<ConnectionStatistics> CREATOR =
        new Creator<ConnectionStatistics>() {
            public ConnectionStatistics createFromParcel(Parcel in) {
                ConnectionStatistics info = new ConnectionStatistics();
                info.structureSize = in.readInt();
                info.totalRxBytes = in.readLong();
                info.totalTxBytes = in.readLong();
                info.totalRxPackets = in.readLong();
                info.totalTxPackets = in.readLong();                
                return info;
            }

            public ConnectionStatistics[] newArray(int size) {
                return new ConnectionStatistics[size];
            }
        };	
}
