/*
 * File Name: LoginRequestDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for login request.
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class LoginRequestDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = null!;

    [Required]
    public string Password { get; set; } = null!;
}
