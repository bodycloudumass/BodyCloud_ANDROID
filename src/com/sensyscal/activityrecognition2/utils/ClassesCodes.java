package com.sensyscal.activityrecognition2.utils;

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
 *
 * This class contains the membership class codes and their text caption. 
 * Moreover it is provided a method to obtain the caption corresponding to a given class code. 
 *
 * @author Raffaele Gravina
 * @author Antonio Guerrieri
 *
 * @version 1.0
 */
public class ClassesCodes {
	
	public final static int STANDING = 0;
		
		public final static int STANDING_STILL = 1;
		
		public final static int STANDING_STILL_MOVING = 2;
		
		public final static int STANDING_WALKING = 3;

		public final static int TRANSITION_STANDING_SITTING = 100;
		
	
	public final static int SITTING = 10;
		
		public final static int SITTING_WORKING = 11;
		
		public final static int SITTING_RELAXED = 12;
		
	
	public final static int LYING = 20;
		
		public final static int LYING_SUPINE = 21;
		
		public final static int LYING_PRONE = 22;
		
		public final static int LYING_SIDE_L = 23;
		
		public final static int LYING_SIDE_R = 24;

	
	public final static String STANDING_CAPTION = "Standing";
		
		public final static String STANDING_STILL_CAPTION = "Standing Still";
		
		public final static String STANDING_STILL_MOVING_CAPTION = "Standing Still Moving";
		
		public final static String STANDING_WALKING_CAPTION = "Walking";
	
	public final static String SITTING_CAPTION = "Sitting";
		
		public final static String SITTING_WORKING_CAPTION = "Sitting Working";
		
		public final static String SITTING_RELAXED_CAPTION = "Sitting Relaxed";
	
	public final static String LYING_CAPTION = "Lying";
		
		public final static String LYING_SUPINE_CAPTION = "Lying Supine";
		
		public final static String LYING_PRONE_CAPTION = "Lying Prone";
		
		public final static String LYING_SIDE_L_CAPTION = "Lying on Left Side";
		
		public final static String LYING_SIDE_R_CAPTION = "Lying on Right Side";
	
	
	public final static int UNKNOWN_CLASS = -1;
		
	public final static String UNKNOWN_CLASS_CAPTION = "Unknown Class";
		
	/**
	 * This utility method is used to obtain the text caption corresponding 
	 * to a given class code.
	 * 
	 * @param classCode the membership class code to translate
	 * @return the text caption of the given class code
	 */
	public static String getClassCaption(int classCode) {
		
		switch (classCode) {
			case STANDING : return STANDING_CAPTION;
			case STANDING_STILL : return STANDING_STILL_CAPTION;
			case STANDING_STILL_MOVING : return STANDING_STILL_MOVING_CAPTION;
			case STANDING_WALKING : return STANDING_WALKING_CAPTION;
			case SITTING : return SITTING_CAPTION;
			case SITTING_WORKING : return SITTING_WORKING_CAPTION;
			case SITTING_RELAXED : return SITTING_RELAXED_CAPTION;
			case LYING : return LYING_CAPTION;
			case LYING_SUPINE : return LYING_SUPINE_CAPTION;
			case LYING_PRONE : return LYING_PRONE_CAPTION;
			case LYING_SIDE_L : return LYING_SIDE_L_CAPTION;
			case LYING_SIDE_R : return LYING_SIDE_R_CAPTION;
			default: return UNKNOWN_CLASS_CAPTION;
		}
		
	}
	
}
