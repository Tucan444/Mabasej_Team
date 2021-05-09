// fetch("http://192.168.1.105:8000/devices_list")
//             .then(function(response){
//                 return response.json();          
//             })
//             .then(function(data){
//                 appendData(data);
//             })
//             .catch(function(err){ 
//                 console.log(err);

//             });
//             function appendData(data){
//                 console.log(data);
//                 console.log(data[1]["files"][0]);
//                 var myJson = JSON.stringify(data[1]["description"]["description_s"]);
//                 var myJson1 = JSON.stringify(data[1]["description"]["title"]);
//                 var myJson2 = JSON.stringify(data[1]["description"]["description_l"]);
//                 var uJson =  myJson.replace(/\"/g, "");
//                 var str = (uJson);
//                 var str_esc = escape(str);
//                 console.log(str_esc + "<br>" + myJson1);
//                 console.log(unescape(str_esc));
//                 var mainContainer = document.getElementById("hou1");
//                     var div = document.createElement("div");
//                     div.setAttribute("id", "hou");
//                     div.innerHTML = unescape(str_esc);

//                     mainContainer.appendChild(div);

//                 var mainContainer1 = document.getElementById("hey1");
//                     var uJson1 = myJson1.replace(/\"/g, "");
//                     var div1 = document.createElement("div");
//                     div1.setAttribute("id", "hey");
//                     div1.innerHTML = unescape(escape(uJson1));
//                     var uJson1 = myJson1.replace(/\"/g, "");

//                     mainContainer1.appendChild(div1);
//                 }
$(document).ready(function() {
    let urls = ["http://192.168.1.105:8000/files/0/library.jpg", "http://192.168.1.105:8000/files/0/library.jpg", "http://192.168.1.105:8000/files/0/library.jpg"];
    let descriptions = ["here", "there"];
    let n = 1;
    function append_element(urls, descriptions, n) {

        let template = `<div class="wsList hey">
        <div ></div>
        <img src="${urls[0]}" alt="#" style="height: 100px;" class="wsImg">
        <div class="klol">
                <div id="hey1"></div>
                <div id="hou1"></div>
        </div>
        <div id="arrow${n}" class="arrow" onclick="onclick${n}()">&#9660</div>
    </div>
    <!-- Rozšírené malé zobrazenie -->
    <div id="listExpand${n}" class="listHide">
        <img src="${urls[1]}" alt="#" class="constantImg">
        <div class="text">${descriptions[0]}</div>
        <div>
            <div class="senzory">Teplota</div>
        <div id="heat" class="senzory"></div>
        </div>
        <div class="info"></div>
    </div>
    <!-- Veľké zobrazenie -->
    <div class="big">
        <img class="obrazok" src="${urls[2]}" alt="#">
        <div class="text">${descriptions[1]}</div>
    </div>`;
    
        $("body").append(template);
    
    }
    append_element(urls, descriptions, n);

    $.get("http://192.168.1.105:8000/devices_list", function(data) {
    console.log(data); 
    var myJson = JSON.stringify(data[1]["description"]["description_s"]);
    var myJson1 = JSON.stringify(data[1]["description"]["title"]);
    console.log(myJson);
    console.log(myJson1);
    uJson = myJson.replace(/\"/g, "");
    var str = (uJson);
    var str_esc = escape(str);
    console.log(str_esc + "<br>" + myJson);
    console.log(unescape(str_esc));
    var main = document.getElementById("hou1");
    var div = document.createElement("div");
    div.setAttribute("id", "hou");
    div.innerHTML = unescape(str_esc);

        main.appendChild(div);

    var sub = document.getElementById("hey1");
    var uJson1 = myJson1.replace(/\"/g, "");
    var div1 = document.createElement("div");
    div1.setAttribute("id", "hey");
    div1.innerHTML = unescape(escape(uJson1));

        sub.appendChild(div1);



    ID = 0;
    $.get(`http://192.168.1.105:8000/${ID}/sensors`, function(data1) {
        console.log(data1);
        var sens = JSON.stringify(data1["teplota"]);
        console.log(sens);
        var uSens = sens.replace(/\"/g, "");
        var heat = document.getElementById("heat");
        var div2 = document.createElement("div");
        div2.setAttribute("id", "hot");
        div2.innerHTML = unescape(escape(uSens));

        heat.appendChild(div2);
})
    })
    
})
