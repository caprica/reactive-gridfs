# Reactive GridFS Example

## Introduction

This small project shows how to use Spring Boot, Spring Web Flux, MongoDB and GridFS to implement a simple web-service
for file uploads and downloads.

This example does *not* deal with GridFS buckets.

## Running the application

This is a standard Spring Boot application, the main class is `ReactiveGridFSApplication`.

Simply run that class inside your IDE, or from the command-line use `mvn spring-boot:run`.

The defaults for MongoDB are used, assuming a non-authenticated database running on localhost:27017. This can be changed
in `src/main/application.properties`.

## Testing the endpoints

The examples shown here use [HTTPie](https://httpie.org/).

### POST (store) a file

```
http -f POST :8080/file file@./some-cool-synthwave-track.mp3
```

Note here that "file" in `file@/...` matches the name declared in the `@RequestPart("file")` controller annotation:

```
@PostMapping
public Mono<FileId> store(@RequestPart("file") Mono<FilePart> filePart) {
    ...
}
```

The response contains the id for the newly uploaded file:
```
HTTP/1.1 200 OK
Content-Length: 33
Content-Type: application/json

{
    "id": "5f4aa7325e33b541d30a69c4"
}
```

### GET all files

```
http :8080/files
```

Response:

```
HTTP/1.1 200 OK
Content-Type: application/json
transfer-encoding: chunked

[
    {
        "filename": "some-cool-synthwave-track.mp3",
        "id": "5f4aa7325e33b541d30a69c4",
        "length": 10697101,
        "uploadDate": "2020-08-29T19:06:26.283+00:00"
    }
]
```

### GET a file

Using the file id:

```
http :8080/files/5f4aa7325e33b541d30a69c4 > download.mp3
```

The file streams to "download.mp3" with a `200 OK` response.

Using a file id that does not exist:

```
http :8080/files/how-very-dare-you
```

Response:

```
HTTP/1.1 404 Not Found
Content-Length: 153
Content-Type: application/json

{
    "error": "Not Found",
    "message": null,
    "path": "/files/how-very-dare-you",
    "requestId": "6dd721c6-27",
    "status": 404,
    "timestamp": "2020-08-29T19:14:48.728+00:00"
}
```

## DELETE a file

Using the file id:

```
http DELETE :8080/files/5f4aa7325e33b541d30a69c4
```

Response:

```
HTTP/1.1 200 OK
content-length: 0
```

Using a file id that does not exist:

```
http DELETE :8080/files/how-very-dare-you
```

Response:

```
HTTP/1.1 200 OK
content-length: 0
```

In this case it is correct that `200 OK` is returned rather than `404 Not Found` since deletes are idempotent, and the
response reflects the successful outcome that the resource does not exist (whether it was actually deleted or not does
not matter).

### DELETE all files

```
http DELETE :8080/files
```

Response:

```
HTTP/1.1 200 OK
content-length: 0
```

## Developer notes

### To use a value object or not?

This application maps the id of the newly uploaded file to a `FileId` value object which is then serialised as JSON:

```
@PostMapping
public Mono<FileId> store(@RequestPart("file") Mono<FilePart> filePart) {
    log.debug("store()");
    return filePart
        .flatMap(part -> filesService.store(part.content(), part.filename()))
        .map(id -> new FileId(id.toHexString()));
}
```

Here `FileId` is explicitly declared on the method return type.

The application referenced below used this construction of a `ResponseEntity` with a dynamically created body:

```
public Mono<ResponseEntity> store(@RequestPart Mono<FilePart> fileParts) {
    log.debug("store()");
    return fileParts
        .flatMap(part -> filesService.store(part.content(), part.filename()))
        .map(id -> ok().body(Map.of("id", id.toHexString())));
}

```

Here the return type is simply `ResponseEntity`.

Take your pick, it does not really matter.

## References

This no-nonsense [example](https://github.com/hantsy/spring-reactive-sample/tree/master/boot-data-mongo-gridfs) was very
helpful.
