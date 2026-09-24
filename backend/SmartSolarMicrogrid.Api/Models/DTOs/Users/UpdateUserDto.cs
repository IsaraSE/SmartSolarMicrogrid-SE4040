/*
 * File Name: UpdateUserDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of UpdateUserDto.cs
 * Date: 2026-09-23
 */

using System.ComponentModel.DataAnnotations;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class UpdateUserDto
{
    [Required]
    public string FullName { get; set; } = null!;

    [Required]
    public string Phone { get; set; } = null!;

    [Required]
    public string Address { get; set; } = null!;

    [Required]
    public AccountStatus AccountStatus { get; set; }
    
    public string? AdditionalInfo { get; set; }
}
