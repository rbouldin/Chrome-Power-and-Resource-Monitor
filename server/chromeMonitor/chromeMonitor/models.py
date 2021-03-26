from django.db import models
 
class ResourceRecord(models.Model):
    user = models.CharField(max_length=20)
    avg_cpu_power = models.FloatField()
    avg_cpu_usage = models.FloatField()
    avg_gpu_usage = models.FloatField()
    avg_mem_usage = models.FloatField()
    batch = models.IntegerField()
