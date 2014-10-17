package com.sensyscal.activityrecognition2.cloud.updater;

import java.sql.Timestamp;

import org.w3c.dom.Document;

public class SUAReport{
	weka.core.Debug.Timestamp received;
	Document report;
	
	public weka.core.Debug.Timestamp getReceived() {
		return received;
	}
	
	public void setReceived(weka.core.Debug.Timestamp timestamp) {
		this.received = timestamp;
	}
	
	public Document getReport() {
		return report;
	}
	
	public void setReport(Document report) {
		this.report = report;
	}
}