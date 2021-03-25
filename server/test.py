import requests
 
res = requests.post('http://3.90.7.37:8000/data/', data={"user": "zinan", "avg_cpu_power": "10%"})
print(res.text)