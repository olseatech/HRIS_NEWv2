package com.ian.web.fileupload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data public class FileDTO {
	private String filename;
	private String contentType;
	private String downloadUri;
	private long fileSize;
}
