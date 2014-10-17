package com.sensyscal.activityrecognition2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import spine.SPINEFactory;
import spine.SPINEManager;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bodycloud.lib.domain.ModalitySpecification;
import com.sensyscal.activityrecognition2.cloud.sensors.DataFromSensorCatcher;
import com.sensyscal.activityrecognition2.cloud.updater.AndroidClient;
import com.sensyscal.activityrecognition2.cloud.utility.ActivityRecognitionApp;
import com.sensyscal.activityrecognition2.utils.Automa;
import com.sensyscal.activityrecognition2.utils.ClassesCodes;
import com.sensyscal.activityrecognition2.utils.TransitionsCodes;
import com.sensyscal.activityrecognition2.worker.log.LoggerUpdaterWorkerThread;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;
import com.sensyscal.activityrecognition2.worker.setup.DiscoveryWorkerThread;
import com.sensyscal.activityrecognition2.worker.setup.StarterWorkerThread;
import com.sensyscal.activityrecognition2.worker.setup.StopperWorkerThread;
import com.sensyscal.activityrecognition2.worker.statistics.StatisticsManagerWorkerThread;
import com.sensyscalactivityrecognition2.worker.custom.CustomDataCollectorWorkerThread;
import com.sensyscalactivityrecognition2.worker.custom.TSGeneratorThread;
import com.sensyscalactivityrecognition2.worker.custom.WekaWorker;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class ActivityRecognitionWidget extends TabActivity implements CompoundButton.OnCheckedChangeListener{
	private static final String FILE_CONFIG = "app.properties";
	private static final int PROGRESS_DIALOG_ID = 0;

	private MonitoringWorkerThread monitoringWorkerThread;
	private StatisticsManagerWorkerThread statisticsManagerWorkerThread;
	private LoggerUpdaterWorkerThread loggerUpdaterWorkerThread;
	private CustomDataCollectorWorkerThread customrWorkerThread;
	public static WekaWorker wekaWorker;
	private TSGeneratorThread tsgeneratorthread;
	private TabHost tabHost;
	public static int[] activity_buffer;
	public static double[] min_buffer;
	
	public static int windows_size = 100;
	public static int shift = 50;
	public static double avg,max,min,diffmax,diffmin,diffminmax;
	public static List<Long> samples = new LinkedList<Long>();
	public static Map<Integer,Double> maxmin = new TreeMap<Integer, Double>();
	public static int inizio, fine;
	public static final String FOLDER = "activity_recognition";
	//public final static String RAWDATA_FILE = "activity_recognition/rawData.csv";
	//public final static String TS_FILE = "activity_recognition/trainingset.csv";
	public final static String RAWDATA_FILE = "rawData.csv";
	public final static String TS_FILE = "trainingset.csv";
	
	// cloud modality types
	public static final String RAW_DATA_FEED = "accel-data-feed3";
	public static final String SINGLE_RAW_ANALYSIS = "single-accel-analysis";
	public static final String GROUP_RAW_ANALYSIS = "group-accel-analysis";
	
	public static final String FEATURE_DATA_FEED = "feature-data-feed";
	public static final String SINGLE_FEATURE_ANALYSIS = "";
	public static final String GROUP_FEATURE_ANALYSIS = "";
	
	public static final String POSITION_DATA_FEED = "position-data-feed";
	public static final String SINGLE_POSITION_ANAYLSIS = "single-position-analysis";
	public static final String GROUP_POSITION_ANAYLSIS = "group-position-analysis";
	
	private LinkedList<AndroidClient> runningModalities;
	
	public int selectedActivity = -1;
	private FileWriter fileWriter;
	
	public static boolean isThigh;
	public static int cloudoption = 1;
	public static boolean groupflag = false;

	public static boolean customknn, customWeka;
	public static ActivityRecognitionWidget ARW;
	public static FileWriter fos;
	
	private static ActivityRecognitionWidget parent;
	   
	Message msg = Message.obtain();
	ActivityRecognitionWidgetHandler ARWH;
	protected static String TAG = "OauthTokenValidatorService";
	
	final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email";
	AccountManager accountManager; 
	
	public static String sampleType;
	    
	
	public static ActivityRecognitionWidget getInstance(){ 
		if (ARW == null) {ARW = new ActivityRecognitionWidget();}
		return ARW; 
		}
	
	public void onToggleClicked(View view) {
	    // Is the toggle on?
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	    	Toast.makeText(getApplicationContext(), "CLoud ON!", Toast.LENGTH_SHORT).show();
	    	cloudpopup();
	    	
	    } else {
	    	Toast.makeText(getApplicationContext(), "CLoud OFF!", Toast.LENGTH_SHORT).show();
	    	shutdownModalities();
	    }
	}
	
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
    	
    	groupflag = isChecked;
        
        if (groupflag){
        	Toast.makeText(getApplicationContext(), "Single is selected!", Toast.LENGTH_SHORT).show();
        }else{
        	Toast.makeText(getApplicationContext(), "Group is selected!", Toast.LENGTH_SHORT).show();
        	
        }
    }
	
	private void cloudpopup() {
		// TODO Auto-generated method stub
		  //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.cloudpopup);
        final Button button = (Button) dialog.findViewById(R.id.Closebutton);
        
        Switch s = (Switch)dialog.findViewById(R.id.cloudflag);
        if (s != null){
        	s.setOnCheckedChangeListener(this);
        }
        

        
        RadioButton c1 = (RadioButton)dialog.findViewById(R.id.option1);
        	c1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cloudoption=1;
				button.setEnabled(true);
			}
		});
        	
        RadioButton c2 = (RadioButton)dialog.findViewById(R.id.option2);
         	c2.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				cloudoption=2;
 				button.setEnabled(true);
 			}
 		});
         	
        RadioButton c3 = (RadioButton)dialog.findViewById(R.id.option3);
         	c3.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				cloudoption=3;
 				button.setEnabled(true);
 			}
 		});
		
        
        button.setOnClickListener(new OnClickListener() {
        @Override
            public void onClick(View v) {
	        
	    		switch(cloudoption){
	    			case 1:
	    				Toast.makeText(getApplicationContext(), "Full Cloud option selected!", Toast.LENGTH_SHORT).show();
	    				ExecuteModalities(RAW_DATA_FEED);
	    				/* Not yet implemented
	    				if (groupflag)
	    					ExecuteModalities(GROUP_RAW_ANALYSIS);
	    				else
	    					ExecuteModalities(SINGLE_RAW_ANALYSIS);
	    				*/
	    				break;
	    			case 2:
	    				Toast.makeText(getApplicationContext(), "Mix option selected!", Toast.LENGTH_SHORT).show();
	    				ExecuteModalities(FEATURE_DATA_FEED);
	    				/* Not yet implemented
	    				if (groupflag)
	    					ExecuteModalities(GROUP_FEATURE_ANALYSIS);
	    				else
	    					ExecuteModalities(SINGLE_FEATURE_ANALYSIS);
	    				*/
	    				break;
	    			case 3:
	    				Toast.makeText(getApplicationContext(), "Full local option selected!", Toast.LENGTH_SHORT).show();
	    				ExecuteModalities(POSITION_DATA_FEED);
	    				if (groupflag)
	    					ExecuteModalities(GROUP_POSITION_ANAYLSIS);
	    				else
	    					ExecuteModalities(SINGLE_POSITION_ANAYLSIS);
	    				break;
	    		
	    		}
	    	 
        		dialog.dismiss();
        	}
        });
        
        dialog.show();
    }

	
private void ExecuteModalities(final String mod){
		Thread refresh = new Thread(new Runnable() {
			
			private List<ModalitySpecification> modalityList = new LinkedList<ModalitySpecification>(); 

			@Override
			public void run() {
	    		if(!ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID)
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
				
				if(!ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID)
				{
					runTest("ERROR: NOT_LOGGED!");
					return;
				}
				
				try{
					
					
					this.modalityList = AndroidClient.getModalities(ActivityRecognitionApp.SERVER_URL, ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
					ModalitySpecification targetModality = null;
					
					for (ModalitySpecification modality : modalityList ) {
						Log.i("UMASS", modality.getName());
						if (modality.getName().equals(mod)) {
							targetModality = modality;
						}
					}
					RunMod(targetModality);
		
				}catch (Exception e){
					Log.i("MAIN_ACTIVITY", "Error while getting modality list", e);
				}
				runTest("Login Successfull!");
						    	
			}
		});
    	
    	refresh.start();
		
	}
	
	/**
	 * Helper method to display dialog to the user
	 * @param t
	 */
	public void runTest(String t) {
		final String text = t;
		new Thread() {
			public void run() {
				
                	try {
                		runOnUiThread(new Runnable() {

                			@Override
                			public void run() {
                				Toast.makeText(ActivityRecognitionWidget.getInstance(), text, Toast.LENGTH_LONG).show();	
                			}
                		});
                		Thread.sleep(300);
                	} catch (InterruptedException e) {
                		e.printStackTrace();
                	}
            }
		}.start();
	}	
	
	/**
	 * Runs a specified modality
	 * @param mod
	 */
	public void RunMod (ModalitySpecification mod){
		try {
			// create buffer and fill with test data
			if (mod.getName().equals(POSITION_DATA_FEED) || mod.getName().equals(RAW_DATA_FEED) 
					|| mod.getName().equals(FEATURE_DATA_FEED)) {
				DataFromSensorCatcher catcher = DataFromSensorCatcher.getInstance();
				if (mod.getName().equals(POSITION_DATA_FEED)) {
					sampleType = "position_data.txt";
				}
				else if (mod.getName().equals(RAW_DATA_FEED)) {
					sampleType = "raw_data.txt";
				}
				else if (mod.getName().equals(FEATURE_DATA_FEED)) {
					sampleType = "feature_data.txt";
				}
				catcher.startUsingSensor(DataFromSensorCatcher.class, mod.hashCode());
			}
			// execute modality
			AndroidClient androidClient = new AndroidClient(ActivityRecognitionApp.SERVER_URL, mod);
			Thread modalityThread = new Thread(androidClient);
			modalityThread.start();
			runningModalities.add(androidClient);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}		
	}
	
	public MonitoringWorkerThread getMonitoringWorkerThread() {
		return monitoringWorkerThread;
	}
	
	
	private void shutdownModalities() {
		for (AndroidClient ac : runningModalities) {
			DataFromSensorCatcher.getInstance().stopUsingSensor(DataFromSensorCatcher.class, ac.getModality().hashCode());
			ac.stopModalityExecution();
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(fileWriter!=null)
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		isThigh = false;
		finish();
	}	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runningModalities = new LinkedList<AndroidClient>();
		createFolder();
		showChooserSensorPosition();
		ARW = this;
		setContentView(R.layout.main);

		inizio = 0;
		fine = windows_size;
		accountManager = AccountManager.get(ActivityRecognitionWidget.getInstance());
	
		ActivityRecognitionApp.getInstance().initApp();
		
		
		 new Thread(new Runnable() {
		        public void run() {
		        	//ManualInit();
		        }
		    }).start();

		
		
		
		//startService(new Intent(this,OauthTokenValidatorService.class));
		
		initializeSPINEManager();
		initializeComponents();
		
		File classifierResultFile = new File(Environment.getExternalStorageDirectory(),"classifier_result.csv");
		try {
			fileWriter = new FileWriter(classifierResultFile);
			fileWriter.write("Real;Classified\n");
			fileWriter.flush();
			
			//this.ExecuteModalities("accel-data-feed.xml");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	    
	
	private void createFolder() {
		// TODO Auto-generated method stub
		File folder = new File(Environment.getExternalStorageDirectory() + "/"+FOLDER );
		boolean success = true;
		if (!folder.exists()) {
		    success = folder.mkdir();
		}
		Log.e("Folder created", success+"");
	}

	private void showChooserSensorPosition() {
		// TODO Auto-generated method stub
		  //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.chooser_dialog);
        final Button button = (Button) dialog.findViewById(R.id.buttonDialog);
        
        RadioButton c1 = (RadioButton)dialog.findViewById(R.id.radioButton5);
        	c1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isThigh = true;
				button.setEnabled(true);
			}
		});
        	
        RadioButton c2 = (RadioButton)dialog.findViewById(R.id.radioButton6);
         	c2.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				isThigh = false;
 				button.setEnabled(true);
 			}
 		});
		
        
        button.setOnClickListener(new OnClickListener() {
        @Override
            public void onClick(View v) {
	        
	    		if(!isThigh){
	    			final CheckBox chk_general = (CheckBox) findViewById(R.id.chk_general);
		    		chk_general.setVisibility(View.VISIBLE);
	    			
		    		final CheckBox chk_customknn = (CheckBox) findViewById(R.id.chk_custom_knn);
		    		final CheckBox chk_customjrip = (CheckBox) findViewById(R.id.chk_custom_jrip);
		    		
		    		File f = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+MonitoringWorkerThread.TRAINING_SET);
		    		if(f.exists()){
		    			chk_customknn.setVisibility(View.VISIBLE);
		    		}
		    		
		    		f = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+WekaWorker.FILE_NAME);
		    		if(f.exists()){
		    			chk_customjrip.setVisibility(View.VISIBLE);
		    		}
	    		}
	    		else{
	    			tabHost.getTabWidget().getChildTabViewAt(4).setEnabled(false);
	    		}
	    	 
        		dialog.dismiss();
        	}
        });
        
        dialog.show();
    }


	private void initializeSPINEManager() {
		try {
			SPINEFactory.createSPINEManager(FILE_CONFIG);
			SPINEManager.getLogger().setLevel(Level.ALL);
		} catch (InstantiationException e) {
			Log.e("SPINEManager error", e.getMessage());
		}
	}

	private void initializeComponents() {
		initializeWorkerThread();

		initializeTabHost();
		initializeSetupTabComponents();
		initializeMonitoringTabComponents();
		initializeCustomTabComponents();
	}

	private void initializeWorkerThread() {
		loggerUpdaterWorkerThread = new LoggerUpdaterWorkerThread(
				new LogTabHandler());
		statisticsManagerWorkerThread = new StatisticsManagerWorkerThread(
				new StatisticsTabHandler(), loggerUpdaterWorkerThread);
		
		monitoringWorkerThread = new MonitoringWorkerThread(
				new MonitoringTabHandler(), this, statisticsManagerWorkerThread);
		
		customrWorkerThread = new CustomDataCollectorWorkerThread(new CustomTabHandler());
		tsgeneratorthread = new TSGeneratorThread(new CustomTabHandler());
		wekaWorker = new WekaWorker(new CustomTabHandler());
	}

	private void initializeTabHost() {
		Resources res = getResources();
		tabHost = getTabHost();
		TabHost.TabSpec spec = tabHost.newTabSpec("setup")
				.setIndicator("Setup", res.getDrawable(R.drawable.tab_setup))
				.setContent(R.id.tab_setup);
		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("monitoring")
				.setIndicator("Monitoring",
						res.getDrawable(R.drawable.tab_monitoring))
				.setContent(R.id.tab_monitoring);
		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("statistics")
				.setIndicator("Statistics",
						res.getDrawable(R.drawable.tab_statistics))
				.setContent(R.id.tab_statistics);
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("log")
				.setIndicator("Log", res.getDrawable(R.drawable.tab_log))
				.setContent(R.id.tab_log);
		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("custom")
				.setIndicator("Custom",
						res.getDrawable(R.drawable.ic_tab_monitoring_sel))
				.setContent(R.id.tab_custom);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}

	private void initializeSetupTabComponents() {
		final Button discoveryButton = (Button) this
				.findViewById(R.id.tab_setup_button_discovery);
		final Button startButton = (Button) this
				.findViewById(R.id.tab_setup_button_start);

		discoveryButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new DiscoveryWorkerThread(new SetupTabHandler()))
						.start();
			}
		});

		startButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				tabHost.getTabWidget().getChildTabViewAt(4).setEnabled(false);
				
				new Thread(new StarterWorkerThread(new SetupTabHandler(),
					monitoringWorkerThread, statisticsManagerWorkerThread,
					loggerUpdaterWorkerThread)).start();
				
			}
		});
		
		final CheckBox chk_general = (CheckBox) findViewById(R.id.chk_general);
		final CheckBox chk_customknn = (CheckBox) findViewById(R.id.chk_custom_knn);
		final CheckBox chk_customjrip = (CheckBox) findViewById(R.id.chk_custom_jrip);
		
		chk_general.setOnClickListener(new
				View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				chk_customknn.setChecked(false);
				chk_customjrip.setChecked(false);
			}
		});
		
		chk_customknn.setOnClickListener(new
				View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				customknn = true;
				customWeka = false;
				chk_general.setChecked(false);
				chk_customjrip.setChecked(false);
				monitoringWorkerThread.loadCustomTrainingSet();
			}
		});
		
		chk_customjrip.setOnClickListener(new
				View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				customWeka = true;
				customknn = false;
				chk_general.setChecked(false);
				chk_customknn.setChecked(false);
				monitoringWorkerThread.loadClassifier();
			}
		});

		
	 
	}

	private void initializeMonitoringTabComponents() {
		final Button pauseButton = (Button) this
				.findViewById(R.id.tab_monitoring_button_pause);
		pauseButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				monitoringWorkerThread.pauseMonitoring();
			}
		});
		
		
		final RadioButton rb1 = (RadioButton) this.findViewById(R.id.radioButton1);
		rb1.setOnClickListener(new RadioButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedActivity = 0;
				Log.e("selectedActivity",""+selectedActivity);
			}
		});
		
		final RadioButton rb2 = (RadioButton) this.findViewById(R.id.radioButton2);
		rb2.setOnClickListener(new RadioButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedActivity = 3;
				Log.e("selectedActivity",""+selectedActivity);
			}
		});
		
		final RadioButton rb3 = (RadioButton) this.findViewById(R.id.radioButton3);
		rb3.setOnClickListener(new RadioButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedActivity = 10;
				Log.e("selectedActivity",""+selectedActivity);
			}
		});
		
		final RadioButton rb4 = (RadioButton) this.findViewById(R.id.radioButton4);
		rb4.setOnClickListener(new RadioButton.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedActivity = 20;
				Log.e("selectedActivity",""+selectedActivity);
			}
		});
	}

	private void initializeCustomTabComponents() {
		// TODO Auto-generated method stub
		final Button customBackButton = (Button) this.findViewById(R.id.tab_back_button);
		
		final Button customButton = (Button) this.findViewById(R.id.tab_custom_button);
		((ImageView) findViewById(R.id.tab_custom_image_activity)).setImageResource(R.drawable.mycustom);
		
		customButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(customButton.getText().equals("Setup sensors")){
					
					new Thread(customrWorkerThread).start();

					final Button startButton = (Button) ActivityRecognitionWidget.this.findViewById(R.id.tab_setup_button_start);
					startButton.setEnabled(false);
				}
				
				else if(customButton.getText().equals("Next")){
					customrWorkerThread.setRecording();
				}
				
				else if(customButton.getText().equals("Rec")){
					customrWorkerThread.startRecording();
				}
				
				else if(customButton.getText().equals("Personalizza")){
					customrWorkerThread.stopRecording();
					new Thread(tsgeneratorthread).start();
		
				}
				
				else if(customButton.getText().equals("Esci")){
					finish();
				}
			}
		});
		
		customBackButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(CustomDataCollectorWorkerThread.cont_activity_rec>0)
					CustomDataCollectorWorkerThread.cont_activity_rec--;
				else
					CustomDataCollectorWorkerThread.cont_activity_rec = 0;
				customrWorkerThread.setRecording();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG_ID:
			ProgressDialog progressDialog = new ProgressDialog(
					ActivityRecognitionWidget.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("Please wait...");

			return progressDialog;

		default:
			return null;
		}
	}

	public SetupTabHandler getSetupTabHandler() {
		return new SetupTabHandler();
	}
	public class SetupTabHandler extends Handler {
		public static final String SHOW_PROGRESS_DIALOG = "ShowProgressDialog";
		public static final String SHOW_TOAST = "ShowToast";
		public static final String ENABLE_DISCOVERY_BUTTON = "EnableDiscoveryButton";
		public static final String ENABLE_START_BUTTON = "EnableStartButton";
		public static final String START_TO_STOP_BUTTON = "StartToStopButton";
		public static final String CHANGE_AVAILABLE_NODES_TEXTVIEW = "ChangeAvailableNodesTextView";
		public static final String CHANGE_STATUS_TEXTVIEW = "ChangeActiveStatusTextView";


		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();

			if (bundle.containsKey(SHOW_PROGRESS_DIALOG)) {
				if (bundle.getBoolean(SHOW_PROGRESS_DIALOG))
					showDialog(PROGRESS_DIALOG_ID);
				else
					dismissDialog(PROGRESS_DIALOG_ID);
			}

			if (bundle.containsKey(ENABLE_DISCOVERY_BUTTON)) {
				if (bundle.getBoolean(ENABLE_DISCOVERY_BUTTON))
					((Button) findViewById(R.id.tab_setup_button_discovery))
							.setEnabled(true);
				else
					((Button) findViewById(R.id.tab_setup_button_discovery))
							.setEnabled(false);
			}

			if (bundle.containsKey(ENABLE_START_BUTTON)) {
				if (bundle.getBoolean(ENABLE_START_BUTTON))
					((Button) findViewById(R.id.tab_setup_button_start))
							.setEnabled(true);
				else
					((Button) findViewById(R.id.tab_setup_button_start))
							.setEnabled(false);
			}

			if (bundle.containsKey(START_TO_STOP_BUTTON)) {
				if (bundle.getBoolean(START_TO_STOP_BUTTON)) {
					final Button stopButton = (Button) findViewById(R.id.tab_setup_button_start);

					stopButton.setText(R.string.tab_setup_button_stop);
					stopButton.setOnClickListener(new Button.OnClickListener() {
						@Override
						public void onClick(View v) {
							new Thread(new StopperWorkerThread(
									new SetupTabHandler(),
									monitoringWorkerThread,
									statisticsManagerWorkerThread,
									loggerUpdaterWorkerThread)).start();
						}
					});
				} else {
					final Button startButton = (Button) findViewById(R.id.tab_setup_button_start);

					startButton.setText(R.string.tab_setup_button_start);
					startButton
							.setOnClickListener(new Button.OnClickListener() {
								@Override
								public void onClick(View v) {
									new Thread(new StarterWorkerThread(
											new SetupTabHandler(),
											monitoringWorkerThread,
											statisticsManagerWorkerThread,
											loggerUpdaterWorkerThread)).start();
								}
							});
				}
			}

			if (bundle.containsKey(CHANGE_AVAILABLE_NODES_TEXTVIEW))
				((TextView) findViewById(R.id.tab_setup_text_availablenodes))
						.setText(""
								+ bundle.getInt(CHANGE_AVAILABLE_NODES_TEXTVIEW));

			if (bundle.containsKey(CHANGE_STATUS_TEXTVIEW))
				((TextView) findViewById(R.id.tab_setup_text_status))
						.setText(bundle.getInt(CHANGE_STATUS_TEXTVIEW));

			if (bundle.containsKey(SHOW_TOAST)) {
				Toast toast = Toast.makeText(getApplicationContext(),
						bundle.getString(SHOW_TOAST), Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	}

	public class MonitoringTabHandler extends Handler {
		public static final String ACTIVITY_ID = "ActivityID";
		public static final String ENABLE_PAUSE_BUTTON = "PauseButton";
		public static final String CHANGE_PAUSE_TO_RESUME_BUTTON = "PauseToResumeButton";
		public static final String ACTIVITY_BUFFER = "ActivityBuffer";

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();

			if (bundle.containsKey(ACTIVITY_ID)) {
				int activityId = bundle.getInt(ACTIVITY_ID);
				int[] buffer_in = ActivityRecognitionWidget.activity_buffer;
				
				activityId = metaClassifier(buffer_in);
				Log.e("metaClassifier.activityId",activityId+"");
				
				//int transition = transitionStandigSitting();
				//Log.e("transictionDetector.transition",transition+"");
				
				//activityId = decider(activityId,transition);
				//Log.e("Decider.activityId",activityId+"");
				
				//Log.e("Current State",Automa.getCurrentState()+" ");
				String s = selectedActivity+";"+activityId+"\n";
				try {
					fileWriter.write(s);
					fileWriter.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				switch (activityId) {
				case ClassesCodes.STANDING:
					((ImageView) findViewById(R.id.tab_monitoring_image_activity))
							.setImageResource(R.drawable.tab_monitoring_standing);
					((TextView) findViewById(R.id.tab_monitoring_text_activity))
							.setText(R.string.tab_monitoring_text_standing);
					break;

				case ClassesCodes.STANDING_WALKING:
					((ImageView) findViewById(R.id.tab_monitoring_image_activity))
							.setImageResource(R.drawable.tab_monitoring_walking);
					((TextView) findViewById(R.id.tab_monitoring_text_activity))
							.setText(R.string.tab_monitoring_text_walking);
					break;

				case ClassesCodes.SITTING:
					((ImageView) findViewById(R.id.tab_monitoring_image_activity))
							.setImageResource(R.drawable.tab_monitoring_sitting);
					((TextView) findViewById(R.id.tab_monitoring_text_activity))
							.setText(R.string.tab_monitoring_text_sitting);
					break;
				
				case ClassesCodes.LYING:
					((ImageView) findViewById(R.id.tab_monitoring_image_activity))
							.setImageResource(R.drawable.tab_monitoring_lying);
					((TextView) findViewById(R.id.tab_monitoring_text_activity))
							.setText(R.string.tab_monitoring_text_lying);
					break;

				default: // STAND-BY
					((ImageView) findViewById(R.id.tab_monitoring_image_activity))
							.setImageResource(R.drawable.tab_monitoring_standby);
					((TextView) findViewById(R.id.tab_monitoring_text_activity))
							.setText(R.string.tab_monitoring_text_standby);
				}

			}

			if (bundle.containsKey(CHANGE_PAUSE_TO_RESUME_BUTTON)) {
				if (bundle.getBoolean(CHANGE_PAUSE_TO_RESUME_BUTTON)) {
					((ImageView) findViewById(R.id.tab_monitoring_image_activity))
							.setImageResource(R.drawable.tab_monitoring_pause);
					((TextView) findViewById(R.id.tab_monitoring_text_activity))
							.setText(R.string.tab_monitoring_text_pause);

					final Button resumeButton = ((Button) findViewById(R.id.tab_monitoring_button_pause));
					resumeButton.setText(R.string.tab_monitoring_button_resume);
					resumeButton
							.setOnClickListener(new Button.OnClickListener() {
								@Override
								public void onClick(View v) {
									monitoringWorkerThread.resumeMonitoring();
								}
							});
				} else {
					final Button pauseButton = (Button) findViewById(R.id.tab_monitoring_button_pause);
					pauseButton.setText(R.string.tab_monitoring_button_pause);
					pauseButton
							.setOnClickListener(new Button.OnClickListener() {
								@Override
								public void onClick(View v) {
									monitoringWorkerThread.pauseMonitoring();
								}
							});
				}
			}

			if (bundle.containsKey(ENABLE_PAUSE_BUTTON)) {
				final Button pauseButton = (Button) findViewById(R.id.tab_monitoring_button_pause);

				if (bundle.getBoolean(ENABLE_PAUSE_BUTTON)) {
					pauseButton.setEnabled(true);
				} else {
					pauseButton.setEnabled(false);
				}
			}
		}
	}
	
	private int moda(int[] buffer) {
		// TODO Auto-generated method stub
		int[] f = new int[buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			for (int j = 0; j < buffer.length; j++) {
				if (buffer[j] == buffer[i])
					f[i]++;
			}
		}
		return buffer[indexOfMax(f)];
	}
	
	static int indexOfMax(int[] a) {
		int currIndex = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] > a[currIndex])
				currIndex = i;
		}
		return currIndex;
	}

	public int metaClassifier(int[] buffer_in) {
		// TODO Auto-generated method stub
		int activityId = -1;
		if(buffer_in[0] == buffer_in.length-1)
			activityId = buffer_in[0];
		else{
			activityId = moda(buffer_in);
		}
		return activityId;
	}


	private int decider(int activityId, int transition){
		return Automa.nextState(activityId, transition);
	}
	
	private int transitionStandigSitting(){
		// TODO Auto-generated method stub
		int transition = TransitionsCodes.NO_TRANSITION;
		//Log.e("samples.size()",samples.size()+" ");
		if (samples.size()>=fine) {
			Log.e("samples size:" , samples.size()+ " - Range: "+inizio+" , "+fine);
			
			avg = calcolaMedia();
			max = calcolaMax();
			min = calcolaMin();
			int indexOfMax = indexOf(max) + 1;
			int indexOfMin = indexOf(min) + 1;
			diffmax = ((max - avg) / avg) * 100;
			diffmin = ((avg - min) / avg) * 100;
			diffminmax = ((max - min) / min) * 100;
			if ((diffmax > 2 &&  diffmin > 2)) {
				Log.e("Transiction detected","true");
				if (indexOfMax < indexOfMin) {
					transition = TransitionsCodes.TRANSITION_STANDING_SITTING;
					Log.e("TRANSITION_STANDING_SITTING: ", transition+" ");
				} else {
					transition = TransitionsCodes.TRANSITION_SITTING_STANDING;
					
					Log.e("TRANSITION_SITTING_STANDING: ", transition+" ");
				}
			}

			
			inizio = (fine-shift)-1;
			fine = inizio + windows_size;
		}
		return transition;
	}

	private static int indexOf(double sample) {
		// TODO Auto-generated method stub
		int index = -1; 
		for(int i=inizio;i<fine;i++)
			if(samples.get(i)==sample)
				index = i;
		return index;
	}


	private static long calcolaMin() {
		// TODO Auto-generated method stub
		long curr_min = Long.MAX_VALUE;
		for(int i=inizio;i<fine;i++)
			if(samples.get(i)<curr_min)
				curr_min = samples.get(i);
		return curr_min;
	}


	private static long calcolaMax() {
		// TODO Auto-generated method stub
		long curr_max = Long.MIN_VALUE;
		for(int i=inizio;i<fine;i++)
			if(samples.get(i)>curr_max)
				curr_max = samples.get(i);
		return curr_max;
	}


	private static long calcolaMedia() {
		// TODO Auto-generated method stub
		long curr_avg = 0;
		for(int i=inizio;i<fine;i++)
			curr_avg+=samples.get(i);
		return curr_avg/windows_size;
	}

	public class StatisticsTabHandler extends Handler {
		public static final String CHANGE_STANDING_TEXTVIEW = "StandingTextView";
		public static final String CHANGE_WALKING_TEXTVIEW = "WalkingTextView";
		public static final String CHANGE_SITTING_TEXTVIEW = "SittingTextView";
		public static final String CHANGE_LYING_TEXTVIEW = "LyingTextView";

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();

			if (bundle.containsKey(CHANGE_STANDING_TEXTVIEW)) {
				double stat = bundle.getDouble(CHANGE_STANDING_TEXTVIEW);
				((TextView) findViewById(R.id.tab_statistics_text_standing))
						.setText(stat + "%");
			}

			if (bundle.containsKey(CHANGE_WALKING_TEXTVIEW)) {
				double stat = bundle.getDouble(CHANGE_WALKING_TEXTVIEW);
				((TextView) findViewById(R.id.tab_statistics_text_walking))
						.setText(stat + "%");
			}

			if (bundle.containsKey(CHANGE_SITTING_TEXTVIEW)) {
				double stat = bundle.getDouble(CHANGE_SITTING_TEXTVIEW);
				((TextView) findViewById(R.id.tab_statistics_text_sitting))
						.setText(stat + "%");
			}

			if (bundle.containsKey(CHANGE_LYING_TEXTVIEW)) {
				double stat = bundle.getDouble(CHANGE_LYING_TEXTVIEW);
				((TextView) findViewById(R.id.tab_statistics_text_lying))
						.setText(stat + "%");
			}
		}
	}

	public class LogTabHandler extends Handler {
		public static final String UPDATE_LOG_TEXTAREA = "UpdateLogTextArea";
		public static final String RESET_LOG_TEXTAREA = "ResetLogTextArea";

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();

			if (bundle.containsKey(UPDATE_LOG_TEXTAREA)) {
				String str = bundle.getString(UPDATE_LOG_TEXTAREA);
				EditText log = ((EditText) findViewById(R.id.tab_log_edittext));

				CharSequence oldLog = (CharSequence) log.getText();
				log.setText(str + "\n" + oldLog);
			}

			if (bundle.containsKey(RESET_LOG_TEXTAREA))
				((EditText) findViewById(R.id.tab_log_edittext)).setText("");
		}
	}

	public class CustomTabHandler extends Handler {
		public static final String UPDATE_CUSTOM_TEXTVIEW_CRONOMETER = "Tempo";
		public static final String UPDATE_REC_ACTIVITY = "Activity rec";
		public static final String SHOW_TOAST_CURRENT_ACTIVITY = "ShowToast";
		public static final String FINISH_REC_ACTIVITY = "Finish rec";
		public static final String ENABLED_SETUP_BUTTON = "Enabled setup button";
		public static final String FINISH_TS_GENERATION = "Finish ts generation";
		public static final String WEKA_FINISH = "Weka finish";
		public static final String SHOW_PROGRESS_DIALOG = "ShowProgressDialog";
		public static final String CHANGE_IMAGE_ACTIVITY = "Change Image Of Current Activity";
		public static final String CHANGE_SETUP_BUTTON = "Change Setup Butotn";
		public static final String CONFIGURATION_FINISHED = "Sensori configurati correttamente.";
		public static final String ENABLE_BUTTON = "Enable Custom/Back Button";
		public static final String ENABLE_BACK_BUTTON = "Enable Back Button";
		public static final String SHOW_DIALOG = "Show Dialog";

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();

			if (bundle.containsKey(UPDATE_CUSTOM_TEXTVIEW_CRONOMETER)) {
				String str = bundle.getString(UPDATE_CUSTOM_TEXTVIEW_CRONOMETER);
				TextView cron = ((TextView) findViewById(R.id.tab_custom_text_cronometer));
				cron.setText(str);
			}
			
			if (bundle.containsKey(CONFIGURATION_FINISHED)) {
				String str = bundle.getString(CONFIGURATION_FINISHED);
				Toast toast= Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

			if (bundle.containsKey(CHANGE_SETUP_BUTTON)) {
				Button button = ((Button) findViewById(R.id.tab_custom_button));
				String txt  = bundle.getString(CHANGE_SETUP_BUTTON);
				button.setText(txt);
			}
			
			if (bundle.containsKey(SHOW_TOAST_CURRENT_ACTIVITY)) {
				TextView cron = ((TextView) findViewById(R.id.tab_custom_text_cronometer));
				cron.setText("");
				Toast toast = Toast.makeText(getApplicationContext(),
						bundle.getString(SHOW_TOAST_CURRENT_ACTIVITY),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			
			if (bundle.containsKey(ENABLE_BUTTON)) {
				((Button) findViewById(R.id.tab_back_button)).setEnabled(bundle.getBoolean(ENABLE_BUTTON));
				((Button) findViewById(R.id.tab_custom_button)).setEnabled(bundle.getBoolean(ENABLE_BUTTON));
			}
			
			if (bundle.containsKey(ENABLE_BACK_BUTTON)) {
				((Button) findViewById(R.id.tab_back_button)).setEnabled(bundle.getBoolean(ENABLE_BACK_BUTTON));
			}
			
			if (bundle.containsKey(FINISH_TS_GENERATION)) {
				Toast toast = Toast.makeText(getApplicationContext(),
						bundle.getString(FINISH_TS_GENERATION),
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

			if (bundle.containsKey(WEKA_FINISH)) {
				Toast toast = Toast.makeText(getApplicationContext(),
						bundle.getString(WEKA_FINISH), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			
			if (bundle.containsKey(SHOW_PROGRESS_DIALOG)) {
				if (bundle.getBoolean(SHOW_PROGRESS_DIALOG))
					showDialog(PROGRESS_DIALOG_ID);
				else
					dismissDialog(PROGRESS_DIALOG_ID);
			}

		}
	}

	
}
