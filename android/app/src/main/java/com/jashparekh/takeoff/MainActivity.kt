package com.jashparekh.takeoff

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: DemoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent { TakeoffApp(vm) }
    }
}

object R0 {
    const val MAIN = "main"
    const val CREATE = "create"
    const val BUILDING = "building"
    const val UPLOAD = "upload"
    const val COVERAGE = "coverage"
    const val TARGET = "target"
    const val HISTORY = "history"
    const val READING = "reading"
    const val REVIEW = "review"
    const val FLOOR3D = "floor3d"
    const val SLAB = "slab"
    const val ISSUES = "issues"
    const val CONFIRM = "confirm"
    const val RESULTS = "results"
    const val CUTTING = "cutting"
    const val REVISION = "revision"
    const val CHECK = "check"
    const val PURCHASE = "purchase"
    const val RECON = "recon"
}

/** Go back one screen; if nothing is left, return to the projects list. */
fun NavHostController.back() {
    if (!popBackStack()) navigate(R0.MAIN)
}

@Composable
fun TakeoffApp(vm: DemoViewModel) {
    val nav = rememberNavController()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val toast: (String) -> Unit = { msg ->
        scope.launch {
            snack.currentSnackbarData?.dismiss()
            snack.showSnackbar(msg)
        }
    }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Orange,
            onPrimary = White,
            background = Bg,
            surface = White,
            onSurface = Ink,
            onBackground = Ink
        )
    ) {
        CompositionLocalProvider(LocalToast provides toast) {
            Box(Modifier.fillMaxSize().background(Bg).systemBarsPadding().imePadding()) {
                NavHost(
                    navController = nav,
                    startDestination = R0.MAIN,
                    enterTransition = { fadeIn(animationSpec = tween(160)) },
                    exitTransition = { fadeOut(animationSpec = tween(160)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(160)) },
                    popExitTransition = { fadeOut(animationSpec = tween(160)) }
                ) {
                    composable(R0.MAIN) { MainScreen(vm, nav) }
                    composable(R0.CREATE) { CreateProjectScreen(vm.s, nav) }
                    composable(
                        "${R0.BUILDING}/{idx}",
                        arguments = listOf(navArgument("idx") { type = NavType.IntType })
                    ) { e -> AddBuildingScreen(vm.s, nav, e.arguments?.getInt("idx") ?: 1) }
                    composable(R0.UPLOAD) { UploadScreen(vm.s, nav) }
                    composable(R0.COVERAGE) { CoverageScreen(vm.s, nav) }
                    composable(R0.TARGET) { TargetUploadScreen(vm.s, nav) }
                    composable(
                        "${R0.HISTORY}/{code}",
                        arguments = listOf(navArgument("code") { type = NavType.StringType })
                    ) { e -> DrawingHistoryScreen(nav, e.arguments?.getString("code") ?: "41-06") }
                    composable(R0.READING) { ReadingScreen(nav) }
                    composable(R0.REVIEW) { ReviewScreen(nav) }
                    composable(R0.FLOOR3D) { Floor3DScreen(nav) }
                    composable(R0.SLAB) { SlabDetailScreen(nav) }
                    composable(R0.ISSUES) { IssuesScreen(vm.s, nav) }
                    composable(R0.CONFIRM) { ConfirmScreen(vm.s, nav) }
                    composable(R0.RESULTS) { ResultsScreen(nav) }
                    composable(R0.CUTTING) { CuttingPlanScreen(nav) }
                    composable(R0.REVISION) { RevisionCompareScreen(nav) }
                    composable(R0.CHECK) { DrawingCheckScreen(nav) }
                    composable(R0.PURCHASE) { PurchaseListScreen(nav) }
                    composable(R0.RECON) { SiteReconScreen(nav) }
                }
                SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 84.dp))
            }
        }
    }
}
