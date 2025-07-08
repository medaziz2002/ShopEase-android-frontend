package com.example.project180.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.Adapter.ChatMessageAdapter
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.ChatMessage
import com.example.project180.Model.SimilarityRequest
import com.example.project180.Model.SimilarityResponse
import com.example.project180.databinding.ActivityChatBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()

        Log.d("ChatActivity", "=== DÉMARRAGE CHAT ACTIVITY ===")
        testConnection()
    }

    private fun testConnection() {
        Log.d("ChatActivity", "Test de connexion API...")
        // Optionnel : envoyer un message de test ici si besoin
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter()
        // CHANGÉ: utilisé recyclerMessages (nom correct du XML)
        binding.recyclerMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        Log.d("ChatActivity", "RecyclerView configuré")
    }

    private fun setupListeners() {
        // Bouton de retour
        binding.btnBack.setOnClickListener {
            Log.d("ChatActivity", "Bouton retour cliqué")
            finish() // Ferme l'activité et retourne à la précédente
        }

        // Bouton d'envoi
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            Log.d("ChatActivity", "=== BOUTON SEND CLIQUÉ ===")
            Log.d("ChatActivity", "Message saisi: '$message'")

            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.text.clear()
                // Masquer les suggestions après le premier message
                binding.suggestionsContainer.visibility = View.GONE
            } else {
                Log.w("ChatActivity", "Message vide!")
                Toast.makeText(this, "Veuillez saisir un message", Toast.LENGTH_SHORT).show()
            }
        }

        // Suggestions rapides - CORRECTION DES IDs
        binding.suggestion1.setOnClickListener {
            val suggestionText = binding.suggestion1.text.toString()
            Log.d("ChatActivity", "=== SUGGESTION 1 CLIQUÉE ===")
            Log.d("ChatActivity", "Texte suggestion: '$suggestionText'")
            sendMessage(suggestionText)
            // Masquer les suggestions après sélection
            binding.suggestionsContainer.visibility = View.GONE
        }

        binding.suggestion2.setOnClickListener {
            val suggestionText = binding.suggestion2.text.toString()
            Log.d("ChatActivity", "=== SUGGESTION 2 CLIQUÉE ===")
            Log.d("ChatActivity", "Texte suggestion: '$suggestionText'")
            sendMessage(suggestionText)
            binding.suggestionsContainer.visibility = View.GONE
        }

        binding.suggestion3.setOnClickListener {
            val suggestionText = binding.suggestion3.text.toString()
            Log.d("ChatActivity", "=== SUGGESTION 3 CLIQUÉE ===")
            Log.d("ChatActivity", "Texte suggestion: '$suggestionText'")
            sendMessage(suggestionText)
            binding.suggestionsContainer.visibility = View.GONE
        }

        binding.suggestion4.setOnClickListener {
            val suggestionText = binding.suggestion4.text.toString()
            Log.d("ChatActivity", "=== SUGGESTION 4 CLIQUÉE ===")
            Log.d("ChatActivity", "Texte suggestion: '$suggestionText'")
            sendMessage(suggestionText)
            binding.suggestionsContainer.visibility = View.GONE
        }
    }

    private fun sendMessage(message: String) {
        Log.d("ChatActivity", "=== DÉBUT SEND MESSAGE ===")
        Log.d("ChatActivity", "Message à envoyer: '$message'")
        Log.d("ChatActivity", "Thread actuel: ${Thread.currentThread().name}")


        val userMessage = ChatMessage(text = message, isUser = true)
        chatAdapter.addMessage(userMessage)

        binding.recyclerMessages.scrollToPosition(chatAdapter.itemCount - 1)
        Log.d("ChatActivity", "Message utilisateur ajouté. Total messages: ${chatAdapter.itemCount}")


        val typingMessage = ChatMessage(text = "Assistant réfléchit...", isUser = false)
        chatAdapter.addMessage(typingMessage)
        val typingPosition = chatAdapter.itemCount - 1

        binding.recyclerMessages.scrollToPosition(typingPosition)


        val request = SimilarityRequest(sentences = listOf(message))


        try {
            val call = RetrofitInstance.apiWithAuth.sendQuestions(request)
            Log.d("ChatActivity", "Call créé: $call")

            call.enqueue(object : Callback<SimilarityResponse> {
                override fun onResponse(call: Call<SimilarityResponse>, response: Response<SimilarityResponse>) {
                    Log.d("ChatActivity", "=== RÉPONSE REÇUE ===")
                    Log.d("ChatActivity", "Code: ${response.code()}")
                    Log.d("ChatActivity", "Success: ${response.isSuccessful}")

                    runOnUiThread {
                        chatAdapter.removeMessageAt(typingPosition)
                    }

                    if (response.isSuccessful) {
                        val similarityResponse = response.body()
                        Log.d("ChatActivity", "Body reçu: $similarityResponse")

                        if (similarityResponse != null && !similarityResponse.results.isNullOrEmpty()) {
                            val firstResult = similarityResponse.results[0]
                            Log.d("ChatActivity", "Premier résultat: $firstResult")

                            val botReply = firstResult.result?.reponse ?: "Pas de réponse disponible"
                            Log.d("ChatActivity", "Réponse extraite: '$botReply'")

                            runOnUiThread {
                                val botMessage = ChatMessage(text = botReply, isUser = false)
                                chatAdapter.addMessage(botMessage)
                                // CHANGÉ: utilisé recyclerMessages (nom correct du XML)
                                binding.recyclerMessages.scrollToPosition(chatAdapter.itemCount - 1)
                                Log.d("ChatActivity", "Message bot affiché")
                            }
                        } else {
                            Log.e("ChatActivity", "Results vide ou null!")
                            runOnUiThread {
                                showError("Aucune réponse trouvée")
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ChatActivity", "Erreur HTTP: ${response.code()}")
                        Log.e("ChatActivity", "Error body: $errorBody")
                        runOnUiThread {
                            showError("Erreur serveur: ${response.code()}")
                        }
                    }
                }

                override fun onFailure(call: Call<SimilarityResponse>, t: Throwable) {
                    Log.e("ChatActivity", "=== ÉCHEC RÉSEAU ===")
                    Log.e("ChatActivity", "Type d'erreur: ${t.javaClass.simpleName}")
                    Log.e("ChatActivity", "Message: ${t.message}")
                    Log.e("ChatActivity", "Cause: ${t.cause}")
                    t.printStackTrace()

                    runOnUiThread {
                        chatAdapter.removeMessageAt(typingPosition)
                        showError("Erreur réseau: ${t.message}")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("ChatActivity", "Erreur lors de la création de l'appel", e)
            runOnUiThread {
                showError("Erreur technique: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Log.e("ChatActivity", "Erreur affichée: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        val errorMessage = ChatMessage(text = "❌ $message", isUser = false)
        chatAdapter.addMessage(errorMessage)
        // CHANGÉ: utilisé recyclerMessages (nom correct du XML)
        binding.recyclerMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }
}