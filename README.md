# File Share
A secure link sharing service based on Java/Spring. All files are stored encrypted. Large uploads are handled by [ng-flow](https://github.com/flowjs/ng-flow) and [flow.js](https://github.com/flowjs/flow.js)

## Installation

### Run (Local)
#### Step 1
Prior to run max-file-share, you need to install the following dependencies:

- [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

#### Step 3
Clone the max-file-share GIT repository and start MAX File Share with the command:

```
Linux/OSX:
./gradlew bootRun

Windows:
gradlew.bat bootRun
```

Or if you have Foreman installed with:

```
foreman run ./gradlew bootRun
```

On the first start, Gradle will download all the Java dependencies. After that, MAX File Share is running at http://localhost:8080.

### Testing
```
./gradlew test
```

## Usage

### Web Interface
If you run MAX File Share locally then the web interface is running at http://localhost:8080. 

### REST API
MAX File Share also offers a simple REST API. 

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
Currently, the max files size is limited to 10MB. This can be changed in the `application.properties` settings. Spring automatically handles bigger files and streams them to the file system (temp directory) to minimize memory usage. Therefore, the temp directory storage needs to be big enough to handle very big files in parallel.

When hosting MAX File Share on Heroku it will not be possible to support big files because of Heroku's 30 second request timeout. Therefore, it is better to host MAX File Share on a service which is supporting long timeouts. 

## Ideas / TODOs / Enhancements

### Scalability / Performance
Because of the stateless design it is possible to run multiple MAX File Share instances behind a load balancer in order to serve many downloads & uploads. To scale the data storage, multiple MongoDB instances can be used (sharding or slave reads) or it would even be easier to just use a hosted solution (e.g. MongoLab).

Further scalability and performance can be gained by:
- Caching the most used files close to the application server. Therefore, no storage requests are necessary for the popular files.
- Gzip certain files (e.g. text) before storing them. This needs a little bit more CPU but saves disk space and makes it easier to scale the data storage.
- Doing non-blocking requests to MongoDB. There is a non-blocking Java Driver for MongoDB, but unfortunately Spring Data does not support this driver. Therefore, it would be necessary to replace Spring Data with an own solution in order to use this driver. 
- Using S3 as file storage. By implementing the `FileStorageService` interface with an S3 implementation its possible to store all files in AWS. Thus scaling the data store is a breeze and storage costs will probably be cheaper than the current MongoDB GridFS solution.

### Resiliency / Error Handling
MAX File Share implements error handling by returning the appropriate HTTP error code or error message. All unexpected errors are logged so they can be monitored and fixed asap. Because of the stateless design temporary issues can be solved by the users them self with a simple re-try.

Nevertheless, there are still some TODOs:
- Show a 404 error page to inform the user and guide him to a valid page.
- Show a error message if the file is bigger than 10MB. Currently no error is shown.
- Show a nice error page/message in case the data storage is not reachable or there is another internal error.
- Use a CDN like CloudFlare to display an "offline" message in case MAX File Share is completely unreachable.

### Security
The following security enhancement would be necessary for productive usage:

- Currently, each file is encrypted with an individual key and those keys are stored inside the DB. In case the DB is compromised it would be better to encrypt them additionally with a master key. This master key could be injected into the application server on start (stored in memory only). 
- Proper CSRF handling for the upload form (Web UI).
- Limit user input size. E.g. limit max. password field length on the server.
- Protect the file uploads with a captcha to prevent bots from uploading files.
- Limit the number of requests by IP, session or even by requiring user accounts.
- In case a link is secured with a password, this password should have a minimal length.
- Redirect all users to use HTTPS.

### Web UI
- Better UI design and a nice landing page.
- Support for multi languages and internationalization.
- When uploading files, show a process bar with the progress (multiple JS-based solutions are available).

### Code
- More unit tests and integration tests are necessary to provide 100% code coverage.
