package com.sensyscalactivityrecognition2.worker.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

import spine.SPINEFunctionConstants;
import spine.datamodel.Feature;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.utils.Utils;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;

public class TSGeneratorThread implements Runnable{

	private final static String[] FEATS = { "1_1_2_1", "1_1_3_1", "1_1_9_2",
											"2_1_3_1", "2_1_4_1", "2_1_9_2" };

	private final static int WINDOW = 40;
	private final static int SHIFT = 20;
	private final static int NODES_COUNT_IN_RAWDATA = 2;
	private final static int COLUMNS_PER_NODE = 3; // acc x,y,z

	private final static Vector<Feature> featsToReq = new Vector<Feature>();
	private final static int[] featsToReqIndexInRawData = new int[FEATS.length];
	
	private FileReader fileReader;
	private static File fileRaw, fileTsWeka, fileTsApp;
	
	// FileWriter per memorizzare il ts da passare a weka
	private FileWriter fileWriterTsWeka,

	// FileWriter per memorizzare il ts dell'app
	fileWriterTsApp;
	
	private Handler handler;
	
	
	public TSGeneratorThread(Handler handler){
		this.handler = handler;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		showProgressDialog(true);
		generateCustomTrainingSet();
		//se si vuole addestrare un algoritmo in weka anzichè utilizzare il knn con il ts dell'app
		ActivityRecognitionWidget.wekaWorker.addestra();
		showProgressDialog(false);
	
		finish();
		
	}

	private void finish() {
		// TODO Auto-generated method stub
		Bundle data = new Bundle();
		data.putString(ActivityRecognitionWidget.CustomTabHandler.FINISH_TS_GENERATION,"Personalizzaizone completata!E' necessario resettare i sensori e riavviare l'applicazione!");
		data.putString(ActivityRecognitionWidget.CustomTabHandler.CHANGE_SETUP_BUTTON,"Esci");
		data.putBoolean(ActivityRecognitionWidget.CustomTabHandler.ENABLE_BACK_BUTTON,false);
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
	

	public void generateCustomTrainingSet() {

		long before = System.nanoTime();

		if (WINDOW <= 0 || SHIFT <= 0 || SHIFT > WINDOW) {
			Log.e("ERRORE", "WINDOW and/or SHIFT INVALID");
			System.exit(-1);
		}

		try {
			
			/*fileTsWeka = new File(Environment.getExternalStorageDirectory(),
					ActivityRecognitionWidget.TS_FILE);
			fileWriterTsWeka = new FileWriter(fileTsWeka);

			fileTsApp = new File(Environment.getExternalStorageDirectory(),
					MonitoringWorkerThread.TRAINING_SET);
			fileWriterTsApp = new FileWriter(fileTsApp);

			// Read raw data to create ts
			fileRaw = new File(Environment.getExternalStorageDirectory(),
					ActivityRecognitionWidget.RAWDATA_FILE);
			fileReader = new FileReader(fileRaw);*/
			
			fileTsWeka = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+ActivityRecognitionWidget.TS_FILE);
			fileWriterTsWeka = new FileWriter(fileTsWeka);

			fileTsApp = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+MonitoringWorkerThread.TRAINING_SET);
			fileWriterTsApp = new FileWriter(fileTsApp);

			// Read raw data to create ts
			fileRaw = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+ActivityRecognitionWidget.RAWDATA_FILE);
			fileReader = new FileReader(fileRaw);
			
			BufferedReader br = new BufferedReader(fileReader);

			String line = br.readLine();

			String[] rawDataLabels = null;
			int t = 0;
			if (line != null) {
				StringTokenizer st = new StringTokenizer(line, " ;", false);
				rawDataLabels = new String[st.countTokens()];
				while (st.hasMoreTokens()) {
					rawDataLabels[t++] = st.nextToken();
				}
			}

			if (rawDataLabels != null) {

				for (int i = 0; i < FEATS.length; i++) {
					Feature currFeat = null;
					try {
						currFeat = Utils.parseFeatureProperties(FEATS[i]);
						featsToReq.addElement((Feature) currFeat.clone());
						currFeat.setFeatureCode(SPINEFunctionConstants.RAW_DATA);
						boolean foundMatch = false;
						for (int j = 0; j < rawDataLabels.length; j++) {
							try {
								Feature currRawFeat = Utils
										.parseFeatureProperties(rawDataLabels[j]);
								currRawFeat
										.setFeatureCode(SPINEFunctionConstants.RAW_DATA);
								if (currFeat.compareTo(currRawFeat) == 0) {
									foundMatch = true;
									featsToReqIndexInRawData[i] = j;
									break;
								}
							} catch (NumberFormatException nfe) {
							}
						}
						if (!foundMatch) {
							Log.e("ERRORE",
									"CANNOT COMPUTE REQUESTED FEATURES FROM GIVEN RAW DATA SET!!");
							System.exit(-1);
						}
					} catch (NumberFormatException nfe) {
						Log.e("ERRORE",
								(i + 1)
										+ "° feature requested MALFORMED; will skip it.");
					}
				}

				// System.out.println("PRE-PROCESS PHASE 2: OK!\n");

				Vector<int[]> rawDataV = new Vector<int[]>();

				try {
					line = br.readLine();
					while (line != null) {
						StringTokenizer st = new StringTokenizer(line, " ;",
								false);

						int k = 0;
						int[] sample = new int[1 + COLUMNS_PER_NODE
								* NODES_COUNT_IN_RAWDATA];
						int reading = 0;
						while (st.hasMoreTokens()) {
							reading = Integer.parseInt((st.nextToken()));
							sample[k++] = reading;
						}

						rawDataV.addElement(sample);
						line = br.readLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				br.close();
				int[][] rawData = new int[1 + COLUMNS_PER_NODE
						* NODES_COUNT_IN_RAWDATA][rawDataV.size()];
				for (int j = 0; j < rawData.length; j++)
					for (int i = 0; i < rawData[0].length; i++)
						rawData[j][i] = ((int[]) rawDataV.elementAt(i))[j];

				String s1 = Arrays.toString(FEATS);
				String newS1 = s1.replaceAll("\\[|\\]", "");

				// Ts weka
				fileWriterTsWeka.write("class," + newS1 + "\r\n");
				fileWriterTsWeka.flush();
				//Log.i("Scrittura su fileTsWeka", "class," + newS1 + "\r\n");

				// Ts app
				newS1 = newS1.replaceAll(", ", ";");
				fileWriterTsApp.write("class;" + newS1 + "\r\n");
				fileWriterTsApp.flush();
				//Log.i("Scrittura su fileTsApp", "class;" + newS1 + "\r\n");

				int startIndex = 0;
				int j = 0;
				int[] dataWindow = new int[WINDOW];
				try {
					long[] currInstance = new long[1 + featsToReq.size()];
					while (true) {
						System.arraycopy(rawData[0], startIndex, dataWindow, 0,
								WINDOW);
						currInstance[0] = Utils.calculate(
								SPINEFunctionConstants.MODE, dataWindow);

						for (int i = 0; i < featsToReq.size(); i++) {
							Feature curr = (Feature) featsToReq.elementAt(i);
							System.arraycopy(
									rawData[featsToReqIndexInRawData[i]],
									startIndex, dataWindow, 0, WINDOW);
							long feat = Utils.calculate(curr.getFeatureCode(),
									dataWindow);

							currInstance[i + 1] = feat;
						}

						startIndex = SHIFT * ++j;

						String s = Arrays.toString(currInstance);

						// Ts weka
						String newS = s.replaceAll("\\[|\\]", "");
						String[] tmp = newS.split(",");
						String tmp0 = tmp[0];
						tmp[0] = getClassLabelOfActivity(tmp0);
						newS = "";
						int i = 0;
						for (String tmps : tmp) {
							if (i == 0)
								newS += tmps;
							else
								newS += "," + tmps;
							i++;
						}
						fileWriterTsWeka.write(newS + "\r\n");
						fileWriterTsWeka.flush();
						//Log.i("Scrittura su fileTsWeka", newS + "\r\n");
						//

						// Ts app
						newS = s.replaceAll("\\[|\\]", "");
						newS = newS.replaceAll(", ", ";");
						fileWriterTsApp.write(newS + "\r\n");
						fileWriterTsApp.flush();
						//Log.i("Scrittura su fileTsApp", newS + "\r\n");
						//
					}
				} catch (Exception e) {
				}

				fileWriterTsWeka.close();
				fileWriterTsApp.close();
			}
		} catch (FileNotFoundException e) {
			Log.e("ERRORE", "FILE NOT FOUND!!");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("INFO", "elapsed time [ms]: " + (System.nanoTime() - before)/ 100000);

	}
	
	private String getClassLabelOfActivity(String tmp0) {
		// TODO Auto-generated method stub
		if(tmp0.equals("0"))
			return "STANDING";
		if(tmp0.equals("10"))
			return "SITTING";
		if(tmp0.equals("20"))
			return "LYINGDOWN";
		if(tmp0.equals("3"))
			return "WALKING";
		return "";

	}
	
	public void showProgressDialog(boolean show) {
		// TODO Auto-generated method stub
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.CustomTabHandler.SHOW_PROGRESS_DIALOG, show);
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

}
