package src;

public class SystemInformation {
	private double maxCpuPower;

	public SystemInformation(double maxPower) {
		maxCpuPower = maxPower;
	}

	public String toJSON() {

		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{");
		jsonBuilder.append("\"max_cpu_power\":").append(getJSONValue(maxCpuPower));
		jsonBuilder.append("}");
		return jsonBuilder.toString();

	}

	private String getJSONValue(double value) {

		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("\"");
		if (!Double.isNaN(value)) {
			String jsonVal = String.format("%.4f", value);
			jsonBuilder.append(jsonVal);
		}
		jsonBuilder.append("\"");
		return jsonBuilder.toString();

	}

}
