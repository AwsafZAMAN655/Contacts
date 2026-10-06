import os

PROJECT_ROOT = "/sdcard/Download/FastContacts"

FILES = {
    "build.gradle.kts": """
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
""",

    "settings.gradle.kts": """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "FastContacts"
include(":app")
""",

    "gradle/libs.versions.toml": """
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
coreKt = "1.15.0"
appcompat = "1.7.0"
material = "1.12.0"
recyclerview = "1.3.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKt" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-recyclerview = { group = "androidx.recyclerview", name = "recyclerview", version.ref = "recyclerview" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
""",

    "app/build.gradle.kts": """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.fastcontacts"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fastcontacts"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
}
""",

    "app/src/main/AndroidManifest.xml": """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.WRITE_CONTACTS" />
    <uses-permission android:name="android.permission.CALL_PHONE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Fast Contacts"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.FastContacts">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustPan">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".DetailActivity" />
        <activity android:name=".DialpadActivity" />

    </application>
</manifest>
""",

    "app/src/main/res/values/styles.xml": """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.FastContacts" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">#2196F3</item>
        <item name="colorPrimaryVariant">#1976D2</item>
        <item name="colorOnPrimary">#FFFFFF</item>
    </style>
</resources>
""",

    "app/src/main/res/layout/activity_main.xml": """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp">

        <EditText
            android:id="@+id/search"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="Search contacts..."
            android:inputType="text"
            android:maxLines="1" />

        <ImageButton
            android:id="@+id/btnClearSearch"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Clear"
            android:src="@android:drawable/ic_menu_close_clear_cancel"
            android:visibility="gone" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp">

        <Button
            android:id="@+id/btnCreateContact"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="New Contact" />

        <Button
            android:id="@+id/dialButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Dialpad" />
    </LinearLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/list"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <TextView
            android:id="@+id/empty"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="No contacts found"
            android:visibility="gone" />

        <LinearLayout
            android:id="@+id/permissionPanel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:gravity="center"
            android:orientation="vertical"
            android:visibility="gone">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Contact permissions required" />

            <Button
                android:id="@+id/permissionButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="Grant Access" />
        </LinearLayout>
    </FrameLayout>
</LinearLayout>
""",

    "app/src/main/java/com/example/fastcontacts/Contact.kt": """package com.example.fastcontacts

data class Phone(val number: String, val type: String)
data class Email(val address: String, val type: String)

data class Contact(
    val id: Long,
    val name: String,
    val phones: List<Phone> = emptyList(),
    val emails: List<Email> = emptyList()
)
""",

    "app/src/main/java/com/example/fastcontacts/ContactsRepository.kt": """package com.example.fastcontacts

import android.content.Context
import android.provider.ContactsContract

object ContactsRepository {
    fun loadAll(context: Context): List<Contact> {
        val contactsMap = mutableMapOf<Long, MutableContact>()
        val cr = context.contentResolver

        val cursor = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            null, null, "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIdx = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            while (it.moveToNext()) {
                val id = it.getLong(idIdx)
                val name = it.getString(nameIdx) ?: "Unknown"
                contactsMap[id] = MutableContact(id, name)
            }
        }

        val phoneCursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
            ),
            null, null, null
        )

        phoneCursor?.use {
            val cIdIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            while (it.moveToNext()) {
                val cId = it.getLong(cIdIdx)
                val num = it.getString(numIdx) ?: continue
                val typeInt = it.getInt(typeIdx)
                val typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources, typeInt, ""
                ).toString()
                contactsMap[cId]?.phones?.add(Phone(num, typeLabel))
            }
        }

        return contactsMap.values.map { it.toContact() }
    }

    private class MutableContact(val id: Long, val name: String) {
        val phones = mutableListOf<Phone>()
        val emails = mutableListOf<Email>()
        fun toContact() = Contact(id, name, phones, emails)
    }
}
""",

    "app/src/main/java/com/example/fastcontacts/SearchEngine.kt": """package com.example.fastcontacts

class SearchEngine(private val contacts: List<Contact>) {
    fun search(query: String): List<Contact> {
        if (query.isBlank()) return contacts
        val q = query.lowercase().trim()
        return contacts.filter { contact ->
            contact.name.lowercase().contains(q) ||
            contact.phones.any { it.number.replace(Regex("[^0-9]"), "").contains(q) }
        }
    }
}
""",

    "app/src/main/java/com/example/fastcontacts/ContactAdapter.kt": """package com.example.fastcontacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.VH>() {

    private var items = emptyList<Contact>()

    fun submit(list: List<Contact>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.text1.text = c.name
        holder.text2.text = c.phones.firstOrNull()?.number ?: "No phone number"
        holder.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val text1: TextView = v.findViewById(android.R.id.text1)
        val text2: TextView = v.findViewById(android.R.id.text2)
    }
}
""",

    "app/src/main/java/com/example/fastcontacts/MainActivity.kt": """package com.example.fastcontacts

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
""",

    "app/src/main/java/com/example/fastcontacts/DetailActivity.kt": """package com.example.fastcontacts

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        val id = intent.getLongExtra(EXTRA_CONTACT_ID, -1L)
        tv.text = "Contact Details ID: $id"
        setContentView(tv)
    }

    companion object {
        const val EXTRA_CONTACT_ID = "extra_contact_id"
    }
}
""",

    "app/src/main/java/com/example/fastcontacts/DialpadActivity.kt": """package com.example.fastcontacts

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DialpadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "Dialpad Screen"
        setContentView(tv)
    }
}
"""
}

def main():
    for rel_path, content in FILES.items():
        full_path = os.path.join(PROJECT_ROOT, rel_path)
        os.makedirs(os.path.dirname(full_path), exist_ok=True)
        with open(full_path, "w", encoding="utf-8") as f:
            f.write(content.strip())
    print(f"Project successfully generated at: {PROJECT_ROOT}")

if __name__ == "__main__":
    main()
