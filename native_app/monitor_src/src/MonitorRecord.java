/** 
 *  MonitorRecord.java
 *
 *  VERSION: 2021.04.01
 *  AUTHORS: Rae Bouldin, Zinan Guo
 *
 *  DESCRIPTION:
 *    ...
 * 
 *  (Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech)
 */
package src;

import com.google.gson.Gson;

public class MonitorRecord {
	
	public String cpu_power;
	public String cpu_usage;
	public String mem_usage;
	public String gpu_usage;
	
	public MonitorRecord(double cpuPower, double cpuUsage, double memUsage, double gpuUsage) {
		
		this();
		
		if ( !Double.isNaN(cpuPower) ) {
			this.cpu_power = String.format("%.4f", cpuPower);
		}
		if ( !Double.isNaN(cpuUsage) ) {
			this.cpu_usage = String.format("%.4f", cpuUsage);
		}
		if ( !Double.isNaN(memUsage) ) {
			this.mem_usage = String.format("%.4f", memUsage);
		}
		if ( !Double.isNaN(gpuUsage) ) {
			this.gpu_usage = String.format("%.4f", gpuUsage);
		}
		
	}
	
	public MonitorRecord() {
		this.cpu_power = "";
		this.cpu_usage = "";
		this.mem_usage = "";
		this.gpu_usage = "";
	}
	
	public String toJSON() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
}