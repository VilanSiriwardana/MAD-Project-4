package com.example.madlabexam4

import NoteDatabaseHelper
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.madlabexam4.databinding.ActivityAddNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var db: NoteDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set content view using binding.root

        db = NoteDatabaseHelper(this)

        // Spinner setup for priority
        val priorityOptions = arrayOf("High", "Medium", "Low")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorityOptions)
        binding.prioritySpinner.adapter = priorityAdapter

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()
            val priority = Priority.valueOf(binding.prioritySpinner.selectedItem.toString()) // Get selected priority
            val deadlineString = binding.deadlineEditText.text.toString()
            val deadline: Date? = if (deadlineString.isNotEmpty()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(deadlineString)
            } else {
                null
            }

            // Ensure deadline is non-null before passing it to the Note constructor
            val note = if (deadline != null) {
                Note(0, title, content, priority, deadline)
            } else {
                // Provide a default value for deadline if it's null
                Note(0, title, content, priority, Date())
            }

            // Insert note using coroutine
            GlobalScope.launch(Dispatchers.IO) {
                db.insertNote(note)
            }

            finish()
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show()
        }
    }
}
