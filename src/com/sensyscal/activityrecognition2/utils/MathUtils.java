package com.sensyscal.activityrecognition2.utils;

import java.util.Vector;

/*****************************************************************
SPINE - Signal Processing In-Node Environment is a framework that 
allows dynamic configuration of feature extraction capabilities 
of WSN nodes via an OtA protocol

Copyright (C) 2007 Telecom Italia S.p.A. 
 
GNU Lesser General Public License
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 
 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

/**
 * This class contains several useful math functions
 *
 * @author Raffaele Gravina
 * @author Antonio Guerrieri
 *
 * @version 1.0
 */

@SuppressWarnings("unchecked")
public class MathUtils {
	
	/**
	 * Computes the so called 'manhattan' distance between to n-dimensional given points
	 * 
	 * @param point1 the first point
	 * @param point2 the second point
	 * 
	 * @return the evaluated 'manhattan' distance
	 */
	public static double manhattanDistance(double[] point1, double[] point2) {
		double distance = 0;
		
		int dimension = point1.length;
		if (dimension != point2.length) 
			return -1;
		
		for (int i = 0; i<dimension; i++) 
			distance += Math.abs(point1[i]-point2[i]);
		
		return distance;
	}
	
	/**
	 * Computes the so called 'euclidean' distance between to n-dimensional given points
	 * 
	 * @param point1 the first point
	 * @param point2 the second point
	 * 
	 * @return the evaluated 'euclidean' distance
	 */
	public static double euclideanDistance(double[] point1, double[] point2) {
		double distance = 0;
		
		int dimension = point1.length;
		if (dimension != point2.length) 
			return -1;
		
		for (int i = 0; i<dimension; i++) 
			distance += Math.pow(point1[i]-point2[i], 2);
		
		return Math.sqrt(distance);
	}

	/**
	 * ASC Sorts Vector of Short or Integer objects
	 * 
	 * @param v the vector to be sorted
	 */
	public static void sortVector(Vector v) {
		Object[] v2 = v.toArray();
		
		for (int i = 0; i < v2.length - 1; i++)
            for (int j = 0; j < v2.length - 1; j++) {
            	
            	if (v2[j] instanceof Short) {            	
	                if (((Short)v2[j]).shortValue() > ((Short)v2[j+1]).shortValue() ) {
	                   Short temp = (Short)v2[j];
	                   v2[j] = v2[j+1];
	                   v2[j+1] = temp;
	                }
            	}
            	else if (v2[j] instanceof Integer) {
            		if (((Integer)v2[j]).intValue() > ((Integer)v2[j+1]).intValue() ) {
            			Integer temp = (Integer)v2[j];
 	                   v2[j] = v2[j+1];
 	                   v2[j+1] = temp;
 	                }
            	}
            }
		
		v.clear();
		for (int i = 0; i < v2.length; i++)
			v.add(v2[i]);		
	}
}
