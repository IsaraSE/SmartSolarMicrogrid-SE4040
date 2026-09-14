/*
 * File Name: UpdateReservationStatusDto.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Data transfer object for admin updating a reservation status.
 * Date: 2026-09-14
 */

using System.ComponentModel.DataAnnotations;
using SmartSolarMicrogrid.Api.Models.Enums;

namespace SmartSolarMicrogrid.Api.Models.DTOs.Reservations;

public class UpdateReservationStatusDto
{
    [Required]
    public ReservationStatus Status { get; set; }
}
