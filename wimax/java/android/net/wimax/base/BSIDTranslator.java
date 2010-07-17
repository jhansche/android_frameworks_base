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

/**
 * This class is responsible for breaking down a byte[] into bit groupings
 * as defined by the int array passed in.  The groupings are then broken
 * out and converted into decimal for display.
 */
public class BSIDTranslator
{
	// Manufacturer Types
	public static final int SAMSUNG   = 0;
	public static final int MOTOROLA  = 1;
	public static final int NOKIA     = 2;

	// bit values
	private static final int[] bits  = new int[32];

	public BSIDTranslator()
	{
		bits[0] = 1;
		for (int i=1;i<32;i++)
		{	bits[i] = bits[i-1] * 2; }
	}

	/**
	 * This method determines the breakdown by the manufacturer type, each
	 * card manufacturer could have a different breakdown of the bits.
	 *
	 * @param bsid byte[] - Base Station Id
	 * @param manufactureType int (SAMSUNG (0), MOTOROLA (1) or NOKIA (2))
	 * @return java.lang.String
	 */
	public String translate(byte[] bsid, int manufactureType)
	{
		int[] breakdown = null;
		if (manufactureType == SAMSUNG)
		{
			/* Samsung layout
			 *
			 * 48 total bits:
			 *   24 MSBs = Operator ID/NAP ID.
			 *   1-bit NSP identifier
			 *   ????  2 bits for vendor identifier - 00/01 for Samsung
			 *   ACR ID (7 Bits): range 0-127
			 *   FA ID (4 Bits): Under a RAS, FA can be 0 ~ 15
			 *   RAS ID (10 Bits): Under a ACR, RAS can be 0 ~ 1023
			 *   Sector ID (2 Bits): Under a FA, Sector can be 0 ~ 3
			 */

			breakdown = new int[] { 24, 1, 2, 5, 4, 10, 2 };
		}
		else
		if (manufactureType == MOTOROLA)
		{
			/* Motorola layout
			 *
			 * 48 total bits:
			 *   24 MSBs = Operator ID/NAP ID.
			 *   1-bit NSP identifier
			 *   Vendor Id (2 bits)
			 *   ACR ID (5 Bits): range 0-127
			 *   ASN-GW ID (4 Bits)
			 *   AP ID (10 Bits)
			 *   Sector ID (2 Bits)
			 */

			breakdown = new int[] { 24, 1, 2, 5, 4, 10, 2 };
		}
		else
		if (manufactureType == NOKIA)
		{
			/* Nokia layout
			 *
			 * 48 total bits:
			 *   24 MSBs = Operator ID/NAP ID.
			 *   1-bit NSP identifier
			 *   ACR ID (7 Bits): range 0-127
			 *   ASN-GW ID (10 Bits)
			 *   Site ID (10 Bits)
			 *   Sector ID (3 Bits)
			 */

			breakdown = new int[] { 24, 1, 2, 8, 10, 3 };
		}

		return translate(breakdown, bsid);
	}

	/**
	 * This method takes in the bit breakdown of the byte[] and translates
	 * each bit group into a decimal.  A string is put together with "." between
	 * each decimal.
	 *
	 * @param breakdown int[]
	 * @param bsid byte[] - Base Station Id
	 * @return java.lang.String - Representation of the BSID, i.e. 4.0.0.1.2.215.2
	 */
	public String translate(int[] breakdown, byte[] bsid)
	{
		String translatedBSID = null;

		if (breakdown != null || breakdown.length > 0)
		{
			int totalLength = bsid.length * 8;
			int breakdownLength = 0;
			for (int i=0;i<breakdown.length;i++)
			{
				breakdownLength += breakdown[i];
			}

			if (totalLength == breakdownLength)
			{
				// The total breakdown length equals the number of bits in the bsid so
				// lets translate it.
				int[] digits = breakdownBSID(breakdown, bsid);
				translatedBSID = formatDisplayableBSID(digits);
			}
		}

		return translatedBSID;
	}

	/**
	 * This method breaks down the byte array into bit groups or integers
	 * as described in the breakdown array.  The values in the breakdown
	 * array describe the bit groups and they must total the bsid bit
	 * length.
	 *
	 * Bytes 26 & 27 are vendor id's:
	 * - 00 & 01 = Samsung
	 * - 11 = Motorola
	 * - 10 = Nokia
	 *
	 * Samsung Example:<FONT FACE= "Courier New" size=-1>
	 * BSID byte values: 00 00 04 01 35 94
	 * BSID binary values: 00000000 00000000 00000100 00000001 00100011 01011110
	 * --------------------|________________________| ||_|___| |__|__________|_|
	 * ------------------------------24 bits----------1-2--5----4------10-----2
	 *
	 * Breakdown: 24, 1, 2, 5, 4, 10, 2  (Adds up to 48 bits or 6 bytes)
	 *
	 * Breakdown Values (BSID): 4.0.0.1.2.215.2</font>
	 *
	 *
	 * @param breakdown int[]
	 * @param bsid int[]
	 * @return int[] - Breakdown values
	 */
	private int[] breakdownBSID(int[] breakdown, byte[] bsid)
	{
		int[] bsidDigits = new int[breakdown.length];

		int currentByte = 0;
		int startNewBytePos = 0;
		for (int i=0;i<breakdown.length;i++)
		{
			startNewBytePos += breakdown[i];
			currentByte = (startNewBytePos / 8) - 1;
			if ((startNewBytePos % 8) > 0) currentByte ++;

			int intValue  = 0;
			int currPos   = startNewBytePos;
			int newBitPos = 0;
			for (int j=0;j<breakdown[i];j++)
			{
				int pos = currPos%8;
				if (pos != 0) pos = 8 - pos;
            int bit = 1 & bsid[currentByte]>>pos;

				if (bit == 1)
					intValue += bits[newBitPos];

				newBitPos++;

				currPos--;
				if ((currPos%8) == 0) currentByte --;
			}

			bsidDigits[i] = intValue;
		}

		return bsidDigits;
	}

	/**
	 * Put the breakdown values into a string delimited by a period.
	 *
	 * @param digits int[]
	 * @return java.lang.String
	 */
	private String formatDisplayableBSID(int[] digits)
	{
		String bsidDisplaybleString = null;
		for (int i=0;i<digits.length;i++)
		{
			if (bsidDisplaybleString == null)
				bsidDisplaybleString = String.valueOf(digits[i]);
			else
				bsidDisplaybleString = bsidDisplaybleString + "." + String.valueOf(digits[i]);
		}

		return bsidDisplaybleString;
	}
}
