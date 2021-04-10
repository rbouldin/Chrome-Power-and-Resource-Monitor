package src;

public class MainTest {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("started");
		NativeMonitor monitor = new NativeMonitor();
		for (int i = 0; i < 20; i++) {
			monitor.updateData();
			System.out.print(monitor.getCPUTotal());
			System.out.print("  ");
			System.out.print(monitor.getMemoryTotal());
			System.out.print("  ");
			System.out.println(monitor.getGPU());
		}

	}
}
