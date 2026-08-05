package au.com.console.kassava

import au.com.console.kassava.model.Animal
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests for the [kotlinToString] extension method.
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class ToStringTest {

    @Test
    fun `a fully populated person has the correct string representation`() {
        val person = Person(
            name = "Jim",
            age = 31,
            address = Person.Address(
                streetNumber = 123,
                streetName = "Sesame",
                country = "US"
            )
        )

        assertEquals(
            "Person(name=Jim, age=31, address=Address(streetNumber=123, streetName=Sesame, country=US))",
            person.toString()
        )
    }

    @Test
    fun `a person with only mandatory properties has the correct string representation`() {
        val person = Person(
            name = "Jim",
            address = Person.Address(country = "US")
        )

        assertEquals(
            "Person(name=Jim, age=null, address=Address(streetNumber=null, streetName=null, country=US))",
            person.toString()
        )
    }

    @Test
    fun `a person with omitNulls enabled and only mandatory properties has the correct string representation`() {
        val person = PersonOmitNulls(
            name = "Jim",
            address = PersonOmitNulls.Address(country = "US")
        )

        assertEquals("PersonOmitNulls(name=Jim, address=Address(country=US))", person.toString())
    }

    @Test
    fun `an anonymous person object has the correct string representation`() {
        val person = object : Person(
            name = "Jim",
            age = 31,
            address = Person.Address(
                streetNumber = 123,
                streetName = "Sesame",
                country = "US"
            )
        ) {}

        assertEquals(
            "Person(name=Jim, age=31, address=Address(streetNumber=123, streetName=Sesame, country=US))",
            person.toString()
        )
    }

    @Test
    fun `a cat that extends animal has the correct string representation (with super field)`() {
        val cat = Animal.Cat(name = "Marmalade", mice = 1)

        assertEquals("Cat(mice=1, super=Animal(name=Marmalade))", cat.toString())
    }

    @Test
    fun `a dog that extends animal has the correct string representation (with super field)`() {
        val dog = Animal.Dog(name = "Fido", bones = 2)

        assertEquals("Dog(bones=2, balls=null, super=Animal(name=Fido))", dog.toString())
    }

    @Test
    fun `an object with a 2D array has the correct string representation`() {
        val value = ClassWithArray(
            array = arrayOf(
                arrayOf(1, 2, 3),
                null,
                arrayOf(4, 5, 6),
                arrayOf(7, 8, 9)
            )
        )

        assertEquals("ClassWithArray(array=[[1, 2, 3], null, [4, 5, 6], [7, 8, 9]])", value.toString())
    }
}

/**
 * Simple Person class.
 */
private open class Person(val name: String, val age: Int? = null, val address: Address) {

    override fun toString() = kotlinToString(
        properties = arrayOf(Person::name, Person::age, Person::address)
    )

    class Address(val streetNumber: Int? = null, val streetName: String? = null, val country: String) {
        override fun toString() = kotlinToString(
            properties = arrayOf(Address::streetNumber, Address::streetName, Address::country)
        )
    }
}

/**
 * Person class that omits nulls in its string representation.
 */
private class PersonOmitNulls(val name: String, val age: Int? = null, val address: Address) {

    override fun toString() = kotlinToString(
        properties = arrayOf(PersonOmitNulls::name, PersonOmitNulls::age, PersonOmitNulls::address),
        omitNulls = true
    )

    class Address(val streetNumber: Int? = null, val streetName: String? = null, val country: String) {
        override fun toString() = kotlinToString(
            properties = arrayOf(Address::streetNumber, Address::streetName, Address::country),
            omitNulls = true
        )
    }
}

/**
 * A class with a 2d array.
 */
private class ClassWithArray(val array: Array<Array<Int>?>) {
    override fun toString() = kotlinToString(properties = arrayOf(ClassWithArray::array))
}
