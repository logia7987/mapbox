function hideModal(id) {
    var myModalEl = document.getElementById(id);
    var modal = bootstrap.Modal.getInstance(myModalEl)
    modal.hide();
}

// 열린 shp 파일(파일 이름, 파일 저장한 날)을 db에 저장하는 함수
function saveShp(filename) {
    $.ajax({
        url : '/api/saveShp',
        type : 'POST',
        data : {
            shpName : filename
        },
        success : function (result){
            console.log(result)
            var shpId = result.shpId;

            for (var i = 0; i < dataArr[filename].data.features.length; i++) {
                saveFeature(shpId, dataArr[filename].data.features[i], (i+1));
            }

            var html = ""
            html = "<a href='#' onclick='getShpData("+shpId+")'>"+filename+"</a>"
            $('.options').append(html)
        },
        error : function (error){
            console.log(error)
        }
    })
}

// 열린 shp 파일 내 Feature들을 db에 저장하는 함수
function saveFeature(shpId, jsonObj, idx) {
    $.ajax({
        url : '/api/saveFeature',
        type : 'POST',
        dataType: 'json',
        contentType: 'application/json', // JSON으로 데이터를 전송함을 명시
        data: JSON.stringify({
            shpId: shpId,
            seq : idx,
            jsonObject: jsonObj
        }),
        success : function (result){
            console.log(result)
        },
        complete: function(xhr, status) {
            if (status === 'error' || !xhr.responseText) {
                console.log('Network error or empty response.');
            }
        },
        error : function (error){
            console.log(error)
        }
    })
}

function getShpData(shpId) {
    $.ajax({
        url : "/api/getShp",
        type : "POST",
        data : {
            shpId : shpId
        },
        beforeSend: function( ) {
            viewLoading()
        },
        complete: function( ) {
            finishLoading();
        },
        success : function(data) {
            console.log(data.data)
            data.fileName = data.shpName
            drawPolyline(data);
            createLayer(data)
        },
        error: function () {
        }
    });
}


// 그려진 폴리곤라인 지도 내에 한눈에 보이도록 하는 함수
function setMapBounds(data) {
    var bounds = new mapboxgl.LngLatBounds();
    data.features.forEach(function (feature) {
        var coordinates = feature.geometry.coordinates[0];
        coordinates.forEach(function (coordinate) {
            bounds.extend(coordinate);
        });
    });

    // 바운더리에 맞게 지도 조정
    map.fitBounds(bounds, { padding: 150 });
}

// 리스트에 db에 저장된 파일 가져오는 함수
function sendFiles() {
    hideModal('loadFile')

    var frmFile = $("#frmFile");
    // 파일 선택 input 요소
    var fileInput = frmFile.find("input[name='shpData']")[0];

    // 선택한 파일 가져오기
    var files = fileInput.files;
    var formData = new FormData();

    hasShp = false
    hasShx = false
    hasDdf = false


    for (var i = 0; i < files.length; i++) {
        if (files[i].name.indexOf('.shp') > -1) {
            hasShp = true
        } else if (files[i].name.indexOf('.shx') > -1) {
            hasShx = true
        } else if (files[i].name.indexOf('.dbf') > -1) {
            hasDdf = true
        }
    }

    if (hasShp === true && hasShx === true && hasDdf === true) {
        for (var i = 0; i < files.length; i++) {
            formData.append('shpData', files[i]);
        }
    } else {
        alert ("필수 파일을 확인해주세요.")
    }

    $.ajax({
        url: '/api/uploadShapeFiles',  // 서버 엔드포인트
        type: 'POST',
        data: formData,
        processData: false,  // 필수: FormData를 query string으로 변환하지 않음
        contentType: false,  // 필수: 파일 전송에는 multipart/form-data 형식이 필요
        beforeSend: function( ) {
            viewLoading()
        },
        complete: function( ) {
            finishLoading();
        },
        success: function (data) {
            /* geojson 형식
            * Point: [longitude, latitude]
            * LineString: [[longitude1, latitude1], [longitude2, latitude2], ...]
            * Polygon: [[[longitude1, latitude1], [longitude2, latitude2], ...], ...]
            */
            const dataType = checkDataType(data);
            if (dataType === "Point")  {
                drawNodePoint(data);
            } else if (dataType === "MultiLineString") {
                drawLinkLine(data)
            } else {
                drawPolyline(data);
            }
            createLayer(data, dataType);
        },
        error: function (error) {
            console.error('Error uploading file:', error);
        }
    });
}


function removePolygon(key) {
    fileName = dataArr[key].fileName

    if (draw.getAll().length > 0) {
        draw.deleteAll();
    }
    map.removeLayer('polygons_'+fileName);
    map.removeLayer('outline_'+fileName);

    map.removeSource('data_'+fileName);
    $("#" + fileName).remove();

    for (i = 0; i < fileNmList.length; i++) {
        if (fileNmList[i] === fileName) {
            fileNmList.splice(i, 1)
        }
    }

    delete dataArr[key]

    if ($(".layer-file").length === 0) {
        $(".file-info-item").remove();
    }
}
function editShp(property) {
    map.removeLayer('polygons_'+(fileNm));
    map.removeLayer('outline_'+(fileNm));
    map.removeSource('data_'+(fileNm));
    var polygonArr = []
    drawArr = []
    $('#btn-status').text("편집 모드")

    propertyArr.push(property)

    for (i = 0; i < dataArr[fileNm].data.features.length; i++) {
        var found = false;
        for (j = 0; j < propertyArr.length; j++) {
            if (propertyArr[j].id === dataArr[fileNm].data.features[i].id) {
                drawArr.push(dataArr[fileNm].data.features[i])
                found = true;
                break;
            }
        }
        if (!found) {
            polygonArr.push(dataArr[fileNm].data.features[i]);
        }
    }

    for (i = 0; i < drawArr.length; i++) {
        draw.add(drawArr[i])
    }
    polygon(polygonArr);
}

function finishPoint() {
    var item = document.getElementsByClassName("file-info-item");
    $('#btn-status').text("보기 모드")
    for (i = 0; i < item.length; i++) {
        item[i].classList.remove("selected");
    }
    if (draw.getAll().features.length > 0) {
        draw.getAll()
        for (i = 0; i < drawArr.length; i++) {
            dataArr[fileNm].data.features[drawArr[i].id-1]=draw.getAll().features[i]
        }
        map.removeLayer('polygons_'+(fileNm));
        map.removeLayer('outline_'+(fileNm));
        map.removeSource('data_'+(fileNm));
        polygon(dataArr[fileNm].data.features)
        draw.deleteAll();
        propertyArr = []
        drawArr = []
    } else {
        alert('편집된 부분이 없습니다')
    }
}

function checkDataType(data) {
    // 대표로 첫번째 인덱스의 정보를 가져와서 타입 검사 실시
    var type = data.data.features[0].geometry.type
    return type
}

function changePolygonColor() {
    var polygon = $("#polygon-color").val()
    polygonColor = polygon
    for (i = 0; i < fileNmList.length; i++) {
        if(fileNm === fileNmList[i]) {
            map.setPaintProperty('polygons_'+fileNmList[i],'fill-color', polygonColor);
        }
    }
}

function changeLineColor() {
    var line = $("#line-color").val()
    lineColor = line
    for (i = 0; i < fileNmList.length; i++) {
        if(fileNm === fileNmList[i]) {
            map.setPaintProperty('outline_'+fileNmList[i],'line-color', lineColor);
        }
    }
}

function  changeLineThickness() {
    var line = $("#line-width").val()
    lineWidth = line
    for (i = 0; i < fileNmList.length; i++) {
        if(fileNm === fileNmList[i]) {
            map.setPaintProperty('outline_'+fileNmList[i],'line-width', Number(lineWidth));
        }
    }
}

function polygon(data) {
    var tData = {
        type: 'geojson',
        data: {
            type : 'FeatureCollection',
            features : data
        }
    }
    map.addSource("data_"+fileNm, tData);
    map.addLayer({
        'id': 'polygons_'+fileNm,
        'type': 'fill',
        'source': 'data_'+fileNm,
        'layout': {},
        'paint': {
            'fill-color': polygonColor,
            'fill-opacity': [
                'case',
                ['boolean', ['feature-state', 'hover'], false],
                1,
                0.5
            ]
        }
    });
    map.addLayer({
        'id': 'outline_'+fileNm,
        'type': 'line',
        'source': 'data_'+fileNm,
        'layout': {},
        'paint': {
            'line-color': lineColor,
            'line-width': Number(lineWidth),
        }
    });

}

function showHideLayer(Nm) {
    if ($('#check_'+Nm).is(':checked') === true) {
        map.setLayoutProperty('polygons_'+Nm, 'visibility', 'visible');
        map.setLayoutProperty('outline_'+Nm, 'visibility', 'visible');
    } else {
        map.setLayoutProperty('polygons_'+Nm, 'visibility', 'none');
        map.setLayoutProperty('outline_'+Nm, 'visibility', 'none');
    }
}

// 지도 스타일 변경하는 함수
mapSelect.onchange = (change) => {
    const changeId = change.target.value;
    map.setStyle('mapbox://styles/mapbox/' + changeId);
    // map.on('style.load', () => {
    //     for (i = 0; i < document.querySelectorAll('.file-tit').length; i++) {
    //         var Name = document.querySelectorAll('.file-tit')[i].textContent
    //         drawPolyline(dataArr[Name])
    //     }
    // });
}

// Polyline 그리는 함수
function drawPolyline(data) {
    let count = 0;
    for (const key in dataArr) {
        if (key.indexOf(data.fileName) > -1) {count++;}
    }
    if (count > 0 ) {
        data.fileName = data.fileName+'_'+count
    }

    dataArr[data.fileName] = data

    var tData = {
        type: 'geojson',
        data: {
            type : 'FeatureCollection',
            features :data.data.features
        }
    }

    setMapBounds(data.data);
    map.addSource("data_"+data.fileName, tData);
    map.addLayer({
        'id': 'polygons_'+data.fileName,
        'type': 'fill',
        'source': 'data_'+data.fileName,
        'paint': {
            'fill-color': polygonColor,
            'fill-opacity': [
                'case',
                ['boolean', ['feature-state', 'hover'], false],
                1,
                0.5
            ]
        }
    });

    map.addLayer({
        'id': 'outline_'+data.fileName,
        'type': 'line',
        'source': 'data_'+data.fileName,
        'layout': {},
        'paint': {
            'line-color': lineColor,
            'line-width': Number(lineWidth)
        }
    });

}

function drawNodePoint(data) {
    let count = 0;
    for (const key in nodeDataArr) {
        if (key.indexOf(data.fileName) > -1) {count++;}
    }
    if (count > 0 ) {
        data.fileName = data.fileName+'_'+count
    }

    nodeDataArr[data.fileName] = data

    var tData = {
        type: 'geojson',
        data: {
            type : 'FeatureCollection',
            features :data.data.features
        }
    }

    map.addSource("nodeData_"+data.fileName, tData);
    map.addLayer({
        'id': 'points_'+data.fileName,
        'type': 'circle',
        'source': 'nodeData_'+data.fileName,
        'paint': {
            'circle-radius': 6, // 점의 반지름 설정
            'circle-color': [
                'case',
                ['boolean', ['feature-state', 'hover'], false],
                '#007dd2', // 클릭한 점의 색상
                '#1aa3ff', // 클릭하지 않은 점의 색상
            ],
            'circle-opacity': [
                'case',
                ['boolean', ['feature-state', 'hover'], false],
                1,
                0.5
            ]
        }
    });
}

function drawLinkLine(data) {
    let count = 0;
    for (const key in linkDataArr) {
        if (key.indexOf(data.fileName) > -1) {count++;}
    }
    if (count > 0 ) {
        data.fileName = data.fileName+'_'+count
    }

    linkDataArr[data.fileName] = data

    var tData = {
        type: 'geojson',
        data: {
            type : 'FeatureCollection',
            features :data.data.features
        }
    }

    map.addSource("lineData_"+data.fileName, tData);
    map.addLayer({
        'id': 'lines_' + data.fileName,
        'type': 'line',
        'source': 'lineData_' + data.fileName, // 선의 데이터를 가리키는 소스
        'paint': {
            'line-color': [
                'case',
                ['boolean', ['feature-state', 'hover'], false],
                '#007dd2', // 클릭한 선의 색상
                '#1aa3ff', // 클릭하지 않은 선의 색상
            ],
            'line-width': 2, // 선의 너비 설정
            'line-opacity': [
                'case',
                ['boolean', ['feature-state', 'hover'], false],
                1,
                0.5
            ]
        }
    });
}

function handleDragOver(e) {
    e.preventDefault();
    uploadContainer.classList.add('drag-over');
}

function handleDragLeave(e) {
    e.preventDefault();
    uploadContainer.classList.remove('drag-over');
}

function handleDrop(e) {
    e.preventDefault();
    uploadContainer.classList.remove('drag-over');

    const files = e.dataTransfer.files;
    $('#file_intro h4').remove()
    $('.comment').remove()

    if (files.length > 0) {
        for (i = 0; i < files.length ; i++) {
            fileName = files[i].name
            var html = ""
            html = "<div class='dropfile basic-font'>"+fileName+"<i class=\"fas fa-solid fa-xmark\" onclick='deleteFileList()'></i></div>"
            $("#file_intro").append(html)
            $("#shpData").prop("files", e.dataTransfer.files)
        }
    }
}