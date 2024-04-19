// 메뉴 모드를 다크 모드 혹은 화이트 모드 바꾸는 함수
function toggleWhiteMode() {
    var icon = document.getElementById("mdicon");
    var styleOption = document.getElementsByClassName("style-option");

    if (tabmenu.style.color === "#020202" || tabmenu.style.color === "" || tabmenu.style.color === 'rgb(2, 2, 2)') {
        // 화이트 모드에서 다크 모드로 전환 될때
        $("#tab, #map-style").css("color", "#ffffff");
        icon.classList.remove('fa-moon');
        icon.classList.add('fa-sun');
        tabmenu.style.backgroundColor = "#666";
        for (i = 0; i < styleOption.length; i++) {
            styleOption[i].style.color = "white"
        }
    } else {
        // 다크 모드에서 화이트 모드로 전환 될때
        $("#tab, #map-style").css("color", "#020202");
        icon.classList.add('fa-moon');
        icon.classList.remove('fa-sun');
        tabmenu.style.backgroundColor = "#fff"
        for (i = 0; i < styleOption.length; i++) {
            styleOption[i].style.color = "black"
        }
    }
}

function checkTab() {
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].classList.remove("active");
    }
}

// 해당 탭을 여는 함수
function openTab(event, tab) {
    if (event.currentTarget.className === "tab-links") {
        checkTab()
        document.getElementById(tab).style.display = "block"
        event.currentTarget.className += " active";
    } else {
        checkTab()
    }
}

function createLayer(data, type) {
    var html = "";
    var content = "";
    if ($(".file-info").length === 0) {
        html += '<div class="layer-file basic-font selected" id=\''+data.fileName+'\'>';
    } else {
        html += '<div class= "layer-file basic-font" id=\''+data.fileName+'\'>';
    }
    html += '<input type="checkbox" id="check_'+data.fileName+'" onclick="showHideLayer(\''+data.fileName+'\')" checked >';
    if (type === "Point") {
        html += '<i class="fa-brands fa-hashnode"></i>'
    } else if (type === "MultiLineString") {
        html += '<i class="fa-solid fa-share-nodes"></i>'
    } else {
        html += '<i class="fa-solid fa-draw-polygon"></i>'
    }
    html += '<div class="file-info" onclick="selectedLayer('+data.fileName+')">';
    html += '<div class="file-tit">'+data.fileName+'</div>';
    html += '</div>';
    html += '<div class="dropdown"> ' +
        '<i class="fa-solid fa-ellipsis-vertical" data-bs-toggle="dropdown"></i></button> <ul class="dropdown-menu">' +
        '<li onclick="saveShp(\''+data.fileName+'\')" class="dropdown-item">저장</li>' +
        '<li onclick="removePolygon(\''+data.fileName+'\')" class="dropdown-item">삭제</li>' +
        '</ul></div></div>'
    $(".layer-file-list").append(html);
    fileNmList.push(data.fileName)

    selectedLayer(data.fileName, type);
}


    function selectedLayer(obj, type) {
        if ($(".file-info").length === 0) {
            fileNm = obj
        } else  {
            var layer = document.getElementsByClassName("layer-file");
            for (i = 0; i < layer.length; i++) {
                layer[i].classList.remove("selected");
            }
            $("#"+obj).addClass("selected")
            fileNm = $('.selected .file-tit').text()
            if (type === "Point")  {
                geoData = nodeDataArr
                getProperties(geoData)
                openTab(event, 'tab3')
            } else if (type === "MultiLineString") {
                geoData = linkDataArr
                getProperties(geoData)
                openTab(event, 'tab3')
            } else {
                geoData = dataArr
                getProperties(geoData)
                polygonDetail()
            }
        }

    }
    function polygonDetail() {
        $(".colors-item .sp-preview-inner").css("background-color", map.getPaintProperty('polygons_'+fileNm,'fill-color'))
        $(".line-item .sp-preview-inner ").css("background-color", map.getPaintProperty('outline_'+fileNm,'line-color'))
        $("#line-width").val(map.getPaintProperty('outline_'+fileNm,'line-width'))
        getProperties()
        openTab(event, 'tab3')
        // if (tabmenu.style.color === "#020202" || tabmenu.style.color === "" || tabmenu.style.color === "rgb(2, 2, 2)"){
        //     tablinks[2].classList.add("active-white");
        // } else {
        //     tablinks[2].classList.add("active-dark")
        // }
        for (i = 0; i < fileNmList.length; i++) {
            map.on('mousemove', 'polygons_'+ fileNmList[i], function () {})
            map.on('mouseleave', 'polygons_'+ fileNmList[i], function () {})
            map.on('click', 'polygons_'+ fileNmList[i], function () {})
            map.setPaintProperty('polygons_'+fileNmList[i],'fill-opacity', 0.5);
        }
        var opacity = ['case', ['boolean', ['feature-state', 'hover'], false], 1, 0.5]
        map.setPaintProperty('polygons_'+fileNm,'fill-opacity', opacity);
        map.on('click', 'polygons_'+fileNm, function (e) {
            selectedShp = e.features[0]
            if (e.features[0].layer.id === 'polygons_'+fileNm) {
                var property = "";
                var id = selectedShp.properties.DIST1_ID;
                var info = dataArr[fileNm].data.features
                for (i = 0; i < info.length; i++) {
                    if (info[i].properties.DIST1_ID === id) {
                        property = info[i]
                    }
                }
                $('#'+ id).parent().addClass("selected")

                openTab(event, 'tab3')
                // if (tabmenu.style.color === "#020202" || tabmenu.style.color === "" || tabmenu.style.color === "rgb(2, 2, 2)"){
                //     tablinks[2].classList.add("active-white");
                // } else {
                //     tablinks[2].classList.add("active-dark")
                // }

                editShp(property)
            }
        });

        map.on('mousemove', 'polygons_'+fileNm, (e) => {
            selectedShp = e.features
            if (e.features[0].layer.id === 'polygons_'+fileNm) {
                if (selectedShp.length > 0) {
                    if (hoveredPolygonId !== null) {
                        map.setFeatureState(
                            { source: 'data_'+fileNm, id: hoveredPolygonId },
                            { hover: false }
                        );
                    }
                    hoveredPolygonId = selectedShp[0].id;
                    map.setFeatureState(
                        { source: 'data_'+fileNm, id: hoveredPolygonId },
                        { hover: true }
                    );
                }
            }

        });
        map.on('mouseleave', 'polygons_'+fileNm, () => {
            if (hoveredPolygonId !== null) {
                map.setFeatureState(
                    { source: 'data_'+fileNm, id: hoveredPolygonId },
                    { hover: false }
                );
            }
            hoveredPolygonId = null;
        });
    }

function getProperties(geoData) {
    var info = geoData[fileNm].data.features
    var html = ""
    if (info.length > 0) {
        $(".file-info-item").remove();
    }
    var titArr = []

    var title = Object.keys(info[0].properties)
    for (var i = 0; i < title.length; i++) {
        var tit = title[i]
        html = "<th>" + title[i] + "</th>"
        $(".property-tit").append(html)
        titArr.push(tit)
    }

    for (j = 0; j < info.length; j++) {
        detail = "<tr></tr>"
        for (k = 0; k < titArr.length; k++) {
            html2 = "<tb>" +info[j].titArr[k].toString() + "</tb>";
            detail.append(html2);
        }
        $(".property-detail").append(detail)
    }


}

function selectedProperty(obj) {
    if (map.getLayoutProperty('polygons_'+fileNm, 'visibility') === 'none') {
        alert("선택하신 레이어가 지도에 없습니다")
    } else {
        var property = "";
        var id = obj.querySelector('.info-id').textContent;
        var info = dataArr[fileNm].data.features
        $(obj).parent().addClass("selected")
        for (i = 0; i < info.length; i++) {
            if (info[i].properties.DIST1_ID === id) {
                property = info[i]
            }
        }

        // if ($(".polygon-properties").length > 0) {
        //     $(".polygon-properties").remove()
        // }
        // var html = "<div class='polygon-properties'> ID : "+property.properties.DIST1_ID +"<br>"+
        //     "시군구 : "+property.properties.F_NAME+"<br>"+
        //     "지역 코드 : "+property.properties.GCODE +"<br>"+
        //     "지역명 : "+property.properties.NAME +"</div>"
        // $(".property-list").append(html)

        editShp(property)
    }
}

function changeProperties(id) {
    changeProper = id
    $(".modal-body form").remove()
    var html =
        "<form method='POST'><label> ID  </label><input id ='proper-dist1id' type='text' value= "+$("#"+id+" .info-id").text()+"><br>"+
        "<label> 지역 코드 </label><input id ='proper-gcode' type='text' value= "+$("#"+id+" .info-gcode").text()+"><br>"+
        "<label> 시군구 </label><input id ='proper-name' type='text' value= "+$("#"+id+" .info-name").text()+"><br>"+
        "<label> 지역명 </label><input id ='proper-fname' type='text' value= "+$("#"+id+" .info-fname").text()+"></div></form>"
    $(".modal-body").append(html)
}

function finishProperties() {
    var data = dataArr[fileNm].data.features
    for (i = 0; i < data.length; i++) {
        if (data[i].id == changeProper) {
            data[i].properties["DIST1_ID"] = $('#proper-dist1id').val()
            data[i].properties["GCODE"] = $('#proper-gcode').val()
            data[i].properties["F_NAME"] = $('#proper-name').val()
            data[i].properties["NAME"] = $('#proper-fname').val()
            $("#"+changeProper+" .info-id").text($('#proper-dist1id').val())
            $("#"+changeProper+" .info-gcode").text($('#proper-gcode').val())
            $("#"+changeProper+" .info-name").text($('#proper-name').val())
            $("#"+changeProper+" .info-fname").text($('#proper-fname').val())
        }
    }
}

// 로딩 화면
function viewLoading() {
    $('#loading-window, .loading-logo').show();
    document.getElementById("map").style.position = "absolute";
}

// 로딩 종료
function finishLoading() {
    $('#loading-window, .loading-logo').hide();
    document.getElementById("map").style.position = "";
}

function showSearch() {
    $('#search-input').toggle()
}