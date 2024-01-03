/*
 * Copyright 2016-2023 Berry Cloud Ltd. All rights reserved.
 */

package dev.learning.xapi.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.learning.xapi.model.validation.constraints.HasScheme;
import dev.learning.xapi.model.validation.constraints.ValidActivityDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * This class represents the xAPI Activity object.
 *
 * @author Thomas Turrell-Croft
 *
 * @see <a href=
 *      "https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2441-when-the-objecttype-is-activity">xAPI
 *      Activity</a>
 */
@Value
@Builder
@AllArgsConstructor
@EqualsAndHashCode(exclude = "definition")
public class Activity implements StatementObject, SubStatementObject {

  private ActivityObjectType objectType;

  /**
   * An identifier for a single unique Activity.
   */
  @NotNull
  @HasScheme
  private URI id;

  /**
   * Metadata.
   */
  @Valid
  @ValidActivityDefinition
  @JsonMerge
  private ActivityDefinition definition;

  // **Warning** do not add fields that are not required by the xAPI specification.

  /**
   * Constructor for Activity.
   *
   * @param id The identifier of the Activity.
   */
  public Activity(String id) {
    this.objectType = null;
    this.id = URI.create(id);
    this.definition = null;
  }

  /**
   * Builder for Activity.
   */
  public static class Builder {

    // This static class extends the lombok builder.

    /**
     * Sets the identifier.
     *
     * @param id The identifier of the Activity.
     *
     * @return This builder
     *
     * @see Activity#id
     */
    public Builder id(URI id) {

      this.id = id;

      return this;
    }

    /**
     * Sets the identifier.
     *
     * @param id The identifier of the Activity.
     *
     * @return This builder
     *
     * @see Activity#id
     */
    public Builder id(String id) {

      this.id = URI.create(id);

      return this;
    }

    /**
     * Consumer Builder for definition.
     *
     * @param definition The Consumer Builder for definition
     *
     * @return This builder
     *
     * @see Activity#definition
     */
    public Builder definition(Consumer<ActivityDefinition.Builder> definition) {

      final var builder = ActivityDefinition.builder();

      definition.accept(builder);

      return definition(builder.build());
    }

    /**
     * Sets the definition.
     *
     * @param definition The definition of the Activity
     *
     * @return This builder
     *
     * @see Activity#definition
     */
    public Builder definition(ActivityDefinition definition) {

      this.definition = definition;

      return this;
    }

  }

  /**
   * This enumeration class represents the optional xAPI Activity object type.
   *
   * @author István Rátkai (Selindek)
   */
  public enum ActivityObjectType {

    /**
     * Activity object type.
     */
    @JsonProperty("Activity")
    ACTIVITY;

  }

}
