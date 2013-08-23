/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.model;

import java.text.SimpleDateFormat;

/**
 * This class represents a contract associating an app with a service, via a plan,
 * being valid between the start and end date/time.
 *
 */
public class Contract {

	private String _id=null;
	private String _appId=null;
	private String _service=null;
	private String _planId=null;
	private String _start=null;
	private String _end=null;
	private long _startTime=0;
	private long _endTime=0;
	
	private static final SimpleDateFormat DATE_TIME_FORMAT=new SimpleDateFormat("yyyyMMddHHmmss z");
	
	/**
	 * The default constructor.
	 */
	public Contract() {
	}
	
	/**
	 * This method returns the id.
	 * 
	 * @return The id
	 */
	public String getId() {
		return (_id);
	}
	
	/**
	 * This method sets the id.
	 * 
	 * @param id The id
	 */
	public void setId(String id) {
		_id = id;
	}
	
	/**
	 * This method returns the app id.
	 * 
	 * @return The app id
	 */
	public String getAppId() {
		return (_appId);
	}
	
	/**
	 * This method sets the app id.
	 * 
	 * @param appId The app id
	 */
	public void setAppId(String appId) {
		_appId = appId;
	}
	
	/**
	 * This method returns the service.
	 * 
	 * @return The service
	 */
	public String getService() {
		return (_service);
	}
	
	/**
	 * This method sets the service.
	 * 
	 * @param service The service
	 */
	public void setService(String service) {
		_service = service;
	}
		
	/**
	 * This method returns the plan id.
	 * 
	 * @return The plan id
	 */
	public String getPlanId() {
		return (_planId);
	}
	
	/**
	 * This method sets the plan id.
	 * 
	 * @param planId The plan id
	 */
	public void setPlanId(String planId) {
		_planId = planId;
	}
	
	/**
	 * This method sets the start date/time of
	 * the contract.
	 * 
	 * @param start The start date/time
	 */
	public void setStart(String start) {
		_start = start;
		
		try {
			java.util.Date date=DATE_TIME_FORMAT.parse(start);
			
			_startTime = date.getTime();
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse start date/time", e);
		}
	}
	
	/**
	 * This method returns the end date/time of
	 * the contract.
	 * 
	 * @return The start date/time
	 */
	public String getStart() {
		return (_start);
	}
	
	/**
	 * This method returns the start time in milliseconds.
	 * 
	 * @return The start time
	 */
	public long startTime() {
		return (_startTime);
	}
	
	/**
	 * This method sets the end date/time of
	 * the contract.
	 * 
	 * @param end The end date/time
	 */
	public void setEnd(String end) {
		_end = end;
		
		try {
			java.util.Date date=DATE_TIME_FORMAT.parse(end);
			
			_endTime = date.getTime();
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse end date/time", e);
		}
	}
	
	/**
	 * This method returns the end date/time of
	 * the contract.
	 * 
	 * @return The end date/time
	 */
	public String getEnd() {
		return (_end);
	}

	/**
	 * This method returns the end time in milliseconds.
	 * 
	 * @return The end time
	 */
	public long endTime() {
		return (_endTime);
	}
	
}
