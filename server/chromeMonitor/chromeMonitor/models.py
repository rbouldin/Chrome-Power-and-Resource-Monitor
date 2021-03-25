from django.db import models
 
class ResourceRecord(models.Model):
    user = models.CharField(max_length=20)