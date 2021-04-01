import requests
import json
 
#res = requests.post('http://0.0.0.0:8000/data/', data={"user": "zinan", "avg_cpu_power": "10", "avg_cpu_usage":"69", "avg_gpu_usage":"70", "avg_mem_usage":"71", "batch":"3", "tabs":"5"})
#print(res.text)

#res = requests.post('http://0.0.0.0:8000/suggestion/', data={"user": "robbie", "avg_cpu_power": "69", "avg_cpu_usage":"5", "avg_gpu_usage":"34", "avg_mem_usage":"19", "batch":"2"})


res = requests.post('http://0.0.0.0:8000/suggestion/', data={"user": "robbie", "batch":"3"})
print(res.text)
