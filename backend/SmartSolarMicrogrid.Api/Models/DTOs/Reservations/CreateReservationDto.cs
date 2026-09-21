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

    [Required]
    public string BookingDate { get; set; } = null!;

    [Required(ErrorMessage = "Start time is required.")]
    public string StartTime { get; set; } = null!;

    public string? Notes { get; set; }

    // Optional: Used when an Admin creates a reservation on behalf of a Prosumer
    public string? ProsumerNic { get; set; }
}
