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

// --- SEED DEFAULT ADMIN & PROSUMER ---
using (var scope = app.Services.CreateScope())
{
    var userRepository = scope.ServiceProvider.GetRequiredService<SmartSolarMicrogrid.Api.Repositories.Users.IUserDetailsRepository>();
    var users = await userRepository.GetAllAsync();
    
    if (!users.Any(u => u.Email == "admin@smartsolar.com"))
    {
        var passwordHash = BCrypt.Net.BCrypt.HashPassword("admin123");
        var adminUser = new SmartSolarMicrogrid.Api.Models.Entities.Users.UserDetail
        {
            FullName = "Admin",
            Email = "admin@smartsolar.com",
            Phone = "0000000000",
            Address = "System",
            PasswordHash = passwordHash,
            Role = SmartSolarMicrogrid.Api.Models.Enums.Users.UserRole.BACKOFFICE,
            AccountStatus = SmartSolarMicrogrid.Api.Models.Enums.Users.AccountStatus.ACTIVE,
            CreatedAt = DateTime.UtcNow
        };
        await userRepository.CreateAsync(adminUser);
        Console.WriteLine("✅ Default admin user created! (Email: admin@smartsolar.com, Password: admin123)");
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
