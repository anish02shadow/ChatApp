package com.example69.chatapp

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.example69.chatapp.data.StoreUserEmail
import com.example69.chatapp.firebase.getMood
import com.example69.chatapp.ui.theme.Screens.getDrawableIdByText
import kotlinx.coroutines.flow.first

object MoodWidget: GlanceAppWidget() {

    val moodKey = stringSetPreferencesKey("mood")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = StoreUserEmail(context)
        var moods = getMood(dataStore).first()
        provideContent {
            ViewStoryLayout2(moods)
        }
    }
}

class MoodWidgetReceiver: GlanceAppWidgetReceiver(){
    override val glanceAppWidget: GlanceAppWidget
        get() = MoodWidget
}

class FetchActionCallBack: ActionCallback{
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context,glanceId){prefs ->
            val currentMoods = prefs[MoodWidget.moodKey]
            if(currentMoods!=null){
                val dataStore = StoreUserEmail(context)
                prefs[MoodWidget.moodKey] = getMood(dataStore = dataStore).first()
            }
            else{
                val dataStore = StoreUserEmail(context)
                prefs[MoodWidget.moodKey] = getMood(dataStore = dataStore).first()
            }
        }
        MoodWidget.update(context = context, id = glanceId)
    }
}
@Composable
fun UserStory2(
    friends: Pair<String,String>
) {
    androidx.glance.layout.Row(
        modifier = GlanceModifier.padding(end = 10.dp, bottom = 5.dp),
        verticalAlignment = androidx.glance.layout.Alignment.CenterVertically,
        horizontalAlignment = androidx.glance.layout.Alignment.CenterHorizontally
    ) {
        androidx.glance.layout.Box(
            modifier = GlanceModifier
                .background(day = Color.Yellow, night = Color.Yellow)
                .size(70.dp)
                .cornerRadius(15.dp)
                .padding(4.dp)
                .clickable{
                         actionRunCallback(FetchActionCallBack::class.java)
                },
            contentAlignment = androidx.glance.layout.Alignment.Center
        ) {
            val drawableId = getDrawableIdByText(friends.second)
            if (drawableId.equals("null")) {
                androidx.glance.Image(
                    provider = ImageProvider(R.drawable.ic_launcher_background),
                    modifier = GlanceModifier
                        .size(64.dp).cornerRadius(15.dp),
                    contentDescription = "Mood of user"
                )

            } else {
                androidx.glance.Image(
                    provider = ImageProvider(drawableId),
                    modifier = GlanceModifier
                        .size(64.dp).cornerRadius(15.dp),
                    contentDescription = "Mood of user"
                )

            }
        }
        androidx.glance.layout.Box(
            modifier = GlanceModifier
                .cornerRadius(15.dp).padding(4.dp),
            contentAlignment = androidx.glance.layout.Alignment.BottomCenter
        ) {
            val displayText = buildAnnotatedString {
                append(friends.first.take(8))
                if (friends.first.length > 8) {
                    addStyle(style = SpanStyle(color = Color.Black), start = 8, end = friends.first.length)
                    append("...")
                }
            }
            Text(
                text = displayText.toString(), style = androidx.glance.text.TextStyle(fontSize = 18.sp, color = ColorProvider(
                    Color.Black), fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ViewStoryLayout2(mood: Set<String>) {
    val friends = currentState<Set<String>>(key = MoodWidget.moodKey)?.toList() ?: emptyList()
    val moods = mutableListOf<Pair<String, String>>()
    if(friends.isEmpty()){
        mood.forEach { it ->
            val (username, moodValue) = it.split("|")
            moods.add(username to moodValue)
        }
    }
    else{
        friends.forEach { it ->
            val (username, moodValue) = it.split("|")
            moods.add(username to moodValue)
        }
    }
    LazyColumn(modifier = GlanceModifier.padding(top = 12.dp, start = 12.dp, bottom = 12.dp, end = 4.dp)) {
        items(moods){ friend ->
            if (friend != null) {
                UserStory2(friends = friend)
            }
        }
    }
}
