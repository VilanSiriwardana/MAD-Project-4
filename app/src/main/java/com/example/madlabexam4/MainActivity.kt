import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.madlabexam4.AddNoteActivity
import com.example.madlabexam4.NotesAdapter
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

