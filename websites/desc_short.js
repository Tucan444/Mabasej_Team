
$(document).ready(function() {
    let base_url = "http://192.168.1.156:8000/";

    function append_element(image_urls, descriptions, title, id) {

        let template = `
            <div class="wsList hey">
            <div ></div>
            <img src="${image_urls[0]}" alt="#" style="height: 100px;" class="wsImg">
            <div class="list_item_holder">
                <div class="title${id}">${title}</div>
                <div class="short_description${id}">${descriptions[0]}</div>
            </div>
            <div id="arrow${id}" class="arrow">&#9660</div>
            </div>
            <!-- Rozšírené malé zobrazenie -->
            <div id="listExpand${id}" class="listHide">
                <img src="${image_urls[1]}" alt="#" class="constantImg">
                <div class="text">${descriptions[1]}</div>
                <div class="sensors">
                </div>
                <div class="info"></div>
            </div>
            <!-- Veľké zobrazenie -->
            <div class="big">
                <img class="obrazok" src="${image_urls[1]}" alt="#">
                <div class="text">${descriptions[1]}</div>
            </div>`;
    
        $("body").append(template);

        $.get(`http://192.168.1.156:8000/${id}/sensors`, function(data1) {
            console.log(data1);
            Object.keys(data1).forEach(function(key) {
                let value = data1[key];
                if (key.startsWith("[translate]-")) {
                    key = key.slice(12, key.length);
                }
                $(".sensors").append(`<p>${key}: ${value}</p>`);
            });
        });

        $(".arrow").click(function() {

            let id = this.id;
            id = id.slice(id.length-1, id.length);
    
            $(`#arrow${id}`).toggleClass("mystyle");
            $(`#listExpand${id}`).toggleClass("expand");
    
        });
    
    }

    $.get(`${base_url}devices_list`, function(data) {

        let connected_id = data[0]["connected_id"];
        let urls = ["", ""];
        let descriptions = ["", ""];
        let title = "";
        let query = [[]];

        for (let i = 1; i < data.length; i++) {
            let spot_data = data[i];
            let spot_id = spot_data["ID"]
            urls = [`${base_url}files/${spot_id}/${spot_data["description"]["photo_s"]}`,
                    `${base_url}files/${spot_id}/${spot_data["description"]["photo_b"]}`];
            descriptions = [spot_data["description"]["description_s"],
                            spot_data["description"]["description_l"]];
            title = spot_data["description"]["title"];

            if (spot_id == connected_id) {
                query[0] = [urls, descriptions, title, parseInt(spot_id)];
            } else {
                query.push([urls, descriptions, title, parseInt(spot_id)]);
            }
        }

        for (let i = 0; i < query.length; i++) {
            append_element(query[i][0], query[i][1], query[i][2], query[i][3]);
        }
 
    })
    
})
