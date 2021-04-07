package co.edu.ierdminayticha.sgd.documents.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import co.edu.ierdminayticha.sgd.documents.entity.DocumentsEntity;

@Repository
public interface IDocumentsRepository extends CrudRepository<DocumentsEntity, Long> {

}
