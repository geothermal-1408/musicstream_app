package com.example.musicstream

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicstream.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.musicstream.models.ChatModel
import ChatAdapter

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatList = mutableListOf<ChatModel>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        chatAdapter = ChatAdapter(chatList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = chatAdapter

        // Send button click listener
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageEditText.text.clear()
            }
        }

        // Listen for real-time updates
        listenForMessages()
    }

    private fun sendMessage(message: String) {
        val senderId = auth.currentUser?.uid ?: "unknown"
        val chatMessage = ChatModel(message)

        db.collection("chats").add(chatMessage)
    }

    private fun listenForMessages() {
        db.collection("chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                chatList.clear()
                for (document in snapshot?.documents!!) {
                    val chatMessage = document.toObject(ChatModel::class.java)
                    chatMessage?.let { chatList.add(it) }
                }
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }
    }
}


