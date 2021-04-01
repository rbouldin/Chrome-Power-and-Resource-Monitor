/** 
 *  MonitorRecord.java
 *
 *  VERSION: 2021.03.30
 *  AUTHORS: Rae Bouldin, Zinan Guo
 *
 *  DESCRIPTION:
 *    ...
 * 
 *  (Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech)
 */
package src;

public class MonitorRecord {
	
	public String cpu_power;
	public String cpu_usage;
	public String mem_usage;
	public String gpu_usage;

	public MonitorRecord(double cpuPower, double cpuUsage, double memUsage, double gpuUsage) {
		
		this.cpu_power = "";
		if ( !Double.isNaN(cpuPower) ) {
			this.cpu_power = String.format("%.4f", cpuPower);
		}
		
		this.cpu_usage = "";
		if ( !Double.isNaN(cpuUsage) ) {
			this.cpu_usage = String.format("%.4f", cpuUsage);
		}
		
		this.mem_usage = "";
		if ( !Double.isNaN(memUsage) ) {
			this.mem_usage = String.format("%.4f", memUsage);
		}
		
		this.gpu_usage = "";
		if ( !Double.isNaN(gpuUsage) ) {
			this.gpu_usage = String.format("%.4f", gpuUsage);
		}
		
	}
	
}