package se.sundsvall.ai.flow.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowException;

class DocumentUtilTest {

	@Test
	void isPdf_true_and_extract() throws IOException {
		// create a minimal PDF in memory
		byte[] pdfBytes;
		try (var document = new PDDocument()) {
			document.addPage(new PDPage());
			try (var outputStream = new ByteArrayOutputStream()) {
				document.save(outputStream);
				pdfBytes = outputStream.toByteArray();
			}
		}

		assertThat(DocumentUtil.isPdf(pdfBytes)).isTrue();
		// extraction path should not throw
		DocumentUtil.extractTextFromPdf(pdfBytes);
	}

	@Test
	void isPdf_false_and_extract_throws() {
		var notPdf = "not a pdf".getBytes();
		assertThat(DocumentUtil.isPdf(notPdf)).isFalse();
		assertThatThrownBy(() -> DocumentUtil.extractTextFromPdf(notPdf))
			.isInstanceOf(FlowException.class)
			.hasMessageContaining("PDF");
	}

	@Test
	void isDocx_true_and_extract() throws Exception {
		byte[] docxBytes;
		try (var docx = new XWPFDocument()) {
			try (var outputStream = new ByteArrayOutputStream()) {
				docx.write(outputStream);
				docxBytes = outputStream.toByteArray();
			}
		}

		assertThat(DocumentUtil.isDocx(docxBytes)).isTrue();
		DocumentUtil.extractTextFromDocx(docxBytes);
	}

	@Test
	void isDocx_false_and_extract_throws() {
		var notDocx = new byte[] {
			0x01, 0x02, 0x03
		};
		assertThat(DocumentUtil.isDocx(notDocx)).isFalse();
		assertThatThrownBy(() -> DocumentUtil.extractTextFromDocx(notDocx))
			.isInstanceOf(FlowException.class)
			.hasMessageContaining("DOCX");
	}
}
