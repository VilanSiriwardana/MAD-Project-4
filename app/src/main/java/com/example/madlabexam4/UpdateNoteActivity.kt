package com.example.madlabexam4

import NoteDatabaseHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.madlabexam4.databinding.ActivityUpdateNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UpdateNoteActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityUpdateNoteBinding
    private lateinit var db: NoteDatabaseHelper
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabaseHelper(this)

        // Spinner setup for priority
        val priorityOptions = arrayOf("High", "Medium", "Low")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorityOptions)
        binding.updatePrioritySpinner.adapter = priorityAdapter

        noteId = intent.getIntExtra("note_id", -1)
        if(noteId == -1){
            finish()
            return
        }

        // Use lifecycleScope to launch a coroutine
        lifecycleScope.launch {
            val note = db.getNoteById(noteId)
            binding.updateTitleEditText.setText(note?.title)
            binding.updateContentEditText.setText(note?.content)

            // Check if note is not null
            note?.let {
                // Set priority
                val priorityIndex = priorityOptions.indexOf(note.priority.name) // Use priority name
                binding.updatePrioritySpinner.setSelection(priorityIndex)

                // Set deadline
                it.deadline?.let { deadline ->
                    val deadlineString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(deadline)
                    binding.updateDeadlineEditText.setText(deadlineString)
                }
            }
        }



        binding.updateSaveButton.setOnClickListener {
            val newTitle = binding.updateTitleEditText.text.toString()
            val newContent = binding.updateContentEditText.text.toString()
            val newPriority = Priority.valueOf(binding.updatePrioritySpinner.selectedItem.toString())
            val newDeadlineString = binding.updateDeadlineEditText.text.toString()
            val newDeadline: Date? = if (newDeadlineString.isNotEmpty()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(newDeadlineString)
            } else {
                null
            }

            // Create the updated note
            val updatedNote = Note(noteId, newTitle, newContent, newPriority, newDeadline ?: Date())

            // Update note using coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                db.updateNote(updatedNote)
            }

            finish()
            Toast.makeText(this@UpdateNoteActivity, "Changes Saved", Toast.LENGTH_SHORT).show()
        }
    }
}
