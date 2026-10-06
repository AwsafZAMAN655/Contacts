package com.example.fastcontacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.ContentValues
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class DetailActivity : AppCompatActivity() {

    private var contactId = -1L
    private var details: ContactDetails? = null
    private val bg = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    private lateinit var avatar: TextView
    private lateinit var nameView: TextView
    private lateinit var btnStar: Button
    private lateinit var phonesBox: LinearLayout
    private lateinit var emailsBox: LinearLayout
    private lateinit var othersBox: LinearLayout
    private lateinit var phonesHeader: View
    private lateinit var emailsHeader: View
    private lateinit var othersHeader: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        contactId = intent.getLongExtra(EXTRA_CONTACT_ID, -1L)

        avatar = findViewById(R.id.detailAvatar)
        nameView = findViewById(R.id.detailName)
        btnStar = findViewById(R.id.btnStar)
        phonesBox = findViewById(R.id.phonesBox)
        emailsBox = findViewById(R.id.emailsBox)
        othersBox = findViewById(R.id.othersBox)
        phonesHeader = findViewById(R.id.phonesHeader)
        emailsHeader = findViewById(R.id.emailsHeader)
        othersHeader = findViewById(R.id.othersHeader)

        findViewById<Button>(R.id.btnEdit).setOnClickListener { editContact() }
        findViewById<Button>(R.id.btnShare).setOnClickListener { shareContact() }
        btnStar.setOnClickListener { toggleStar() }
        findViewById<Button>(R.id.btnAddNumber).setOnClickListener { addNumber() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { confirmDelete() }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroy() {
        super.onDestroy()
        bg.shutdown()
    }

    private fun load() {
        bg.execute {
            val d: ContactDetails? = try {
                DetailRepository.load(applicationContext, contactId)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                if (d == null) {
                    finish()
                } else {
                    details = d
                    render(d)
                }
            }
        }
    }

    private fun render(d: ContactDetails) {
        nameView.text = d.displayName
        avatar.text = d.displayName.trim().firstOrNull { it.isLetterOrDigit() }
            ?.uppercaseChar()?.toString() ?: "#"
        (avatar.background.mutate() as GradientDrawable).setColor(0xFF1A73E8.toInt())

        btnStar.text = if (d.isStarred) "Starred" else "Star"

        val inflater = LayoutInflater.from(this)

        phonesBox.removeAllViews()
        phonesHeader.visibility = if (d.phones.isEmpty()) View.GONE else View.VISIBLE
        for (p in d.phones) {
            val row = inflater.inflate(R.layout.item_phone, phonesBox, false)
            row.findViewById<TextView>(R.id.phoneNumber).text = p.number
            row.findViewById<TextView>(R.id.phoneType).text = p.typeLabel
            row.findViewById<Button>(R.id.btnCall).setOnClickListener { directCallOrDial(p.number) }
            row.findViewById<Button>(R.id.btnSms).setOnClickListener { sms(p.number) }
            row.findViewById<Button>(R.id.btnCopy).setOnClickListener { copy(p.number) }
            phonesBox.addView(row)
        }

        emailsBox.removeAllViews()
        emailsHeader.visibility = if (d.emails.isEmpty()) View.GONE else View.VISIBLE
        for (e in d.emails) {
            val row = inflater.inflate(R.layout.item_info, emailsBox, false)
            row.findViewById<TextView>(R.id.infoLabel).text = e.label
            row.findViewById<TextView>(R.id.infoValue).text = e.value
            row.setOnClickListener { email(e.value) }
            emailsBox.addView(row)
        }

        othersBox.removeAllViews()
        othersHeader.visibility = if (d.others.isEmpty()) View.GONE else View.VISIBLE
        for (o in d.others) {
            val row = inflater.inflate(R.layout.item_info, othersBox, false)
            row.findViewById<TextView>(R.id.infoLabel).text = o.label
            row.findViewById<TextView>(R.id.infoValue).text = o.value
            othersBox.addView(row)
        }
    }

    private fun safeStart(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app found for this action", Toast.LENGTH_SHORT).show()
        }
    }

    private fun directCallOrDial(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null)))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
            }
        } else {
            safeStart(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
        }
    }

    private fun sms(number: String) =
        safeStart(Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null)))

    private fun email(address: String) =
        safeStart(Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null)))

    private fun copy(number: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("phone number", number))
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareContact() {
        val d = details ?: return
        val text = buildString {
            append("Contact: ").append(d.displayName).append("\n")
            d.phones.forEach { append(it.typeLabel).append(": ").append(it.number).append("\n") }
            d.emails.forEach { append(it.label).append(": ").append(it.value).append("\n") }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        safeStart(Intent.createChooser(intent, "Share contact via"))
    }

    private fun toggleStar() {
        val d = details ?: return
        val newStarred = if (d.isStarred) 0 else 1
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, d.id)
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, newStarred)
        }
        bg.execute {
            try {
                contentResolver.update(uri, values, null, null)
                main.post { load() }
            } catch (e: Exception) {
                main.post { Toast.makeText(this, "Could not update favorite status", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun lookupUri(): Uri? {
        val d = details ?: return null
        return ContactsContract.Contacts.getLookupUri(d.id, d.lookupKey)
    }

    private fun editContact() {
        val uri = lookupUri() ?: return
        safeStart(Intent(Intent.ACTION_EDIT, uri))
    }

    private fun addNumber() {
        val uri = lookupUri() ?: return
        safeStart(
            Intent(Intent.ACTION_EDIT, uri)
                .putExtra(ContactsContract.Intents.Insert.PHONE, "")
        )
    }

    private fun confirmDelete() {
        val d = details ?: return
        AlertDialog.Builder(this)
            .setTitle("Delete contact?")
            .setMessage("Delete " + d.displayName + " from this phone's contacts?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteContact() }
            .show()
    }

    private fun deleteContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_CONTACTS), REQ_WRITE
            )
            return
        }
        val uri = lookupUri() ?: return
        val deleted = try {
            contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            0
        }
        if (deleted > 0) {
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Could not delete this contact", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_WRITE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            deleteContact()
        }
    }

    companion object {
        const val EXTRA_CONTACT_ID = "contact_id"
        private const val REQ_WRITE = 2
    }
}
