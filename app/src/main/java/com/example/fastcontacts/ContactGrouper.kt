package com.example.fastcontacts

data class ContactRow(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val isStarred: Boolean
)

data class NameRow(val contactId: Long, val given: String, val family: String, val display: String)

data class PhoneRow(val contactId: Long, val number: String, val typeLabel: String)

object ContactGrouper {
    private val WS = Regex("\\s+")

    fun dedupeKey(number: String): String {
        val d = number.filter { it.isDigit() }
        return when {
            d.isEmpty() -> number.trim()
            d.length > 10 -> d.takeLast(10)
            else -> d
        }
    }

    fun group(
        contacts: List<ContactRow>,
        names: List<NameRow>,
        phones: List<PhoneRow>
    ): List<Contact> {
        val displayById = HashMap<Long, String>()
        for (c in contacts) displayById[c.id] = c.displayName

        val exact = HashMap<Long, NameRow>()
        val any = HashMap<Long, NameRow>()
        for (n in names) {
            if (n.given.isBlank() && n.family.isBlank()) continue
            if (!any.containsKey(n.contactId)) any[n.contactId] = n
            if (!exact.containsKey(n.contactId) &&
                n.display.equals(displayById[n.contactId], ignoreCase = true)
            ) {
                exact[n.contactId] = n
            }
        }

        val phonesById = HashMap<Long, ArrayList<PhoneNumber>>()
        val seen = HashSet<String>()
        for (p in phones) {
            if (p.number.isBlank()) continue
            if (!seen.add(p.contactId.toString() + "|" + dedupeKey(p.number))) continue
            phonesById.getOrPut(p.contactId) { ArrayList() }
                .add(PhoneNumber(p.number.trim(), p.typeLabel))
        }

        val out = ArrayList<Contact>(contacts.size)
        for (row in contacts) {
            val plist: List<PhoneNumber> = phonesById[row.id] ?: emptyList()
            val display = row.displayName.trim().ifEmpty {
                plist.firstOrNull()?.number ?: "(No name)"
            }

            var first = ""
            var last = ""
            var full = ""
            val nr = exact[row.id] ?: any[row.id]
            if (nr != null) {
                val givenTokens = nr.given.trim().split(WS).filter { it.isNotEmpty() }
                first = givenTokens.firstOrNull() ?: ""
                last = when {
                    nr.family.isNotBlank() -> nr.family.trim()
                    givenTokens.size > 1 -> givenTokens.last()
                    else -> ""
                }
                full = (nr.given + " " + nr.family).trim()
            }
            if (first.isEmpty()) {
                val parts = display.split(WS).filter { it.isNotEmpty() }
                first = parts.firstOrNull() ?: display
                last = if (parts.size > 1) parts.last() else ""
                full = display
            }
            out.add(Contact(row.id, row.lookupKey, display, first, last, full, plist, row.isStarred))
        }
        return out
    }
}
