package co.edu.ierdminayticha.sgd.documents.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import co.edu.ierdminayticha.sgd.documents.dto.BinaryInfoDto;
import co.edu.ierdminayticha.sgd.documents.dto.ChildrenInDto;
import co.edu.ierdminayticha.sgd.documents.dto.ContentInfoDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentInfoDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentListResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentaryTypeOutDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentaryUnitDto;
import co.edu.ierdminayticha.sgd.documents.dto.PreservationInfo;
import co.edu.ierdminayticha.sgd.documents.dto.SpecificMetadataDto;
import co.edu.ierdminayticha.sgd.documents.entity.DocumentEntity;
import co.edu.ierdminayticha.sgd.documents.entity.MetadataEntity;
import co.edu.ierdminayticha.sgd.documents.entity.SpecificMetadataEntity;
import co.edu.ierdminayticha.sgd.documents.exception.GeneralException;
import co.edu.ierdminayticha.sgd.documents.repository.IDocumentsRepository;
import co.edu.ierdminayticha.sgd.documents.repository.IMetadataRepository;
import co.edu.ierdminayticha.sgd.documents.util.Properties;
import co.edu.ierdminayticha.sgd.documents.util.ResponseCodeConstants;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DocumentsServiceImpl implements IDocumentsService {

	private static final String NODE_TYPE_DOCUMENT = "DOCUMENT";
    private final static double fB = 1024.0;

	@Autowired
	private IDocumentsRepository repository;
	@Autowired
	private IMetadataRepository metadataRepository;
	@Autowired
	private Properties properties;
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public DocumentResponseDto create(DocumentRequestDto request) {
		log.info("create");
		this.validateExistenceResource(request.getDocumentInfo().getName(), 
				request.getParent());
		// Validar información de la unidad documental (serio o subserie) y obtener la
		// fecha de preservaciónde la misma
		LocalDate preservationDate = this.getInfoDocumentaryUnit(
				request.getDocumentaryUnit());
		// Validar existencia del tipo documental y obtener su información
		String documentaryType = invokeDocumentaryTypeMicroservice(
				request.getDocumentaryUnit().getDocumentaryType());
		// Validar existencia del folder padre y obtener su información
		DocumentEntity entity = toPersist(request, preservationDate);
		// retornar respuesta
		return createSuccessfulResponse(entity, documentaryType);
	}

	@Override
	public DocumentResponseDto findById(Long id) {
		log.info("findById");
		DocumentEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new GeneralException(
						ResponseCodeConstants.ERROR_BUSINESS_DOCUMENT_NOT_EXIST));
		
		String documentaryType = this.invokeDocumentaryTypeMicroservice(
				entity.getMetadataEntity().getDocumentaryType());
		return createSuccessfulResponse(entity, documentaryType);
	}

	@Override
	public void update(Long id, DocumentUpdateRequestDto request) {
		log.info("update");
		DocumentEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new GeneralException(
						ResponseCodeConstants.ERROR_BUSINESS_DOCUMENT_NOT_EXIST));
		
		// Modificación del nombre
		if (request.getName() != null) {
			entity.getMetadataEntity().setName(request.getName());
		}
		// Modificacion de los cometarios
		if (request.getComment() != null) {
			entity.getMetadataEntity().setComment(request.getComment());
		}
		// Modificar fecha de conservación (cuando aplique)
		if (request.getPreservationInfo() != null) {
			LocalDate dateAux = null;
			// Validar a que parte de la fecha se quiere agregar mas tiempo
			// (dias, meses o anios)
			if (request.getPreservationInfo().getPartDate().equals("DIAS")) {
				dateAux = entity.getMetadataEntity().getPreservationDate()
						.plusDays(request.getPreservationInfo().getTime());
			} else if (request.getPreservationInfo().getPartDate().equals("MESES")) {
				dateAux = entity.getMetadataEntity().getPreservationDate()
						.plusMonths(request.getPreservationInfo().getTime());
			} else if (request.getPreservationInfo().getPartDate().equals("ANIOS")) {
				dateAux = entity.getMetadataEntity().getPreservationDate()
						.plusYears(request.getPreservationInfo().getTime());
			}
			entity.getMetadataEntity().setPreservationDate(dateAux);
		}
		// Agregar nuevos metadatos especificos
		if (request.getSpecificMetadata() != null) {
			for (SpecificMetadataDto specificMetadata : request.getSpecificMetadata()) {
				SpecificMetadataEntity specificMetadataEntity = new SpecificMetadataEntity();
				specificMetadataEntity.setName(specificMetadata.getName());
				specificMetadataEntity.setValue(specificMetadata.getValue());
				entity.getMetadataEntity().addSpecificMetadata(specificMetadataEntity);
			}
		}
		//Modificar ubicación
		if (request.getLocationInfo() != null) {
			entity.getMetadataEntity().setParent(request.getLocationInfo().getFolderId());
		}
		entity.getMetadataEntity().setLastModifiedDate(new Date());
		this.repository.save(entity);
	}

	@Override
	public void delete(Long idParentFolder, Long idDocument) {
		log.info("delete");
		
		DocumentEntity entity = this.repository.findById(idDocument)
				.orElseThrow(() -> new GeneralException(
						ResponseCodeConstants.ERROR_BUSINESS_DOCUMENT_NOT_EXIST));
		
		this.invokeLogicalFolderDeleteChildren(idParentFolder, idDocument);
		repository.delete(entity);
	}
	
	@Override
	public List<DocumentListResponseDto> findAll(String filter, 
			String filterValue) {
		log.info("findAll - obtener lista de documentos con filtro {}: {}", 
				filter, filterValue);
		List<DocumentListResponseDto> documentListDto = new ArrayList<>();
		List<MetadataEntity> documentListEntity = null;
		
		if (filter.equals("NAME")) {
			documentListEntity = this.metadataRepository.findByNameContaining(filterValue);
		}else if (filter.equals("SM")) {
			documentListEntity = this.metadataRepository.findBySpecificMetadata(filterValue);			
		}else {
			documentListEntity = this.metadataRepository.findByCreationUser(filterValue);
		}
		
		documentListEntity.forEach(documentEntity -> {
			DocumentListResponseDto documentDto =new DocumentListResponseDto();
			documentDto.setId(documentEntity.getId());
			documentDto.setNodeType(NODE_TYPE_DOCUMENT);
			documentDto.setCreationDate(documentEntity.getCreationDate());
			documentDto.setCreationUser(documentEntity.getCreationUser());
			documentDto.setName(documentEntity.getName());
			documentListDto.add(documentDto);
		});
		
		return documentListDto;
	}
	
	

	private void validateExistenceResource(String name, Long idParent) {
		MetadataEntity entity = 
				this.metadataRepository.findByNameAndParent(name, idParent);
		if (entity != null) {
			log.info("validateExistenceOfResource - El documento con nombre ({})"
					+ " ya existe en la carpeta actual", name);
			throw new GeneralException(ResponseCodeConstants.ERROR_BUSINESS_DOCUMENT_ALREADY_EXIST);
		}
	}

	private DocumentEntity toPersist(DocumentRequestDto request, LocalDate preservationDate) {
		DocumentEntity entity = new DocumentEntity();
		entity.setBinaryCode(request.getBinaryInfo().getFieldId());
		// Metadata
		MetadataEntity metadataEntity = new MetadataEntity();
		// Información general
		metadataEntity.setParent(request.getParent());
		metadataEntity.setDocumentaryType(request.getDocumentaryUnit().getDocumentaryType());
		// Set información del documento
		metadataEntity.setName(request.getDocumentInfo().getName());
		metadataEntity.setCreationUser(request.getDocumentInfo().getCreationUser());
		metadataEntity.setComment(request.getDocumentInfo().getComment());
		metadataEntity.setCreationDate(LocalDate.now());
		// Set información del contenido del documento
		metadataEntity.setContentType(request.getContentInfo().getContentType());
		metadataEntity.setDocumentFamily(request.getContentInfo().getDocumentFamily());
		metadataEntity.setSize(Double.parseDouble(request.getContentInfo().getSize()));
		// Set información del binario
		metadataEntity.setFieldId(request.getBinaryInfo().getFieldId());
		// Set información preservación del documento
		// Definiendo la fecha de preservación del documento
		metadataEntity.setPreservationDate(preservationDate);
		if (request.getDocumentaryUnit().getIdSerie() != null && request.getDocumentaryUnit().getIdSubserie() != null) {
			metadataEntity.setIdSubSerie(request.getDocumentaryUnit().getIdSubserie());
		} else {
			metadataEntity.setIdSerie(request.getDocumentaryUnit().getIdSerie());
		}
		// Set información metadatos especificos
		if (request.getSpecificMetadata()!=null) {
			for (SpecificMetadataDto specificMetadataDto : request.getSpecificMetadata()) {
				SpecificMetadataEntity specificMetadataEntity = new SpecificMetadataEntity();
				specificMetadataEntity.setName(specificMetadataDto.getName());
				specificMetadataEntity.setValue(specificMetadataDto.getValue());
				metadataEntity.addSpecificMetadata(specificMetadataEntity);
			}

		}
		entity.setMetadataEntity(metadataEntity);
		DocumentEntity entityOut = repository.save(entity);
		// Invocar microservicio para agregar el documento al folder correspondinete
		invokeLogicalFolderAddChildren(entityOut);
		return entity;
	}

	private DocumentResponseDto createSuccessfulResponse(DocumentEntity entity, String documentaryType) {
		DocumentResponseDto response = new DocumentResponseDto();
		// Set información general
		response.setId(entity.getId());		
		response.setParent(entity.getMetadataEntity().getParent());
//		response.setLocation(this.setLocationDocument(entity.getMetadataEntity().getParent()));
		// Set información tipo documental
		response.setDocumentaryType(new DocumentaryTypeOutDto());
		JSONObject jsonObjectDocumentaryType = new JSONObject(documentaryType);
		response.getDocumentaryType().setId(jsonObjectDocumentaryType.getLong("id"));
		response.getDocumentaryType().setName(jsonObjectDocumentaryType.getString("name"));
		// Set información del documento
		response.setDocumentInfo(new DocumentInfoDto());
		response.getDocumentInfo().setName(entity.getMetadataEntity().getName());
		response.getDocumentInfo().setCreationUser(entity.getMetadataEntity().getCreationUser());
		response.getDocumentInfo().setComment(entity.getMetadataEntity().getComment());
		response.getDocumentInfo().setCreationDate(entity.getMetadataEntity().getCreationDate());
		response.getDocumentInfo().setLastModifiedDate(entity.getMetadataEntity().getLastModifiedDate());
		// Set información del contenido del documento
		response.setContentInfo(new ContentInfoDto());
		response.getContentInfo().setContentType(entity.getMetadataEntity().getContentType());
		response.getContentInfo().setDocumentFamily(entity.getMetadataEntity().getDocumentFamily());
		response.getContentInfo().setSize(this.getElementSize(entity.getMetadataEntity().getSize()));
		// Set información del binario
		response.setBinaryInfo(new BinaryInfoDto());
		response.getBinaryInfo().setFieldId(entity.getMetadataEntity().getFieldId());
		// Set información preservación del documento
		response.setPreservationInfo(new PreservationInfo());
		response.getPreservationInfo().setPreservatoinDate(entity.getMetadataEntity().getPreservationDate());
		response.getPreservationInfo().setConservationTime(Duration.between(LocalDate.now().atStartOfDay(), entity.getMetadataEntity().getPreservationDate().atStartOfDay()).toDays());
		// Set información metadatos especificos
		if (entity.getMetadataEntity().getSpecificMetadata()!=null) {
			response.setSpecificMetadata(new ArrayList<>());
			for (SpecificMetadataEntity specificMetadataEntity : 
				 entity.getMetadataEntity().getSpecificMetadata()) {
				SpecificMetadataDto specificMetadataDto = new SpecificMetadataDto();
				specificMetadataDto.setId(specificMetadataEntity.getId());
				specificMetadataDto.setName(specificMetadataEntity.getName());
				specificMetadataDto.setValue(specificMetadataEntity.getValue());
				response.getSpecificMetadata().add(specificMetadataDto);
			}

		}
		return response;
	}

	private String setLocationDocument(Long parent) {
		//String responseLogicalFolder = this.invokeLogicalFolderById(parent);		
		//return this.extractLocationFolderFromResponse(responseLogicalFolder);
		return null;
	}

	private String extractLocationFolderFromResponse(String responseLogicalFolder) {
		
		return null;
	}

	public String getElementSize (Double size) {
            double fL = size;
            if (fL <= fB) {
                return String.valueOf(fL).concat(" bytes");
            } else {
                double sizeKB = getFileSizeInKB(fL);
                if(getFileSizeInKB(fL) <= fB)
                    return String.valueOf(sizeKB).concat(" KB");
                else {
                    double sizeMB = getFileSizeInMB(fL);
                    if(sizeMB <= fB)
                        return String.valueOf(sizeMB).concat(" MB");
                    else {
                        double sizeGB = getFileSizeInGB(fL);
                            return String.valueOf(sizeGB).concat(" GB");

                    }
                }
            }
    }
 
    private double getFileSizeInKB (double f) {
        f = (f/fB);
        int fs = (int) Math.pow(10,2);
        return Math.rint(f*fs)/fs;
    }
 
    private double getFileSizeInMB (double f) {
        f = f / Math.pow(fB,2);
        int fs = (int) Math.pow(10,2);
        return Math.rint(f*fs)/fs;
    }

    private double getFileSizeInGB (double f) {
        f = f / Math.pow(fB,3);
        int fs = (int) Math.pow(10,2);
        return Math.rint(f*fs)/fs;
    }

	private String invokeDocumentaryTypeMicroservice(Long id) {
		log.info("invokeDocumentaryTypeMicroservice");
		String response = null;
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("documentary-type-id", id);
		try {
			ResponseEntity<String> responseEntity = 
					this.restTemplate.getForEntity(
							properties.getUrlGetDocumentaryTypeById(),
					String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error("invokeDocumentaryTypeMicroservice - httpClientErrorException "
					+ "falló, error: {}",e.getCause());
			throw new GeneralException(ResponseCodeConstants.ERROR_INVOKING_GET_DOCUMENTARY_TYPE_SERVICE);
		}
		return response;
	}

	private void invokeLogicalFolderAddChildren(DocumentEntity documentsEntity) {
		log.info("invokeLogicalFolderAddChildren");
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", 
					  documentsEntity.getMetadataEntity().getParent());
		ChildrenInDto childrenInDto = new ChildrenInDto();
		childrenInDto.setId(documentsEntity.getId());
		childrenInDto.setName(documentsEntity.getMetadataEntity().getName());
		childrenInDto.setNodeType(2L);
		HttpEntity<ChildrenInDto> request = new HttpEntity<>(childrenInDto);
		try {
			restTemplate.postForEntity(properties.getUrlAddChildrenToFolder(), 
					request, String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("invokeLogicalFolderAddChildren - httpClientErrorException) "
					+ "falló, error: {}", e.getCause());
			throw new GeneralException(ResponseCodeConstants.ERROR_INVOKING_GET_ADD_CHILDREN_SERVICE);
		}
	}

	private LocalDate getInfoDocumentaryUnit(DocumentaryUnitDto documentaryUnit) {
		LocalDate date = LocalDate.now();
		String infoDocumentaryUnit = null;
		if (documentaryUnit.getIdSerie() != null && 
			documentaryUnit.getIdSubserie() != null) {
			infoDocumentaryUnit = 
					this.invokeMicroserviceSunSerie(documentaryUnit.getIdSubserie());
		} else {
			infoDocumentaryUnit = 
					this.invokeMicroserviceSerie(documentaryUnit.getIdSerie());
		}
		// Extraer fecha de preservación de la unidad documental
		JSONObject jsonObjectDocumentaryUnit = new JSONObject(infoDocumentaryUnit);
		date = date.plusYears(jsonObjectDocumentaryUnit.getLong("retentionTime"));
		return date;
	}

	private String invokeMicroserviceSerie(Long idSerie) {
		log.info("invokeMicroserviceSerie");
		String response = "";
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("serie-id", idSerie);
		try {
			ResponseEntity<String> responseEntity = 
					this.restTemplate.getForEntity(properties.getUrlFindSerieById(), 
							String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error("invokeMicroserviceSerie - httpClientErrorException falló, "
					+ "error: {}", e.getCause());
			throw new GeneralException(ResponseCodeConstants.ERROR_INVOKING_GET_SERIE_SERVICE);
		}
		return response;
	}

	private String invokeMicroserviceSunSerie(Long idSubSerie) {
		log.info("invokeMicroserviceSunSerie",
				idSubSerie);
		String response = "";
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("sub-serie-id", idSubSerie);
		try {
			ResponseEntity<String> responseEntity = 
					restTemplate.getForEntity(properties.getUrlFindSubSerieById(), 
							String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error("invokeMicroserviceSunSerie - httpClientErrorException, "
					+ "error: {}", e.getCause());
			throw new GeneralException(ResponseCodeConstants.ERROR_INVOKING_GET_SUBSER_SERVICE);
		}
		return response;
	}
	
	private void invokeLogicalFolderDeleteChildren(Long idParentFolder, Long idDocument) {
		log.info("invokeLogicalFolderDeleteChildren");
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", idParentFolder);
		uriParams.put("child-id", idDocument);
		try {
			restTemplate.delete(properties.getUrlDeleteChildrenToFolder(), uriParams);
		} catch (HttpClientErrorException e) {
			log.error("invokeLogicalFolderDeleteChildren - "
					+ "httpClientErrorException , error: {}", e.getCause());
			throw new GeneralException(ResponseCodeConstants.ERROR_INVOKING_GET_DELETE_CHILDREN_SERVICE);
		}
	}
	
	private String invokeLogicalFolderById(Long idFolder) {
		log.info("invokeLogicalFolderById");
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", idFolder);
		try {			
			return restTemplate.getForObject(
					properties.getUrlGetFolderById(), String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("invokeLogicalFolderById - "
					+ "httpClientErrorException , error: {}", e.getCause());
			throw new GeneralException(ResponseCodeConstants.ERROR_INVOKING_GET_FOLDER_ID_SERVICE);
		}
	}

}
