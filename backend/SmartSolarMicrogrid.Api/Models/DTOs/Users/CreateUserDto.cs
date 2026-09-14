/*
 * File Name: CreateUserDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for creating a user (Backoffice/Grid Operator).
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class CreateUserDto
{
    [Required]
    public string FullName { get; set; } = null!;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = null!;

    [Required]
    public string Phone { get; set; } = null!;

    [Required]
    [MinLength(6)]
    public string Password { get; set; } = null!;

    [Required]
    public UserRole Role { get; set; }

    [Required]
    public string Address { get; set; } = null!;
}
