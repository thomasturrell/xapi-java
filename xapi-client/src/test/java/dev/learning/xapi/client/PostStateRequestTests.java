/*
 * Copyright 2016-2023 Berry Cloud Ltd. All rights reserved.
 */
package dev.learning.xapi.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import dev.learning.xapi.client.PostStateRequest.Builder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * PostStateRequest Tests.
 *
 * @author Thomas Turrell-Croft
 */
@DisplayName("PostStateRequest Tests")
class PostStateRequestTests {

  @Test
  void whenBuildingPostStateRequestWithAllParametersThenNoExceptionIsThrown() {

    // When Building PostStateRequest With All Parameters
    final Builder<?, ?> builder = PostStateRequest.builder()

        .activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!");

    // Then No Exception Is Thrown
    assertDoesNotThrow(() -> builder.build());

  }

  @Test
  void givenPostStateRequestWithAllParametersWhenGettingURLThenResultIsExpected() {

    // Given PostStateRequest With All Parameters
    final PostStateRequest request = PostStateRequest.builder()

        .activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!")

        .build();

    final Map<String, Object> queryParams = new HashMap<>();

    // When Getting URL
    final var result =
        request.url(UriComponentsBuilder.fromUriString("https://example.com/xapi/"), queryParams)
            .build(queryParams);

    // Then Result Is Expected
    assertThat(result, is(URI.create(
        "https://example.com/xapi/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark")));

  }

  @Test
  void givenPostStateRequestWithoutRegistrationWhenGettingURLThenResultIsExpected() {

    // Given PostStateRequest Without Registration
    final PostStateRequest request = PostStateRequest.builder()

        .activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!")

        .build();

    final Map<String, Object> queryParams = new HashMap<>();

    // When Getting URL
    final var result =
        request.url(UriComponentsBuilder.fromUriString("https://example.com/xapi/"), queryParams)
            .build(queryParams);

    // Then Result Is Expected
    assertThat(result, is(URI.create(
        "https://example.com/xapi/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark")));

  }

}
