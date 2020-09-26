# Simple Desktop Search

### Features

1. HTML Based UI
2. Configure directories to be searched using `FileWatcher.filePaths` field in `application.properties`
3. Search file path, content and type `file(default)`, `content`, `type` 
    1. Search using [lucene query syntax](https://lucene.apache.org/core/2_9_4/queryparsersyntax.html) <br/>
    2. It searches in tokenized fields like absolute path, type, content <br/>
        `e.g.` `/opt/run/cmd/test - can be found by searching opt, run, cmd or test` <br/>
        `e.g.` `content: hello world`, `type:mp4`
3. Auto update index when changes occur in the configured directories  

### Technologies used

1. Html for UI
2. Java backend (Spring boot webflux)
3. Search using Apache Lucene
4. Content parsing using Apache Tika
5. Index auto update using WatchService 
6. Async communication to backend through javascript
