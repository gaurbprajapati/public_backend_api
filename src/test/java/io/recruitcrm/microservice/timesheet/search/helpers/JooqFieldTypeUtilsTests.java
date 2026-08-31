package io.recruitcrm.microservice.timesheet.search.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.jooq.Field;
import org.jooq.impl.DSL;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("JooqFieldTypeUtils Tests")
class JooqFieldTypeUtilsTests {

	@Test
	@DisplayName("cast should return same field when types are assignable")
	void testCastWithAssignableTypes() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		Field<Integer> result = JooqFieldTypeUtils.cast(integerField, Integer.class);

		assertThat(result).isNotNull().isEqualTo(integerField);
	}

	@Test
	@DisplayName("cast should throw exception when types are not assignable")
	void testCastWithNonAssignableTypes() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		assertThatThrownBy(() -> JooqFieldTypeUtils.cast(integerField, String.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Field type mismatch");
	}

	@Test
	@DisplayName("cast should work with parent-child type relationships")
	void testCastWithParentChildTypes() {
		// jOOQ DEFAULT dialect rejects abstract/collection types in DSL.field; stub
		// Field#getType only.
		Field<?> arrayListField = mock(Field.class);
		given(arrayListField.getType()).willAnswer((invocation) -> ArrayList.class);

		Field<?> result = JooqFieldTypeUtils.cast(arrayListField, List.class);

		assertThat(result).isNotNull().isEqualTo(arrayListField);
	}

	@Test
	@DisplayName("safeExact should return same field when types match exactly")
	void testSafeExactWithMatchingTypes() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		Field<Integer> result = JooqFieldTypeUtils.safeExact(integerField, Integer.class);

		assertThat(result).isNotNull().isEqualTo(integerField);
	}

	@Test
	@DisplayName("safeExact should throw exception when types do not match exactly")
	void testSafeExactWithNonMatchingTypes() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		assertThatThrownBy(() -> JooqFieldTypeUtils.safeExact(integerField, String.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Expected");
	}

	@Test
	@DisplayName("safeExact should throw exception even for assignable types")
	void testSafeExactWithAssignableButNotExactTypes() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		// Integer is assignable to Number, but safeExact requires exact match
		assertThatThrownBy(() -> JooqFieldTypeUtils.safeExact(integerField, Number.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Expected");
	}

	@Test
	@DisplayName("coerce should create new field with specified type")
	void testCoerceCreatesNewField() {
		Field<?> originalField = DSL.field("test_field", Integer.class);

		Field<String> coercedField = JooqFieldTypeUtils.coerce(originalField, String.class);

		assertThat(coercedField).isNotNull();
		assertThat(coercedField.getName()).isEqualTo(originalField.getName());
		// The type should be coerced
		assertThat(coercedField.getType()).isEqualTo(String.class);
	}

	@Test
	@DisplayName("coerce should preserve field name")
	void testCoercePreservesFieldName() {
		String fieldName = "my_custom_field";
		Field<?> originalField = DSL.field(fieldName, Integer.class);

		Field<String> coercedField = JooqFieldTypeUtils.coerce(originalField, String.class);

		assertThat(coercedField.getName()).isEqualTo(fieldName);
	}

	@Test
	@DisplayName("coerce should work with different target types")
	void testCoerceWithDifferentTargetTypes() {
		Field<?> originalField = DSL.field("test_field", Integer.class);

		Field<String> stringField = JooqFieldTypeUtils.coerce(originalField, String.class);
		Field<Long> longField = JooqFieldTypeUtils.coerce(originalField, Long.class);
		Field<Double> doubleField = JooqFieldTypeUtils.coerce(originalField, Double.class);

		assertThat(stringField).isNotNull();
		assertThat(stringField.getType()).isEqualTo(String.class);
		assertThat(longField).isNotNull();
		assertThat(longField.getType()).isEqualTo(Long.class);
		assertThat(doubleField).isNotNull();
		assertThat(doubleField.getType()).isEqualTo(Double.class);
	}

	@Test
	@DisplayName("cast should handle null field gracefully")
	void testCastWithNullField() {
		assertThatThrownBy(() -> JooqFieldTypeUtils.cast(null, Integer.class)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("safeExact should handle null field gracefully")
	void testSafeExactWithNullField() {
		assertThatThrownBy(() -> JooqFieldTypeUtils.safeExact(null, Integer.class))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("coerce should handle null field gracefully")
	void testCoerceWithNullField() {
		assertThatThrownBy(() -> JooqFieldTypeUtils.coerce(null, Integer.class))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("cast should work with String type")
	void testCastWithStringType() {
		Field<String> stringField = DSL.field("test_field", String.class);

		Field<String> result = JooqFieldTypeUtils.cast(stringField, String.class);

		assertThat(result).isNotNull().isEqualTo(stringField);
	}

	@Test
	@DisplayName("cast should work with Long type")
	void testCastWithLongType() {
		Field<Long> longField = DSL.field("test_field", Long.class);

		Field<Long> result = JooqFieldTypeUtils.cast(longField, Long.class);

		assertThat(result).isNotNull().isEqualTo(longField);
	}

	@Test
	@DisplayName("safeExact should work with String type")
	void testSafeExactWithStringType() {
		Field<String> stringField = DSL.field("test_field", String.class);

		Field<String> result = JooqFieldTypeUtils.safeExact(stringField, String.class);

		assertThat(result).isNotNull().isEqualTo(stringField);
	}

	@Test
	@DisplayName("safeExact should work with Long type")
	void testSafeExactWithLongType() {
		Field<Long> longField = DSL.field("test_field", Long.class);

		Field<Long> result = JooqFieldTypeUtils.safeExact(longField, Long.class);

		assertThat(result).isNotNull().isEqualTo(longField);
	}

	@Test
	@DisplayName("coerce should work with complex field names")
	void testCoerceWithComplexFieldNames() {
		String[] complexNames = { "table.field", "schema.table.field", "`quoted field`", "field_name_123" };

		for (String name : complexNames) {
			Field<?> originalField = DSL.field(name, Integer.class);
			Field<String> coercedField = JooqFieldTypeUtils.coerce(originalField, String.class);

			assertThat(coercedField).isNotNull();
			assertThat(coercedField.getName()).isEqualTo(name);
		}
	}

	@Test
	@DisplayName("cast error message should include expected and actual types")
	void testCastErrorMessage() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		assertThatThrownBy(() -> JooqFieldTypeUtils.cast(integerField, String.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("expected")
			.hasMessageContaining("String")
			.hasMessageContaining("Integer");
	}

	@Test
	@DisplayName("safeExact error message should include expected and actual types")
	void testSafeExactErrorMessage() {
		Field<Integer> integerField = DSL.field("test_field", Integer.class);

		assertThatThrownBy(() -> JooqFieldTypeUtils.safeExact(integerField, String.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Expected")
			.hasMessageContaining("String")
			.hasMessageContaining("Integer");
	}

}
