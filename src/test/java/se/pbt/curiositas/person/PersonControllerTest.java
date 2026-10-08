package se.pbt.curiositas.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import se.pbt.curiositas.security.SecurityConfig;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static se.pbt.curiositas.person.StoredPersons.stored;

/**
 * Verifies the person endpoints at the HTTP level: paths, status codes, headers, JSON and error
 * responses. The service is replaced by a mock, so these tests run without a database.
 */
@WebMvcTest(PersonController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "curiositas.admin.password=test-password")
class PersonControllerTest {

    private static final UUID ID = UUID.fromString("7f1c8a52-0d1e-4f4b-9a7e-3c2b1d0e9f8a");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PersonService service;

    /** Returns the person as JSON with its version as ETag, which later guards replacements. */
    @Test
    void returnsPersonWithETag() {
        given(service.get(ID)).willReturn(stored(new Person("Edward Teach", Gender.MALE), ID, 3));

        assertThat(mvc.get().uri("/api/v1/persons/{id}", ID))
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .hasHeader("ETag", "\"3\"")
                .bodyJson()
                .hasPathSatisfying("$.id", id -> assertThat(id).asString().isEqualTo(ID.toString()))
                .hasPathSatisfying("$.name", name -> assertThat(name).asString().isEqualTo("Edward Teach"))
                .hasPathSatisfying("$.gender", gender -> assertThat(gender).asString().isEqualTo("MALE"));
    }

    /** Leaves unknown values out of the JSON instead of sending null, as the contract promises. */
    @Test
    void leavesUnknownValuesOutOfJson() {
        given(service.get(ID)).willReturn(stored(new Person("Edward Teach", Gender.MALE), ID, 0));

        assertThat(mvc.get().uri("/api/v1/persons/{id}", ID))
                .hasStatusOk()
                .bodyJson()
                .doesNotHavePath("$.birth")
                .doesNotHavePath("$.birthCountry")
                .doesNotHavePath("$.wikidataId")
                .doesNotHavePath("$.ageAtDeath");
    }

    /** Answers 404 as Problem Details when the person does not exist. */
    @Test
    void answersNotFoundForUnknownPerson() {
        given(service.get(ID)).willThrow(new PersonNotFoundException(ID));

        assertThat(mvc.get().uri("/api/v1/persons/{id}", ID))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.status", status -> assertThat(status).asNumber().isEqualTo(404))
                .hasPathSatisfying("$.detail", detail -> assertThat(detail).asString().contains(ID.toString()));
    }

    /** Answers 400 as Problem Details when the id is not a UUID. */
    @Test
    void answersBadRequestForMalformedId() {
        assertThat(mvc.get().uri("/api/v1/persons/not-a-uuid"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /** Returns the requested page with paging information. */
    @Test
    void listsOnePageOfPersons() {
        Person teach = stored(new Person("Edward Teach", Gender.MALE), ID, 0);
        given(service.list(1, 10)).willReturn(new PageImpl<>(List.of(teach), PageRequest.of(1, 10), 11));

        assertThat(mvc.get().uri("/api/v1/persons?page=1&size=10"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.content[0].name", name -> assertThat(name).asString().isEqualTo("Edward Teach"))
                .hasPathSatisfying("$.page", page -> assertThat(page).asNumber().isEqualTo(1))
                .hasPathSatisfying("$.totalElements", total -> assertThat(total).asNumber().isEqualTo(11))
                .hasPathSatisfying("$.totalPages", pages -> assertThat(pages).asNumber().isEqualTo(2));
    }

    /** Uses page 0 and 20 persons per page when the client does not ask for anything else. */
    @Test
    void usesDefaultPaging() {
        given(service.list(0, 20)).willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        assertThat(mvc.get().uri("/api/v1/persons")).hasStatusOk();
    }

    /** Answers 400 as Problem Details when paging parameters are outside the limits in the contract. */
    @Test
    void answersBadRequestForInvalidPaging() {
        assertThat(mvc.get().uri("/api/v1/persons?page=-1"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.detail", detail -> assertThat(detail).asString().contains("page"));
        assertThat(mvc.get().uri("/api/v1/persons?size=101"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.detail", detail -> assertThat(detail).asString().contains("size"));
    }

    /** Answers 501 for writes that are not built yet, instead of pretending they worked. */
    @Test
    void answersNotImplementedForWrites() {
        assertThat(mvc.post().uri("/api/v1/persons")
                .with(httpBasic("admin", "test-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Edward Teach\", \"gender\": \"MALE\"}"))
                .hasStatus(HttpStatus.NOT_IMPLEMENTED)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
