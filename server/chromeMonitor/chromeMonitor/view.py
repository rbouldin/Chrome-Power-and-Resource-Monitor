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
        response['info'] = user_name + ' : ' + cpu_power
        new_record = ResourceRecord(user = user_name)
        new_record.save()
        return HttpResponse(response)