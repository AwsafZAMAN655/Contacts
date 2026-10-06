package com.example.fastcontacts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DialpadActivity : AppCompatActivity() {

    private val number = StringBuilder()
    private lateinit var display: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialpad)
        display = findViewById(R.id.display)

        val pad = findViewById<LinearLayout>(R.id.pad)
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )
        for (keys in rows) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
            for (k in keys) {
                val b = Button(this)
                b.text = k
                b.textSize = 26f
                b.layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1f
                )
                b.setOnClickListener { append(k) }
                if (k == "0") {
                    b.setOnLongClickListener { append("+"); true }
                }
                row.addView(b)
            }
            pad.addView(row)
        }

        val back = findViewById<Button>(R.id.btnBackspace)
        back.setOnClickListener {
            if (number.isNotEmpty()) number.deleteCharAt(number.length - 1)
            refresh()
        }
        back.setOnLongClickListener {
            number.setLength(0)
            refresh()
            true
        }

        findViewById<Button>(R.id.btnDial).setOnClickListener {
            if (number.isNotEmpty()) {
                safeStart(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number.toString(), null)))
            }
        }
        findViewById<Button>(R.id.btnSaveContact).setOnClickListener {
            if (number.isNotEmpty()) {
                val i = Intent(Intent.ACTION_INSERT_OR_EDIT)
                i.type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                i.putExtra(ContactsContract.Intents.Insert.PHONE, number.toString())
                safeStart(i)
            }
        }
    }

    private fun append(s: String) {
        number.append(s)
        refresh()
    }

    private fun refresh() {
        display.text = number.toString()
    }

    private fun safeStart(i: Intent) {
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app found for this action", Toast.LENGTH_SHORT).show()
        }
    }
}
