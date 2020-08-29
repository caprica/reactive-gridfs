package uk.co.caprica.reactive.gridfs.files;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Implementation of a file service that uses GridFS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FilesService implements Files.Service {

    private final ReactiveGridFsTemplate gridFsTemplate;

    @Override
    public Flux<FileInfo> all() {
        log.debug("all()");
        return gridFsTemplate
            .find(new Query())
            .map(FilesService::newFileInfo);
    }

    @Override
    public Mono<ObjectId> store(Flux<DataBuffer> buffer, String filename) {
        log.debug("store(filename={})", filename);
        return gridFsTemplate
            .store(buffer, filename);
    }

    @Override
    public Mono<Void> deleteAll() {
        log.debug("deleteAll()");
        return gridFsTemplate
            .delete(new Query());
    }

    @Override
    public Flux<DataBuffer> fetch(String id) {
        log.debug("fetch(id={})", id);
        return gridFsTemplate
            .findOne(query(where("_id").is(id)))
            .flatMap(gridFsTemplate::getResource)
            .flatMapMany(ReactiveGridFsResource::getDownloadStream);
    }

    @Override
    public Mono<Void> delete(String id) {
        return gridFsTemplate
            .delete(query(where("_id").is(id)));
    }

    private static FileInfo newFileInfo(GridFSFile file) {
        return new FileInfo(file.getObjectId().toHexString(), file.getFilename(), file.getLength(), file.getUploadDate());
    }
}
