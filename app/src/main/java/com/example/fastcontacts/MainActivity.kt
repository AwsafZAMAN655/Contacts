package com.example.fastcontacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var search: EditText
    private lateinit var btnClearSearch: ImageButton
    private lateinit var list: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var permissionPanel: View
    private lateinit var permissionButton: Button

    private var pendingCallNumber: String? = null

    private val adapter = ContactAdapter(
        onClick = { c ->
            startActivity(
                Intent(this, DetailActivity::class.java)
                    .putExtra(DetailActivity.EXTRA_CONTACT_ID, c.id)
            )
        },
        onCallClick = { c ->
            val num = c.phones.firstOrNull()?.number
            if (!num.isNullOrEmpty()) {
                makeDirectCall(num)
            }
        }
    )

    private val bg: ExecutorService = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    private var engine: SearchEngine? = null

    @Volatile
    private var generation = 0
    private var listShown = false
    private var observerRegistered = false
    private var permanentlyDenied = false

    private val reloadRunnable = Runnable { reload() }
    private val searchRunnable = Runnable { runSearch() }

    private val observer = object : ContentObserver(main) {
        override fun onChange(selfChange: Boolean) {
            main.removeCallbacks(reloadRunnable)
            main.postDelayed(reloadRunnable, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search = findViewById(R.id.search)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        list = findViewById(R.id.list)
        emptyView = findViewById(R.id.empty)
        permissionPanel = findViewById(R.id.permissionPanel)
        permissionButton = findViewById(R.id.permissionButton)

        list.layoutManager = LinearLayoutManager(this)
        list.setHasFixedSize(true)
        list.adapter = adapter

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnClearSearch.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                scheduleSearch()
            }
        })
        search.setOnEditorActionListener { _, _, _ ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(search.windowToken, 0)
            true
        }

        btnClearSearch.setOnClickListener {
            search.setText("")
        }

        findViewById<Button>(R.id.btnCreateContact).setOnClickListener {
            safeStart(Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI))
        }

        findViewById<Button>(R.id.dialButton).setOnClickListener {
            startActivity(Intent(this, DialpadActivity::class.java))
        }
        permissionButton.setOnClickListener { onPermissionButton() }

        if (hasPermission()) showList() else showPermissionPanel()
    }

    override fun onResume() {
        super.onResume()
        if (hasPermission() && !listShown) showList()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (observerRegistered) contentResolver.unregisterContentObserver(observer)
        main.removeCallbacksAndMessages(null)
        bg.shutdown()
    }

    private fun makeDirectCall(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null)))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
            }
        } else {
            pendingCallNumber = number
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CALL_PHONE), REQ_CALL
            )
        }
    }

    private fun safeStart(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No suitable app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    private fun showPermissionPanel() {
        listShown = false
        permissionPanel.visibility = View.VISIBLE
        list.visibility = View.GONE
        emptyView.visibility = View.GONE
        permissionButton.setText(
            if (permanentlyDenied) "Open app settings" else "Allow contacts access"
        )
    }

    private fun onPermissionButton() {
        if (permanentlyDenied) {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageName, null))
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS), REQ_READ
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_READ) {
            if (hasPermission()) {
                showList()
            } else {
                permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_CONTACTS
                )
                showPermissionPanel()
            }
        } else if (requestCode == REQ_CALL) {
            val num = pendingCallNumber
            pendingCallNumber = null
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && num != null) {
                makeDirectCall(num)
            } else if (num != null) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", num, null)))
            }
        }
    }

    private fun showList() {
        listShown = true
        permissionPanel.visibility = View.GONE
        list.visibility = View.VISIBLE
        if (!observerRegistered) {
            contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, observer
            )
            observerRegistered = true
        }
        reload()
    }

    private fun reload() {
        if (!hasPermission()) return
        bg.execute {
            val contacts: List<Contact> = try {
                ContactsRepository.loadAll(applicationContext)
            } catch (e: Exception) {
                emptyList<Contact>()
            }
            engine = SearchEngine(contacts)
            main.post { runSearch() }
        }
    }

    private fun scheduleSearch() {
        generation++
        main.removeCallbacks(searchRunnable)
        main.postDelayed(searchRunnable, 40)
    }

    private fun runSearch() {
        val myGeneration = generation
        val query = search.text.toString()
        bg.execute {
            val result = engine?.search(query)
            if (result != null) {
                main.post {
                    if (myGeneration == generation) {
                        adapter.submit(result)
                        emptyView.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private companion object {
        const val REQ_READ = 1
        const val REQ_CALL = 2
    }
}
