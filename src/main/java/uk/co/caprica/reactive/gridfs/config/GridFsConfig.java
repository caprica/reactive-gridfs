package uk.co.caprica.reactive.gridfs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

@Configuration
@Slf4j
public class GridFsConfig {

    @Value("${gridfs.bucket}")
    private String bucket;

    /**
     * Create a template component for interacting with the GridFS.
     * <p>
     * This example uses a bucket name configured from "application.properties".
     * <p>
     * If multiple different buckets were required, create a new @Bean with a unique name for each one, then when
     * auto-wiring use @Qualifier with the corresponding name.
     *
     * @param reactiveMongoDbFactory database factory
     * @param mappingMongoConverter database entity converter
     * @return template component
     */
    @Bean
    public ReactiveGridFsTemplate reactiveGridFsTemplate(ReactiveMongoDatabaseFactory reactiveMongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        log.debug("Using bucket '{}'", bucket);
        return new ReactiveGridFsTemplate(reactiveMongoDbFactory, mappingMongoConverter, bucket);
    }
}
