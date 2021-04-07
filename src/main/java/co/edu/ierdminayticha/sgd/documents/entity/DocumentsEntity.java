package co.edu.ierdminayticha.sgd.documents.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "\"DOCUMENTOS\"")
public class DocumentsEntity {

	@Id
	@SequenceGenerator(name = "\"SEQ_DOCUMENTOS_ID\"", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "\"SEQ_DOCUMENTOS_ID\"")
	@Column(name = "\"ID\"")
	private Long id;

	@Column(name = "\"ID_BINARIO\"")
	private String binaryCode;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"ID\"")
	private MetadataEntity metadataEntity;

}
