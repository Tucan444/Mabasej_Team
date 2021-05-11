fetch("http://192.168.1.105:8000/docs#/default/update_sensors_update_sensor_post")
            .then(function(response1){
                return response.json();          
            })
            .then(function(data1){
                appendData(data1);
            })
            .catch(function(err1){

            });
            function appendChild(data1){
                
            }