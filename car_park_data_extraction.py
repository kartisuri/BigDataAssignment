import sys
import os
import datetime
import time
import requests
from os.path import abspath, dirname, join

url = 'https://api.data.gov.sg/v1/transport/carpark-availability'
#url -> url for get request

wait_seconds = 60
#wait_seconds -> sleep time after each query
#program need to sleep because data is updated in the api for every one minute

file_name = 'carpark_availability_' + datetime.datetime.now().strftime('%d_%m_%Y') + '.csv'
file_to_write = join(dirname(abspath(__file__)), file_name)
#file_to_write -> file name along with the same path as the program
print(file_to_write)

#run continously till we press ctrl + c
while True:
    try:
        current_datetime = datetime.datetime.now().strftime('%Y-%m-%dT%H:%M:%S') 
        #current_datetime -> current datetime in YYYY-MM-DDTHH:MM:SS format
        print(current_datetime)
    
        response = requests.get(url=url, params={'date_time': current_datetime}).json()
        #response -> dictionary containing car park api response

        total_car_parks = len(response['items'][0]['carpark_data'])
        #total_car_parks -> total number of car parks data available
        print(total_car_parks)
        
        for i in range(total_car_parks):
            value = (response['items'][0]['carpark_data'][i]['carpark_number'] + ',' + \
                    response['items'][0]['carpark_data'][i]['carpark_info'][0]['total_lots'] + ',' + \
                    response['items'][0]['carpark_data'][i]['carpark_info'][0]['lot_type'] + ',' + \
                    response['items'][0]['carpark_data'][i]['carpark_info'][0]['lots_available'] + ',' + \
                    response['items'][0]['carpark_data'][i]['update_datetime'] + ',' + \
                    response['items'][0]['timestamp'] + '\n').encode('utf-8')
            #value -> comma separated single car park data 
            print(value)

            # if file not present create file and write data else append data
            if not os.path.isfile(file_name):
                with open(file_to_write, 'wb') as file_obj:
                	#column_head -> header line for csv file for file creation
                    column_head = 'CarParkNo,TotalLots,LotType,LotsAvailable,UpdatedDateTime,QueryDateTime\n'.encode('utf-8')
                    file_obj.write(column_head)
                    file_obj.write(value)
            else:
                with open(file_to_write, 'ab') as file_obj:
                    file_obj.write(value)

        #sleep for wait time specified
        print('Sleep for {} seconds before next query'.format(wait_seconds))
        time.sleep(wait_seconds)
    
    except KeyboardInterrupt:
    	#soft exit on Ctrl + C
    	#quit the program only when it is in sleep
    	print('Program terminated with "Ctrl + C"')
    	sys.exit()
