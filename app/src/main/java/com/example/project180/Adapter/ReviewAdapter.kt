package com.example.project180.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.Model.ReviewDto
import com.example.project180.R
import java.text.SimpleDateFormat
import java.text.ParseException
import java.util.*

class ReviewAdapter(private var reviewList: List<ReviewDto>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateReviews(newReviews: List<ReviewDto>) {
        reviewList = newReviews.filter { it.comment != null } // Filtre les avis sans commentaire
        notifyDataSetChanged()
        Log.d("ADAPTER_DEBUG", "Updated with ${reviewList.size} reviews")
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)

        // Formatters pour parser et formatter les dates
        private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        private val inputDateFormatSimple = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val outputDateTimeFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault())

        fun bind(review: ReviewDto) {
            tvUserName.text = "${review.userNom} ${review.userPrenom}" ?: "Anonyme"
            tvComment.text = review.comment ?: "Pas de commentaire"

            // Formatage de la date
            tvDate.text = formatDate(review.updatedAt)

            ratingBar.rating = review.rating ?: 0f
        }

        private fun formatDate(dateString: Any?): String {
            return try {
                when (dateString) {
                    is String -> {
                        val date = parseIsoDate(dateString)
                        if (date != null) {
                            // Retourne juste la date
                            outputDateFormat.format(date)

                            // Ou si vous voulez la date et l'heure :
                            // outputDateTimeFormat.format(date)
                        } else {
                            "Date invalide"
                        }
                    }
                    else -> "Date non disponible"
                }
            } catch (e: Exception) {
                Log.e("DATE_FORMAT", "Erreur formatage date: $dateString", e)
                "Erreur date"
            }
        }

        private fun parseIsoDate(dateString: String): Date? {
            return try {
                // Essaie d'abord avec les microsecondes
                if (dateString.contains('.')) {
                    // Tronque les microsecondes à 3 chiffres max (millisecondes)
                    val cleanDateString = dateString.replace(Regex("\\.\\d{4,6}"), ".000")
                    inputDateFormat.parse(cleanDateString.replace(".000", ".000"))
                } else {
                    // Format sans microsecondes
                    inputDateFormatSimple.parse(dateString)
                }
            } catch (e: ParseException) {
                Log.e("DATE_PARSE", "Erreur parsing date: $dateString", e)
                null
            }
        }
    }
}