package com.example.fastcontacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactSearchTest {

    private var nextId = 1L

    private fun person(display: String, vararg numbers: String): Contact {
        val id = nextId++
        val row = ContactRow(id, "key$id", display, false)
        val phones = numbers.map { PhoneRow(id, it, "Mobile") }
        return ContactGrouper.group(listOf(row), emptyList(), phones).single()
    }

    private fun base(): List<Contact> = listOf(
        person("David Ahmed", "01711111111"),
        person("Daniel Khan", "01544444444"),
        person("Dipu Rahman", "01655555555")
    )

    private fun names(list: List<Contact>) = list.map { it.displayName }

    @Test
    fun searchIsCaseInsensitive() {
        val e = SearchEngine(base())
        assertEquals(names(e.search("d")), names(SearchEngine(base()).search("D")))
    }

    @Test
    fun starredContactAppearsFirstInDefaultList() {
        val c1 = Contact(1, "k1", "Alpha", "Alpha", "", "Alpha", emptyList(), false)
        val c2 = Contact(2, "k2", "Zeta", "Zeta", "", "Zeta", emptyList(), true)
        val list = SearchEngine(listOf(c1, c2)).search("")
        assertEquals("Zeta", list[0].displayName)
    }
}
