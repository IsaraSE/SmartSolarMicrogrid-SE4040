/*
 * File Name: ReservationDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for reservation response.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class ReservationDto
{
    public string ReservationId { get; set; } = null!;
    public string ProsumerNic { get; set; } = null!;
    public string StationId { get; set; } = null!;
    public string SlotId { get; set; } = null!;
    public DateTime ScheduledStartDateTime { get; set; }
    public DateTime ScheduledEndDateTime { get; set; }
    public ReservationStatus Status { get; set; }
    public string QrReference { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
}
