package com.aim.app.presentation.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aim.app.R
import kotlinx.coroutines.launch

private enum class OnboardingPage(
    val emoji: String,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
) {
    CONCEPT("🎯", R.string.onboarding_concept_title, R.string.onboarding_concept_body),
    PERMISSIONS("🔔", R.string.onboarding_permissions_title, R.string.onboarding_permissions_body),
    FIRST_GOAL("🚀", R.string.onboarding_first_goal_title, R.string.onboarding_first_goal_body),
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages = OnboardingPage.entries
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* результат не критичен */ }

    val finish = {
        viewModel.completeOnboarding()
        onFinished()
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { index ->
                OnboardingPageContent(pages[index])
            }

            PageIndicator(count = pages.size, selected = pagerState.currentPage)
            Spacer(Modifier.height(24.dp))

            val isLast = pagerState.currentPage == pages.lastIndex
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = finish) {
                    Text(stringResource(R.string.onboarding_skip))
                }
                Button(
                    onClick = {
                        when {
                            pages[pagerState.currentPage] == OnboardingPage.PERMISSIONS &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                            isLast -> finish()
                            else -> scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                ) {
                    Text(
                        stringResource(
                            if (isLast) R.string.onboarding_start else R.string.onboarding_next,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(targetState = page, label = "OnboardingEmoji") { p ->
            Text(text = p.emoji, fontSize = 96.sp)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PageIndicator(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(if (index == selected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}
