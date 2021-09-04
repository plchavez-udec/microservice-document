package co.edu.ierdminayticha.sgd.documents.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.ierdminayticha.sgd.documents.entity.DocumentEntity;
import co.edu.ierdminayticha.sgd.documents.entity.MetadataEntity;

@Repository
public interface IMetadataRepository extends CrudRepository<MetadataEntity, Long> {

	MetadataEntity findByNameAndParent(String name, Long idParent);
	List<MetadataEntity> findByNameContaining(String name);
	List<MetadataEntity> findByCreationUser(String userCreation);
	@Query(value = "SELECT * FROM public.\"METADATOS\" C "
				 + "INNER JOIN public.\"METADATOS_ESPECIFICOS_DOCUMENTO\" ME "
				 + "ON C.\"ID\" = ME.\"ID_METADATA_FK\" "
				 + "WHERE"
				 + " ME.\"VALOR\" LIKE %?1%", nativeQuery = true)
	List<MetadataEntity> findBySpecificMetadata(String specificMetadata);

}
