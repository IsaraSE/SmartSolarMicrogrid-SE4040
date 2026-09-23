/*
 * File Name: UpdateReservationDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for updating a reservation.
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class UpdateReservationDto
{
    [Required]
    public string SlotId { get; set; } = null!;
    
    public string? Notes { get; set; }
}
