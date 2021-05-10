$(document).ready(function(){
    $.get("test.txt", function(data){
        console.log(data);
        let data_string = data.toString();
        
        data_string.replace(new RegExp("\\\\n", "g"), "*");
        console.log(JSON.stringify(data_string));
        $("#projectInfo").html(data_string);
    });           
});