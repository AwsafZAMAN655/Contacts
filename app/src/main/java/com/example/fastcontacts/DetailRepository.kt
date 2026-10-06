package com.example.fastcontacts

import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data

class LabeledValue(val label: String, val value: String)

class ContactDetails(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val isStarred: Boolean,
    val phones: List<PhoneNumber>,
    val emails: List<LabeledValue>,
    val others: List<LabeledValue>
)

object DetailRepository {

    fun load(context: Context, contactId: Long): ContactDetails? {
        val cr = context.contentResolver
        val res = context.resources

        var lookup = ""
        var name: String? = null
        var isStarred = false
        cr.query(
            ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
            arrayOf(Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY, Contacts.STARRED),
            null,
            null,
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                lookup = c.getString(0) ?: ""
                name = c.getString(1) ?: ""
                isStarred = c.getInt(2) == 1
            }
        }
        val displayName: String = name ?: return null

        val phones = ArrayList<PhoneNumber>()
        val emails = ArrayList<LabeledValue>()
        val others = ArrayList<LabeledValue>()
        val seen = HashSet<String>()

        cr.query(
            Data.CONTENT_URI,
            arrayOf(Data.MIMETYPE, Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4),
            Data.CONTACT_ID + " = ?",
            arrayOf(contactId.toString()),
            Data.IS_SUPER_PRIMARY + " DESC, " + Data.IS_PRIMARY + " DESC"
        )?.use { c ->
            while (c.moveToNext()) {
                val mime = c.getString(0) ?: continue
                val d1 = c.getString(1) ?: ""
                if (d1.isBlank() && mime != Organization.CONTENT_ITEM_TYPE) continue
                when (mime) {
                    Phone.CONTENT_ITEM_TYPE -> {
                        if (seen.add(ContactGrouper.dedupeKey(d1))) {
                            val label = Phone.getTypeLabel(res, c.getInt(2), c.getString(3)).toString()
                            phones.add(PhoneNumber(d1.trim(), label))
                        }
                    }
                    Email.CONTENT_ITEM_TYPE -> {
                        val label = Email.getTypeLabel(res, c.getInt(2), c.getString(3)).toString()
                        emails.add(LabeledValue(label, d1.trim()))
                    }
                    Organization.CONTENT_ITEM_TYPE -> {
                        val title = c.getString(4) ?: ""
                        val text = listOf(d1, title).filter { it.isNotBlank() }.joinToString(" - ")
                        if (text.isNotEmpty()) others.add(LabeledValue("Organization", text))
                    }
                    StructuredPostal.CONTENT_ITEM_TYPE -> {
                        val label = StructuredPostal.getTypeLabel(res, c.getInt(2), c.getString(3)).toString()
                        others.add(LabeledValue(label, d1.trim()))
                    }
                    Event.CONTENT_ITEM_TYPE -> {
                        val label = Event.getTypeLabel(res, c.getInt(2), c.getString(3)).toString()
                        others.add(LabeledValue(label, d1.trim()))
                    }
                    Website.CONTENT_ITEM_TYPE -> others.add(LabeledValue("Website", d1.trim()))
                    Note.CONTENT_ITEM_TYPE -> others.add(LabeledValue("Note", d1.trim()))
                    else -> {}
                }
            }
        }
        return ContactDetails(contactId, lookup, displayName, isStarred, phones, emails, others)
    }
}
