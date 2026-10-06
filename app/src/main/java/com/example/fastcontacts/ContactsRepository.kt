package com.example.fastcontacts

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data

object ContactsRepository {

    fun loadAll(context: Context): List<Contact> {
        val cr = context.contentResolver
        val res = context.resources

        val contacts = ArrayList<ContactRow>()
        cr.query(
            Contacts.CONTENT_URI,
            arrayOf(Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY, Contacts.STARRED),
            Contacts.IN_VISIBLE_GROUP + " = 1",
            null,
            null
        )?.use { c ->
            while (c.moveToNext()) {
                contacts.add(
                    ContactRow(
                        c.getLong(0),
                        c.getString(1) ?: "",
                        c.getString(2) ?: "",
                        c.getInt(3) == 1
                    )
                )
            }
        }

        val names = ArrayList<NameRow>()
        cr.query(
            Data.CONTENT_URI,
            arrayOf(
                Data.CONTACT_ID,
                StructuredName.GIVEN_NAME,
                StructuredName.FAMILY_NAME,
                StructuredName.DISPLAY_NAME
            ),
            Data.MIMETYPE + " = ?",
            arrayOf(StructuredName.CONTENT_ITEM_TYPE),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                names.add(
                    NameRow(
                        c.getLong(0),
                        c.getString(1) ?: "",
                        c.getString(2) ?: "",
                        c.getString(3) ?: ""
                    )
                )
            }
        }

        val phones = ArrayList<PhoneRow>()
        cr.query(
            Phone.CONTENT_URI,
            arrayOf(Phone.CONTACT_ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL),
            null,
            null,
            Phone.CONTACT_ID + " ASC, " + Data.IS_SUPER_PRIMARY + " DESC, " + Data.IS_PRIMARY + " DESC"
        )?.use { c ->
            while (c.moveToNext()) {
                val label = Phone.getTypeLabel(res, c.getInt(2), c.getString(3)).toString()
                phones.add(PhoneRow(c.getLong(0), c.getString(1) ?: "", label))
            }
        }

        return ContactGrouper.group(contacts, names, phones)
    }
}
