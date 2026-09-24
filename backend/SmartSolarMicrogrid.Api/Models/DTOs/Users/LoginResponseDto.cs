/*
 * File Name: LoginResponseDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of LoginResponseDto.cs
 * Date: 2026-09-24
 */

using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class LoginResponseDto
{
    public string UserId { get; set; } = null!;
    public string Nic { get; set; } = null!;
    public string FullName { get; set; } = null!;
    public UserRole Role { get; set; }
    public AccountStatus AccountStatus { get; set; }
    public string Token { get; set; } = null!;
}
