from chromeMonitor.models import ResourceRecord
from django.core.exceptions import ObjectDoesNotExist
from django.http import HttpResponse
import json
import pymysql

#  store_data(request):
#  request should be an HTTP POST in JSON format:
#  {
#     "batch":"1119110537",
#     "user":"alice",
#     "session_ID":"21119110537",
#     "suggestions":[],
#     "tabs":"4",
#     "avg_cpu_power":"",
#     "avg_cpu_usage":"33.990396",
#     "avg_gpu_usage":"9.278687",
#     "avg_mem_usage":"5.967924",
#     "all_records":[...]
#  }
def store_data(request):
    if request.method == 'POST':
        # Load JSON string from the POST request.
        data = json.loads(request.body)
        # An array to hold any warnings (e.g. about parse errors).
        warn = []
        # Keep track of how many fields are invalid or empty.
        nuldat = 0
        nuldat_max = 4

        # ---------------------------------------------------------------------
        # Read the JSON fields and mark errors and warnings where appropriate.
        # ---------------------------------------------------------------------

        batchNum = data.get('batch')
        if not batchNum:
            # We can't store records without a batch number because they cannot be retrieved later.
            return HttpResponse('{"Status":"ERROR","Errors":["PARSE ERROR: data.get(\'batch\')"]}')
        
        user_name = data.get('user')
        if not user_name:
            # We can't store records without a user name because they cannot be retrieved later.
            return HttpResponse('{"Status":"ERROR","Errors":["PARSE ERROR: data.get(\'user\')"]}')

        tabNum = data.get('tabs')
        if not tabNum:
            tabNum = None
            nuldat += 1
            warn += ['"Warning: could not parse data.get(\'tabs\')"']
        
        cpu_power = data.get('avg_cpu_power')
        if not cpu_power:
            cpu_power = None
            nuldat += 1
            warn += ['"Warning: could not parse data.get(\'avg_cpu_power\')"']
        
        cpu_usage = data.get('avg_cpu_usage')
        if not cpu_usage:
            cpu_usage = None
            nuldat += 1
            warn += ['"Warning: could not parse data.get(\'avg_cpu_usage\')"']
        
        gpu_usage = data.get('avg_gpu_usage')
        if not gpu_usage:
            gpu_usage = None
            nuldat += 1
            warn += ['"Warning: could not parse data.get(\'avg_gpu_usage\')"']
        
        mem_usage = data.get('avg_mem_usage')
        if not mem_usage:
            mem_usage = None
            nuldat += 1
            warn += ['"Warning: could not parse data.get(\'avg_mem_usage\')"']

        # Store valid record in the database.
        if nuldat < nuldat_max:
            new_record = ResourceRecord(user = user_name, avg_cpu_power = cpu_power, avg_cpu_usage = cpu_usage, avg_gpu_usage = gpu_usage, avg_mem_usage = mem_usage, batch = batchNum, tabs = tabNum)
            new_record.save()
            # Send HTTP response back to the native app indicating that the POST request was processed.
            return HttpResponse('{"Status":"OK","Warnings":[' + ','.join(warn) + ']}')
        else:
            return HttpResponse('{"Status":"ERROR","Errors":["PARSE ERROR: Too many empty or invalid fields in the POST request."]}')
    
    else:
        return HttpResponse('{"Status":"ERROR","Errors":["HTTP ERROR: Expected a HTTP POST request."]}')



#  get_suggestions(request):
#  request should be an HTTP GET in JSON format:
#  {
#     "batch":"1119110537",
#     "user":"alice"
#  }
def get_suggestions(request):
    # Load JSON string from the GET request.
    requestJSON = json.loads(request.body)
    # Read the JSON fields.
    userName = str(requestJSON.get('user'))
    batchNum = str(requestJSON.get('batch'))
    # Try to retrieve the record correlating to the request from the database.
    try:
        batch = ResourceRecord.objects.get(user=userName, batch=batchNum)
    except ObjectDoesNotExist:
        return HttpResponse('ERROR: ObjectDoesNotExist for ' + str(requestJSON))

    # Define array to hold a list of the generated suggestions and a string to 
    # hold a message about those suggestions.
    suggestions = []
    suggestion_msg = '""'
    # Define bools to mark where high power/resource usage has been detected.
    cpu_usage_is_high = False
    gpu_usage_is_high = False
    mem_usage_is_high = False
    tab_usage_is_high = False
    # Define numbers to identify high power/resource usage.
    med_cpu_usage_bound = 15.0
    high_cpu_usage_bound = 30.0
    med_gpu_usage_bound = 10.0
    high_gpu_usage_bound = 20.0
    med_mem_usage_bound = 15.0
    high_mem_usage_bound = 30.0
    med_tab_usage_bound = 5
    high_tab_usage_bound = 10
    # Define usage levels to send back in an HTTP Response
    # (e.g. "avg_cpu_usage":"high", "avg_gpu_usage":"low")
    avg_cpu_usage_level = '""'
    avg_gpu_usage_level = '""'
    avg_mem_usage_level = '""'
    avg_tab_usage_level = '""'

    # Analyze the database record to mark high power/resource usage.
    if batch.avg_cpu_usage:
        if (batch.avg_cpu_usage >= high_cpu_usage_bound):
            cpu_usage_is_high = True
            avg_cpu_usage_level = '"high"'
        elif(batch.avg_cpu_usage < med_cpu_usage_bound):
            avg_cpu_usage_level = '"low"'
        else:
            avg_cpu_usage_level = '"medium"'
    if batch.avg_gpu_usage:
        if (batch.avg_gpu_usage >= high_gpu_usage_bound):
            gpu_usage_is_high = True
            avg_gpu_usage_level = '"high"'
        elif (batch.avg_gpu_usage < med_gpu_usage_bound):
            avg_gpu_usage_level = '"low"'
        else:
            avg_gpu_usage_level = '"medium"'
    if batch.avg_mem_usage:
        if (batch.avg_mem_usage >= high_mem_usage_bound):
            mem_usage_is_high = True
            avg_mem_usage_level = '"high"'
        elif(batch.avg_mem_usage < med_mem_usage_bound):
            avg_mem_usage_level = '"low"'
        else:
            avg_mem_usage_level = '"medium"'
    if batch.tabs:
        if (batch.tabs >= high_tab_usage_bound):
            tab_usage_is_high = True
            avg_tab_usage_level = '"high"'
        elif(batch.tabs < med_tab_usage_bound):
            avg_tab_usage_level = '"low"'
        else:
            avg_tab_usage_level = '"medium"'

    # Set which suggestions to send back in an HTTP Response
    if tab_usage_is_high:
        suggestions += ['"useTabDiscarder"']
        suggestions += ['"useTabSuspender"']
        suggestion_msg = '"It looks like you\'re using a lot of tabs! Installing an extension to discard or suspend unused tabs may help reduce power and resource consumption."'
    else:
        if cpu_usage_is_high and gpu_usage_is_high and mem_usage_is_high:
            suggestions += ['"manageExtensions"']
            suggestion_msg = '"Resource usage is high for your CPU, GPU, and Memory. Take a look at the extensions you have installed to see if any could be causing resource consumption to spike."'
        elif cpu_usage_is_high and gpu_usage_is_high:
            suggestions += ['"useAdBlocker"']
            suggestion_msg = '"Resource usage is high for your CPU and GPU. Visiting websites that load a lot of ads may be one cause. Installing an extension to block these ads from loading may help to reduce power and resource consumption."'
        elif cpu_usage_is_high and mem_usage_is_high:
            suggestions += ['"manageExtensions"']
            suggestion_msg = '"Resource usage is high for your CPU and Memory. Take a look at the extensions you have installed to see if any could be causing resource consumption to spike."'
        elif gpu_usage_is_high and mem_usage_is_high:
            suggestions += ['"manageExtensions"']
            suggestions += ['"useAdBlocker"']
            suggestion_msg = '"Resource usage is high for your GPU and Memory. The culprit may be websites that load a lot of advertisements or one or more extensions you have enabled. Installing an extension to block ads from loading may help to reduce power and resource consumption. Also, take a look at the extensions you have installed to see if any could be causing resource consumption to spike."'
        elif cpu_usage_is_high:
            suggestions += ['"clearBrowsingData"']
            suggestions += ['"disableHardwareAcceleration"']
            suggestions += ['"useAdBlocker"']
            suggestion_msg = '"Your CPU usage is high. Clearing your browsing data or using an ad blocker may help reduce power and resource consumption. In some cases, disabling Chrome\'s Hardware Acceleration feature may also help."'
        elif gpu_usage_is_high:
            suggestions += ['"checkMemLeak"']
            suggestion_msg = '"Your GPU usage is high. Open Chrome\'s Task Manager to see if any tasks are leaking memory."'
        elif mem_usage_is_high:
            suggestions += ['"manageExtensions"']
            suggestions += ['"useAdBlocker"']
            suggestion_msg = '"Your Memory usage is high. The culprit may be websites that load a lot of advertisements or one or more extensions you have enabled. Installing an extension to block ads from loading may help to reduce power and resource consumption. Also, take a look at the extensions you have installed to see if any could be causing resource consumption to spike."'
        else:
            suggestion_msg = '"No suggestions: everything looks good!"'

    # Return an HTTP Response with a JSON array of suggestions.
    levelsJSON = '"avg_cpu_usage":' + avg_cpu_usage_level + ',"avg_gpu_usage":' + avg_gpu_usage_level + ',"avg_mem_usage":' + avg_mem_usage_level + ',"avg_tabs":' + avg_tab_usage_level
    suggestionsJSON = '"suggestions":[' + ','.join(suggestions) + ']'
    suggestionMsgJSON = '"suggestion_msg":' + suggestion_msg
    return HttpResponse('{' + levelsJSON + ',' + suggestionsJSON + ',' + suggestionMsgJSON + '}')



def get_all_suggestions(request):
    if request.method == 'POST':
        reqestJSON = json.loads(request.body)
        return HttpResponse("OK")



# def gen_sugg(request):
#     data = json.loads(request.body)
#     #user_name = data.get('user')
#     batchNum = data.get("batch")

#     bat = str(batchNum)
#     memSum = 0
#     gpuSum = 0
#     cpuPSum = 0
#     cpuUSum = 0
#     tabSum = 0
#     ctr = 0

#     maxmem = 0
#     maxgpu = 0
#     maxcpup = 0
#     maxcpuu = 0
#     maxtab = 0
#     mintab = 0
#     firstTab = 0

#     n = 0

#     for p in ResourceRecord.objects.raw('SELECT * FROM chrome_data.chromeMonitor_resourcerecord WHERE batch='+bat):
#         if p.avg_mem_usage and p.avg_gpu_usage and p.tabs and p.avg_cpu_usage and p.avg_cpu_power:
#             thisTab = p.tabs

#             if ctr == 0:
#                 firstTab = p.tabs
        
#             if memSum > maxmem:
#                 maxmem = memSum
#             if gpuSum > maxgpu:
#                 maxgpu = gpuSum
#             if cpuPSum > maxcpup:
#                 maxcpup = cpuPSum
#             if cpuUSum > maxcpuu:
#                 maxcpuu = cpuUSum
        
#             if thisTab >= maxtab:
#                 maxtab = thisTab
#                 if n == 0:
#                     mintab = maxtab
#                     n = 1
#             if thisTab <= mintab:
#                 mintab = thisTab

#             memSum = memSum + p.avg_mem_usage
#             gpuSum = gpuSum + p.avg_gpu_usage
#             cpuPSum = cpuPSum + p.avg_cpu_power
#             cpuUSum = cpuUSum + p.avg_cpu_usage
#             tabSum = tabSum + p.tabs
#             ctr = ctr + 1

#         else:
#             return  HttpResponse("NULL value, no suggestion to give")

#     sug = []

#     memSum = memSum / ctr
#     gpuSum = gpuSum / ctr
#     cpuPSum = cpuPSum / ctr
#     cpuUSum = cpuUSum / ctr
#     tabSum = tabSum / ctr

#     hmem = 0
#     hmem = 0
#     hmem = 0
#     hgpu = 0
#     hcpup = 0
#     hcpuu = 0
    
#     none = 0

#     mmem = 0
#     mgpu = 0
#     mcpup = 0
#     mcpuu = 0

#     if memSum >= 0 and memSum <= 33: 
#         if gpuSum >= 0 and gpuSum <= 33:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     none = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     hcpuu = 1
#                     hcpup = 1
#         elif gpuSum > 33 and gpuSum <= 66:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mgpu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mgpu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mgpu = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mgpu = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mgpu = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mgpu = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     mgpu = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     mgpu = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     mgpu = 1
#                     hcpuu = 1
#                     hcpup = 1
#         elif gpuSum > 66:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hgpu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hgpu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hgpu = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hgpu = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hgpu = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hgpu = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     hgpu = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     hgpu = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     hgpu = 1
#                     hcpuu = 1
#                     hcpup = 1
#     elif memSum > 33 and memSum <= 66:
#         if gpuSum >= 0 and gpuSum <= 33:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mmem = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mmem = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     mmem = 1
#                     hcpuu = 1
#                     hcpup = 1
#         elif gpuSum > 33 and gpuSum <= 66:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     mgpu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     mgpu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mmem = 1
#                     mgpu = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     mgpu = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     mgpu = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mmem = 1
#                     mgpu = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     mgpu = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     mgpu = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     mmem = 1
#                     mgpu = 1
#                     hcpuu = 1
#                     hcpup = 1
#         elif gpuSum > 66:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     hgpu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     hgpu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mmem = 1
#                     hgpu = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     hgpu = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     hgpu = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     mmem = 1
#                     hgpu = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     mmem = 1
#                     hgpu = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     mmem = 1
#                     hgpu = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     mmem = 1
#                     hgpu = 1
#                     hcpuu = 1
#                     hcpup = 1
#     elif memSum > 66:
#         if gpuSum >= 0 and gpuSum <= 33:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hmem = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hmem = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     hmem = 1
#                     hcpuu = 1
#                     hcpup = 1
#         elif gpuSum > 33 and gpuSum <= 66:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     mgpu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     mgpu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hmem = 1
#                     mgpu = 1
#                     hcpup = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     mgpu = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     mgpu = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hmem = 1
#                     mgpu = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     mgpu = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     mgpu = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     hmem = 1
#                     mgpu = 1
#                     hcpuu = 1
#                     hcpup = 1
#         elif gpuSum > 66:
#             if cpuUSum >= 0 and cpuUSum <= 33:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     hgpu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     hgpu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hmem = 1
#             elif cpuUSum > 33 and cpuUSum <= 66:
#                 if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     hgpu = 1
#                     mcpuu = 1
#                 elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     hgpu = 1
#                     mcpuu = 1
#                     mcpup = 1
#                 elif cpuPSum > 66:
#                     hmem = 1
#                     hgpu = 1
#                     mcpuu = 1
#                     hcpup = 1
#             elif cpuUSum > 66:
#                  if cpuPSum >= 0 and cpuPSum <= 33:
#                     hmem = 1
#                     hgpu = 1
#                     hcpuu = 1
#                  elif cpuPSum > 33 and cpuPSum <= 66:
#                     hmem = 1
#                     hgpu = 1
#                     hcpuu = 1
#                     mcpup = 1
#                  elif cpuPSum > 66:
#                     hmem = 1
#                     hgpu = 1
#                     hcpuu = 1
#                     hcpup = 1

#     #print('hmem: '+str(hmem)+' hgpu: '+str(hgpu)+' hcpuu: '+str(hcpuu)+' hcpup: '+str(hcpup)+' mmem: '+str(mmem)+' mgpu: '+str(mgpu)+' mcpuu: '+str(mcpuu)+' mcpup: '+str(mcpup))

#     if hmem == 1:
#         ans = 'High Memory Usage. Close down unneeded tabs.'
#         sug += [ans]

#     if hgpu == 1:
#         ans = 'High GPU Usage. Close any tabs containing videos or images.'
#         sug += [ans]

#     if hcpuu == 1:
#         ans ='High Computer Resource usage. End any tabs with running processes.'
#         sug += [ans]

#     if hcpup == 1:
#         ans = 'High Computer Power Usage. Close down tabs that are using a lot of resources.'
#         sug += [ans]

#     if maxtab != mintab:
#         if maxtab > firstTab:
#             #opened tabs during session
#             if hmem == 1:
#                 ans = 'Tabs were opened during monitoring session and memory usage increased. Close down any newly opened tabs.'
#                 sug += [ans]
#             else:
#                 ans = 'Tabs were opened during monitoring session but had no effect on memory usage. The opened tab did not effect the memory usage.'
#                 sug += [ans]
#         elif mintab < firstTab:
#             #closed tabs during session
#             if hmem == 0:
#                 ans = 'Tabs were closed during monitoring session and memory usage decreased. Good job!'
#                 sug += [ans]
#             else:
#                 ans = 'Tabs were closed during monitoring session but did not help lower memory usage. Try closing down tabs that are using more data.'
#                 sug += [ans]
#     else:
#         if tabSum >= 5:
#             ans = 'You have a decent amount of tabs opened. Do you need them all right now?'
#             sug += [ans]
    
#     return HttpResponse(sug)