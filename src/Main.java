package src;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		while (true) {
			TimeUnit.SECONDS.sleep(1);
			ResourceMonitor.updateResourceData();
			Gson gson = new Gson();
			ResourceRecord newRecord = new ResourceRecord(ResourceMonitor.getMemoryTotal(),
					ResourceMonitor.getCPUTotal());
			//convert object to JSON using GSON Lib
			String resourceJson = gson.toJson(newRecord);
			//send the formatted JSON to chrome
			sendJSONMessage(resourceJson);
		}
	}

	public static void sendJSONMessage(String message) throws IOException {
		System.out.write(getBytes(message.length()));
		System.out.write(message.getBytes("UTF-8"));
		System.out.flush();
	}

	public static byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (length & 0xFF);
		bytes[1] = (byte) ((length >> 8) & 0xFF);
		bytes[2] = (byte) ((length >> 16) & 0xFF);
		bytes[3] = (byte) ((length >> 24) & 0xFF);
		return bytes;
	}

	/*public static void original_main(String[] args) throws IOException {
	
		System.out.println("\n----- PowerMonitor Testing -----\n");
		PowerMonitor.updatePowerData();
	
		double package_val = PowerMonitor.getPowerSensorValue("CPU Package");
		System.out.println("CPU Pckg Power (W) = " + package_val);
		double cores_val = PowerMonitor.getPowerSensorValue("CPU Cores");
		System.out.println("CPU Core Power (W) = " + cores_val);
		double graphics_val = PowerMonitor.getPowerSensorValue("CPU Graphics");
		System.out.println("CPU Grph Power (W) = " + graphics_val);
		double dram_val = PowerMonitor.getPowerSensorValue("CPU DRAM");
		System.out.println("CPU DRAM Power (W) = " + dram_val);
	
		double sum = cores_val + graphics_val + dram_val;
		System.out.println("\n" + sum);
		System.out.println();
	
		System.out.println("\n----- ResourceMonitor Testing -----\n");
		ResourceMonitor.updateResourceData();
	
		System.out.println("Mem Total (?) = " + ResourceMonitor.getMemoryTotal());
		System.out.println("CPU Total (%) = " + ResourceMonitor.getCPUTotal());
		System.out.println("GPU Memory (%) = " + ResourceMonitor.getGPUMemory());
	
	}*/
}