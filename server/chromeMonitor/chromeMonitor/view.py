from django.http import HttpResponse
import pymysql
import json
from chromeMonitor.models import ResourceRecord

#dataformat: {'user', 'time', 'last_time', 'avg_cpu_power', 'avg_cpu_usage', 'avg_gpu_usage', 'avg_mem_usage'}
def store_data(request):
    if request.method == 'POST':
        response={}
        user_name = request.POST.get("user")
        cpu_power = request.POST.get("avg_cpu_power")
        cpu_usage = request.POST.get("avg_cpu_usage")
        gpu_usage = request.POST.get("avg_gpu_usage")
        mem_usage = request.POST.get("avg_mem_usage")
        batchNum = request.POST.get("batch")
        response = user_name + ' : CPU Power = ' + cpu_power + '%, CPU Usage = ' + cpu_usage + '%, GPU Usage = ' + gpu_usage + '%, MEM Usage = ' + mem_usage + '%, Batch = ' + batchNum
        new_record = ResourceRecord(user = user_name, avg_cpu_power = cpu_power, avg_cpu_usage = cpu_usage, avg_gpu_usage = gpu_usage, avg_mem_usage = mem_usage, batch = batchNum)
        new_record.save()
        return HttpResponse(response)
