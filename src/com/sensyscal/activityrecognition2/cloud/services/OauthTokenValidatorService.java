package com.sensyscal.activityrecognition2.cloud.services;

import java.io.IOException;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidgetHandler;
import com.sensyscal.activityrecognition2.cloud.utility.ActivityRecognitionApp;

public class OauthTokenValidatorService extends Service {
	Message msg = Message.obtain();
	ActivityRecognitionWidgetHandler ARWH;
	protected static String TAG = "OauthTokenValidatorService";
	
	final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email";
	AccountManager accountManager = AccountManager.get(ActivityRecognitionWidget.getInstance());
	public static ActivityRecognitionWidget ARW;
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public static void updareUserToken(ActivityRecognitionWidget activity) throws OperationCanceledException, AuthenticatorException, IOException
	{
		
	
		String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email";
		
		AccountManager accountManager = AccountManager.get(ActivityRecognitionWidget.getInstance());
		
		accountManager.invalidateAuthToken("com.google", ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);								
		
		Account[] accounts = accountManager.getAccountsByType("com.google");
		
    	AccountManagerFuture<Bundle> amf = accountManager.getAuthToken(accounts[0], AUTH_TOKEN_TYPE, null, activity, null, null); 

    	Bundle bundle = amf.getResult();

		ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL = (String) bundle.get(AccountManager.KEY_ACCOUNT_NAME);
		ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		activity.runTest(ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL);
		activity.runTest("hahahaha");
	}
	
	
	@Override
	public void onStart(Intent intent, int flag/*,int startId*/) {
		Log.i("UMASS: ","ALAA I AM HERE AGAIN");
        new AsyncTask<String, String, String[]>(){

			@Override
			protected String[] doInBackground(String... params) {
				
	        	Log.i(TAG, "Started");

				while(true)
				{
					ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = false;
					
					while(!ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID)
					{
					
						try{
							//this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());
							//System.out.println("INVALIDO IL TOKEN: ");
							//System.out.println("TOKEN: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
													
							//accountManager.invalidateAuthToken("com.google", ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
	
							//this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());
							//ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = true;
							
							//***********************************************************************//
							//TODO: I CODICE QUI SOTTO, UTILE PER TESTARE SE UN TOKEN E' VALIDO O NO 
							//		DOVREBBE ESSERE FUNZIONANTE UNA VOLTA CORRETTO L'ERRORE CON RESTLET ED SSL
	
							if(ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN == null)
								this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());
	
							if(this.isTokenValid())
							{
								ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = true;

								msg.obj=ActivityRecognitionWidgetHandler.LOGIN_OK;//OUR HANDLER STUFF							
						    	ARWH.sendMessage(msg);
								
								
					        	Log.i(TAG, "Token is Valid: App logged");
							} else {
					        	Log.i(TAG, "Token is NOT Valid: Invalidate Token and Request for a new one");
					        	
								accountManager.invalidateAuthToken("com.google", ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
								this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());
							}
							
							/*
							System.out.println("VERIFICO IL TOKEN: ");
							ClientResource clientResource = new ClientResource(
									"https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="
									+ ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
							//clientResource.setHostRef("https://www.googleapis.com/");
							
							try {
								Representation rep = clientResource.get();
								JsonRepresentation json = new JsonRepresentation(rep);
								JSONObject object = json.getJsonObject();
								String user = (String) object.get("email");
								if (user == null)
								{
									System.out.println("TOKEN NON PIU' VALIDO: LO AGGIORNO !!");
									accountManager.invalidateAuthToken(AUTH_TOKEN_TYPE, ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
									ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = false;
									//this.updateValues(getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance()));
									this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());
									ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = true;
								}else {
									System.out.println("TOKEN ANCORA VALIDO: LO TENGO !!");						
									return null;
								}
							
								
							} catch (Exception e) { e.printStackTrace(); this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance()); 
							*/   
						} catch (Exception e){ 
				        	Log.e(TAG, "Request Token Error: sleep for 30 seconds");

				        	try {
								Thread.sleep(30*1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
					
	                // Have a rest for 15 minutes
		        	Log.i(TAG, "sleep for 5 minutes");
					try {
						Thread.sleep(5*60*1000);
					} catch (InterruptedException e) {}
				}
			}
			/*
			protected void updateValues(String[] userParams) {
				if(userParams == null ) return;
				
				ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL = userParams[0];
				ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN = userParams[1];
				
				System.out.println("EMAIL: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL);
				System.out.println("TOKEN: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
				
            	//ActivityRecognitionWidget.getInstance().refreshListView();
            	
				//Toast.makeText(ActivityRecognitionWidget.getInstance(), "Oauth Token Updated", Toast.LENGTH_LONG).show();
			}
			*/
			
		    public void getUserTokenAndUpdateAppValues(Activity activity) throws OperationCanceledException, AuthenticatorException, IOException 
		    {
				//AccountManager accountManager = AccountManager.get(ActivityRecognitionWidget.getInstance());
				Account[] accounts = accountManager.getAccountsByType("com.google");
				
		    	AccountManagerFuture<Bundle> amf = accountManager.getAuthToken(accounts[0], AUTH_TOKEN_TYPE, null, activity, null, null); 

		    	Bundle bundle = amf.getResult();
	            //String[] userParanms = new String[2];

	            //userParanms[0] = (String) bundle.get(AccountManager.KEY_ACCOUNT_NAME);
	            //userParanms[1] = bundle.getString(AccountManager.KEY_AUTHTOKEN);
	            
				ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL = (String) bundle.get(AccountManager.KEY_ACCOUNT_NAME);
				ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN = bundle.getString(AccountManager.KEY_AUTHTOKEN);
	            
				System.out.println("EMAIL: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL);
				System.out.println("TOKEN: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
		    }
		    
		    public boolean isTokenValid()
		    {
				ClientResource clientResource = new ClientResource(
						"https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="
						+ ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
				
				try {
					Representation rep = clientResource.get();
					JsonRepresentation json = new JsonRepresentation(rep);
					JSONObject object = json.getJsonObject();
					String user = (String) object.get("email");
					if (user == null)
					{
						/*
						System.out.println("TOKEN NON PIU' VALIDO: LO AGGIORNO !!");
						accountManager.invalidateAuthToken(AUTH_TOKEN_TYPE, ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
						ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = false;
						//this.updateValues(getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance()));
						this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());
						ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID = true;
						*/
						return false;
					}else {
						/*
						System.out.println("TOKEN ANCORA VALIDO: LO TENGO !!");						
						return null;
						*/
						return true;
					}
				
					
				} /*catch(ResourceException re){
					
		        	Log.e(TAG, "Token Validation Error: sleep for 1 minute");
		        	re.printStackTrace();
		        	
					// Have a rest for 1 minutes
					try {
						Thread.sleep(1*60*1000);
					} catch (InterruptedException e) {}
					
					return this.isTokenValid();
				}*/ catch (Exception e) { 
					return false; } /*e.printStackTrace(); this.getUserTokenAndUpdateAppValues(ActivityRecognitionWidget.getInstance());*/	
		    }
		    
        }.execute("");
        //return START_STICKY;
	}
}
