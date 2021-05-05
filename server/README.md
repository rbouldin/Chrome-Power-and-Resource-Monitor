#Server README
##Summary
The server for the Chrome Monitoring Extension is used to record and evaluate a users information relating to their usage of Chrome. The server has a router to handle different 
kinds of requests (adding user data and generating a suggestion for the user) and utilizes a SQL table to store all the data. The server can be accessed thorugh a terminal connected
to an Amazon ec2 instance.

##Getting the server running
1. Open a terminal on your local machine and run the command: ssh -i "capstone.pem" ec2-user@ec2-52-91-154-176.compute-1.amazonaws.com
2. Once you are logged on, then run the command: python3 Chrome-Power-and-Resource-Monitor/server/chromeMonitor/manage.py runserver 0.0.0.0:8000
3. The server is now set up to handle incoming requests

##How to send requests to the server from the terminal
1. First you have to get the server running by following the above steps
2. Once the server is running, open another terminal on your local machine
3. Create a python file and on the first line of the file include the import statement "import requests"
4. On the following line, you can type one of the allowable requests to the server
    * The server handles requests to the urls http://52.91.154.176:8000/data/ and http://52.91.154.176:8000/suggestions/ dealing with user data
6. To send a request to the server, format the request like this: requests.post('http://52.91.154.176:8000/data/', data={...}) or requests.post('http://52.91.154.176:8000/suggestions/', data={...})
7. Following the format spefications described in Chrome-Power-and-Resource-Monitor/server/chromeMonitor/chromeMonitor/view.py, create a data set that will be sent to the server
8. Replace "data={...}" with "data={<the_data_set_you_made>}"
9. Save the file
10. Return to the local terminal and run the file using either "python <name_of_file>.py" or "python3 <name_of_file>.py" depending on the version of python you have
11. On the termial that is running the server, a message displaying the type of request and error code can be seen

##Viewing SQL table
The table that stores all the data on the server is an SQL table. You can access it and view all of the data stored in the tables using regular sql queries.
1. Open a terminal on your local machine and run the command: ssh -i "capstone.pem" ec2-user@ec2-52-91-154-176.compute-1.amazonaws.com
2. Run the command: mysql -u root -p
3. You will then be prompted for a password to access the database. Please contact the creators for this password
4. To show the databases run: show databases;
5. Locate the "chrome_data" database and run the command: use chrome_data;
6. To show the tables run: show tables;
7. The information relating to the users information is the table named "chromeMonitor_resourcerecord"
8. To show all the entires in the database run: select * from chromeMonitor_resourcerecord;
