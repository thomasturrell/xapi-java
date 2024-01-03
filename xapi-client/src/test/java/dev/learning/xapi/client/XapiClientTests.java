/*
 * Copyright 2016-2023 Berry Cloud Ltd. All rights reserved.
 */
package dev.learning.xapi.client;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.learning.xapi.model.About;
import dev.learning.xapi.model.Activity;
import dev.learning.xapi.model.Person;
import dev.learning.xapi.model.Statement;
import dev.learning.xapi.model.StatementFormat;
import dev.learning.xapi.model.Verb;
import java.net.URI;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError;

/**
 * XapiClient Tests.
 *
 * @author Thomas Turrell-Croft
 */
@DisplayName("XapiClient Tests")
@SpringBootTest
class XapiClientTests {

  @Autowired
  private WebClient.Builder webClientBuilder;

  private MockWebServer mockWebServer;
  private XapiClient client;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    webClientBuilder.baseUrl(mockWebServer.url("").toString());

    client = new XapiClient(webClientBuilder);

  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  // Get Statement

  @Test
  void whenGettingStatementThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingStatementThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statement
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6"));
  }

  @Test
  void whenGettingStatementThenBodyIsInstanceOfStatement() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Statement
    final var response =
        client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    // Then Body Is Instance Of Statement
    assertThat(response.getBody(), instanceOf(Statement.class));
  }

  @Test
  void whenGettingStatementWithAttachmentsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statement With Attachments
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").attachments(true))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&attachments=true"));
  }

  @Test
  void whenGettingStatementWithCanonicalFormatThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statement With Canonical Format
    client
        .getStatement(
            r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").format(StatementFormat.CANONICAL))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&format=canonical"));
  }

  // Posting Statements

  @Test
  void whenPostingStatementsThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final var attemptedStatement = Statement.builder()

        .agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final var passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    final List<Statement> statements = Arrays.asList(attemptedStatement, passedStatement);

    // When posting Statements
    client.postStatements(r -> r.statements(statements)).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingStatementsThenBodyIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final var attemptedStatement = Statement.builder()

        .agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final var passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    // When Posting Statements
    client.postStatements(r -> r.statements(attemptedStatement, passedStatement)).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(), is(
        "[{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}},{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/passed\",\"display\":{\"und\":\"passed\"}},\"object\":{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}]"));
  }

  @Test
  void whenPostingStatementsArrayThenBodyIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final var attemptedStatement = Statement.builder()

        .agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final var passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    final List<Statement> statements = Arrays.asList(attemptedStatement, passedStatement);

    // When Posting Statements Array
    client.postStatements(r -> r.statements(statements)).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(), is(
        "[{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}},{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/passed\",\"display\":{\"und\":\"passed\"}},\"object\":{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}]"));
  }

  @Test
  void whenPostingStatementsThenContentTypeHeaderIsApplicationJson() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final var attemptedStatement = Statement.builder()

        .agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final var passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    final List<Statement> statements = Arrays.asList(attemptedStatement, passedStatement);

    // When Posting Statements
    client.postStatements(r -> r.statements(statements)).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPostingStatementsThenResponseBodyIsInstanceOfUUIDArray() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody(
            "[\"2eb84e56-441a-492c-9d7b-f8e9ddd3e15d\",\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .addHeader("Content-Type", "application/json"));

    final var attemptedStatement = Statement.builder()

        .agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final var passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    final List<Statement> statements = Arrays.asList(attemptedStatement, passedStatement);

    // When Posting Statements
    final var response = client.postStatements(r -> r.statements(statements)).block();

    // Then Response Body Is Instance Of UUID Array
    assertThat(response.getBody(), instanceOf(List.class));
  }

  // Posting a Statement

  @Test
  void whenPostingStatementThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When posting Statement
    client
        .postStatement(
            r -> r
                .statement(
                    s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

                        .verb(Verb.ATTEMPTED)

                        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingStatementThenBodyIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Posting Statement
    client
        .postStatement(
            r -> r
                .statement(
                    s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

                        .verb(Verb.ATTEMPTED)

                        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(), is(
        "{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}"));
  }

  @Test
  void whenPostingStatementThenContentTypeHeaderIsApplicationJson() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Posting Statement
    client
        .postStatement(
            r -> r
                .statement(
                    s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

                        .verb(Verb.ATTEMPTED)

                        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void givenApiResponseIsEmptyWhenPostingStatementThenMissingResponseBodyExceptionIsThrown() {

    // Given Api Response Is Empty
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setHeader("Content-Type",
        "application/json"));

    // When Posting Statement
    final var response = client.postStatement(r -> r
        .statement(s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .verb(Verb.ATTEMPTED)

            .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))));

    // Then MissingResponseBodyException Is Thrown
    assertThrows(MissingResponseBodyException.class, () -> response.block());

  }

  @Test
  void givenApiResponseIsBadRequestWhenPostingStatementThenBadRequestIsThrown() {

    // Given Api Response Is Bad Request
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 400 Bad Request"));

    // When Posting Statement
    final var response = client.postStatement(r -> r
        .statement(s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .verb(Verb.ATTEMPTED)

            .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))));

    // Then BadRequest Is Thrown
    assertThrows(BadRequest.class, () -> response.block());

  }

  @Test
  void givenApiResponseIsInternalServerErrorWhenPostingStatementThenInternalServerErrorIsThrown() {

    // Given Api Response Is Internal Server Error
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 500 Internal Server Error"));

    // When Posting Statement
    final var response = client.postStatement(r -> r
        .statement(s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .verb(Verb.ATTEMPTED)

            .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))));

    // Then InternalServerError Is Thrown
    assertThrows(InternalServerError.class, () -> response.block());

  }

  // Posting a Signed Statement

  @Test
  void whenPostingSignedStatementThenExceptionIsThrown() throws NoSuchAlgorithmException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    final var keyPair = keyPairGenerator.generateKeyPair();

    // When posting Signed Statement Then Exception Is Thrown
    // ( Signing statements requires additional dependencies which are
    // NOT included in these tests by default. )
    assertThrows(IllegalStateException.class,
        () -> client.postStatement(r -> r
            .signedStatement(
                s -> s.agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

                    .verb(Verb.ATTEMPTED)

                    .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                        .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement"))),

                keyPair.getPrivate())

            .build()));

  }

  // Get Voided Statement

  @Test
  void whenGettingVoidedStatementThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement
    client.getVoidedStatement(
        r -> r.voidedId(UUID.fromString("4df42866-40e7-45b6-bf7c-8d5fccbdccd6"))).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingVoidedStatementThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement
    client.getVoidedStatement(r -> r.voidedId("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?voidedStatementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6"));
  }

  @Test
  void whenGettingVoidedStatementWithAttachmentsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement With Attachments
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").attachments(true))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&attachments=true"));
  }

  @Test
  void whenGettingVoidedStatementWithCanonicalFormatThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement With Canonical Format
    client
        .getStatement(
            r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").format(StatementFormat.CANONICAL))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&format=canonical"));
  }

  // Get Statements

  @Test
  void whenGettingStatementsThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements
    client.getStatements().block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingStatementsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements
    client.getStatements().block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is("/statements"));
  }

  @Test
  void whenGettingStatementsWithAllParametersThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With All Parameters
    client.getStatements(r -> r

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ANSWERED)

        .activity("https://example.com/activity/1")

        .registration("dbf5d9e8-d2aa-4d57-9754-b11e3f195fe3")

        .relatedActivities(true)

        .relatedAgents(true)

        .since(Instant.parse("2016-01-01T00:00:00Z"))

        .until(Instant.parse("2018-01-01T00:00:00Z"))

        .limit(10)

        .format(StatementFormat.CANONICAL)

        .attachments(true)

        .ascending(true)

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/statements?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&verb=http%3A%2F%2Fadlnet.gov%2Fexpapi%2Fverbs%2Fanswered&activity=https%3A%2F%2Fexample.com%2Factivity%2F1&since=2016-01-01T00%3A00%3A00Z&until=2018-01-01T00%3A00%3A00Z&registration=dbf5d9e8-d2aa-4d57-9754-b11e3f195fe3&related_activities=true&related_agents=true&limit=10&format=canonical&attachments=true&ascending=true"));
  }

  @Test
  void whenGettingStatementsWithAgentParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Agent Parameter
    client.getStatements(r -> r

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/statements?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  @Test
  void whenGettingStatementsWithVerbParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Verb Parameter
    client.getStatements(r -> r

        .verb("http://adlnet.gov/expapi/verbs/answered")

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?verb=http%3A%2F%2Fadlnet.gov%2Fexpapi%2Fverbs%2Fanswered"));
  }

  @Test
  void whenGettingStatementsWithActivityParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Activity Parameter
    client.getStatements(r -> r

        .activity("https://example.com/activity/1")

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?activity=https%3A%2F%2Fexample.com%2Factivity%2F1"));
  }

  @Test
  void whenGettingMoreStatementsThenRequestMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Activity Parameter
    client.getMoreStatements(r -> r

        .more(mockWebServer.url("/xapi/statements/869cc589-76fa-4283-8e96-eea86f9124e1").uri())

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingMoreStatementsThenRequestURLExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Activity Parameter
    client.getMoreStatements(r -> r

        .more(URI.create("/xapi/statements/869cc589-76fa-4283-8e96-eea86f9124e1"))

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Request URL Is Expected
    assertThat(recordedRequest.getRequestUrl(),
        is(mockWebServer.url("/xapi/statements/869cc589-76fa-4283-8e96-eea86f9124e1")));
  }

  // Get Single State

  @Test
  void whenGettingASingleStateThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single State
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark"), String.class).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single State
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark"), String.class).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenGettingASingleStateWithoutRegistrationThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Getting A Single State Without Registration
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark"), String.class)

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Getting A Single State Without Registration
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark"), String.class)

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark"));
  }

  @Test
  void givenStateContentTypeIsTextPlainWhenGettingStateThenBodyIsInstanceOfString()
      throws InterruptedException {

    // Given State Content Type Is Text Plain
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("Hello World!")
        .addHeader("Content-Type", "text/plain; charset=utf-8"));

    // When Getting State
    final var response = client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark"), String.class)

        .block();

    // Then Body Is Instance Of String
    assertThat(response.getBody(), instanceOf(String.class));
  }

  @Test
  void givenStateContentTypeIsTextPlainWhenGettingStateThenBodyIsExpected()
      throws InterruptedException {

    // Given State Content Type Is Text Plain
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("Hello World!")
        .addHeader("Content-Type", "text/plain; charset=utf-8"));

    // When Getting State
    final var response = client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark"), String.class)

        .block();

    // Then Body Is Expected
    assertThat(response.getBody(), is("Hello World!"));
  }

  // Post Single State

  @Test
  void whenPostingASingleStateThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenPostingASingleStateWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State With Content Type Text Plain
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPostingASingleStateWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State Without Content Type
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPostingASingleStateWithoutRegistrationThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State Without Registration
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State Without Registration
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark"));
  }

  // Put Single State

  @Test
  void whenPuttingASingleStateThenMethodIsPut() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("PUT"));
  }

  @Test
  void whenPuttingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenPuttingASingleStateWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State With Content Type Text Plain
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPuttingASingleStateWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State Without Content Type
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPuttingASingleStateWithoutRegistrationThenMethodIsPut() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State Without Registration
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("PUT"));
  }

  @Test
  void whenPuttingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State Without Registration
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark"));
  }

  // Deleting Single State

  @Test
  void whenDeletingASingleStateThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenDeletingASingleStateWithoutRegistrationThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State Without Registration
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State Without Registration
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  // Getting Multiple States

  @Test
  void whenGettingMultipleStatesThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingMultipleStatesThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6"));
  }

  @Test
  void whenGettingMultipleStatesWithoutRegistrationThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States Without Registration
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingMultipleStatesWithoutRegistrationThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States Without Registration
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  @Test
  void givenMultipleStatesExistWhenGettingMultipleStatesThenBodyIsInstanceOfStringArray()
      throws InterruptedException {

    // Given Multiple States Exist
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    final var response = client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    // Then Body Is Instance Of String Array
    assertThat(response.getBody(), instanceOf(List.class));
  }

  @Test
  void givenMultipleStatesExistWhenGettingMultipleStatesThenBodyIsExpected()
      throws InterruptedException {

    // Given Multiple States Exist
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    final var response = client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    // Then Body Is Expected
    assertThat(response.getBody(), is(Arrays.asList("State1", "State2", "State3")));
  }

  // Deleting Multiple States

  @Test
  void whenDeletingMultipleStatesThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingMultipleStatesThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6"));
  }

  @Test
  void whenDeletingMultipleStatesWithoutRegistrationThenMethodIsDelete()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States Without Registration
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingMultipleStatesWithoutRegistrationThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States Without Registration
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

    ).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  // Get Single Agent Profile

  @Test
  void whenGettingASingleAgentProfileThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single Agent Profile
    client.getAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting"), String.class).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingASingleAgentProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single Agent Profile
    client.getAgentProfile(r -> r

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting"), String.class)

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/agents/profile?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&profileId=greeting"));
  }

  // Delete Single Agent Profile

  @Test
  void whenDeletingASingleAgentProfileThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Deleting A Single Agent Profile
    client.deleteAgentProfile(r -> r

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletetingASingleAgentProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Deleting A Single Agent Profile
    client.deleteAgentProfile(
        r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .profileId("greeting"))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/agents/profile?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&profileId=greeting"));
  }

  // Put Single Agent Profile

  @Test
  void whenPuttingASingleAgentProfileThenMethodIsPut() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Putting A Single Agent Profile
    client.putAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting")

        .profile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Put
    assertThat(recordedRequest.getMethod(), is("PUT"));
  }

  @Test
  void whenPuttingASingleAgentProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Putting A Single Agent Profile
    client.putAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting")

        .profile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/agents/profile?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&profileId=greeting"));
  }

  @Test
  void whenPuttingASingleAgentProfileWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single Agent Profile With Content Type Text Plain
    client.putAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting")

        .profile("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPuttingASingleAgentProfileWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single Agent Profile Without Content Type
    client.putAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("greeting")

        .profile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPuttingASingleAgentProfileWithoutContentTypeThenBodyIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Putting A Single Agent Profile Without Content Type
    client.putAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .profileId("person")

        .profile(new SamplePerson("A N", "Other")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(),
        is("{\"firstName\":\"A N\",\"lastName\":\"Other\"}"));
  }

  // Post Single Agent Profile

  @Test
  void whenPostingASingleAgentProfileThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Posting A Single Agent Profile
    client
        .postAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .profileId("greeting")

            .profile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingASingleAgentProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Posting A Single Agent Profile
    client
        .postAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .profileId("greeting")

            .profile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/agents/profile?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&profileId=greeting"));
  }

  @Test
  void whenPostingASingleAgentProfileWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single Agent Profile With Content Type Text Plain
    client
        .postAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .profileId("greeting")

            .profile("Hello World!")

            .contentType(MediaType.TEXT_PLAIN))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPostingASingleAgentProfileWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single Agent Profile Without Content Type
    client
        .postAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .profileId("greeting")

            .profile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPostingASingleAgentProfileWithoutContentTypeThenBodyIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Posting A Single Agent Profile Without Content Type
    client
        .postAgentProfile(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .profileId("person")

            .profile(new SamplePerson("A N", "Other")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(),
        is("{\"firstName\":\"A N\",\"lastName\":\"Other\"}"));
  }

  @Test
  void whenGettingProfilesThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Getting Profiles
    client
        .getAgentProfiles(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingProfilesThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Getting Profiles
    client
        .getAgentProfiles(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/agents/profile?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  @Test
  void whenGettingProfilesWithSinceParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Getting Profiles
    client
        .getAgentProfiles(r -> r.agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .since(Instant.parse("2016-01-01T00:00:00Z")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/agents/profile?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&since=2016-01-01T00%3A00%3A00Z"));
  }

  // Get Activity

  @Test
  void whenGettingActivityByUriThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Activity By Uri
    client
        .getActivity(r -> r.activityId(URI.create("https://example.com/activity/simplestatement")))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingActivityByStringThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Activity By String
    client.getActivity(r -> r.activityId("https://example.com/activity/simplestatement")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingActivityThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Activity
    client
        .getActivity(r -> r.activityId(URI.create("https://example.com/activity/simplestatement")))
        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/activities?activityId=https%3A%2F%2Fexample.com%2Factivity%2Fsimplestatement"));
  }

  @Test
  void whenGettingActivityThenBodyIsInstanceOfActivity() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Activity
    final var response = client
        .getActivity(r -> r.activityId(URI.create("https://example.com/activity/simplestatement")))
        .block();

    // Then Body Is Instance Of Activity
    assertThat(response.getBody(), instanceOf(Activity.class));
  }

  // Get Agents

  @Test
  void whenGettingAgentsThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Agents
    client.getAgents(r -> r.agent(a -> a.mbox("mailto:another@example.com"))).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingAgentsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Agents
    client.getAgents(r -> r.agent(a -> a.mbox("mailto:another@example.com"))).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/agents?agent=%7B%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  @Test
  void whenGettingAgentsThenBodyIsInstanceOfPerson() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"name\":[\"A N Other\"],\"mbox\":[\"mailto:another@example.com\"],\"objectType\":\"Person\"}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Agents
    final var response =
        client.getAgents(r -> r.agent(a -> a.mbox("mailto:another@example.com"))).block();

    // Then Body Is Instance Of Activity
    assertThat(response.getBody(), instanceOf(Person.class));
  }

  // Get About

  @Test
  void whenGettingAboutThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"extensions\":{\"https://example.com/extensions/test\":{\"name\":\"Example extension\"}},\"version\":[\"0.9\",\"0.95\",\"1.0.3\"]}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting About
    client.getAbout().block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingAboutThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"extensions\":{\"https://example.com/extensions/test\":{\"name\":\"Example extension\"}},\"version\":[\"0.9\",\"0.95\",\"1.0.3\"]}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting About
    client.getAbout().block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is("/about"));
  }

  @Test
  void whenGettingAboutThenBodyIsInstanceOfAbout() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"extensions\":{\"https://example.com/extensions/test\":{\"name\":\"Example extension\"}},\"version\":[\"0.9\",\"0.95\",\"1.0.3\"]}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting About
    final var response = client.getAbout().block();

    // Then Body Is Instance Of About
    assertThat(response.getBody(), instanceOf(About.class));
  }

  // Get Single Activity Profile

  @Test
  void whenGettingASingleActivityProfileThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single Activity Profile
    client.getActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark"), String.class).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingASingleActivityProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single Activity Profile
    client.getActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark"), String.class).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/profile?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&profileId=bookmark"));
  }

  @Test
  void givenActivityProfileContentTypeIsTextPlainWhenGettingActivityProfileThenBodyIsInstanceOfString()
      throws InterruptedException {

    // Given ActivityProfile Content Type Is Text Plain
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("Hello World!")
        .addHeader("Content-Type", "text/plain; charset=utf-8"));

    // When Getting ActivityProfile
    final var response = client
        .getActivityProfile(r -> r.activityId("https://example.com/activity/1")

            .profileId("bookmark"), String.class)

        .block();

    // Then Body Is Instance Of String
    assertThat(response.getBody(), instanceOf(String.class));
  }

  @Test
  void givenActivityProfileContentTypeIsTextPlainWhenGettingActivityProfileThenBodyIsExpected()
      throws InterruptedException {

    // Given ActivityProfile Content Type Is Text Plain
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("Hello World!")
        .addHeader("Content-Type", "text/plain; charset=utf-8"));

    // When Getting ActivityProfile
    final var response = client
        .getActivityProfile(r -> r.activityId("https://example.com/activity/1")

            .profileId("bookmark"), String.class)

        .block();

    // Then Body Is Expected
    assertThat(response.getBody(), is("Hello World!"));
  }

  // Post Single Activity Profile

  @Test
  void whenPostingASingleActivityProfileThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single Activity Profile
    client.postActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingASingleActivityProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single Activity Profile
    client.postActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/profile?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&profileId=bookmark"));
  }

  @Test
  void whenPostingASingleActivityProfileWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single Activity Profile With Content Type Text Plain
    client.postActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPostingASingleActivityProfileWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single Activity Profile Without Content Type
    client.postActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  // Put Single Activity Profile

  @Test
  void whenPuttingASingleActivityProfileThenMethodIsPut() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single Activity Profile
    client.putActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("PUT"));
  }

  @Test
  void whenPuttingASingleActivityProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single Activity Profile
    client.putActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/profile?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&profileId=bookmark"));
  }

  @Test
  void whenPuttingASingleActivityProfileWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single Activity Profile With Content Type Text Plain
    client.putActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPuttingASingleActivityProfileWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single Activity Profile Without Content Type
    client.putActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")

        .activityProfile("Hello World!"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  // Deleting Single Activity Profile

  @Test
  void whenDeletingASingleActivityProfileThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single Activity Profile
    client.deleteActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingASingleActivityProfileThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single Activity Profile
    client.deleteActivityProfile(r -> r.activityId("https://example.com/activity/1")

        .profileId("bookmark")).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/profile?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&profileId=bookmark"));
  }

  @Test
  void whenGettingActivityProfilesWithSinceParameterThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("[\"bookmark\"]")
        .setHeader("Content-Type", "application/json"));

    // When Getting Profiles With Since Parameter
    client.getActivityProfiles(r -> r.activityId("https://example.com/activity/1")

        .since(Instant.parse("2016-01-01T00:00:00Z")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingActivityProfilesWithSinceParameterThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("[\"bookmark\"]")
        .setHeader("Content-Type", "application/json"));

    // When Getting Profiles With Since Parameter
    client.getActivityProfiles(r -> r.activityId("https://example.com/activity/1")

        .since(Instant.parse("2016-01-01T00:00:00Z")))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/profile?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&since=2016-01-01T00%3A00%3A00Z"));
  }

  @Test
  void whenGettingActivityProfilesWithoutSinceParameterThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("[\"bookmark\"]")
        .setHeader("Content-Type", "application/json"));

    // When Getting Profiles Without Since Parameter
    client.getActivityProfiles(r -> r.activityId("https://example.com/activity/1"))

        .block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/activities/profile?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1"));
  }

  @Test
  void whenGettingStatementIteratorViaMultipeResponsesThenResultIsExpected()
      throws InterruptedException {
    final var body1 = """
        {
          "statements" : [
            {
              "id" : "c0aaea0b-252b-4d9d-b7ad-46c541572570"
            }
          ],
          "more" : "/statements/more/1"
        }
        """;
    final var body2 = """
        {
          "statements" : [
            {
              "id" : "4ed0209a-f50f-4f57-8602-ba5f981d211a"
            }
          ],
          "more" : ""
        }
        """;

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body1)
        .addHeader("Content-Type", "application/json; charset=utf-8"));
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body2)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator Via Multipe Responses
    final var iterator = client.getStatementIterator().block();

    // Then Result Is Expected
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next().getId(),
        is(UUID.fromString("c0aaea0b-252b-4d9d-b7ad-46c541572570")));
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next().getId(),
        is(UUID.fromString("4ed0209a-f50f-4f57-8602-ba5f981d211a")));
    assertThat(iterator.hasNext(), is(false));

  }

  @Test
  void givenApiResponseIsEmptyWhenGettingStatementIteratorThenMissingResponseBodyExceptionIsThrown() {

    // Given Api Response Is Empty
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setHeader("Content-Type",
        "application/json"));

    // When Getting Statement Iterator
    final var response = client.getStatementIterator();

    // Then MissingResponseBodyException Is Thrown
    assertThrows(MissingResponseBodyException.class, () -> response.block());

  }

  @Test
  void whenGettingStatementIteratorViaMultipeResponsesThenRequestsAreExpected()
      throws InterruptedException {
    final var body1 = """
        {
          "statements" : [
            {
              "id" : "c0aaea0b-252b-4d9d-b7ad-46c541572570"
            }
          ],
          "more" : "/statements/more/1"
        }
        """;
    final var body2 = """
        {
          "statements" : [
            {
              "id" : "4ed0209a-f50f-4f57-8602-ba5f981d211a"
            }
          ],
          "more" : ""
        }
        """;

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body1)
        .addHeader("Content-Type", "application/json; charset=utf-8"));
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body2)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator Via Multipe Responses
    final var iterator = client.getStatementIterator().block();
    iterator.next();
    iterator.next();

    // Then Requests Are Expected
    assertThat(mockWebServer.takeRequest().getPath(), is("/statements"));
    assertThat(mockWebServer.takeRequest().getPath(), is("/statements/more/1"));

  }

  @Test
  void whenGettingStatementIteratorThenRequestsAreExpected() throws InterruptedException {
    final var body1 = """
        {
          "statements" : [
            {
              "id" : "c0aaea0b-252b-4d9d-b7ad-46c541572570"
            }
          ],
          "more" : "/statements/more/1"
        }
        """;
    final var body2 = """
        {
          "statements" : [
            {
              "id" : "4ed0209a-f50f-4f57-8602-ba5f981d211a"
            }
          ],
          "more" : ""
        }
        """;

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body1)
        .addHeader("Content-Type", "application/json; charset=utf-8"));
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body2)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator
    client.getStatementIterator().block();

    // Then Requests Are Expected
    assertThat(mockWebServer.takeRequest().getPath(), is("/statements"));
    assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS), is(nullValue()));

  }

  @Test
  void whenGettingStatementIteratorAndProcessingItAsStreamThenRequestsAreExpected()
      throws InterruptedException {
    final var body1 = """
        {
          "statements" : [
            {
              "id" : "c0aaea0b-252b-4d9d-b7ad-46c541572570"
            },
            {
              "id" : "940a3f5c-1f31-47c7-82fc-5979e2786c02"
            }
          ],
          "more" : "/statements/more/1"
        }
        """;
    final var body2 = """
        {
          "statements" : [
            {
              "id" : "4ed0209a-f50f-4f57-8602-ba5f981d211a"
            }
          ],
          "more" : ""
        }
        """;

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body1)
        .addHeader("Content-Type", "application/json; charset=utf-8"));
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body2)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator
    final var iterator = client.getStatementIterator().block();

    // And Processing it As Stream
    iterator.toStream().limit(1).forEach(s -> {
    });

    // Then Requests Are Expected
    assertThat(mockWebServer.takeRequest().getPath(), is("/statements"));
    assertThat(mockWebServer.takeRequest(1, TimeUnit.SECONDS), is(nullValue()));

  }

  @Test
  void givenEmptyStatementResultWhenGettingStatementIteratorThenHasNextIsFalse()
      throws InterruptedException {

    // Given Empty StatementResult
    final var body = """
        {
          "statements" : [
          ],
          "more" : ""
        }
        """;

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator
    final var iterator = client.getStatementIterator().block();

    // Then HasNext Is False
    assertThat(iterator.hasNext(), is(false));

  }

  @Test
  void givenEmptyResponseWhenGettingStatementIteratorThenHasNextIsFalse()
      throws InterruptedException {

    // Given Empty Response
    // This response is technically invalid by the xAPI specification, but we cannot assume
    // conformance.
    // conformance of the commercial LRSs.
    final var body = "{}";

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator
    final var iterator = client.getStatementIterator().block();

    // Then HasNext Is False
    assertThat(iterator.hasNext(), is(false));

  }

  @Test
  void givenEmptyResponseWhenGettingStatementIteratorThenNextThrowsAnException()
      throws InterruptedException {

    // Given Empty Response
    final var body = """
        {
          "statements" : [
          ],
          "more" : ""
        }
        """;

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody(body)
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting StatementIterator
    final var iterator = client.getStatementIterator().block();

    // Then Next Throws An Exception
    assertThrows(NoSuchElementException.class, () -> iterator.next());

  }

  @Test
  void whenVoidingStatementThenBodyIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"2eb84e56-441a-492c-9d7b-f8e9ddd3e15d\"]")
        .addHeader("Content-Type", "application/json"));

    final var attemptedStatement = Statement.builder()

        .id(UUID.fromString("175c9264-692f-4108-9b7d-0ba64bd59ac3"))

        .agentActor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    // When Voiding Statement
    client.voidStatement(attemptedStatement).block();

    final var recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(), is(
        "{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/voided\",\"display\":{\"und\":\"voided\"}},\"object\":{\"id\":\"175c9264-692f-4108-9b7d-0ba64bd59ac3\",\"objectType\":\"StatementRef\"}}"));
  }

  @Getter
  private static class SamplePerson {

    private final String firstName;
    private final String lastName;

    public SamplePerson(String firstName, String lastName) {
      super();
      this.firstName = firstName;
      this.lastName = lastName;
    }

  }

}
