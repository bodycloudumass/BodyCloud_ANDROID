package com.sensyscal.activityrecognition2.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;
import com.sensyscalactivityrecognition2.worker.custom.WekaWorker;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;


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
 * Loads a training set and optionally a test set from a given txt file containing the features tuples.
 *
 * @author Raffaele Gravina
 * @author Antonio Guerrieri
 *
 * @version 1.0
 */

@SuppressWarnings("unchecked")
public class TrainingSetLoaderFromSd {
	
	private Sample[] trainingSet;
	private Sample[] testSet;
	private Vector columnLabels = new Vector();
	
	private static Activity a;
	
	
	/**
	 * Constructor for a TrainingSetLoader object.
	 * 
	 * @param pathName the source txt file containing the tuples
	 * @param columnsToLoad Vector containing the list of the columns to be inserted in the training set
	 * @param columnsSpecifiedByName specifies if the Vector contains the names of the columns or directly their indexes
	 * @param loadTestSet specify whether the test set has to be loaded or not.
	 * 		  if <code>true</code> the source file will be split alternatively in two generating both the
	 * 		  training set and the test set	
	 * @throws FileNotFoundException 
	 */
	public TrainingSetLoaderFromSd(Activity activity, String pathName, Vector columnsToLoad, boolean columnsSpecifiedByName, boolean loadTestSet) throws FileNotFoundException {
		a = activity;
		Log.e("TrainingsetLoaderFromSd", "TrainingsetLoaderFromSd");
		load(pathName, columnsToLoad, columnsSpecifiedByName, loadTestSet);
	}
	
	/**
	 * Constructor for a TrainingSetLoader object. This constructor will load the whole file.
	 * 
	 * @param pathName the source txt file containing the tuples
	 * @param loadTestSet specify whether the test set has to be loaded or not.
	 * 		  if <code>true</code> the source file will be split alternatively in two generating both the
	 * 		  training set and the test set	
	 * @param hasColumnLabels true if the training-set file has the column labels in the first row 
	 * @throws FileNotFoundException 
	 */
	public TrainingSetLoaderFromSd(String pathName, boolean loadTestSet, boolean hasColumnLabels) throws FileNotFoundException {
		load(pathName, loadTestSet, hasColumnLabels);
	}
	
	/**
	 * Private method that actually loads the training set and the test set	
	 * 
	 * @param pathName the source txt file containing the tuples
	 * @param columnsToLoad Vector containing the list of the columns to be inserted in the training set
	 * @param columnsSpecifiedByName specifies if the Vector contains the names of the columns or directly their indexes
	 * @param loadTestSet specify whether the test set has to be loaded or not.
	 * 		  if <code>true</code> the source file will be split alternatively in two generating both the
	 * 		  training set and the test set	
	 */
	private void load(String pathName, Vector columnsToLoad, boolean columnsSpecifiedByName, boolean loadTestSet) throws FileNotFoundException {
		File file =  new File(Environment.getExternalStorageDirectory(),pathName);
		
		if (columnsToLoad == null || columnsToLoad.size()==0)
			return;	
		
		Vector columnsPosition = new Vector();
		
		if (columnsSpecifiedByName) {
			
			Vector tSColumnsNames = new Vector();
			BufferedReader br = null;
			try {
				//Resources resources = a.getResources();
				//AssetManager assetManager = resources.getAssets();
				br = new BufferedReader(new FileReader(file));
			
			} catch(IOException e){Log.e("scs", "io eccezione");}
			try {	
				String firstLine = br.readLine();
				if (firstLine != null) {				
					StringTokenizer st = new StringTokenizer(firstLine, " ;", false);
					while (st.hasMoreTokens()) {
						tSColumnsNames.addElement(st.nextToken());
					}
				}			
				br.close();
			} catch (IOException e) {
				Log.e("scs", "seconda eccezione");
			}
			
			for (int i = 0; i<columnsToLoad.size(); i++) 
				for (int j = 0; j<tSColumnsNames.size(); j++) 
					if ( ((String)columnsToLoad.elementAt(i)).equals((String)tSColumnsNames.elementAt(j)) ) {
						columnsPosition.addElement(new Integer(j));
						break;
					}
		}
		else {
			columnsPosition = columnsToLoad; // into 'columnsToLoad' there are already the positions (indexes)
		}
		
		if (columnsPosition.size() == 0)
			return;
		
		BufferedReader br = null;
		try {
			//Resources resources = a.getResources();
			//AssetManager assetManager = resources.getAssets();
			br = new BufferedReader(new FileReader(file));
		} catch (IOException e1) {}
		String newLine;
		
		int length = 0;
		try {
			
			newLine = br.readLine();
			while (newLine != null) {
				 if(Character.isDigit(newLine.charAt(0))) 				 	
				 	length++;					
				 newLine = br.readLine();					
			}
		} catch (IOException e) {	e.printStackTrace(); }
		  
		if (loadTestSet) {
			trainingSet = new Sample[length/2];
			testSet = new Sample[length/2];
		}
		else 
			trainingSet = new Sample[length];
		
		
		//Resources resources = a.getResources();
		//AssetManager assetManager = resources.getAssets();
		InputStreamReader inputStream = null;
		try {
			inputStream = new InputStreamReader(new FileInputStream(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		br = new BufferedReader(inputStream);
		try {
			int trainIndex = 0;
			int testIndex = 0;
			
			newLine = br.readLine();
			boolean nextTupleInTestSet = false;
			while (newLine != null) {
				try {
					double[] sTemp = new double[columnsPosition.size()];
					String classTemp = null;						
					for (int i = 0; i < columnsPosition.size(); i++) {
						StringTokenizer st = new StringTokenizer(newLine, " ;", false);
						int posCurr = 0;
						String curr = null;
						while (st.hasMoreTokens()
								&& posCurr <= ((Integer)columnsPosition.get(i)).intValue()) {
							curr = st.nextToken();
							if (posCurr == 0 && classTemp == null)
								classTemp = curr;
							posCurr++;
						}
						sTemp[i] = Double.parseDouble(curr);
						
					}
					if (!nextTupleInTestSet) { // here we insert the current parsed tuple in the Training Set
						nextTupleInTestSet = loadTestSet;
						trainingSet[trainIndex] = new Sample(sTemp, Integer.parseInt(classTemp));
						trainIndex++;
					}
					else { // here we insert the current parsed tuple in the Test Set
						nextTupleInTestSet = false;
						testSet[testIndex] = new Sample(sTemp, Integer.parseInt(classTemp));
						testIndex++;
					}
				} catch (NumberFormatException e) {
					// if the current row contains non-numeric values (i.e. the columnLabels row), simply skip that row
				}
				
				newLine = br.readLine();
			}				
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Private method that actually loads the whole training set and the test set	
	 * 
	 * @param pathName the source txt file containing the tuples
	 * @param loadTestSet specify whether the test set has to be loaded or not.
	 * 		  if <code>true</code> the source file will be split alternatively in two generating both the
	 * 		  training set and the test set
	 * @param hasColumnLabels true if the training-set file has the column labels in the first row	
	 * @throws FileNotFoundException 
	 */
	private void load(String pathName, boolean loadTestSet, boolean hasColumnLabels) throws FileNotFoundException {
		//BufferedReader br = new BufferedReader(new FileReader(pathName));
		BufferedReader br = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory(),pathName)));
		String newLine;
		
		int length = 0;
		try {
			
			
			if (hasColumnLabels) {
				newLine = br.readLine();
				StringTokenizer st = new StringTokenizer(newLine, " ;", false);
				if (st.hasMoreTokens()) // the first label is the ClassLabel caption, so it is useless 
					st.nextToken();
				while (st.hasMoreTokens()) {
					columnLabels.add(st.nextToken());
				}
			}
			
			newLine = br.readLine();
			while (newLine != null) {
				 if(Character.isDigit(newLine.charAt(0))) 				 	
				 	length++;					
				 newLine = br.readLine();					
			}
		} catch (IOException e) {	e.printStackTrace(); }
		  
		if (loadTestSet) {
			trainingSet = new Sample[length/2];
			testSet = new Sample[length/2];
		}
		else 
			trainingSet = new Sample[length];
		
		br = new BufferedReader(new FileReader(pathName));
		
		try {
			int trainIndex = 0;
			int testIndex = 0;
			
			boolean nextTupleInTestSet = false;
			
			
			newLine = br.readLine();
			while (newLine != null) {
				try {
					//double[] sTemp = new double[featureColumnPosition.size()];
					Vector sTemp = new Vector();
					String classTemp = null;						
					StringTokenizer st = new StringTokenizer(newLine, " ;", false);
					String curr = null;
					while (st.hasMoreTokens()) {
						curr = st.nextToken();
						if (classTemp == null)
							classTemp = curr;
						else
							sTemp.add(new Double(curr));
					}
					
					if (!nextTupleInTestSet) { // here we insert the current parsed tuple in the Training Set
						nextTupleInTestSet = loadTestSet;
						trainingSet[trainIndex] = new Sample(Utils.vectorToDoubleArray(sTemp), Integer.parseInt(classTemp));
						trainIndex++;
					}
					else { // here we insert the current parsed tuple in the Test Set
						nextTupleInTestSet = false;
						testSet[testIndex] = new Sample(Utils.vectorToDoubleArray(sTemp), Integer.parseInt(classTemp));
						testIndex++;
					}
				} catch (NumberFormatException e) {
					// if the current row contains non-numeric values (i.e. the columnLabels row), simply skip that row
				}
				
				newLine = br.readLine();
			}				
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Returns the loaded training set
	 * @return the loaded training set
	 */
	public Sample[] getTrainingSet() {
		return trainingSet;
	}

	/**
	 * Returns the loaded test set
	 * @return the loaded test set
	 */
	public Sample[] getTestSet() {
		return testSet;
	}

	/**
	 * Returns the loaded column labels
	 * @return the loaded column labels
	 */
	public Vector getColumnLabels() {
		return columnLabels;
	}
}

