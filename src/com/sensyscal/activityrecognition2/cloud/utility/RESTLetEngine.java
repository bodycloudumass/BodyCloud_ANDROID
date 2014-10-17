package com.sensyscal.activityrecognition2.cloud.utility;

import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;

import android.util.Log;

import com.bodycloud.lib.rest.api.DeviceResource;
//import com.kdcloud.lib.rest.api.ModalitiesResource;

public final class RESTLetEngine {

	public static ClientResource getClientResource(String URI)
	{
		ClientResource cr = new ClientResource(URI);
		cr.setRequestEntityBuffering(true);
		
		Log.i("NOSTRO DEBUG", "sono nel getClientRes quesot � il token " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
		Log.i("NOSTRO DEBUG", "sono nel getClientRes quesot � URI " + URI);
		
		cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC,
				ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL,
				//"oauth",
				ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);

		return cr;
	}

	/*
	public static Long createNewDataset(String name, String description) throws Exception
	{
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + "/data");

		UserDataResource resource = cr.wrap(UserDataResource.class);
		
		try{
			return resource.createDataset(new Dataset(name, description));
       	} catch(Exception e){ e.printStackTrace(); }
		
		return null;
	}
	
	public static ArrayList<Dataset> getAllDatasets()
	{
		System.out.println("RESTlet Richiesta");
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + "/data");
		
		UserDataResource resource = cr.wrap(UserDataResource.class);
		
		try{
			return resource.listDataset();
       	} catch(Exception e){ e.printStackTrace(); }
		
		return new ArrayList<Dataset>();
	}
	*/

	/*public static List<ModalitySpecification> getAllModalities() throws Exception
	{
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + ModalityResource.URI);
		ModalitiesResource resource = cr.wrap(ModalitiesResource.class);

		return resource.listModalities().asList();
	}*/

	/*
	public static void uploadDatasetValues(Long idDataset, LinkedList<DataRow> dataRows) throws Exception
	{
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + "/data/" + idDataset);
		
		DatasetResource resource = cr.wrap(DatasetResource.class);
		resource.uploadData(dataRows);
	}
	*/
	/*
	public static void requestProcess(Long idDataset) throws Exception
	{
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + "/process/ecg/" + idDataset);
		cr.setRequestEntityBuffering(true);
		
		SchedulerResource resource = cr.wrap(SchedulerResource.class);
		Long processId = resource.requestProcess(); //registerProcess(GCMRegistrar.getRegistrationId(MainActivity.getInstance()));

	}
	*/
	
	public static void clientRegistration(String regId) throws Exception
	{
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + "/device");
		DeviceResource resource = cr.wrap(DeviceResource.class);
		
		resource.register(regId);
		Log.i("NOSTRO DEBUG", "sono nel clientRegistration" + regId);
	}
	
	public static void clientUnregistration(String regId) throws Exception
	{
		ClientResource cr = RESTLetEngine.getClientResource(ActivityRecognitionApp.SERVER_URL + "/device");
		DeviceResource resource = cr.wrap(DeviceResource.class);
		
		resource.unregister(regId);
	}
	
	class Exxx extends Exception{
		
	}
}
