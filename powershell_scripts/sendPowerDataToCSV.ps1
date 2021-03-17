Get-Wmiobject Sensor -namespace root\OpenHardwareMonitor |
    Where-Object { $_.SensorType -eq "Power" } |
    Sort-Object -Property InstanceId |
    Select Name,
    @{Name="SensorType";Expression={[String]$_.SensorType}},
    @{Name="Value (W)";Expression={[String]$_.Value}},
    @{Name="ProcessID";Expression={[String]$_.ProcessId}} |
Export-CSV -Path output_POW.csv