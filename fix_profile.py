import os

screen_path = "/Users/isara/4Y_2S/EAD/SmartSolarMicrogrid-SE4040/android/SmartSolarMicrogridMobile/app/src/main/java/com/smartsolarmicrogrid/prosumer/ui/profile/ProfileScreen.kt"

with open(screen_path, 'r') as f:
    content = f.read()

target = """                        OutlinedButton(
                            onClick = { showDeactivateDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Request Deactivation")
                        }"""

new_btn = """                        OutlinedButton(
                            onClick = { profileViewModel.logout(onDone = onDeactivated) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Logout")
                        }"""

content = content.replace(target, new_btn)

with open(screen_path, 'w') as f:
    f.write(content)

print("Fixed")
