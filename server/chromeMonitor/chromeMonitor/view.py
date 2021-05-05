from chromeMonitor.models import ResourceRecord
from django.core.exceptions import ObjectDoesNotExist
from django.http import HttpResponse
import json
import pymysql

def check_user_exists(request):
    # Load JSON string from the GET request.
    requestJSON = json.loads(request.body)
    # Read the JSON fields.
    userName = str(requestJSON.get('user'))
    if not userName:
        return HttpResponse('{"Status":"ERROR","Errors":["PARSE ERROR: .get(\'user\')"]}')
    # Try to retrieve the record correlating to the request from the database.
    try:
        batch = ResourceRecord.objects.get(user=userName)
        return HttpResponse('True')
    except ObjectDoesNotExist:
        return HttpResponse('False')



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

