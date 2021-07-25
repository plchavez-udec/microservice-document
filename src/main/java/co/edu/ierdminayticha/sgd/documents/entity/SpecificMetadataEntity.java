package co.edu.ierdminayticha.sgd.documents.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "\"METADATOS_ESPECIFICOS_DOCUMENTO\"")
public class SpecificMetadataEntity{

	@Id
	@SequenceGenerator(name = "\"SEQ_METADATOS_ESPECIFICOS_DOCUMENTO_ID\"", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "\"SEQ_METADATOS_ESPECIFICOS_DOCUMENTO_ID\"")
	@Column(name = "\"ID\"")
	private Long id;

	@ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "\"ID_METADATA_FK\"")
	private MetadataEntity metadata;

	@Column(name = "\"NOMBRE\"")
	private String name;

	@Column(name = "\"VALOR\"")
	private String value;
}