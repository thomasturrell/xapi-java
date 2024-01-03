/*
 * Copyright 2016-2023 Berry Cloud Ltd. All rights reserved.
 */

package dev.learning.xapi.model.validation.constraints;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dev.learning.xapi.model.validation.internal.validators.StatementRevisionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated element must have a valid revision.
 *
 * @author István Rátkai (Selindek)
 */
@Documented
@Constraint(validatedBy = {StatementRevisionValidator.class})
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidStatementRevision {

  /**
   * Error Message.
   */
  String message() default "invalid Statement Revision (Object must be an Activity)";

  /**
   * Groups.
   */
  Class<?>[] groups() default {};

  /**
   * Payload.
   */
  Class<? extends Payload>[] payload() default {};

}
