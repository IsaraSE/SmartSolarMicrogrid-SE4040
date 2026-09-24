/*
 * File Name: QrVerificationResultDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of QrVerificationResultDto.cs
 * Date: 2026-09-15
 */

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class QrVerificationResultDto
{
    public bool Valid { get; set; }
    public string? Message { get; set; }
    public string? ReservationId { get; set; }
    public string? ReservationNumber { get; set; }
    public string? ProsumerNic { get; set; }
    public string? ProsumerName { get; set; }
    public string? StationId { get; set; }
    public string? StationName { get; set; }
    public string? SlotId { get; set; }
    public string? BookingDate { get; set; }
    public string? StartTime { get; set; }
    public string? EndTime { get; set; }
    public string? Status { get; set; }
}
