package com.example.madlabexam4

import NoteDatabaseHelper
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotesAdapter(private var notes: List<Note>, private val context: Context) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val db: NoteDatabaseHelper = NoteDatabaseHelper(context)
    private var filteredNotes: List<Note> = notes

    fun refreshData(newNotes: List<Note>) {
        CoroutineScope(Dispatchers.Main).launch {
            notes = newNotes
            filteredNotes = newNotes
            notifyDataSetChanged()
        }
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = filteredNotes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
                putExtra("note_id", note.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val db = NoteDatabaseHelper(context)
                db.deleteNote(note.id)
                val updatedNotes = db.getAllNotes()
                holder.itemView.post {
                    notes = updatedNotes
                    notifyDataSetChanged()
                    Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    fun filter(query: String) {
        filteredNotes = if (query.isEmpty()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
