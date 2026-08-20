package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExceptionTest {

  @Test
  void badRequestException() {
    var ex = new BadRequestException("Bad request");
    assertEquals(400, ex.getStatus().value());
    assertEquals("Bad request", ex.getMessage());
  }

  @Test
  void resourceNotFoundException() {
    var ex = ResourceNotFoundException.of("User", UUID.randomUUID());
    assertEquals(404, ex.getStatus().value());
    assertTrue(ex.getMessage().contains("User"));
    assertTrue(ex.getMessage().contains("Not found"));
  }

  @Test
  void conflictException() {
    var ex = new ConflictException("Conflict");
    assertEquals(409, ex.getStatus().value());
  }

  @Test
  void unauthorizedException() {
    var ex = new UnauthorizedException("Unauthorized");
    assertEquals(401, ex.getStatus().value());
  }

  @Test
  void forbiddenException() {
    var ex = new ForbiddenException("Forbidden");
    assertEquals(403, ex.getStatus().value());
  }

  @Test
  void notFoundException() {
    var ex = NotFoundException.of("Resource", UUID.randomUUID());
    assertEquals(404, ex.getStatus().value());
  }
}
