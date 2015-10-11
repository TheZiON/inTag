/***************************************************************************************************
 
 The technology detailed in this software is the subject of various pending and issued patents,
 both internationally and in the United States, including one or more of the following patents:
 
 5,636,292 C1; 5,710,834; 5,832,119 C1; 6,286,036; 6,311,214; 6,353,672; 6,381,341; 6,400,827;
 6,516,079; 6,580,808; 6,614,914; 6,647,128; 6,681,029; 6,700,990; 6,704,869; 6,813,366;
 6,879,701; 6,988,202; 7,003,132; 7,013,021; 7,054,465; 7,068,811; 7,068,812; 7,072,487;
 7,116,781; 7,158,654; 7,280,672; 7,349,552; 7,369,678; 7,461,136; 7,564,992; 7,567,686;
 7,590,259; 7,657,057; 7,672,477; 7,720,249; 7,751,588; and EP 1137251 B1; EP 0824821 B1;
 and JP-3949679, all owned by Digimarc Corporation.
 
 Use of such technology requires a license from Digimarc Corporation, USA.  Receipt of this software
 conveys no license under the foregoing patents, nor under any of Digimarc's other patent, trademark,
 or copyright rights.
 
 This software comprises CONFIDENTIAL INFORMATION, including TRADE SECRETS, of Digimarc Corporation,
 USA, and is protected by a license agreement and/or non-disclosure agreement with Digimarc.  It is
 important that this software be used, copied and/or disclosed only in accordance with such
 agreements.
 
 Copyright, Digimarc Corporation, USA.  All Rights Reserved.
 
***************************************************************************************************/
package com.digimarc.disdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

public class DISItemData implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String mSubtitle = "";
//	public Bitmap mBitmap = null;
	public String mPayloadId = "";
	public String mPayOff = "";
//	public String mReportActionToken = "";
	public String mTitle = "";

	@SuppressWarnings("unused")
    private static final int NO_IMAGE = -1;
	private String mStorageFile = null;
	private File mFile = null;
	
	public DISItemData()
	{
	}
	
	public void setFile(String file ) {
		mStorageFile = file;
	}
	
	public boolean serialize() {
		boolean result = true;
		mFile = new File(mStorageFile);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(mFile));
			this.writeObject(oos); // write the class as an 'object'
			oos.flush(); // flush the stream to insure all of the information was written to 'save.bin'
			oos.close();// close the stream

		} catch (FileNotFoundException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean deserialize() {
		boolean result = true;
    	mFile = new File(mStorageFile);
        ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(mFile));
		    this.readObject(ois);
		    ois.close();
		} catch (StreamCorruptedException e) {
			result = false;
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			// EOF Exception is handled here too.  No need to print stack.
			result = false;
		} catch (ClassNotFoundException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	public String toString()
	{
		String result = mSubtitle;
		if((result == null) || (result.isEmpty() == true))
		{
			result = "unknown";
		}
		return(result);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
//        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // write title
        out.writeInt(mTitle.getBytes().length);
        out.write(mTitle.getBytes());
 
		// write description
        out.writeInt(mSubtitle.getBytes().length);
        out.write(mSubtitle.getBytes());
 
        // write payloadId
        out.writeInt(mPayloadId.getBytes().length);
        out.write(mPayloadId.getBytes());
        
        // write payoff
        out.writeInt(mPayOff.getBytes().length);
        out.write(mPayOff.getBytes());
        
        // write report action token
//        out.writeInt(mReportActionToken.getBytes().length);
//        out.write(mReportActionToken.getBytes());
//               
//	    if (mBitmap != null) {
//	        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//	        final byte[] imageByteArray = stream.toByteArray();
//	        out.writeInt(imageByteArray.length);
//	        out.write(imageByteArray);
//	    } else {
//	        out.writeInt(NO_IMAGE);
//	    }
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		
	    int length;
	    byte[] bytes;
	    
        // construct the title
        length = in.readInt();
        if (length > 0) {
            bytes = new byte[length];
            in.readFully(bytes);
            mTitle = new String(bytes);
        }

	    // construct the description
	    length = in.readInt();
	    if (length > 0) {
		    bytes = new byte[length];
		    in.readFully(bytes);
		    mSubtitle = new String(bytes);
	    }

	    // construct the payloadId
	    length = in.readInt();
	    if (length > 0) {
	    	bytes = new byte[length];
	    	in.readFully(bytes);
	    	mPayloadId = new String(bytes); 
	    }

	    // construct the payoff
	    length = in.readInt();
	    if (length > 0) {
	    	bytes = new byte[length];
	    	in.readFully(bytes);
	    	mPayOff = new String(bytes); 
	    }

//	    // construct the payoff
//	    length = in.readInt();
//	    if (length > 0) {
//	    	bytes = new byte[length];
//	    	in.readFully(bytes);
//	    	mReportActionToken = new String(bytes);
//	    }
//	    
//	    // construct the bitmap
//	    length = in.readInt();
//	    if (length != NO_IMAGE) {
//	        final byte[] imageByteArray = new byte[length];
//	        in.readFully(imageByteArray);
//	        mBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, length);
//	    }
	}

    @Override
    public boolean equals( Object o )
    {
        if ( o == null || o.getClass() != DISItemData.class )
            return super.equals( o );
        else
        {
            DISItemData item = (DISItemData) o;

            return ( mPayloadId.compareToIgnoreCase( item.mPayloadId ) == 0 );
        }
    }
}



