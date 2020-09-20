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
        let fileLink = row0.insertCell(0);
        var fileLinkData = "<h4> <a href=\"file:///" + result.file + "\" target=_blank>" + result.file + "</a></h4>";
        fileLink.innerHTML = fileLinkData;
        let row1 = table.insertRow();
        let fileMeta = row1.insertCell(0);
        var fileMetaData = "<h5> Meta - " + result.contentType + "&nbsp;&nbsp;" + result.contentLength + "</h5>"
        fileMeta.innerHTML = fileMetaData;
        let row2 = table.insertRow();
        let fileSummary = row2.insertCell(0);
        fileSummary.innerHTML = result.summary;
        i++;
    });
}

function handleKeyPress(e) {
    if (e.keyCode == 13) {
        search();
    }
}
