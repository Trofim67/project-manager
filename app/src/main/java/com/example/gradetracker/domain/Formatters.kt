package com.example.gradetracker.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ru = Locale("ru", "RU")
private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val monthFormat = DateTimeFormatter.ofPattern("LLLL yyyy", ru)

/** Единый формат среднего балла во всём приложении: 2 знака, запятая. */
fun formatAverage(value: Double?): String =
    if (value == null) "—" else String.format(ru, "%.2f", value)

fun formatDelta(value: Double?): String =
    if (value == null) "—" else String.format(ru, "%+.2f", value)

fun formatDate(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dateFormat)

fun formatMonth(month: YearMonth): String =
    month.format(monthFormat).replaceFirstChar { it.uppercase() }
