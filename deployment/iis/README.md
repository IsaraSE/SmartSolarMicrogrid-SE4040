# IIS Deployment Guide – Smart Solar Microgrid Web API

This guide explains how the C# Web API is hosted on Windows IIS and how the
web and Android clients connect to it. Follow the steps in order.

## Architecture

| Component | Technology | Runs on | Address |
|---|---|---|---|
| Web API | ASP.NET Core Web API (C#, .NET 10) | IIS 10 on Windows | `http://localhost:5235` |
| Database | MongoDB | MongoDB Atlas (cloud) | `mongodb+srv` connection string |
| Web client | React + Vite + Tailwind CSS | Host machine | `http://localhost:5173` |
| Mobile client | Native Android (Kotlin) + SQLite | Android emulator | `http://10.0.2.2:5235/` |

All business logic is in the Web API (FAT service). Both clients call it over REST only.

## 1. Enable IIS

1. Open **Turn Windows features on or off**.
2. Tick **Internet Information Services**, including **IIS Management Console**.
3. Click **OK**. Check that `http://localhost` shows the IIS welcome page.

## 2. Install the ASP.NET Core Hosting Bundle

1. Download **ASP.NET Core Runtime 10.x – Windows Hosting Bundle** from
   https://dotnet.microsoft.com/download/dotnet/10.0
2. Install it. This adds the ASP.NET Core Module v2, which IIS needs to run the API.
3. From an **Administrator** Command Prompt, restart IIS:

```
iisreset
```

## 3. Publish the API

From an **Administrator** Command Prompt:

```
cd backend\SmartSolarMicrogrid.Api
dotnet publish -c Release -o C:\inetpub\SmartSolarApi
```

The publish step generates `web.config` in `C:\inetpub\SmartSolarApi`. IIS uses
this file to run the app through the ASP.NET Core Module, so it is not kept in
source control.

## 4. Configure the MongoDB connection (production)

IIS runs the app in the **Production** environment, where .NET User Secrets are
not loaded. Create `C:\inetpub\SmartSolarApi\appsettings.Production.json`:

```json
{
  "SmartSolarMicrogridDatabase": {
    "ConnectionString": "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/",
    "DatabaseName": "SmartSolarMicrogrid"
  }
}
```

- This file stays **only on the server**. Never commit it, because it contains the database password.
- In MongoDB Atlas, open **Security → Network Access** and allow the host machine's IP address.

## 5. Create the IIS site

In **IIS Manager**:

1. Right-click **Sites → Add Website…**
   - Site name: `SmartSolarApi`
   - Physical path: `C:\inetpub\SmartSolarApi`
   - Binding: type `http`, IP address `All Unassigned`, port `5235`, host name left empty
2. Open **Application Pools**, then **SmartSolarApi**, and set **.NET CLR version** to **No Managed Code**.
   ASP.NET Core uses its own runtime, not the .NET Framework CLR.
3. Make sure the site and the application pool both show **Started**.

## 6. Open the firewall port (for other devices)

Run from an Administrator Command Prompt:

```
netsh advfirewall firewall add rule name="SmartSolar API" dir=in action=allow protocol=TCP localport=5235
```

## 7. Verify the deployment

| Test | Expected result |
|---|---|
| Open `http://localhost:5235/api/stations` | JSON with `"success": true` and the station list from MongoDB |
| Open `http://<host-ip>:5235/api/stations` from another device on the same network | Same JSON response |
| Stop the site in IIS Manager and refresh the URL | Request fails, which confirms IIS is serving the API |
| Log in to the web client | Dashboard loads live data |
| Log in to the Android app | Stations and bookings load |
| Create a booking on Android | The booking appears on the web client's Reservations page |

## 8. Client configuration

**Web client:** `web/SmartSolarMicrogrid.Web/src/services/api.js`

```js
const API_BASE_URL = 'http://127.0.0.1:5235/api';
```

The API's CORS policy (`AllowReactApp` in `Program.cs`) allows `http://localhost:5173`.
If the web client is served from a different address, add that address to `WithOrigins(...)`.

**Android client:** `RetrofitClient.kt`

```kotlin
private const val BASE_URL = "http://10.0.2.2:5235/"   // Android emulator → host PC
// Physical device: use the host PC's LAN IP from ipconfig, e.g. "http://192.168.1.5:5235/"
```

The manifest sets `usesCleartextTraffic="true"` so the app can call the local HTTP endpoint.

## 9. Running the full system

1. **API:** IIS starts automatically with Windows. Check that `http://localhost:5235/api/stations` returns data.
2. **Web client:**

   ```
   cd web\SmartSolarMicrogrid.Web
   npm install
   npm run dev
   ```

   Then open `http://localhost:5173/login`.
3. **Android:** open `android/SmartSolarMicrogridMobile` in Android Studio, start an emulator (Pixel 7, API 35), select the `app` configuration and click **Run**.

## Redeploying after backend changes

1. In IIS Manager, stop the **SmartSolarApi** site.
2. Run `dotnet publish -c Release -o C:\inetpub\SmartSolarApi` again.
3. Start the site again.

The `appsettings.Production.json` file is not overwritten by publishing.

## Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| HTTP Error 500.19 | Hosting Bundle is missing | Install the Hosting Bundle, then run `iisreset` |
| HTTP Error 500.30 | The app failed to start, usually because MongoDB is unreachable | Check `appsettings.Production.json` and Atlas Network Access |
| Phone cannot reach the API | Firewall or network isolation | Check the firewall rule, and make sure both devices are on the same network |