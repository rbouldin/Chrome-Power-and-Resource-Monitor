import requests
 
res = requests.post('http://100.26.233.49:8000/data/', data={"user": "zinan", "avg_cpu_power": "10", "avg_cpu_usage":"69", "avg_gpu_usage":"70", "avg_mem_usage":"71", "batch":"1"})
print(res.text)
