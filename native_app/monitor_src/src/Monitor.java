/** 
 *  Monitor.java
 *
 *  VERSION: 2021.05.04
 *  AUTHORS: Rae Bouldin, Zinan Guo
 *
 *  DESCRIPTION:
 *    Contains methods to get data about the System resources and Open Hardware
 *    Monitor's Power Sensors.
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public class Monitor {

	// System Information
	private SystemInfo systemInfo;
	private HardwareAbstractionLayer hardware;
	private OperatingSystem os;
	private int systemCpuCount;
	private long systemMemory;

	// Open Hardware Monitor (OHM) WMI Query variables	
	private final String OHM_NAMESPACE = "root\\OpenHardwareMonitor";
	private final String OHM_SENSOR_CLASSNAME = "Sensor";
	public static enum SensorProperty {
		Identifier, Index, InstanceId, Max, Min, Name, Parent, ProcessId, SensorType, Value
	}
	private WmiQuery<SensorProperty> sensorQuery;

	// Predicates to isolate Chrome and OpenHardwareMonitor from system processes.
	private final Predicate<OSProcess> chromeProcessFilter = p -> p.getName().equalsIgnoreCase("Chrome");
	private final Predicate<OSProcess> ohmProcessFilter = p -> p.getName().equalsIgnoreCase("OpenHardwareMonitor");

	
	public Monitor() {
		// Initialize System Information
		this.systemInfo = new SystemInfo();
		this.hardware = systemInfo.getHardware();
		this.os = systemInfo.getOperatingSystem();
		this.systemCpuCount = hardware.getProcessor().getLogicalProcessorCount();
		this.systemMemory = hardware.getMemory().getTotal();
		
		// Initialize Open Hardware Monitor variables
		this.sensorQuery = new WmiQuery<SensorProperty>(OHM_NAMESPACE, this.OHM_SENSOR_CLASSNAME, SensorProperty.class);
		Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED);
	}
	
	
	/**
	 *  Attempt to start an instance of Open Hardware Monitor (checks if one is
	 *  already running first, to avoid duplicated processes).
	 *  
	 *  @return The number of OpenHardwareMonitor.exe instances currently running.
	 */
	public int startOpenHardwareMonitor() throws IOException {
		List<OSProcess> ohmProcList = os.getProcesses(ohmProcessFilter, null, 0);
		if (ohmProcList.size() < 1) {
			Runtime.getRuntime().exec("run_OHM.bat");
		}
		while (ohmProcList.size() < 1) {
			ohmProcList = os.getProcesses(ohmProcessFilter, null, 0);
		}
		return ohmProcList.size();
	}
	
	
	/**
	 * Return the max cpu power value for this monitoring session.
	 */
	public double getMaxCpuPower() {
		float maxCpuPackage = 0;
		WmiResult<SensorProperty> result = sensorQuery.execute();
		for (int i = 0; i < result.getResultCount(); i++) {
			if (result.getValue(SensorProperty.SensorType, i).equals("Power")) {
				if (result.getValue(SensorProperty.Name, i).equals("CPU Package")) {
					maxCpuPackage = (float) result.getValue(SensorProperty.Max, i);
					break;
				}
			}
		}
		return (double) maxCpuPackage;
	}
	
	
	/**
	 * Return the system integrated gpu value for this monitoring session.
	 */
	public float getSystemIntegratedGpu() {
		float totalIntegratedGpu = 0;
		WmiResult<SensorProperty> result = sensorQuery.execute();
		for (int i = 0; i < result.getResultCount(); i++) {
			if (result.getValue(SensorProperty.SensorType, i).equals("Power")) {
				if (result.getValue(SensorProperty.Name, i).equals("CPU Graphics")) {
					totalIntegratedGpu = (float) result.getValue(SensorProperty.Max, i);
					break;
				}
			}
		}
		return totalIntegratedGpu;
	}

	
	/**
	 *  Return a MonitorRecord as a data point recorded by the monitor in the 
	 *  instant this method was called.
	 */
	public MonitorRecord getData() {
		
		double systemCpuPower = Double.NaN;
		double systemCpuUsage = Double.NaN;
		double systemMemUsage = Double.NaN;
		double systemGpuUsage = Double.NaN;
		double chromeCpuPower = Double.NaN;
		double chromeCpuUsage = Double.NaN;
		double chromeMemUsage = Double.NaN;
		double chromeGpuUsage = Double.NaN;

		// Get data from OpenHardwareMonitor
		WmiResult<SensorProperty> sensors = sensorQuery.execute();
		for (int i = 0; i < sensors.getResultCount(); i++) {
			if (sensors.getValue(SensorProperty.SensorType, i).equals("Power")) {
				// CPU Package gets the sum of the CPU power usage in Watts.
				if (sensors.getValue(SensorProperty.Name, i).equals("CPU Package")) {
					systemCpuPower = (float) sensors.getValue(SensorProperty.Value, i);
				}
				// Calculate GPU usage from integrated GPU.
				else if (sensors.getValue(SensorProperty.Name, i).equals("CPU Graphics")) {
					chromeGpuUsage = 100d * (float) sensors.getValue(SensorProperty.Value, i) / getSystemIntegratedGpu();
				}
			} else if (sensors.getValue(SensorProperty.SensorType, i).equals("Load")) {
				// CPU Total Load
				if (sensors.getValue(SensorProperty.Name, i).equals("CPU Total")) {
					systemCpuUsage = (float) sensors.getValue(SensorProperty.Value, i);
				}
				// Memory Load
				else if (sensors.getValue(SensorProperty.Name, i).equals("Memory")) {
					systemMemUsage = (float) sensors.getValue(SensorProperty.Value, i);
				}
			}

		}

		// Get data from the system
		double totalCpu = 0, totalMem = 0;
		List<OSProcess> chromeProcList = os.getProcesses(chromeProcessFilter, null, 0);
		for (int i = 0; i < chromeProcList.size(); i++) {
			OSProcess p = chromeProcList.get(i);
			totalCpu += p.getProcessCpuLoadBetweenTicks(p);
			totalMem += p.getResidentSetSize();
		}
		chromeCpuUsage = 100d * totalCpu / systemCpuCount;
		chromeMemUsage = 100d * totalMem / systemMemory;

		// Return a record with the recorded data.
		return new MonitorRecord(systemCpuPower, systemCpuUsage, systemGpuUsage, systemMemUsage, chromeCpuPower,
				chromeCpuUsage, chromeGpuUsage, chromeMemUsage);
		
	}
	
	
}
