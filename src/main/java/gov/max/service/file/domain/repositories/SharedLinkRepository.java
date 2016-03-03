package gov.max.service.file.domain.repositories;

import gov.max.service.file.domain.model.SharedLink;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SharedLinkRepository extends MongoRepository<SharedLink, String> {

    SharedLink findByPublicId(String publicId);
    SharedLink findByStorageId(String storageId);
    List<SharedLink> findByCreatedBy(String createdBy);
    List<SharedLink> findByCreatedBefore(Instant created);

}
