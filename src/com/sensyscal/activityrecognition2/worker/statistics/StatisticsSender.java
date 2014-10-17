package com.sensyscal.activityrecognition2.worker.statistics;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class StatisticsSender {
	private final static String UPGRADE_STATISTICS_URL = "http://www.getyourweb.it/inviadati.aspx";
	private final static String RESET_STATISTICS_URL = "http://www.getyourweb.it/resetdati.aspx";

	public final static int STANDING = 0;
	public final static int WALKING = 1;
	public final static int SITTING = 2;
	public final static int LYIING = 3;

	public static void updateStatistics(final int activityId, final double[] stats) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URI targetUri = new URI(UPGRADE_STATISTICS_URL + getParameter(activityId, stats));
					HttpGet request = new HttpGet(targetUri);

					new DefaultHttpClient().execute(request, discoveryResponseHandler);
				} catch (Exception e) {
				}
			}
		}).start();
	}

	private static String getParameter(int activityId, double[] stats) {
		return "?postura=" + activityId + "&timestamp=" + System.currentTimeMillis() + "&latitudine=39%2022.080%20N&longitudine=16%2013.516%20E" + "&ip=" + stats[STANDING] + "&se=" + stats[SITTING] + "&ca="
				+ stats[WALKING] + "&sd=" + stats[LYIING];
	}

	private static final ResponseHandler<String> discoveryResponseHandler = new ResponseHandler<String>() {
		@Override
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			return "";
		}
	};

	public static void resetStatistics() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URI targetUri = new URI(RESET_STATISTICS_URL);
					HttpGet request = new HttpGet(targetUri);

					new DefaultHttpClient().execute(request, discoveryResponseHandler);
				} catch (Exception e) {
				}
			}
		}).start();
	}
}
