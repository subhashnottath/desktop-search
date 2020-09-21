function search() {
    const url = '/search';
    (async () => {
        const query = document.getElementById("query").value;
        const rawResponse = await fetch(url, {
            method: 'POST',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
            query: query,
            })
        });
        const response = await rawResponse.json();
        if (response.length == 0) {
            const table = document.getElementById("responseTable");
            table.innerHTML = "";
            let row = table.insertRow();
            let message = row.insertCell(0);
            message.innerHTML = "Not found";
        } else {
            loadTableData(response);
        }
    })();
}

function loadTableData(results) {
    const table = document.getElementById("responseTable");
    table.innerHTML = "";
    var i = 1;
    results.forEach(result => {
        let row0 = table.insertRow();
        let resultRow = row0.insertCell(0);
        var fileLinkData = "<h3> <a href=\"file:///" + result.file + "\" target=_blank>" + result.file + "</a></h3>";
        var fileMetaData = "<b>Type - " + result.contentType + "&nbsp;&nbsp; Size - " + result.contentLength + "</b>";
        resultRow.innerHTML = fileLinkData + result.summary + "<br><br>" +  fileMetaData;
        i++;
    });
}

function handleKeyPress(e) {
    if (e.keyCode == 13) {
        search();
    }
}
