# MAX Drive

## Introduction

A secure link sharing service based on Java/Spring. All files are stored encrypted. Large uploads are handled by [ng-flow](https://github.com/flowjs/ng-flow) and [flow.js](https://github.com/flowjs/flow.js)

## Description

This application uses ng-flow (the file uploader) client connected to a MAX Spring-based application. 

It implements a file uploader Java resource/service that handles the required functions of the ng-flow client library. It will save all uploaded chunks/files into the service home filebucket directory, or local test directory in development mode.

1. The ng-flow client will retrieve a new flow identifier from the server at the start of an upload. There is a REST API method that handles returning a random UUID generated server-side.

2. The client will test if a chunk has already been uploaded returning 502 if it hasn't.  

3. If the test returns a 502, the client will do a Multipart POST with a 1MB chunk of data as the next file chunk. This chunk will be saved off to a chunk temp file.

4. The application tracks which chunks received, and which ones haven't, in a temporary MongoDb document.

5. Once the last chunk arrives the application will merge them back into the original file and delete the temporary chunks.

## Installation

### Run (Local)
#### Step 1
Prior to running max-drive, you need to install the following dependencies:

- [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [MongodDB](https://www.mongodb.org/downloads)

#### Step 3
Clone the max-file-share GIT repository and start MAX File Share with the command:


Linux/OSX:

```./gradlew bootRun```

Windows:

```gradlew.bat bootRun```


Or if you have Foreman installed with:


```foreman run ./gradlew bootRun```


On the first start, Gradle will download all the Java dependencies. After that, MAX Drive is running at http://localhost:8080.

### Run (Max)
 
Set the Spring profile for the target environment

- Edit the 'profile' property in gradle.properties and set it to either 'test' or 'production'
- Edit the 'spring.profiles.active' property in application.yml and set it to either 'test' or 'production'


Linux/OSX:

```./gradlew clean assemble```

Windows:

```gradlew.bat clean assemble```


This above gradle command will generate the executable jar file in build/libs.

### Testing
```
./gradlew test
```

## Usage

### Web Interface
If you run MAX Drive locally (using development profile) then the web interface is running at http://localhost:8080. 

### REST API
MAX Drive also offers a simple REST API. The implementation of the REST service is contained in the gov.max.service.file.web.rest.ApiController class. 

#### Upload

Request Structure:

```
POST /files
Content-Type: form/multipart

file: Containing the binary data
password: Password to secure the link (optional)
```

Sample response:
```
{
    url: '<url to shared file>'
}
```

#### Download

Request Structure:

```
GET /files/<id>
Authorization: <password>
```

## Limitations
Currently, the max files size is limited to 2GB. This can be changed in the `application.properties` settings. Spring automatically handles bigger files and streams them to the file system (temp directory) to minimize memory usage. Therefore, the temp directory storage needs to be big enough to handle very big files in parallel.

When hosting MAX Drive on system with short timeouts, it will not be possible to support big files. For example, cloud provider Heroku's 30 second request timeout will cause issues. Therefore, it is better to host MAX Drive on a service which is supporting long timeouts. 

## Pontential Enhancements 

### Scalability / Performance
Because of the stateless design it is possible to run multiple MAX Drive instances behind a load balancer in order to serve many downloads & uploads. To scale the data storage, multiple MongoDB instances can be used (sharding or slave reads) or it would even be easier to just use a hosted solution (e.g. MongoLab).

Further scalability and performance can be gained by:
- Caching the most used files close to the application server. Therefore, no storage requests are necessary for the popular files.
- Gzip certain files (e.g. text) before storing them. This needs a little bit more CPU but saves disk space and makes it easier to scale the data storage.
- Doing non-blocking requests to MongoDB. There is a non-blocking Java Driver for MongoDB, but unfortunately Spring Data does not support this driver. Therefore, it would be necessary to replace Spring Data with an own solution in order to use this driver. 
- Using S3 as file storage. By implementing the `FileStorageService` interface with an S3 implementation its possible to store all files in AWS. Thus scaling the data store is a breeze and storage costs will probably be cheaper than the current file system, or a MongoDB GridFS solution.

### Resiliency / Error Handling
MAX Drive implements error handling by returning the appropriate HTTP error code or error message. All unexpected errors are logged so they can be monitored and fixed asap. Because of the stateless design temporary issues can be solved by the users them self with a simple re-try.

Nevertheless, there are still some TODOs:
- Show a 404 error page to inform the user and guide him to a valid page.
- Show a error message if the file is bigger than 2GB. Currently no error is shown.
- Show a nice error page/message in case the data storage is not reachable or there is another internal error.

### Security
The following security enhancement would be desired for productive usage:

- Currently, each file is encrypted with an individual key and those keys are stored inside the DB. In case the DB is compromised it would be better to encrypt them additionally with a master key. This master key could be injected into the application server on start (stored in memory only). 
- Proper CSRF handling for the upload form (Web UI).
- Limit the number of requests by IP, session or even by requiring user accounts.
- Limit the amount of space a user may use on the storage system

### Web UI

### Code
- More unit tests and integration tests are necessary to provide 100% code coverage.
