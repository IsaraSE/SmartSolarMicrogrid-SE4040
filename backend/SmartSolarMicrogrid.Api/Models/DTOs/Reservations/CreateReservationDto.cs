/*
 * File Name: CreateReservationDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for creating a reservation.
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class CreateReservationDto
{
    [Required]
    public string StationId { get; set; } = null!;

    [Required]
    public string SlotId { get; set; } = null!;

    // Optional: Used when an Admin creates a reservation on behalf of a Prosumer
    public string? ProsumerNic { get; set; }
}
