package uk.co.caprica.reactive.gridfs.files.domain;

import lombok.Value;
import uk.co.caprica.reactive.gridfs.files.domain.FileMetadata;

import java.util.Date;

@Value
public class FileInfo {
    String id;
    String filename;
    long length;
    Date uploadDate;
    FileMetadata metadata;
}
