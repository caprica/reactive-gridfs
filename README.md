# Reactive GridFS Example

## Introduction

This small project shows how to use Spring Boot, Spring Web Flux, MongoDB and GridFS to implement a simple web-service
for file uploads and downloads.

## Running the application

This is a standard Spring Boot application, the main class is `ReactiveGridFSApplication`.

Simply run that class inside your IDE, or from the command-line use `mvn spring-boot:run`.

The defaults for MongoDB are used, assuming a non-authenticated database running on localhost:27017. This can be changed
in `src/main/application.properties`.

## GridFS buckets

This application uses a GridFS bucket with a name configured in `src/main/application.properties`.

The default bucket may be fine for some use-cases, in which case behind the scenes a collection named "fs" is used for
the bucket.

If an application needed multiple buckets, then a separate `ReactiveGridFsTemplate` bean is needed for each bucket - the
`@Bean` annotation can take a name, which is then referenced when auto-wiring the components using an `@Qualifier`
annotation with the same name.

See the `GridFsConfig` class.

## Metadata

GridFS allows for the storage of metadata associated with the file. Some details such as the upload data, file size and
so on are provided for by default.

It is possible to associate application-specific metadata with the stored files, this example implements the optional
supply of such metadata.

## Testing the endpoints

The examples shown here use [HTTPie](https://httpie.org/).

### POST (store) a file

```
http -f POST :8080/file file@./some-cool-synthwave-track.mp3
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

With optional JSON metadata:

```
http -f POST :8080/file file@./some-cool-synthwave-track.mp3 data@./some-metadata.json
```

Note here that "data" in `data@...` and "file" in `file@...` match the values declared in the `@RequestPart` controller
annotations:

```
@PostMapping
public Mono<FileId> store(@RequestPart(value="data", required=false) FileMetadata metadata, @RequestPart("file") Mono<FilePart> filePart) {
    ...
}
```

Here the metadata is optional whereas the upload file itself is mandatory, and this is why those annotations have
different forms.

The JSON metadata file should match the structure of the `FileMetadata.java` class, for example:

```
{
    "owner": "Neon Nox"
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
    },
    {
        "filename": "another-cool-synthwave-track.mp3",
        "id": "5f4f4360224725129b139eb0",
        "length": 14322462,
        "metadata": {
            "owner": "Neon Nox"
        },
        "uploadDate": "2020-08-29T19:16:52.881+00:00"
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

## GET metadata for a file

```
http :8080/files/5f4f4360224725129b139eb0/metadata
```

Response:

```
HTTP/1.1 200 OK
Content-Length: 20
Content-Type: application/json

{
    "owner": "Neon Nox"
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
