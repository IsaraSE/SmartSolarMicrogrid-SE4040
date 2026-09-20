import os
import re

path = "/Users/isara/4Y_2S/EAD/SmartSolarMicrogrid-SE4040/android/SmartSolarMicrogridMobile/app/src/main/java/com/smartsolarmicrogrid/prosumer/ui/navigation/NavGraph.kt"
with open(path, "r") as f:
    content = f.read()

# Replace the exact block
old_block = """        composable(Screen.Dashboard.route) {
            WithBottomBar(navController, Screen.Dashboard.route) {
                DashboardScreen(
                    onViewAllBookings = { navController.navigate(Screen.BookingList.route) },
                    onBack = null
                )
            }
        }"""

new_block = """        composable(Screen.Dashboard.route) {
            WithBottomBar(navController, Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToStations = { navController.navigate(Screen.Stations.route) },
                    onNavigateToBookings = { navController.navigate(Screen.BookingList.route) },
                    onNavigateToMap = { navController.navigate(Screen.Stations.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
        }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    print("Replaced exact block")
else:
    print("Exact block not found! Trying regex.")
    pattern = re.compile(r'composable\(Screen\.Dashboard\.route\) \{\s*WithBottomBar\(navController, Screen\.Dashboard\.route\) \{\s*DashboardScreen\([^)]*\)\s*\}\s*\}', re.MULTILINE)
    content = pattern.sub(new_block.strip(), content)

with open(path, "w") as f:
    f.write(content)
print("Done")
