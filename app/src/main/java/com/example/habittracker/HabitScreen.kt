package com.example.habittracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HabitScreen(modifier: Modifier = Modifier) {
    val habits = remember {
        mutableStateListOf(
            Habit(1, "Чтение", 30, "Образование", false, true, 2),
            Habit(2, "Программирование", 60, "Работа", true, true, 4),
            Habit(3, "Английский", 20, "Образование", false, true, 3),
            Habit(4, "Тренировка", 40, "Здоровье", true, false, 4),
            Habit(5, "Медитация", 10, "Здоровье", false, true, 1),
        )
    }
    var nextId by remember { mutableStateOf(6) }


    var showForm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Здоровье") }
    var required by remember { mutableStateOf(false) }
    var daily by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf(3f) }


    var filter by remember { mutableStateOf("Все") }


    val isValid = name.trim().isNotEmpty() &&
            (minutes.toIntOrNull()?.let { it > 0 } == true)

    val completed = habits.count { it.done }
    val total = habits.size
    val percent = if (total == 0) 0 else completed * 100 / total
    val doneMinutes = habits.filter { it.done }.sumOf { it.minutes }

    val displayed: List<Habit> = when (filter) {
        "Выполненные" -> habits.filter { it.done }
        "Невыполненные" -> habits.filter { !it.done }
        else -> habits
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = !showForm }) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "Форма привычки"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { HeaderRow(completed = completed, total = total) }
            item { StatsSurface(completed, total, percent, doneMinutes) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {

                        for (i in habits.indices) {
                            habits[i] = habits[i].copy(done = false)
                        }
                    }) { Text("Сбросить статистику") }
                }
            }

            item { FilterRow(filter = filter, onFilterChange = { filter = it }) }

            if (showForm) {
                item {
                    HabitForm(
                        name = name, onNameChange = { name = it },
                        minutes = minutes, onMinutesChange = { minutes = it },
                        category = category, onCategoryChange = { category = it },
                        required = required, onRequiredChange = { required = it },
                        daily = daily, onDailyChange = { daily = it },
                        difficulty = difficulty, onDifficultyChange = { difficulty = it },
                        isValid = isValid,
                        onAdd = {
                            habits.add(
                                Habit(
                                    id = nextId++,
                                    name = name.trim(),
                                    minutes = minutes.toInt(),
                                    category = category,
                                    required = required,
                                    daily = daily,
                                    difficulty = difficulty.toInt()
                                )
                            )
                            name = ""; minutes = ""
                            category = "Здоровье"
                            required = false; daily = false
                            difficulty = 3f
                        },
                        onClear = {
                            name = ""; minutes = ""
                            category = "Здоровье"
                            required = false; daily = false
                            difficulty = 3f
                        }
                    )
                }
            }

            if (displayed.isEmpty()) {
                item {
                    Text(
                        text = "Нет привычек для отображения",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(displayed, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onToggle = {
                            val index = habits.indexOfFirst { it.id == habit.id }
                            if (index >= 0) {
                                habits[index] = habits[index].copy(done = !habits[index].done)
                            }
                        },
                        onDelete = {
                            val index = habits.indexOfFirst { it.id == habit.id }
                            if (index >= 0) habits.removeAt(index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(completed: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.habit_logo),
            contentDescription = "Логотип трекера",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp).clip(CircleShape)
        )
        Column {
            Text("Трекер привычек", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Выполнено сегодня: $completed из $total",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsSurface(completed: Int, total: Int, percent: Int, doneMinutes: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(16.dp)
        ) {
            Text(
                "Прогресс",
                modifier = Modifier.align(Alignment.TopStart),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                "$percent%",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Выполнено: $completed из $total",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("$doneMinutes мин",
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun FilterRow(filter: String, onFilterChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Все", "Выполненные", "Невыполненные").forEach { option ->
            FilterChip(
                selected = filter == option,
                onClick = { onFilterChange(option) },
                label = { Text(option) }
            )
        }
        AssistChip(onClick = { onFilterChange("Все") },
            label = { Text("Сбросить фильтр") })
    }
}

@Composable
private fun HabitCard(habit: Habit, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.done)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = habit.done, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (habit.done) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "Выполнено",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = habit.name,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (habit.done) TextDecoration.LineThrough else null
                    )
                }
                Text(
                    "${habit.category} • ${habit.minutes} мин • сложность ${habit.difficulty}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (habit.required) BadgeLabel("Обязательная")
                    if (habit.daily) BadgeLabel("Ежедневно")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Удалить привычку"
                )
            }
        }
    }
}

@Composable
private fun BadgeLabel(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun HabitForm(
    name: String, onNameChange: (String) -> Unit,
    minutes: String, onMinutesChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    required: Boolean, onRequiredChange: (Boolean) -> Unit,
    daily: Boolean, onDailyChange: (Boolean) -> Unit,
    difficulty: Float, onDifficultyChange: (Float) -> Unit,
    isValid: Boolean,
    onAdd: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Новая привычка", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Название") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = name.isNotEmpty() && name.isBlank()
            )

            OutlinedTextField(
                value = minutes,
                onValueChange = { nv -> if (nv.all { it.isDigit() }) onMinutesChange(nv) },
                label = { Text("Время (минуты)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = minutes.isNotEmpty() &&
                        (minutes.toIntOrNull()?.let { it > 0 } != true),
                supportingText = {
                    if (minutes.isNotEmpty() &&
                        (minutes.toIntOrNull()?.let { it > 0 } != true)
                    ) Text("Введите положительное целое число")
                }
            )

            Text("Категория", fontWeight = FontWeight.Medium)
            listOf("Здоровье", "Образование", "Работа", "Отдых").forEach { cat ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = category == cat,
                        onClick = { onCategoryChange(cat) }
                    )
                    Text(cat)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = required, onCheckedChange = onRequiredChange)
                Spacer(Modifier.width(4.dp))
                Text("Обязательная")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = daily, onCheckedChange = onDailyChange)
                Spacer(Modifier.width(8.dp))
                Text("Ежедневно")
            }

            Text("Сложность: ${difficulty.toInt()}")
            Slider(
                value = difficulty, onValueChange = onDifficultyChange,
                valueRange = 1f..5f, steps = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAdd, enabled = isValid,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(12.dp)
                ) { Text("Добавить") }
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) { Text("Очистить") }
            }
        }
    }
}