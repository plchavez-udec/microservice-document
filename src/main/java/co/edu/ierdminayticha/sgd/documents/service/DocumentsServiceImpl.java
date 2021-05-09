package co.edu.ierdminayticha.sgd.documents.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import co.edu.ierdminayticha.sgd.documents.dto.BinaryInfoDto;
import co.edu.ierdminayticha.sgd.documents.dto.ChildrenInDto;
import co.edu.ierdminayticha.sgd.documents.dto.ContentInfoDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentInfoDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentResponseDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentUpdateRequestDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentaryTypeOutDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentaryUnitDto;
import co.edu.ierdminayticha.sgd.documents.dto.PreservationInfo;
import co.edu.ierdminayticha.sgd.documents.dto.SecurityLevelDto;
import co.edu.ierdminayticha.sgd.documents.dto.SpecificMetadataDto;
import co.edu.ierdminayticha.sgd.documents.entity.DocumentEntity;
import co.edu.ierdminayticha.sgd.documents.entity.MetadataEntity;
import co.edu.ierdminayticha.sgd.documents.entity.SpecificMetadataEntity;
import co.edu.ierdminayticha.sgd.documents.exception.GeneralException;
import co.edu.ierdminayticha.sgd.documents.repository.IDocumentsRepository;
import co.edu.ierdminayticha.sgd.documents.repository.IMetadataRepository;
import co.edu.ierdminayticha.sgd.documents.repository.ISecurityLevelRepository;
import co.edu.ierdminayticha.sgd.documents.util.Properties;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DocumentsServiceImpl implements IDocumentsService {

	private static final String EXISTING_RESOURCE_MESSAGE = "El documento con nombre (%s) ya existe";
	private static final String NO_EXISTEN_RESOURCE_MESSAGE = "No existe el documento con id (%s) ";

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
		log.info("create :: Creando documento {}", request);
		this.validateExistenceOfResource(request.getDocumentInfo().getName(), request.getParent());
		// Vlidar información de la unidad documental (serio o subserie) y obtener la
		// fecha de preservaciónde la misma
		Date preservationDate = this.getInfoDocumentaryUnit(request.getDocumentaryUnit());
		// Validar existencia del tipo documental y obtener su información
		String documentaryType = invokeDocumentaryTypeMicroservice(request.getDocumentaryType());
		// Validar existencia del folder padre y obtener su información
		String logicalFolder = invokeLogicalFolderByIdMicroservice(request.getParent());
		DocumentEntity entity = toPersist(request, preservationDate, logicalFolder);
		// retornar respuesta
		return createSuccessfulResponse(entity, documentaryType);
	}

	@Override
	public DocumentResponseDto findById(Long id) {
		log.info("findById :: Consultar documento por Id {}", id);
		DocumentEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException(String.format(NO_EXISTEN_RESOURCE_MESSAGE, id)));
		String documentaryType = invokeDocumentaryTypeMicroservice(entity.getMetadataEntity().getDocumentaryType());
		return createSuccessfulResponse(entity, documentaryType);
	}

	@Override
	public void update(Long id, DocumentUpdateRequestDto request) {
		log.info("update :: Actualizar carpeta {}", request);
		DocumentEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException(String.format(NO_EXISTEN_RESOURCE_MESSAGE, id)));
		// Modificación del nombre
		if (request.getName() != null) {
			entity.getMetadataEntity().setName(request.getName());
		}
		// Modificacion de los cometarios
		if (request.getComment() != null) {
			entity.getMetadataEntity().setComment(request.getComment());
		}
		// Modificar la unicación
		if (request.getLocation() != null) {
			entity.getMetadataEntity().setLocation(request.getLocation());
		}
		// Modificar fecha de conservación (cuando aplique)
		if (request.getPreservationInfo() != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(entity.getMetadataEntity().getPreservationDate());
			// Validar a que parte de la fecha se quiere agregar mas tiempo
			// (dias, meses o anios)
			if (request.getPreservationInfo().getPartDate().equals("DIAS")) {
				c.add(Calendar.DAY_OF_YEAR, request.getPreservationInfo().getTime());
			} else if (request.getPreservationInfo().getPartDate().equals("MESES")) {
				c.add(Calendar.MONTH, request.getPreservationInfo().getTime());
			} else if (request.getPreservationInfo().getPartDate().equals("ANIOS")) {
				c.add(Calendar.YEAR, request.getPreservationInfo().getTime());
			}
			entity.getMetadataEntity().setPreservationDate(c.getTime());
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
		entity.getMetadataEntity().setLastModifiedDate(new Date());
		this.repository.save(entity);
	}

	@Override
	public void delete(Long id) {
		log.info("delete :: Eliminando recurso");
		DocumentEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException(String.format(NO_EXISTEN_RESOURCE_MESSAGE, id)));
		repository.delete(entity);
	}

	private void validateExistenceOfResource(String name, Long idParent) {
		MetadataEntity entity = this.metadataRepository.findByNameAndParent(name, idParent);
		if (entity != null) {
			log.info("validateExistenceOfResource :: El documento con nombre ({}) ya existe en la carpeta actual", name);
			throw new GeneralException(String.format(EXISTING_RESOURCE_MESSAGE, name));
		}
	}

	private DocumentEntity toPersist(DocumentRequestDto request, Date preservationDate, String logicalFolder) {
		DocumentEntity entity = new DocumentEntity();
		entity.setBinaryCode(request.getBinaryInfo().getFieldId());
		// Set valores metadata
		MetadataEntity metadataEntity = new MetadataEntity();
		// Set información general
		metadataEntity.setParent(request.getParent());
		JSONObject jsonObjectLogicalFolder = new JSONObject(logicalFolder);
		metadataEntity.setLocation(jsonObjectLogicalFolder.getString("location"));
		metadataEntity.setDocumentaryType(request.getDocumentaryType());
		// Set información del documento
		metadataEntity.setName(request.getDocumentInfo().getName());
		metadataEntity.setCreationUser(request.getDocumentInfo().getCreationUser());
		metadataEntity.setComment(request.getDocumentInfo().getComment());
		metadataEntity.setCreationDate(new Date());
		// Set información del contenido del documento
		metadataEntity.setContentType(request.getDontentInfo().getContentType());
		metadataEntity.setDocumentFamily(request.getDontentInfo().getDocumentFamily());
		metadataEntity.setSize(request.getDontentInfo().getSize());
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
		response.setLocation(entity.getMetadataEntity().getLocation());
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
		response.getContentInfo().setSize(entity.getMetadataEntity().getSize());
		// Set información del binario
		response.setBinaryInfo(new BinaryInfoDto());
		response.getBinaryInfo().setFieldId(entity.getMetadataEntity().getFieldId());
		// Set información preservación del documento
		response.setPreservationInfo(new PreservationInfo());
		response.getPreservationInfo().setPreservatoinDate(entity.getMetadataEntity().getPreservationDate());
		// Set información metadatos especificos
		if (entity.getMetadataEntity().getSpecificMetadata()!=null) {
			response.setSpecificMetadata(new ArrayList<>());
			for (SpecificMetadataEntity specificMetadataEntity : entity.getMetadataEntity().getSpecificMetadata()) {
				SpecificMetadataDto specificMetadataDto = new SpecificMetadataDto();
				specificMetadataDto.setId(specificMetadataEntity.getId());
				specificMetadataDto.setName(specificMetadataEntity.getName());
				specificMetadataDto.setValue(specificMetadataEntity.getValue());
				response.getSpecificMetadata().add(specificMetadataDto);
			}

		}
		return response;
	}

	private String invokeDocumentaryTypeMicroservice(Long id) {
		log.info("invokeDocumentaryTypeMicroservice :: Invocando microservicio documentarytype");
		String response = null;
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("documentary-type-id", id);
		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(properties.getUrlGetDocumentaryTypeById(),
					String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error(
					"invokeDocumentaryTypeMicroservice :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error(
					"invokeDocumentaryTypeMicroservice :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		}
		return response;
	}

	private String invokeLogicalFolderByIdMicroservice(Long id) {
		log.info("invokeLogicalFolderByIdMicroservice :: Invocando microservicio LogicalFolder");
		String response = null;
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", id);
		try {
			String prueba = properties.getUrlGetLogicalFolderById();
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(prueba, String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error(
					"invokeLogicalFolderByIdMicroservice :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error(
					"invokeLogicalFolderByIdMicroservice :: (HttpClientErrorException) falló el consumo del microservicio, error: {}zxxxhg",
					e.getCause());
			throw new GeneralException(e.getMessage());
		}
		return response;
	}

	private void invokeLogicalFolderAddChildren(DocumentEntity documentsEntity) {
		log.info("invokeLogicalFolderAddChildren :: Invocando microservicio LogicalFolder");
		// Uri params de la solicitud
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", documentsEntity.getMetadataEntity().getParent());
		// Cuerpo de la solicitud
		ChildrenInDto childrenInDto = new ChildrenInDto();
		childrenInDto.setId(documentsEntity.getId());
		childrenInDto.setName(documentsEntity.getMetadataEntity().getName());
		childrenInDto.setNodeType(2L);
		HttpEntity<ChildrenInDto> request = new HttpEntity<>(childrenInDto);
		try {
			restTemplate.postForEntity(properties.getUrlAddChildrenToFolder(), request, String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("invokeLogicalFolderAddChildren :: (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error("invokeLogicalFolderAddChildren :: (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		}
	}

	private Date getInfoDocumentaryUnit(DocumentaryUnitDto documentaryUnit) {
		String infoDocumentaryUnit = null;
		// Invocar microservicio correspondiente a la unidad documental
		if (documentaryUnit.getIdSerie() != null && documentaryUnit.getIdSubserie() != null) {
			infoDocumentaryUnit = this.invokeMicroserviceSunSerie(documentaryUnit.getIdSubserie());
		} else {
			infoDocumentaryUnit = this.invokeMicroserviceSerie(documentaryUnit.getIdSerie());
		}
		// Extraer fecha de preservación de la unidad documental
		JSONObject jsonObjectDocumentaryUnit = new JSONObject(infoDocumentaryUnit);
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, (int) jsonObjectDocumentaryUnit.getLong("retentionTime"));
		return c.getTime();
	}

	private String invokeMicroserviceSerie(Long idSerie) {
		log.info("invokeMicroserviceSerie :: Invocando microservicio serie para obtener la serie con id {}", idSerie);
		String response = "";
		// Parametros de URL
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("serie-id", idSerie);
		try {
			// consumo a microservicio
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(properties.getUrlFindSerieById(),
					String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error(
					"invokeMicroserviceSerie :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error(
					"invokeMicroserviceSerie :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		}

		return response;
	}

	private String invokeMicroserviceSunSerie(Long idSubSerie) {
		log.info("invokeMicroserviceSunSerie :: Invocando microservicio sub serie para obtener la subserie con id {}",
				idSubSerie);
		String response = "";
		// Parametros de URL
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("sub-serie-id", idSubSerie);
		try {
			// consumo a microservicio
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(properties.getUrlFindSubSerieById(),
					String.class, uriParams);
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.error(
					"invokeMicroserviceSunSerie :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error(
					"invokeMicroserviceSunSerie :: (HttpClientErrorException) falló el consumo del microservicio, error: {}",
					e.getCause());
			throw new GeneralException(e.getMessage());
		}

		return response;
	}

}
