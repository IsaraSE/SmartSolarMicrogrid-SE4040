/*
 * File Name: ChangePasswordDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for changing an authenticated user's password.
 * Date: 2026-09-19
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class ChangePasswordDto
{
    [Required]
    public string CurrentPassword { get; set; } = null!;

    [Required]
    [MinLength(8)]
    public string NewPassword { get; set; } = null!;
}
