package gov.max.service.file.domain.repositories;

import gov.max.service.file.domain.model.Upload;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data Mongo repository for the Upload entity.
 */
public interface UploadRepository extends MongoRepository<Upload, String> {

}
