package co.edu.ierdminayticha.sgd.documents.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.ierdminayticha.sgd.documents.entity.MetadataEntity;

@Repository
public interface IMetadataRepository extends CrudRepository<MetadataEntity, Long> {

	MetadataEntity findByNameAndParent(String name, Long idParent);

}
