fetch("http://192.168.1.105:8000/devices_list")
            .then(function(response){
                return response.json();          
            })
            .then(function(data){
                appendData(data);
            })
            .catch(function(err){

            });
            function appendData(data){
                console.log(data);
                console.log(data[1]["files"][0]);
                var myJson1 = JSON.stringify(data[1]["description"]["title"]);
                var uJson1 =  myJson1.replace(/\"/g, "");
                var str1 = (uJson1);
                var str_esc1 = escape(str1);
                console.log(str_esc1 + "<br>");
                console.log(unescape(str_esc1));
                var mainContainer = document.getElementById("myTitle");
                    var div1 = document.createElement("p");
                    div.setAttribute("id", "titleData")
                    div.innerHTML = unescape(str_esc1);

                    mainContainer.appendChild(div1);
                }