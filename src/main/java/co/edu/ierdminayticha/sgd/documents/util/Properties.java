package co.edu.ierdminayticha.sgd.documents.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class Properties {

	@Getter
	@Value("${url.microservice.documentary-unit.find-by-id}")
	private String urlGetDocumentaryUnitById;

	@Getter
	@Value("${url.microservice.documentary-type.find-by-id}")
	private String urlGetDocumentaryTypeById;
	
	@Getter
	@Value("${url.microservice.logical-fodler.find-by-id}")
	private String urlGetLogicalFolderById;
	
	@Getter
	@Value("${url.microservice.logical-fodler.add-children}")
	private String urlAddChildrenToFolder;

}
