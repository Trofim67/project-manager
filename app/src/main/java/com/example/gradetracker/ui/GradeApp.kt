package com.example.gradetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private object Routes {
    const val HOME = "home"
    const val DISCIPLINE = "discipline/{disciplineId}"
    const val DISCIPLINE_FORM = "disciplineForm/{disciplineId}" // 0 = новая
    const val GRADE_FORM = "gradeForm/{disciplineId}/{gradeId}" // gradeId 0 = новая
    const val DYNAMICS = "dynamics"
    const val CALCULATOR = "calculator"

    fun discipline(id: Long) = "discipline/$id"
    fun disciplineForm(id: Long) = "disciplineForm/$id"
    fun gradeForm(disciplineId: Long, gradeId: Long) = "gradeForm/$disciplineId/$gradeId"
}

private val longArg = { name: String -> navArgument(name) { type = NavType.LongType } }

@Composable
fun GradeApp(vm: GradesViewModel = viewModel()) {
    val nav = rememberNavController()
    val data by vm.data.collectAsStateWithLifecycle()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                data = data,
                onOpenDiscipline = { nav.navigate(Routes.discipline(it)) },
                onAddDiscipline = { nav.navigate(Routes.disciplineForm(0)) },
                onEditDiscipline = { nav.navigate(Routes.disciplineForm(it)) },
                onDeleteDiscipline = vm::deleteDiscipline,
                onDynamics = { nav.navigate(Routes.DYNAMICS) },
                onCalculator = { nav.navigate(Routes.CALCULATOR) },
                onLoadSample = vm::loadSampleData,
                onClearAll = vm::clearAll
            )
        }
        composable(Routes.DISCIPLINE, arguments = listOf(longArg("disciplineId"))) { entry ->
            val id = entry.arguments?.getLong("disciplineId") ?: 0L
            DisciplineScreen(
                disciplineId = id,
                data = data,
                onBack = { nav.popBackStack() },
                onAddGrade = { nav.navigate(Routes.gradeForm(id, 0)) },
                onEditGrade = { nav.navigate(Routes.gradeForm(id, it)) },
                onDeleteGrade = vm::deleteGrade
            )
        }
        composable(Routes.DISCIPLINE_FORM, arguments = listOf(longArg("disciplineId"))) { entry ->
            DisciplineFormScreen(
                disciplineId = entry.arguments?.getLong("disciplineId") ?: 0L,
                data = data,
                onSave = { vm.saveDiscipline(it); nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.GRADE_FORM, arguments = listOf(longArg("disciplineId"), longArg("gradeId"))) { entry ->
            GradeFormScreen(
                disciplineId = entry.arguments?.getLong("disciplineId") ?: 0L,
                gradeId = entry.arguments?.getLong("gradeId") ?: 0L,
                data = data,
                onSave = { vm.saveGrade(it); nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.DYNAMICS) { DynamicsScreen(data) { nav.popBackStack() } }
        composable(Routes.CALCULATOR) { CalculatorScreen(data) { nav.popBackStack() } }
    }
}
