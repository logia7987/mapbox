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