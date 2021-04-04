/** 
 *  MonitorLog.java
 *
 *  VERSION: 2021.04.03
 *  AUTHORS: Rae Bouldin
 *
 *  DESCRIPTION:
 *    Contains a collection of all the MonitorRecords in a monitoring session 
 *    along with some information on the user who initiated the session.
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.util.ArrayList;
import java.util.List;

public class MonitorLog {
	
	private List<MonitorRecord> monitorRecords;
	
	private String user_ID;
	private String session_ID;
	private String suggestions;
	private String num_tabs;
	private List<String> all_records;
	
	
	public MonitorLog(String userID, String sessionID, String suggestionsAsJsonArray, String numTabs) {
		this();
		this.user_ID = "\"" + userID + "\"";
		this.session_ID = "\"" + sessionID + "\"";
		this.suggestions = suggestionsAsJsonArray;
		this.num_tabs = "\"" + numTabs + "\"";
	}
	
	
	/**
	 * The default constructor initializes all variables as empty JSON fields.
	 */
	public MonitorLog() {
		monitorRecords = new ArrayList<MonitorRecord>();
		user_ID = "\"\"";
		session_ID = "\"\"";
		suggestions = "\"\"";
		num_tabs = "\"\"";
		all_records = new ArrayList<String>();
	}
	
	
	/**
	 *  Adds the record to this MonitorLog session as a MonitorRecord object 
	 *  and as a JSON formatted String.
	 *  
	 *  @return True if successful; False if the record failed to be appended 
	 *          to the MonitorLog List or to the JSON formatted String List.
	 */
	public boolean appendRecord(MonitorRecord record, String recordAsJson) {
		boolean recordAdded = monitorRecords.add(record);
		boolean jsonAdded = all_records.add(recordAsJson);
		return recordAdded && jsonAdded;
	}
	
	
	/**
	 *  Converts this MonitorLog to a JSON formatted String in the following 
	 *  format:
	 *  
	 *  {
     *    "user_ID":"username",
     *    "session_ID":"0",
     *    "suggestions":[],
     *    "num_tabs":"0",
     *    "avg_cpu_power":"0.0000",
     *    "avg_cpu_usage":"0.0000",
     *    "avg_gpu_usage":"0.0000",
     *    "avg_mem_usage":"0.0000",
     *    "all_records":[
     *        {"cpu_power":"0.0000","cpu_usage":"0.0000","mem_usage":"0.0000","gpu_usage":"0.0000"},
     *        {"cpu_power":"0.0000","cpu_usage":"0.0000","mem_usage":"0.0000","gpu_usage":"0.0000"},
     *        {"cpu_power":"0.0000","cpu_usage":"0.0000","mem_usage":"0.0000","gpu_usage":"0.0000"},
     *        .
     *        .
     *        .
     *    ]
     *  }
     *  
     *  Fields which could not be calculated will be left empty (e.g. "avg_cpu_power":"").
	 *  
	 *  @return This MonitorLog session as a JSON formatted String.
	 */
	public String toJSON() {
		
		String avg_cpu_power = "\"\"";
		double avgCpuPower = averageCpuPower();
		if ( !Double.isNaN(avgCpuPower) ) {
			avg_cpu_power = String.format("\"%f\"", avgCpuPower);
		}
		
		String avg_cpu_usage = "\"\"";
		double avgCpuUsage = averageCpuUsage();
		if ( !Double.isNaN(avgCpuUsage) ) {
			avg_cpu_usage = String.format("\"%f\"", avgCpuUsage);
		}
		
		String avg_gpu_usage = "\"\"";
		double avgGpuUsage = averageGpuUsage();
		if ( !Double.isNaN(avgGpuUsage) ) {
			avg_gpu_usage = String.format("\"%f\"", avgGpuUsage);
		}
		
		String avg_mem_usage = "\"\"";
		double avgMemUsage = averageMemUsage();
		if ( !Double.isNaN(avgMemUsage) ) {
			avg_mem_usage = String.format("\"%f\"", avgMemUsage);
		}
		
		StringBuilder log = new StringBuilder();
		log.append("{");
		log.append("\"user\":").append(user_ID).append(",");
		log.append("\"batch\":").append(session_ID).append(",");
		log.append("\"suggestions\":").append(suggestions).append(",");
		log.append("\"tabs\":").append(num_tabs).append(",");
		log.append("\"avg_cpu_power\":").append(avg_cpu_power).append(",");
		log.append("\"avg_cpu_usage\":").append(avg_cpu_usage).append(",");
		log.append("\"avg_gpu_usage\":").append(avg_gpu_usage).append(",");
		log.append("\"avg_mem_usage\":").append(avg_mem_usage).append(",");
		log.append("\"all_records\":[");
		if (all_records.size() > 0) {
			for (int i = 0; i < all_records.size()-1; i++) {
				log.append(all_records.get(i)).append(",");
			}
			log.append(all_records.get(all_records.size()-1));
		}
		log.append("]");
		log.append("}");
		return log.toString();
		
	}
	
	
	/**
	 *  Calculates the average CPU power by iterating through all elements 
	 *  in monitorRecords.
	 * 
	 *  @return The average CPU power for this session log; or
	 *          NaN if the average couldn't be calculated.
	 */
	private double averageCpuPower() {
		double sum = 0.0;
		for (int i = 0; i < monitorRecords.size(); i++) {
			if (monitorRecords.get(i).cpu_power.isEmpty()) {
				return Double.NaN;
			}
			sum += Double.parseDouble(monitorRecords.get(i).cpu_power);
		}
		return (sum / monitorRecords.size());
	}
	
	
	/**
	 *  Calculates the average CPU usage by iterating through all elements 
	 *  in monitorRecords.
	 * 
	 *  @return The average CPU usage for this session log; or
	 *          NaN if the average couldn't be calculated.
	 */
	private double averageCpuUsage() {
		double sum = 0.0;
		for (int i = 0; i < monitorRecords.size(); i++) {
			if (monitorRecords.get(i).cpu_usage.isEmpty()) {
				return Double.NaN;
			}
			sum += Double.parseDouble(monitorRecords.get(i).cpu_usage);
		}
		return (sum / monitorRecords.size());
	}
	
	
	/**
	 *  Calculates the average GPU usage by iterating through all elements 
	 *  in monitorRecords.
	 * 
	 *  @return The average GPU usage for this session log; or
	 *          NaN if the average couldn't be calculated.
	 */
	private double averageGpuUsage() {
		double sum = 0.0;
		for (int i = 0; i < monitorRecords.size(); i++) {
			if (monitorRecords.get(i).gpu_usage.isEmpty()) {
				return Double.NaN;
			}
			sum += Double.parseDouble(monitorRecords.get(i).gpu_usage);
		}
		return (sum / monitorRecords.size());
	}
	
	
	/**
	 *  Calculates the average memory usage by iterating through all elements 
	 *  in monitorRecords.
	 * 
	 *  @return The average memory usage for this session log; or
	 *          NaN if the average couldn't be calculated.
	 */
	private double averageMemUsage() {
		double sum = 0.0;
		for (int i = 0; i < monitorRecords.size(); i++) {
			if (monitorRecords.get(i).mem_usage.isEmpty()) {
				return Double.NaN;
			}
			sum += Double.parseDouble(monitorRecords.get(i).mem_usage);
		}
		return (sum / monitorRecords.size());
	}

}
