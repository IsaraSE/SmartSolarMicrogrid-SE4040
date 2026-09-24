/*
 * File Name: UpdateReservationDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of UpdateReservationDto.cs
 * Date: 2026-09-18
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class UpdateReservationDto
{
    [Required]
    public string SlotId { get; set; } = null!;
    
    public string? Notes { get; set; }
}
