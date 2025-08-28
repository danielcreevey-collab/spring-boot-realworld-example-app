package io.spring.graphql.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters parameters = mock(DataFetcherExceptionHandlerParameters.class);
    when(parameters.getException()).thenReturn(exception);
    when(parameters.getPath()).thenReturn(mock(ResultPath.class));

    DataFetcherExceptionHandlerResult result = handler.onException(parameters);

    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error).isInstanceOf(TypedGraphQLError.class);
    assertThat(error.getMessage()).isEqualTo("invalid email or password");
    
    Map<String, Object> extensions = error.getExtensions();
    if (extensions != null && extensions.containsKey("errorType")) {
      assertThat(extensions.get("errorType")).isEqualTo("UNAUTHENTICATED");
    }
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    ConstraintViolationException exception = new ConstraintViolationException("Validation failed", Set.of());

    DataFetcherExceptionHandlerParameters parameters = mock(DataFetcherExceptionHandlerParameters.class);
    when(parameters.getException()).thenReturn(exception);
    when(parameters.getPath()).thenReturn(mock(ResultPath.class));

    DataFetcherExceptionHandlerResult result = handler.onException(parameters);

    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error).isInstanceOf(TypedGraphQLError.class);
    assertThat(error.getMessage()).isEqualTo("Validation failed");
    
    TypedGraphQLError typedError = (TypedGraphQLError) error;
    assertThat(typedError.getErrorType()).isNotEqualTo(ErrorType.UNAUTHENTICATED);
  }
}
