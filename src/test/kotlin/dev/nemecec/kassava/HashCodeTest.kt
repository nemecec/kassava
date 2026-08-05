package dev.nemecec.kassava

import dev.nemecec.kassava.model.Animal
import dev.nemecec.kassava.model.Company
import dev.nemecec.kassava.model.Employee
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for the [kotlinHashCode] extension method.
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class HashCodeTest {

    @Nested
    @DisplayName("a person")
    inner class APerson {

        private val person = Employee(name = "Jim", age = 31)

        @Test
        fun `has the same hash as the same person object`() {
            assertEquals(person.hashCode(), person.hashCode())
        }

        @Test
        fun `has the same hash as a person with the same name and age`() {
            assertEquals(person.hashCode(), Employee(name = "Jim", age = 31).hashCode())
        }

        @Test
        fun `has a different hash to a person with a different name`() {
            assertNotEquals(person.hashCode(), Employee(name = "Jill", age = 31).hashCode())
        }

        @Test
        fun `has a different hash to a person with a different age`() {
            assertNotEquals(person.hashCode(), Employee(name = "Jim", age = 42).hashCode())
        }

        @Test
        fun `has a different hash to a person with a null age`() {
            assertNotEquals(person.hashCode(), Employee(name = "Jim").hashCode())
        }
    }

    @Nested
    @DisplayName("a company with employees (array scenario)")
    inner class ACompanyWithEmployees {

        private val company =
            Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))

        @Test
        fun `has the same hash as a company with the same name and the same array of employees`() {
            val otherCompany = Company(name = "ACME", employees = company.employees)

            assertEquals(company.hashCode(), otherCompany.hashCode())
        }

        @Test
        fun `has the same hash as a company with the same name and a new array of the same employees (deep equals)`() {
            val otherCompany = Company(name = "ACME", employees = company.employees.copyOf())

            assertEquals(company.hashCode(), otherCompany.hashCode())
        }

        @Test
        fun `has the same hash as a company with the same name and a new array of similar employees (deep equals)`() {
            val otherCompany =
                Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))

            assertEquals(company.hashCode(), otherCompany.hashCode())
        }

        @Test
        fun `has a different hash to a company with the same name and slightly different employees`() {
            val otherCompany =
                Company(name = "ACME", employees = arrayOf(Employee(name = "James"), Employee(name = "Alice")))

            assertNotEquals(company.hashCode(), otherCompany.hashCode())
        }
    }

    @Nested
    @DisplayName("a cat (polymorphic scenario)")
    inner class ACat {

        private val cat = Animal.Cat(name = "Felix", mice = 2)

        @Test
        fun `has the same hash as an identical cat`() {
            assertEquals(cat.hashCode(), Animal.Cat(name = "Felix", mice = 2).hashCode())
        }

        @Test
        fun `has a different hash to a cat with a different name`() {
            assertNotEquals(cat.hashCode(), Animal.Cat(name = "Felixio", mice = 2).hashCode())
        }

        @Test
        fun `has a different hash to a cat with a different number of mice`() {
            assertNotEquals(cat.hashCode(), Animal.Cat(name = "Felix", mice = 3).hashCode())
        }

        @Test
        fun `has a different hash to a dog with the same name`() {
            assertNotEquals(cat.hashCode(), Animal.Dog(name = "Felix", bones = 2).hashCode())
        }
    }
}
