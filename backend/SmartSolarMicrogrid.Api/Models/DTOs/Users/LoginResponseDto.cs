/*
 * File Name: LoginResponseDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for login response containing JWT and user details.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class LoginResponseDto
{
    public string UserId { get; set; } = null!;
    public string FullName { get; set; } = null!;
    public UserRole Role { get; set; }
    public AccountStatus AccountStatus { get; set; }
    public string Token { get; set; } = null!;
}
