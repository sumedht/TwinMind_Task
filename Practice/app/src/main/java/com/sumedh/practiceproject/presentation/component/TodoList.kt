package com.sumedh.practiceproject.presentation.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumedh.practiceproject.presentation.viewmodel.TodoViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TodoList(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val todoState = viewModel.todoListState.value

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                title = {
                    Text(text = "Todo List")
                },
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn( modifier = Modifier
                .fillMaxWidth()
            ) {
                items(todoState.todos) { todo ->
                    TodoListItem(todo = todo)
                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}