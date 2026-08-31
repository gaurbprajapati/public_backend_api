package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryBuilder Tests")
class QueryBuilderTests {

	private static final Integer CURRENT_USER_ID = 100;

	@Mock
	private AccessLevelHandler accessLevelHandler;

	@Mock
	private EntityManager entityManager;

	@Mock
	private AuthHolder authHolder;

	@Mock
	private CriteriaBuilder criteriaBuilder;

	@Mock
	private CriteriaQuery<Object> criteriaQuery;

	@Mock
	private Root<Object> root;

	@Mock
	private Path<Object> ownerIdPath;

	@Mock
	private Predicate ownedOnlyPredicate;

	@Mock
	private Predicate teamOnlyPredicate;

	@Mock
	private Predicate disjunctionPredicate;

	@Mock
	private TypedQuery<Integer> teamQuery;

	private QueryBuilder queryBuilder;

	private AccessControlDto accessControlDto;

	@BeforeEach
	void setUp() {
		this.queryBuilder = new QueryBuilder(this.accessLevelHandler, this.entityManager, this.authHolder);
		this.accessControlDto = new AccessControlDto();
	}

	@Test
	@DisplayName("Build query should add owned-only predicate")
	void testBuildQueryOwnedOnlyAddsOwnerPredicate() {
		this.mockCriteriaSetupForSuccessfulBuildQuery();
		given(this.root.get("ownerId")).willReturn(this.ownerIdPath);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.OWNED_ONLY);
		given(this.criteriaBuilder.equal(this.ownerIdPath, CURRENT_USER_ID)).willReturn(this.ownedOnlyPredicate);

		CriteriaQuery<Object> result = this.queryBuilder.buildQuery(Object.class, Entity.CANDIDATES,
				this.accessControlDto, Permission.CAN_VIEW);

		ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
		then(this.criteriaQuery).should().where(predicateCaptor.capture());
		assertThat(predicateCaptor.getValue()).hasSize(1);
		assertThat(predicateCaptor.getValue()[0]).isEqualTo(this.ownedOnlyPredicate);
		assertThat(result).isEqualTo(this.criteriaQuery);
	}

	@Test
	@DisplayName("Build query should add team-only predicate from team members")
	void testBuildQueryTeamOnlyAddsTeamPredicate() {
		this.mockCriteriaSetupForSuccessfulBuildQuery();
		given(this.root.get("ownerId")).willReturn(this.ownerIdPath);
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.TEAM_ONLY);
		given(this.entityManager.createQuery(anyString(), eq(Integer.class))).willReturn(this.teamQuery);
		given(this.teamQuery.setParameter("userId", CURRENT_USER_ID)).willReturn(this.teamQuery);
		given(this.teamQuery.getResultList()).willReturn(List.of(10, 20));
		given(this.ownerIdPath.in(List.of(10, 20))).willReturn(this.teamOnlyPredicate);

		CriteriaQuery<Object> result = this.queryBuilder.buildQuery(Object.class, Entity.CANDIDATES,
				this.accessControlDto, Permission.CAN_VIEW);

		ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
		then(this.criteriaQuery).should().where(predicateCaptor.capture());
		assertThat(predicateCaptor.getValue()).hasSize(1);
		assertThat(predicateCaptor.getValue()[0]).isEqualTo(this.teamOnlyPredicate);
		assertThat(result).isEqualTo(this.criteriaQuery);
	}

	@Test
	@DisplayName("Build query should not add predicate for yes level")
	void testBuildQueryYesAddsNoPredicate() {
		this.mockCriteriaSetupForSuccessfulBuildQuery();
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.YES);

		this.queryBuilder.buildQuery(Object.class, Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW);

		ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
		then(this.criteriaQuery).should().where(predicateCaptor.capture());
		assertThat(predicateCaptor.getValue()).isEmpty();
	}

	@Test
	@DisplayName("Build query should not add predicate for everything level")
	void testBuildQueryEverythingAddsNoPredicate() {
		this.mockCriteriaSetupForSuccessfulBuildQuery();
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.EVERYTHING);

		this.queryBuilder.buildQuery(Object.class, Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW);

		ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
		then(this.criteriaQuery).should().where(predicateCaptor.capture());
		assertThat(predicateCaptor.getValue()).isEmpty();
	}

	@Test
	@DisplayName("Build query should add disjunction for no level")
	void testBuildQueryNoAddsDisjunctionPredicate() {
		this.mockCriteriaSetupForSuccessfulBuildQuery();
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.NO);
		given(this.criteriaBuilder.disjunction()).willReturn(this.disjunctionPredicate);

		this.queryBuilder.buildQuery(Object.class, Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW);

		ArgumentCaptor<Predicate[]> predicateCaptor = ArgumentCaptor.forClass(Predicate[].class);
		then(this.criteriaQuery).should().where(predicateCaptor.capture());
		assertThat(predicateCaptor.getValue()).hasSize(1);
		assertThat(predicateCaptor.getValue()[0]).isEqualTo(this.disjunctionPredicate);
	}

	@Test
	@DisplayName("Build query should throw for unsupported permission level")
	void testBuildQueryUnsupportedLevelThrowsException() {
		this.mockCriteriaSetupWithoutWhereClause();
		given(this.accessLevelHandler.getAccessLevel(Entity.CANDIDATES, this.accessControlDto, Permission.CAN_VIEW,
				null))
			.willReturn(PermissionLevel.NOTHING);

		assertThatThrownBy(() -> this.queryBuilder.buildQuery(Object.class, Entity.CANDIDATES, this.accessControlDto,
				Permission.CAN_VIEW))
			.isInstanceOf(UnknownAccessLevelException.class)
			.hasMessageContaining("Unsupported permission level");
	}

	@Test
	@DisplayName("Get access level should delegate to access level handler")
	void testGetAccessLevelDelegatesToHandler() {
		given(this.accessLevelHandler.getAccessLevel(Entity.JOBS, this.accessControlDto, Permission.CAN_EDIT, null))
			.willReturn(PermissionLevel.TEAM_ONLY);

		PermissionLevel result = this.queryBuilder.getAccessLevel(Entity.JOBS, this.accessControlDto,
				Permission.CAN_EDIT);

		assertThat(result).isEqualTo(PermissionLevel.TEAM_ONLY);
		then(this.accessLevelHandler).should()
			.getAccessLevel(Entity.JOBS, this.accessControlDto, Permission.CAN_EDIT, null);
	}

	private void mockCriteriaSetupForSuccessfulBuildQuery() {
		this.mockCriteriaSetupWithoutWhereClause();
		given(this.criteriaQuery.where(any(Predicate[].class))).willReturn(this.criteriaQuery);
	}

	private void mockCriteriaSetupWithoutWhereClause() {
		given(this.authHolder.getAuthenticationPrincipalUniqueIdentifier()).willReturn(CURRENT_USER_ID);
		given(this.entityManager.getCriteriaBuilder()).willReturn(this.criteriaBuilder);
		given(this.criteriaBuilder.createQuery(Object.class)).willReturn(this.criteriaQuery);
		given(this.criteriaQuery.from(Object.class)).willReturn(this.root);
	}

}
