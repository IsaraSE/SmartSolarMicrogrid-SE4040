/*
 * File Name: UpdateProsumerDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Sewmi
 * Description: Data transfer object for a prosumer updating their own editable profile fields.
 * Date: 2026-09-19
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Users;

public class UpdateProsumerDto
{
    [Required]
    public string FullName { get; set; } = null!;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = null!;

    [Required]
    public string Phone { get; set; } = null!;

    [Required]
    public string Address { get; set; } = null!;
}
