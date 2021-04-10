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

public class NativeMonitor {
	//util class
	private SystemInfo systemInfo;
	private OperatingSystem os;
	private HardwareAbstractionLayer hardware;

	private WmiQuery<SensorProperty> wmiQuery;

	//system info that does not change over time
	private long systemMemory;
	private int logicalCpuCount;
	private float totalIntegratedGpu = 0;

	//query result that stores the latest query result
	//Unit %
	private double chromeCpu = 0;
	private double chromeMem = 0;
	private double SystemIntegratedGpu = 0;

	//Unit Watt
	private float cpuPower = 0;

	//helper variables
	Predicate<OSProcess> chromeFilter = p -> p.getName().equalsIgnoreCase("Chrome");

	public NativeMonitor() {
		//initiate OSHI
		systemInfo = new SystemInfo();
		os = systemInfo.getOperatingSystem();
		hardware = systemInfo.getHardware();

		//initiate the connect to WMI data
		wmiQuery = new WmiQuery<SensorProperty>("root\\OpenHardwareMonitor", "Sensor", SensorProperty.class);
		Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED);

		//get the static data
		systemMemory = hardware.getMemory().getTotal();
		logicalCpuCount = hardware.getProcessor().getLogicalProcessorCount();

		WmiResult<SensorProperty> result = wmiQuery.execute();
		for (int i = 0; i < result.getResultCount(); i++) {
			if (result.getValue(SensorProperty.SensorType, i).equals("Power")
					&& result.getValue(SensorProperty.Name, i).equals("CPU Graphics")) {
				totalIntegratedGpu = (float) result.getValue(SensorProperty.Value, i);
			}
		}
	}

	public void updateData() {
		updatePowerData();
		updateResourceData();
	}

	private void updatePowerData() {
		WmiResult<SensorProperty> result = wmiQuery.execute();
		for (int i = 0; i < result.getResultCount(); i++) {
			if (result.getValue(SensorProperty.SensorType, i).equals("Power")) {
				if (result.getValue(SensorProperty.Name, i).equals("CPU Package")) {
					cpuPower = (float) result.getValue(SensorProperty.Value, i);
				} else if (result.getValue(SensorProperty.Name, i).equals("CPU Graphics")) {
					SystemIntegratedGpu = 100d * (float) result.getValue(SensorProperty.Value, i) / totalIntegratedGpu;
				}
			}
		}
	}

	private void updateResourceData() {
		double totalCpu = 0, totalMem = 0;
		List<OSProcess> chromeProcList = os.getProcesses(chromeFilter, null, 0);
		for (int i = 0; i < chromeProcList.size(); i++) {
			OSProcess p = chromeProcList.get(i);
			totalCpu += p.getProcessCpuLoadBetweenTicks(p);
			totalMem += p.getResidentSetSize();
		}
		chromeCpu = 100d * totalCpu / logicalCpuCount;
		chromeMem = 100d * totalMem / systemMemory;
	}

	public double getCPUTotal() {
		return chromeCpu;
	}

	public double getMemoryTotal() {
		return chromeMem;
	}

	public double getGPU() {
		return SystemIntegratedGpu;
	}

	public float getCpuPower() {
		return cpuPower;
	}
}
