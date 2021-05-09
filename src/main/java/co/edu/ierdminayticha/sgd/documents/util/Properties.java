package co.edu.ierdminayticha.sgd.documents.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class Properties {

	@Getter
	@Value("${url.microservice.documentary-type.find-by-id}")
	private String urlGetDocumentaryTypeById;

	@Getter
	@Value("${url.microservice.logical-fodler.find-by-id}")
	private String urlGetLogicalFolderById;

	@Getter
	@Value("${url.microservice.logical-fodler.add-children}")
	private String urlAddChildrenToFolder;

	@Getter
	@Value("${url.microservice.serie.find-by-id}")
	private String urlFindSerieById;

	@Getter
	@Value("${url.microservice.subserie.find-by-id}")
	private String urlFindSubSerieById;

}
