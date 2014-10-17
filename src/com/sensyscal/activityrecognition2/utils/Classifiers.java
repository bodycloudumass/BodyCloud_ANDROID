package com.sensyscal.activityrecognition2.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import android.util.Log;

import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;

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
 * This class contains classifiers algorithms
 * 
 * @author Raffaele Gravina
 * @author Antonio Guerrieri
 * 
 * @version 1.3
 */
public class Classifiers {

	static Timestamp ts, ts1;

	/**
	 * This method implements the KNN (k-nearest neighbor) classifier algorithm
	 * 
	 * @param trainingSet
	 *            the training set to perform the classification
	 * @param tupleToClassify
	 *            the new reading to be classified
	 * @param k
	 *            the number of neighbor for this KNN estimation
	 * @see apps.demo.logic.ClassesCodes for details about the class codes
	 * 
	 * @return the estimated membership class code
	 */
	public static int knn(Sample[] trainingSet, Sample tupleToClassify, int k) {

		double[] distances = new double[trainingSet.length];
		int[] classes = new int[trainingSet.length];

		for (int i = 0; i < trainingSet.length; i++) {
			distances[i] = computeDistance(trainingSet[i], tupleToClassify);
			classes[i] = trainingSet[i].getMembershipClass();
		}

		sort(distances, classes);

		Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>();
		Integer temp;
		for (int i = 0; i < k; i++) {
			temp = (Integer) ht.get(new Integer(classes[i]));
			if (temp != null)
				ht.put(new Integer(classes[i]), new Integer(
						(temp.intValue() + 1)));
			else
				ht.put(new Integer(classes[i]), new Integer(1));
		}

		Iterator<Integer> i = ht.keySet().iterator();
		int max = 0;
		int maxClass = -1;
		Integer key;
		int value = 0;
		while (i.hasNext()) {
			key = (Integer) i.next();
			value = ((Integer) ht.get(key)).intValue();
			if (value > max) {
				max = value;
				maxClass = key.intValue();
			}
		}

		return maxClass;
	}

	/**
	 * This method implements the KNN (k-nearest neighbor) classifier algorithm
	 * 
	 * @param trainingSet
	 *            the training set to perform the classification
	 * @param tupleToClassify
	 *            the new reading to be classified (you can use the hashtable
	 *            passed to the listener method 'superFrameReceived')
	 * @param k
	 *            the number of neighbor for this KNN estimation
	 * @see apps.demo.logic.ClassesCodes for details about the class codes
	 * 
	 * @return the estimated membership class code
	 */
	public static int knn(Sample[] trainingSet, double[] tupleToClassify, int k) {
		return knn(trainingSet, new Sample(tupleToClassify), k);
	}

	/**
	 * Sorts the given 'distances' array and then reorder the 'classes' array
	 * according to the sort process on the 'distances' array
	 * 
	 * @param distances
	 *            the array containing the distance between the point to be
	 *            classified and every tuple in the training set
	 * @param classes
	 *            the membership class of the training set tuple of the
	 *            corresponding distance
	 */
	private static void sort(double[] distances, int[] classes) {
		double tempDist;
		int tempClass;
		for (int i = 0; i < distances.length - 1; i++)
			for (int j = 0; j < distances.length - 1; j++) {
				if (distances[j] > distances[j + 1]) {
					tempDist = distances[j];
					distances[j] = distances[j + 1];
					distances[j + 1] = tempDist;

					tempClass = classes[j];
					classes[j] = classes[j + 1];
					classes[j + 1] = tempClass;
				}
			}
	}

	/**
	 * Computes the distance between to given n-dimensional tuples
	 * 
	 * @param sample1
	 *            the first tuple
	 * @param sample2
	 *            the second tuple
	 * @return the distance between to given tuples
	 */
	private static double computeDistance(Sample sample1, Sample sample2) {
		// return MathUtils.euclideanDistance((double[])(sample1.getFeatures()),
		// (double[])(sample2.getFeatures()));
		return MathUtils.manhattanDistance((double[]) (sample1.getFeatures()),
				(double[]) (sample2.getFeatures()));
	}

	public static int customKnnClassifier(double[] newInstanceArray) {
		// TODO Auto-generated method stub
		ts1 = new Timestamp(System.currentTimeMillis());
		int activityId = 0;
		String classLabel = "";
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add("STANDING");
		classVal.add("SITTING");
		classVal.add("LYINGDOWN");
		classVal.add("WALKING");
		atts.add(new Attribute("class", classVal));
		atts.add(new Attribute("1_1_2_1"));
		atts.add(new Attribute("1_1_3_1"));
		atts.add(new Attribute("1_1_9_2"));
		atts.add(new Attribute("2_1_3_1"));
		atts.add(new Attribute("2_1_4_1"));
		atts.add(new Attribute("2_1_9_2"));
		Instances dataUnlabeled = new Instances("TestInstances", atts, 0);
		dataUnlabeled.add(new DenseInstance(1.0, newInstanceArray));
		dataUnlabeled.setClassIndex(0);
		try {
			activityId = (int) (MonitoringWorkerThread.cls
					.classifyInstance(dataUnlabeled.firstInstance()));
			classLabel = dataUnlabeled.firstInstance().classAttribute()
					.value(activityId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ts = new Timestamp(System.currentTimeMillis());
		// Log.e("classifyActivity Knn," -> Impiegati:
		// "+(ts.getTime()-ts1.getTime())+" ms;\n");
		return getActivityIDofClassLabel(classLabel);
	}

	// Classes Codes : STANDING=0 SITTING=10 LYING=20 WALKING=3
	public static int jrip(double[] newInstanceArray) {
		// TODO Auto-generated method stub
		double max1_1 = newInstanceArray[0], min1_1 = newInstanceArray[1], total_energy1_2 = newInstanceArray[2], min2_1 = newInstanceArray[3], range2_1 = newInstanceArray[4], total_energy2_2 = newInstanceArray[5];

		if ((total_energy2_2 <= 6811202) && (min1_1 >= 2213)
				&& (min1_1 >= 2312))
			return 20;

		if (min2_1 >= 2664)
			return 20;

		if ((total_energy2_2 <= 6821741) && (total_energy2_2 >= 4977414)
				&& (total_energy2_2 <= 6087822) && (min1_1 >= 2159))
			return 20;

		if ((total_energy2_2 <= 7118509) && (range2_1 >= 48)
				&& (range2_1 <= 478) && (total_energy2_2 <= 6810302)
				&& (total_energy1_2 <= 5972668) && (min2_1 <= 2336))
			return 20;

		if ((max1_1 >= 2346) && (min1_1 <= 1792) && (max1_1 <= 2498))
			return 20;

		if ((max1_1 >= 2346) && (min2_1 >= 2487) && (range2_1 >= 48))
			return 20;

		if ((total_energy2_2 <= 7127331) && (range2_1 >= 133)
				&& (total_energy1_2 >= 5637360) && (total_energy1_2 <= 5972668)
				&& (total_energy2_2 <= 7006551))
			return 20;

		if ((total_energy1_2 >= 5926101) && (total_energy1_2 <= 6146225)
				&& (max1_1 >= 2216) && (min1_1 >= 2191)
				&& (total_energy1_2 <= 6125779) && (total_energy1_2 >= 6085520))
			return 20;

		if ((min1_1 <= 2160) && (total_energy2_2 >= 7170917))
			return 3;

		if ((range2_1 >= 101) && (min1_1 <= 2164) && (range2_1 >= 409))
			return 3;

		if ((range2_1 >= 86) && (min1_1 <= 2164)
				&& (total_energy1_2 <= 5273807) && (min1_1 >= 2042))
			return 3;

		if ((range2_1 >= 115) && (total_energy1_2 <= 5307877)
				&& (min1_1 <= 2188) && (total_energy2_2 >= 7155061))
			return 3;

		if ((range2_1 >= 53) && (total_energy1_2 <= 5365868)
				&& (min1_1 <= 2176) && (max1_1 <= 2344))
			return 3;

		if ((range2_1 >= 37) && (total_energy1_2 <= 5964536)
				&& (max1_1 >= 2227) && (min1_1 <= 2186) && (min1_1 >= 2058)
				&& (min2_1 <= 2380))
			return 3;

		if ((range2_1 >= 32) && (total_energy1_2 <= 5253362)
				&& (max1_1 >= 2262) && (total_energy1_2 >= 4945524))
			return 3;

		if ((range2_1 >= 53) && (total_energy1_2 <= 5792763)
				&& (total_energy2_2 >= 7141680) && (total_energy1_2 >= 5513987)
				&& (min1_1 <= 2160))
			return 3;

		if ((range2_1 >= 33) && (total_energy1_2 <= 5359073)
				&& (min1_1 <= 2208) && (max1_1 >= 2234)
				&& (total_energy2_2 >= 7129773))
			return 3;

		if ((min1_1 <= 2173) && (total_energy1_2 <= 5213813)
				&& (max1_1 <= 2270) && (min2_1 >= 2479))
			return 3;

		if ((total_energy1_2 >= 5754471) && (total_energy1_2 >= 5909530)
				&& (min1_1 >= 2181))
			return 10;

		if ((total_energy1_2 >= 5733508) && (total_energy2_2 >= 7130388)
				&& (range2_1 <= 52))
			return 10;

		if ((max1_1 >= 2248) && (total_energy2_2 >= 7137201)
				&& (min1_1 >= 2232))
			return 10;

		if ((max1_1 >= 2245) && (min2_1 <= 2477) && (range2_1 <= 26)
				&& (min1_1 >= 2217))
			return 10;

		if ((max1_1 >= 2244) && (total_energy1_2 >= 5886578))
			return 10;

		if ((max1_1 >= 2242) && (total_energy2_2 >= 7138268)
				&& (min2_1 <= 2481) && (min1_1 >= 2209)
				&& (total_energy2_2 >= 7140947))
			return 10;

		if ((total_energy2_2 >= 7132803) && (max1_1 >= 2242)
				&& (min2_1 <= 2470) && (min1_1 >= 2203))
			return 10;

		if ((total_energy1_2 >= 5918362) && (total_energy1_2 >= 5984709))
			return 10;

		if ((total_energy2_2 >= 7133599) && (total_energy1_2 >= 5567395)
				&& (total_energy2_2 >= 7138406) && (range2_1 <= 20))
			return 10;

		if ((total_energy2_2 >= 7132803) && (max1_1 >= 2235)
				&& (min2_1 <= 2480) && (range2_1 <= 22))
			return 10;

		if ((total_energy2_2 >= 7132807) && (total_energy2_2 <= 7139868)
				&& (total_energy1_2 >= 5316745) && (max1_1 >= 2200)
				&& (min1_1 >= 2168) && (max1_1 >= 2202)
				&& (total_energy1_2 >= 5381373) && (total_energy1_2 <= 5453195))
			return 10;

		if ((total_energy2_2 >= 7136272) && (total_energy1_2 >= 5583143)
				&& (min2_1 <= 2479) && (total_energy1_2 >= 5627238))
			return 10;

		if ((max1_1 >= 2242) && (range2_1 >= 52) && (max1_1 <= 2276))
			return 10;

		return 0;
	}

	public static int customJRipClassifier(double[] newInstanceArray) {
		// TODO Auto-generated method stub
		ts1 = new Timestamp(System.currentTimeMillis());
		int activityId = 0;
		String classLabel = "";
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add("STANDING");
		classVal.add("WALKING");
		classVal.add("SITTING");
		classVal.add("LYINGDOWN");
		atts.add(new Attribute("class", classVal));
		atts.add(new Attribute("1_1_2_1"));
		atts.add(new Attribute("1_1_3_1"));
		atts.add(new Attribute("1_1_9_2"));
		atts.add(new Attribute("2_1_3_1"));
		atts.add(new Attribute("2_1_4_1"));
		atts.add(new Attribute("2_1_9_2"));

		Instances dataUnlabeled = new Instances("TestInstances", atts, 0);
		dataUnlabeled.add(new DenseInstance(1.0, newInstanceArray));
		dataUnlabeled.setClassIndex(0);
		try {
			activityId = (int) MonitoringWorkerThread.cls
					.classifyInstance(dataUnlabeled.firstInstance());
			Log.i("classifyActivity JRip ---->", activityId + "");
			classLabel = dataUnlabeled.firstInstance().classAttribute()
					.value((int) activityId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ts = new Timestamp(System.currentTimeMillis());
		// Log.i("classifyActivity JRip"," -> Impiegati: "+(ts.getTime()-ts1.getTime())+" ms;\n");
		return getActivityIDofClassLabel(classLabel);
	}

	public static int getActivityIDofClassLabel(String classLabel) {
		// TODO Auto-generated method stub
		if (classLabel.equals("STANDING"))
			return 0;
		if (classLabel.equals("SITTING"))
			return 10;
		if (classLabel.equals("LYINGDOWN"))
			return 20;
		if (classLabel.equals("WALKING"))
			return 3;
		return 0;
	}

	public static int customJ48Classifier(double[] newInstanceArray) {
		// TODO Auto-generated method stub
		ts1 = new Timestamp(System.currentTimeMillis());
		int activityId = 0;
		String classLabel = "";
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add("STANDING");
		classVal.add("SITTING");
		classVal.add("LYINGDOWN");
		classVal.add("WALKING");
		atts.add(new Attribute("class", classVal));
		atts.add(new Attribute("1_1_2_1"));
		atts.add(new Attribute("1_1_3_1"));
		atts.add(new Attribute("1_1_9_2"));
		atts.add(new Attribute("2_1_3_1"));
		atts.add(new Attribute("2_1_4_1"));
		atts.add(new Attribute("2_1_9_2"));
		Instances dataUnlabeled = new Instances("TestInstances", atts, 0);
		dataUnlabeled.add(new DenseInstance(1.0, newInstanceArray));
		dataUnlabeled.setClassIndex(0);
		try {
			activityId = (int) getJ48ActivityId(MonitoringWorkerThread.cls
					.classifyInstance(dataUnlabeled.firstInstance()));
			classLabel = dataUnlabeled.firstInstance().classAttribute()
					.value((int) activityId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ts = new Timestamp(System.currentTimeMillis());
		// Log.e("classifyActivity J48"," -> Impiegati: "+(ts.getTime()-ts1.getTime())+" ms;\n");
		return activityId;// getActivityIDofClassLabel(classLabel);
	}

	// l'idActivity del j48 di weka non corrisponde all'activityId usata
	// nell'app
	private static int getJ48ActivityId(double id) {
		// TODO Auto-generated method stub
		if (id == 0)
			return 10;
		if (id == 1)
			return 0;
		if (id == 2)
			return 3;
		if (id == 3)
			return 20;

		return -1;
	}

	static J48 j48 = new J48();

	public static int j48(Object[] newInstanceArray) {
		// TODO Auto-generated method stub
		return (int) getJ48ActivityId(j48.classify(newInstanceArray));
	}

	static class J48 {

		public double classify(Object[] i) {

			double p = Double.NaN;
			p = J48.N11137080(i);
			return p;
		}

		static double N11137080(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2160.0) {
				p = J48.N133f1d71(i);
			} else if (((Double) i[1]).doubleValue() > 2160.0) {
				p = J48.N94948a50(i);
			}
			return p;
		}

		static double N133f1d71(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 85.0) {
				p = J48.N14a99722(i);
			} else if (((Double) i[4]).doubleValue() > 85.0) {
				p = J48.N7fdcde21(i);
			}
			return p;
		}

		static double N14a99722(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5873590.0) {
				p = J48.Na013353(i);
			} else if (((Double) i[2]).doubleValue() > 5873590.0) {
				p = J48.N171732b19(i);
			}
			return p;
		}

		static double Na013353(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 2;
			} else if (((Double) i[2]).doubleValue() <= 5279032.0) {
				p = J48.N14d33434(i);
			} else if (((Double) i[2]).doubleValue() > 5279032.0) {
				p = J48.Nf818438(i);
			}
			return p;
		}

		static double N14d33434(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 25.0) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() > 25.0) {
				p = J48.N1608e055(i);
			}
			return p;
		}

		static double N1608e055(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 2;
			} else if (((Double) i[2]).doubleValue() <= 5114286.0) {
				p = J48.Nbf32c6(i);
			} else if (((Double) i[2]).doubleValue() > 5114286.0) {
				p = 2;
			}
			return p;
		}

		static double Nbf32c6(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2466.0) {
				p = J48.N89fbe37(i);
			} else if (((Double) i[3]).doubleValue() > 2466.0) {
				p = 2;
			}
			return p;
		}

		static double N89fbe37(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2133.0) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() > 2133.0) {
				p = 1;
			}
			return p;
		}

		static double Nf818438(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 2;
			} else if (((Double) i[3]).doubleValue() <= 2450.0) {
				p = J48.Ndd5b9(i);
			} else if (((Double) i[3]).doubleValue() > 2450.0) {
				p = J48.Nc4bcdc10(i);
			}
			return p;
		}

		static double Ndd5b9(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7130281.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7130281.0) {
				p = 2;
			}
			return p;
		}

		static double Nc4bcdc10(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2130.0) {
				p = J48.N4b433311(i);
			} else if (((Double) i[1]).doubleValue() > 2130.0) {
				p = J48.N1cafa9e17(i);
			}
			return p;
		}

		static double N4b433311(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2474.0) {
				p = J48.N128e20a12(i);
			} else if (((Double) i[3]).doubleValue() > 2474.0) {
				p = J48.Nb4d3d515(i);
			}
			return p;
		}

		static double N128e20a12(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() <= 5567428.0) {
				p = J48.N1100d7a13(i);
			} else if (((Double) i[2]).doubleValue() > 5567428.0) {
				p = 0;
			}
			return p;
		}

		static double N1100d7a13(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 3;
			} else if (((Double) i[5]).doubleValue() <= 7137752.0) {
				p = 3;
			} else if (((Double) i[5]).doubleValue() > 7137752.0) {
				p = J48.Ne4f97214(i);
			}
			return p;
		}

		static double Ne4f97214(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 38.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 38.0) {
				p = 1;
			}
			return p;
		}

		static double Nb4d3d515(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 27.0) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() > 27.0) {
				p = J48.N1bf52a516(i);
			}
			return p;
		}

		static double N1bf52a516(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2479.0) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() > 2479.0) {
				p = 0;
			}
			return p;
		}

		static double N1cafa9e17(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 18.0) {
				p = J48.N10b9d0418(i);
			} else if (((Double) i[4]).doubleValue() > 18.0) {
				p = 1;
			}
			return p;
		}

		static double N10b9d0418(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2225.0) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() > 2225.0) {
				p = 1;
			}
			return p;
		}

		static double N171732b19(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2157.0) {
				p = J48.N140453620(i);
			} else if (((Double) i[1]).doubleValue() > 2157.0) {
				p = 3;
			}
			return p;
		}

		static double N140453620(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() <= 7165830.0) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() > 7165830.0) {
				p = 2;
			}
			return p;
		}

		static double N7fdcde21(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 2;
			} else if (((Double) i[2]).doubleValue() <= 5948587.0) {
				p = J48.N7d848322(i);
			} else if (((Double) i[2]).doubleValue() > 5948587.0) {
				p = J48.N1bf677048(i);
			}
			return p;
		}

		static double N7d848322(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2222.0) {
				p = J48.N86f24123(i);
			} else if (((Double) i[0]).doubleValue() > 2222.0) {
				p = J48.N1a2961b29(i);
			}
			return p;
		}

		static double N86f24123(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7190106.0) {
				p = J48.N18ac73824(i);
			} else if (((Double) i[5]).doubleValue() > 7190106.0) {
				p = 2;
			}
			return p;
		}

		static double N18ac73824(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5645533.0) {
				p = J48.N1d609625(i);
			} else if (((Double) i[2]).doubleValue() > 5645533.0) {
				p = J48.Nbb6ab627(i);
			}
			return p;
		}

		static double N1d609625(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2396.0) {
				p = J48.Nb02e7a26(i);
			} else if (((Double) i[3]).doubleValue() > 2396.0) {
				p = 1;
			}
			return p;
		}

		static double Nb02e7a26(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2333.0) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() > 2333.0) {
				p = 2;
			}
			return p;
		}

		static double Nbb6ab627(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7140564.0) {
				p = J48.N5afd2928(i);
			} else if (((Double) i[5]).doubleValue() > 7140564.0) {
				p = 2;
			}
			return p;
		}

		static double N5afd2928(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5682400.0) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() > 5682400.0) {
				p = 2;
			}
			return p;
		}

		static double N1a2961b29(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 2;
			} else if (((Double) i[4]).doubleValue() <= 407.0) {
				p = J48.N12d03f930(i);
			} else if (((Double) i[4]).doubleValue() > 407.0) {
				p = J48.N55571e45(i);
			}
			return p;
		}

		static double N12d03f930(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 2;
			} else if (((Double) i[5]).doubleValue() <= 7170289.0) {
				p = J48.N5ffb1831(i);
			} else if (((Double) i[5]).doubleValue() > 7170289.0) {
				p = J48.N178703843(i);
			}
			return p;
		}

		static double N5ffb1831(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 2;
			} else if (((Double) i[4]).doubleValue() <= 393.0) {
				p = J48.N15dfd7732(i);
			} else if (((Double) i[4]).doubleValue() > 393.0) {
				p = 3;
			}
			return p;
		}

		static double N15dfd7732(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2038.0) {
				p = J48.N1abc7b933(i);
			} else if (((Double) i[1]).doubleValue() > 2038.0) {
				p = J48.N1ac3c0835(i);
			}
			return p;
		}

		static double N1abc7b933(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 3;
			} else if (((Double) i[4]).doubleValue() <= 217.0) {
				p = J48.Nc55e3634(i);
			} else if (((Double) i[4]).doubleValue() > 217.0) {
				p = 2;
			}
			return p;
		}

		static double Nc55e3634(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 3;
			} else if (((Double) i[1]).doubleValue() <= 2025.0) {
				p = 3;
			} else if (((Double) i[1]).doubleValue() > 2025.0) {
				p = 0;
			}
			return p;
		}

		static double N1ac3c0835(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 2;
			} else if (((Double) i[5]).doubleValue() <= 7066449.0) {
				p = J48.N9971ad36(i);
			} else if (((Double) i[5]).doubleValue() > 7066449.0) {
				p = J48.N1f630dc37(i);
			}
			return p;
		}

		static double N9971ad36(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 3;
			} else if (((Double) i[4]).doubleValue() <= 223.0) {
				p = 3;
			} else if (((Double) i[4]).doubleValue() > 223.0) {
				p = 2;
			}
			return p;
		}

		static double N1f630dc37(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 2;
			} else if (((Double) i[2]).doubleValue() <= 5371405.0) {
				p = 2;
			} else if (((Double) i[2]).doubleValue() > 5371405.0) {
				p = J48.N1c5c138(i);
			}
			return p;
		}

		static double N1c5c138(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2260.0) {
				p = J48.N5e060239(i);
			} else if (((Double) i[0]).doubleValue() > 2260.0) {
				p = J48.Nb09e8942(i);
			}
			return p;
		}

		static double N5e060239(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2249.0) {
				p = J48.Ndc840f40(i);
			} else if (((Double) i[0]).doubleValue() > 2249.0) {
				p = 1;
			}
			return p;
		}

		static double Ndc840f40(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2135.0) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() > 2135.0) {
				p = J48.N1621e4241(i);
			}
			return p;
		}

		static double N1621e4241(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5656571.0) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() > 5656571.0) {
				p = 2;
			}
			return p;
		}

		static double Nb09e8942(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 2;
			} else if (((Double) i[3]).doubleValue() <= 2349.0) {
				p = 2;
			} else if (((Double) i[3]).doubleValue() > 2349.0) {
				p = 3;
			}
			return p;
		}

		static double N178703843(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2372.0) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() > 2372.0) {
				p = J48.Nfa9cf44(i);
			}
			return p;
		}

		static double Nfa9cf44(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 3;
			} else if (((Double) i[3]).doubleValue() <= 2383.0) {
				p = 3;
			} else if (((Double) i[3]).doubleValue() > 2383.0) {
				p = 2;
			}
			return p;
		}

		static double N55571e45(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 1906.0) {
				p = J48.Nca832746(i);
			} else if (((Double) i[1]).doubleValue() > 1906.0) {
				p = 2;
			}
			return p;
		}

		static double Nca832746(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 3;
			} else if (((Double) i[1]).doubleValue() <= 1727.0) {
				p = J48.N16897b247(i);
			} else if (((Double) i[1]).doubleValue() > 1727.0) {
				p = 2;
			}
			return p;
		}

		static double N16897b247(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2320.0) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() > 2320.0) {
				p = 3;
			}
			return p;
		}

		static double N1bf677048(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() <= 7175080.0) {
				p = J48.N1201a2549(i);
			} else if (((Double) i[5]).doubleValue() > 7175080.0) {
				p = 2;
			}
			return p;
		}

		static double N1201a2549(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 3;
			} else if (((Double) i[1]).doubleValue() <= 2058.0) {
				p = 3;
			} else if (((Double) i[1]).doubleValue() > 2058.0) {
				p = 0;
			}
			return p;
		}

		static double N94948a50(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2327.0) {
				p = J48.Na401c251(i);
			} else if (((Double) i[1]).doubleValue() > 2327.0) {
				p = J48.Ncac268164(i);
			}
			return p;
		}

		static double Na401c251(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5904674.0) {
				p = J48.N16f8cd052(i);
			} else if (((Double) i[2]).doubleValue() > 5904674.0) {
				p = J48.N480457148(i);
			}
			return p;
		}

		static double N16f8cd052(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 2;
			} else if (((Double) i[3]).doubleValue() <= 2447.0) {
				p = J48.N85af8053(i);
			} else if (((Double) i[3]).doubleValue() > 2447.0) {
				p = J48.N1f7d13470(i);
			}
			return p;
		}

		static double N85af8053(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 3;
			} else if (((Double) i[5]).doubleValue() <= 7001254.0) {
				p = J48.Nc5135554(i);
			} else if (((Double) i[5]).doubleValue() > 7001254.0) {
				p = J48.N15fea6056(i);
			}
			return p;
		}

		static double Nc5135554(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 4922098.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 4922098.0) {
				p = J48.N78717155(i);
			}
			return p;
		}

		static double N78717155(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 2;
			} else if (((Double) i[3]).doubleValue() <= 1978.0) {
				p = 2;
			} else if (((Double) i[3]).doubleValue() > 1978.0) {
				p = 3;
			}
			return p;
		}

		static double N15fea6056(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2231.0) {
				p = J48.N1457cb57(i);
			} else if (((Double) i[0]).doubleValue() > 2231.0) {
				p = J48.Nf3d6a562(i);
			}
			return p;
		}

		static double N1457cb57(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5382425.0) {
				p = J48.N18fef3d58(i);
			} else if (((Double) i[2]).doubleValue() > 5382425.0) {
				p = J48.N1bd472260(i);
			}
			return p;
		}

		static double N18fef3d58(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2168.0) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() > 2168.0) {
				p = J48.Na3bcc159(i);
			}
			return p;
		}

		static double Na3bcc159(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7169813.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7169813.0) {
				p = 2;
			}
			return p;
		}

		static double N1bd472260(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5791642.0) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() > 5791642.0) {
				p = J48.N1891d8f61(i);
			}
			return p;
		}

		static double N1891d8f61(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7132025.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7132025.0) {
				p = 0;
			}
			return p;
		}

		static double Nf3d6a562(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2201.0) {
				p = J48.N911f7163(i);
			} else if (((Double) i[1]).doubleValue() > 2201.0) {
				p = J48.N1f20eeb66(i);
			}
			return p;
		}

		static double N911f7163(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2237.0) {
				p = J48.N1a73d3c64(i);
			} else if (((Double) i[0]).doubleValue() > 2237.0) {
				p = 2;
			}
			return p;
		}

		static double N1a73d3c64(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 3;
			} else if (((Double) i[0]).doubleValue() <= 2232.0) {
				p = 3;
			} else if (((Double) i[0]).doubleValue() > 2232.0) {
				p = J48.Na56a7c65(i);
			}
			return p;
		}

		static double Na56a7c65(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7132820.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7132820.0) {
				p = 2;
			}
			return p;
		}

		static double N1f20eeb66(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2288.0) {
				p = J48.Nb179c367(i);
			} else if (((Double) i[0]).doubleValue() > 2288.0) {
				p = 2;
			}
			return p;
		}

		static double Nb179c367(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2242.0) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() > 2242.0) {
				p = J48.N1b10d4268(i);
			}
			return p;
		}

		static double N1b10d4268(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 310.0) {
				p = J48.Ndd87b269(i);
			} else if (((Double) i[4]).doubleValue() > 310.0) {
				p = 0;
			}
			return p;
		}

		static double Ndd87b269(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 137.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 137.0) {
				p = 1;
			}
			return p;
		}

		static double N1f7d13470(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2231.0) {
				p = J48.Nc7e55371(i);
			} else if (((Double) i[1]).doubleValue() > 2231.0) {
				p = J48.N26e431141(i);
			}
			return p;
		}

		static double Nc7e55371(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5733175.0) {
				p = J48.N1a0c10f72(i);
			} else if (((Double) i[2]).doubleValue() > 5733175.0) {
				p = J48.Nbd0108134(i);
			}
			return p;
		}

		static double N1a0c10f72(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2244.0) {
				p = J48.Ne2eec873(i);
			} else if (((Double) i[0]).doubleValue() > 2244.0) {
				p = J48.N12498b5116(i);
			}
			return p;
		}

		static double Ne2eec873(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7132257.0) {
				p = J48.Naa983574(i);
			} else if (((Double) i[5]).doubleValue() > 7132257.0) {
				p = J48.N67ac1978(i);
			}
			return p;
		}

		static double Naa983574(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2467.0) {
				p = J48.N1eec61275(i);
			} else if (((Double) i[3]).doubleValue() > 2467.0) {
				p = 1;
			}
			return p;
		}

		static double N1eec61275(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2236.0) {
				p = J48.N10dd1f776(i);
			} else if (((Double) i[0]).doubleValue() > 2236.0) {
				p = 2;
			}
			return p;
		}

		static double N10dd1f776(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7129333.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7129333.0) {
				p = J48.N53c01577(i);
			}
			return p;
		}

		static double N53c01577(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2190.0) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() > 2190.0) {
				p = 1;
			}
			return p;
		}

		static double N67ac1978(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5566961.0) {
				p = J48.N53ba3d79(i);
			} else if (((Double) i[2]).doubleValue() > 5566961.0) {
				p = J48.N929206106(i);
			}
			return p;
		}

		static double N53ba3d79(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 33.0) {
				p = J48.Ne80a5980(i);
			} else if (((Double) i[4]).doubleValue() > 33.0) {
				p = J48.Nb169f8103(i);
			}
			return p;
		}

		static double Ne80a5980(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2178.0) {
				p = J48.N1ff5ea781(i);
			} else if (((Double) i[1]).doubleValue() > 2178.0) {
				p = J48.N84abc989(i);
			}
			return p;
		}

		static double N1ff5ea781(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2200.0) {
				p = J48.N9f2a0b82(i);
			} else if (((Double) i[0]).doubleValue() > 2200.0) {
				p = J48.N91375086(i);
			}
			return p;
		}

		static double N9f2a0b82(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7138402.0) {
				p = J48.N1813fac83(i);
			} else if (((Double) i[5]).doubleValue() > 7138402.0) {
				p = 1;
			}
			return p;
		}

		static double N1813fac83(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2171.0) {
				p = J48.N7b707284(i);
			} else if (((Double) i[1]).doubleValue() > 2171.0) {
				p = 1;
			}
			return p;
		}

		static double N7b707284(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2196.0) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() > 2196.0) {
				p = J48.N13622885(i);
			}
			return p;
		}

		static double N13622885(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 21.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 21.0) {
				p = 1;
			}
			return p;
		}

		static double N91375086(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() <= 7141000.0) {
				p = J48.N1c672d087(i);
			} else if (((Double) i[5]).doubleValue() > 7141000.0) {
				p = J48.N19bd03e88(i);
			}
			return p;
		}

		static double N1c672d087(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2483.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2483.0) {
				p = 1;
			}
			return p;
		}

		static double N19bd03e88(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2466.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2466.0) {
				p = 1;
			}
			return p;
		}

		static double N84abc989(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2216.0) {
				p = J48.N2a340e90(i);
			} else if (((Double) i[1]).doubleValue() > 2216.0) {
				p = J48.N9fef6f96(i);
			}
			return p;
		}

		static double N2a340e90(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2184.0) {
				p = J48.Nbfbdb091(i);
			} else if (((Double) i[1]).doubleValue() > 2184.0) {
				p = 1;
			}
			return p;
		}

		static double Nbfbdb091(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2199.0) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() > 2199.0) {
				p = J48.N3e86d092(i);
			}
			return p;
		}

		static double N3e86d092(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7141620.0) {
				p = J48.N105016993(i);
			} else if (((Double) i[5]).doubleValue() > 7141620.0) {
				p = 1;
			}
			return p;
		}

		static double N105016993(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5379823.0) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() > 5379823.0) {
				p = J48.N19fcc6994(i);
			}
			return p;
		}

		static double N19fcc6994(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2201.0) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() > 2201.0) {
				p = J48.N25349895(i);
			}
			return p;
		}

		static double N25349895(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() <= 5405800.0) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() > 5405800.0) {
				p = 1;
			}
			return p;
		}

		static double N9fef6f96(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2480.0) {
				p = J48.N209f4e97(i);
			} else if (((Double) i[3]).doubleValue() > 2480.0) {
				p = J48.Nf38798101(i);
			}
			return p;
		}

		static double N209f4e97(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 22.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 22.0) {
				p = J48.N1bac74898(i);
			}
			return p;
		}

		static double N1bac74898(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2477.0) {
				p = J48.N17172ea99(i);
			} else if (((Double) i[3]).doubleValue() > 2477.0) {
				p = 1;
			}
			return p;
		}

		static double N17172ea99(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2243.0) {
				p = J48.N12f6684100(i);
			} else if (((Double) i[0]).doubleValue() > 2243.0) {
				p = 0;
			}
			return p;
		}

		static double N12f6684100(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2223.0) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() > 2223.0) {
				p = 1;
			}
			return p;
		}

		static double Nf38798101(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 17.0) {
				p = J48.N4b222f102(i);
			} else if (((Double) i[4]).doubleValue() > 17.0) {
				p = 1;
			}
			return p;
		}

		static double N4b222f102(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2485.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2485.0) {
				p = 1;
			}
			return p;
		}

		static double Nb169f8103(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2221.0) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() > 2221.0) {
				p = J48.N1a457b6104(i);
			}
			return p;
		}

		static double N1a457b6104(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 2;
			} else if (((Double) i[1]).doubleValue() <= 2190.0) {
				p = J48.N7a78d3105(i);
			} else if (((Double) i[1]).doubleValue() > 2190.0) {
				p = 1;
			}
			return p;
		}

		static double N7a78d3105(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2171.0) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() > 2171.0) {
				p = 2;
			}
			return p;
		}

		static double N929206106(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2483.0) {
				p = J48.Nb0f13d107(i);
			} else if (((Double) i[3]).doubleValue() > 2483.0) {
				p = 1;
			}
			return p;
		}

		static double Nb0f13d107(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5680196.0) {
				p = J48.Nae000d108(i);
			} else if (((Double) i[2]).doubleValue() > 5680196.0) {
				p = 0;
			}
			return p;
		}

		static double Nae000d108(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7135197.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7135197.0) {
				p = J48.N1855af5109(i);
			}
			return p;
		}

		static double N1855af5109(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 20.0) {
				p = J48.N169e11110(i);
			} else if (((Double) i[4]).doubleValue() > 20.0) {
				p = J48.Ne39a3e111(i);
			}
			return p;
		}

		static double N169e11110(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2166.0) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() > 2166.0) {
				p = 0;
			}
			return p;
		}

		static double Ne39a3e111(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2476.0) {
				p = J48.Na39137112(i);
			} else if (((Double) i[3]).doubleValue() > 2476.0) {
				p = J48.N9fbe93114(i);
			}
			return p;
		}

		static double Na39137112(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5621153.0) {
				p = J48.N92e78c113(i);
			} else if (((Double) i[2]).doubleValue() > 5621153.0) {
				p = 0;
			}
			return p;
		}

		static double N92e78c113(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2165.0) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() > 2165.0) {
				p = 1;
			}
			return p;
		}

		static double N9fbe93114(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 21.0) {
				p = J48.N198dfaf115(i);
			} else if (((Double) i[4]).doubleValue() > 21.0) {
				p = 1;
			}
			return p;
		}

		static double N198dfaf115(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2196.0) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() > 2196.0) {
				p = 0;
			}
			return p;
		}

		static double N12498b5116(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7138164.0) {
				p = J48.N1a5ab41117(i);
			} else if (((Double) i[5]).doubleValue() > 7138164.0) {
				p = J48.N16930e2126(i);
			}
			return p;
		}

		static double N1a5ab41117(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2212.0) {
				p = J48.N18e3e60118(i);
			} else if (((Double) i[1]).doubleValue() > 2212.0) {
				p = J48.N197d257124(i);
			}
			return p;
		}

		static double N18e3e60118(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5143723.0) {
				p = J48.N1a125f0119(i);
			} else if (((Double) i[2]).doubleValue() > 5143723.0) {
				p = J48.N15601ea123(i);
			}
			return p;
		}

		static double N1a125f0119(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2474.0) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() > 2474.0) {
				p = J48.Nc1cd1f120(i);
			}
			return p;
		}

		static double Nc1cd1f120(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 2;
			} else if (((Double) i[0]).doubleValue() <= 2284.0) {
				p = J48.N181afa3121(i);
			} else if (((Double) i[0]).doubleValue() > 2284.0) {
				p = 1;
			}
			return p;
		}

		static double N181afa3121(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 2;
			} else if (((Double) i[5]).doubleValue() <= 7133463.0) {
				p = 2;
			} else if (((Double) i[5]).doubleValue() > 7133463.0) {
				p = J48.N131f71a122(i);
			}
			return p;
		}

		static double N131f71a122(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2193.0) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() > 2193.0) {
				p = 0;
			}
			return p;
		}

		static double N15601ea123(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 27.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 27.0) {
				p = 2;
			}
			return p;
		}

		static double N197d257124(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2475.0) {
				p = J48.N7259da125(i);
			} else if (((Double) i[3]).doubleValue() > 2475.0) {
				p = 1;
			}
			return p;
		}

		static double N7259da125(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 29.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 29.0) {
				p = 1;
			}
			return p;
		}

		static double N16930e2126(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2481.0) {
				p = J48.N108786b127(i);
			} else if (((Double) i[3]).doubleValue() > 2481.0) {
				p = J48.Nf5da06133(i);
			}
			return p;
		}

		static double N108786b127(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() <= 5056992.0) {
				p = J48.N119c082128(i);
			} else if (((Double) i[2]).doubleValue() > 5056992.0) {
				p = 2;
			}
			return p;
		}

		static double N119c082128(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2474.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2474.0) {
				p = J48.N1add2dd129(i);
			}
			return p;
		}

		static double N1add2dd129(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 26.0) {
				p = J48.Neee36c130(i);
			} else if (((Double) i[4]).doubleValue() > 26.0) {
				p = J48.Ndefa1a132(i);
			}
			return p;
		}

		static double Neee36c130(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2324.0) {
				p = J48.N194df86131(i);
			} else if (((Double) i[0]).doubleValue() > 2324.0) {
				p = 1;
			}
			return p;
		}

		static double N194df86131(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() <= 4969291.0) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() > 4969291.0) {
				p = 1;
			}
			return p;
		}

		static double Ndefa1a132(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2221.0) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() > 2221.0) {
				p = 1;
			}
			return p;
		}

		static double Nf5da06133(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() <= 68.0) {
				p = 1;
			} else if (((Double) i[4]).doubleValue() > 68.0) {
				p = 0;
			}
			return p;
		}

		static double Nbd0108134(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7130391.0) {
				p = J48.N8ed465135(i);
			} else if (((Double) i[5]).doubleValue() > 7130391.0) {
				p = 0;
			}
			return p;
		}

		static double N8ed465135(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 1;
			} else if (((Double) i[3]).doubleValue() <= 2473.0) {
				p = J48.N11a698a136(i);
			} else if (((Double) i[3]).doubleValue() > 2473.0) {
				p = J48.N107077e137(i);
			}
			return p;
		}

		static double N11a698a136(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7122913.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7122913.0) {
				p = 0;
			}
			return p;
		}

		static double N107077e137(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7129455.0) {
				p = J48.N7ced01138(i);
			} else if (((Double) i[5]).doubleValue() > 7129455.0) {
				p = J48.N765291140(i);
			}
			return p;
		}

		static double N7ced01138(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2195.0) {
				p = J48.N1ac04e8139(i);
			} else if (((Double) i[0]).doubleValue() > 2195.0) {
				p = 1;
			}
			return p;
		}

		static double N1ac04e8139(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() <= 2178.0) {
				p = 1;
			} else if (((Double) i[1]).doubleValue() > 2178.0) {
				p = 0;
			}
			return p;
		}

		static double N765291140(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2482.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2482.0) {
				p = 1;
			}
			return p;
		}

		static double N26e431141(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2481.0) {
				p = J48.N14f8dab142(i);
			} else if (((Double) i[3]).doubleValue() > 2481.0) {
				p = 1;
			}
			return p;
		}

		static double N14f8dab142(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2270.0) {
				p = J48.N1ddebc3143(i);
			} else if (((Double) i[0]).doubleValue() > 2270.0) {
				p = J48.N17943a4147(i);
			}
			return p;
		}

		static double N1ddebc3143(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2248.0) {
				p = J48.Na18aa2144(i);
			} else if (((Double) i[0]).doubleValue() > 2248.0) {
				p = J48.N194ca6c145(i);
			}
			return p;
		}

		static double Na18aa2144(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2476.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2476.0) {
				p = 1;
			}
			return p;
		}

		static double N194ca6c145(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() <= 4906902.0) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() > 4906902.0) {
				p = J48.N17590db146(i);
			}
			return p;
		}

		static double N17590db146(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 25.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 25.0) {
				p = 1;
			}
			return p;
		}

		static double N17943a4147(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() <= 7141094.0) {
				p = 1;
			} else if (((Double) i[5]).doubleValue() > 7141094.0) {
				p = 0;
			}
			return p;
		}

		static double N480457148(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2643.0) {
				p = J48.N14fe5c149(i);
			} else if (((Double) i[3]).doubleValue() > 2643.0) {
				p = 3;
			}
			return p;
		}

		static double N14fe5c149(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() <= 6095437.0) {
				p = J48.N47858e150(i);
			} else if (((Double) i[5]).doubleValue() > 6095437.0) {
				p = J48.N19134f4151(i);
			}
			return p;
		}

		static double N47858e150(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() <= 4922098.0) {
				p = 0;
			} else if (((Double) i[5]).doubleValue() > 4922098.0) {
				p = 3;
			}
			return p;
		}

		static double N19134f4151(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2350.0) {
				p = J48.N2bbd86152(i);
			} else if (((Double) i[0]).doubleValue() > 2350.0) {
				p = J48.N17182c1161(i);
			}
			return p;
		}

		static double N2bbd86152(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 0;
			} else if (((Double) i[2]).doubleValue() <= 6133200.0) {
				p = J48.N1a7bf11153(i);
			} else if (((Double) i[2]).doubleValue() > 6133200.0) {
				p = 0;
			}
			return p;
		}

		static double N1a7bf11153(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2215.0) {
				p = J48.N1f12c4e154(i);
			} else if (((Double) i[0]).doubleValue() > 2215.0) {
				p = J48.Ndf6ccd157(i);
			}
			return p;
		}

		static double N1f12c4e154(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2181.0) {
				p = J48.N93dee9155(i);
			} else if (((Double) i[1]).doubleValue() > 2181.0) {
				p = 0;
			}
			return p;
		}

		static double N93dee9155(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2471.0) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() > 2471.0) {
				p = J48.Nfabe9156(i);
			}
			return p;
		}

		static double Nfabe9156(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() <= 5954834.0) {
				p = 1;
			} else if (((Double) i[2]).doubleValue() > 5954834.0) {
				p = 3;
			}
			return p;
		}

		static double Ndf6ccd157(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 3;
			} else if (((Double) i[2]).doubleValue() <= 5939015.0) {
				p = 3;
			} else if (((Double) i[2]).doubleValue() > 5939015.0) {
				p = J48.N601bb1158(i);
			}
			return p;
		}

		static double N601bb1158(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 3;
			} else if (((Double) i[4]).doubleValue() <= 32.0) {
				p = 3;
			} else if (((Double) i[4]).doubleValue() > 32.0) {
				p = J48.N1ba34f2159(i);
			}
			return p;
		}

		static double N1ba34f2159(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 185.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 185.0) {
				p = J48.N1ea2dfe160(i);
			}
			return p;
		}

		static double N1ea2dfe160(Object[] i) {
			double p = Double.NaN;
			if (i[2] == null) {
				p = 3;
			} else if (((Double) i[2]).doubleValue() <= 6107871.0) {
				p = 3;
			} else if (((Double) i[2]).doubleValue() > 6107871.0) {
				p = 0;
			}
			return p;
		}

		static double N17182c1161(Object[] i) {
			double p = Double.NaN;
			if (i[3] == null) {
				p = 0;
			} else if (((Double) i[3]).doubleValue() <= 2486.0) {
				p = J48.N13f5d07162(i);
			} else if (((Double) i[3]).doubleValue() > 2486.0) {
				p = 3;
			}
			return p;
		}

		static double N13f5d07162(Object[] i) {
			double p = Double.NaN;
			if (i[4] == null) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() <= 334.0) {
				p = 0;
			} else if (((Double) i[4]).doubleValue() > 334.0) {
				p = J48.Nf4a24a163(i);
			}
			return p;
		}

		static double Nf4a24a163(Object[] i) {
			double p = Double.NaN;
			if (i[1] == null) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() <= 2273.0) {
				p = 0;
			} else if (((Double) i[1]).doubleValue() > 2273.0) {
				p = 3;
			}
			return p;
		}

		static double Ncac268164(Object[] i) {
			double p = Double.NaN;
			if (i[5] == null) {
				p = 3;
			} else if (((Double) i[5]).doubleValue() <= 6818185.0) {
				p = 3;
			} else if (((Double) i[5]).doubleValue() > 6818185.0) {
				p = J48.N1a16869165(i);
			}
			return p;
		}

		static double N1a16869165(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() <= 2473.0) {
				p = 0;
			} else if (((Double) i[0]).doubleValue() > 2473.0) {
				p = J48.N1cde100166(i);
			}
			return p;
		}

		static double N1cde100166(Object[] i) {
			double p = Double.NaN;
			if (i[0] == null) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() <= 2566.0) {
				p = 1;
			} else if (((Double) i[0]).doubleValue() > 2566.0) {
				p = 3;
			}
			return p;
		}
	}

}
