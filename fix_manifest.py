import os

path = "/Users/isara/4Y_2S/EAD/SmartSolarMicrogrid-SE4040/android/SmartSolarMicrogridMobile/app/src/main/AndroidManifest.xml"
with open(path, "r") as f:
    content = f.read()

if "com.google.android.geo.API_KEY" not in content:
    meta_tag = '        <meta-data android:name="com.google.android.geo.API_KEY" android:value="mock-api-key" />\n'
    content = content.replace("<application", "<application\n" + meta_tag)
    with open(path, "w") as f:
        f.write(content)
    print("Added dummy API key to AndroidManifest.xml")
else:
    print("API key already present.")
