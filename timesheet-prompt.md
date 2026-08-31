---
description: |
  Configuration file for generating Spring Boot REST APIs with:
  - Automated endpoint creation
  - Business logic integration
  - Comprehensive test coverage
  - Consistent architectural patterns
globs:
  - src/main/java/io/recruitcrm/microservice/**/**/**/.java
  - src/test/java/io/recruitcrm/microservice/**/**/**/.java
alwaysApply: false
---

# CURSOR AI API GENERATION RULES

## CORE CONFIGURATION
framework: Spring Boot 3.2 (Java 21)
architecture: MVC
codeStyle:
naming:
controllers: [Entity]Controller
services: [Entity]Service
repositories: [Entity]Repository
jpaRepositories: [Entity]JpaRepository
packages:
entities: io.recruitcrm.microservice.[module].domain
dtos: io.recruitcrm.microservice.[module].model
mappers: io.recruitcrm.microservice.[module].mapper

## ENDPOINT TEMPLATE
endpoints:
- method: GET
  path: /v1/timesheet-settings/assignment/{assignmentId}
  implementation:
  controller: TimesheetSettingController
  service: TimesheetSettingService
  repository: TimesheetSettingRepository
  request: PathVariable only
  response: JSON (all fields)
  businessLogic: |
    1. Validate assignmentId exists in AssignCandidateJob
    2. Fetch jobId/candidateId
    3. Query timesheet settings by composite key
    4. Throw ResourceNotFoundException if missing
       exceptions:
    - ResourceNotFoundException
    - InvalidInputException

## CODE STRUCTURE RULES
structure:
- interfaceFirst: true
- folderOrganization: By entity name
- mandatoryComponents:
    - Controller (REST)
    - Service (Interface + Impl)
    - Repository (Interface)
    - JPA Repository (if needed)
    - DTOs (Request/Response)
    - Mappers
- constructorInjection: Required for all classes

## TESTING REQUIREMENTS
testing:
coverage: 100%
frameworks:
- JUnit 5
- Mockito
patterns:
- Arrange-Act-Assert
- Given-When-Then
testTypes:
unit: Required
integration: Recommended
namingConventions:
testClasses: [Component]Tests
testMethods: test[Scenario]_[Condition]

## CONTROLLER TEST TEMPLATES
templates:
getEndpoint: |
@Test
@DisplayName("GET /endpoint - Success")
void testGetEntity_validInput_returns200() {
// Arrange
when(service.method(any())).thenReturn(mockResponse);

        // Act
        var response = controller.method(validInput);

        // Assert
        verify(service).method(validInput);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

errorHandling: |
@Test
@DisplayName("GET /endpoint - Not Found")
void testGetEntity_invalidInput_throws404() {
// Arrange
when(service.method(invalidInput))
.thenThrow(new ResourceNotFoundException());

        // Act/Assert
        assertThrows(ResourceNotFoundException.class,
            () -> controller.method(invalidInput));
    }

## REPOSITORY TESTING STANDARDS
repositoryTests:
mockDataRequirements:
- Separate MockData class per entity
- Realistic test values
- Variants for edge cases
verificationMethods:
- ArgumentCaptor for entity state
- Query parameter verification
- Exception scenario testing

## QUALITY GATES
validation:
- Static code analysis (SonarQube)
- Test coverage enforcement
- Architectural compliance checks
- Style consistency validation

# IMPLEMENTATION NOTES
1. Always generate interfaces before implementations
2. Constructor injection is mandatory
3. Include OpenAPI annotations
4. Generate integration tests for happy paths
5. Add validation annotations to DTOs
6. Include logging at service layer