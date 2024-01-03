/*
 * Copyright 2016-2023 Berry Cloud Ltd. All rights reserved.
 */

package dev.learning.xapi.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.learning.xapi.model.Agent.AgentObjectType;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

/**
 * SubStatement Tests.
 *
 * @author Lukáš Sahula
 * @author Martin Myslik
 * @author István Rátkai (Selindek)
 */
@DisplayName("SubStatement tests")
class SubStatementTests {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void whenDeserializingSubStatementThenResultIsInstanceOfSubStatement() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Result Is Instance Of SubStatement
    assertThat(result, instanceOf(SubStatement.class));

  }

  @Test
  void whenDeserializingSubStatementThenTimestampIsExpected() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Timestamp Is Expected
    assertThat(result.getTimestamp(), is(Instant.parse("2015-11-18T11:17:00Z")));

  }

  @Test
  void whenDeserializingSubStatementThenVerbIsInstanceOfVerb() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Verb Is Instance Of Verb
    assertThat(result.getVerb(), instanceOf(Verb.class));

  }

  @Test
  void whenDeserializingSubStatementThenObjectIsInstanceOfStatementReference() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Object Is Instance Of StatementReference
    assertThat(result.getObject(), instanceOf(StatementReference.class));

  }

  @Test
  void whenDeserializingSubStatementThenResultIsInstanceOfResult() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Result Is Instance Of Result
    assertThat(result.getResult(), instanceOf(Result.class));

  }

  @Test
  void whenDeserializingSubStatementThenContextIsInstanceOfContext() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Context Is Instance Of Context
    assertThat(result.getContext(), instanceOf(Context.class));

  }

  @Test
  void whenDeserializingSubStatementThenActorIsInstanceOfActor() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Actor Is Instance Of Actor
    assertThat(result.getActor(), instanceOf(Actor.class));

  }

  @Test
  void whenDeserializingSubStatementThenAttachmentsIsInstanceOfAttachment() throws IOException {

    final var file = ResourceUtils.getFile("classpath:sub_statement/sub_statement.json");

    // When Deserializing SubStatement
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Attachments Is Instance Of Attachment
    assertThat(result.getAttachments().get(0), instanceOf(Attachment.class));

  }

  @Test
  void whenDeserializingSubStatementWithObjectOfTypeSubStatementThenExceptionIsThrown()
      throws IOException {

    final var file = ResourceUtils
        .getFile("classpath:sub_statement/sub_statement_with_object_of_type_sub_statement.json");

    assertThrows(Exception.class, () -> {

      // When Deserializing SubStatement With Object Of Type SubStatement
      objectMapper.readValue(file, SubStatement.class);

      // Then Exception Is Thrown
    });

  }

  @Test
  void whenDeserializingSubStatementWithObjectOfTypeActivityThenObjectIsInstanceOfActivity()
      throws IOException {

    final var file = ResourceUtils
        .getFile("classpath:sub_statement/sub_statement_with_object_of_type_activity.json");

    // when Deserializing SubStatement With Object Of Type Activity
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Object Is Instance Of Activity
    assertThat(result.getObject(), instanceOf(Activity.class));

  }

  @Test
  void whenDeserializingSubStatementWithObjectOfTypeAgentThenObjectIsInstanceOfAgentThenObjectIsInstanceOfAgent()
      throws IOException {

    final var file = ResourceUtils
        .getFile("classpath:sub_statement/sub_statement_with_object_of_type_agent.json");

    // When Deserializing SubStatement With Object Of Type Agent
    final var result = objectMapper.readValue(file, SubStatement.class);

    // Then Object Is Instance Of Agent
    assertThat(result.getObject(), instanceOf(Agent.class));

  }

  @Test
  void whenDeserializingSubStatementWithObjectOfTypeGroupThenObjectIsInstanceOfGroup()
      throws IOException {

    final var file = ResourceUtils
        .getFile("classpath:sub_statement/sub_statement_with_object_of_type_group.json");

    // When Deserializing SubStatement With Object Of Type Group
    final var result = objectMapper.readValue(file, SubStatement.class);


    // Then Object Is Instance Of Group
    assertThat(result.getObject(), instanceOf(Group.class));

  }


  @Test
  void whenDeserializingSubStatementWithObjectOfTypeStatementReferenceThenObjectIsInstanceOfStatementReference()
      throws IOException {

    final var file = ResourceUtils.getFile(
        "classpath:sub_statement/sub_statement_with_object_of_type_statement_reference.json");

    // When Deserializing SubStatement With Object Of Type StatementReference
    final var result = objectMapper.readValue(file, SubStatement.class);


    // Then Object Is Instance Of StatementReference
    assertThat(result.getObject(), instanceOf(StatementReference.class));

  }



  @Test
  void whenSerializingSubStatementThenResultIsEqualToExpectedJson() throws IOException {

    final Agent agent = Agent.builder().mbox("mailto:agent@example.com").build();

    final Group group = Group.builder().name("Example Group").build();

    final var statementRef = StatementReference.builder()
        .id(UUID.fromString("9e13cefd-53d3-4eac-b5ed-2cf6693903bb")).build();

    final var score = Score.builder()

        .max(5.0f)

        .min(0.0f)

        .raw(1.0f)

        .scaled(1.0f)

        .build();

    final var resultInstance = Result.builder()

        .score(score)

        .completion(true)

        .success(true)

        .duration("P1D")

        .response("test")

        .build();

    final var activity =
        Activity.builder().id(URI.create("http://www.example.co.uk/exampleactivity")).build();

    final var contextActivities = ContextActivities.builder()

        .parent(Collections.singletonList(activity))

        .grouping(Collections.singletonList(activity))

        .category(Collections.singletonList(activity))

        .other(Collections.singletonList(activity)).build();

    final var extensions = new LinkedHashMap<URI, Object>();
    extensions.put(URI.create("http://url"), "www.example.com");

    final var context = Context.builder()

        .registration(UUID.fromString("6d969975-8d7e-4506-ac19-877c57f2921a"))

        .revision("revision")

        .platform("platform")

        .language(Locale.US)

        .instructor(agent)

        .team(group)

        .statement(statementRef)

        .contextActivities(contextActivities)

        .extensions(extensions)

        .build();

    final var attachment = Attachment.builder().usageType(URI.create("http://example.com"))
        .fileUrl(URI.create("http://example.com"))

        .addDisplay(Locale.US, "value")

        .addDescription(Locale.US, "value")

        .length(123)

        .sha2("123")

        .contentType("file")

        .build();

    final var subStatement = SubStatement.builder()

        .timestamp(Instant.parse("2015-11-18T11:17:00Z"))

        .agentActor(a -> a.mbox("mailto:agent@example.com"))

        .verb(v -> v.id(URI.create("http://example.com/confirmed")).addDisplay(Locale.US,
            "confirmed"))

        .object(statementRef)

        .result(resultInstance)

        .context(context)

        .addAttachment(attachment)

        .addAttachment(a -> a.usageType(URI.create("http://example.com"))

            .fileUrl(URI.create("http://example.com"))

            .addDisplay(Locale.ENGLISH, "value")

            .addDescription(Locale.ENGLISH, "value")

            .length(123)

            .sha2("123")

            .contentType("file"))

        .build();

    // When Serializing SubStatement
    final var result = objectMapper.readTree(objectMapper.writeValueAsString(subStatement));

    // Then Result Is Equal To Expected Json
    assertThat(result,
        is(objectMapper.readTree(objectMapper.writeValueAsString(objectMapper.readValue(

            ResourceUtils.getFile("classpath:sub_statement/sub_statement.json"),
            SubStatement.class)))));

  }

  @Test
  void whenCallingToStringThenResultIsExpected() throws IOException {

    final var subStatement = objectMapper.readValue(
        ResourceUtils.getFile("classpath:sub_statement/sub_statement.json"), SubStatement.class);

    // When Calling ToString
    final var result = subStatement.toString();

    // Then Result Is Expected
    assertThat(result, is(
        "SubStatement(objectType=SubStatement, actor=Agent(super=Actor(name=null, mbox=mailto:agent@example.com, mboxSha1sum=null, openid=null, account=null), objectType=null), verb=Verb(id=http://example.com/confirmed, display={en_US=confirmed}), object=StatementReference(objectType=StatementRef, id=9e13cefd-53d3-4eac-b5ed-2cf6693903bb), result=Result(score=Score(scaled=1.0, raw=1.0, min=0.0, max=5.0), success=true, completion=true, response=test, duration=P1D, extensions=null), context=Context(registration=6d969975-8d7e-4506-ac19-877c57f2921a, instructor=Agent(super=Actor(name=null, mbox=mailto:agent@example.com, mboxSha1sum=null, openid=null, account=null), objectType=null), team=Group(super=Actor(name=Example Group, mbox=null, mboxSha1sum=null, openid=null, account=null), objectType=Group, member=null), contextActivities=ContextActivities(parent=[Activity(objectType=null, id=http://www.example.co.uk/exampleactivity, definition=null)], grouping=[Activity(objectType=null, id=http://www.example.co.uk/exampleactivity, definition=null)], category=[Activity(objectType=null, id=http://www.example.co.uk/exampleactivity, definition=null)], other=[Activity(objectType=null, id=http://www.example.co.uk/exampleactivity, definition=null)]), revision=revision, platform=platform, language=en_US, statement=StatementReference(objectType=StatementRef, id=9e13cefd-53d3-4eac-b5ed-2cf6693903bb), extensions={http://url=www.example.com}), timestamp=2015-11-18T11:17:00Z, attachments=[Attachment(usageType=http://example.com, display={en_US=value}, description={en_US=value}, contentType=file, length=123, sha2=123, fileUrl=http://example.com, content=null), Attachment(usageType=http://example.com, display={en=value}, description={en=value}, contentType=file, length=123, sha2=123, fileUrl=http://example.com, content=null)])"));

  }

  @Test
  void whenBuildingSubStatementWithAgentObjectWithoutObjectTypeThenSubStatementObjectObjectTypeIsNotNull() {

    final Agent agent = Agent.builder().name("A N Other").objectType(null).build();

    // When Building SubStatement With Agent Object Without ObjectType
    final var subStatement = SubStatement.builder()

        .agentActor(a -> a.name("A N Other"))

        .verb(Verb.PASSED)

        .object(agent)

        .build();

    // Then SubStatement Object ObjectType Is Not Null
    assertThat(((Agent) subStatement.getObject()).getObjectType(), is(notNullValue()));

  }

  @Test
  void whenBuildingSubStatementWithAgentObjectWithObjectTypeThenSubStatementObjectObjectTypeIsNotNull() {

    final Agent agent = Agent.builder().name("A N Other").objectType(AgentObjectType.AGENT).build();

    // When Building SubStatement With Agent Object With ObjectType
    final var subStatement = SubStatement.builder()

        .agentActor(a -> a.name("A N Other"))

        .verb(Verb.PASSED)

        .object(agent)

        .build();

    // Then SubStatement Object ObjectType Is Not Null
    assertThat(((Agent) subStatement.getObject()).getObjectType(), is(notNullValue()));

  }


}
