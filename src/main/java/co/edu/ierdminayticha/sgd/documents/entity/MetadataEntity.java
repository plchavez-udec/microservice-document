package co.edu.ierdminayticha.sgd.documents.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "\"METADATOS\"")
public class MetadataEntity {

	@Id
	@SequenceGenerator(name = "\"SEQ_METADATA_ID\"", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "\"SEQ_METADATA_ID\"")
	@Column(name = "\"ID\"")
	private Long id;
	@Column(name = "\"ID_TIPO_DOCUMENTAL_FK\"")
	private Long documentaryType;
	@Column(name = "\"ID_SERIE_FK\"")
	private Long idSerie;
	@Column(name = "\"ID_SUBSERIE_FK\"")
	private Long idSubSerie;
	@Column(name = "\"ID_CARPETA_PADRE_FK\"")
	private Long parent;
	@Column(name = "\"NOMBRE\"")
	private String name;
	@Column(name = "\"COMENTARIO\"")
	private String comment;
	@Column(name = "\"TAMANO\"")
	private Double size;
	@Column(name = "\"CODIGO_BINARIO\"")
	private String fieldId;
	@Column(name = "\"FECHA_CONSERVACION\"")
	private LocalDate preservationDate;
	@Column(name = "\"FUNCIONAL_ID\"")
	private Long funcionalId;
	@Column(name = "\"FUNIONAL_VERSION\"")
	private String funcionalVersion;
	@Column(name = "\"TIPO_CONTENIDO\"")
	private String contentType;
	@Column(name = "\"FAMILIA_DOCUMENTO\"")
	private String documentFamily;
	@Column(name = "\"USUARIO_CREA\"")
	private String creationUser;
	@Column(name = "\"FECHA_CREACION\"")
	private LocalDate creationDate;
	@Column(name = "\"FECHA_MODIFICACION\"")
	private Date lastModifiedDate;
	@OneToMany(mappedBy = "metadata", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	private List<SpecificMetadataEntity> specificMetadata;

	public void addSpecificMetadata(SpecificMetadataEntity specificMetadataEntity) {
		if (specificMetadata == null)
			specificMetadata = new ArrayList<>();
		specificMetadata.add(specificMetadataEntity);
		specificMetadataEntity.setMetadata(this);
	}

}
