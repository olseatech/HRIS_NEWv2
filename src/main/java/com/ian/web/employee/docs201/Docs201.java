package com.ian.web.employee.docs201;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.ian.web.employee.Employee;
import com.ian.web.systemsettings.document_type.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor 
@AllArgsConstructor
public class Docs201 {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate transDate;
	
	private String remarks;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;
	
	@Transient
	private String showMode;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_type_id")
	private DocumentType documentType;
	
	@ElementCollection
	private List<String> docFileUrls;
	
	@Transient
	private MultipartFile[] docFiles;

}
