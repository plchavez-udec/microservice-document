package co.edu.ierdminayticha.sgd.documents.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "\"NIVELES_SEGURIDAD\"")
public class SecurityLevelEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "\"SEQ_NIVELES_SEGURIDAD_ID\"", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "\"SEQ_NIVELES_SEGURIDAD_ID\"")
	@Column(name = "\"ID\"")
	private Long id;

	@Column(name = "\"DESCRIPCION\"")
	private String descripcion;

}