package com.sensyscal.activityrecognition2.utils;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

import spine.SPINEFunctionConstants;
import spine.datamodel.Address;
import spine.datamodel.Feature;
import spine.datamodel.Node;


/*****************************************************************
SPINE - Signal Processing In-Node Environment is a framework that 
allows dynamic configuration of feature extraction capabilities 
of WSN nodes via an OtA protocol

Copyright (C) 2007 Telecom Italia S.p.A. 
�
GNU Lesser General Public License
�
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 
�
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.� See the GNU
Lesser General Public License for more details.
�
You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA� 02111-1307, USA.
*****************************************************************/

/**
 * This class contains some utility methods that have been used by the demo application.  
 *
 * @author Raffaele Gravina
 * @author Antonio Guerrieri
 *
 * @version 1.0
 */

@SuppressWarnings("unchecked")
public class Utils {

	public static double[] vectorToDoubleArray(Vector v) {
		 double[] array = new  double[v.size()];
		 for (int i=0; i<v.size(); i++)
			 array[i] = ((Double)v.elementAt(i)).doubleValue();
		 return array;
	}
	
	public static int parseFeatureProperties(String propValue, Feature[] feat) {
		
		StringTokenizer st = new StringTokenizer(propValue, "_", false);
		int nodeId = (new Integer(st.nextToken())).intValue();
		byte sensorCode = (new Byte(st.nextToken())).byteValue();
		byte featureCode = (new Byte(st.nextToken())).byteValue();
		byte axis = (new Byte(st.nextToken())).byteValue();
		
		Feature f = new Feature();
		Node dummyNode = new Node(new Address(""+nodeId));
		dummyNode.setLogicalID(new Address(""+nodeId));
		f.setNode(dummyNode);
		f.setSensorCode(sensorCode);
		f.addChannelToBitmask(axis);
		f.setFeatureCode(featureCode);
		feat[0] = f;
		
		return nodeId;
	}
	
	public static void orderFeatureVector(Vector f) {
		Object[] fTemp = f.toArray();
		
		for (int i = 0; i < fTemp.length - 1; i++)
            for (int j = 0; j < fTemp.length - 1; j++) {            	
            	if ( ((Feature)fTemp[j]).compareTo(((Feature)fTemp[j+1])) > 0) {
            			Feature temp = (Feature)fTemp[j];
            			fTemp[j] = fTemp[j+1];
            			fTemp[j+1] = temp;
 	                }
            	}
		
		f.clear();
		for (int i = 0; i < fTemp.length; i++)
			f.add(fTemp[i]);	
	}
	
	public static void orderNodesVector(Vector nodes) {
		Object[] nTemp = nodes.toArray();
		
		for (int i = 0; i < nTemp.length - 1; i++)
            for (int j = 0; j < nTemp.length - 1; j++) {            	
            	if ( ((Node)nTemp[j]).getPhysicalID().getAsInt() > (((Node)nTemp[j+1])).getPhysicalID().getAsInt() ) {
            			Node temp = (Node)nTemp[j];
            			nTemp[j] = nTemp[j+1];
            			nTemp[j+1] = temp;
 	                }
            	}
		
		nodes.clear();
		for (int i = 0; i < nTemp.length; i++)
			nodes.add(nTemp[i]);	
	}
	
	public static long calculate(byte featurecode, int[] data) {

		switch (featurecode) {
		case SPINEFunctionConstants.MAX:
			return max(data);
		case SPINEFunctionConstants.MIN:
			return min(data);
		case SPINEFunctionConstants.RANGE:
			return range(data);
		case SPINEFunctionConstants.MEAN:
			return mean(data);
		case SPINEFunctionConstants.AMPLITUDE:
			return amplitude(data);
		case SPINEFunctionConstants.RMS:
			return rms(data);
		case SPINEFunctionConstants.ST_DEV:
			return stDev(data);
		case SPINEFunctionConstants.TOTAL_ENERGY:
			return totEnergy(data);
		case SPINEFunctionConstants.VARIANCE:
			return variance(data);
		case SPINEFunctionConstants.MODE:
			return mode(data);
		case SPINEFunctionConstants.MEDIAN:
			return median(data);
		default:
			return 0;
		}

	}

	private static int max(int[] data) {
		int max = data[0];
		for (int i = 1; i < data.length; i++)
			if (data[i] > max)
				max = data[i];
		return max;
	}

	private static int min(int[] data) {
		int min = data[0];
		for (int i = 1; i < data.length; i++)
			if (data[i] < min)
				min = data[i];
		return min;
	}

	private static int range(int[] data) {
		int min = data[0];
		int max = min;
		// we don't use the methods 'max' and 'min';
		// instead, to boost the alg, we can compute both using one single for
		// loop ( O(n) vs O(2n) )
		for (int i = 1; i < data.length; i++) {
			if (data[i] < min)
				min = data[i];
			if (data[i] > max)
				max = data[i];
		}
		return (max - min);
	}

	private static int mean(int[] data) {
		double mean = 0;

		for (int i = 0; i < data.length; i++)
			mean += data[i];

		return (int) (Math.round(mean / data.length));
	}

	private static int amplitude(int[] data) {
		return (max(data) - mean(data));
	}

	private static int rms(int[] data) {
		double rms = 0;
		for (int i = 0; i < data.length; i++)
			rms += (data[i] * data[i]);
		rms /= data.length;
		return (int) Math.round(Math.sqrt(rms));
	}

	private static int variance(int[] data) {
		double var = 0, mu = 0;
		int val = 0;

		for (int i = 0; i < data.length; i++) {
			val = data[i];
			mu += val;
			var += (val * val);
		}

		mu /= data.length;
		var /= data.length;
		var -= (mu * mu);
		return (int) Math.round(var);
	}

	private static int stDev(int[] data) {
		return (int) (Math.round(Math.sqrt(variance(data))));
	}

	private static int mode(int[] data) {
		int iMax = 0;
		int[] orderedData = new int[data.length];
		System.arraycopy(data, 0, orderedData, 0, data.length);
		int[] tmp = new int[data.length];

		// to boost the algorithm, we first sort the array (mergeSort takes
		// O(nlogn))
		Arrays.sort(orderedData);

		int i = 0;
		// now we look for the max number of occurences per each value
		while (i < data.length - 1) {
			for (int j = i + 1; j < data.length; j++)
				if (orderedData[i] == orderedData[j]) {
					tmp[i] = j - i + 1;
					if (j == (data.length - 1))
						i = data.length - 1; // exit condition
				} else {
					i = j;
					break;
				}
		}

		// we choose the overall max
		for (i = 1; i < data.length; i++)
			if (tmp[i] > tmp[iMax])
				iMax = i;

		return orderedData[iMax];
	}

	private static int median(int[] data) {
		int[] sortedData = new int[data.length];

		System.arraycopy(data, 0, sortedData, 0, data.length);
		Arrays.sort(sortedData);

		return (data.length % 2 == 0) ? (sortedData[data.length / 2] + sortedData[(data.length / 2) - 1]) / 2
				: sortedData[(data.length - 1) / 2];
	}

	private static int totEnergy(int[] data) {
		double totEn = 0;

		for (int i = 0; i < data.length; i++)
			totEn += (data[i] * data[i]);

		return (int) (totEn / data.length);
	}

	public static Feature parseFeatureProperties(String propValue) {

		StringTokenizer st = new StringTokenizer(propValue, "_", false);
		int nodeId = (new Integer(st.nextToken())).intValue();
		byte sensorCode = (new Byte(st.nextToken())).byteValue();
		byte featureCode = (new Byte(st.nextToken())).byteValue();
		byte axis = (new Byte(st.nextToken())).byteValue();

		Feature f = new Feature();
		Node dummyNode = new Node(new Address("" + nodeId));
		dummyNode.setLogicalID(new Address("" + nodeId));
		f.setNode(dummyNode);
		f.setSensorCode(sensorCode);
		f.addChannelToBitmask(axis);
		f.setFeatureCode(featureCode);

		return f;
	}

	
}
