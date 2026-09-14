/*
 * File Name: UserDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for user response.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class UserDto
{
    public string UserId { get; set; } = null!;
    public string? Nic { get; set; }
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string Phone { get; set; } = null!;
    public UserRole Role { get; set; }
    public AccountStatus AccountStatus { get; set; }
    public string Address { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
}
