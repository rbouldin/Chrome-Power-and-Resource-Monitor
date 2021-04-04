::
:: Run Options: "monitor.jar [-records int] [-native on] [-native off] [-server on] [-server off] [-clean]"
::
:: -records int       An integer number of records you'd like to record (the number of times the 
::                    monitor will loop). This is 60 by default. Each loop should take ~2-3 seconds.
::
:: -native [on/off]   Turn native messaging on or off. It is ON by default.
::
:: -server [on/off]   Turn server messaging on or off. It is OFF by default.
::
:: -clean             Deletes any output files created by previous runs then exits.
::
@echo off
java -jar "monitor.jar" -records 60 -native on -server off %*