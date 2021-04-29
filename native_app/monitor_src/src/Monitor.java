/** 
 *  Monitor.java
 *
 *  VERSION: 2021.04.11
 *  AUTHORS: Rae Bouldin, Zinan Guo
 *
 *  DESCRIPTION:
 *    Contains methods to get data about the System resources and Open Hardware
 *    Monitor's Power Sensors.
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

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

	// util class
	private SystemInfo systemInfo;
	private OperatingSystem os;
	private HardwareAbstractionLayer hardware;

	// system info that does not change over time
	private long systemMemory;
	private int systemCpuCount;
	private float totalIntegratedGpu = 0;
	private float maxCpuPackage = 0;

	// Open Hardware Monitor WMI Query variables
	private final String powerSensorNameSpace = "root\\OpenHardwareMonitor";
	private final String powerSensorClassName = "Sensor";

	private enum SensorProperty {
		Identifier, Index, InstanceId, Max, Min, Name, Parent, ProcessId, SensorType, Value
	}

	private WmiQuery<SensorProperty> powerSensorQuery;

	//	// query result that stores the latest query result
	//	// Unit %
	//	private double chromeCpu = 0;
	//	private double chromeMem = 0;
	//	private double SystemIntegratedGpu = 0;
	//
	//	// Unit Watt
	//	private float cpuPower = 0;

	// helper variables
	Predicate<OSProcess> chromeFilter = p -> p.getName().equalsIgnoreCase("Chrome");

	public Monitor() {
		// initiate OSHI
		systemInfo = new SystemInfo();
		os = systemInfo.getOperatingSystem();
		hardware = systemInfo.getHardware();

		// initiate the connect to WMI data
		powerSensorQuery = new WmiQuery<SensorProperty>(powerSensorNameSpace, powerSensorClassName,
				SensorProperty.class);
		Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED);

		// get the static data
		systemMemory = hardware.getMemory().getTotal();
		systemCpuCount = hardware.getProcessor().getLogicalProcessorCount();

		WmiResult<SensorProperty> result = powerSensorQuery.execute();
		for (int i = 0; i < result.getResultCount(); i++) {
			if (result.getValue(SensorProperty.SensorType, i).equals("Power")) {
				if (result.getValue(SensorProperty.Name, i).equals("CPU Graphics")) {
					totalIntegratedGpu = (float) result.getValue(SensorProperty.Max, i);
				} else if (result.getValue(SensorProperty.Name, i).equals("CPU Package")) {
					maxCpuPackage = (float) result.getValue(SensorProperty.Max, i);
				}
			}
		}
	}

	public MonitorRecord getData() {
		double systemCpuPower = Double.NaN;
		double systemCpuUsage = Double.NaN;
		double systemMemUsage = Double.NaN;
		double systemGpuUsage = Double.NaN;
		double chromeCpuPower = Double.NaN;
		double chromeCpuUsage = Double.NaN;
		double chromeMemUsage = Double.NaN;
		double chromeGpuUsage = Double.NaN;

		// Get current power data
		WmiResult<SensorProperty> sensors = powerSensorQuery.execute();
		for (int i = 0; i < sensors.getResultCount(); i++) {
			//			Object sensorType = sensors.getValue(SensorProperty.SensorType, i);
			if (sensors.getValue(SensorProperty.SensorType, i).equals("Power")) {
				// CPU Package gets the sum of the CPU power usage in Watts.
				if (sensors.getValue(SensorProperty.Name, i).equals("CPU Package")) {
					systemCpuPower = (float) sensors.getValue(SensorProperty.Value, i);
				}
				// Calculate GPU usage from integrated GPU.
				else if (sensors.getValue(SensorProperty.Name, i).equals("CPU Graphics")) {
					chromeGpuUsage = 100d * (float) sensors.getValue(SensorProperty.Value, i) / totalIntegratedGpu;
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

		// Get current resource data.
		double totalCpu = 0, totalMem = 0;
		List<OSProcess> chromeProcList = os.getProcesses(chromeFilter, null, 0);
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

	public SystemInformation getSysinfo() {
		return new SystemInformation((double) maxCpuPackage);
	}
}
