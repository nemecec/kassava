package dev.nemecec.kassava

import dev.nemecec.kassava.model.ColouredPoint
import dev.nemecec.kassava.model.Company
import dev.nemecec.kassava.model.Employee
import dev.nemecec.kassava.model.Point
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests for the [kotlinEquals] extension method.
 *
 * The multi-type equality example (Point, ColouredPoint, etc) is taken from the excellent Artima
 * article at https://www.artima.com/articles/how-to-write-an-equality-method-in-java
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class EqualsTest {

  @Nested
  @DisplayName("a person")
  inner class APerson {

    private val person = Employee(name = "Jim", age = 31)

    @Test
    fun `is equal to the same person object`() {
      assertEquals(person, person)
    }

    @Test
    fun `is not equal to null`() {
      assertFalse(person.equals(null))
    }

    @Test
    fun `is not equal to another type`() {
      assertFalse(person.equals("person"))
    }

    @Test
    fun `is equal to a person with the same name and age`() {
      assertEquals(person, Employee(name = "Jim", age = 31))
    }

    @Test
    fun `is not equal to a person with a different name`() {
      assertNotEquals(person, Employee(name = "Jill", age = 31))
    }

    @Test
    fun `is not equal to a person with a different age`() {
      assertNotEquals(person, Employee(name = "Jim", age = 42))
    }

    @Test
    fun `is not equal to a person with a null age`() {
      assertNotEquals(person, Employee(name = "Jim"))
    }
  }

  @Nested
  @DisplayName("a point, coloured point and anonymous point")
  inner class Points {

    private val point = Point(x = 1, y = 2)
    private val colouredPoint = ColouredPoint(1, 2, "INDIGO")
    private val anonymousPoint =
      object : Point(x = 1, y = 1) {
        override val y = 2
      }

    @Test
    fun `a hash set holding the point contains the point and the anonymous point`() {
      val set = hashSetOf<Point>(point)

      assertTrue(point in set)
      assertFalse(colouredPoint in set)
      assertTrue(anonymousPoint in set)
    }

    @Test
    fun `the point and the coloured point are not equal`() {
      assertNotEquals<Point>(point, colouredPoint)
      assertNotEquals<Point>(colouredPoint, point)
    }

    @Test
    fun `the point and the anonymous point are equal - the anonymous point doesn't override canEqual()`() {
      assertEquals<Point>(point, anonymousPoint)
      assertEquals<Point>(anonymousPoint, point)
    }

    @Test
    fun `the coloured point and the anonymous point are not equal`() {
      assertNotEquals<Point>(colouredPoint, anonymousPoint)
      assertNotEquals<Point>(anonymousPoint, colouredPoint)
    }
  }

  @Nested
  @DisplayName("a company with employees")
  inner class ACompanyWithEmployees {

    private val company =
      Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))

    @Test
    fun `is equal to a company with the same name and the same array of employees`() {
      assertEquals(company, Company(name = "ACME", employees = company.employees))
    }

    @Test
    fun `is equal to a company with the same name and a new array of the same employees (deep equals)`() {
      assertEquals(company, Company(name = "ACME", employees = company.employees.copyOf()))
    }

    @Test
    fun `is equal to a company with the same name and a new array of similar employees (deep equals)`() {
      val otherCompany =
        Company(
          name = "ACME",
          employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")),
        )

      assertEquals(company, otherCompany)
    }

    @Test
    fun `is not equal to a company with the same name and slightly different employees`() {
      val otherCompany =
        Company(
          name = "ACME",
          employees = arrayOf(Employee(name = "James"), Employee(name = "Alice")),
        )

      assertNotEquals(company, otherCompany)
    }
  }
}
