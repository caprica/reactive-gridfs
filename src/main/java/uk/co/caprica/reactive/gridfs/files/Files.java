package uk.co.caprica.reactive.gridfs.files;

import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.co.caprica.reactive.gridfs.files.domain.FileInfo;
import uk.co.caprica.reactive.gridfs.files.domain.FileMetadata;

/**
 * Specification for the "Files" component.
 * <p>
 * Contains interfaces for all pluggable or architecturally significant components.
 * <p>
 * Declaring interfaces like this means there is no need to have e.g. the awkwardly named FilesServiceImpl or
 * DefaultFilesService and so on.
 */
public interface Files {

    /**
     * Specification for the files service.
     * <p>
     * Note that the service does not expose any details of the underlying storage implementation.
     */
    interface Service {

        Flux<FileInfo> all();

        Mono<ObjectId> store(@Nullable FileMetadata metadata, Flux<DataBuffer> buffer, String filename);

        Mono<Void> deleteAll();

        Flux<DataBuffer> fetch(String id);

        Mono<Void> delete(String id);

        Mono<FileMetadata> metadata(String id);
    }
}
