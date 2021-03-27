# controller param = 0
function getMemory {
	(Get-Process chrome | Measure-Object WorkingSet -sum).sum / 1GB
}

# controller param = 1
function getMemoryLoop {
	# This param can be implemented to set a certin amount of times to loop 
	# Param([int]$times)
	# $curr = 0
	# while ($curr -lt $times) {
	while(1) {
        (Get-Process chrome | Measure-Object WorkingSet -sum).sum / 1GB
		# curr++
	}
}

# controller param = 2
function getCPU {
    $totalRam = (Get-CimInstance Win32_PhysicalMemory | Measure-Object -Property capacity -Sum).Sum
    # $date = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $cpuTime = (Get-Counter '\Processor(_Total)\% Processor Time').CounterSamples.CookedValue
    # $availMem = (Get-Counter '\Memory\Available MBytes').CounterSamples.CookedValue
	# cpu.Time is the only value we are concerned with but I left the rest of the stuff here in case it was needed. It can be removed it not
    # $date + ' > CPU: ' + $cpuTime.ToString("#,0.000") + '%, Avail. Mem.: ' + $availMem.ToString("N0") + 'MB (' + (104857600 * $availMem / $totalRam).ToString("#,0.0") + '%)'
	$cpuTime.ToString("#,0.000")
	# This can be changed to set how fast/slow this method returns
	Start-Sleep -s 2
}

# controller param = 3
function getCPULoop {
	# This param can be implemented to set a certin amount of times to loop 
	# Param([int]$times)
	# $curr = 0
	$totalRam = (Get-CimInstance Win32_PhysicalMemory | Measure-Object -Property capacity -Sum).Sum
	# while ($curr -lt $times) {
 	while($true) {
     	# $date = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
     	$cpuTime = (Get-Counter '\Processor(_Total)\% Processor Time').CounterSamples.CookedValue
     	# $availMem = (Get-Counter '\Memory\Available MBytes').CounterSamples.CookedValue
		# cpu.Time is the only value we are concerned with but I left the rest of the stuff here in case it was needed. It can be removed it not
     	# $date + ' > CPU: ' + $cpuTime.ToString("#,0.000") + '%, Avail. Mem.: ' + $availMem.ToString("N0") + 'MB (' + (104857600 * $availMem / $totalRam).ToString("#,0.0") + '%)'
     	# This can be changed to set how fast/slow this method returns
		$cpuTime.ToString("#,0.000")
		Start-Sleep -s 2
		#curr++
 	}	
}

function getGPU {
	Get-Wmiobject Sensor -namespace root\OpenHardwareMonitor |
    Where-Object { $_.SensorType -eq "Load" } |
    Where-Object { $_.Name -like "GPU*" } |
    Sort-Object -Property Value -Descending |
    Select Name,
    @{Name="Value";Expression={[String]$_.Value}}
}

function getGPUMemory {
	Get-Wmiobject Sensor -namespace root\OpenHardwareMonitor |
    Where-Object { $_.SensorType -eq "Load" } |
    Where-Object { $_.Name -eq "GPU Memory" } |
    Select Name,
    @{Name="Value";Expression={[String]$_.Value}}
}

# For use later in the project
function indvTabs {
    $ProcessNames = @('chrome.exe')
 	Get-WmiObject Win32_Process -Computer 'localhost' |
     Where-Object { $ProcessNames -contains $_.Name } |
     Sort-Object -Property ws -Descending | `
     Select processname,
     @{Name="Mem Usage(MB)";Expression={[math]::round($_.ws / 1mb)}},
     @{Name="ProcessID";Expression={[String]$_.ProcessID}},
     @{Name="UserID";Expression={$_.getowner().user}}

}

function controller {
	Param([int]$met)
	if (0 -eq $met) {
		getMemory
	}
	elseif (1 -eq $met) {
		getMemoryLoop
	}
	elseif (2 -eq $met) {
		getCPU
	}
	elseif (3 -eq $met) {
		getCPULoop
	}
	elseif (4 -eq $met) {
		getGPUMemory
	}
}


$memTotal = (controller -met 0)
$cpuTotal = (controller -met 2)
$gpuMem = (controller -met 4)

$csvOutput = @(
	[pscustomobject]@{
		Name = 'Memory Total'
		Value = $memTotal
	}
	[pscustomobject]@{
		Name = 'CPU Total'
		Value = $cpuTotal
	}
	[PSCustomObject]@{
		Name = $gpuMem.Name
		Value = $gpuMem.Value
	}
)
$csvOutput | Export-CSV -Path output_RES.csv