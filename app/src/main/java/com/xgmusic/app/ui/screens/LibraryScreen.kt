package com.xgmusic.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xgmusic.app.data.model.MusicItem
import com.xgmusic.app.player.PlayerManager

@Composable
fun LibraryScreen(
    playerManager: PlayerManager
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("我喜欢", "最近播放", "我的歌单", "本地音乐")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> FavoriteList()
            1 -> RecentList()
            2 -> PlaylistList()
            3 -> LocalMusicList()
        }
    }
}

@Composable
fun FavoriteList() {
    Text("暂无收藏", modifier = Modifier.padding(16.dp))
}

@Composable
fun RecentList() {
    Text("暂无播放记录", modifier = Modifier.padding(16.dp))
}

@Composable
fun PlaylistList() {
    Text("暂无歌单", modifier = Modifier.padding(16.dp))
}

@Composable
fun LocalMusicList() {
    Text("暂无本地音乐", modifier = Modifier.padding(16.dp))
}
