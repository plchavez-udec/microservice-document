package co.edu.ierdminayticha.sgd.documents.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
import co.edu.ierdminayticha.sgd.documents.dto.DocumentaryTypeOutDto;
import co.edu.ierdminayticha.sgd.documents.dto.DocumentsResponseListDto;
import co.edu.ierdminayticha.sgd.documents.dto.PreservationInfo;
import co.edu.ierdminayticha.sgd.documents.dto.SecurityLevelDto;
import co.edu.ierdminayticha.sgd.documents.dto.SpecificMetadataDto;
import co.edu.ierdminayticha.sgd.documents.entity.DocumentsEntity;
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

	private static final String EXISTING_RESOURCE_MESSAGE = "El documento con nombre (%s) ya existe ";
	private static final String NO_EXISTEN_RESOURCE_MESSAGE = "No existe el recurso con id (%s) ";
	private static final String NO_EXISTEN_INFO_MESSAGE = "No existe información para mostrar";

	@Autowired
	private IDocumentsRepository repository;

	@Autowired
	private IMetadataRepository metadataRepository;

	@Autowired
	private ISecurityLevelRepository securityLevelRepository;

	@Autowired
	private Properties properties;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public DocumentResponseDto create(DocumentRequestDto request) {

		log.info("DocumentsServiceImpl : create - Creando recurso");

		validateExistenceOfTheResource(request.getDocumentInfo().getName());

		ResponseEntity<String> responseDocumentaryUnit = invokeDocumentaryUnitMicroservice(
				request.getDocumentaryUnit());

		ResponseEntity<String> responseDocumentaryType = invokeDocumentaryTypeMicroservice(
				request.getDocumentaryType());

		ResponseEntity<String> responseLogicalFolder = invokeLogicalFolderByIdMicroservice(request.getParent());

		DocumentsEntity entity = toPersist(request, responseDocumentaryUnit.getBody(), responseLogicalFolder.getBody());

		return createSuccessfulResponse(entity, responseDocumentaryType.getBody());

	}

	@Override
	public DocumentResponseDto findById(Long id) {

		log.info("DocumentsServiceImpl : findById - Consultando documento por Id");

		DocumentsEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException(String.format(NO_EXISTEN_RESOURCE_MESSAGE, id)));

		ResponseEntity<String> responseDocumentaryType = invokeDocumentaryTypeMicroservice(
				entity.getMetadataEntity().getDocumentaryType());

		return createSuccessfulResponse(entity, responseDocumentaryType.getBody());
	}

	@Override
	public List<DocumentsResponseListDto> findAll() {

		log.info("DocumentsServiceImpl : findAll - Consultando lista de  recursos");

		Iterable<DocumentsEntity> entityList = this.repository.findAll();

		if (entityList == null) {
			throw new NoSuchElementException(NO_EXISTEN_INFO_MESSAGE);
		}

		return createSuccessfulResponse(entityList);
	}

	@Override
	public void update(Long id, DocumentRequestDto dto) {

		log.info("DocumentsServiceImpl : update - Actualizando recurso");

		DocumentsEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException(String.format(NO_EXISTEN_RESOURCE_MESSAGE, id)));

		this.modelMapper.map(dto, entity);

		// entity.setLastModifiedDate(new Date());

		this.repository.save(entity);

	}

	@Override
	public void delete(Long id) {

		log.info("DocumentsServiceImpl : delete - Eliminando recurso");

		DocumentsEntity entity = this.repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException(String.format(NO_EXISTEN_RESOURCE_MESSAGE, id)));

		repository.delete(entity);

	}

	private void validateExistenceOfTheResource(String name) {

		MetadataEntity entity = this.metadataRepository.findByName(name);

		if (entity != null) {

			log.info("DocumentsServiceImpl : validateExistenceOfTheResource - "
					+ "el documento con nombre ({}) ya existe", name);

			throw new GeneralException(String.format(EXISTING_RESOURCE_MESSAGE, name));
		}

	}

	private DocumentsEntity toPersist(DocumentRequestDto request, String documentaryUnit, String logicalFolder) {

		DocumentsEntity entity = new DocumentsEntity();
		entity.setBinaryCode(request.getBinaryInfo().getFieldId());
		// Set valores metadata
		MetadataEntity metadataEntity = new MetadataEntity();
		// Set información general
		metadataEntity.setParent(request.getParent());
		JSONObject JSONObjectLogicalFolder = new JSONObject(logicalFolder);
		metadataEntity.setLocation(JSONObjectLogicalFolder.getString("location"));
		metadataEntity.setDocumentaryType(request.getDocumentaryType());
		metadataEntity.setDocumentaryUnit(request.getDocumentaryUnit());
		metadataEntity.setSecurityLevel(this.securityLevelRepository.findById(request.getSecurityLevel()).orElseThrow(
				() -> new NoSuchElementException("El identificador del nivelde seguridad informado no existe")));
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
		JSONObject JSONObjectDocumentaryUnit = new JSONObject(documentaryUnit);
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, (int) JSONObjectDocumentaryUnit.getLong("retentionTime"));
		metadataEntity.setPreservationDate(c.getTime());
		// Set información metadatos especificos
		if (!request.getSpecificMetadata().isEmpty()) {
			for (SpecificMetadataDto specificMetadataDto : request.getSpecificMetadata()) {
				SpecificMetadataEntity specificMetadataEntity = new SpecificMetadataEntity();
				specificMetadataEntity.setName(specificMetadataDto.getName());
				specificMetadataEntity.setValue(specificMetadataDto.getValue());
				metadataEntity.addSpecificMetadata(specificMetadataEntity);
			}

		}
		entity.setMetadataEntity(metadataEntity);
		DocumentsEntity entityOut =  repository.save(entity);
		
		//Invocar microservicio para agregar el documento al folder correspondinete
		invokeLogicalFolderAddChildren(entityOut);

		return entity;

	}

	private DocumentResponseDto createSuccessfulResponse(DocumentsEntity entity, String documentaryType) {

		DocumentResponseDto response = new DocumentResponseDto();
		// Set información general
		response.setId(entity.getId());
		response.setParent(entity.getMetadataEntity().getParent());
		response.setLocation(entity.getMetadataEntity().getLocation());
		// Set información tipo documental
		response.setDocumentaryType(new DocumentaryTypeOutDto());
		JSONObject JSONObjectDocumentaryType = new JSONObject(documentaryType);
		response.getDocumentaryType().setId(JSONObjectDocumentaryType.getLong("id"));
		response.getDocumentaryType().setName(JSONObjectDocumentaryType.getString("name"));
		// Set información nivel de seguridad
		response.setSecurityLevel(new SecurityLevelDto());
		response.getSecurityLevel().setId(entity.getMetadataEntity().getSecurityLevel().getId());
		response.getSecurityLevel().setDescripcion(entity.getMetadataEntity().getSecurityLevel().getDescripcion());
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
		if (!entity.getMetadataEntity().getSpecificMetadata().isEmpty()) {
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

	private List<DocumentsResponseListDto> createSuccessfulResponse(Iterable<DocumentsEntity> entityList) {

		List<DocumentsResponseListDto> dtoList = modelMapper.map(entityList,
				new TypeToken<List<DocumentsResponseListDto>>() {
				}.getType());

		return dtoList;

	}

	private ResponseEntity<String> invokeDocumentaryUnitMicroservice(Long id) {
		log.info("DocumentsServiceImpl : invokeDocumentaryUnitMicroservice - Invocando microservicio TRD");
		ResponseEntity<String> response = null;
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("documentary-unit-id", id);
		try {
			response = restTemplate.getForEntity(properties.getUrlGetDocumentaryUnitById(), String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("DocumentsServiceImpl : invokeDocumentaryUnitMicroservice - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error("DocumentsServiceImpl : invokeDocumentaryUnitMicroservice - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		}
		return response;
	}

	private ResponseEntity<String> invokeDocumentaryTypeMicroservice(Long id) {
		log.info("DocumentsServiceImpl : invokeDocumentaryTypeMicroservice - Invocando microservicio documentarytype");
		ResponseEntity<String> response = null;
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("documentary-type-id", id);
		try {
			response = restTemplate.getForEntity(properties.getUrlGetDocumentaryTypeById(), String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("DocumentsServiceImpl : invokeDocumentaryTypeMicroservice - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error("DocumentsServiceImpl : invokeDocumentaryTypeMicroservice - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		}
		return response;
	}

	private ResponseEntity<String> invokeLogicalFolderByIdMicroservice(Long id) {
		log.info("DocumentsServiceImpl : invokeLogicalFolderByIdMicroservice - Invocando microservicio LogicalFolder");
		ResponseEntity<String> response = null;
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", id);
		try {
			String prueba  = properties.getUrlGetLogicalFolderById();
			response = restTemplate.getForEntity(prueba, String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("DocumentsServiceImpl : invokeLogicalFolderByIdMicroservice - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error("DocumentsServiceImpl : invokeLogicalFolderByIdMicroservice - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}zxxxhg", e.getCause());
			throw new GeneralException(e.getMessage());
		}
		return response;
	}
	
	private void invokeLogicalFolderAddChildren(DocumentsEntity documentsEntity) {
		log.info("DocumentsServiceImpl : invokeLogicalFolderAddChildren - Invocando microservicio LogicalFolder");
		
		//Uri params de la solicitud
		Map<String, Object> uriParams = new HashMap<>();
		uriParams.put("logical-folder-id", documentsEntity.getMetadataEntity().getParent());
		
		//Cuerpo de la solicitud
		ChildrenInDto childrenInDto= new ChildrenInDto();
		childrenInDto.setId(documentsEntity.getId());
		childrenInDto.setName(documentsEntity.getMetadataEntity().getName());
		childrenInDto.setNodeType(2L);
		HttpEntity<ChildrenInDto> request = new HttpEntity<>(childrenInDto);
		
		try {
			restTemplate.postForEntity(properties.getUrlAddChildrenToFolder(), request, String.class, uriParams);
		} catch (HttpClientErrorException e) {
			log.error("DocumentsServiceImpl : invokeLogicalFolderAddChildren - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		} catch (RestClientException e) {
			log.error("DocumentsServiceImpl : invokeLogicalFolderAddChildren - (HttpClientErrorException) falló el "
					+ "consumo del microservicio, error: {}", e.getCause());
			throw new GeneralException(e.getMessage());
		}
	}

}
