using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Middlewares;
using SmartSolarMicrogrid.Api.Repositories;
using SmartSolarMicrogrid.Api.Services;

using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.Configure<SmartSolarMicrogridDatabaseSettings>(
    builder.Configuration.GetSection("SmartSolarMicrogridDatabase"));

builder.Services.Configure<JwtSettings>(
    builder.Configuration.GetSection("JwtSettings"));

builder.Services.AddSingleton<IMongoClient>(sp =>
{
    var settings = builder.Configuration.GetSection("SmartSolarMicrogridDatabase").Get<SmartSolarMicrogridDatabaseSettings>();
    return new MongoClient(settings?.ConnectionString);
});

// Register Repositories
builder.Services.AddScoped(typeof(IBaseRepository<>), typeof(BaseRepository<>));
builder.Services.AddScoped<IUserDetailsRepository, UserDetailsRepository>();
builder.Services.AddScoped<ISolarStationInfoRepository, SolarStationInfoRepository>();
builder.Services.AddScoped<IEnergyBookingSlotRepository, EnergyBookingSlotRepository>();
builder.Services.AddScoped<IEnergyReservationRepository, EnergyReservationRepository>();

// Register Services
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IProsumerService, ProsumerService>();
builder.Services.AddScoped<IStationService, StationService>();
builder.Services.AddScoped<ISlotService, SlotService>();
builder.Services.AddScoped<IReservationService, ReservationService>();
builder.Services.AddScoped<IDashboardService, DashboardService>();

// Configure JWT Authentication
var jwtSettings = builder.Configuration.GetSection("JwtSettings").Get<JwtSettings>();
var key = Encoding.ASCII.GetBytes(jwtSettings?.SecretKey ?? "fallback_secret_key");

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = jwtSettings?.Issuer,
        ValidAudience = jwtSettings?.Audience,
        IssuerSigningKey = new SymmetricSecurityKey(key)
    };
});

// Configure CORS for React frontend
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowReactApp", policy =>
    {
        policy.WithOrigins("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174")
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });
});

builder.Services.AddAuthorization();
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new System.Text.Json.Serialization.JsonStringEnumConverter());
    });

// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();
var app = builder.Build();

app.UseGlobalExceptionHandling();

// --- SEED DEFAULT BACKOFFICE & PROSUMER ---
using (var scope = app.Services.CreateScope())
{
    var userRepository = scope.ServiceProvider.GetRequiredService<SmartSolarMicrogrid.Api.Repositories.Users.IUserDetailsRepository>();
    var users = await userRepository.GetAllAsync();
    
    if (!users.Any(u => u.Email == "backoffice@smartsolar.com"))
    {
        var passwordHash = BCrypt.Net.BCrypt.HashPassword("backoffice123");
        var backofficeUser = new SmartSolarMicrogrid.Api.Models.Entities.Users.UserDetail
        {
            FullName = "Backoffice User",
            Email = "backoffice@smartsolar.com",
            Phone = "0000000000",
            Address = "System",
            PasswordHash = passwordHash,
            Role = SmartSolarMicrogrid.Api.Models.Enums.Users.UserRole.BACKOFFICE,
            AccountStatus = SmartSolarMicrogrid.Api.Models.Enums.Users.AccountStatus.ACTIVE,
            CreatedAt = DateTime.UtcNow
        };
        await userRepository.CreateAsync(backofficeUser);
        Console.WriteLine("✅ Default backoffice user created! (Email: backoffice@smartsolar.com, Password: backoffice123)");
    }

    if (!users.Any(u => u.Email == "john@example.com"))
    {
        var prosumerUser = new SmartSolarMicrogrid.Api.Models.Entities.Users.UserDetail
        {
            Nic = "123456789V",
            FullName = "John Doe (Prosumer)",
            Email = "john@example.com",
            Phone = "0771234567",
            Address = "Colombo",
            PasswordHash = BCrypt.Net.BCrypt.HashPassword("password123"),
            Role = SmartSolarMicrogrid.Api.Models.Enums.Users.UserRole.PROSUMER,
            AccountStatus = SmartSolarMicrogrid.Api.Models.Enums.Users.AccountStatus.ACTIVE,
            CreatedAt = DateTime.UtcNow
        };
        await userRepository.CreateAsync(prosumerUser);
        Console.WriteLine("✅ Default prosumer created! (Email: john@example.com, Password: password123)");
    }

    // Seed 5 Pending Prosumers
    for (int i = 1; i <= 5; i++)
    {
        string email = $"pending{i}@example.com";
        if (!users.Any(u => u.Email == email))
        {
            var pendingUser = new SmartSolarMicrogrid.Api.Models.Entities.Users.UserDetail
            {
                Nic = $"80000000{i}V",
                FullName = $"Pending User {i}",
                Email = email,
                Phone = $"071000000{i}",
                Address = $"City {i}",
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("password123"),
                Role = SmartSolarMicrogrid.Api.Models.Enums.Users.UserRole.PROSUMER,
                AccountStatus = SmartSolarMicrogrid.Api.Models.Enums.Users.AccountStatus.PENDING,
                CreatedAt = DateTime.UtcNow.AddDays(-i)
            };
            await userRepository.CreateAsync(pendingUser);
        }
    }

    // Seed 5 Deactivated Prosumers
    for (int i = 1; i <= 5; i++)
    {
        string email = $"deactivated{i}@example.com";
        if (!users.Any(u => u.Email == email))
        {
            var deactivatedUser = new SmartSolarMicrogrid.Api.Models.Entities.Users.UserDetail
            {
                Nic = $"90000000{i}V",
                FullName = $"Deactivated User {i}",
                Email = email,
                Phone = $"072000000{i}",
                Address = $"Town {i}",
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("password123"),
                Role = SmartSolarMicrogrid.Api.Models.Enums.Users.UserRole.PROSUMER,
                AccountStatus = SmartSolarMicrogrid.Api.Models.Enums.Users.AccountStatus.DEACTIVATED,
                CreatedAt = DateTime.UtcNow.AddDays(-(i + 10))
            };
            await userRepository.CreateAsync(deactivatedUser);
        }
    }

    // --- SEED STATIONS, SLOTS & SAMPLE RESERVATIONS ---
    var stationRepository = scope.ServiceProvider.GetRequiredService<SmartSolarMicrogrid.Api.Repositories.Stations.ISolarStationInfoRepository>();
    var slotRepository = scope.ServiceProvider.GetRequiredService<SmartSolarMicrogrid.Api.Repositories.Stations.IEnergyBookingSlotRepository>();
    var reservationRepository = scope.ServiceProvider.GetRequiredService<SmartSolarMicrogrid.Api.Repositories.Reservations.IEnergyReservationRepository>();

    var existingStations = await stationRepository.GetAllAsync();
    if (!existingStations.Any())
    {
        var stationsSeed = new[]
        {
            new { Name = "Colombo Central Hub", Address = "Union Place, Colombo 02", Lat = 6.9214, Lng = 79.8590, Cap = 250.0, Slots = 8 },
            new { Name = "Kandy Solar Station", Address = "Peradeniya Rd, Kandy", Lat = 7.2906, Lng = 80.6337, Cap = 180.0, Slots = 6 },
            new { Name = "Galle Coastal Grid", Address = "Fort, Galle", Lat = 6.0329, Lng = 80.2168, Cap = 150.0, Slots = 5 }
        };

        var createdStationIds = new List<string>();
        foreach (var s in stationsSeed)
        {
            var stationId = MongoDB.Bson.ObjectId.GenerateNewId().ToString();
            createdStationIds.Add(stationId);
            await stationRepository.CreateAsync(new SmartSolarMicrogrid.Api.Models.Entities.Stations.SolarStationInfo
            {
                StationId = stationId,
                StationName = s.Name,
                Address = s.Address,
                Latitude = s.Lat,
                Longitude = s.Lng,
                Capacity = s.Cap,
                BatterySlotCount = s.Slots,
                OperatingStartTime = "08:00",
                OperatingEndTime = "18:00",
                Description = $"{s.Name} - solar energy trading hub.",
                Status = SmartSolarMicrogrid.Api.Models.Enums.Stations.StationStatus.ACTIVE
            });

            // Two AVAILABLE slots per day for the next 3 days (within the 7-day booking window).
            for (int day = 1; day <= 3; day++)
            {
                var baseDate = DateTime.UtcNow.Date.AddDays(day);
                var slotTimes = new[] { (Start: 9, End: 11), (Start: 14, End: 16) };
                foreach (var t in slotTimes)
                {
                    await slotRepository.CreateAsync(new SmartSolarMicrogrid.Api.Models.Entities.Stations.EnergyBookingSlot
                    {
                        SlotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                        SlotName = $"{baseDate:yyyy-MM-dd} {t.Start:00}:00-{t.End:00}:00",
                        StationId = stationId,
                        StartDateTime = baseDate.AddHours(t.Start),
                        EndDateTime = baseDate.AddHours(t.End),
                        Status = SmartSolarMicrogrid.Api.Models.Enums.Stations.SlotStatus.AVAILABLE
                    });
                }
            }
        }

        // Sample reservations for the default prosumer (NIC 123456789V) so the mobile
        // Pending / Current / History screens all have data to show.
        const string prosumerNic = "123456789V";
        var mainStationId = createdStationIds[0];

        async Task SeedReservation(
            DateTime start,
            SmartSolarMicrogrid.Api.Models.Enums.Reservations.ReservationStatus status,
            SmartSolarMicrogrid.Api.Models.Enums.Stations.SlotStatus slotStatus)
        {
            var slotId = MongoDB.Bson.ObjectId.GenerateNewId().ToString();
            await slotRepository.CreateAsync(new SmartSolarMicrogrid.Api.Models.Entities.Stations.EnergyBookingSlot
            {
                SlotId = slotId,
                SlotName = $"{start:yyyy-MM-dd HH:mm} (booked)",
                StationId = mainStationId,
                StartDateTime = start,
                EndDateTime = start.AddHours(2),
                Status = slotStatus,
                ReservedBy = prosumerNic
            });

            var now = DateTime.UtcNow;
            await reservationRepository.CreateAsync(new SmartSolarMicrogrid.Api.Models.Entities.Reservations.EnergyReservation
            {
                ReservationId = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
                ReservationNumber = "RES-" + Random.Shared.Next(10000, 99999),
                ProsumerNic = prosumerNic,
                StationId = mainStationId,
                SlotId = slotId,
                ScheduledStartDateTime = start,
                ScheduledEndDateTime = start.AddHours(2),
                Status = status,
                QrReference = Guid.NewGuid().ToString("N"),
                CreatedAt = now,
                UpdatedAt = now,
                CompletedAt = status == SmartSolarMicrogrid.Api.Models.Enums.Reservations.ReservationStatus.COMPLETED ? now : (DateTime?)null
            });
        }

        await SeedReservation(DateTime.UtcNow.Date.AddDays(2).AddHours(10),
            SmartSolarMicrogrid.Api.Models.Enums.Reservations.ReservationStatus.PENDING,
            SmartSolarMicrogrid.Api.Models.Enums.Stations.SlotStatus.RESERVED);
        await SeedReservation(DateTime.UtcNow.Date.AddDays(3).AddHours(10),
            SmartSolarMicrogrid.Api.Models.Enums.Reservations.ReservationStatus.APPROVED,
            SmartSolarMicrogrid.Api.Models.Enums.Stations.SlotStatus.RESERVED);
        await SeedReservation(DateTime.UtcNow.Date.AddDays(-5).AddHours(10),
            SmartSolarMicrogrid.Api.Models.Enums.Reservations.ReservationStatus.COMPLETED,
            SmartSolarMicrogrid.Api.Models.Enums.Stations.SlotStatus.UNAVAILABLE);

        Console.WriteLine("✅ Seeded 3 stations, available slots, and 3 sample reservations for NIC 123456789V.");
    }

    // --- MIGRATION: BACKFILL CAPACITIES FOR EXISTING SLOTS ---
    var allSlots = await slotRepository.GetAllAsync();
    var random = new Random();
    var possibleCapacities = new[] { 5.0, 10.0, 15.0, 20.0 };
    int updatedCount = 0;
    
    foreach (var slot in allSlots)
    {
        if (slot.Capacity == 0)
        {
            slot.Capacity = possibleCapacities[random.Next(possibleCapacities.Length)];
            await slotRepository.UpdateAsync(slot.SlotId, slot);
            updatedCount++;
        }
    }
    
    if (updatedCount > 0)
    {
        Console.WriteLine($"✅ Successfully backfilled {updatedCount} existing slots with random capacities.");
    }
    
    // --- MIGRATION: BACKFILL NOTES FOR EXISTING RESERVATIONS ---
    var allReservations = await reservationRepository.GetAllAsync();
    var possibleNotes = new[] { "Need full charge", "Selling excess energy", "Urgent battery issue", "Standard charge", "Selling 5kWh" };
    int updatedResCount = 0;
    
    foreach (var res in allReservations)
    {
        if (string.IsNullOrEmpty(res.Notes))
        {
            res.Notes = possibleNotes[random.Next(possibleNotes.Length)];
            await reservationRepository.UpdateAsync(res.ReservationId, res);
            updatedResCount++;
        }
    }
    
    if (updatedResCount > 0)
    {
        Console.WriteLine($"✅ Successfully backfilled {updatedResCount} existing reservations with random notes.");
    }
}
// -------------------------------

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseCors("AllowReactApp");
app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
