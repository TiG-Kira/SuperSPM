# Debug Session: app-crash

## Session Info
- **Session ID**: app-crash
- **Status**: [FIXED]
- **Created**: 2026-07-03
- **Symptom**: APP crashes immediately on launch (闪退)
- **Environment**: Android
- **Regression Window**: After recent UI changes to SpeedometerScreen, DetailScreen, HistoryScreen

## Root Cause Analysis

### Confirmed Issues:

1. **StatCard fillMaxWidth in Row** (H3)
   - **Problem**: `StatCard`, `HistoryStatCard`, `DetailStatCard` all used `.fillMaxWidth()` modifier
   - When placed inside a `Row`, this caused layout conflicts as each card tried to occupy full width
   - **Fix**: Removed `.fillMaxWidth()` from all three card components

2. **SpeedometerGauge for-loop with .align()** (H1)
   - **Problem**: Using `.align(Alignment.Center)` inside a for-loop within a `Box` is invalid
   - `.align()` can only be used once per direct child in a `Box`
   - **Fix**: Replaced the for-loop with individual Text elements positioned using padding

3. **SpeedChart division by zero** (H2)
   - **Problem**: `xScale = chartWidth / (points.size - 1)` would cause division by zero when `points.size == 1`
   - **Fix**: Added guard conditions: `if (points.size > 1) chartWidth / (points.size - 1).toFloat() else 0f`

4. **SpeedometerGauge nativeCanvas crash** (H1 - Additional)
   - **Problem**: Using `drawContext.canvas.nativeCanvas` in Compose Canvas caused runtime crash
   - **Fix**: Rewrote gauge to use Compose `Box` with `Align` modifiers for text labels instead of native canvas

## Additional Fixes Applied

5. **Permission dialog "Go to Settings" button**
   - **Problem**: Clicking "去授权" button didn't navigate to system settings page
   - **Fix**: Changed to open `ACTION_APPLICATION_DETAILS_SETTINGS` intent to navigate to app's system settings

6. **Permission refresh after granting**
   - **Problem**: Permission status wasn't refreshed after returning from system settings
   - **Fix**: Added `onResume()` override in MainActivity to recheck permissions

7. **Navigation bar not showing on detail/about/openSource pages**
   - **Problem**: Bottom navigation bar only showed on speedometer/history/settings pages
   - **Fix**: Added "detail", "about", "openSource" routes to the navigation bar visibility check

8. **History/detail page stat cards overlapping**
   - **Problem**: Cards had no fixed width, causing them to overlap or not display properly
   - **Fix**: Added fixed width to `HistoryStatCard` (70dp) and `DetailStatCard` (80dp)

9. **Position refresh timer always running**
   - **Problem**: Position refresh timer ran even when not recording
   - **Fix**: Modified `LaunchedEffect` to only run timer when status is NOT_STARTED

## Fixes Applied

| File | Issue | Fix |
|------|-------|-----|
| [SpeedometerScreen.kt](file:///d:/Local%20Datas/Sources/SuperSPM-By-Kira/SuperSPM/app/src/main/java/com/kira/superspm/ui/SpeedometerScreen.kt) | StatCard.fillMaxWidth() in Row | Removed fillMaxWidth |
| [SpeedometerScreen.kt](file:///d:/Local%20Datas/Sources/SuperSPM-By-Kira/SuperSPM/app/src/main/java/com/kira/superspm/ui/SpeedometerScreen.kt) | for-loop with .align() in Box | Replaced with individual Text elements |
| [HistoryScreen.kt](file:///d:/Local%20Datas/Sources/SuperSPM-By-Kira/SuperSPM/app/src/main/java/com/kira/superspm/ui/HistoryScreen.kt) | HistoryStatCard.fillMaxWidth() in Row | Removed fillMaxWidth |
| [DetailScreen.kt](file:///d:/Local%20Datas/Sources/SuperSPM-By-Kira/SuperSPM/app/src/main/java/com/kira/superspm/ui/DetailScreen.kt) | DetailStatCard.fillMaxWidth() in Row | Removed fillMaxWidth |
| [DetailScreen.kt](file:///d:/Local%20Datas/Sources/SuperSPM-By-Kira/SuperSPM/app/src/main/java/com/kira/superspm/ui/DetailScreen.kt) | SpeedChart division by zero | Added guard conditions |

## Verification
- Build successful: `BUILD SUCCESSFUL`
- No compilation errors

## Notes
None
