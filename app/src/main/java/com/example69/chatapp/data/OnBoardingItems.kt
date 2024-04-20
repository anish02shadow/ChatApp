package com.example69.chatapp.data

import com.example69.chatapp.R

class OnBoardingItems(
    val image: Int,
    val title: String,
    val desc: String
) {
    companion object{
        fun getData(): List<OnBoardingItems>{
            return listOf(
                OnBoardingItems(R.drawable.moodiconsfull, "Connect with MoodChat", "Share your mood and messages with friends, but they can only view them, not reply."),
                OnBoardingItems(R.drawable.endtoend, "Unbreakable Privacy", "Every message is end-to-end encrypted, ensuring only you and your friends can access them."),
                OnBoardingItems(R.drawable.img_1, "Stay Updated Anywhere", "Add the MoodChat widget to your home screen for quick mood checks. Tap any mood to instantly update the widget."),
                OnBoardingItems(R.drawable.honest, "Choose Wisely", "While you can add and delete friends, remember that messages are permanent. Send your messages thoughtfully!"),
            )
        }
    }
}