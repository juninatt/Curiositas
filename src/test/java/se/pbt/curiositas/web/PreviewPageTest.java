package se.pbt.curiositas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import se.pbt.curiositas.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the development preview is the start page and is open to everyone, since it is
 * the quickest way to see that data flows from the database through the API.
 */
@SpringBootTest(properties = "curiositas.admin.password=test-password")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PreviewPageTest {

    @Autowired
    private MockMvcTester mvc;

    /** Uses the preview as start page, so it is what a visitor sees at the root URL. */
    @Test
    void usesPreviewAsStartPage() {
        assertThat(mvc.get().uri("/").accept(MediaType.TEXT_HTML))
                .hasStatusOk()
                .hasForwardedUrl("index.html");
    }

    /** Shows a read-only preview that reads persons from the public API. */
    @Test
    void showsPreviewOfPersons() {
        assertThat(mvc.get().uri("/index.html"))
                .hasStatusOk()
                .bodyText()
                .contains("Development preview")
                .contains("/api/v1/persons");
    }
}
