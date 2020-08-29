package uk.co.caprica.reactive.gridfs.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FilesController {

    private final Files.Service filesService;

    @GetMapping
    public Flux<FileInfo> all() {
        log.debug("all()");
        return filesService.all();
    }

    @PostMapping
    public Mono<FileId> store(@RequestPart("file") Mono<FilePart> filePart) {
        log.debug("store()");
        return filePart
            .flatMap(part -> filesService.store(part.content(), part.filename()))
            .map(id -> new FileId(id.toHexString()));
    }

    @DeleteMapping
    public Mono<Void> deleteAll() {
        log.debug("deleteAll()");
        return filesService.deleteAll();
    }

    @GetMapping("{id}")
    public Flux<Void> fetch(@PathVariable("id") String id, ServerWebExchange exchange) {
        log.debug("fetch(id={})", id);
        return filesService.fetch(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .publish(stream -> exchange.getResponse().writeWith(stream));
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        log.debug("delete(id={})", id);
        return filesService.delete(id);
    }
}
