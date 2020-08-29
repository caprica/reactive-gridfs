package uk.co.caprica.reactive.gridfs.files;

import lombok.Value;

import java.util.Date;

@Value
public class FileInfo {
    String id;
    String filename;
    long length;
    Date uploadDate;
}
