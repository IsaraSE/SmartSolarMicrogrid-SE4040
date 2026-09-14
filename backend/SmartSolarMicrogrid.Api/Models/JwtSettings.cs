/*
 * File Name: JwtSettings.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Configuration settings for JWT Authentication.
 * Date: 2026-09-14
 */

namespace SmartSolarMicrogrid.Api.Models;

public class JwtSettings
{
    public string SecretKey { get; set; } = null!;
    public string Issuer { get; set; } = null!;
    public string Audience { get; set; } = null!;
    public int ExpiryMinutes { get; set; }
}
