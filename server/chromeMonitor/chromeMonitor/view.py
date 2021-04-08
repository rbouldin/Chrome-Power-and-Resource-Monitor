from django.http import HttpResponse
import pymysql
import json
from chromeMonitor.models import ResourceRecord

#dataformat: {'user', 'time', 'last_time', 'avg_cpu_power', 'avg_cpu_usage', 'avg_gpu_usage', 'avg_mem_usage'}
def store_data(request):
    if request.method == 'POST':
        nuldat = 0
        err = []

        data = json.loads(request.body) #formerly request.POST
        
        if data.get("user"):
            user_name = data.get("user")
        else:
            user_name = None
        if user_name is None:
            return HttpResponse("PARSE ERROR: request.POST.get('user')")
        
        if data.get("avg_cpu_power"):
            cpu_power = data.get("avg_cpu_power")
        else:
            cpu_power = None
        if cpu_power is None:
            nuldat = 1
            er = "PARSE ERROR: request.POST.get('avg_cpu_power') "
            err += [er]
        
        if data.get("avg_cpu_usage"):
            cpu_usage = data.get("avg_cpu_usage")
        else:
            cpu_usage = None
        if cpu_usage is None:
            nuldat = 1
            er = "PARSE ERROR: request.POST.get('avg_cpu_usage') "
            err += [er]
        
        if data.get("avg_gpu_usage"):
            gpu_usage = data.get("avg_gpu_usage")
        else:
            gpu_usage = None
        if gpu_usage is None:
            nuldat = 1
            er = "PARSE ERROR: request.POST.get('avg_gpu_usage') "
            err += [er]
        
        if data.get("avg_mem_usage"):
            mem_usage = data.get("avg_mem_usage")
        else:
            mem_usage = None
        if mem_usage is None:
            nuldat = 1
            er = "PARSE ERROR: request.POST.get('avg_mem_usage') "
            err += [er]
        
        if data.get("batch"):
            batchNum = data.get("batch")
        else:
            batchNum = None
        if batchNum is None:
            return HttpResponse("PARSE ERROR: request.POST.get('batch')")
        
        if data.get("tabs"):
            tabNum = data.get("tabs")
        else:
            tabNum = None
        if tabNum is None:
            nuldat = 1
            er = "PARSE ERROR: request.POST.get('tabs') "
            err += [er]
        
        #response = user_name + ' : CPU Power = ' + cpu_power + '%, CPU Usage = ' + cpu_usage + '%, GPU Usage = ' + gpu_usage + '%, MEM Usage = ' + mem_usage + '%, Batch = ' + batchNum + ' tabs: ' + tabNum
        
        res = {
                'user': user_name,
                'avg_cpu_power': cpu_power,
                'avg_cpu_usage': cpu_usage,
                'avg_gpu_usage': gpu_usage,
                'avg_mem_usage': mem_usage,
                'batch': batchNum,
                'tabs': tabNum
                }

        new_record = ResourceRecord(user = user_name, avg_cpu_power = cpu_power, avg_cpu_usage = cpu_usage, avg_gpu_usage = gpu_usage, avg_mem_usage = mem_usage, batch = batchNum, tabs = tabNum)
        new_record.save()

        if nuldat == 0:
            return HttpResponse("OK")
        else:
            return HttpResponse(err)


def gen_sugg(request):
    data = json.loads(request.body)
    #user_name = data.get('user')
    batchNum = data.get("batch")

    bat = str(batchNum)
    memSum = 0
    gpuSum = 0
    cpuPSum = 0
    cpuUSum = 0
    tabSum = 0
    ctr = 0

    maxmem = 0
    maxgpu = 0
    maxcpup = 0
    maxcpuu = 0
    maxtab = 0
    mintab = 0
    firstTab = 0

    n = 0

    for p in ResourceRecord.objects.raw('SELECT * FROM chrome_data.chromeMonitor_resourcerecord WHERE batch='+bat):
        if p.avg_mem_usage and p.avg_gpu_usage and p.tabs and p.avg_cpu_usage and p.avg_cpu_power:
            thisTab = p.tabs

            if ctr == 0:
                firstTab = p.tabs
        
            if memSum > maxmem:
                maxmem = memSum
            if gpuSum > maxgpu:
                maxgpu = gpuSum
            if cpuPSum > maxcpup:
                maxcpup = cpuPSum
            if cpuUSum > maxcpuu:
                maxcpuu = cpuUSum
        
            if thisTab >= maxtab:
                maxtab = thisTab
                if n == 0:
                    mintab = maxtab
                    n = 1
            if thisTab <= mintab:
                mintab = thisTab

            memSum = memSum + p.avg_mem_usage
            gpuSum = gpuSum + p.avg_gpu_usage
            cpuPSum = cpuPSum + p.avg_cpu_power
            cpuUSum = cpuUSum + p.avg_cpu_usage
            tabSum = tabSum + p.tabs
            ctr = ctr + 1

        else:
            return  HttpResponse("NULL value, no suggestion to give")

    sug = []

    memSum = memSum / ctr
    gpuSum = gpuSum / ctr
    cpuPSum = cpuPSum / ctr
    cpuUSum = cpuUSum / ctr
    tabSum = tabSum / ctr

    hmem = 0
    hmem = 0
    hmem = 0
    hgpu = 0
    hcpup = 0
    hcpuu = 0
    
    none = 0

    mmem = 0
    mgpu = 0
    mcpup = 0
    mcpuu = 0

    if memSum >= 0 and memSum <= 33: 
        if gpuSum >= 0 and gpuSum <= 33:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    none = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mcpup = 1
                elif cpuPSum > 66:
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    hcpuu = 1
                    hcpup = 1
        elif gpuSum > 33 and gpuSum <= 66:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mgpu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mgpu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mgpu = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mgpu = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mgpu = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mgpu = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    mgpu = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    mgpu = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    mgpu = 1
                    hcpuu = 1
                    hcpup = 1
        elif gpuSum > 66:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hgpu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hgpu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hgpu = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hgpu = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hgpu = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hgpu = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    hgpu = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    hgpu = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    hgpu = 1
                    hcpuu = 1
                    hcpup = 1
    elif memSum > 33 and memSum <= 66:
        if gpuSum >= 0 and gpuSum <= 33:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mmem = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mmem = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    mmem = 1
                    hcpuu = 1
                    hcpup = 1
        elif gpuSum > 33 and gpuSum <= 66:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    mgpu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    mgpu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mmem = 1
                    mgpu = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    mgpu = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    mgpu = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mmem = 1
                    mgpu = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    mgpu = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    mgpu = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    mmem = 1
                    mgpu = 1
                    hcpuu = 1
                    hcpup = 1
        elif gpuSum > 66:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    hgpu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    hgpu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mmem = 1
                    hgpu = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    hgpu = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    hgpu = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    mmem = 1
                    hgpu = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    mmem = 1
                    hgpu = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    mmem = 1
                    hgpu = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    mmem = 1
                    hgpu = 1
                    hcpuu = 1
                    hcpup = 1
    elif memSum > 66:
        if gpuSum >= 0 and gpuSum <= 33:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hmem = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hmem = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    hmem = 1
                    hcpuu = 1
                    hcpup = 1
        elif gpuSum > 33 and gpuSum <= 66:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    mgpu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    mgpu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hmem = 1
                    mgpu = 1
                    hcpup = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    mgpu = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    mgpu = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hmem = 1
                    mgpu = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    mgpu = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    mgpu = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    hmem = 1
                    mgpu = 1
                    hcpuu = 1
                    hcpup = 1
        elif gpuSum > 66:
            if cpuUSum >= 0 and cpuUSum <= 33:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    hgpu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    hgpu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hmem = 1
            elif cpuUSum > 33 and cpuUSum <= 66:
                if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    hgpu = 1
                    mcpuu = 1
                elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    hgpu = 1
                    mcpuu = 1
                    mcpup = 1
                elif cpuPSum > 66:
                    hmem = 1
                    hgpu = 1
                    mcpuu = 1
                    hcpup = 1
            elif cpuUSum > 66:
                 if cpuPSum >= 0 and cpuPSum <= 33:
                    hmem = 1
                    hgpu = 1
                    hcpuu = 1
                 elif cpuPSum > 33 and cpuPSum <= 66:
                    hmem = 1
                    hgpu = 1
                    hcpuu = 1
                    mcpup = 1
                 elif cpuPSum > 66:
                    hmem = 1
                    hgpu = 1
                    hcpuu = 1
                    hcpup = 1

    #print('hmem: '+str(hmem)+' hgpu: '+str(hgpu)+' hcpuu: '+str(hcpuu)+' hcpup: '+str(hcpup)+' mmem: '+str(mmem)+' mgpu: '+str(mgpu)+' mcpuu: '+str(mcpuu)+' mcpup: '+str(mcpup))

    if hmem == 1:
        ans = 'High Memory Usage. Close down unneeded tabs.'
        sug += [ans]

    if hgpu == 1:
        ans = 'High GPU Usage. Close any tabs containing videos or images.'
        sug += [ans]

    if hcpuu == 1:
        ans ='High Computer Resource usage. End any tabs with running processes.'
        sug += [ans]

    if hcpup == 1:
        ans = 'High Computer Power Usage. Close down tabs that are using a lot of resources.'
        sug += [ans]

    if maxtab != mintab:
        if maxtab > firstTab:
            #opened tabs during session
            if hmem == 1:
                ans = 'Tabs were opened during monitoring session and memory usage increased. Close down any newly opened tabs.'
                sug += [ans]
            else:
                ans = 'Tabs were opened during monitoring session but had no effect on memory usage. The opened tab did not effect the memory usage.'
                sug += [ans]
        elif mintab < firstTab:
            #closed tabs during session
            if hmem == 0:
                ans = 'Tabs were closed during monitoring session and memory usage decreased. Good job!'
                sug += [ans]
            else:
                ans = 'Tabs were closed during monitoring session but did not help lower memory usage. Try closing down tabs that are using more data.'
                sug += [ans]
    else:
        if tabSum >= 5:
            ans = 'You have a decent amount of tabs opened. Do you need them all right now?'
            sug += [ans]
    
    return HttpResponse(sug)
