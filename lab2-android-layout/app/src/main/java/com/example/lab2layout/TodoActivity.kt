package com.example.lab2layout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 第 5 题：Compose 版待办清单。
 * 与前面四题的区别是这里没有一行 XML —— 状态用 mutableStateListOf 管理，
 * 勾选后重组（recomposition）自动刷新完成计数和删除线，这就是声明式 UI 的核心。
 */
class TodoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoScreen()
            }
        }
    }
}

private data class TodoItem(
    val title: String,
    val done: Boolean = false
)

@Composable
fun TodoScreen() {
    val todos = remember {
        mutableStateListOf(
            TodoItem("复习 LinearLayout 的 weight 分配", true),
            TodoItem("实现 TableLayout 菜单表"),
            TodoItem("用 ConstraintLayout 排 16 键计算器")
        )
    }
    var draft by remember { mutableStateOf("") }

    val doneCount = todos.count { it.done }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F5F7))
    ) {
        // 页头：和 XML 页面保持同一套 navy 视觉
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B3A57))
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp)
        ) {
            Text(
                text = "COMPOSE",
                color = Color(0xFF9FB8CC),
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "待办清单 · Jetpack Compose",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "状态驱动 UI：勾选后计数与删除线同时变化",
                color = Color(0xFFAFC5D6),
                fontSize = 12.sp
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "今日进度",
                        color = Color(0xFF7A8794),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "已完成 $doneCount / ${todos.size}",
                        color = Color(0xFF1B3A57),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(modifier = Modifier.padding(vertical = 6.dp)) {
                    items(todos) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { checked ->
                                    val index = todos.indexOf(item)
                                    if (index >= 0) {
                                        todos[index] = item.copy(done = checked)
                                    }
                                }
                            )
                            Text(
                                text = item.title,
                                color = if (item.done) Color(0xFF9AA5AE) else Color(0xFF22272B),
                                fontSize = 14.sp,
                                textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("复习 LazyColumn", fontSize = 13.sp) }
                )
                Button(
                    onClick = {
                        val text = draft.trim()
                        if (text.isNotEmpty()) {
                            todos.add(TodoItem(text))
                            draft = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0FA3A3))
                ) {
                    Text("添加", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "提示：新增条目后列表会自动重排，这是 LazyColumn + 状态列表的默认表现。",
                color = Color(0xFF8B959E),
                fontSize = 11.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoPreview() {
    MaterialTheme { TodoScreen() }
}
