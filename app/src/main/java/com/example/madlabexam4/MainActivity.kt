package com.example.madlabexam4

import NoteDatabaseHelper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.madlabexam4.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: NoteDatabaseHelper
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = NoteDatabaseHelper(this)
        notesAdapter = NotesAdapter(emptyList(), this) // Initialize with empty list

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }

        // Fetch notes asynchronously when activity starts
        CoroutineScope(Dispatchers.Main).launch {
            val notes = db.getAllNotesAsync()
            notesAdapter.refreshData(notes)
        }


        // Setup priority filter spinner
        val priorityOptions = arrayOf("All", "High", "Medium", "Low")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorityOptions)
        binding.prioritySpinner.adapter = priorityAdapter

        binding.prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPriority = priorityOptions[position]
                if (selectedPriority == "All") {
                    CoroutineScope(Dispatchers.Main).launch {
                        val notes = db.getAllNotesAsync()
                        notesAdapter.refreshData(notes)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        val filteredNotes = db.filterNotesByPriority(selectedPriority)
                        notesAdapter.refreshData(filteredNotes)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchNotes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchNotes(it) }
                return true
            }
        })
    }

    private fun searchNotes(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val filteredNotes = db.searchNotes(query)
            notesAdapter.refreshData(filteredNotes)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh notes when activity resumes
        CoroutineScope(Dispatchers.Main).launch {
            val notes = db.getAllNotesAsync()
            notesAdapter.refreshData(notes)
        }
    }
}

