from django.http import HttpResponse
import pymysql
import json
from chromeMonitor.models import ResourceRecord

#dataformat: {'user', 'time', 'last_time', 'avg_cpu_power', 'avg_cpu_usage', 'avg_gpu_usage', 'avg_mem_usage'}
def store_data(request):
    if request.method == 'POST':
        #response={}
        user_name = request.POST.get("user")
        cpu_power = request.POST.get("avg_cpu_power")
        cpu_usage = request.POST.get("avg_cpu_usage")
        gpu_usage = request.POST.get("avg_gpu_usage")
        mem_usage = request.POST.get("avg_mem_usage")
        batchNum = request.POST.get("batch")
        tabNum = request.POST.get("tabs")
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

        #print('YES: '+str(json.dumps(res)))

        new_record = ResourceRecord(user = user_name, avg_cpu_power = cpu_power, avg_cpu_usage = cpu_usage, avg_gpu_usage = gpu_usage, avg_mem_usage = mem_usage, batch = batchNum, tabs = tabNum)
        new_record.save()
        return HttpResponse(res)



def gen_sugg(request):
    user_name = request.POST.get('user')
    batchNum = request.POST.get("batch")
    
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

    sug = []

    memSum = memSum / ctr
    gpuSum = gpuSum / ctr
    cpuPSum = cpuPSum / ctr
    cpuUSum = cpuUSum / ctr
    tabSum = tabSum / ctr

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
                    hgpu = 1
                    hcpup = 1
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
    
    #print(str(sug))

    return HttpResponse(sug)
