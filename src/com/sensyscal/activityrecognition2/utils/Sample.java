package com.sensyscal.activityrecognition2.utils;

import java.util.Arrays;

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
 * This class represents a tuple of the dataset used in the Classifiers methods
 *
 * @author Raffaele Gravina
 * @author Antonio Guerrieri
 *
 * @version 1.0
 */
public class Sample {
	private double[] features;
	
	private int membershipClass;
	
	/**
	 * Constructor for a Sample object
	 * 
	 * @param features the set of coordinates of the Sample object. 
	 * 					 The size of this array define the dimension of the sample
	 * @param membershipClass the membership class code of the sample
	 * @see apps.demo.logic.ClassesCodes for details about the membership class codes 
	 */
	public Sample (double[] features, int membershipClass) {
		this.features = features;
		this.membershipClass = membershipClass;
	}
	
	/**
	 * Constructor for a Sample object belonging from a live sensor reading
	 * 
	 * @param features the set of coordinates of the Sample object. 
	 * 					 The size of this array define the dimension of the sample
	 * @see apps.demo.logic.ClassesCodes for details about the membership class codes 
	 */
	public Sample (double[] features) {
		this.features = features;
	}
	
	/**
	 * 
	 * @return the set of coordinates of the sample
	 */
	public double[] getFeatures() {
		return features;
	}
	
	/**
	 * 
	 * @return the membership class code of the sample
	 * @see apps.demo.logic.ClassesCodes for details
	 */
	public int getMembershipClass() {
		return membershipClass;
	}
	
	public String toString() {
		return "features: "+Arrays.toString(features)+" - class:"+membershipClass;
	}
	
}
