import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.madlabexam4.Note
import com.example.madlabexam4.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    suspend fun getAllNotesAsync(): List<Note> = withContext(Dispatchers.IO) { getAllNotes() }

    companion object {
        private const val DATABASE_NAME = "notesapp.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "allnotes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_PRIORITY = "priority"
        private const val COLUMN_DEADLINE = "deadline"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TITLE TEXT, $COLUMN_CONTENT TEXT, $COLUMN_PRIORITY TEXT, $COLUMN_DEADLINE TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            writableDatabase.use { db ->
                ContentValues().apply {
                    put(COLUMN_TITLE, note.title)
                    put(COLUMN_CONTENT, note.content)
                    put(COLUMN_PRIORITY, note.priority.name) // Store priority as string
                    put(COLUMN_DEADLINE, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(note.deadline)) // Format deadline as yyyy-MM-dd
                }.also { values ->
                    db.insert(TABLE_NAME, null, values)
                }
            }
        }
    }

    suspend fun getAllNotes(): List<Note> = withContext(Dispatchers.IO) {
        readableDatabase.use { db ->
            val notesList = mutableListOf<Note>()
            db.rawQuery("SELECT * FROM $TABLE_NAME", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                    val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                    val priority = Priority.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)))
                    val deadlineString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEADLINE))
                    val deadline = deadlineString?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
                    } ?: Date() // Provide a default value if the deadline is null
                    notesList.add(Note(id, title, content, priority, deadline))
                }
            }
            notesList
        }
    }



    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, note.title)
                put(COLUMN_CONTENT, note.content)
                put(COLUMN_PRIORITY, note.priority.name) // Store priority as string
                put(COLUMN_DEADLINE, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(note.deadline)) // Format deadline as yyyy-MM-dd
            }
            val whereClause = "$COLUMN_ID = ?"
            val whereArgs = arrayOf(note.id.toString())
            db.update(TABLE_NAME, values, whereClause, whereArgs)
            db.close()
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return withContext(Dispatchers.IO) {
            val db = readableDatabase
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $noteId"
            val cursor: Cursor? = db.rawQuery(query, null)
            var note: Note? = null
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                    val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                    val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT))
                    val priority = Priority.valueOf(it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY))) // Retrieve priority from string
                    val deadline = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.getString(it.getColumnIndexOrThrow(COLUMN_DEADLINE)))!! // Parse deadline from string
                    note = Note(id, title, content, priority, deadline)
                }
            }
            cursor?.close()
            note
        }
    }

    suspend fun deleteNote(noteId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val db = writableDatabase
            val whereClause = "$COLUMN_ID = ?"
            val whereArgs = arrayOf(noteId.toString())
            val rowsAffected = db.delete(TABLE_NAME, whereClause, whereArgs)
            db.close()
            // If rowsAffected > 0, deletion was successful
            rowsAffected > 0
        }
    }


    suspend fun searchNotes(query: String): List<Note> {
        return withContext(Dispatchers.IO) {
            val notesList = mutableListOf<Note>()
            val db = readableDatabase
            val selection = "$COLUMN_TITLE LIKE ? OR $COLUMN_CONTENT LIKE ?"
            val selectionArgs = arrayOf("%$query%", "%$query%")
            val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
            cursor.use {
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                    val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                    val priority = Priority.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY))) // Retrieve priority from string
                    val deadline = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEADLINE)))!! // Parse deadline from string
                    val note = Note(id, title, content, priority, deadline)
                    notesList.add(note)
                }
            }
            notesList
        }
    }


}
