/** 
 *  MonitorRecord.java
 *
 *  VERSION: 2021.04.11
 *  AUTHORS: Rae Bouldin
 *
 *  DESCRIPTION:
 *    Represents a single data point in a monitoring log.
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

public class MonitorRecord {
	
	private double systemCpuPower;
	private double systemCpuUsage;
	private double systemGpuUsage;
	private double systemMemUsage;
	
	private double chromeCpuPower;
	private double chromeCpuUsage;
	private double chromeGpuUsage;
	private double chromeMemUsage;
	
	public MonitorRecord(double systemCpuPower, double systemCpuUsage, double systemGpuUsage, double systemMemUsage,
			double chromeCpuPower, double chromeCpuUsage, double chromeGpuUsage, double chromeMemUsage) {
		
		this.systemCpuPower = systemCpuPower;
		this.systemCpuUsage = systemCpuUsage;
		this.systemGpuUsage = systemGpuUsage;
		this.systemMemUsage = systemMemUsage;
		
		this.chromeCpuPower = chromeCpuPower;
		this.chromeCpuUsage = chromeCpuUsage;
		this.chromeGpuUsage = chromeGpuUsage;
		this.chromeMemUsage = chromeMemUsage;
		
	}
	
	public MonitorRecord(double chromeCpuPower, double chromeCpuUsage, double chromeGpuUsage, double chromeMemUsage) {
		
		this.systemCpuPower = Double.NaN;
		this.systemCpuUsage = Double.NaN;
		this.systemGpuUsage = Double.NaN;
		this.systemMemUsage = Double.NaN;
		
		this.chromeCpuPower = chromeCpuPower;
		this.chromeCpuUsage = chromeCpuUsage;
		this.chromeGpuUsage = chromeGpuUsage;
		this.chromeMemUsage = chromeMemUsage;
		
	}
	
	public MonitorRecord() {
		
		this.systemCpuPower = Double.NaN;
		this.systemCpuUsage = Double.NaN;
		this.systemGpuUsage = Double.NaN;
		this.systemMemUsage = Double.NaN;
		this.chromeCpuPower = Double.NaN;
		this.chromeCpuUsage = Double.NaN;
		this.chromeGpuUsage = Double.NaN;
		this.chromeMemUsage = Double.NaN;
		
	}
	
	public double getSystemCpuPower() {
		return systemCpuPower;
	}
	
	public double getSystemCpuUsage() {
		return systemCpuUsage;
	}
	
	public double getSystemMemUsage() {
		return systemMemUsage;
	}
	
	public double getSystemGpuUsage() {
		return systemGpuUsage;
	}
	
	public double getChromeCpuPower() {
		return chromeCpuPower;
	}
	
	public double getChromeCpuUsage() {
		return chromeCpuUsage;
	}
	
	public double getChromeMemUsage() {
		return chromeMemUsage;
	}
	
	public double getChromeGpuUsage() {
		return chromeGpuUsage;
	}
	
	/**
	 *  {
	 *    "cpu_power":"",
	 *    "cpu_usage":"",
	 *    "gpu_usage":"",
	 *    "mem_usage":"",
	 *    "system_cpu_power":"",
	 *    "system_cpu_usage":"",
	 *    "system_gpu_usage":"",
	 *    "system_mem_usage":""
	 *  }
	 */
	public String toJSON() {
		
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{");
		jsonBuilder.append("\"cpu_power\":").append(toJSONValue(chromeCpuPower)).append(",");
		jsonBuilder.append("\"cpu_usage\":").append(toJSONValue(chromeCpuUsage)).append(",");
		jsonBuilder.append("\"gpu_usage\":").append(toJSONValue(chromeGpuUsage)).append(",");
		jsonBuilder.append("\"mem_usage\":").append(toJSONValue(chromeMemUsage)).append(",");
		jsonBuilder.append("\"system_cpu_power\":").append(toJSONValue(systemCpuPower)).append(",");
		jsonBuilder.append("\"system_cpu_usage\":").append(toJSONValue(systemCpuUsage)).append(",");
		jsonBuilder.append("\"system_gpu_usage\":").append(toJSONValue(systemGpuUsage)).append(",");
		jsonBuilder.append("\"system_mem_usage\":").append(toJSONValue(systemMemUsage));
		jsonBuilder.append("}");
		return jsonBuilder.toString();
		
	}
	
	/**
	 *  {
	 *    "from":"chrome",
	 *    "cpu_power":"",
	 *    "cpu_usage":"",
	 *    "gpu_usage":"",
	 *    "mem_usage":""
	 *  }
	 */
	public String chromeDataToJSON() {
		return typeToJSON("chrome", chromeCpuPower, chromeCpuUsage, chromeGpuUsage, chromeMemUsage);
	}
	
	/**
	 *  {
	 *    "from":"system",
	 *    "cpu_power":"",
	 *    "cpu_usage":"",
	 *    "gpu_usage":"",
	 *    "mem_usage":""
	 *  }
	 */
	public String systemDataToJSON() {
		return typeToJSON("system", systemCpuPower, systemCpuUsage, systemGpuUsage, systemMemUsage);
	}
	
	/**
	 *  {
	 *    "from":"fromType",
	 *    "cpu_power":"",
	 *    "cpu_usage":"",
	 *    "gpu_usage":"",
	 *    "mem_usage":""
	 *  }
	 */
	private String typeToJSON(String fromType, double cpuPower, double cpuUsage, double gpuUsage, double memUsage) {
		
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{");
		jsonBuilder.append("\"from\":").append(toJSONValue(fromType)).append(",");
		jsonBuilder.append("\"cpu_power\":").append(toJSONValue(cpuPower)).append(",");
		jsonBuilder.append("\"cpu_usage\":").append(toJSONValue(cpuUsage)).append(",");
		jsonBuilder.append("\"gpu_usage\":").append(toJSONValue(gpuUsage)).append(",");
		jsonBuilder.append("\"mem_usage\":").append(toJSONValue(memUsage));
		jsonBuilder.append("}");
		return jsonBuilder.toString();
		
	}
	
	private String toJSONValue(double value) {
		
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("\"");
		if ( !Double.isNaN(value) ) {
			String jsonVal = String.format("%.4f", value);
			jsonBuilder.append(jsonVal);
		}
		jsonBuilder.append("\"");
		return jsonBuilder.toString();
		
	}
	
	private String toJSONValue(String value) {
		
		if (value == null || value.length() < 1) {
			return "\"\"";
		}
		StringBuilder jsonBuilder = new StringBuilder();
		if (value.length() > 0 && value.charAt(0) != '\"') {
			jsonBuilder.append("\"");
		}
		jsonBuilder.append(value);
		if (value.length() > 0 && value.charAt(value.length()-1) != '\"') {
			jsonBuilder.append("\"");
		}
		return jsonBuilder.toString();
		
	}
	
}